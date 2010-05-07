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

/**
 * Defines all Json-RPC 2.0 error codes.
 *
 * @author Willi Schoenborn
 */
public enum ErrorCode {

    /**
     * Invalid JSON. An error occurred on the server while parsing the JSON text.
     */
    PARSE_ERROR(32700),
    
    /**
     * The received JSON is not a valid JSON-RPC Request.
     */
    INVALID_REQUEST(32600),
    
    /**
     * The requested remote-procedure does not exist / is not available.
     */
    METHOD_NOT_FOUND(32601),
    
    /**
     * Invalid method parameters.
     */
    INVALID_PARAMS(32602),
    
    /**
     * Internal JSON-RPC error.
     */
    INTERNAL_ERROR(32603);
    
    private int code;
    
    private ErrorCode(int code) {
        this.code = code;
    }
    
    /**
     * Creates an error response based on this error code and the
     * specified id.
     * 
     * @param id the request/response id
     * @param e the exception
     * @return a new response map
     * @throws NullPointerException if e is null
     */
    public Map<String, Object> getResponse(Object id, Throwable e) {
        Preconditions.checkNotNull(e, "Exception");
        final Map<String, Object> response = Maps.newHashMap();
        
        final Map<String, Object> error = Maps.newHashMap();
        
        error.put(JsonRpc.CODE, code);
        error.put(JsonRpc.MESSAGE, e.getMessage());
        error.put(JsonRpc.DATA, e);
        
        response.put(JsonRpc.ERROR, error);
        response.put(JsonRpc.ID, id);
        
        return response;
    }
    
    /**
     * Creates an error response based on this error code, the specified id
     * and message.
     * 
     * @param id the request/response id
     * @param message the error message
     * @return a new response map
     * @throws NullPointerException if message is null
     */
    public Map<String, Object> getResponse(Object id, String message) {
        Preconditions.checkNotNull(message, "Message");
        final Map<String, Object> response = Maps.newHashMap();
        
        final Map<String, Object> error = Maps.newHashMap();
        
        error.put(JsonRpc.CODE, code);
        error.put(JsonRpc.MESSAGE, message);
        
        response.put(JsonRpc.ERROR, error);
        response.put(JsonRpc.ID, id);
        
        return response;
    }
    
}
