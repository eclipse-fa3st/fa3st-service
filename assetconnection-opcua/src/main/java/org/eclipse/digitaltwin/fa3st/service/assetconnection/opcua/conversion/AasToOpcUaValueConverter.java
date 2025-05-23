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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.opcua.conversion;

import org.eclipse.digitaltwin.fa3st.common.model.value.TypedValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;


/**
 * Converts values from AAS types to OPC UA types.
 */
public interface AasToOpcUaValueConverter {

    /**
     * Converts a given AAS-based value to an OPC UA-based value.
     *
     * @param value AAS-based input value
     * @param targetType OPC UA-based target type
     * @return OPC UA-compliant value
     * @throws ValueConversionException if conversion fails
     */
    public Variant convert(TypedValue<?> value, NodeId targetType) throws ValueConversionException;
}
