/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rule;

import com.google.common.annotations.Beta;
import lombok.EqualsAndHashCode;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractCachingExpertRule extends AbstractExpertRule {

    public abstract void clearCache();
}

