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
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceAlreadyExistsException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotAContainerElementException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValidationException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.IdShortPath;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.PostSubmodelElementByPathRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.PostSubmodelElementByPathResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementCreateEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.validation.ModelValidator;
import org.eclipse.digitaltwin.fa3st.common.model.value.mapper.ElementValueMapper;
import org.eclipse.digitaltwin.fa3st.common.util.ElementValueHelper;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceBuilder;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.PostSubmodelElementByPathRequest} in the
 * service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.PostSubmodelElementByPathResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostSubmodelElementByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<PostSubmodelElementByPathRequest, PostSubmodelElementByPathResponse> {

    @Override
    public PostSubmodelElementByPathResponse doProcess(PostSubmodelElementByPathRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, ValueMappingException, ValidationException, ResourceNotAContainerElementException, AssetConnectionException, MessageBusException,
            ResourceAlreadyExistsException, PersistenceException {
        ModelValidator.validate(request.getSubmodelElement(), context.getCoreConfig().getValidationOnCreate());
        IdShortPath idShortPath = IdShortPath.parse(request.getPath());
        Reference parentReference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(idShortPath)
                .build();
        ReferenceBuilder childReferenceBuilder = ReferenceBuilder.with(parentReference);
        if (idShortPath.isEmpty()) {
            childReferenceBuilder.element(request.getSubmodelElement().getIdShort());
        }
        else {
            SubmodelElement parent = context.getPersistence().getSubmodelElement(parentReference, QueryModifier.DEFAULT);
            if (SubmodelElementList.class.isAssignableFrom(parent.getClass())) {
                childReferenceBuilder.index(((SubmodelElementList) parent).getValue().size());
            }
            else {
                childReferenceBuilder.element(request.getSubmodelElement().getIdShort());
            }
        }
        Reference childReference = childReferenceBuilder.build();
        if (context.getPersistence().submodelElementExists(childReference)) {
            throw new ResourceAlreadyExistsException(childReference);
        }
        context.getPersistence().insert(parentReference, request.getSubmodelElement());
        if (ElementValueHelper.isSerializableAsValue(request.getSubmodelElement().getClass())) {
            context.getAssetConnectionManager().setValue(childReference, ElementValueMapper.toValue(request.getSubmodelElement()));
        }
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementCreateEventMessage.builder()
                    .element(childReference)
                    .value(request.getSubmodelElement())
                    .build());
        }
        return PostSubmodelElementByPathResponse.builder()
                .payload(request.getSubmodelElement())
                .created()
                .build();
    }

}
