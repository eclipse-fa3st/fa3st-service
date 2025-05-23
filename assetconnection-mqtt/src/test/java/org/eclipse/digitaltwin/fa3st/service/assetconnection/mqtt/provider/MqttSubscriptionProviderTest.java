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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.mqtt.provider;

import java.util.UUID;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;


public class MqttSubscriptionProviderTest {

    @Test
    public void testEquals() throws MqttException {
        MqttClient client1 = new MqttClient("tcp://foo.org", UUID.randomUUID().toString(), new MemoryPersistence());//new MqttClient(config.getServerUri(), config.getClientId(), new MemoryPersistence());
        MqttClient client2 = new MqttClient("tcp://bar.org", UUID.randomUUID().toString(), new MemoryPersistence());
        EqualsVerifier.simple().forClass(MqttSubscriptionProvider.class)
                .withPrefabValues(MqttClient.class, client1, client2)
                .verify();
    }

}
