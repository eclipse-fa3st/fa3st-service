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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.conceptdescription;

import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.fa3st.common.dataformat.DeserializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.OutputModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.PagingInfo;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.conceptdescription.GetAllConceptDescriptionsByIsCaseOfRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.conceptdescription.GetAllConceptDescriptionsByIsCaseOfResponse;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.model.HttpRequest;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.AbstractRequestMapperWithOutputModifierAndPaging;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.QueryParameters;


/**
 * class to map HTTP-GET-Request path: concept-descriptions.
 */
public class GetAllConceptDescriptionsByIsCaseOfRequestMapper
        extends AbstractRequestMapperWithOutputModifierAndPaging<GetAllConceptDescriptionsByIsCaseOfRequest, GetAllConceptDescriptionsByIsCaseOfResponse> {

    private static final String PATTERN = "concept-descriptions";

    public GetAllConceptDescriptionsByIsCaseOfRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN);
    }


    @Override
    public boolean matchesUrl(HttpRequest httpRequest) {
        return super.matchesUrl(httpRequest) && httpRequest.hasQueryParameter(QueryParameters.IS_CASE_OF);
    }


    @Override
    public GetAllConceptDescriptionsByIsCaseOfRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier, PagingInfo pagingInfo)
            throws InvalidRequestException {
        try {
            return GetAllConceptDescriptionsByIsCaseOfRequest.builder()
                    .isCaseOf(deserializer.read(
                            getParameterBase64UrlEncoded(httpRequest.getQueryParameters(), QueryParameters.IS_CASE_OF),
                            Reference.class))
                    .build();
        }
        catch (DeserializationException e) {
            throw new InvalidRequestException(
                    String.format("error deserializing %s (value: %s)", QueryParameters.IS_CASE_OF, httpRequest.getQueryParameter(QueryParameters.IS_CASE_OF)), e);
        }
    }
}
