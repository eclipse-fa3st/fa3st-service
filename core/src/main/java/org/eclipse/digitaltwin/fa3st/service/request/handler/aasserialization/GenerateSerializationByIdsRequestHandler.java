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
package org.eclipse.digitaltwin.fa3st.service.request.handler.aasserialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.InMemoryFile;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.fa3st.common.dataformat.SerializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.EnvironmentContext;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Content;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Extent;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Level;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.OutputModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.PagingInfo;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.aasserialization.GenerateSerializationByIdsRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.aasserialization.GenerateSerializationByIdsResponse;
import org.eclipse.digitaltwin.fa3st.common.model.persistence.ConceptDescriptionSearchCriteria;
import org.eclipse.digitaltwin.fa3st.common.model.visitor.AssetAdministrationShellElementWalker;
import org.eclipse.digitaltwin.fa3st.common.model.visitor.DefaultAssetAdministrationShellElementVisitor;
import org.eclipse.digitaltwin.fa3st.common.util.LambdaExceptionHelper;
import org.eclipse.digitaltwin.fa3st.service.request.handler.AbstractRequestHandler;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.request.aasserialization.GenerateSerializationByIdsRequest} in
 * the service and to send the corresponding response
 * {@link org.eclipse.digitaltwin.fa3st.common.model.api.response.aasserialization.GenerateSerializationByIdsResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GenerateSerializationByIdsRequestHandler extends AbstractRequestHandler<GenerateSerializationByIdsRequest, GenerateSerializationByIdsResponse> {

    private static final OutputModifier OUTPUT_MODIFIER = new OutputModifier.Builder()
            .content(Content.NORMAL)
            .extent(Extent.WITH_BLOB_VALUE)
            .level(Level.DEEP)
            .build();

    @Override
    public GenerateSerializationByIdsResponse process(GenerateSerializationByIdsRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, SerializationException, IOException, PersistenceException {
        DefaultEnvironment environment;
        if (request.getAasIds().isEmpty() && request.getSubmodelIds().isEmpty()) {
            environment = new DefaultEnvironment.Builder()
                    .assetAdministrationShells(context.getPersistence().getAllAssetAdministrationShells(OutputModifier.DEFAULT, PagingInfo.ALL).getContent())
                    .submodels(context.getPersistence().getAllSubmodels(OutputModifier.DEFAULT, PagingInfo.ALL).getContent())
                    .conceptDescriptions(context.getPersistence().getAllConceptDescriptions(OutputModifier.DEFAULT, PagingInfo.ALL).getContent())
                    .build();
        }
        else {
            environment = new DefaultEnvironment.Builder()
                    .assetAdministrationShells(request.getAasIds().stream()
                            .map(LambdaExceptionHelper.rethrowFunction(x -> context.getPersistence().getAssetAdministrationShell(x, OUTPUT_MODIFIER),
                                    ResourceNotFoundException.class,
                                    PersistenceException.class))
                            .collect(Collectors.toList()))
                    .submodels(request.getSubmodelIds().stream()
                            .map(LambdaExceptionHelper.rethrowFunction(x -> context.getPersistence().getSubmodel(x, OUTPUT_MODIFIER),
                                    ResourceNotFoundException.class,
                                    PersistenceException.class))
                            .collect(Collectors.toList()))
                    .conceptDescriptions(request.getIncludeConceptDescriptions()
                            ? context.getPersistence().findConceptDescriptions(
                                    ConceptDescriptionSearchCriteria.NONE,
                                    OUTPUT_MODIFIER,
                                    PagingInfo.ALL)
                                    .getContent()
                            : List.of())
                    .build();
        }
        List<InMemoryFile> files = new ArrayList<>();
        AssetAdministrationShellElementWalker.builder()
                .visitor(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(File file) {
                        try {
                            if (context.getFileStorage().contains(file.getValue())) {
                                files.add(new InMemoryFile(context.getFileStorage().get(file.getValue()), file.getValue()));
                            }
                        }
                        catch (ResourceNotFoundException | PersistenceException e) {
                            //intentionally empty
                        }
                    }
                })
                .build()
                .walk(environment);
        files.addAll(environment.getAssetAdministrationShells().stream()
                .filter(Objects::nonNull)
                .filter(x -> Objects.nonNull(x.getAssetInformation()))
                .filter(x -> Objects.nonNull(x.getAssetInformation().getDefaultThumbnail()))
                .filter(x -> Objects.nonNull(x.getAssetInformation().getDefaultThumbnail().getPath()))
                .distinct()
                .map(x -> x.getAssetInformation().getDefaultThumbnail().getPath())
                .map(LambdaExceptionHelper.rethrowFunction(x -> new InMemoryFile(context.getFileStorage().get(x), x),
                        ResourceNotFoundException.class,
                        PersistenceException.class))
                .collect(Collectors.toList()));
        return GenerateSerializationByIdsResponse.builder()
                .dataformat(request.getSerializationFormat())
                .payload(EnvironmentContext.builder()
                        .environment(environment)
                        .files(files)
                        .build())
                .success()
                .build();
    }
}
