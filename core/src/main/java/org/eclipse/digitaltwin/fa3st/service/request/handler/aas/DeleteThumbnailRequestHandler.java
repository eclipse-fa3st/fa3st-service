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
package org.eclipse.digitaltwin.fa3st.service.request.handler.aas;

import java.io.IOException;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aas.DeleteThumbnailRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.aas.DeleteThumbnailResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementUpdateEventMessage;
import org.eclipse.digitaltwin.fa3st.common.util.StringHelper;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.aas.DeleteThumbnailRequest}.
 */
public class DeleteThumbnailRequestHandler extends AbstractRequestHandler<DeleteThumbnailRequest, DeleteThumbnailResponse> {

    @Override
    public DeleteThumbnailResponse process(DeleteThumbnailRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, MessageBusException, IOException, PersistenceException {
        AssetAdministrationShell aas = context.getPersistence().getAssetAdministrationShell(request.getId(), QueryModifier.DEFAULT);
        if (Objects.isNull(aas.getAssetInformation())
                || Objects.isNull(aas.getAssetInformation().getDefaultThumbnail())
                || StringHelper.isBlank(aas.getAssetInformation().getDefaultThumbnail().getPath())) {
            throw new ResourceNotFoundException(String.format("no thumbnail information set for AAS (id: %s)", request.getId()));
        }
        AssetInformation assetInformation = aas.getAssetInformation();
        context.getFileStorage().delete(assetInformation.getDefaultThumbnail().getPath());
        assetInformation.setDefaultThumbnail(null);
        aas.setAssetInformation(assetInformation);
        context.getPersistence().save(aas);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .value(aas)
                    .element(aas)
                    .build());
        }
        return DeleteThumbnailResponse.builder()
                .success()
                .build();
    }

}
