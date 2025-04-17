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

import java.util.Objects;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotAContainerElementException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.GetSubmodelElementByPathRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.GetSubmodelElementByPathResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.ElementReadEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ValueChangeEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.value.DataElementValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.ElementValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.mapper.ElementValueMapper;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceBuilder;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.GetSubmodelElementByPathRequest} in the
 * service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.GetSubmodelElementByPathResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetSubmodelElementByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<GetSubmodelElementByPathRequest, GetSubmodelElementByPathResponse> {

    @Override
    public GetSubmodelElementByPathResponse doProcess(GetSubmodelElementByPathRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, ValueMappingException, AssetConnectionException, MessageBusException, ResourceNotAContainerElementException, PersistenceException {
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        SubmodelElement submodelElement = context.getPersistence().getSubmodelElement(reference, request.getOutputModifier());
        Optional<DataElementValue> valueFromAssetConnection = context.getAssetConnectionManager().readValue(reference);
        if (valueFromAssetConnection.isPresent()) {
            ElementValue oldValue = ElementValueMapper.toValue(submodelElement);
            if (!Objects.equals(valueFromAssetConnection, oldValue)) {
                submodelElement = ElementValueMapper.setValue(submodelElement, valueFromAssetConnection.get());
                context.getPersistence().update(reference, submodelElement);
                if (!request.isInternal()) {
                    context.getMessageBus().publish(ValueChangeEventMessage.builder()
                            .element(reference)
                            .oldValue(oldValue)
                            .newValue(valueFromAssetConnection.get())
                            .build());
                }
            }
        }
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementReadEventMessage.builder()
                    .element(reference)
                    .value(submodelElement)
                    .build());
        }
        return GetSubmodelElementByPathResponse.builder()
                .payload(submodelElement)
                .success()
                .build();
    }
}
