
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

package io.github.agache41.generic.rest.jpa.modell.resources;

import io.github.agache41.generic.rest.jpa.modell.dataBinder.SubModellAFksDataBinder;
import io.github.agache41.generic.rest.jpa.modell.dataaccess.SubModellAFksDataAccess;
import io.github.agache41.generic.rest.jpa.modell.entities.SubModellAFks;
import io.github.agache41.generic.rest.jpa.resourceService.AbstractResourceServiceImpl;
import lombok.Getter;

@Getter
public class SubModellAFksResourceService extends AbstractResourceServiceImpl<SubModellAFks, SubModellAFks, Long> {
    protected SubModellAFksDataBinder dataBinder = new SubModellAFksDataBinder();
    protected SubModellAFksDataAccess dataAccess = new SubModellAFksDataAccess();

    public SubModellAFksResourceService() {
        this.postConstruct();
    }
}