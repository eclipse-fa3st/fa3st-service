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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http.response.mapper;

import org.eclipse.digitaltwin.fa3st.common.model.api.request.aas.PostSubmodelReferenceRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.aas.PostSubmodelReferenceResponse;
import org.eclipse.digitaltwin.fa3st.common.util.EncodingHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;


/**
 * Response mapper for {@link PostSubmodelReferenceResponse}.
 */
public class PostSubmodelReferenceResponseMapper extends AbstractPostResponseWithLocationHeaderMapper<PostSubmodelReferenceResponse, PostSubmodelReferenceRequest> {

    public PostSubmodelReferenceResponseMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    protected String computeLocationHeader(PostSubmodelReferenceRequest apiRequest, PostSubmodelReferenceResponse apiResponse) {
        return String.format("/%s", EncodingHelper.base64UrlEncode(apiResponse.getPayload().getKeys().get(0).getValue()));
    }
}
