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

import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.GetOperationAsyncResultRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.GetOperationAsyncResultResponse;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.GetOperationAsyncResultRequest} in the
 * service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.GetOperationAsyncResultResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetOperationAsyncResultRequestHandler extends AbstractRequestHandler<GetOperationAsyncResultRequest, GetOperationAsyncResultResponse> {

    @Override
    public GetOperationAsyncResultResponse process(GetOperationAsyncResultRequest request, RequestExecutionContext context) throws ResourceNotFoundException, PersistenceException {
        OperationResult operationResult = context.getPersistence().getOperationResult(request.getHandle());
        return GetOperationAsyncResultResponse.builder()
                .payload(operationResult)
                .success()
                .build();
    }
}
