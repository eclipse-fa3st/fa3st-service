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
package org.eclipse.digitaltwin.fa3st.service.request.handler.aasrepository;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceAlreadyExistsException;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aasrepository.PostAssetAdministrationShellRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.aasrepository.PostAssetAdministrationShellResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementCreateEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.validation.ModelValidator;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.aasrepository.PostAssetAdministrationShellRequest} in
 * the service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.aasrepository.PostAssetAdministrationShellResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostAssetAdministrationShellRequestHandler extends AbstractRequestHandler<PostAssetAdministrationShellRequest, PostAssetAdministrationShellResponse> {

    @Override
    public PostAssetAdministrationShellResponse process(PostAssetAdministrationShellRequest request, RequestExecutionContext context) throws Exception {
        ModelValidator.validate(request.getAas(), context.getCoreConfig().getValidationOnCreate());
        if (context.getPersistence().assetAdministrationShellExists(request.getAas().getId())) {
            throw new ResourceAlreadyExistsException(request.getAas().getId(), AssetAdministrationShell.class);
        }
        context.getPersistence().save(request.getAas());
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementCreateEventMessage.builder()
                    .element(request.getAas())
                    .value(request.getAas())
                    .build());
        }
        return PostAssetAdministrationShellResponse.builder()
                .payload(request.getAas())
                .created()
                .build();
    }
}
