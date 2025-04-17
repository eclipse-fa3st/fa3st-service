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
public interface RequestExecutionContext {

    public AssetConnectionManager getAssetConnectionManager();


    public CoreConfig getCoreConfig();


    public Endpoint getEndpoint();


    public FileStorage getFileStorage();


    public MessageBus getMessageBus();


    public Persistence<?> getPersistence();


    /**
     * Returns is an endpoint is present in the context, i.e., if a request has been made via and endpoint or not.
     *
     * @return true if the request has been made via and endpoint, false otherwise
     */
    public boolean hasEndpoint();


    /**
     * Creates a new copy of this execution context with the provided endpoint.
     *
     * @param endpoint the endpoint to use
     * @return new instance of the execution context with endpoint set
     */
    public RequestExecutionContext withEndpoint(Endpoint endpoint);
}
