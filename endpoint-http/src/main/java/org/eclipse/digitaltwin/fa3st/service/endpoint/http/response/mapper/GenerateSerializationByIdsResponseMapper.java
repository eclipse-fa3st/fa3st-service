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
import org.eclipse.digitaltwin.fa3st.common.dataformat.EnvironmentSerializationManager;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aasserialization.GenerateSerializationByIdsRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.aasserialization.GenerateSerializationByIdsResponse;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.util.HttpHelper;


/**
 * HTTP response mapper for {@link GenerateSerializationByIdsResponse}, serializing the requested content according to
 * the desired data format.
 */
public class GenerateSerializationByIdsResponseMapper extends AbstractResponseMapper<GenerateSerializationByIdsResponse, GenerateSerializationByIdsRequest> {

    public GenerateSerializationByIdsResponseMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    public void map(GenerateSerializationByIdsRequest apiRequest, GenerateSerializationByIdsResponse apiResponse, HttpServletResponse httpResponse) throws Exception {
        HttpHelper.sendContent(httpResponse,
                apiResponse.getStatusCode(),
                EnvironmentSerializationManager.serializerFor(apiResponse.getDataformat()).write(apiResponse.getPayload()),
                apiResponse.getDataformat().getContentType(),
                Map.of("Content-Disposition",
                        String.format(
                                "attachment; filename=\"download.%s\"",
                                apiResponse.getDataformat().getFileExtensions().get(0))));

    }
}
