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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.aasbasicdiscovery;

import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.fa3st.common.dataformat.DeserializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.PagingInfo;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aasbasicdiscovery.GetAllAssetAdministrationShellIdsByAssetLinkRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.aasbasicdiscovery.GetAllAssetAdministrationShellIdsByAssetLinkResponse;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.model.HttpRequest;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.AbstractRequestMapperWithPaging;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.QueryParameters;


/**
 * class to map HTTP-GET-Request path: lookup/shells.
 */
public class GetAllAssetAdministrationShellIdsByAssetLinkRequestMapper
        extends AbstractRequestMapperWithPaging<GetAllAssetAdministrationShellIdsByAssetLinkRequest, GetAllAssetAdministrationShellIdsByAssetLinkResponse> {

    private static final String PATTERN = "lookup/shells";

    public GetAllAssetAdministrationShellIdsByAssetLinkRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN);
    }


    @Override
    public GetAllAssetAdministrationShellIdsByAssetLinkRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, PagingInfo pagingInfo)
            throws InvalidRequestException {
        try {
            GetAllAssetAdministrationShellIdsByAssetLinkRequest.Builder builder = GetAllAssetAdministrationShellIdsByAssetLinkRequest.builder();
            if (httpRequest.hasQueryParameter(QueryParameters.ASSET_IDS)) {
                builder = builder.assetIdentifierPairs(deserializer.readList(
                        getParameterBase64UrlEncoded(httpRequest.getQueryParameters(), QueryParameters.ASSET_IDS),
                        SpecificAssetId.class));
            }
            return builder.build();
        }
        catch (DeserializationException e) {
            throw new InvalidRequestException(
                    String.format("error deserializing %s (value: %s)", QueryParameters.ASSET_IDS, httpRequest.getQueryParameter(QueryParameters.ASSET_IDS)), e);
        }
    }
}
