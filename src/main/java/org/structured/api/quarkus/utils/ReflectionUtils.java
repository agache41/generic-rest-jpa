package org.structured.api.quarkus.utils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ReflectionUtils {

    /**
     * <pre>
     * Returns a list of declared fields in the class and in the base class(es).
     * </pre>
     *
     * @param cls the cls
     * @return the declared fields
     */
    public static List<Field> getDeclaredFields(Class<?> cls) {
        List<Field> declaredFields = new LinkedList<>();
        while (cls != null && !cls.equals(Object.class)) {
            Collections.addAll(declaredFields, cls.getDeclaredFields());
            cls = cls.getSuperclass();
        }
        return declaredFields;
    }
}
