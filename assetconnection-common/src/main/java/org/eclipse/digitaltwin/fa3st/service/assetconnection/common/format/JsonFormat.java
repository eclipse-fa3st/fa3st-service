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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.common.format;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.digitaltwin.fa3st.common.dataformat.DeserializationException;
import org.eclipse.digitaltwin.fa3st.common.dataformat.SerializationException;
import org.eclipse.digitaltwin.fa3st.common.dataformat.json.JsonApiDeserializer;
import org.eclipse.digitaltwin.fa3st.common.dataformat.json.JsonApiSerializer;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.UnsupportedModifierException;
import org.eclipse.digitaltwin.fa3st.common.model.value.DataElementValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.Datatype;
import org.eclipse.digitaltwin.fa3st.common.typing.ElementValueTypeInfo;
import org.eclipse.digitaltwin.fa3st.common.typing.TypeInfo;
import org.eclipse.digitaltwin.fa3st.common.util.LambdaExceptionHelper;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.ElementInfo;


/**
 * Implementation of JSON format for asset connection.
 */
@Dataformat(key = JsonFormat.KEY)
public class JsonFormat implements Format {

    public static final String KEY = "JSON";
    private static final String MIME_TYPE = "application/json";
    private final JsonApiSerializer serializer;
    private final JsonApiDeserializer deserializer;

    public JsonFormat() {
        this.serializer = new JsonApiSerializer();
        this.deserializer = new JsonApiDeserializer();
    }


    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }


    @Override
    public Map<String, DataElementValue> read(String value, Map<String, ElementInfo> elements) throws AssetConnectionException {
        if (elements == null) {
            return Map.of();
        }
        if (value == null) {
            return elements.keySet().stream().collect(Collectors.toMap(x -> x, x -> null));
        }
        return elements.entrySet().stream().collect(Collectors.toMap(
                Entry::getKey,
                LambdaExceptionHelper.rethrowFunction(x -> {
                    String query = x.getValue().getQuery();
                    String actualValue = value;
                    if (!StringUtils.isBlank(query)) {
                        try {
                            List<Object> jsonPathResult = JsonPath
                                    .using(Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST))
                                    .parse(value)
                                    .read(query);
                            if (jsonPathResult.isEmpty()) {
                                throw new AssetConnectionException(String.format("JSONPath expression did not return any value (JSON path: %s, JSON: %s)", query, value));
                            }
                            if (jsonPathResult.size() > 1) {
                                throw new AssetConnectionException(String.format("JSONPath expression returned more than one value (JSON path: %s, JSON: %s)", query, value));
                            }
                            actualValue = jsonPathResult.get(0).toString();
                        }
                        catch (PathNotFoundException e) {
                            throw new AssetConnectionException(String.format("value addressed by JSONPath not found (JSON path: %s, JSON: %s)", query, value), e);
                        }
                        catch (InvalidPathException e) {
                            throw new AssetConnectionException(String.format("invalid JSONPath (JSON path: %s)", query), e);
                        }
                        catch (JsonPathException e) {
                            throw new AssetConnectionException(String.format("error resolving JSONPath (JSON path: %s, JSON: %s)", query, value), e);
                        }
                    }
                    try {
                        TypeInfo<?> typeInfo = x.getValue().getTypeInfo();
                        // if datatype is string, we need to escape and wrap it with additional quotes
                        if (typeInfo != null
                                && ElementValueTypeInfo.class.isAssignableFrom(typeInfo.getClass())
                                && ((ElementValueTypeInfo) typeInfo).getDatatype() == Datatype.STRING
                                && !actualValue.startsWith("\"")
                                && !actualValue.endsWith("\"")) {
                            actualValue = String.format("\"%s\"", escapeJson(actualValue));
                        }
                        return deserializer.readValue(actualValue, x.getValue().getTypeInfo());
                    }
                    catch (DeserializationException e) {
                        throw new AssetConnectionException(String.format("JSON deserialization failed (json: %S)", actualValue), e);
                    }
                })));
    }


    @Override
    public String write(DataElementValue value) throws AssetConnectionException {
        try {
            return serializer.write(value);
        }
        catch (SerializationException | UnsupportedModifierException e) {
            throw new AssetConnectionException("serializing value to JSON failed", e);
        }
    }


    private static String escapeJson(String json) {
        return new String(JsonStringEncoder.getInstance().quoteAsString(json));
    }
}
