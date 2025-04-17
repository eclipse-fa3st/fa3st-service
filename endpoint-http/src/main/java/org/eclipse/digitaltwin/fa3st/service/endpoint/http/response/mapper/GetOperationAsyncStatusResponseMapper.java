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
import java.util.Map;
import org.eclipse.digitaltwin.fa3st.common.model.api.StatusCode;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.GetOperationAsyncStatusRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.GetOperationAsyncStatusResponse;
import org.eclipse.digitaltwin.fa3st.common.util.EncodingHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.serialization.HttpJsonApiSerializer;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.util.HttpHelper;


/**
 * HTTP response mapper for {@link GetOperationAsyncStatusResponse}.
 */
public class GetOperationAsyncStatusResponseMapper extends AbstractResponseMapper<GetOperationAsyncStatusResponse, GetOperationAsyncStatusRequest> {

    public GetOperationAsyncStatusResponseMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    public void map(GetOperationAsyncStatusRequest apiRequest, GetOperationAsyncStatusResponse apiResponse, HttpServletResponse httpResponse) throws Exception {
        switch (apiResponse.getPayload().getExecutionState()) {
            case INITIATED:
            case RUNNING: {
                HttpHelper.sendJson(
                        httpResponse,
                        StatusCode.SUCCESS,
                        new HttpJsonApiSerializer().write(apiResponse.getPayload()));
                break;
            }
            case COMPLETED:
            case FAILED:
            case CANCELED:
            case TIMEOUT:
            default: {
                HttpHelper.sendEmpty(
                        httpResponse,
                        StatusCode.SUCCESS_FOUND,
                        Map.of("Location", String.format(
                                "../operation-results/%s",
                                EncodingHelper.base64UrlEncode(apiRequest.getHandle().getHandleId()))));
                break;
            }
        }
    }
}
