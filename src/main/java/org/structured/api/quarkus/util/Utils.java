package org.structured.api.quarkus.util;

import java.util.function.Predicate;

/**
 * The type Utils.
 */
public class Utils {
   /**
    * Not predicate.
    *
    * @param <T> the type parameter
    * @param p   the p
    * @return the predicate
    */
   public static <T> Predicate<T> not(Predicate<T> p) { return o -> !p.test(o); }
}
