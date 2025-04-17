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
import org.eclipse.digitaltwin.fa3st.common.model.api.StatusCode;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Extent;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.OutputModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.PatchSubmodelElementValueByPathRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.PatchSubmodelElementValueByPathResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ValueChangeEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.value.ElementValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.mapper.ElementValueMapper;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceBuilder;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.PatchSubmodelElementValueByPathRequest} in the service
 * and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.PatchSubmodelElementValueByPathResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PatchSubmodelElementValueByPathRequestHandler
        extends AbstractSubmodelInterfaceRequestHandler<PatchSubmodelElementValueByPathRequest<?>, PatchSubmodelElementValueByPathResponse> {

    @Override
    public PatchSubmodelElementValueByPathResponse doProcess(PatchSubmodelElementValueByPathRequest request, RequestExecutionContext context) throws Exception {
        if (request == null || request.getValueParser() == null) {
            throw new IllegalArgumentException("value parser of request must be non-null");
        }
        PatchSubmodelElementValueByPathResponse response = new PatchSubmodelElementValueByPathResponse();
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        SubmodelElement submodelElement = context.getPersistence().getSubmodelElement(
                reference,
                new OutputModifier.Builder()
                        .extent(Extent.WITH_BLOB_VALUE)
                        .build());
        ElementValue oldValue = ElementValueMapper.toValue(submodelElement);
        ElementValue newValue = request.getValueParser().parse(request.getRawValue(), oldValue.getClass());
        ElementValueMapper.setValue(submodelElement, newValue);
        if (request.isSyncWithAsset()) {
            context.getAssetConnectionManager().setValue(reference, newValue);
        }
        try {
            context.getPersistence().update(reference, submodelElement);
        }
        catch (IllegalArgumentException e) {
            // empty on purpose
        }
        response.setStatusCode(StatusCode.SUCCESS_NO_CONTENT);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ValueChangeEventMessage.builder()
                    .element(reference)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build());
        }
        return response;
    }

}
