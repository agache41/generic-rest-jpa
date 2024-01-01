package org.structured.api.quarkus.util;

import java.util.function.Predicate;

public class Utils {
   public static <T> Predicate<T> not(Predicate<T> p) { return o -> !p.test(o); }
}
