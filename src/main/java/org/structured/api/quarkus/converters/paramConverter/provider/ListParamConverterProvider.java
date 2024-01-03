
/*
 *    Copyright 2022-2023  Alexandru Agache
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.structured.api.quarkus.converters.paramConverter.provider;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.structured.api.quarkus.converters.paramConverter.IntegerListParamConverter;
import org.structured.api.quarkus.converters.paramConverter.LocalDateParamConverter;
import org.structured.api.quarkus.converters.paramConverter.LongListParamConverter;
import org.structured.api.quarkus.converters.paramConverter.StringListParamConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type List param converter provider.
 */
@Provider
public class ListParamConverterProvider implements ParamConverterProvider {
    /**
     * The Log.
     */
    @Inject
    Logger log;

    private final Map<Type, ParamConverter<?>> paramConverterMap = new HashMap<>();

    /**
     * Post construct.
     */
    @PostConstruct
    public void postConstruct() {
        this.paramConverterMap.put(String.class, new StringListParamConverter(log));
        this.paramConverterMap.put(Integer.class, new IntegerListParamConverter(log));
        this.paramConverterMap.put(Long.class, new LongListParamConverter(log));
        this.paramConverterMap.put(LocalDate.class, new LocalDateParamConverter(log));
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType,
                                              Annotation[] annotations) {
        if (rawType.equals(List.class)) {
            ParamConverter<?> paramConverter = null;
            if (genericType instanceof ParameterizedType) {
                ParameterizedType ptype = (ParameterizedType) genericType;
                Type[] actualTypeArguments = ptype.getActualTypeArguments();
                if (actualTypeArguments.length > 0) {
                    Type parameterType = actualTypeArguments[0];
                    paramConverter = paramConverterMap.get(parameterType);
                }
            }
            if (paramConverter == null) {
                log.errorf("No Parameter Converter found for Class %s with generic Type %s", rawType.getSimpleName(), genericType);
            } else {
                log.infof(" Binding %s to param of type %s ", paramConverter.getClass()
                                                                            .getSimpleName(), genericType);
            }
            return (ParamConverter<T>) paramConverter;
        }
        return null;
    }
}