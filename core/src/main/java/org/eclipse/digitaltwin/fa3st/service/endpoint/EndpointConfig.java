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
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;
import org.eclipse.digitaltwin.fa3st.common.model.ServiceSpecificationProfile;
import org.eclipse.digitaltwin.fa3st.service.config.Config;


/**
 * Generic endpoint configuration. When implementing a custom endpoint inherit from this class to create a custom
 * configuration.
 *
 * @param <T> type of the endpoint
 */
public class EndpointConfig<T extends Endpoint> extends Config<T> {

    protected List<ServiceSpecificationProfile> profiles = ServiceSpecificationProfile.ALL;

    public List<ServiceSpecificationProfile> getProfiles() {
        return profiles;
    }


    public void setProfiles(List<ServiceSpecificationProfile> profiles) {
        this.profiles = profiles;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EndpointConfig that = (EndpointConfig) o;
        return Objects.equals(profiles, that.profiles);
    }


    @Override
    public int hashCode() {
        return Objects.hash(profiles);
    }

    /**
     * Abstract builder class that should be used for builders of inheriting classes.
     *
     * @param <T> type of the endpoint of the config to build
     * @param <C> type of the config to build
     * @param <B> type of this builder, needed for inheritance builder pattern
     */
    public abstract static class AbstractBuilder<T extends Endpoint, C extends EndpointConfig<T>, B extends AbstractBuilder<T, C, B>> extends ExtendableBuilder<C, B> {
        public B profiles(List<ServiceSpecificationProfile> value) {
            getBuildingInstance().setProfiles(value);
            return getSelf();
        }


        public B profile(ServiceSpecificationProfile value) {
            getBuildingInstance().getProfiles().add(value);
            return getSelf();
        }
    }

    /**
     * Builder for EndpointConfig class.
     *
     * @param <T> type of the endpoint of the config to build
     */
    public static class Builder<T extends Endpoint> extends AbstractBuilder<T, EndpointConfig<T>, Builder<T>> {

        @Override
        protected Builder<T> getSelf() {
            return this;
        }


        @Override
        protected EndpointConfig<T> newBuildingInstance() {
            return new EndpointConfig<>();
        }

    }
}
