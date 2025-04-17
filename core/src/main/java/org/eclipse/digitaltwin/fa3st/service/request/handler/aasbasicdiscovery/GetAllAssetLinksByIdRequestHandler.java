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
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aasbasicdiscovery.GetAllAssetLinksByIdRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.aasbasicdiscovery.GetAllAssetLinksByIdResponse;
import org.eclipse.digitaltwin.fa3st.common.util.Fa3stConstants;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.aasbasicdiscovery.GetAllAssetLinksByIdRequest} in the
 * service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.aasbasicdiscovery.GetAllAssetLinksByIdResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllAssetLinksByIdRequestHandler extends AbstractRequestHandler<GetAllAssetLinksByIdRequest, GetAllAssetLinksByIdResponse> {

    @Override
    public GetAllAssetLinksByIdResponse process(GetAllAssetLinksByIdRequest request, RequestExecutionContext context) throws ResourceNotFoundException, PersistenceException {
        AssetAdministrationShell aas = context.getPersistence().getAssetAdministrationShell(request.getId(), QueryModifier.DEFAULT);
        List<SpecificAssetId> result = new ArrayList<>(aas.getAssetInformation().getSpecificAssetIds());
        if (Objects.nonNull(aas.getAssetInformation().getGlobalAssetId())) {
            result.add(new DefaultSpecificAssetId.Builder()
                    .name(Fa3stConstants.KEY_GLOBAL_ASSET_ID)
                    .value(aas.getAssetInformation().getGlobalAssetId())
                    .build());
        }
        return GetAllAssetLinksByIdResponse.builder()
                .payload(result)
                .success()
                .build();
    }

}
