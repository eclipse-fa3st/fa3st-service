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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.submodelrepository;

import java.util.Map;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Content;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.OutputModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodelrepository.DeleteSubmodelByIdRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodelrepository.DeleteSubmodelByIdResponse;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.model.HttpRequest;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.AbstractSubmodelInterfaceRequestMapper;


/**
 * class to map HTTP-DELETE-Request paths: submodels/{submodelIdentifier},
 * shells/{aasIdentifier}/submodels/{submodelIdentifier}.
 */
public class DeleteSubmodelByIdRequestMapper extends AbstractSubmodelInterfaceRequestMapper<DeleteSubmodelByIdRequest, DeleteSubmodelByIdResponse> {

    private static final String PATTERN = "";

    public DeleteSubmodelByIdRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.DELETE, PATTERN, Content.METADATA, Content.PATH, Content.REFERENCE, Content.VALUE);
    }


    @Override
    public DeleteSubmodelByIdRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) {
        return DeleteSubmodelByIdRequest.builder().build();
    }
}
