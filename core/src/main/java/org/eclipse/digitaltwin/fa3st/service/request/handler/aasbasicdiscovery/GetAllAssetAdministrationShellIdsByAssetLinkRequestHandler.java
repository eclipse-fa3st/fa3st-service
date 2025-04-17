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

import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.Page;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aasbasicdiscovery.GetAllAssetAdministrationShellIdsByAssetLinkRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.aasbasicdiscovery.GetAllAssetAdministrationShellIdsByAssetLinkResponse;
import org.eclipse.digitaltwin.fa3st.common.model.persistence.AssetAdministrationShellSearchCriteria;
import org.eclipse.digitaltwin.fa3st.common.util.AssetIdHelper;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Not supported yet! Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.aasbasicdiscovery.GetAllAssetAdministrationShellIdsByAssetLinkRequest}
 * in the service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.aasbasicdiscovery.GetAllAssetAdministrationShellIdsByAssetLinkResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllAssetAdministrationShellIdsByAssetLinkRequestHandler
        extends AbstractRequestHandler<GetAllAssetAdministrationShellIdsByAssetLinkRequest, GetAllAssetAdministrationShellIdsByAssetLinkResponse> {

    @Override
    public GetAllAssetAdministrationShellIdsByAssetLinkResponse process(GetAllAssetAdministrationShellIdsByAssetLinkRequest request, RequestExecutionContext context)
            throws PersistenceException {
        Page<AssetAdministrationShell> aass = context.getPersistence().findAssetAdministrationShells(
                AssetAdministrationShellSearchCriteria.builder()
                        .assetIds(AssetIdHelper.fromSpecificAssetIds(request.getAssetIdentifierPairs()))
                        .build(),
                QueryModifier.DEFAULT,
                request.getPagingInfo());

        List<String> result = aass.getContent()
                .stream()
                .map(Identifiable::getId)
                .collect(Collectors.toList());
        return GetAllAssetAdministrationShellIdsByAssetLinkResponse.builder()
                .payload(Page.<String> builder()
                        .metadata(aass.getMetadata())
                        .result(result)
                        .build())
                .success()
                .build();
    }
}
