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
package org.eclipse.digitaltwin.fa3st.service.endpoint.http.serialization;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import net.javacrumbs.jsonunit.core.Option;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.Result;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResult;
import org.eclipse.digitaltwin.fa3st.common.dataformat.SerializationException;
import org.eclipse.digitaltwin.fa3st.common.exception.UnsupportedModifierException;
import org.eclipse.digitaltwin.fa3st.common.model.api.Message;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;


public class HttpJsonSerializerTest {

    private final HttpJsonApiSerializer serializer = new HttpJsonApiSerializer();

    @Test
    public void testEnumsWithCustomNaming() throws SerializationException, UnsupportedModifierException {
        Assert.assertEquals("\"Error\"", serializer.write(MessageTypeEnum.ERROR));
    }


    @Test
    public void testResult() throws SerializationException, ParseException, UnsupportedModifierException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Result result = new DefaultResult.Builder()
                .messages(Message.builder()
                        .text(HttpStatus.getMessage(404))
                        .messageType(MessageTypeEnum.ERROR)
                        .code(HttpStatus.getMessage(404))
                        .timestamp("2022-01-01T00:00:00.000+00:00")
                        .build())
                .build();
        String actual = serializer.write(result);
        String expected = "{\n"
                + "  \"messages\" : [ {\n"
                + "    \"messageType\" : \"Error\",\n"
                + "    \"text\" : \"Not Found\",\n"
                + "    \"code\" : \"Not Found\",\n"
                + "    \"timestamp\" : \"2022-01-01T00:00:00.000+00:00\"\n"
                + "  } ]\n"
                + "}";
        assertThatJson(actual)
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }
}
