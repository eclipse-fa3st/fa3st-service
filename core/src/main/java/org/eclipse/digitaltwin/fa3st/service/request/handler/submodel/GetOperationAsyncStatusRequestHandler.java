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
package org.eclipse.digitaltwin.fa3st.service.request.handler.submodel;

import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.GetOperationAsyncStatusRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.GetOperationAsyncStatusResponse;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.GetOperationAsyncStatusRequest}.
 */
public class GetOperationAsyncStatusRequestHandler extends AbstractRequestHandler<GetOperationAsyncStatusRequest, GetOperationAsyncStatusResponse> {

    @Override
    public GetOperationAsyncStatusResponse process(GetOperationAsyncStatusRequest request, RequestExecutionContext context) throws ResourceNotFoundException, PersistenceException {
        return GetOperationAsyncStatusResponse.builder()
                .payload(context.getPersistence().getOperationResult(request.getHandle()))
                .success()
                .build();
    }
}
