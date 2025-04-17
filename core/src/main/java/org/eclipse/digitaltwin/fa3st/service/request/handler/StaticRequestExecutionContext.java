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
public class StaticRequestExecutionContext implements RequestExecutionContext {

    private final AssetConnectionManager assetConnectionManager;
    private final CoreConfig coreConfig;
    private final Endpoint endpoint;
    private final FileStorage fileStorage;
    private final MessageBus messageBus;
    private final Persistence persistence;

    public StaticRequestExecutionContext(CoreConfig coreConfig,
            Persistence persistence,
            FileStorage fileStorage,
            MessageBus messageBus,
            AssetConnectionManager assetConnectionManager,
            Endpoint endpoint) {
        this.coreConfig = coreConfig;
        this.persistence = persistence;
        this.fileStorage = fileStorage;
        this.messageBus = messageBus;
        this.assetConnectionManager = assetConnectionManager;
        this.endpoint = endpoint;
    }


    public StaticRequestExecutionContext(CoreConfig coreConfig,
            Persistence persistence,
            FileStorage fileStorage,
            MessageBus messageBus,
            AssetConnectionManager assetConnectionManager) {
        this(coreConfig, persistence, fileStorage, messageBus, assetConnectionManager, null);
    }


    @Override
    public AssetConnectionManager getAssetConnectionManager() {
        return assetConnectionManager;
    }


    @Override
    public CoreConfig getCoreConfig() {
        return coreConfig;
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
        return fileStorage;
    }


    @Override
    public MessageBus getMessageBus() {
        return messageBus;
    }


    @Override
    public Persistence<?> getPersistence() {
        return persistence;
    }


    @Override
    public StaticRequestExecutionContext withEndpoint(Endpoint endpoint) {
        return new StaticRequestExecutionContext(coreConfig, persistence, fileStorage, messageBus, assetConnectionManager, endpoint);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StaticRequestExecutionContext that = (StaticRequestExecutionContext) o;
        return Objects.equals(assetConnectionManager, that.assetConnectionManager)
                && Objects.equals(coreConfig, that.coreConfig)
                && Objects.equals(endpoint, that.endpoint)
                && Objects.equals(fileStorage, that.fileStorage)
                && Objects.equals(messageBus, that.messageBus)
                && Objects.equals(persistence, that.persistence);
    }


    @Override
    public int hashCode() {
        return Objects.hash(assetConnectionManager, coreConfig, endpoint, fileStorage, messageBus, persistence);
    }
}
