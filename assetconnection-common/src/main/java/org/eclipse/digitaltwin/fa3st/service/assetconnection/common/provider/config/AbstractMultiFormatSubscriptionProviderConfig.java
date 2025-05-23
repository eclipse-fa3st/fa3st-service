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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.common.provider.config;

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Base class for AssetSubscriptionProviderConfig supporting multiple data formats.
 */
public abstract class AbstractMultiFormatSubscriptionProviderConfig implements MultiFormatSubscriptionProviderConfig {

    protected String format;
    protected String query;

    @Override
    public String getFormat() {
        return format;
    }


    @Override
    public void setFormat(String format) {
        this.format = format;
    }


    @Override
    public String getQuery() {
        return query;
    }


    @Override
    public void setQuery(String query) {
        this.query = query;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractMultiFormatSubscriptionProviderConfig that = (AbstractMultiFormatSubscriptionProviderConfig) o;
        return Objects.equals(format, that.format)
                && Objects.equals(query, that.query);
    }


    @Override
    public int hashCode() {
        return Objects.hash(format, query);
    }

    protected abstract static class AbstractBuilder<T extends AbstractMultiFormatSubscriptionProviderConfig, B extends AbstractBuilder<T, B>>
            extends ExtendableBuilder<T, B> {

        public B query(String value) {
            getBuildingInstance().setQuery(value);
            return getSelf();
        }


        public B format(String value) {
            getBuildingInstance().setFormat(value);
            return getSelf();
        }
    }
}
