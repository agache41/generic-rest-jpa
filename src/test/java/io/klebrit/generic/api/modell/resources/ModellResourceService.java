
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

package io.klebrit.generic.api.modell.resources;


import io.klebrit.generic.api.modell.dataaccess.ModellDataAccess;
import io.klebrit.generic.api.modell.entities.Modell;
import io.klebrit.generic.api.resourceService.AbstractResourceServiceImpl;
import lombok.Getter;

@Getter
public class ModellResourceService extends AbstractResourceServiceImpl<Modell, Long> {
    protected ModellDataAccess dataAccess = new ModellDataAccess();
}