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

import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.model.value.DataElementValue;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.provider.config.MultiFormatWriteProviderConfig;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.util.MultiFormatReadWriteHelper;


/**
 * Abstract base class for providers that support writing values using multiple formats.
 *
 * @param <T> type of matching configuration
 */
public abstract class AbstractMultiFormatWriteProvider<T extends MultiFormatWriteProviderConfig> extends AbstractMultiFormatProvider<T> implements MultiFormatWriteProvider {

    protected AbstractMultiFormatWriteProvider(T config) {
        super(config);
    }


    @Override
    public void setValue(DataElementValue value) throws AssetConnectionException {
        setRawValue(MultiFormatReadWriteHelper.convertForWrite(config, value));
    }
}
