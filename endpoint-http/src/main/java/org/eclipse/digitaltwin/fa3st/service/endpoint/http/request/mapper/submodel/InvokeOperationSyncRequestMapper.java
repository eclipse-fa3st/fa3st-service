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
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.fa3st.common.dataformat.DeserializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.model.IdShortPath;
import org.eclipse.digitaltwin.fa3st.common.model.SubmodelElementIdentifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Content;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.OutputModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.GetSubmodelElementByPathRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.InvokeOperationSyncRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.GetSubmodelElementByPathResponse;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.InvokeOperationSyncResponse;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.common.util.EncodingHelper;
import org.eclipse.digitaltwin.fa3st.common.util.RegExHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.model.HttpRequest;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.AbstractSubmodelInterfaceRequestMapper;


/**
 * class to map HTTP-POST-Request paths: submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/invoke,
 * shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/invoke.
 */
public class InvokeOperationSyncRequestMapper extends AbstractSubmodelInterfaceRequestMapper<InvokeOperationSyncRequest, InvokeOperationSyncResponse> {

    private static final String SUBMODEL_ELEMENT_PATH = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("submodel-elements/%s/invoke(/\\$value)?", pathElement(SUBMODEL_ELEMENT_PATH));

    public InvokeOperationSyncRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.POST, PATTERN);
    }


    @Override
    public InvokeOperationSyncRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException {
        InvokeOperationSyncRequest result;
        SubmodelElementIdentifier identifier = SubmodelElementIdentifier.builder()
                .submodelId(getParameterBase64UrlEncoded(urlParameters, SUBMODEL_ID))
                .idShortPath(IdShortPath.parse(EncodingHelper.urlDecode(urlParameters.get(SUBMODEL_ELEMENT_PATH))))
                .build();
        if (outputModifier.getContent() == Content.VALUE) {
            GetSubmodelElementByPathResponse response = serviceContext.execute(GetSubmodelElementByPathRequest.builder()
                    .internal()
                    .submodelId(identifier.getSubmodelId())
                    .path(identifier.getIdShortPath().toString())
                    .build());
            if (!Operation.class.isAssignableFrom(response.getPayload().getClass())) {
                throw new InvalidRequestException("element is not an operation");
            }
            Operation operation = (Operation) response.getPayload();
            try {
                result = deserializer.readValueOperationRequest(
                        httpRequest.getBodyAsString(),
                        InvokeOperationSyncRequest.class,
                        identifier,
                        operation.getInputVariables(),
                        operation.getInoutputVariables());
            }
            catch (DeserializationException e) {
                throw new InvalidRequestException(e);
            }
        }
        else {
            result = parseBody(httpRequest, InvokeOperationSyncRequest.class);
        }
        result.setSubmodelId(identifier.getSubmodelId());
        result.setPath(identifier.getIdShortPath().toString());
        return result;
    }
}
