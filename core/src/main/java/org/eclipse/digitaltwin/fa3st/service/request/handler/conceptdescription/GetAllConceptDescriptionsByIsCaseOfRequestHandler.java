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

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.Page;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.conceptdescription.GetAllConceptDescriptionsByIsCaseOfRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.conceptdescription.GetAllConceptDescriptionsByIsCaseOfResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.ElementReadEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.persistence.ConceptDescriptionSearchCriteria;
import org.eclipse.digitaltwin.fa3st.common.util.LambdaExceptionHelper;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.conceptdescription.GetAllConceptDescriptionsByIsCaseOfRequest}
 * in the service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.conceptdescription.GetAllConceptDescriptionsByIsCaseOfResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllConceptDescriptionsByIsCaseOfRequestHandler
        extends AbstractRequestHandler<GetAllConceptDescriptionsByIsCaseOfRequest, GetAllConceptDescriptionsByIsCaseOfResponse> {

    @Override
    public GetAllConceptDescriptionsByIsCaseOfResponse process(GetAllConceptDescriptionsByIsCaseOfRequest request, RequestExecutionContext context)
            throws MessageBusException, PersistenceException {
        Page<ConceptDescription> page = context.getPersistence().findConceptDescriptions(
                ConceptDescriptionSearchCriteria.builder()
                        .isCaseOf(request.getIsCaseOf())
                        .build(),
                request.getOutputModifier(),
                request.getPagingInfo());
        if (!request.isInternal() && Objects.nonNull(page.getContent())) {
            page.getContent().forEach(LambdaExceptionHelper.rethrowConsumer(
                    x -> context.getMessageBus().publish(ElementReadEventMessage.builder()
                            .element(x)
                            .value(x)
                            .build())));
        }
        return GetAllConceptDescriptionsByIsCaseOfResponse.builder()
                .payload(page)
                .success()
                .build();
    }
}
