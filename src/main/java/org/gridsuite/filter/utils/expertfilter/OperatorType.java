/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils.expertfilter;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
public enum OperatorType {
    // Common
    EQUALS,
    NOT_EQUALS,
    IN,
    IS_IN,
    IS_NOT_IN,
    NOT_IN,
    // Number and String and Boolean
    EXISTS,
    NOT_EXISTS,
    // Number
    LOWER,
    LOWER_OR_EQUALS,
    GREATER,
    GREATER_OR_EQUALS,
    BETWEEN,
    // String
    IS,
    CONTAINS,
    BEGINS_WITH,
    ENDS_WITH,
    // Uuid
    IS_PART_OF,
    IS_NOT_PART_OF;

    public static boolean isMultipleCriteriaOperator(OperatorType operator) {
        return operator == IN || operator == NOT_IN || operator == BETWEEN || operator == IS_PART_OF || operator == IS_NOT_PART_OF;
    }
}
