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
package org.eclipse.digitaltwin.fa3st.service.request.handler;

import java.util.Objects;
import org.eclipse.digitaltwin.fa3st.service.Service;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetConnectionManager;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;
import org.eclipse.digitaltwin.fa3st.service.endpoint.Endpoint;
import org.eclipse.digitaltwin.fa3st.service.filestorage.FileStorage;
import org.eclipse.digitaltwin.fa3st.service.messagebus.MessageBus;
import org.eclipse.digitaltwin.fa3st.service.persistence.Persistence;


/**
 * Immutable wrapper class containing access to all relevant information of a Service to execute a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.Request}.
 */
public class DynamicRequestExecutionContext implements RequestExecutionContext {

    private final Endpoint endpoint;
    private final Service service;

    public DynamicRequestExecutionContext(Service service, Endpoint endpoint) {
        this.service = service;
        this.endpoint = endpoint;
    }


    public DynamicRequestExecutionContext(Service service) {
        this(service, null);
    }


    @Override
    public AssetConnectionManager getAssetConnectionManager() {
        return service.getAssetConnectionManager();
    }


    @Override
    public CoreConfig getCoreConfig() {
        return service.getConfig().getCore();
    }


    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }


    @Override
    public boolean hasEndpoint() {
        return Objects.nonNull(endpoint);
    }


    @Override
    public FileStorage getFileStorage() {
        return service.getFileStorage();
    }


    @Override
    public MessageBus getMessageBus() {
        return service.getMessageBus();
    }


    @Override
    public Persistence<?> getPersistence() {
        return service.getPersistence();
    }


    @Override
    public DynamicRequestExecutionContext withEndpoint(Endpoint endpoint) {
        return new DynamicRequestExecutionContext(service, endpoint);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DynamicRequestExecutionContext that = (DynamicRequestExecutionContext) o;
        return Objects.equals(endpoint, that.endpoint)
                && Objects.equals(service, that.service);
    }


    @Override
    public int hashCode() {
        return Objects.hash(endpoint, service);
    }
}
