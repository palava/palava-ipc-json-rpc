/**
 * Copyright 2010 CosmoCode GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.cosmocode.palava.ipc.json.rpc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.internal.Maps;

import de.cosmocode.collections.Procedure;
import de.cosmocode.palava.core.Registry;
import de.cosmocode.palava.core.Registry.Key;
import de.cosmocode.palava.core.lifecycle.Disposable;
import de.cosmocode.palava.core.lifecycle.Initializable;
import de.cosmocode.palava.core.lifecycle.LifecycleException;
import de.cosmocode.palava.ipc.IpcArguments;
import de.cosmocode.palava.ipc.IpcCall;
import de.cosmocode.palava.ipc.IpcCallCreateEvent;
import de.cosmocode.palava.ipc.IpcCallDestroyEvent;
import de.cosmocode.palava.ipc.IpcCallScope;
import de.cosmocode.palava.ipc.IpcCommandExecutionException;
import de.cosmocode.palava.ipc.IpcCommandExecutor;
import de.cosmocode.palava.ipc.IpcConnection;
import de.cosmocode.palava.ipc.IpcConnectionDestroyEvent;
import de.cosmocode.palava.ipc.IpcSession;
import de.cosmocode.palava.ipc.IpcSessionProvider;
import de.cosmocode.palava.ipc.json.Json;
import de.cosmocode.palava.ipc.protocol.DetachedConnection;
import de.cosmocode.palava.ipc.protocol.MapProtocol;
import de.cosmocode.palava.ipc.protocol.Protocol;
import de.cosmocode.palava.ipc.protocol.ProtocolException;

/**
 * Implementation of the {@link JsonIpcConnectorMapHandler} interface which
 * uses the Json-RPC 1.0 protocol as defined by:
 * <a href="http://json-rpc.org/wiki/specification">http://json-rpc.org/</a>.
 *
 * @author Willi Schoenborn
 */
final class JsonRpcProtocol extends MapProtocol implements IpcConnectionDestroyEvent, Initializable, Disposable {

    private static final Logger LOG = LoggerFactory.getLogger(JsonRpcProtocol.class);
    
    private static final String METHOD_ERROR = String.format("%s must be a string", JsonRpc.METHOD);
    private static final String PARAMS_ERROR = String.format("%s must be an array", JsonRpc.PARAMS);
    
    private static final ImmutableSet<String> KEYS = ImmutableSet.of(JsonRpc.METHOD, JsonRpc.PARAMS, JsonRpc.ID);
    
    private static final UUID IDENTIFIER = UUID.randomUUID();
    private static final String IDENTIFIER_VALUE = "Json-RPC 1.0";
    
    private final Registry registry;
    
    private final IpcSessionProvider sessionProvider;
    
    private final IpcCommandExecutor commandExecutor;
    
    private final IpcCallScope scope;
    
    @Inject
    public JsonRpcProtocol(Registry registry, IpcSessionProvider sessionProvider,
        IpcCommandExecutor commandExecutor, IpcCallScope scope) {
    
        this.registry = Preconditions.checkNotNull(registry, "Registry");
        this.sessionProvider = Preconditions.checkNotNull(sessionProvider, "SessionProvider");
        this.commandExecutor = Preconditions.checkNotNull(commandExecutor, "CommandExecutor");
        this.scope = Preconditions.checkNotNull(scope, "Scope");
    }
    
    @Override
    public void initialize() throws LifecycleException {
        registry.register(Key.get(Protocol.class, Json.class), this);
        registry.register(IpcConnectionDestroyEvent.class, this);
    }
    
    @Override
    public boolean supports(Map<?, ?> request) {
        return request.size() == KEYS.size() && request.keySet().containsAll(KEYS);
    }
    
    @Override
    public Object process(Map<?, ?> request, DetachedConnection connection) throws ProtocolException {
        LOG.trace("Processing json-rpc 1.0 call: {}", request);
        
        final Object untypedMethod = request.get(JsonRpc.METHOD);
        
        final String method;
        if (untypedMethod instanceof String) {
            method = String.class.cast(untypedMethod);
            LOG.trace("Requested method: {}", method);
        } else {
            throw new ProtocolException(METHOD_ERROR);
        }
        
        final Object untypedParams = request.get(JsonRpc.PARAMS);
        
        final List<?> params;
        if (untypedParams instanceof List<?>) {
            params = List.class.cast(untypedParams);
            LOG.trace("Incoming params: {}", params);
        } else {
            throw new ProtocolException(PARAMS_ERROR);
        }
        
        final Object id = request.get(JsonRpc.ID);
        LOG.trace("Call id: {}", id);
        
        final IpcArguments arguments = new JsonRpcArguments(params);
        
        final IpcSession session = sessionProvider.getSession(null, null);
        connection.attachTo(session);
        connection.set(IDENTIFIER, IDENTIFIER_VALUE);
        final IpcCall call = new JsonRpcCall(arguments, connection);
        
        registry.notify(IpcCallCreateEvent.class, new Procedure<IpcCallCreateEvent>() {
            
            @Override
            public void apply(IpcCallCreateEvent input) {
                input.eventIpcCallCreate(call);
            }
            
        });
        
        scope.enter(call);
        
        try {
            final Map<String, Object> result;
            
            try {
                result = commandExecutor.execute(method, call);
            /*CHECKSTYLE:OFF*/
            } catch (RuntimeException e) {
            /*CHECKSTYLE:ON*/
                throw new IpcCommandExecutionException(e);
            } finally {
                registry.notifySilent(IpcCallDestroyEvent.class, new Procedure<IpcCallDestroyEvent>() {
                    
                    @Override
                    public void apply(IpcCallDestroyEvent input) {
                        input.eventIpcCallDestroy(call);
                    }
                    
                });
            }
            if (id == null) {
                LOG.trace("Request was notification, returning no result");
                return Protocol.NO_RESPONSE;
            } else {
                LOG.trace("Returning {}", result);
                final Map<String, Object> returnValue = Maps.newHashMap();
                returnValue.put(JsonRpc.RESULT, result);
                returnValue.put(JsonRpc.ERROR, null);
                returnValue.put(JsonRpc.ID, id);
                return returnValue;
            }
        } catch (IpcCommandExecutionException e) {
            final Map<String, Object> returnValue = Maps.newHashMap();
            returnValue.put(JsonRpc.RESULT, null);
            returnValue.put(JsonRpc.ERROR, e);
            returnValue.put(JsonRpc.ID, id);
            return returnValue;
        } finally {
            scope.exit();
        }
    }
    
    @Override
    public Object onError(Throwable t, Map<?, ?> request) {
        final Map<String, Object> returnValue = Maps.newHashMap();
        returnValue.put(JsonRpc.RESULT, null);
        returnValue.put(JsonRpc.ERROR, t);
        returnValue.put(JsonRpc.ID, request.get(JsonRpc.ID));
        return returnValue;
    }
    
    @Override
    public void eventIpcConnectionDestroy(IpcConnection connection) {
        final String identifier = connection.get(IDENTIFIER);
        if (identifier == null) return;
        if (identifier.equals(IDENTIFIER_VALUE)) {
            connection.getSession().clear();
        }
    }
    
    @Override
    public void dispose() throws LifecycleException {
        registry.remove(this);
    }
    
}
