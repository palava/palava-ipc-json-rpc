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
 * Tests {@link JsonRpc2Protocol#supports(Map)}.
 *
 * @author Willi Schoenborn
 */
public final class JsonRpc2ProtocolSupportsTest implements UnitProvider<MapProtocol> {

    @Override
    public MapProtocol unit() {
        return Palava.newFramework().getInstance(JsonRpc2Protocol.class);
    }
    
    /**
     * Tests {@link JsonRpc2Protocol#supports(Map)} with valid values.
     */
    @Test
    public void supports() {
        Assert.assertTrue(unit().supports(ImmutableMap.of(
            "jsonrpc", "2.0",
            "method", Echo.class.getName(),
            "params", Collections.emptyList(),
            "id", System.nanoTime()
        )));
        Assert.assertTrue(unit().supports(ImmutableMap.of(
            "jsonrpc", "2.0",
            "method", Echo.class.getName(),
            "params", Collections.emptyMap(),
            "id", System.nanoTime()
        )));
        Assert.assertTrue(unit().supports(ImmutableMap.<String, Object>of(
            "jsonrpc", "2.0",
            "method", Echo.class.getName(),
            "id", System.nanoTime()
        )));
        Assert.assertTrue(unit().supports(ImmutableMap.of(
            "jsonrpc", "2.0",
            "method", Echo.class.getName(),
            "params", Collections.emptyList()
        )));
        Assert.assertTrue(unit().supports(ImmutableMap.of(
            "jsonrpc", "2.0",
            "method", Echo.class.getName(),
            "params", Collections.emptyMap()
        )));
        Assert.assertTrue(unit().supports(ImmutableMap.<String, Object>of(
            "jsonrpc", "2.0",
            "method", Echo.class.getName()
        )));
    }
    
    /**
     * Tests {@link JsonRpc2Protocol#supports(Map)} with invalid values.
     */
    @Test
    public void doesNotSupport() {
        // jsonrpc missing
        Assert.assertFalse(unit().supports(ImmutableMap.of(
            "method", Echo.class.getName(),
            "params", Collections.emptyList(),
            "id", System.nanoTime()
        )));
        Assert.assertFalse(unit().supports(ImmutableMap.of(
            "method", Echo.class.getName(),
            "params", Collections.emptyMap(),
            "id", System.nanoTime()
        )));
        // method missing
        Assert.assertFalse(unit().supports(ImmutableMap.of(
            "jsonrpc", "2.0",
            "params", Collections.emptyList(),
            "id", System.nanoTime()
        )));
        Assert.assertFalse(unit().supports(ImmutableMap.of(
            "jsonrpc", "2.0",
            "params", Collections.emptyMap(),
            "id", System.nanoTime()
        )));
    }
    
}
