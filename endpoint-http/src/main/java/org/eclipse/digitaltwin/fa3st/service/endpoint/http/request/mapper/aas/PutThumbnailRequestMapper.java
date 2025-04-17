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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.aas;

import static org.eclipse.digitaltwin.fa3st.service.endpoint.http.util.HttpConstants.HEADER_CONTENT_TYPE;

import com.google.common.net.MediaType;
import java.util.Map;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.model.TypedInMemoryFile;
import org.eclipse.digitaltwin.fa3st.common.model.api.Request;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aas.PutThumbnailRequest;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.common.util.RegExHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.model.HttpRequest;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.AbstractRequestMapper;


/**
 * class to map HTTP-PUT-Request path: shells/{aasIdentifier}/asset-information/thumbnail.
 */
public class PutThumbnailRequestMapper extends AbstractRequestMapper {

    private static final String AAS_ID = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("shells/%s/asset-information/thumbnail", pathElement(AAS_ID));

    public PutThumbnailRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.PUT, PATTERN);
    }


    @Override
    public Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        MediaType contentType = MediaType.parse(httpRequest.getHeader(HEADER_CONTENT_TYPE));
        Map<String, TypedInMemoryFile> multipart = parseMultiPartBody(httpRequest, contentType);
        return PutThumbnailRequest.builder()
                .id(getParameterBase64UrlEncoded(urlParameters, AAS_ID))
                .content(new TypedInMemoryFile.Builder()
                        .content(multipart.get("file").getContent())
                        .contentType(multipart.get("file").getContentType())
                        .path(new String(multipart.get("fileName").getContent()))
                        .build())
                .build();
    }
}
