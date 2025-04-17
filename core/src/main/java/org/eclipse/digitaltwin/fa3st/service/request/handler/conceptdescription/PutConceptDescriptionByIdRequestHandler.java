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

import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValidationException;
import org.eclipse.digitaltwin.fa3st.common.model.api.StatusCode;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.conceptdescription.PutConceptDescriptionByIdRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.conceptdescription.PutConceptDescriptionByIdResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementUpdateEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.validation.ModelValidator;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.conceptdescription.PutConceptDescriptionByIdRequest}
 * in the service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.conceptdescription.PutConceptDescriptionByIdResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PutConceptDescriptionByIdRequestHandler extends AbstractRequestHandler<PutConceptDescriptionByIdRequest, PutConceptDescriptionByIdResponse> {

    @Override
    public PutConceptDescriptionByIdResponse process(PutConceptDescriptionByIdRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, MessageBusException, ValidationException, PersistenceException {
        ModelValidator.validate(request.getConceptDescription(), context.getCoreConfig().getValidationOnUpdate());
        context.getPersistence().getConceptDescription(request.getConceptDescription().getId(), QueryModifier.DEFAULT);
        context.getPersistence().save(request.getConceptDescription());
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .element(request.getConceptDescription())
                    .value(request.getConceptDescription())
                    .build());
        }
        return PutConceptDescriptionByIdResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
    }

}
