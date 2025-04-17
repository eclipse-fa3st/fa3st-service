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

import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
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
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodelrepository.PatchSubmodelByIdRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodelrepository.PatchSubmodelByIdResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementUpdateEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.validation.ModelValidator;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceBuilder;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.submodelrepository.PatchSubmodelByIdRequest}.
 */
public class PatchSubmodelByIdRequestHandler extends AbstractRequestHandler<PatchSubmodelByIdRequest, PatchSubmodelByIdResponse> {

    @Override
    public PatchSubmodelByIdResponse process(PatchSubmodelByIdRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, AssetConnectionException, ValueMappingException, MessageBusException, ValidationException, ResourceNotAContainerElementException,
            InvalidRequestException, PersistenceException {
        Submodel current = context.getPersistence().getSubmodel(request.getId(), QueryModifier.DEFAULT);
        Submodel updated = applyMergePatch(request.getChanges(), current, Submodel.class);
        ModelValidator.validate(updated, context.getCoreConfig().getValidationOnUpdate());
        context.getPersistence().save(updated);
        Reference reference = ReferenceBuilder.forSubmodel(updated);
        cleanupDanglingAssetConnectionsForParent(reference, context.getPersistence(), context);
        syncWithAsset(reference, updated.getSubmodelElements(), !request.isInternal(), context);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .element(reference)
                    .value(updated)
                    .build());
        }
        return PatchSubmodelByIdResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
    }

}
