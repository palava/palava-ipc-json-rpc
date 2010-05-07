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
