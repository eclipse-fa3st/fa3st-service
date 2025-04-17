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
package org.eclipse.digitaltwin.fa3st.service.request.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotAContainerElementException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.api.Request;
import org.eclipse.digitaltwin.fa3st.common.model.api.Response;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ValueChangeEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.value.DataElementValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.ElementValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.mapper.ElementValueMapper;
import org.eclipse.digitaltwin.fa3st.common.util.DeepCopyHelper;
import org.eclipse.digitaltwin.fa3st.common.util.LambdaExceptionHelper;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceHelper;
import org.eclipse.digitaltwin.fa3st.service.persistence.Persistence;


/**
 * Base class for implementing code to execute a given Request.
 *
 * @param <I> type of the request
 * @param <O> type of the corresponding response
 */
public abstract class AbstractRequestHandler<I extends Request<O>, O extends Response> {

    /**
     * Creates a empty response object.
     *
     * @return new empty response object
     * @throws NoSuchMethodException if response type does not implement a parameterless constructor
     * @throws InstantiationException if response type is abstract
     * @throws InvocationTargetException if parameterless constructor of response type throws an exception
     * @throws IllegalAccessException if parameterless constructor of response type is inaccessible
     */
    public O newResponse() throws NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
        return (O) ConstructorUtils.invokeConstructor(
                TypeToken.of(getClass())
                        .resolveType(AbstractRequestHandler.class.getTypeParameters()[1])
                        .getRawType());
    }


    /**
     * Processes a request and returns the resulting response.
     *
     * @param request the request
     * @param context the execution context
     * @return the response
     * @throws Exception if processing the request fails
     */
    public abstract O process(I request, RequestExecutionContext context) throws Exception;


    /**
     * Check for each SubmodelElement if there is an AssetConnection.If yes read the value from it and compare it to the
     * current value.If they differ from each other update the submodelelement with the value from the AssetConnection.
     *
     * @param parent of the SubmodelElement List
     * @param submodelElements List of SubmodelElements which should be considered and updated
     * @param publishOnMessageBus if ValueChangeEventMessages should be sent on message bus
     * @param context the execution context
     * @throws ResourceNotFoundException if reference does not point to valid element
     * @throws ResourceNotAContainerElementException if reference does not point to valid element
     * @throws AssetConnectionException if reading value from asset connection fails
     * @throws org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException if mapping value read from
     *             asset connection fails
     * @throws org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException if publishing fails
     */
    protected void syncWithAsset(Reference parent, Collection<SubmodelElement> submodelElements, boolean publishOnMessageBus, RequestExecutionContext context)
            throws ResourceNotFoundException, ResourceNotAContainerElementException, AssetConnectionException, ValueMappingException, MessageBusException, PersistenceException {
        if (parent == null || submodelElements == null) {
            return;
        }
        Map<SubmodelElement, ElementValue> updatedSubmodelElements = new HashMap<>();
        for (SubmodelElement submodelElement: submodelElements) {
            Reference reference = AasUtils.toReference(parent, submodelElement);
            Optional<DataElementValue> newValue = context.getAssetConnectionManager().readValue(reference);
            if (newValue.isPresent()) {
                ElementValue oldValue = ElementValueMapper.toValue(submodelElement);
                if (!Objects.equals(oldValue, newValue.get())) {
                    updatedSubmodelElements.put(submodelElement, newValue.get());
                }
            }
            else if (SubmodelElementCollection.class.isAssignableFrom(submodelElement.getClass())) {
                syncWithAsset(reference, ((SubmodelElementCollection) submodelElement).getValue(), publishOnMessageBus, context);
            }
        }

        for (var update: updatedSubmodelElements.entrySet()) {
            Reference reference = AasUtils.toReference(parent, update.getKey());
            SubmodelElement oldElement = update.getKey();
            SubmodelElement newElement = DeepCopyHelper.deepCopy(oldElement, SubmodelElement.class);
            ElementValueMapper.setValue(newElement, update.getValue());
            context.getPersistence().update(reference, newElement);
            submodelElements.remove(oldElement);
            submodelElements.add(newElement);
            if (publishOnMessageBus) {
                context.getMessageBus().publish(ValueChangeEventMessage.builder()
                        .element(reference)
                        .oldValue(ElementValueMapper.toValue(oldElement))
                        .newValue(ElementValueMapper.toValue(newElement))
                        .build());
            }
        }
    }


    /**
     * Removes all asset connections to elements contained in this element.If there are no more providers registerd, the
     * asset connection is disconnected.
     *
     * @param parent reference to the parent element, e.g. a submodel
     * @param persistence persistence implementation needed to check if submodel elements still exist
     * @param context the execution context
     * @throws AssetConnectionException if disconnection fails
     */
    protected void cleanupDanglingAssetConnectionsForParent(Reference parent, Persistence persistence, RequestExecutionContext context) throws AssetConnectionException {
        Predicate<Reference> condition = x -> ReferenceHelper.startsWith(x, parent) && !persistence.submodelElementExists(x);
        context.getAssetConnectionManager().getConnections().stream()
                .forEach(LambdaExceptionHelper.rethrowConsumer(connection -> {
                    connection.getValueProviders().keySet().removeIf(condition);
                    connection.getOperationProviders().keySet().removeIf(condition);
                    connection.getSubscriptionProviders().keySet().removeIf(condition);
                    if (connection.getValueProviders().isEmpty()
                            && connection.getOperationProviders().isEmpty()
                            && connection.getSubscriptionProviders().isEmpty()) {
                        connection.disconnect();
                    }
                }));
    }


    /**
     * Creates an updated element based on a JSON merge patch.
     *
     * @param <T> the type of the element to update
     * @param patch the JSON merge patch containing the changes to apply
     * @param targetBean the original element to apply the update to
     * @param type the type information
     * @return the updated element
     * @throws org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException if applying the merge patch
     *             fails
     */
    protected <T> T applyMergePatch(JsonMergePatch patch, T targetBean, Class<T> type) throws InvalidRequestException {
        try {
            JsonNode json = new JsonSerializer().toNode(targetBean);
            JsonNode updatedJson = patch.apply(json);
            return new JsonDeserializer().read(updatedJson, type);
        }
        catch (JsonPatchException | IllegalArgumentException | DeserializationException e) {
            throw new InvalidRequestException("Error applying JSON merge patch", e);
        }
    }

}
