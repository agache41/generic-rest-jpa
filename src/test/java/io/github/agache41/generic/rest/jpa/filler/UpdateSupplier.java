
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

package io.github.agache41.generic.rest.jpa.filler;

import io.github.agache41.generic.rest.jpa.dataAccess.PrimaryKey;
import io.github.agache41.generic.rest.jpa.update.reflector.ClassReflector;
import io.github.agache41.generic.rest.jpa.update.reflector.FieldReflector;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class UpdateSupplier<T> implements java.util.function.Supplier<T> {

    public static final int listSize = 128;
    private static final Map<Class<?>, UpdateSupplier<?>> feederCache = new ConcurrentHashMap<>();

    static {
        //UpdateSupplier.add(new StringRandomSupplier());
        UpdateSupplier.add(new EnglishWordsSupplier());
        UpdateSupplier.add(new IntegerRandomSupplier());
        UpdateSupplier.add(new LongRandomSupplier());
        UpdateSupplier.add(new DoubleRandomSupplier());
        UpdateSupplier.add(new DoubleRandomSupplier());
        UpdateSupplier.add(new BooleanRandomSupplier());
        UpdateSupplier.add(new ShortRandomSupplier());
        UpdateSupplier.add(new BigDecimalRandomSupplier());
        UpdateSupplier.add(new BigIntegerRandomSupplier());
        //feederCache.put(String.class, new StringFeeder());
    }

    protected final Random random = new Random();
    private final Class<T> clazz;

    public UpdateSupplier(final Class<T> clazz) {
        this.clazz = clazz;
    }

    public static void add(final UpdateSupplier<?> supplier) {
        feederCache.put(supplier.getClazz(), supplier);
    }

    public static <R> UpdateSupplier<R> ofClass(@NotNull final Class<R> clazz) {
        return (UpdateSupplier<R>) feederCache.computeIfAbsent(clazz, cls -> new UpdateSupplier(cls));
    }

    public List<T> list() {
        return this.list(listSize);
    }

    public List<T> list(final int size) {
        final List<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
            result.add(this.get());
        return result;
    }

    @Override
    public T get() {

        final ClassReflector<T> classReflector = ClassReflector.ofClass(this.clazz);

        final T result = classReflector.newInstance();

        for (final FieldReflector fieldReflector : classReflector.getUpdateReflectorsArray()) {
            final Class<?> fieldType = fieldReflector.getType();
            if (fieldReflector.isValue()) {
                if (UpdateSupplier.feederCache.containsKey(fieldType)) {
                    fieldReflector.set(result, UpdateSupplier.feederCache.get(fieldType)
                                                                         .get());
                } else {
                    System.out.println("No Value feeder for type " + fieldType.getSimpleName());
                }
            } else if (fieldReflector.isCollection()) {
                final Class<?> collectionParameter = fieldReflector.getFirstParameter();
                final Collection collection = (Collection) fieldReflector.get(result);
                if (collection != null && collectionParameter != null) {
                    collection.addAll(UpdateSupplier.ofClass(collectionParameter)
                                                    .list(listSize));
                }
            } else if (fieldReflector.isMap()) {
                final Class<?> mapKeyParameter = fieldReflector.getFirstParameter();
                final Class<?> mapValueParameter = fieldReflector.getSecondParameter();

                final Map map = (Map) fieldReflector.get(result);
                if (map != null && mapKeyParameter != null && mapValueParameter != null) {
                    if (!PrimaryKey.class.isAssignableFrom(mapValueParameter)) {
                        final UpdateSupplier<?> valueSupplier = UpdateSupplier.ofClass(mapValueParameter);
                        final UpdateSupplier<?> keySupplier = UpdateSupplier.ofClass(mapKeyParameter);
                        for (int i = 0; i < listSize; i++) {
                            map.put(keySupplier.get(), valueSupplier.get());
                        }
                    } else {
                        final UpdateSupplier<PrimaryKey> valueSupplier = UpdateSupplier.ofClass((Class<PrimaryKey>) mapValueParameter);
                        for (int i = 0; i < listSize; i++) {
                            final PrimaryKey primaryKey = valueSupplier.get();
                            map.put(primaryKey.getId(), primaryKey);
                        }
                    }
                }
            } else {
                // do recurse on the type
                fieldReflector.set(result, UpdateSupplier.ofClass(fieldType)
                                                         .get());
            }
        }
        return result;
    }

    public Class<T> getClazz() {
        return this.clazz;
    }

    public static class StringRandomSupplier extends UpdateSupplier<String> {

        private static final String[] commonWords = {"the", "of", "and", "a", "to", "in", "is", "you", "that", "it", "he", "was", "for", "on", "are", "as", "with", "his", "they", "I", "at", "be", "this", "have", "from", "or", "one", "had", "by", "word", "but", "not", "what", "all", "were", "we", "when", "your", "can", "said", "there", "use", "an", "each", "which", "she", "do", "how", "their", "if", "will", "up", "other", "about", "out", "many", "then", "them", "these", "so", "some", "her", "would", "make", "like", "him", "into", "time", "has", "look", "two", "more", "write", "go", "see", "number", "no", "way", "could", "people", "my", "than", "first", "water", "been", "call", "who", "oil", "its", "now", "find", "long", "down", "day", "did", "get", "come", "made", "may", "part"};
        final int leftLimit = 101; // letter 'a '
        final int rightLimit = 122; // letter 'z'
        final int targetStringLength = 16;

        public StringRandomSupplier() {
            super(String.class);
        }

        @Override
        public String get() {
            final StringBuilder buffer = new StringBuilder(this.targetStringLength);
            for (int i = 0; i < this.targetStringLength; i++) {
                final int randomLimitedInt = this.leftLimit + (int) (this.random.nextFloat() * (this.rightLimit - this.leftLimit + 1));
                buffer.append((char) randomLimitedInt);
            }
            return buffer.toString();
        }
    }

    public static class EnglishWordsSupplier extends UpdateSupplier<String> {

        private static final String[] commonWords = {"the", "of", "and", "a", "to", "in", "is", "you", "that", "it", "he", "was", "for", "on", "are", "as", "with", "his", "they", "I", "at", "be", "this", "have", "from", "or", "one", "had", "by", "word", "but", "not", "what", "all", "were", "we", "when", "your", "can", "said", "there", "use", "an", "each", "which", "she", "do", "how", "their", "if", "will", "up", "other", "about", "out", "many", "then", "them", "these", "so", "some", "her", "would", "make", "like", "him", "into", "time", "has", "look", "two", "more", "write", "go", "see", "number", "no", "way", "could", "people", "my", "than", "first", "water", "been", "call", "who", "oil", "its", "now", "find", "long", "down", "day", "did", "get", "come", "made", "may", "part"};

        //private static final String[] commonWords = {"t", "th", "the", "the ", "the quick", "the quick brown", "the quick brown fox"}
        public EnglishWordsSupplier() {
            super(String.class);
        }

        @Override
        public String get() {
            return commonWords[(int) (this.random.nextFloat() * (commonWords.length - 1))];
        }
    }

    public static class IntegerRandomSupplier extends UpdateSupplier<Integer> {
        final int max = 100000;

        public IntegerRandomSupplier() {
            super(Integer.class);
        }

        @Override
        public Integer get() {
            return (int) (this.random.nextFloat() * this.max);
        }
    }

    public static class LongRandomSupplier extends UpdateSupplier<Long> {
        final long max = 10000000;

        public LongRandomSupplier() {
            super(Long.class);
        }

        @Override
        public Long get() {
            return (long) (this.random.nextFloat() * this.max);
        }
    }

    public static class BooleanRandomSupplier extends UpdateSupplier<Boolean> {

        public BooleanRandomSupplier() {
            super(Boolean.class);
        }

        @Override
        public Boolean get() {
            return this.random.nextFloat() > 0.5;
        }
    }

    public static class ShortRandomSupplier extends UpdateSupplier<Short> {

        final short max = 10000;

        public ShortRandomSupplier() {
            super(Short.class);
        }

        @Override
        public Short get() {
            return (short) (this.random.nextFloat() * this.max);
        }
    }

    public static class DoubleRandomSupplier extends UpdateSupplier<Double> {
        final short max = 10000;

        public DoubleRandomSupplier() {
            super(Double.class);
        }

        @Override
        public Double get() {
            return (double) (this.random.nextFloat() * this.max);
        }
    }

    public static class FloatRandomSupplier extends UpdateSupplier<Float> {
        final short max = 10000;

        public FloatRandomSupplier() {
            super(Float.class);
        }

        @Override
        public Float get() {
            return this.random.nextFloat() * this.max;
        }
    }

    public static class BigIntegerRandomSupplier extends UpdateSupplier<BigInteger> {
        final short max = 10000;

        public BigIntegerRandomSupplier() {
            super(BigInteger.class);
        }

        @Override
        public BigInteger get() {
            return BigInteger.valueOf((long) (this.random.nextFloat() * this.max));
        }
    }

    public static class BigDecimalRandomSupplier extends UpdateSupplier<BigDecimal> {
        final short max = 10000;

        public BigDecimalRandomSupplier() {
            super(BigDecimal.class);
        }

        @Override
        public BigDecimal get() {
            return BigDecimal.valueOf((long) (this.random.nextFloat() * this.max));
        }
    }
}