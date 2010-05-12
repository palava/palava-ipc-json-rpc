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

import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import de.cosmocode.junit.UnitProvider;
import de.cosmocode.palava.core.Palava;
import de.cosmocode.palava.ipc.protocol.MapProtocol;

/**
 * Tests {@link JsonRpcProtocol#supports(Map)}.
 *
 * @author Willi Schoenborn
 */
public final class JsonRpcProtocolSupportsTest implements UnitProvider<MapProtocol> {

    @Override
    public MapProtocol unit() {
        return Palava.newFramework().getInstance(JsonRpcProtocol.class);
    }
    
    /**
     * Tests {@link JsonRpcProtocol#supports(Map)} with valid values.
     */
    @Test
    public void supports() {
        Assert.assertTrue(unit().supports(ImmutableMap.of(
            "method", Echo.class.getName(),
            "params", Collections.emptyList(),
            "id", System.nanoTime()
        )));
    }
    
    /**
     * Tests {@link JsonRpcProtocol#supports(Map)} with invalid values.
     */
    @Test
    public void doesNotSupport() {
        Assert.assertFalse(unit().supports(ImmutableMap.of(
            "params", Collections.emptyList(),
            "id", System.nanoTime()
        )));
        Assert.assertFalse(unit().supports(ImmutableMap.<String, Object>of(
            "method", Echo.class.getName(),
            "id", System.nanoTime()
        )));
        Assert.assertFalse(unit().supports(ImmutableMap.of(
            "method", Echo.class.getName(),
            "params", Collections.emptyList()
        )));
    }
    
}
