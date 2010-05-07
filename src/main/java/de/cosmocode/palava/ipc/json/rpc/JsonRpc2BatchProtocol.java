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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import de.cosmocode.palava.ipc.protocol.DetachedConnection;
import de.cosmocode.palava.ipc.protocol.ListProtocol;
import de.cosmocode.palava.ipc.protocol.ProtocolException;

final class JsonRpc2BatchProtocol extends ListProtocol {

    private static final Logger LOG = LoggerFactory.getLogger(JsonRpc2BatchProtocol.class);

    private final JsonRpcProtocol protocol;
    
    private final Predicate<Object> supports = new Predicate<Object>() {
        
        @Override
        public boolean apply(Object input) {
            return protocol.supports(input);
        }
        
    };
    
    @Inject
    public JsonRpc2BatchProtocol(JsonRpcProtocol protocol) {
        this.protocol = Preconditions.checkNotNull(protocol, "Protocol");
    }

    @Override
    public boolean supports(List<?> request) {
        return Iterables.all(request, supports);
    }

    @Override
    public Object process(List<?> request, final DetachedConnection connection) throws ProtocolException {
        LOG.trace("Handling json-rpc batch call: {}", request);
        return Lists.newArrayList(Lists.transform(request, new Function<Object, Object>() {
            
            @Override
            public Object apply(Object from) {
                try {
                    return protocol.process(from, connection);
                } catch (RuntimeException e) {
                    return protocol.onError(e, from);
                } catch (ProtocolException e) {
                    return protocol.onError(e, from);
                }
            }
            
        }));
    }

    @Override
    public Object onError(final Throwable t, List<?> request) {
        return ErrorCode.INTERNAL_ERROR.getResponse(null, t);
    }

}
