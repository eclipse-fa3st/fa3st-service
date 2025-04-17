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
package org.eclipse.digitaltwin.fa3st.service.request.handler.aas;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.api.StatusCode;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aas.DeleteSubmodelReferenceRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.aas.DeleteSubmodelReferenceResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementUpdateEventMessage;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceHelper;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.aas.DeleteSubmodelReferenceRequest}
 * in the service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.aas.DeleteSubmodelReferenceResponse}. Is responsible
 * for communication with the persistence and sends the corresponding events to the message bus.
 */
public class DeleteSubmodelReferenceRequestHandler extends AbstractRequestHandler<DeleteSubmodelReferenceRequest, DeleteSubmodelReferenceResponse> {

    @Override
    public DeleteSubmodelReferenceResponse process(DeleteSubmodelReferenceRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, MessageBusException, PersistenceException {
        DeleteSubmodelReferenceResponse response = new DeleteSubmodelReferenceResponse();
        AssetAdministrationShell aas = context.getPersistence().getAssetAdministrationShell(request.getId(), QueryModifier.DEFAULT);
        Reference submodelRefToDelete = aas.getSubmodels().stream()
                .filter(x -> ReferenceHelper.equals(request.getSubmodelRef(), x, false))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format(
                        "SubmodelReference '%s' not found in AAS with id '%s'",
                        ReferenceHelper.toString(request.getSubmodelRef()),
                        request.getId())));
        aas.getSubmodels().remove(submodelRefToDelete);
        context.getPersistence().save(aas);
        response.setStatusCode(StatusCode.SUCCESS_NO_CONTENT);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .element(aas)
                    .value(aas)
                    .build());
        }
        return response;
    }

}
