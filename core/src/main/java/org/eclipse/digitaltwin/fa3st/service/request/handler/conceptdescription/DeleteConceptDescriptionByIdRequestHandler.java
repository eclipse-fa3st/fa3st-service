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
package org.eclipse.digitaltwin.fa3st.service.request.handler.conceptdescription;

import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.api.StatusCode;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.conceptdescription.DeleteConceptDescriptionByIdRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.conceptdescription.DeleteConceptDescriptionByIdResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementDeleteEventMessage;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.conceptdescription.DeleteConceptDescriptionByIdRequest}
 * in the service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.conceptdescription.DeleteConceptDescriptionByIdResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class DeleteConceptDescriptionByIdRequestHandler extends AbstractRequestHandler<DeleteConceptDescriptionByIdRequest, DeleteConceptDescriptionByIdResponse> {

    @Override
    public DeleteConceptDescriptionByIdResponse process(DeleteConceptDescriptionByIdRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, MessageBusException, PersistenceException {
        DeleteConceptDescriptionByIdResponse response = new DeleteConceptDescriptionByIdResponse();
        ConceptDescription conceptDescription = context.getPersistence().getConceptDescription(request.getId(), QueryModifier.DEFAULT);
        context.getPersistence().deleteConceptDescription(request.getId());
        response.setStatusCode(StatusCode.SUCCESS_NO_CONTENT);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementDeleteEventMessage.builder()
                    .element(conceptDescription)
                    .value(conceptDescription)
                    .build());
        }
        return response;
    }

}
