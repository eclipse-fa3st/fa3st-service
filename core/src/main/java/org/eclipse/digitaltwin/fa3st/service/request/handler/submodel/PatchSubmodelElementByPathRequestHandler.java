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
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotAContainerElementException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValidationException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.api.StatusCode;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.PatchSubmodelElementByPathRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.PatchSubmodelElementByPathResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementCreateEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementUpdateEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ValueChangeEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.validation.ModelValidator;
import org.eclipse.digitaltwin.fa3st.common.model.value.ElementValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.mapper.ElementValueMapper;
import org.eclipse.digitaltwin.fa3st.common.util.ElementValueHelper;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceBuilder;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.PatchSubmodelElementByPathRequest}.
 */
public class PatchSubmodelElementByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<PatchSubmodelElementByPathRequest, PatchSubmodelElementByPathResponse> {

    @Override
    public PatchSubmodelElementByPathResponse doProcess(PatchSubmodelElementByPathRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, ValueMappingException, AssetConnectionException, MessageBusException, ValidationException, ResourceNotAContainerElementException,
            InvalidRequestException, PersistenceException {
        Submodel current = context.getPersistence().getSubmodel(request.getSubmodelId(), QueryModifier.DEFAULT);
        Submodel updated = applyMergePatch(request.getChanges(), current, Submodel.class);
        context.getPersistence().save(updated);
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        SubmodelElement oldSubmodelElement = context.getPersistence().getSubmodelElement(reference, QueryModifier.DEFAULT);
        SubmodelElement newSubmodelElement = applyMergePatch(request.getChanges(), oldSubmodelElement, SubmodelElement.class);
        ModelValidator.validate(newSubmodelElement, context.getCoreConfig().getValidationOnUpdate());
        context.getPersistence().update(reference, newSubmodelElement);
        cleanupDanglingAssetConnectionsForParent(reference, context.getPersistence(), context);
        if (!request.isInternal() && Objects.isNull(oldSubmodelElement)) {
            context.getMessageBus().publish(ElementCreateEventMessage.builder()
                    .element(reference)
                    .value(newSubmodelElement)
                    .build());
        }
        else if (Objects.equals(oldSubmodelElement.getClass(), newSubmodelElement.getClass())
                && ElementValueHelper.isSerializableAsValue(oldSubmodelElement.getClass())) {
            ElementValue oldValue = ElementValueMapper.toValue(oldSubmodelElement);
            ElementValue newValue = ElementValueMapper.toValue(newSubmodelElement);
            if (!Objects.equals(oldValue, newValue)) {
                context.getAssetConnectionManager().setValue(reference, newValue);
                if (!request.isInternal()) {
                    context.getMessageBus().publish(ValueChangeEventMessage.builder()
                            .element(reference)
                            .oldValue(oldValue)
                            .newValue(newValue)
                            .build());
                }
            }
        }
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .element(reference)
                    .value(newSubmodelElement)
                    .build());
        }
        return PatchSubmodelElementByPathResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
    }
}
