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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.submodel;

import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.OutputModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.PutSubmodelElementByPathRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.PutSubmodelElementByPathResponse;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.common.util.EncodingHelper;
import org.eclipse.digitaltwin.fa3st.common.util.RegExHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.model.HttpRequest;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.AbstractSubmodelInterfaceRequestMapper;


/**
 * class to map HTTP-PUT-Request paths: submodels/{submodelIdentifier}/submodel-elements/{idShortPath},
 * shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}.
 */
public class PutSubmodelElementByPathRequestMapper extends AbstractSubmodelInterfaceRequestMapper<PutSubmodelElementByPathRequest, PutSubmodelElementByPathResponse> {

    private static final String SUBMODEL_ELEMENT_PATH = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("submodel-elements/%s", pathElement(SUBMODEL_ELEMENT_PATH));

    public PutSubmodelElementByPathRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.PUT, PATTERN);
    }


    @Override
    public PutSubmodelElementByPathRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException {
        return PutSubmodelElementByPathRequest.builder()
                .path(EncodingHelper.urlDecode(urlParameters.get(SUBMODEL_ELEMENT_PATH)))
                .submodelElement(parseBody(httpRequest, SubmodelElement.class))
                .build();
    }
}
