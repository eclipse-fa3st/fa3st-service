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

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotAContainerElementException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.GetSubmodelRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.GetSubmodelResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.ElementReadEventMessage;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.GetSubmodelRequest} in the
 * service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.GetSubmodelResponse}. Is responsible for
 * communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetSubmodelRequestHandler extends AbstractSubmodelInterfaceRequestHandler<GetSubmodelRequest, GetSubmodelResponse> {

    @Override
    public GetSubmodelResponse doProcess(GetSubmodelRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, AssetConnectionException, ValueMappingException, MessageBusException, ResourceNotAContainerElementException, PersistenceException {
        Submodel submodel = context.getPersistence().getSubmodel(request.getSubmodelId(), request.getOutputModifier());
        Reference reference = AasUtils.toReference(submodel);
        syncWithAsset(reference, submodel.getSubmodelElements(), !request.isInternal(), context);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementReadEventMessage.builder()
                    .element(reference)
                    .value(submodel)
                    .build());
        }
        return GetSubmodelResponse.builder()
                .payload(submodel)
                .success()
                .build();
    }
}
