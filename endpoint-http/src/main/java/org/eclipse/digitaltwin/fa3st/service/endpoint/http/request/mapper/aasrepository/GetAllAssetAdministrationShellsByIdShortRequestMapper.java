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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.aasrepository;

import java.util.Map;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Content;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.OutputModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.PagingInfo;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aasrepository.GetAllAssetAdministrationShellsByIdShortRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.aasrepository.GetAllAssetAdministrationShellsByIdShortResponse;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.model.HttpRequest;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.AbstractRequestMapperWithOutputModifierAndPaging;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.QueryParameters;


/**
 * class to map HTTP-Request path: shells.
 */
public class GetAllAssetAdministrationShellsByIdShortRequestMapper
        extends AbstractRequestMapperWithOutputModifierAndPaging<GetAllAssetAdministrationShellsByIdShortRequest, GetAllAssetAdministrationShellsByIdShortResponse> {

    private static final String PATTERN = "shells$";

    public GetAllAssetAdministrationShellsByIdShortRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN, Content.REFERENCE);
    }


    @Override
    public boolean matchesUrl(HttpRequest httpRequest) {
        return super.matchesUrl(httpRequest) && httpRequest.hasQueryParameter(QueryParameters.ID_SHORT);
    }


    @Override
    public GetAllAssetAdministrationShellsByIdShortRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier,
                                                                   PagingInfo pagingInfo) {
        return GetAllAssetAdministrationShellsByIdShortRequest.builder()
                .idShort(httpRequest.getQueryParameters().get(QueryParameters.ID_SHORT))
                .build();
    }
}
