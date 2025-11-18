/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.exception;

import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Mohamed BENREJEB <mohamed.ben-rejeb at rte-france.com>
 */
@Getter
public class FilterCycleException extends RuntimeException {

    private final List<UUID> cycleFilterIds;

    public FilterCycleException(String message, List<UUID> cycleFilterIds) {
        super(Objects.requireNonNull(message, "message must not be null"));
        this.cycleFilterIds = List.copyOf(Objects.requireNonNull(cycleFilterIds, "cycleFilterIds must not be null"));
    }
}
