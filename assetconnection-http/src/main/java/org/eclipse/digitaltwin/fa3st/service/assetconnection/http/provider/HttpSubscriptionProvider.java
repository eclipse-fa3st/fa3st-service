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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.http.provider;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.model.value.DataElementValue;
import org.eclipse.digitaltwin.fa3st.common.typing.TypeInfo;
import org.eclipse.digitaltwin.fa3st.common.util.Ensure;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.provider.MultiFormatSubscriptionProvider;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.util.MultiFormatReadWriteHelper;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.http.HttpAssetConnectionConfig;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.http.util.HttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides the capability to "subscribe" to en element via HTTP. This is done via periodic polling.
 */
public class HttpSubscriptionProvider extends MultiFormatSubscriptionProvider<HttpSubscriptionProviderConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpSubscriptionProvider.class);
    public static final String DEFAULT_METHOD = "GET";
    public static final long MINIMUM_INTERVAL = 100;
    private final ServiceContext serviceContext;
    private final Reference reference;
    private final HttpClient client;
    private final HttpAssetConnectionConfig connectionConfig;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> executorHandler;
    private Optional<DataElementValue> lastValue;

    public HttpSubscriptionProvider(
            ServiceContext serviceContext,
            Reference reference,
            HttpClient client,
            HttpAssetConnectionConfig connectionConfig,
            HttpSubscriptionProviderConfig config) {
        super(config);
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(client, "client must be non-null");
        Ensure.requireNonNull(connectionConfig, "connectionConfig must be non-null");
        this.serviceContext = serviceContext;
        this.reference = reference;
        this.client = client;
        this.connectionConfig = connectionConfig;
        this.lastValue = Optional.empty();
    }


    private byte[] readRawValue() throws AssetConnectionException {
        try {
            HttpResponse<byte[]> response = HttpHelper.execute(
                    client,
                    connectionConfig.getBaseUrl(),
                    config.getPath(),
                    config.getFormat(),
                    DEFAULT_METHOD,

                    BodyPublishers.noBody(),
                    BodyHandlers.ofByteArray(),
                    HttpHelper.mergeHeaders(connectionConfig.getHeaders(), config.getHeaders()));
            if (!HttpHelper.is2xxSuccessful(response)) {
                throw new AssetConnectionException(String.format("error reading value from asset conenction (reference: %s)", ReferenceHelper.toString(reference)));
            }
            return response.body();
        }
        catch (IOException | InterruptedException | URISyntaxException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("error reading value from asset conenction (reference: %s)", ReferenceHelper.toString(reference)), e);
        }
    }


    @Override
    protected void subscribe() throws AssetConnectionException {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newScheduledThreadPool(0);
            executorHandler = executor.scheduleAtFixedRate(() -> {
                try {
                    fireNewDataReceived(readRawValue());
                }
                catch (AssetConnectionException e) {
                    LOGGER.debug("error subscribing to asset connection (reference: {})", ReferenceHelper.toString(reference), e);
                }
            }, 0, Math.max(MINIMUM_INTERVAL, config.getInterval()), TimeUnit.MILLISECONDS);
        }
    }


    @Override
    protected void fireNewDataReceived(byte[] value) {
        try {
            DataElementValue newValue = MultiFormatReadWriteHelper.convertForRead(config, value, getTypeInfo());
            if (lastValue.isEmpty() || !Objects.equals(lastValue.get(), newValue)) {
                lastValue = Optional.ofNullable(newValue);
                super.fireNewDataReceived(value);
            }
        }
        catch (AssetConnectionException e) {
            LOGGER.error("error deserializing message (received message: {})",
                    new String(value),
                    e);
        }
    }


    @Override
    protected void unsubscribe() throws AssetConnectionException {
        if (executorHandler != null) {
            executorHandler.cancel(true);
        }
        if (executor != null) {
            executor.shutdown();
        }
    }


    @Override
    protected TypeInfo getTypeInfo() {
        try {
            return serviceContext.getTypeInfo(reference);
        }
        catch (ResourceNotFoundException | PersistenceException e) {
            throw new IllegalStateException(String.format(
                    "HTTP subscription provider could not get type info as resource does not exist or storage failed - this should not be able to occur (reference: %s)",
                    ReferenceHelper.toString(reference)),
                    e);
        }
    }

}
