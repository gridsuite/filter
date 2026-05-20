package org.gridsuite.filter.wip.expert.data;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
public enum OperatorType {
    // Common
    EQUALS,
    NOT_EQUALS,
    IN,
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
        return operator == IN ||
               operator == NOT_IN ||
               operator == BETWEEN ||
               operator == IS_PART_OF ||
               operator == IS_NOT_PART_OF;
    }
}
