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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.common;

import org.eclipse.digitaltwin.fa3st.common.typing.TypeInfo;


/**
 * Utility class encapsulating information for mapping operation parameters.
 */
public class ElementInfo {

    private final String query;
    private final TypeInfo<?> typeInfo;

    public ElementInfo(String query, TypeInfo<?> typeInfo) {
        this.query = query;
        this.typeInfo = typeInfo;
    }


    public String getQuery() {
        return query;
    }


    public TypeInfo getTypeInfo() {
        return typeInfo;
    }


    /**
     * Creates a new instance with given {@code query} and {@code typeInfo}.
     *
     * @param query the query
     * @param typeInfo the typeInfo
     * @return new {@link ElementInfo}
     */
    public static ElementInfo of(String query, TypeInfo<?> typeInfo) {
        return new ElementInfo(query, typeInfo);
    }
}
