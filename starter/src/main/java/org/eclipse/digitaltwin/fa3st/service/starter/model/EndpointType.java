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
package org.eclipse.digitaltwin.fa3st.service.starter.model;

import org.eclipse.digitaltwin.fa3st.service.endpoint.Endpoint;
import org.eclipse.digitaltwin.fa3st.service.endpoint.EndpointConfig;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.HttpEndpointConfig;


/**
 * Utility enum for available endpoint types.
 */
public enum EndpointType {
    HTTP(HttpEndpointConfig.class.getName());
    //OPCUA(OpcUaEndpointConfig.class.getName());

    private final String implementationClass;

    private EndpointType(String implementationClass) {
        this.implementationClass = implementationClass;
    }


    /**
     * Gets the corresponding implementation class.
     *
     * @return the corresponding implementation class
     * @throws ClassNotFoundException if class is not present
     */
    public Class<? extends EndpointConfig<? extends Endpoint>> getImplementation() throws ClassNotFoundException {
        return (Class<? extends EndpointConfig<? extends Endpoint>>) Class.forName(implementationClass);
    }
}
