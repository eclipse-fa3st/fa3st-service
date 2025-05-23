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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.common.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.fa3st.common.exception.AssetConnectionException;
import org.eclipse.digitaltwin.fa3st.common.exception.ValueMappingException;
import org.eclipse.digitaltwin.fa3st.common.model.value.DataElementValue;
import org.eclipse.digitaltwin.fa3st.common.model.value.mapper.ElementValueMapper;
import org.eclipse.digitaltwin.fa3st.common.typing.TypeExtractor;
import org.eclipse.digitaltwin.fa3st.common.util.DeepCopyHelper;
import org.eclipse.digitaltwin.fa3st.common.util.LambdaExceptionHelper;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.AssetOperationProvider;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.ElementInfo;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.format.Format;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.format.FormatFactory;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.provider.config.MultiFormatOperationProviderConfig;
import org.eclipse.digitaltwin.fa3st.service.assetconnection.common.util.TemplateHelper;


/**
 * Abstract base class for custom implementations of AssetOperationProvider supporting multiple data formats.
 *
 * @param <T> concrete type of matching configuration
 */
public abstract class MultiFormatOperationProvider<T extends MultiFormatOperationProviderConfig> extends AbstractMultiFormatProvider<T> implements AssetOperationProvider {

    protected MultiFormatOperationProvider(T config) {
        super(config);
    }


    @Override
    public T getConfig() {
        return config;
    }


    @Override
    public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
        final Map<String, DataElementValue> inputParameter = parseParameters(input);
        final Map<String, DataElementValue> inoutputParameter = parseParameters(inoutput);
        Set<String> duplicateInputParameters = inputParameter.keySet().stream()
                .filter(inoutputParameter::containsKey)
                .collect(Collectors.toSet());
        if (!duplicateInputParameters.isEmpty()) {
            throw new AssetConnectionException(String.format("duplicate input/inoutput parameter(s) found - must be either input or inoutput parameter but not both(%s)",
                    String.join(",", duplicateInputParameters)));
        }
        Format format = FormatFactory.create(config.getFormat());
        // handle inoutput
        Map<String, Object> variableReplacements = Stream.concat(inputParameter.entrySet().stream(), inoutputParameter.entrySet().stream())
                .collect(Collectors.toMap(
                        Entry::getKey,
                        LambdaExceptionHelper.rethrowFunction(x -> format.write(x.getValue()))));
        UnaryOperator<String> variableReplacer = x -> TemplateHelper.replace(x, variableReplacements);
        String request = variableReplacer.apply(config.getTemplate());
        String response = new String(invoke(request != null ? request.getBytes() : new byte[0], variableReplacer));
        Map<String, ElementInfo> mapping = Stream.concat(Stream.of(getOutputParameters()), Stream.of(inoutput))
                .collect(Collectors.toMap(
                        x -> x.getValue().getIdShort(),
                        x -> ElementInfo.of(
                                config.getQueries().get(x.getValue().getIdShort()),
                                TypeExtractor.extractTypeInfo(x.getValue()))));

        Map<String, DataElementValue> output = format.read(response, mapping);
        for (int i = 0; i < inoutput.length; i++) {
            if (output.containsKey(inoutput[i].getValue().getIdShort())) {
                try {
                    ElementValueMapper.setValue(inoutput[i].getValue(), output.get(inoutput[i].getValue().getIdShort()));
                }
                catch (ValueMappingException e) {
                    throw new AssetConnectionException("error reading inoutput parameters", e);
                }
            }
        }
        return Stream.of(getOutputParameters())
                .map(LambdaExceptionHelper.rethrowFunction(x -> {
                    SubmodelElement newValue = DeepCopyHelper.deepCopy(x.getValue(), SubmodelElement.class);
                    try {
                        ElementValueMapper.setValue(newValue, output.get(newValue.getIdShort()));
                    }
                    catch (ValueMappingException e) {
                        throw new AssetConnectionException("error reading output parameters", e);
                    }
                    return new DefaultOperationVariable.Builder()
                            .value(newValue)
                            .build();
                }))
                .toArray(OperationVariable[]::new);
    }


    /**
     * Parses OperationVariables to a easier to use format.
     *
     * @param parameters the parameters to parse
     * @return formatted parameters
     * @throws AssetConnectionException if mapping fails
     */
    protected Map<String, DataElementValue> parseParameters(OperationVariable[] parameters) throws AssetConnectionException {
        if (parameters == null) {
            return new HashMap<>();
        }
        try {
            return Stream.of(parameters).collect(Collectors.toMap(
                    x -> x.getValue().getIdShort(),
                    LambdaExceptionHelper.rethrowFunction(x -> ElementValueMapper.toValue(x.getValue(), DataElementValue.class))));
        }
        catch (ValueMappingException e) {
            throw new AssetConnectionException("Could not extract value of parameters", e);
        }
    }


    /**
     * Gets list of output parameters of underlying operation.
     *
     * @return list of output parameters
     */
    protected abstract OperationVariable[] getOutputParameters();


    /**
     * Invokes the underlying operation.
     *
     * @param input the raw input for the operation
     * @param variableReplacer functin to replace/subsctitute variables if needed, e.g. in URLs
     * @return result of executing the operation
     * @throws AssetConnectionException if operation fails
     */
    protected abstract byte[] invoke(byte[] input, UnaryOperator<String> variableReplacer) throws AssetConnectionException;
}
