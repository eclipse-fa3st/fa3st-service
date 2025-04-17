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
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.Page;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.GetAllSubmodelElementsPathRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.GetAllSubmodelElementsPathResponse;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.serialization.HttpJsonApiSerializer;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.util.HttpHelper;


/**
 * HTTP response mapper for {@link GetAllSubmodelElementsPathResponse}.
 */
public class GetAllSubmodelElementsPathResponseMapper extends AbstractResponseMapper<GetAllSubmodelElementsPathResponse, GetAllSubmodelElementsPathRequest> {

    public GetAllSubmodelElementsPathResponseMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    public void map(GetAllSubmodelElementsPathRequest apiRequest, GetAllSubmodelElementsPathResponse apiResponse, HttpServletResponse httpResponse) throws Exception {
        Page<String> result = Page.of(
                apiResponse.getPayload().getContent().stream().map(Object::toString).toList(),
                apiResponse.getPayload().getMetadata());
        HttpHelper.sendJson(httpResponse,
                apiResponse.getStatusCode(),
                new HttpJsonApiSerializer().write(result));
    }
}
