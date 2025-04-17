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
package org.eclipse.digitaltwin.fa3st.service.persistence.file;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.fa3st.common.dataformat.DeserializationException;
import org.eclipse.digitaltwin.fa3st.common.dataformat.EnvironmentSerializationManager;
import org.eclipse.digitaltwin.fa3st.common.dataformat.SerializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.ConfigurationException;
import org.eclipse.digitaltwin.fa3st.common.exception.ConfigurationInitializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotAContainerElementException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.SubmodelElementIdentifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.operation.OperationHandle;
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.Page;
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.PagingInfo;
import org.eclipse.digitaltwin.fa3st.common.model.persistence.AssetAdministrationShellSearchCriteria;
import org.eclipse.digitaltwin.fa3st.common.model.persistence.ConceptDescriptionSearchCriteria;
import org.eclipse.digitaltwin.fa3st.common.model.persistence.SubmodelElementSearchCriteria;
import org.eclipse.digitaltwin.fa3st.common.model.persistence.SubmodelSearchCriteria;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;
import org.eclipse.digitaltwin.fa3st.service.persistence.Persistence;
import org.eclipse.digitaltwin.fa3st.service.persistence.memory.PersistenceInMemory;
import org.eclipse.digitaltwin.fa3st.service.persistence.memory.PersistenceInMemoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of {@link org.eclipse.digitaltwin.fa3st.service.persistence.Persistence} for a file storage.
 *
 * <p>Following types are not supported in the current version:
 * <ul>
 * <li>AASX packages
 * <li>Package Descriptors
 * </ul>
 */
public class PersistenceFile implements Persistence<PersistenceFileConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceFile.class);
    private static final String OPERATION_STATES_FILENAME = "operation-states.json";
    private final ObjectMapper mapper;
    private PersistenceFileConfig config;
    private PersistenceInMemory persistence;
    private File operationStatesFile;

    public PersistenceFile() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addKeySerializer(OperationHandle.class, new JsonSerializer<OperationHandle>() {
            @Override
            public void serialize(OperationHandle value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeFieldName(value.getHandleId());
            }
        });
        module.addKeyDeserializer(OperationHandle.class, new KeyDeserializer() {
            @Override
            public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
                return OperationHandle.builder()
                        .handleId(key)
                        .build();
            }
        });
    }


    @Override
    public PersistenceFileConfig asConfig() {
        return config;
    }


    @Override
    public void init(CoreConfig coreConfig, PersistenceFileConfig config, ServiceContext context) throws ConfigurationInitializationException {
        this.config = config;
        try {
            config.init();
            Environment aasEnvironment = config.loadInitialModel();
            persistence = PersistenceInMemoryConfig.builder()
                    .initialModel(aasEnvironment)
                    .build()
                    .newInstance(coreConfig, context);
            saveEnvironment();
            operationStatesFile = Path.of(config.getDataDir(), OPERATION_STATES_FILENAME).toFile();
            loadOperationStates();
        }
        catch (ConfigurationException | DeserializationException e) {
            throw new ConfigurationInitializationException("initializing file persistence failed", e);
        }
    }


    @Override
    public void start() throws PersistenceException {
        //intentionally left empty
    }


    @Override
    public void stop() {
        //intentionally left empty
    }


    @Override
    public AssetAdministrationShell getAssetAdministrationShell(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return persistence.getAssetAdministrationShell(id, modifier);
    }


    @Override
    public Submodel getSubmodel(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return persistence.getSubmodel(id, modifier);
    }


    @Override
    public ConceptDescription getConceptDescription(String id, QueryModifier modifier) throws ResourceNotFoundException {
        return persistence.getConceptDescription(id, modifier);
    }


    @Override
    public SubmodelElement getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier) throws ResourceNotFoundException {
        return persistence.getSubmodelElement(identifier, modifier);
    }


    @Override
    public Page<Reference> getSubmodelRefs(String aasId, PagingInfo paging) throws ResourceNotFoundException {
        return persistence.getSubmodelRefs(aasId, paging);
    }


    @Override
    public OperationResult getOperationResult(OperationHandle handle) throws ResourceNotFoundException {
        return persistence.getOperationResult(handle);
    }


    @Override
    public Page<AssetAdministrationShell> findAssetAdministrationShells(AssetAdministrationShellSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        return persistence.findAssetAdministrationShells(criteria, modifier, paging);
    }


    @Override
    public Page<Submodel> findSubmodels(SubmodelSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        return persistence.findSubmodels(criteria, modifier, paging);
    }


    @Override
    public Page<SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws ResourceNotFoundException {
        return persistence.findSubmodelElements(criteria, modifier, paging);
    }


    @Override
    public Page<ConceptDescription> findConceptDescriptions(ConceptDescriptionSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) {
        return persistence.findConceptDescriptions(criteria, modifier, paging);
    }


    @Override
    public void save(AssetAdministrationShell assetAdministrationShell) {
        persistence.save(assetAdministrationShell);
        saveEnvironment();
    }


    @Override
    public void save(ConceptDescription conceptDescription) {
        persistence.save(conceptDescription);
        saveEnvironment();
    }


    @Override
    public void save(Submodel submodel) {
        persistence.save(submodel);
        saveEnvironment();
    }


    @Override
    public void insert(SubmodelElementIdentifier parentIdentifier, SubmodelElement submodelElement) throws ResourceNotFoundException, ResourceNotAContainerElementException {
        persistence.insert(parentIdentifier, submodelElement);
        saveEnvironment();
    }


    @Override
    public void update(SubmodelElementIdentifier identifier, SubmodelElement submodelElement) throws ResourceNotFoundException {
        persistence.update(identifier, submodelElement);
        saveEnvironment();
    }


    @Override
    public void save(OperationHandle handle, OperationResult result) {
        persistence.save(handle, result);
        saveOperationStates();
    }


    @Override
    public void deleteAssetAdministrationShell(String id) throws ResourceNotFoundException {
        persistence.deleteAssetAdministrationShell(id);
        saveEnvironment();
    }


    @Override
    public void deleteSubmodel(String id) throws ResourceNotFoundException {
        persistence.deleteSubmodel(id);
        saveEnvironment();
    }


    @Override
    public void deleteConceptDescription(String id) throws ResourceNotFoundException {
        persistence.deleteConceptDescription(id);
        saveEnvironment();
    }


    @Override
    public void deleteSubmodelElement(SubmodelElementIdentifier identifier) throws ResourceNotFoundException {
        persistence.deleteSubmodelElement(identifier);
        saveEnvironment();
    }


    @Override
    public void deleteAll() throws PersistenceException {
        persistence.deleteAll();
        saveEnvironment();
    }


    private void saveEnvironment() {
        try {
            EnvironmentSerializationManager
                    .serializerFor(config.getDataformat())
                    .write(new File(String.valueOf(config.getFilePath())), persistence.getEnvironment());
        }
        catch (IOException | SerializationException e) {
            LOGGER.error(String.format("Could not save environment to file %s", config.getFilePath()), e);
        }
    }


    private void saveOperationStates() {
        try {
            mapper.writeValue(operationStatesFile, persistence.getOperationStates());
        }
        catch (IOException e) {
            LOGGER.error(String.format("Error persisting operation states to file %s", operationStatesFile.getAbsolutePath()), e);
        }
    }


    private void loadOperationStates() {
        try {
            if (Objects.nonNull(operationStatesFile) && operationStatesFile.exists()) {
                persistence.setOperationStates(
                        mapper.readValue(
                                operationStatesFile,
                                new TypeReference<HashMap<OperationHandle, OperationResult>>() {}));
            }
        }
        catch (IOException e) {
            LOGGER.error(String.format("Error loading operation states from file %s", operationStatesFile.getAbsolutePath()), e);
        }
    }

}
