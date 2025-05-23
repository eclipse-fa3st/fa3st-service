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
package org.eclipse.digitaltwin.fa3st.service.assetconnection.common.util;

import java.util.Map;
import java.util.Objects;


/**
 * Helper to replace variables of type ${...} in strings.
 */
public class TemplateHelper {

    private static final String KEY_TEMPLATE = "\\$\\{%s\\}";

    private TemplateHelper() {}


    /**
     * Replaces as set of {@code values} within a given string. The keys of {@code values} are wrapped in <i>${...}</i>
     * and those occurances in the {@code template} are replaced by the corresponding values.
     *
     * @param template the template to replace values in
     * @param values the values to replace
     * @return {@code template} with replaced values
     */
    public static String replace(String template, Map<String, Object> values) {
        return values.entrySet().stream()
                .reduce(template,
                        (temp, element) -> temp.replaceAll(
                                String.format(KEY_TEMPLATE, element.getKey()),
                                Objects.toString(element.getValue())),
                        (x, y) -> y);
    }
}
