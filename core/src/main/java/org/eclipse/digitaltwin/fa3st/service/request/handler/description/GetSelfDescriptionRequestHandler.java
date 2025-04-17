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
package org.eclipse.digitaltwin.fa3st.service.request.handler.description;

import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.ServiceDescription;
import org.eclipse.digitaltwin.fa3st.common.model.api.StatusCode;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.description.GetSelfDescriptionRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.description.GetSelfDescriptionResponse;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.description.GetSelfDescriptionRequest}.
 */
public class GetSelfDescriptionRequestHandler extends AbstractRequestHandler<GetSelfDescriptionRequest, GetSelfDescriptionResponse> {

    @Override
    public GetSelfDescriptionResponse process(GetSelfDescriptionRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, AssetConnectionException, ValueMappingException {
        return GetSelfDescriptionResponse.builder()
                .payload(
                        context.hasEndpoint()
                                ? ServiceDescription.builder()
                                        .profiles(context.getEndpoint().getProfiles())
                                        .build()
                                : ServiceDescription.builder().build())
                .statusCode(StatusCode.SUCCESS)
                .build();
    }

}
