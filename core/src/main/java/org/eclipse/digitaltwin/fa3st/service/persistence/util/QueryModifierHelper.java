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
package org.eclipse.digitaltwin.fa3st.service.persistence.util;

import java.util.Collection;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Extent;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Level;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.QueryModifier;
import org.eclipse.digitaltwin.fa3st.common.model.visitor.AssetAdministrationShellElementVisitor;
import org.eclipse.digitaltwin.fa3st.common.model.visitor.AssetAdministrationShellElementWalker;
import org.eclipse.digitaltwin.fa3st.common.model.visitor.DefaultAssetAdministrationShellElementSubtypeResolvingVisitor;
import org.eclipse.digitaltwin.fa3st.common.model.visitor.DefaultAssetAdministrationShellElementVisitor;
import org.eclipse.digitaltwin.fa3st.common.util.Ensure;


/**
 * Helper class to apply query modifier.
 */
public class QueryModifierHelper {

    private QueryModifierHelper() {}


    /**
     * Apply the {@link QueryModifier} to a list of referables Consider the
     * {@link org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Extent} and {@link Level} of a query modifier If
     * the extend of the query modifier is "WithoutBlobValue" all submodel elements of type {@link Blob} are removed.If
     * the level of the query modifier is "Core" all underlying submodel element collection values are removed.
     *
     * @param list which should be adapted by the query modifier
     * @param modifier which should be applied
     * @param <T> type of referable
     * @return the modified list
     */
    public static <T extends Referable> List<T> applyQueryModifier(List<T> list, QueryModifier modifier) {
        if (list != null) {
            list.forEach(x -> applyQueryModifier(x, modifier));
        }
        return list;
    }


    /**
     * Apply the {@link QueryModifier} to a referable Consider the
     * {@link org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Extent} and {@link Level} of a query modifier If
     * the extend of the query modifier is "WithoutBlobValue" all submodel elements of type {@link Blob} are removed.If
     * the level of the query modifier is "Core" all underlying submodel element collection values are removed.
     *
     * @param <T> type of the referable
     * @param referable which should be adapted by the query modifier
     * @param modifier which should be applied
     * @return the modified referable
     */
    public static <T extends Referable> T applyQueryModifier(T referable, QueryModifier modifier) {
        Ensure.requireNonNull(referable, "referable must be non-null");
        Ensure.requireNonNull(modifier, "modifier must be non-null");
        applyQueryModifierExtend(referable, modifier);
        applyQueryModifierLevel(referable, modifier);
        return referable;
    }


    private static void applyQueryModifierExtend(Referable referable, QueryModifier modifier) {
        if (modifier.getExtent() == Extent.WITHOUT_BLOB_VALUE) {
            AssetAdministrationShellElementWalker.builder()
                    .visitor(new DefaultAssetAdministrationShellElementVisitor() {
                        @Override
                        public void visit(Blob blob) {
                            blob.setValue(null);
                        }
                    })
                    .build()
                    .walk(referable);
        }
    }


    private static void applyQueryModifierLevel(Referable referable, QueryModifier modifier) {
        if (modifier.getLevel() == Level.CORE) {
            new DefaultAssetAdministrationShellElementSubtypeResolvingVisitor() {
                @Override
                public void visit(Submodel submodel) {
                    clearSubcollections(submodel.getSubmodelElements());
                }


                private void clearSubcollections(Collection<SubmodelElement> list) {
                    AssetAdministrationShellElementVisitor visitor = new DefaultAssetAdministrationShellElementSubtypeResolvingVisitor() {
                        @Override
                        public void visit(SubmodelElementCollection submodelElementCollection) {
                            submodelElementCollection.getValue().clear();
                        }
                    };
                    list.forEach(visitor::visit);
                }


                @Override
                public void visit(SubmodelElementCollection submodelElementCollection) {
                    clearSubcollections(submodelElementCollection.getValue());
                }
            }.visit(referable);
        }
    }
}
