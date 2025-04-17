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
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceAlreadyExistsException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aas.PostSubmodelReferenceRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.aas.PostSubmodelReferenceResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementUpdateEventMessage;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.aas.PostSubmodelReferenceRequest} in
 * the service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.aas.PostSubmodelReferenceResponse}. Is responsible
 * for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostSubmodelReferenceRequestHandler extends AbstractRequestHandler<PostSubmodelReferenceRequest, PostSubmodelReferenceResponse> {

    @Override
    public PostSubmodelReferenceResponse process(PostSubmodelReferenceRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, MessageBusException, ResourceAlreadyExistsException, PersistenceException {
        AssetAdministrationShell aas = context.getPersistence().getAssetAdministrationShell(request.getId(), QueryModifier.DEFAULT);
        if (aas.getSubmodels().contains(request.getSubmodelRef())) {
            throw new ResourceAlreadyExistsException(request.getSubmodelRef());
        }
        aas.getSubmodels().add(request.getSubmodelRef());
        context.getPersistence().save(aas);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .element(aas)
                    .value(aas)
                    .build());
        }
        return PostSubmodelReferenceResponse.builder()
                .payload(request.getSubmodelRef())
                .created()
                .build();
    }

}
