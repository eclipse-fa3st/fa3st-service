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

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.Page;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aasrepository.GetAllAssetAdministrationShellsByIdShortRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.aasrepository.GetAllAssetAdministrationShellsByIdShortResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.ElementReadEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.persistence.AssetAdministrationShellSearchCriteria;
import org.eclipse.digitaltwin.fa3st.common.util.LambdaExceptionHelper;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.aasrepository.GetAllAssetAdministrationShellsByIdShortRequest}
 * in the service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.aasrepository.GetAllAssetAdministrationShellsByIdShortResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllAssetAdministrationShellsByIdShortRequestHandler
        extends AbstractRequestHandler<GetAllAssetAdministrationShellsByIdShortRequest, GetAllAssetAdministrationShellsByIdShortResponse> {

    @Override
    public GetAllAssetAdministrationShellsByIdShortResponse process(GetAllAssetAdministrationShellsByIdShortRequest request, RequestExecutionContext context)
            throws MessageBusException, PersistenceException {
        Page<AssetAdministrationShell> page = context.getPersistence().findAssetAdministrationShells(
                AssetAdministrationShellSearchCriteria.builder()
                        .idShort(request.getIdShort())
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
        return GetAllAssetAdministrationShellsByIdShortResponse.builder()
                .payload(page)
                .success()
                .build();
    }

}
