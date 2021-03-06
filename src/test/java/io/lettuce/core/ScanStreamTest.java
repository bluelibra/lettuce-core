/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.lettuce.core;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import io.lettuce.core.api.reactive.RedisReactiveCommands;

/**
 * @author Mark Paluch
 */
public class ScanStreamTest extends AbstractRedisClientTest {

    @Test
    public void shouldScanIteratively() {

        for (int i = 0; i < 1000; i++) {
            redis.set("key-" + i, value);
        }
        ScanIterator<String> scan = ScanIterator.scan(redis);
        List<String> list = Flux.fromIterable(() -> scan).collectList().block();

        RedisReactiveCommands<String, String> reactive = redis.getStatefulConnection().reactive();

        StepVerifier.create(ScanStream.scan(reactive, ScanArgs.Builder.limit(200)).take(250)).expectNextCount(250)
                .verifyComplete();
        StepVerifier.create(ScanStream.scan(reactive)).expectNextSequence(list).verifyComplete();
    }

    @Test
    public void shouldHscanIteratively() {

        for (int i = 0; i < 1000; i++) {
            redis.hset(key, "field-" + i, "value-" + i);
        }

        RedisReactiveCommands<String, String> reactive = redis.getStatefulConnection().reactive();

        StepVerifier.create(ScanStream.hscan(reactive, key, ScanArgs.Builder.limit(200)).take(250)).expectNextCount(250)
                .verifyComplete();
        StepVerifier.create(ScanStream.hscan(reactive, key)).expectNextCount(1000).verifyComplete();
    }

    @Test
    public void shouldSscanIteratively() {

        for (int i = 0; i < 1000; i++) {
            redis.sadd(key, "value-" + i);
        }

        RedisReactiveCommands<String, String> reactive = redis.getStatefulConnection().reactive();

        AtomicInteger ai = new AtomicInteger();
        StepVerifier.create(ScanStream.sscan(reactive, key, ScanArgs.Builder.limit(200)), 0).thenRequest(250)
                .expectNextCount(250).thenCancel().verify();
        StepVerifier.create(ScanStream.sscan(reactive, key).count()).expectNext(1000L).verifyComplete();
    }

    @Test
    public void shouldZscanIteratively() {

        for (int i = 0; i < 1000; i++) {
            redis.zadd(key, (double) i, "value-" + i);
        }

        RedisReactiveCommands<String, String> reactive = redis.getStatefulConnection().reactive();

        StepVerifier.create(ScanStream.zscan(reactive, key, ScanArgs.Builder.limit(200)).take(250)).expectNextCount(250)
                .verifyComplete();
        StepVerifier.create(ScanStream.zscan(reactive, key)).expectNextCount(1000).verifyComplete();
    }
}
