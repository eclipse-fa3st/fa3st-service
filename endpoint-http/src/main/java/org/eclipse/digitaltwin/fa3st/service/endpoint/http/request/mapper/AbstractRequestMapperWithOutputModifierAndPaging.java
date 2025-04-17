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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper;

import java.util.Map;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Content;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.OutputModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.paging.PagingInfo;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.AbstractRequestWithModifierAndPaging;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.AbstractPagedResponse;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.model.HttpRequest;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.util.PagingHelper;


/**
 * Base class for mapping HTTP requests including output modifier and paging information.
 *
 * @param <T> type of request
 * @param <R> type of response to the request
 */
public abstract class AbstractRequestMapperWithOutputModifierAndPaging<T extends AbstractRequestWithModifierAndPaging<R>, R extends AbstractPagedResponse>
        extends AbstractRequestMapperWithOutputModifier<T, R> {

    protected AbstractRequestMapperWithOutputModifierAndPaging(ServiceContext serviceContext, HttpMethod method, String urlPattern, Content... excludedContentModifiers) {
        super(serviceContext, method, urlPattern, excludedContentModifiers);
    }


    /**
     * Converts the HTTP request to protocol-agnostic request including output modifier information.
     *
     * @param httpRequest the HTTP request to convert
     * @param urlParameters map of named regex groups and their values
     * @param outputModifier output modifier for this request
     * @param pagingInfo the paging information
     * @return the protocol-agnostic request
     * @throws InvalidRequestException if conversion fails
     */
    public abstract T doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier, PagingInfo pagingInfo) throws InvalidRequestException;


    @Override
    public T doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException {
        PagingInfo pagingInfo = PagingHelper.parsePagingInfo(httpRequest.getQueryParameters());
        T result = doParse(httpRequest, urlParameters, outputModifier, pagingInfo);
        result.setPagingInfo(pagingInfo);
        return result;
    }

}
