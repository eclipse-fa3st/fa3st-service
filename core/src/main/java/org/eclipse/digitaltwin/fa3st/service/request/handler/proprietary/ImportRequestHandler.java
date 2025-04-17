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
package org.eclipse.digitaltwin.fa3st.service.request.handler.proprietary;

import com.google.common.net.MediaType;
import java.io.ByteArrayInputStream;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.fa3st.common.dataformat.DeserializationException;
import org.eclipse.digitaltwin.fa3st.common.dataformat.EnvironmentSerializationManager;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceAlreadyExistsException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.EnvironmentContext;
import org.eclipse.digitaltwin.fa3st.common.model.InMemoryFile;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.proprietary.ImportRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.proprietary.ImportResponse;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.proprietary.ImportResult;
import org.eclipse.digitaltwin.fa3st.common.model.messagebus.event.change.ElementCreateEventMessage;
import org.eclipse.digitaltwin.fa3st.common.model.serialization.DataFormat;
import org.eclipse.digitaltwin.fa3st.common.util.StreamHelper;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to handle a {@link ImportRequest}.
 */
public class ImportRequestHandler extends AbstractRequestHandler<ImportRequest, ImportResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestHandler.class);

    @Override
    public ImportResponse process(ImportRequest request, RequestExecutionContext context) throws ResourceNotFoundException, DeserializationException {
        EnvironmentContext environmentContext = EnvironmentSerializationManager
                .deserializerFor(
                        DataFormat.forContentType(
                                MediaType.parse(request.getContentType())))
                .read(new ByteArrayInputStream(request.getContent()));
        ImportResult.Builder result = ImportResult.builder();
        StreamHelper.concat(
                environmentContext.getEnvironment().getAssetAdministrationShells().stream(),
                environmentContext.getEnvironment().getSubmodels().stream(),
                environmentContext.getEnvironment().getConceptDescriptions().stream())
                .forEach(x -> {
                    try {
                        saveAndSend(x, context);
                    }
                    catch (PersistenceException | ResourceAlreadyExistsException e) {
                        result.modelError(AasUtils.toReference(x), e.getMessage());
                    }
                });
        environmentContext.getFiles().forEach(x -> {
            try {
                context.getFileStorage().save(
                        InMemoryFile.builder()
                                .content(x.getFileContent())
                                .path(x.getPath())
                                .build());
            }
            catch (PersistenceException e) {
                result.fileError(x.getPath(), e.getMessage());
            }
        });
        return ImportResponse.builder()
                .success()
                .payload(result.build())
                .build();
    }


    private void saveAndSend(Identifiable identifiable, RequestExecutionContext context) throws PersistenceException, ResourceAlreadyExistsException {
        try {
            if (identifiable instanceof AssetAdministrationShell aas) {
                if (context.getPersistence().assetAdministrationShellExists(aas.getId())) {
                    throw new ResourceAlreadyExistsException(AasUtils.toReference(aas));
                }
                context.getPersistence().save(aas);
            }
            else if (identifiable instanceof Submodel submodel) {
                if (context.getPersistence().submodelExists(submodel.getId())) {
                    throw new ResourceAlreadyExistsException(AasUtils.toReference(submodel));
                }
                context.getPersistence().save(submodel);
            }
            else if (identifiable instanceof ConceptDescription cd) {
                if (context.getPersistence().conceptDescriptionExists(cd.getId())) {
                    throw new ResourceAlreadyExistsException(AasUtils.toReference(cd));
                }
                context.getPersistence().save(cd);
            }
            context.getMessageBus().publish(ElementCreateEventMessage.builder()
                    .value(identifiable)
                    .element(identifiable)
                    .build());
        }
        catch (MessageBusException e) {
            LOGGER.warn("Publishing ElementCreateEvent on message bus after import failed (reference: {})", AasUtils.toReference(identifiable));
        }
    }
}
