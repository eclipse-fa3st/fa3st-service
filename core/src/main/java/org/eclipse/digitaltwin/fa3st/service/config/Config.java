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
package org.eclipse.digitaltwin.fa3st.service.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.google.common.reflect.TypeToken;
import org.eclipse.digitaltwin.fa3st.common.exception.ConfigurationException;
import org.eclipse.digitaltwin.fa3st.common.exception.ConfigurationInstantiationException;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.config.serialization.ConfigTypeResolver;


/**
 * Superclass of all config classes that are coupled with a concrete implementation class (via generics). Each config
 * class can be serialized to/parsed from JSON in the form of { "@class": "[implemenation class], [normal JSON
 * serialization of properties] } where [implemenation class] is the fully qualified class name of an implementation
 * class (i.e. implementing the interface Configurable) that can be configured with this configuration.
 *
 * @param <T> type of the implementation class configured by this configuration
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "@class", visible = true)
@JsonTypeIdResolver(ConfigTypeResolver.class)
public abstract class Config<T extends Configurable> {

    /**
     * Utility method to get the concrete type of the corresponding implementation.
     *
     * @return the type of the corresponding implementation
     */
    protected Class<T> getImplementationType() {
        return (Class<T>) TypeToken.of(getClass()).resolveType(Config.class.getTypeParameters()[0]).getRawType();
    }


    /**
     * Creates a new instance of the implementation class that is initialized with this configuration.
     *
     * @param coreConfig the coreConfig to initialize the implementation class with
     * @param context context information about the service
     * @return a new instance of the implementation class that is initialized with this configuration
     * @throws ConfigurationInstantiationException when creating a new instance fails
     */
    public T newInstance(CoreConfig coreConfig, ServiceContext context) throws ConfigurationException {
        try {
            T result = getImplementationType().newInstance();
            result.init(coreConfig, this, context);
            return result;
        }
        catch (IllegalAccessException | InstantiationException e) {
            throw new ConfigurationInstantiationException("error instantiating configuration implementation class", e);
        }
    }
}
