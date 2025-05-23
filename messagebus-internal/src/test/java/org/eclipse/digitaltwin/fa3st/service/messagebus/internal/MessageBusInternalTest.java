/**
 * Copyright (c) 2025 the Eclipse FA³ST Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.digitaltwin.fa3st.service.messagebus.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.EventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.SubscriptionInfo;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ChangeEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ValueChangeEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.error.ErrorEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.error.ErrorLevel;
import org.eclipse.digitaltwin.fa3st.common.model.value.PropertyValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.primitive.IntValue;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class MessageBusInternalTest {

    private static ValueChangeEventMessage valueChangeMessage;
    private static ErrorEventMessage errorMessage;
    // default timeout in milliseconds
    private static final long DEFAULT_TIMEOUT = 1000;

    private static final Reference property1Reference = new DefaultReference.Builder()
            .keys(new DefaultKey.Builder()
                    .type(KeyTypes.PROPERTY)
                    .value("property1")
                    .build())
            .build();

    @BeforeClass
    public static void init() {
        valueChangeMessage = new ValueChangeEventMessage();
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.setValue(new IntValue(100));
        valueChangeMessage.setOldValue(propertyValue);
        propertyValue.setValue(new IntValue(123));
        valueChangeMessage.setNewValue(propertyValue);

        errorMessage = new ErrorEventMessage();
        errorMessage.setElement(property1Reference);
        errorMessage.setLevel(ErrorLevel.ERROR);
    }


    @Test
    public void testExactTypeSubscription() throws InterruptedException, MessageBusException {
        MessageBusInternal messageBus = new MessageBusInternal();
        messageBus.start();
        CountDownLatch condition = new CountDownLatch(1);
        final AtomicReference<EventMessage> response = new AtomicReference<>();
        messageBus.subscribe(SubscriptionInfo.create(
                ValueChangeEventMessage.class,
                x -> {
                    response.set(x);
                    condition.countDown();
                }));
        messageBus.publish(valueChangeMessage);
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(valueChangeMessage, response.get());
        messageBus.stop();
    }


    @Test
    public void testSuperTypeSubscription() throws InterruptedException {
        MessageBusInternal messageBus = new MessageBusInternal();
        messageBus.start();
        Set<EventMessage> messages = Set.of(valueChangeMessage, errorMessage);
        Set<EventMessage> responses = Collections.synchronizedSet(new HashSet<>());
        CountDownLatch condition = new CountDownLatch(messages.size());
        messageBus.subscribe(SubscriptionInfo.create(
                EventMessage.class,
                x -> {
                    responses.add(x);
                    condition.countDown();
                }));
        messages.forEach(x -> {
            try {
                messageBus.publish(x);
            }
            catch (Exception e) {
                Assert.fail();
            }
        });
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(messages, responses);
        messageBus.stop();
    }


    @Test
    public void testDistinctTypesSubscription() throws InterruptedException {
        MessageBusInternal messageBus = new MessageBusInternal();
        messageBus.start();
        Map<Class<? extends EventMessage>, Set<EventMessage>> messages = Map.of(
                ChangeEventMessage.class, Set.of(valueChangeMessage),
                ErrorEventMessage.class, Set.of(errorMessage));
        Map<Class<? extends EventMessage>, Set<EventMessage>> responses = Collections.synchronizedMap(Map.of(
                ChangeEventMessage.class, new HashSet<>(),
                ErrorEventMessage.class, new HashSet<>()));
        CountDownLatch condition = new CountDownLatch(messages.values().stream().mapToInt(x -> x.size()).sum());
        responses.entrySet().forEach(entry -> messageBus.subscribe(
                SubscriptionInfo.create(
                        entry.getKey(),
                        x -> {
                            entry.getValue().add(x);
                            condition.countDown();
                        })));
        messages.values().stream().flatMap(x -> x.stream()).forEach(x -> {
            try {
                messageBus.publish(x);
            }
            catch (Exception e) {
                Assert.fail();
            }
        });
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(messages, responses);
        messageBus.stop();
    }


    @Test
    public void testNotMatchingSubscription() throws InterruptedException {
        MessageBusInternal messageBus = new MessageBusInternal();
        messageBus.start();
        CountDownLatch condition = new CountDownLatch(1);
        messageBus.subscribe(SubscriptionInfo.create(
                ErrorEventMessage.class,
                x -> {
                    Assert.fail();
                    condition.countDown();
                }));
        try {
            messageBus.publish(valueChangeMessage);
        }
        catch (Exception e) {
            Assert.fail();
        }
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        messageBus.stop();
    }


    @Test
    public void testSubscribeUnsubscribe() throws InterruptedException {
        MessageBusInternal messageBus = new MessageBusInternal();
        messageBus.start();
        CountDownLatch condition = new CountDownLatch(1);
        final AtomicReference<EventMessage> response = new AtomicReference<>();
        messageBus.unsubscribe(messageBus.subscribe(SubscriptionInfo.create(
                ValueChangeEventMessage.class,
                x -> {
                    response.set(x);
                    condition.countDown();
                })));
        try {
            messageBus.publish(valueChangeMessage);
        }
        catch (Exception e) {
            Assert.fail();
        }
        Assert.assertFalse(condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS));
        messageBus.stop();
    }
}
