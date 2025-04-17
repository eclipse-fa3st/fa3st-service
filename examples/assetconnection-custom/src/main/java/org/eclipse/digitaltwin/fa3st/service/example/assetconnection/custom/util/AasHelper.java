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
package org.eclipse.digitaltwin.fa3st.service.example.assetconnection.custom.util;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.util.ReflectionHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.value.Datatype;
import org.eclipse.digitaltwin.fa3st.common.typing.ElementValueTypeInfo;
import org.eclipse.digitaltwin.fa3st.common.typing.TypeInfo;
import org.eclipse.digitaltwin.fa3st.common.util.EnvironmentHelper;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;


public class AasHelper {

    private AasHelper() {}


    public static Datatype getDatatype(Reference reference, ServiceContext serviceContext) throws ValueMappingException, ResourceNotFoundException, PersistenceException {
        TypeInfo typeInfo = serviceContext.getTypeInfo(reference);
        if (!ElementValueTypeInfo.class.isAssignableFrom(typeInfo.getClass())) {
            throw new IllegalArgumentException(String.format("type info does not provide datatype (type info: %s)", typeInfo.getClass()));
        }
        return ((ElementValueTypeInfo) typeInfo).getDatatype();
    }


    public static void ensureType(Reference reference, Class<?> type, ServiceContext serviceContext) throws ResourceNotFoundException, PersistenceException {
        Referable element = EnvironmentHelper.resolve(reference, serviceContext.getAASEnvironment());
        if (element == null) {
            throw new IllegalArgumentException(String.format("element could not be resolved (reference: %s)", ReferenceHelper.toString(reference)));
        }
        if (!type.isAssignableFrom(element.getClass())) {
            throw new IllegalArgumentException(String.format("unsupported element type (expected: %s, found: %s)",
                    type.getName(),
                    ReflectionHelper.getAasInterface(element.getClass()).getName()));
        }
    }
}
