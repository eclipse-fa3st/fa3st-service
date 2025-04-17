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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http.response.mapper;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import org.eclipse.digitaltwin.fa3st.common.model.api.Request;
import org.eclipse.digitaltwin.fa3st.common.model.api.Response;
import org.eclipse.digitaltwin.fa3st.common.util.Ensure;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;


/**
 * Base class for mapping protocol-agnostic API responses to HTTP .
 *
 * @param <T> type of the response this class can be handled
 * @param <U> type of the request
 */
public abstract class AbstractResponseMapper<T extends Response, U extends Request<T>> {

    protected final ServiceContext serviceContext;

    protected AbstractResponseMapper(ServiceContext serviceContext) {
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        this.serviceContext = serviceContext;
    }


    /**
     * @param apiRequest the API request received
     * @param apiResponse the API response that shall be sent as a response to the apiRequest
     * @param httpResponse the HTTP response object to write to
     * @throws Exception if mapping fails
     */
    public abstract void map(U apiRequest, T apiResponse, HttpServletResponse httpResponse) throws Exception;


    @Override
    public int hashCode() {
        return Objects.hash(serviceContext);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractResponseMapper<T, U> other = (AbstractResponseMapper<T, U>) obj;
        return Objects.equals(this.serviceContext, other.serviceContext);
    }

}
