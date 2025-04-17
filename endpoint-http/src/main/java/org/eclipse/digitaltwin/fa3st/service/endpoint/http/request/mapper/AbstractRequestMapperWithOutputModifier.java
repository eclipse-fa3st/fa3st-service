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

import com.github.curiousoddman.rgxgen.RgxGen;
import com.google.common.reflect.TypeToken;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.eclipse.digitaltwin.fa3st.common.exception.InvalidRequestException;
import org.eclipse.digitaltwin.fa3st.common.exception.UnsupportedContentModifierException;
import org.eclipse.digitaltwin.fa3st.common.model.api.Response;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Content;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Extent;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.Level;
import org.eclipse.digitaltwin.fa3st.common.model.api.modifier.OutputModifier;
import org.eclipse.digitaltwin.fa3st.common.model.api.request.AbstractRequestWithModifier;
import org.eclipse.digitaltwin.fa3st.common.model.http.HttpMethod;
import org.eclipse.digitaltwin.fa3st.common.util.RegExHelper;
import org.eclipse.digitaltwin.fa3st.common.util.StringHelper;
import org.eclipse.digitaltwin.fa3st.service.ServiceContext;
import org.eclipse.digitaltwin.fa3st.service.endpoint.http.model.HttpRequest;


/**
 * Base class for mapping HTTP requests including output modifier information.
 *
 * @param <T> type of request
 * @param <R> type of response to the request
 */
public abstract class AbstractRequestMapperWithOutputModifier<T extends AbstractRequestWithModifier<R>, R extends Response> extends AbstractRequestMapper {

    private static final String CONTENT_MODIFIER_REGEX = "\\$(\\w*)";
    private static final String CONTENT_MODIFIER_EXCLUDE_REGEX_TEMPLATE = "\\$((?!%s)\\w*)";
    private static final Pattern HAS_CONTENT_MODIFIER_PATTERN = Pattern.compile(String.format("^%s|.+/%s$", CONTENT_MODIFIER_REGEX, CONTENT_MODIFIER_REGEX));

    protected AbstractRequestMapperWithOutputModifier(ServiceContext serviceContext, HttpMethod method, String urlPattern, Content... excludedContentModifiers) {
        super(serviceContext, method, ensureUrlPatternAllowsContentModifier(urlPattern, excludedContentModifiers));
    }


    /**
     * Ensures that the provided url pattern accepts content modifier.
     *
     * @param urlPattern the url pattern
     * @param excludedContentModifiers content modifiers that are not allowed for this request as they are handled
     *            explicitely by another request. This is requred so that the generated URL patterns do not overlap.
     * @return the potentially modified url pattern accepting content modifier
     */
    protected static String ensureUrlPatternAllowsContentModifier(String urlPattern, Content... excludedContentModifiers) {
        String exampleUrl = RgxGen.parse(RegExHelper.removeGroupNames(urlPattern)).generate();
        if (HAS_CONTENT_MODIFIER_PATTERN.matcher(exampleUrl).matches()) {
            return urlPattern;
        }
        exampleUrl += "/$" + Content.DEFAULT.name().toLowerCase();
        if (exampleUrl.matches(urlPattern)) {
            return urlPattern;
        }
        String updatedUrlPattern = urlPattern;
        boolean regexContainsEndline = false;
        if (updatedUrlPattern.endsWith("$")) {
            updatedUrlPattern = updatedUrlPattern.substring(0, updatedUrlPattern.length() - 1);
            regexContainsEndline = true;
        }
        String contentModifierRegex = Objects.isNull(excludedContentModifiers) || excludedContentModifiers.length == 0
                ? CONTENT_MODIFIER_REGEX
                : String.format(
                        CONTENT_MODIFIER_EXCLUDE_REGEX_TEMPLATE,
                        Stream.of(excludedContentModifiers)
                                .map(Enum::name)
                                .map(String::toLowerCase)
                                .collect(Collectors.joining("|")));
        updatedUrlPattern = String.format("%s(%s%s)?%s",
                updatedUrlPattern,
                StringHelper.isBlank(urlPattern) ? "" : "/",
                contentModifierRegex,
                regexContainsEndline ? "$" : "");
        return updatedUrlPattern;
    }


    /**
     * Converts the HTTP request to protocol-agnostic request including output modifier information.
     *
     * @param httpRequest the HTTP request to convert
     * @param urlParameters map of named regex groups and their values
     * @param outputModifier output modifier for this request
     * @return the protocol-agnostic request
     * @throws InvalidRequestException if conversion fails
     */
    public abstract T doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException;


    private static Content parseContentModifier(String urlPath) throws UnsupportedContentModifierException {
        Matcher matcher = HAS_CONTENT_MODIFIER_PATTERN.matcher(urlPath);
        if (matcher.find()) {
            return Content.fromString(Objects.nonNull(matcher.group(1)) ? matcher.group(1) : matcher.group(2));
        }
        return Content.DEFAULT;
    }


    @Override
    public T doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        Class<AbstractRequestWithModifier<R>> rawType = (Class<AbstractRequestWithModifier<R>>) TypeToken.of(getClass())
                .resolveType(AbstractRequestMapperWithOutputModifier.class.getTypeParameters()[0])
                .getRawType();
        try {
            AbstractRequestWithModifier<R> request = ConstructorUtils.invokeConstructor(rawType);
            OutputModifier.Builder outputModifierBuilder = new OutputModifier.Builder();
            Content content = parseContentModifier(httpRequest.getPath());
            request.checkContenModifierValid(content);
            outputModifierBuilder.content(content);
            if (httpRequest.hasQueryParameter(QueryParameters.LEVEL)) {
                Level level = Level.fromString(httpRequest.getQueryParameter(QueryParameters.LEVEL));
                request.checkLevelModifierValid(level);
                outputModifierBuilder.level(level);
            }
            if (httpRequest.hasQueryParameter(QueryParameters.EXTENT)) {
                Extent extent = Extent.fromString(httpRequest.getQueryParameter(QueryParameters.EXTENT));
                request.checkExtentModifierValid(extent);
                outputModifierBuilder.extent(extent);
            }
            OutputModifier outputModifier = outputModifierBuilder.build();
            T result = doParse(httpRequest, urlParameters, outputModifier);
            result.setOutputModifier(outputModifier);
            return result;
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new InvalidRequestException("error resolving request class while trying to determine output modifier constraints", e);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidRequestException("invalid output modifier", e);
        }
    }
}
