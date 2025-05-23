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

import org.eclipse.digitaltwin.fa3st.service.messagebus.MessageBusConfig;


/**
 * Configuration class for {@link MessageBusInternal}.
 */
public class MessageBusInternalConfig extends MessageBusConfig<MessageBusInternal> {

    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends MessageBusInternalConfig, B extends AbstractBuilder<T, B>>
            extends MessageBusConfig.AbstractBuilder<MessageBusInternal, T, B> {

    }

    public static class Builder extends AbstractBuilder<MessageBusInternalConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected MessageBusInternalConfig newBuildingInstance() {
            return new MessageBusInternalConfig();
        }
    }
}
