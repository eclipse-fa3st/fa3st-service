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
package org.eclipse.digitaltwin.fa3st.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.ConfigurationException;
import org.eclipse.digitaltwin.fa3st.common.exception.EndpointException;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidConfigurationException;
import org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.api.InternalErrorResponse;
import org.eclipse.digitaltwin.fa3st.common.model.api.Request;
import org.eclipse.digitaltwin.fa3st.common.model.api.Response;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.PagingInfo;
import org.eclipse.digitaltwin.fa3st.common.model.persistence.AssetAdministrationShellSearchCriteria;
import org.eclipse.digitaltwin.fa3st.common.model.persistence.ConceptDescriptionSearchCriteria;
import org.eclipse.digitaltwin.fa3st.common.model.persistence.SubmodelSearchCriteria;
import org.eclipse.digitaltwin.fa3st.common.model.serialization.DataFormat;
import org.eclipse.digitaltwin.fa3st.common.typing.TypeExtractor;
import org.eclipse.digitaltwin.fa3st.common.typing.TypeInfo;
import org.eclipse.digitaltwin.fa3st.common.util.Ensure;
import org.eclipse.digitaltwin.fa3st.common.util.FileHelper;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceHelper;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetConnection;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetConnectionConfig;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetConnectionManager;
import org.eclipse.digitaltwin.fa3st.service.config.CoreConfig;
import org.eclipse.digitaltwin.fa3st.service.config.ServiceConfig;
import org.eclipse.digitaltwin.fa3st.service.endpoint.Endpoint;
import org.eclipse.digitaltwin.fa3st.service.endpoint.EndpointConfig;
import org.eclipse.digitaltwin.fa3st.service.filestorage.FileStorage;
import org.eclipse.digitaltwin.fa3st.service.messagebus.MessageBus;
import org.eclipse.digitaltwin.fa3st.service.persistence.Persistence;
import org.eclipse.digitaltwin.fa3st.service.registry.RegistrySynchronization;
import org.eclipse.digitaltwin.fa3st.service.request.RequestHandlerManager;
import org.eclipse.digitaltwin.fa3st.service.request.handler.DynamicRequestExecutionContext;
import org.eclipse.digitaltwin.fa3st.service.request.handler.RequestExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Central class of the FA³ST Service accumulating and connecting all different
 * components.
 */
public class Service implements ServiceContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);
    private final ServiceConfig config;
    private AssetConnectionManager assetConnectionManager;
    private List<Endpoint> endpoints;
    private MessageBus messageBus;
    private Persistence persistence;
    private FileStorage fileStorage;
    private RequestExecutionContext requestExecutionContext;

    private RegistrySynchronization registrySynchronization;
    private RequestHandlerManager requestHandler;

    /**
     * Creates a new instance of {@link Service}.
     *
     * @param coreConfig core configuration
     * @param persistence persistence implementation
     * @param fileStorage fileStorage implementation
     * @param messageBus message bus implementation
     * @param endpoints endpoints
     * @param assetConnections asset connections
     * @throws IllegalArgumentException if coreConfig is null
     * @throws IllegalArgumentException if persistence is null
     * @throws IllegalArgumentException if messageBus is null
     * @throws RuntimeException if creating a deep copy of aasEnvironment fails
     * @throws ConfigurationException the configuration the
     *             {@link AssetConnectionManager} fails
     * @throws AssetConnectionException when initializing asset connections
     *             fails
     */
    public Service(CoreConfig coreConfig,
            Persistence persistence,
            FileStorage fileStorage,
            MessageBus messageBus,
            List<Endpoint> endpoints,
            List<AssetConnection> assetConnections) throws ConfigurationException, AssetConnectionException {
        Ensure.requireNonNull(coreConfig, "coreConfig must be non-null");
        Ensure.requireNonNull(persistence, "persistence must be non-null");
        Ensure.requireNonNull(messageBus, "messageBus must be non-null");
        if (endpoints == null) {
            this.endpoints = new ArrayList<>();
            LOGGER.warn("no endpoint configuration found, starting service without endpoint which means the service will not be accessible via any kind of API");
        }
        else {
            this.endpoints = endpoints;
        }
        this.config = ServiceConfig.builder()
                .core(coreConfig)
                .build();
        this.persistence = persistence;
        this.fileStorage = fileStorage;
        this.messageBus = messageBus;
        this.assetConnectionManager = new AssetConnectionManager(config.getCore(), assetConnections, this);
        this.requestHandler = new RequestHandlerManager(config.getCore());
        this.requestExecutionContext = new DynamicRequestExecutionContext(this);
        this.registrySynchronization = new RegistrySynchronization(config.getCore(), persistence, messageBus, endpoints);
    }


    /**
     * Creates a new instance of {@link Service}.
     *
     * @param config service configuration
     * @throws IllegalArgumentException if config is null
     * @throws ConfigurationException if invalid configuration is provided
     * @throws AssetConnectionException when initializing asset connections
     *             fails
     */
    public Service(ServiceConfig config)
            throws ConfigurationException, AssetConnectionException {
        Ensure.requireNonNull(config, "config must be non-null");
        this.config = config;
        init();
    }


    @Override
    public Response execute(Endpoint source, Request request) {
        try {
            return requestHandler.execute(request, requestExecutionContext.withEndpoint(source));
        }
        catch (Exception e) {
            LOGGER.trace("Error executing request", e);
            return new InternalErrorResponse(e.getMessage());
        }
    }


    @Override
    public OperationVariable[] getOperationOutputVariables(Reference reference) throws ResourceNotFoundException, PersistenceException {
        if (reference == null) {
            throw new IllegalArgumentException("reference must be non-null");
        }
        SubmodelElement element = persistence.getSubmodelElement(reference, QueryModifier.DEFAULT);
        if (element == null) {
            throw new ResourceNotFoundException(String.format("reference could not be resolved (reference: %s)", ReferenceHelper.toString(reference)));
        }
        if (!Operation.class.isAssignableFrom(element.getClass())) {
            throw new IllegalArgumentException(String.format("reference points to invalid type (reference: %s, expected type: Operation, actual type: %s)",
                    ReferenceHelper.toString(reference),
                    element.getClass()));
        }
        return ((Operation) element).getOutputVariables().toArray(new OperationVariable[0]);
    }


    @Override
    public TypeInfo getTypeInfo(Reference reference) throws ResourceNotFoundException, PersistenceException {
        return TypeExtractor.extractTypeInfo(persistence.getSubmodelElement(reference, QueryModifier.DEFAULT));
    }


    @Override
    public boolean hasValueProvider(Reference reference) {
        return Objects.nonNull(assetConnectionManager.getValueProvider(reference));
    }


    @Override
    public Environment getAASEnvironment() throws PersistenceException {
        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(
                        persistence.findAssetAdministrationShells(
                                AssetAdministrationShellSearchCriteria.NONE,
                                QueryModifier.DEFAULT,
                                PagingInfo.ALL)
                                .getContent())
                .submodels(
                        persistence.findSubmodels(
                                SubmodelSearchCriteria.NONE,
                                QueryModifier.DEFAULT,
                                PagingInfo.ALL)
                                .getContent())
                .conceptDescriptions(
                        persistence.findConceptDescriptions(
                                ConceptDescriptionSearchCriteria.NONE,
                                QueryModifier.DEFAULT,
                                PagingInfo.ALL)
                                .getContent())
                .build();
    }


    /**
     * Executes a request asynchroniously.
     *
     * @param request request to execute
     * @param callback callback handler that is called when execution if
     *            finished
     * @throws IllegalArgumentException if request is null
     * @throws IllegalArgumentException if callback is null
     */
    public void executeAsync(Request request, Consumer<Response> callback) {
        Ensure.requireNonNull(request, "request must be non-null");
        Ensure.requireNonNull(callback, "callback must be non-null");
        this.requestHandler.executeAsync(request, callback, requestExecutionContext);
    }


    @Override
    public MessageBus getMessageBus() {
        return messageBus;
    }


    public AssetConnectionManager getAssetConnectionManager() {
        return assetConnectionManager;
    }


    public FileStorage getFileStorage() {
        return fileStorage;
    }


    public ServiceConfig getConfig() {
        return config;
    }


    public Persistence getPersistence() {
        return persistence;
    }


    /**
     * Starts the service.This includes starting the message bus and endpoints.
     *
     * @throws org.eclipse.digitaltwin.fa3st.common.exception.MessageBusException if starting message bus fails
     * @throws org.eclipse.digitaltwin.fa3st.service.exception.EndpointException if starting endpoints fails
     * @throws IllegalArgumentException if AAS environment is null/has not been properly initialized
     */
    public void start() throws MessageBusException, EndpointException, PersistenceException {
        LOGGER.debug("Get command for starting FA³ST Service");
        persistence.start();
        messageBus.start();
        if (!endpoints.isEmpty()) {
            LOGGER.info("Starting endpoints...");
        }
        for (Endpoint endpoint: endpoints) {
            LOGGER.debug("Starting endpoint {}", endpoint.getClass().getSimpleName());
            endpoint.start();
        }
        registrySynchronization.start();
        assetConnectionManager.start();
        LOGGER.debug("FA³ST Service is running!");
    }


    /**
     * Stop the service. This includes stopping the message bus and all
     * endpoints.
     */
    public void stop() {
        LOGGER.debug("Get command for stopping FA³ST Service");
        messageBus.stop();
        assetConnectionManager.stop();
        registrySynchronization.stop();
        persistence.stop();
        endpoints.forEach(Endpoint::stop);
    }


    private void init() throws ConfigurationException {
        Ensure.requireNonNull(config.getPersistence(), new InvalidConfigurationException("config.persistence must be non-null"));
        Ensure.requireNonNull(config.getFileStorage(), new InvalidConfigurationException("config.filestorage must be non-null"));
        Ensure.requireNonNull(config.getMessageBus(), new InvalidConfigurationException("config.messagebus must be non-null"));
        ensureInitialModelFilesAreLoaded();
        persistence = (Persistence) config.getPersistence().newInstance(config.getCore(), this);
        fileStorage = (FileStorage) config.getFileStorage().newInstance(config.getCore(), this);
        messageBus = (MessageBus) config.getMessageBus().newInstance(config.getCore(), this);
        if (config.getAssetConnections() != null) {
            List<AssetConnection> assetConnections = new ArrayList<>();
            for (AssetConnectionConfig assetConnectionConfig: config.getAssetConnections()) {
                assetConnections.add((AssetConnection) assetConnectionConfig.newInstance(config.getCore(), this));
            }
            assetConnectionManager = new AssetConnectionManager(config.getCore(), assetConnections, this);
        }
        endpoints = new ArrayList<>();
        if (config.getEndpoints() == null || config.getEndpoints().isEmpty()) {
            LOGGER.warn("no endpoint configuration found, starting service without endpoint which means the service will not be accessible via any kind of API");
        }
        else {
            for (EndpointConfig endpointConfig: config.getEndpoints()) {
                Endpoint endpoint = (Endpoint) endpointConfig.newInstance(config.getCore(), this);
                endpoints.add(endpoint);
            }
        }
        this.requestHandler = new RequestHandlerManager(config.getCore());
        this.requestExecutionContext = new DynamicRequestExecutionContext(this);
        this.registrySynchronization = new RegistrySynchronization(config.getCore(), persistence, messageBus, endpoints);
    }


    private void ensureInitialModelFilesAreLoaded() {
        if (Objects.nonNull(config.getPersistence().getInitialModelFile())
                && DataFormat.forFileExtension(FileHelper.getFileExtensionWithoutSeparator(config.getPersistence().getInitialModelFile())).stream()
                        .anyMatch(DataFormat::getCanStoreFiles)) {
            config.getFileStorage().setInitialModelFile(config.getPersistence().getInitialModelFile());
        }
    }
}
