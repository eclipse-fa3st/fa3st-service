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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.http.provider.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.provider.config.AbstractMultiFormatOperationProviderConfig;


/**
 * * Config file for HTTP-based {@link org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetOperationProvider}.
 */
public class HttpOperationProviderConfig extends AbstractMultiFormatOperationProviderConfig {

    private String path;
    private String method;
    private Map<String, String> headers;

    public HttpOperationProviderConfig() {
        this.headers = new HashMap<>();
    }


    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }


    public String getMethod() {
        return method;
    }


    public void setMethod(String method) {
        this.method = method;
    }


    public Map<String, String> getHeaders() {
        return headers;
    }


    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpOperationProviderConfig that = (HttpOperationProviderConfig) o;
        return super.equals(that)
                && Objects.equals(path, that.path)
                && Objects.equals(method, that.method)
                && Objects.equals(headers, that.headers);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path, method, headers);
    }


    public static Builder builder() {
        return new Builder();
    }

    protected abstract static class AbstractBuilder<T extends HttpOperationProviderConfig, B extends AbstractBuilder<T, B>>
            extends AbstractMultiFormatOperationProviderConfig.AbstractBuilder<T, B> {

        public B path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }


        public B method(String value) {
            getBuildingInstance().setMethod(value);
            return getSelf();
        }


        public B headers(Map<String, String> value) {
            getBuildingInstance().setHeaders(value);
            return getSelf();
        }


        public B header(String name, String value) {
            getBuildingInstance().getHeaders().put(name, value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<HttpOperationProviderConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected HttpOperationProviderConfig newBuildingInstance() {
            return new HttpOperationProviderConfig();
        }
    }

}
