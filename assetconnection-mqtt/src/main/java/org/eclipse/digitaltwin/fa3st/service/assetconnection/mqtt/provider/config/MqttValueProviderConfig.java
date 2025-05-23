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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.mqtt.provider.config;

import java.util.Objects;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.provider.config.AbstractMultiFormatValueProviderConfig;


/**
 * * Config file for MQTT-based {@link org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetValueProvider}.
 */
public class MqttValueProviderConfig extends AbstractMultiFormatValueProviderConfig {

    private String topic;

    public String getTopic() {
        return topic;
    }


    public void setTopic(String topic) {
        this.topic = topic;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MqttValueProviderConfig that = (MqttValueProviderConfig) o;
        return super.equals(that)
                && Objects.equals(topic, that.topic);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), topic);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends MqttValueProviderConfig, B extends AbstractBuilder<T, B>>
            extends AbstractMultiFormatValueProviderConfig.AbstractBuilder<T, B> {

        public B topic(String value) {
            getBuildingInstance().setTopic(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<MqttValueProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected MqttValueProviderConfig newBuildingInstance() {
            return new MqttValueProviderConfig();
        }
    }
}
