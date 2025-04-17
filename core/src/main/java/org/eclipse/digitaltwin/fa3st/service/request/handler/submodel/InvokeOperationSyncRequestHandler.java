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
import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationResult;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.InvokeOperationSyncRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.InvokeOperationSyncResponse;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.OperationFinishEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.access.OperationInvokeEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.error.ErrorEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.error.ErrorLevel;
import org.eclipse.digitaltwin.fa3st.common.util.ElementValueHelper;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceBuilder;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetOperationProvider;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetOperationProviderConfig;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to handle a {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.submodel.InvokeOperationSyncRequest}
 * in the service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.submodel.InvokeOperationSyncResponse}. Is responsible
 * for communication with the persistence and sends the corresponding events to the message bus.
 */
public class InvokeOperationSyncRequestHandler extends AbstractInvokeOperationRequestHandler<InvokeOperationSyncRequest, InvokeOperationSyncResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvokeOperationSyncRequestHandler.class);

    @Override
    public InvokeOperationSyncResponse doProcess(InvokeOperationSyncRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, InvalidRequestException, PersistenceException {
        InvokeOperationSyncResponse result = super.doProcess(request, context);
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        Operation operation = context.getPersistence().getSubmodelElement(reference, QueryModifier.MINIMAL, Operation.class);
        AssetOperationProviderConfig config = context.getAssetConnectionManager().getOperationProvider(reference).getConfig();
        if (result.getPayload().getSuccess()) {
            result.getPayload().setOutputArguments(
                    validateAndPrepare(
                            operation.getOutputVariables(),
                            result.getPayload().getOutputArguments(),
                            config.getOutputValidationMode(),
                            ArgumentType.OUTPUT));
        }
        return result;
    }


    @Override
    protected InvokeOperationSyncResponse executeOperation(Reference reference, InvokeOperationSyncRequest request, RequestExecutionContext context) {
        if (!request.isInternal()) {
            try {
                publishSafe(OperationInvokeEventMessage.builder()
                        .element(reference)
                        .input(ElementValueHelper.toValueMap(request.getInputArguments()))
                        .inoutput(ElementValueHelper.toValueMap(request.getInoutputArguments()))
                        .build(),
                        context);
            }
            catch (ValueMappingException e) {
                String message = String.format("Publishing OperationInvokeEvent on message bus failed (reason: %s)", e.getMessage());
                LOGGER.warn(message, e);
                publishSafe(ErrorEventMessage.builder()
                        .element(reference)
                        .level(ErrorLevel.WARN)
                        .message(message)
                        .build(),
                        context);
            }
        }
        AssetOperationProvider assetOperationProvider = context.getAssetConnectionManager().getOperationProvider(reference);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<OperationVariable[]> future = executor.submit(new Callable<OperationVariable[]>() {
            @Override
            public OperationVariable[] call() throws Exception {
                return assetOperationProvider.invoke(
                        request.getInputArguments().toArray(new OperationVariable[0]),
                        request.getInoutputArguments().toArray(new OperationVariable[0]));
            }
        });
        OperationResult result;
        try {
            OperationVariable[] outputVariables = future.get(request.getTimeout().getTimeInMillis(Calendar.getInstance()), TimeUnit.MILLISECONDS);
            result = new DefaultOperationResult.Builder()
                    .executionState(ExecutionState.COMPLETED)
                    .inoutputArguments(request.getInoutputArguments())
                    .outputArguments(Arrays.asList(outputVariables))
                    .success(true)
                    .build();
        }
        catch (TimeoutException e) {
            future.cancel(true);
            result = new DefaultOperationResult.Builder()
                    .inoutputArguments(request.getInoutputArguments())
                    .executionState(ExecutionState.TIMEOUT)
                    .success(false)
                    .build();
            Thread.currentThread().interrupt();
        }
        catch (InterruptedException | ExecutionException e) {
            result = new DefaultOperationResult.Builder()
                    .inoutputArguments(request.getInoutputArguments())
                    .executionState(ExecutionState.FAILED)
                    .success(false)
                    .build();
            Thread.currentThread().interrupt();
        }
        finally {
            executor.shutdown();
        }
        if (!request.isInternal()) {
            try {
                publishSafe(OperationFinishEventMessage.builder()
                        .element(reference)
                        .inoutput(ElementValueHelper.toValueMap(result.getInoutputArguments()))
                        .output(ElementValueHelper.toValueMap(result.getOutputArguments()))
                        .build(),
                        context);
            }
            catch (ValueMappingException e) {
                String message = String.format("Publishing OperationFinishEvent on message bus failed (reason: %s)", e.getMessage());
                LOGGER.warn(message, e);
                publishSafe(ErrorEventMessage.builder()
                        .element(reference)
                        .level(ErrorLevel.WARN)
                        .message(message)
                        .build(),
                        context);
            }
        }
        return InvokeOperationSyncResponse.builder()
                .payload(result)
                .success()
                .build();
    }
}
