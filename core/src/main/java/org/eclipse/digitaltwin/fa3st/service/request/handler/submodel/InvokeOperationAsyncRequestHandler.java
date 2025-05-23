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

import java.util.Arrays;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationResult;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.api.Message;
import org.eclipse.digitaltwin.fa3st.common.model.api.StatusCode;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.operation.OperationHandle;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.InvokeOperationAsyncRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.InvokeOperationAsyncResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.OperationFinishEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.OperationInvokeEventMessage;
import org.eclipse.digitaltwin.fa3st.common.util.ElementValueHelper;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetOperationProviderConfig;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.InvokeOperationAsyncRequest} in the service
 * and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.InvokeOperationAsyncResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class InvokeOperationAsyncRequestHandler extends AbstractInvokeOperationRequestHandler<InvokeOperationAsyncRequest, InvokeOperationAsyncResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvokeOperationAsyncRequestHandler.class);

    private void handleOperationSuccess(Reference reference, OperationHandle operationHandle, OperationVariable[] inoutput, OperationVariable[] output,
                                        RequestExecutionContext context) {
        handleOperationResult(
                reference,
                operationHandle,
                new DefaultOperationResult.Builder()
                        .executionState(ExecutionState.COMPLETED)
                        .inoutputArguments(Arrays.asList(inoutput))
                        .outputArguments(Arrays.asList(output))
                        .success(true)
                        .build(),
                context);
    }


    private void handleOperationFailure(Reference reference, List<OperationVariable> inoutput, OperationHandle operationHandle, Throwable error, RequestExecutionContext context) {
        handleOperationResult(
                reference,
                operationHandle,
                new DefaultOperationResult.Builder()
                        .executionState(ExecutionState.FAILED)
                        .inoutputArguments(inoutput)
                        .outputArguments(List.of())
                        .messages(Message.builder()
                                .messageType(MessageTypeEnum.ERROR)
                                .text(String.format(
                                        "operation failed to execute (reason: %s)",
                                        error.getMessage()))
                                .build())
                        .success(false)
                        .build(),
                context);
    }


    private void handleOperationResult(Reference reference,
                                       OperationHandle operationHandle,
                                       OperationResult operationResult,
                                       RequestExecutionContext context) {
        try {
            Operation operation = context.getPersistence().getSubmodelElement(reference, QueryModifier.MINIMAL, Operation.class);
            AssetOperationProviderConfig config = context.getAssetConnectionManager().getOperationProvider(reference).getConfig();
            if (operationResult.getSuccess()) {
                operationResult.setOutputArguments(
                        validateAndPrepare(
                                operation.getOutputVariables(),
                                operationResult.getOutputArguments(),
                                config.getOutputValidationMode(),
                                ArgumentType.OUTPUT));
            }
        }
        catch (ResourceNotFoundException | InvalidRequestException | PersistenceException e) {
            handleOperationFailure(reference, operationResult.getInoutputArguments(), operationHandle, e, context);
        }

        try {
            context.getPersistence().save(operationHandle, operationResult);
            context.getMessageBus().publish(OperationFinishEventMessage.builder()
                    .element(reference)
                    .inoutput(ElementValueHelper.toValueMap(operationResult.getInoutputArguments()))
                    .output(ElementValueHelper.toValueMap(operationResult.getOutputArguments()))
                    .build());
        }
        catch (ValueMappingException | MessageBusException | PersistenceException e) {
            LOGGER.warn("could not publish OperationFinishedEventMessage on messagebus", e);
        }
    }


    private void handleOperationInvoke(Reference reference,
                                       OperationHandle operationHandle,
                                       InvokeOperationAsyncRequest request,
                                       RequestExecutionContext context) {

        try {
            context.getPersistence().save(
                    operationHandle,
                    new DefaultOperationResult.Builder()
                            .inoutputArguments(request.getInoutputArguments())
                            .executionState(ExecutionState.RUNNING)
                            .build());
            context.getMessageBus().publish(OperationInvokeEventMessage.builder()
                    .element(reference)
                    .input(ElementValueHelper.toValueMap(request.getInputArguments()))
                    .inoutput(ElementValueHelper.toValueMap(request.getInoutputArguments()))
                    .build());
        }
        catch (ValueMappingException | MessageBusException | PersistenceException e) {
            LOGGER.warn("could not publish OperationFinishedEventMessage on messagebus", e);
        }
    }


    @Override
    protected InvokeOperationAsyncResponse executeOperation(Reference reference, InvokeOperationAsyncRequest request, RequestExecutionContext context) {
        OperationHandle operationHandle = new OperationHandle();
        handleOperationInvoke(reference, operationHandle, request, context);

        // The timeout is ignored, as the server may choose to take it into account or ignore it.
        try {
            context.getAssetConnectionManager().getOperationProvider(reference).invokeAsync(
                    request.getInputArguments().toArray(new OperationVariable[0]),
                    request.getInoutputArguments().toArray(new OperationVariable[0]),
                    (output, inoutput) -> handleOperationSuccess(reference, operationHandle, inoutput, output, context),
                    error -> handleOperationFailure(reference, request.getInoutputArguments(), operationHandle, error, context));
        }
        catch (Exception e) {
            handleOperationFailure(reference, request.getInoutputArguments(), operationHandle, e, context);
        }
        return InvokeOperationAsyncResponse.builder()
                .payload(operationHandle)
                .statusCode(StatusCode.SUCCESS_ACCEPTED)
                .build();
    }
}
