
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

package io.github.agache41.generic.rest.jpa.update.updater;

/**
 * The Updater defines a single method meant to update a target based on data coming from a source.
 * The update process happens at field level and the implementing classes treat the cases for simpek types, collections ...
 *
 * @param <TARGET> the type parameter
 * @param <SOURCE> the type parameter
 */
public interface Updater<TARGET, SOURCE> {
    /**
     * The method updates the field in target based on the field the source
     *
     * @param target the target
     * @param source the source
     * @return if the update introduced changes in the target
     */
    boolean update(TARGET target,
                   SOURCE source);
}
