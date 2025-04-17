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
import org.eclipse.digitaltwin.fa3st.common.exception.EndpointException;
import org.eclipse.digitaltwin.fa3st.common.model.ServiceSpecificationProfile;
import org.eclipse.digitaltwin.fa3st.service.config.Configurable;


/**
 * An Endpoint is an implementation of the interfaces and methods described by Part 2 of the AAS specification. It is
 * also often called upper DT interface.
 *
 * @param <T> type of the corresponding configuration class
 */
public interface Endpoint<T extends EndpointConfig> extends Configurable<T> {

    /**
     * Starts the endpoint.
     *
     * @throws EndpointException if starting the endpoint fails
     */
    public void start() throws EndpointException;


    /**
     * Stops the endpoint.
     */
    public void stop();


    /**
     * Gets a list of supported service profiles.
     *
     * @return list of supported service profiles.
     */
    public List<ServiceSpecificationProfile> getProfiles();


    /**
     * Gets endpoint information for an AAS. This is used for automatic registration with a registry. The returned result
     * may include multiple endpoints, e.g. with different interfaces like AAS-REPOSITORY and AAS.
     *
     * @param aasId the id of the AAS
     * @return a list of endpoint information where this AAS can be accessed
     */
    public default List<org.eclipse.digitaltwin.aas4j.v3.model.Endpoint> getAasEndpointInformation(String aasId) {
        return List.of();
    }


    /**
     * Gets endpoint information for a submodel. This is used for automatic registration with a registry. The returned
     * result may include multiple endpoints, e.g. with different interfaces like SUBMODEL-REPOSITORY and SUBMODEL.
     *
     * @param submodelId the id of the submodel
     * @return a list of endpoint information where this submodel can be accessed
     */
    public default List<org.eclipse.digitaltwin.aas4j.v3.model.Endpoint> getSubmodelEndpointInformation(String submodelId) {
        return List.of();
    }
}
