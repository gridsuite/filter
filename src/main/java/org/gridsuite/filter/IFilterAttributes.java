/*
 *  Copyright (c) 2021, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter;

import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 */
public interface IFilterAttributes {
    java.util.UUID getId();

    java.util.Date getModificationDate();

    FilterType getType();

    EquipmentType getEquipmentType();
}
