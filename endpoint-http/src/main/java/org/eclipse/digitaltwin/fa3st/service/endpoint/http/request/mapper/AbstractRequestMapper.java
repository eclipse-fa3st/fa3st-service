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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.google.common.net.MediaType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.fileupload.MultipartStream;
import org.eclipse.digitaltwin.fa3st.common.dataformat.DeserializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.model.TypedInMemoryFile;
import org.eclipse.digitaltwin.fa3st.common.model.api.Request;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.common.util.EncodingHelper;
import org.eclipse.digitaltwin.fa3st.common.util.Ensure;
import org.eclipse.digitaltwin.fa3st.common.util.RegExHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.model.HttpRequest;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.serialization.HttpJsonApiDeserializer;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.util.HttpConstants;


/**
 * Base class for mapping HTTP requests to protocol-agnostic requests.
 */
public abstract class AbstractRequestMapper {

    private static final String MSG_ERROR_PARSING_BODY = "error parsing body";
    protected static final String BOUNDARY = "boundary";
    protected static final Pattern PATTERN_NAME = Pattern.compile("name=\"([^\"]+)\"");
    protected static final Pattern PATTERN_CONTENT_TYPE = Pattern.compile(HttpConstants.HEADER_CONTENT_TYPE + ": ([^\n^\r]+)");

    protected final ServiceContext serviceContext;
    protected final HttpJsonApiDeserializer deserializer;
    protected final HttpMethod method;
    protected String urlPattern;

    protected AbstractRequestMapper(ServiceContext serviceContext, HttpMethod method, String urlPattern) {
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(method, "method must be non-null");
        Ensure.requireNonNull(urlPattern, "urlPattern must be non-null");
        this.serviceContext = serviceContext;
        this.method = method;
        this.urlPattern = urlPattern;
        this.deserializer = new HttpJsonApiDeserializer();
        init();
    }


    /**
     * Utility method to create named regex groups used to represent URL path elements that are variable, e.g.
     * <i>/shells/[id]/</i>.
     *
     * @param name the name of the regex group
     * @return a string representation of a named regex group with given {@code name}
     */
    protected static final String pathElement(String name) {
        return String.format("(?<%s>[^/$]*)", name);
    }


    private void init() {
        urlPattern = RegExHelper.ensureLineMatch(urlPattern);
    }


    public HttpMethod getMethod() {
        return method;
    }


    /**
     * Decides if a given HTTP request matches this concrete protocol-agnostic request.
     *
     * @param httpRequest the HTTP request to check
     * @return true if matches, otherwise false
     * @throws IllegalArgumentException if httpRequest is null
     */
    public boolean matchesUrl(HttpRequest httpRequest) {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        return httpRequest.getPath().matches(urlPattern);
    }


    /**
     * Decides if a given URL matches this concrete protocol-agnostic request.
     *
     * @param url the URL to check
     * @return true if matches, otherwise false
     * @throws IllegalArgumentException if url is null
     */
    public boolean matchesUrl(String url) {
        Ensure.requireNonNull(url, "url must be non-null");
        return matchesUrl(HttpRequest.builder()
                .path(url)
                .build());
    }


    /**
     * Converts the HTTP request to protocol-agnostic request.
     *
     * @param httpRequest the HTTP request to convert
     * @return the protocol-agnostic request
     * @throws InvalidRequestException if conversion fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    public Request parse(HttpRequest httpRequest) throws InvalidRequestException {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        Matcher matcher = Pattern.compile(urlPattern).matcher(httpRequest.getPath());
        if (matcher.matches()) {
            return doParse(httpRequest, RegExHelper.getGroupValues(urlPattern, httpRequest.getPath()));
        }
        throw new IllegalStateException(String.format("request was matched but no suitable parser found (HTTP method: %s, URL pattern: %s", method, urlPattern));
    }


    /**
     * Converts the HTTP request to protocol-agnostic request.
     *
     * @param httpRequest the HTTP request to convert
     * @param urlParameters map of named regex groups and their values
     * @return parsed request
     * @throws InvalidRequestException if conversion fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    public abstract Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException;


    /**
     * Deserializes HTTP body to given type.
     *
     * @param <T> expected type
     * @param httpRequest HTTP request
     * @param type expected type
     * @return deserialized payload
     * @throws InvalidRequestException if deserialization fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    protected <T> T parseBody(HttpRequest httpRequest, Class<T> type) throws InvalidRequestException {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        try {
            return deserializer.read(httpRequest.getBodyAsString(), type);
        }
        catch (DeserializationException e) {
            throw new InvalidRequestException(MSG_ERROR_PARSING_BODY, e);
        }
    }


    /**
     * Deserializes HTTP body multipart form data.
     *
     * @param httpRequest HTTP request
     * @param contentType the multipart contentType containing the boundary
     * @return deserialized payload
     * @throws InvalidRequestException if deserialization fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    protected Map<String, TypedInMemoryFile> parseMultiPartBody(HttpRequest httpRequest, MediaType contentType) throws InvalidRequestException {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        Map<String, TypedInMemoryFile> map = new HashMap<>();
        try {
            MultipartStream multipartStream = new MultipartStream(
                    new ByteArrayInputStream(httpRequest.getBody()),
                    contentType.parameters().get(BOUNDARY).get(0).getBytes(), 4096, null);
            boolean nextPart = multipartStream.skipPreamble();
            while (nextPart) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                String multipartHeaders = multipartStream.readHeaders();
                multipartStream.readBodyData(output);
                if (Objects.equals(headerMatcher(PATTERN_NAME, multipartHeaders), "fileName")) {
                    map.put("fileName", new TypedInMemoryFile.Builder()
                            .content(output.toByteArray())
                            .contentType(MediaType.PLAIN_TEXT_UTF_8.toString())
                            .build());
                }
                else {
                    map.put("file", new TypedInMemoryFile.Builder()
                            .content(output.toByteArray())
                            .contentType(headerMatcher(PATTERN_CONTENT_TYPE, multipartHeaders))
                            .build());
                }
                nextPart = multipartStream.readBoundary();
            }
        }
        catch (IOException e) {
            throw new InvalidRequestException(MSG_ERROR_PARSING_BODY, e);
        }
        return map;
    }


    /**
     * Reads and decodes a base64Url-encoded query parameter.
     *
     * @param urlParameters the map of known query parameters and their original values
     * @param parameterName the name of the parameter to read
     * @return the decoded value of the query parameter.
     * @throws InvalidRequestException if the query parameter is unkown or is not valid base64
     */
    protected String getParameterBase64UrlEncoded(Map<String, String> urlParameters, String parameterName) throws InvalidRequestException {
        Ensure.requireNonNull(urlParameters, "urlParameter must be non-null");
        Ensure.requireNonNull(parameterName, "parameterName must be non-null");
        String value = urlParameters.get(parameterName);
        if (Objects.isNull(value)) {
            return null;
        }
        try {
            return EncodingHelper.base64UrlDecode(value);
        }
        catch (IllegalArgumentException e) {
            if (RegExHelper.isGroupName(parameterName)) {
                throw new InvalidRequestException(String.format(
                        "invalid URL path segment, must be base64Url-encoded (value: %s)",
                        urlParameters.get(parameterName)));
            }
            throw new InvalidRequestException(String.format(
                    "invalid query parameter, must be base64Url-encoded (name: %s, value: %s)",
                    parameterName,
                    urlParameters.get(parameterName)));
        }
    }


    /**
     * Deserializes HTTP body to a list of given type.
     *
     * @param <T> expected type
     * @param httpRequest HTTP request
     * @param type expected type
     * @return deserialized payload as list of given type
     * @throws InvalidRequestException if deserialization fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    protected <T> List<T> parseBodyAsList(HttpRequest httpRequest, Class<T> type) throws InvalidRequestException {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        try {
            return deserializer.readList(httpRequest.getBodyAsString(), type);
        }
        catch (DeserializationException e) {
            throw new InvalidRequestException(MSG_ERROR_PARSING_BODY, e);
        }
    }


    /**
     * Parses a string to a JSON merge patch.
     *
     * @param json the JSON to parse
     * @return the parsed merge patch
     * @throws org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException if json cannot be parsed as
     *             JSON Merge Patch
     */
    protected JsonMergePatch parseMergePatch(String json) throws InvalidRequestException {

        try {
            return new ObjectMapper().readValue(json, JsonMergePatch.class);
        }
        catch (JsonProcessingException e) {
            throw new InvalidRequestException(String.format("unable to create JSON merge patch (reason: %s, JSON payload: %s)",
                    e.getMessage(),
                    json),
                    e);
        }

    }


    @Override
    public int hashCode() {
        return Objects.hash(serviceContext, deserializer, method, urlPattern);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractRequestMapper other = (AbstractRequestMapper) obj;
        return Objects.equals(this.urlPattern, other.urlPattern)
                && Objects.equals(this.serviceContext, other.serviceContext)
                && Objects.equals(this.deserializer, other.deserializer)
                && Objects.equals(this.method, other.method);
    }


    private String headerMatcher(Pattern pattern, String header) {
        Matcher matcher = pattern.matcher(header);
        String result = matcher.find() ? matcher.group(1) : null;
        if (!Objects.isNull(result) && result.contains("charset")) {
            result = result.split(";")[0];
        }
        return result;
    }
}
