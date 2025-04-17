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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.submodel;

import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.fa3st.common.dataformat.DeserializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.exception.PersistenceException;
import org.eclipse.digitaltwin.fa3st.common.exception.ResourceNotFoundException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.OutputModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.PatchSubmodelElementValueByPathRequest;
import org.eclipse.digitaltwin.fa3st.common.model.api.response.PatchSubmodelElementValueByPathResponse;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.common.model.value.ElementValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.ElementValueParser;
import org.eclipse.digitaltwin.fa3st.common.model.value.mapper.ElementValueMapper;
import org.eclipse.digitaltwin.fa3st.common.util.EncodingHelper;
import org.eclipse.digitaltwin.fa3st.common.util.ReferenceBuilder;
import org.eclipse.digitaltwin.fa3st.common.util.RegExHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.model.HttpRequest;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.request.mapper.AbstractSubmodelInterfaceRequestMapper;


/**
 * class to map HTTP-PATCH-Request path: submodels/{submodelIdentifier}/submodel-elements/{idShortPath},
 * shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}.
 */
public class PatchSubmodelElementValueByPathRequestMapper
        extends AbstractSubmodelInterfaceRequestMapper<PatchSubmodelElementValueByPathRequest<?>, PatchSubmodelElementValueByPathResponse> {

    private static final String SUBMODEL_ELEMENT_PATH = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("submodel-elements/%s/\\$value", pathElement(SUBMODEL_ELEMENT_PATH));

    public PatchSubmodelElementValueByPathRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.PATCH, PATTERN);
    }


    @Override
    public PatchSubmodelElementValueByPathRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier)
            throws InvalidRequestException {
        final String path = EncodingHelper.urlDecode(urlParameters.get(SUBMODEL_ELEMENT_PATH));
        final String identifier = getParameterBase64UrlEncoded(urlParameters, SUBMODEL_ID);
        return PatchSubmodelElementValueByPathRequest.builder()
                .path(path)
                .value(httpRequest.getBodyAsString())
                .valueParser(new ElementValueParser<Object>() {
                    @Override
                    public <U extends ElementValue> U parse(Object raw, Class<U> type) throws DeserializationException {
                        String rawString;
                        if (raw.getClass().isAssignableFrom(byte[].class)) {
                            rawString = new String((byte[]) raw);
                        }
                        else {
                            rawString = raw.toString();
                        }
                        if (ElementValue.class.isAssignableFrom(type)) {
                            try {
                                return deserializer.readValue(
                                        rawString,
                                        serviceContext.getTypeInfo(
                                                new ReferenceBuilder()
                                                        .submodel(identifier)
                                                        .idShortPath(path)
                                                        .build()));
                            }
                            catch (ResourceNotFoundException | PersistenceException e) {
                                throw new DeserializationException("unable to obtain type information as resource does not exist or storage failed", e);
                            }
                        }
                        else if (SubmodelElement.class.isAssignableFrom(type)) {
                            SubmodelElement submodelElement = (SubmodelElement) deserializer.read(rawString, type);
                            try {
                                return ElementValueMapper.toValue(submodelElement, type);
                            }
                            catch (ValueMappingException e) {
                                throw new DeserializationException("error mapping submodel element to value object", e);
                            }
                        }
                        throw new DeserializationException(
                                String.format("error deserializing payload - invalid type '%s' (must be either instance of ElementValue or SubmodelElement",
                                        type.getSimpleName()));
                    }
                })
                .build();
    }

}
