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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.aas;

import java.util.Map;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.model.api.Request;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aas.DeleteSubmodelReferenceRequest;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceBuilder;
import org.eclipse.digitaltwin.fa3st.common.util.RegExHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.model.HttpRequest;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.AbstractRequestMapper;


/**
 * class to map HTTP-DELETE-Request path: shells/{aasIdentifier}/submodel-refs/{submodelIdentifier}.
 */
public class DeleteSubmodelReferenceRequestMapper extends AbstractRequestMapper {

    private static final String AAS_ID = RegExHelper.uniqueGroupName();
    private static final String SUBMODEL_ID = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("shells/%s/submodel-refs/%s",
            pathElement(AAS_ID),
            pathElement(SUBMODEL_ID));

    public DeleteSubmodelReferenceRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.DELETE, PATTERN);
    }


    @Override
    public Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        return DeleteSubmodelReferenceRequest.builder()
                .id(getParameterBase64UrlEncoded(urlParameters, AAS_ID))
                .submodelRef(ReferenceBuilder.forSubmodel(getParameterBase64UrlEncoded(urlParameters, SUBMODEL_ID)))
                .build();
    }
}
