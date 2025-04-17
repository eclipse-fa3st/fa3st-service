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

import java.io.IOException;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotAContainerElementException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.api.StatusCode;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.PutFileByPathRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.PutFileByPathResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ValueChangeEventMessage;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceBuilder;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.PutFileByPathRequest}.
 */
public class PutFileByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<PutFileByPathRequest, PutFileByPathResponse> {

    @Override
    public PutFileByPathResponse doProcess(PutFileByPathRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, ValueMappingException, AssetConnectionException, MessageBusException, ResourceNotAContainerElementException, IOException,
            PersistenceException {
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        File file = context.getPersistence().getSubmodelElement(reference, request.getOutputModifier(), File.class);
        file.setContentType(request.getContent().getContentType());
        file.setValue(request.getContent().getPath());
        context.getPersistence().update(reference, file);
        context.getFileStorage().save(file.getValue(), request.getContent().getContent());
        if (!request.isInternal()) {
            context.getMessageBus().publish(ValueChangeEventMessage.builder()
                    .element(reference)
                    .build());
        }
        return PutFileByPathResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
    }
}
