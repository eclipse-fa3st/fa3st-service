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
package org.eclipse.digitaltwin.fa3st.service.assetconnection;

import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.model.value.DataElementValue;


/**
 * An AssetValueReadProvider provides methods to reade data values from an asset.
 */
public interface AssetValueReadProvider extends AssetProvider {

    /**
     * Read a data value from the asset.
     *
     * @return the data value
     * @throws org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException when fetching the value
     *             from the asset connection fails
     */
    public DataElementValue getValue() throws AssetConnectionException;
}
