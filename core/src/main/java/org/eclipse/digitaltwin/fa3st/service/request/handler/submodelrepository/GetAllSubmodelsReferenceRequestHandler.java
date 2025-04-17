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
package org.eclipse.digitaltwin.fa3st.service.request.handler.submodelrepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotAContainerElementException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.Page;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodelrepository.GetAllSubmodelsReferenceRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodelrepository.GetAllSubmodelsReferenceResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.ElementReadEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.persistence.SubmodelSearchCriteria;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceBuilder;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.submodelrepository.GetAllSubmodelsReferenceRequest} in
 * the service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.submodelrepository.GetAllSubmodelsReferenceResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllSubmodelsReferenceRequestHandler extends AbstractRequestHandler<GetAllSubmodelsReferenceRequest, GetAllSubmodelsReferenceResponse> {

    @Override
    public GetAllSubmodelsReferenceResponse process(GetAllSubmodelsReferenceRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, AssetConnectionException, ValueMappingException, MessageBusException, ResourceNotAContainerElementException, PersistenceException {
        Page<Submodel> page = context.getPersistence().findSubmodels(
                SubmodelSearchCriteria.NONE,
                request.getOutputModifier(),
                request.getPagingInfo());
        if (!request.isInternal() && Objects.nonNull(page.getContent())) {
            for (Submodel submodel: page.getContent()) {
                Reference reference = AasUtils.toReference(submodel);
                context.getMessageBus().publish(ElementReadEventMessage.builder()
                        .element(reference)
                        .value(submodel)
                        .build());
            }
        }
        List<Reference> result = page.getContent().stream()
                .map(ReferenceBuilder::forSubmodel)
                .collect(Collectors.toList());
        return GetAllSubmodelsReferenceResponse.builder()
                .payload(Page.of(result, page.getMetadata()))
                .success()
                .build();
    }
}
