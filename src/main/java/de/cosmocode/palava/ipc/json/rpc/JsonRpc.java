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

/**
 * Static constants used by the Json-RPC protocol.
 *
 * @author Willi Schoenborn
 */
final class JsonRpc {

    public static final String JSON_RPC = "jsonrpc";
    
    public static final String METHOD = "method";
    public static final String PARAMS = "params";
    public static final String ID = "id";
    
    public static final String RESULT = "result";
    
    public static final String ERROR = "error";
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String DATA = "data";

    private JsonRpc() {
        
    }

}
