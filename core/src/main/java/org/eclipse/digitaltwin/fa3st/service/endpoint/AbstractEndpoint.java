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
package org.eclipse.digitaltwin.fa3st.service.endpoint;

import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.fa3st.common.model.ServiceSpecificationProfile;
import org.eclipse.digitaltwin.fa3st.common.util.Ensure;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;


/**
 * Abstract base class for {@link Endpoint} implementations.
 *
 * @param <T> type of the configuration
 */
public abstract class AbstractEndpoint<T extends EndpointConfig> implements Endpoint<T> {

    protected T config;
    protected ServiceContext serviceContext;

    @Override
    public T asConfig() {
        return config;
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException is config is null
     */
    @Override
    public void init(CoreConfig coreConfig, T config, ServiceContext serviceContext) {
        Ensure.requireNonNull(config, "config must be non-null");
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        this.config = config;
        this.serviceContext = serviceContext;
    }


    @Override
    public List<ServiceSpecificationProfile> getProfiles() {
        return config.getProfiles();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractEndpoint<T> that = (AbstractEndpoint<T>) o;
        return Objects.equals(config, that.config)
                && Objects.equals(serviceContext, that.serviceContext);
    }


    @Override
    public int hashCode() {
        return Objects.hash(config, serviceContext);
    }
}
