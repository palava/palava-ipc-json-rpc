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

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.inject.internal.Maps;

import de.cosmocode.palava.ipc.IpcArguments;
import de.cosmocode.palava.ipc.IpcCall;
import de.cosmocode.palava.ipc.IpcConnection;
import de.cosmocode.palava.scope.AbstractScopeContext;

/**
 * Json-RPC implementation of the {@link IpcCall} interface. 
 *
 * @author Willi Schoenborn
 */
final class JsonRpcCall extends AbstractScopeContext implements IpcCall {

    private Map<Object, Object> context;
    
    private final IpcArguments arguments;
    
    private final IpcConnection connection;
    
    public JsonRpcCall(IpcArguments arguments, IpcConnection connection) {
        this.arguments = Preconditions.checkNotNull(arguments, "Arguments");
        this.connection = Preconditions.checkNotNull(connection, "Connection");
    }
    
    @Override
    protected Map<Object, Object> context() {
        if (context == null) {
            context = Maps.newHashMap();
        }
        return context;
    }

    @Override
    public IpcArguments getArguments() {
        return arguments;
    }

    @Override
    public IpcConnection getConnection() {
        return connection;
    }

}
