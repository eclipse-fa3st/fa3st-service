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
import org.eclipse.digitaltwin.fa3st.common.model.api.Request;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.OutputModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.AbstractRequestWithModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.AbstractResponseWithPayload;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.serialization.HttpJsonApiSerializer;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.util.HttpHelper;


/**
 * Generic response mapper for any responses that contain a payload.It is used when no more specific mapper is present.
 *
 * @param <T> type of the payload
 * @param <U> type of the request
 */
public class ResponseWithPayloadResponseMapper<T extends AbstractResponseWithPayload, U extends Request<T>> extends AbstractResponseMapper<T, U> {

    public ResponseWithPayloadResponseMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    public void map(U apiRequest, T apiResponse, HttpServletResponse httpResponse) throws Exception {
        HttpHelper.sendJson(httpResponse,
                apiResponse.getStatusCode(),
                new HttpJsonApiSerializer().write(
                        apiResponse.getPayload(),
                        AbstractRequestWithModifier.class.isAssignableFrom(apiRequest.getClass())
                                ? ((AbstractRequestWithModifier) apiRequest).getOutputModifier()
                                : OutputModifier.DEFAULT));

    }
}
