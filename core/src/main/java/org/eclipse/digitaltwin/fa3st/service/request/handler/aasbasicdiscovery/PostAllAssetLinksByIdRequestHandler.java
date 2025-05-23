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
package org.eclipse.digitaltwin.fa3st.service.request.handler.aasbasicdiscovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.api.StatusCode;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aasbasicdiscovery.PostAllAssetLinksByIdRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.aasbasicdiscovery.PostAllAssetLinksByIdResponse;
import org.eclipse.digitaltwin.fa3st.common.util.Fa3stConstants;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.aasbasicdiscovery.PostAllAssetLinksByIdRequest} in the
 * service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.aasbasicdiscovery.PostAllAssetLinksByIdResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostAllAssetLinksByIdRequestHandler extends AbstractRequestHandler<PostAllAssetLinksByIdRequest, PostAllAssetLinksByIdResponse> {

    @Override
    public PostAllAssetLinksByIdResponse process(PostAllAssetLinksByIdRequest request, RequestExecutionContext context) throws ResourceNotFoundException, PersistenceException {
        AssetAdministrationShell aas = context.getPersistence().getAssetAdministrationShell(request.getId(), QueryModifier.DEFAULT);
        List<SpecificAssetId> globalKeys = request.getAssetLinks().stream()
                .filter(x -> Fa3stConstants.KEY_GLOBAL_ASSET_ID.equals(x.getName()))
                .collect(Collectors.toList());
        if (!globalKeys.isEmpty()) {
            if (globalKeys.size() == 1 && globalKeys.get(0) != null) {
                aas.getAssetInformation().setGlobalAssetId(globalKeys.get(0).getValue());
            }
            else {
                return PostAllAssetLinksByIdResponse.builder()
                        .error(StatusCode.CLIENT_ERROR_BAD_REQUEST,
                                String.format("request can contain at most 1 element with key '%s', but %d found",
                                        Fa3stConstants.KEY_GLOBAL_ASSET_ID,
                                        globalKeys.size()))
                        .build();
            }
        }
        //TODO potentially check if assetLinks already exist and throw ResourceAlreadyExistsException, but currently unclear how this is expected to work
        List<SpecificAssetId> newSpecificAssetIds = request.getAssetLinks().stream()
                .filter(x -> !Objects.equals(Fa3stConstants.KEY_GLOBAL_ASSET_ID, x.getName()))
                .collect(Collectors.toList());
        for (var newSpecificAssetId: newSpecificAssetIds) {
            List<SpecificAssetId> existingLinks = aas.getAssetInformation().getSpecificAssetIds().stream()
                    .filter(x -> Objects.equals(x.getName(), newSpecificAssetId.getName()))
                    .collect(Collectors.toList());
            if (existingLinks.isEmpty()) {
                aas.getAssetInformation().getSpecificAssetIds().add(newSpecificAssetId);
            }
            else if (existingLinks.size() == 1) {
                aas.getAssetInformation().getSpecificAssetIds().remove(existingLinks.get(0));
                aas.getAssetInformation().getSpecificAssetIds().add(newSpecificAssetId);
            }
            else {
                return PostAllAssetLinksByIdResponse.builder()
                        .error(StatusCode.CLIENT_ERROR_BAD_REQUEST,
                                String.format("error updating specificAssetId - found %d entries for key '%s', but expected only one",
                                        existingLinks.size(),
                                        newSpecificAssetId.getName()))
                        .build();
            }
        }
        context.getPersistence().save(aas);
        List<SpecificAssetId> result = new ArrayList<>(aas.getAssetInformation().getSpecificAssetIds());
        if (aas.getAssetInformation().getGlobalAssetId() != null) {
            result.add(new DefaultSpecificAssetId.Builder()
                    .name(Fa3stConstants.KEY_GLOBAL_ASSET_ID)
                    .value(aas.getAssetInformation().getGlobalAssetId())
                    .build());
        }
        return PostAllAssetLinksByIdResponse.builder()
                .payload(result)
                .created()
                .build();
    }

}
