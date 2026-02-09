/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public interface FilterLoader {
    List<AbstractFilter> getFilters(List<UUID> uuids);

    default Optional<AbstractFilter> getFilter(UUID uuid) {
        return getFilters(List.of(uuid)).stream().findFirst();
    }
}
