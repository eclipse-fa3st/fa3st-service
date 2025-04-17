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
package org.eclipse.digitaltwin.fa3st.service.request.handler.submodel;

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotAContainerElementException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.IdShortPath;
import org.eclipse.digitaltwin.fa3st.common.model.SubmodelElementIdentifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.GetSubmodelElementByPathReferenceRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.GetSubmodelElementByPathReferenceResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.ElementReadEventMessage;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceBuilder;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.GetSubmodelElementByPathReferenceRequest} in
 * the service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.GetSubmodelElementByPathReferenceResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetSubmodelElementByPathReferenceRequestHandler
        extends AbstractSubmodelInterfaceRequestHandler<GetSubmodelElementByPathReferenceRequest, GetSubmodelElementByPathReferenceResponse> {

    @Override
    public GetSubmodelElementByPathReferenceResponse doProcess(GetSubmodelElementByPathReferenceRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, ValueMappingException, AssetConnectionException, MessageBusException, ResourceNotAContainerElementException, PersistenceException {
        Reference reference = resolveReferenceWithTypes(request.getSubmodelId(), request.getPath(), context);
        SubmodelElement submodelElement = context.getPersistence().getSubmodelElement(reference, request.getOutputModifier());
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementReadEventMessage.builder()
                    .element(reference)
                    .value(submodelElement)
                    .build());
        }
        return GetSubmodelElementByPathReferenceResponse.builder()
                .payload(reference)
                .success()
                .build();
    }


    private Reference resolveReferenceWithTypes(String submodelId, String idShortPath, RequestExecutionContext context) throws ResourceNotFoundException, PersistenceException {
        ReferenceBuilder builder = new ReferenceBuilder();
        builder.submodel(submodelId);
        IdShortPath.Builder pathBuilder = IdShortPath.builder();
        for (String pathElement: IdShortPath.parse(idShortPath).getElements()) {
            IdShortPath subPath = pathBuilder.pathSegment(pathElement).build();
            SubmodelElement submodelElement = context.getPersistence().getSubmodelElement(
                    SubmodelElementIdentifier.builder()
                            .submodelId(submodelId)
                            .idShortPath(subPath)
                            .build(),
                    QueryModifier.MINIMAL);
            if (pathElement.startsWith("[") && pathElement.endsWith("]")) {
                builder.element(pathElement.substring(1, pathElement.length() - 1), submodelElement.getClass());
            }
            else {
                builder.element(submodelElement);
            }
        }
        return builder.build();
    }
}
