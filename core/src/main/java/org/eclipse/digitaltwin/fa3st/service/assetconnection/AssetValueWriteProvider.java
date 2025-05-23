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
 * An AssetValueWriteProvider provides methods to write data values to an asset.
 */
public interface AssetValueWriteProvider extends AssetProvider {

    /**
     * Sets the data value on an asset.
     *
     * @param value the value to set
     * @throws org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException when writing the value to the
     *             asset connection fails
     */
    public void setValue(DataElementValue value) throws AssetConnectionException;
}
