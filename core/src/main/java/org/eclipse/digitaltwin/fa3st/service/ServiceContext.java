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
package org.eclipse.digitaltwin.fa3st.service;

import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.api.Request;
import org.eclipse.digitaltwin.fa3st.common.model.api.Response;
import org.eclipse.digitaltwin.fa3st.common.typing.TypeInfo;
import org.eclipse.digitaltwin.fa3st.service.endpoint.Endpoint;
import org.eclipse.digitaltwin.fa3st.service.messagebus.MessageBus;


/**
 * Abstraction of Service to be used in other components to limit their access to the service.
 */
public interface ServiceContext {

    /**
     * Provides type information about an element identified by reference.
     *
     * @param reference reference identifying the element
     * @return type information of the referenced element, empty
     *         {@link org.eclipse.digitaltwin.fa3st.common.typing.ContainerTypeInfo} if no matching type is found, null if
     *         reference is null
     * @throws ResourceNotFoundException if reference can not be resolved on AAS environment of the service
     * @throws PersistenceException if storage error occurs
     */
    public TypeInfo getTypeInfo(Reference reference) throws ResourceNotFoundException, PersistenceException;


    /**
     * Executes a request.
     *
     * @param <T> type of expected response
     * @param source the endpoint via which the request has been triggered
     * @param request request to execute
     * @return result of executing the request
     */
    public <T extends Response> T execute(Endpoint source, Request<T> request);


    /**
     * Execute a request without context of an endpoint. This is typically used when executed for custom code.
     *
     * @param <T> type of expected response
     * @param request The request to execute.
     * @return the corresponding response
     */
    public default <T extends Response> T execute(Request<T> request) {
        return execute(null, request);
    }


    /**
     * Get a copied version of the Environment instance of the service.
     *
     * @return a deep copied Environment instance of the service
     */
    public Environment getAASEnvironment() throws PersistenceException;


    /**
     * Returns the message bus of the service.
     *
     * @return the message bus of the service
     */
    public MessageBus getMessageBus();


    /**
     * Returns the output variables of an operation identified by a reference.
     *
     * @param reference the reference identifying the operation
     * @return output variables of the operation identified by the reference
     * @throws ResourceNotFoundException if reference cannot be resolved or does not point to an operation
     * @throws IllegalArgumentException if reference is null
     * @throws IllegalArgumentException if reference cannot be resolved
     * @throws IllegalArgumentException if reference does not point to an operation
     */
    public OperationVariable[] getOperationOutputVariables(Reference reference) throws ResourceNotFoundException, PersistenceException;


    /**
     * Checks if an element is backed by a {@link org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetValueProvider}.
     *
     * @param reference the reference to the element
     * @return true if element is backed by a
     *         {@link org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetValueProvider}, otherwise false
     */
    public boolean hasValueProvider(Reference reference);
}
