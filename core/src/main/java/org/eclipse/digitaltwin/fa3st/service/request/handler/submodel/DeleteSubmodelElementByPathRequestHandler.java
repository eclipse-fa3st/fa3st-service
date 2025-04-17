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
import org.eclipse.digitaltwin.fa3st.common.model.IdShortPath;
import org.eclipse.digitaltwin.fa3st.common.model.SubmodelElementIdentifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.StatusCode;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.DeleteSubmodelElementByPathRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.DeleteSubmodelElementByPathResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementDeleteEventMessage;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceBuilder;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.DeleteSubmodelElementByPathRequest} in the
 * service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.DeleteSubmodelElementByPathResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class DeleteSubmodelElementByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<DeleteSubmodelElementByPathRequest, DeleteSubmodelElementByPathResponse> {

    @Override
    protected DeleteSubmodelElementByPathResponse doProcess(DeleteSubmodelElementByPathRequest request, RequestExecutionContext context) throws Exception {
        DeleteSubmodelElementByPathResponse response = new DeleteSubmodelElementByPathResponse();
        SubmodelElementIdentifier.builder()
                .submodelId(request.getSubmodelId())
                .idShortPath(IdShortPath.parse(request.getPath()))
                .build();
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        SubmodelElement submodelElement = context.getPersistence().getSubmodelElement(reference, QueryModifier.DEFAULT);
        context.getPersistence().deleteSubmodelElement(SubmodelElementIdentifier.fromReference(reference));
        response.setStatusCode(StatusCode.SUCCESS_NO_CONTENT);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementDeleteEventMessage.builder()
                    .element(reference)
                    .value(submodelElement)
                    .build());
        }
        return response;
    }
}
