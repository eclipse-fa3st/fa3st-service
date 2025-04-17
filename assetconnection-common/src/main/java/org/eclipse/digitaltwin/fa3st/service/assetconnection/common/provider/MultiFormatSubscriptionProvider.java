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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.common.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.model.value.DataElementValue;
import org.eclipse.digitaltwin.fa3st.common.typing.TypeInfo;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetSubscriptionProvider;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.NewDataListener;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.provider.config.MultiFormatSubscriptionProviderConfig;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.util.MultiFormatReadWriteHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract base class for custom implementations of AssetSubscriptionProvider supporting multiple data formats.
 *
 * @param <T> concrete type of matching configuration
 */
public abstract class MultiFormatSubscriptionProvider<T extends MultiFormatSubscriptionProviderConfig> extends AbstractMultiFormatProvider<T> implements AssetSubscriptionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiFormatSubscriptionProvider.class);
    protected final List<NewDataListener> listeners;

    protected MultiFormatSubscriptionProvider(T config) {
        super(config);
        this.listeners = Collections.synchronizedList(new ArrayList<>());
    }


    @Override
    public void addNewDataListener(NewDataListener listener) throws AssetConnectionException {
        if (listeners.isEmpty()) {
            subscribe();
        }
        listeners.add(listener);
    }


    /**
     * Notifies all listeners about new event.
     *
     * @param value new data to notify about
     */
    protected void fireNewDataReceived(byte[] value) {
        try {
            DataElementValue newValue = MultiFormatReadWriteHelper.convertForRead(config, value, getTypeInfo());
            synchronized (listeners) {
                listeners.forEach(x -> {
                    try {
                        x.newDataReceived(newValue);
                    }
                    catch (Exception e) {
                        LOGGER.warn("error while calling newDataReceived handler", e);
                    }
                });
            }
        }
        catch (AssetConnectionException e) {
            LOGGER.error("error deserializing message (received message: {})",
                    new String(value),
                    e);
        }
    }


    @Override
    public void removeNewDataListener(NewDataListener listener) throws AssetConnectionException {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            unsubscribe();
        }
    }


    /**
     * Gets type information about the underlying element.
     *
     * @return the type info
     */
    protected abstract TypeInfo getTypeInfo();


    /**
     * Subscribe via underlying protocol.
     *
     * @throws AssetConnectionException if subscription fails
     */
    protected abstract void subscribe() throws AssetConnectionException;


    /**
     * Unsubscribe via underlying protocol.
     *
     * @throws AssetConnectionException if unsubscribe fails
     */
    protected abstract void unsubscribe() throws AssetConnectionException;


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), listeners);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MultiFormatSubscriptionProvider)) {
            return false;
        }
        final MultiFormatSubscriptionProvider<?> that = (MultiFormatSubscriptionProvider<?>) obj;
        return super.equals(that)
                && Objects.equals(listeners, that.listeners);
    }
}
