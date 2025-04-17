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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.aasserialization;

import com.google.common.net.MediaType;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.model.api.Request;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aasserialization.GenerateSerializationByIdsRequest;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.common.model.serialization.DataFormat;
import org.eclipse.digitaltwin.fa3st.common.util.BooleanHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.model.HttpRequest;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.AbstractRequestMapper;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.QueryParameters;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.util.HttpConstants;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.util.HttpHelper;


/**
 * class to map HTTP-GET-Request path: /serialization.
 */
public class GenerateSerializationByIdsRequestMapper extends AbstractRequestMapper {

    private static final String PATTERN = "serialization";

    public GenerateSerializationByIdsRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN);
    }


    @Override
    public Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        GenerateSerializationByIdsRequest.Builder builder = GenerateSerializationByIdsRequest.builder();
        if (httpRequest.hasQueryParameter(QueryParameters.AAS_IDS)) {
            builder.aasIds(parseAndDecodeQueryParameter(httpRequest.getQueryParameters(), QueryParameters.AAS_IDS));
        }
        if (httpRequest.hasQueryParameter(QueryParameters.SUBMODEL_IDS)) {
            builder.submodelIds(parseAndDecodeQueryParameter(httpRequest.getQueryParameters(), QueryParameters.SUBMODEL_IDS));
        }
        if (httpRequest.hasQueryParameter(QueryParameters.INCLUDE_CONCEPT_DESCRIPTIONS)) {
            try {
                builder.includeConceptDescriptions(
                        BooleanHelper.parseStrictIgnoreCase(
                                httpRequest.getQueryParameter(QueryParameters.INCLUDE_CONCEPT_DESCRIPTIONS)));
            }
            catch (IllegalArgumentException e) {
                throw new InvalidRequestException(
                        String.format("invalid query parameter, must be valid boolean (name: %s, value: %s)",
                                QueryParameters.INCLUDE_CONCEPT_DESCRIPTIONS,
                                httpRequest.getQueryParameter(QueryParameters.INCLUDE_CONCEPT_DESCRIPTIONS)),
                        e);
            }
        }
        if (httpRequest.hasHeader(HttpConstants.HEADER_ACCEPT)) {
            builder.serializationFormat(determineDataFormat(httpRequest.getHeader(HttpConstants.HEADER_ACCEPT)));
        }
        return builder.build();
    }


    private static DataFormat determineDataFormat(String acceptHeaderValue) throws InvalidRequestException {
        List<MediaType> acceptedTypes = HttpHelper.parseCommaSeparatedList(acceptHeaderValue)
                .stream()
                .map(MediaType::parse)
                .map(MediaType::withoutParameters)
                .toList();
        for (MediaType type: acceptedTypes) {
            Optional<DataFormat> match = Stream.of(DataFormat.values())
                    .filter(x -> x.getContentType().withoutParameters().is(type))
                    .sorted(Comparator.comparingInt(DataFormat::getPriority))
                    .findFirst();
            if (match.isPresent()) {
                return match.get();
            }
        }
        throw new InvalidRequestException(String.format("requested data format not valid (%s)", acceptHeaderValue));
    }


    private List<String> parseAndDecodeQueryParameter(Map<String, String> parameters, String parameterName) throws InvalidRequestException {
        return HttpHelper.parseCommaSeparatedList(getParameterBase64UrlEncoded(parameters, parameterName));
    }
}
