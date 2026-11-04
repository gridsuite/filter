/*
 * Copyright (c) 2025, RTE
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.utils;

import com.powsybl.iidm.network.IdentifiableType;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.ExpertFilter;
import org.gridsuite.filter.wip.rule.CombinatorExpertRule;
import org.gridsuite.filter.wip.rule.ExpertRule;
import org.gridsuite.filter.wip.rule.StringExpertRule;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FilterWithEquipmentTypesUtils
 * @author Florent MILLOT <florent.millot_externe at rte-france.com>
 */
class FilterWithEquipmentTypesUtilsTest {

    @Test
    void testCreateRuleWithOneField() {
        List<String> ids = List.of("S1", "S2");
        ExpertRule rule = FilterWithEquipmentTypesUtils.createRuleWithOneField(ids, FieldType.SUBSTATION_ID);
        assertInstanceOf(StringExpertRule.class, rule);
        StringExpertRule stringRule = (StringExpertRule) rule;
        assertEquals(OperatorType.IN, stringRule.getOperator());
        assertEquals(FieldType.SUBSTATION_ID, stringRule.getField());
        assertEquals(ids, stringRule.getValues());
    }

    @Test
    void testCreateRuleWithTwoFields() {
        List<String> ids = List.of("S1");
        ExpertRule rule = FilterWithEquipmentTypesUtils.createRuleWithTwoFields(ids, FieldType.SUBSTATION_ID_1, FieldType.SUBSTATION_ID_2);
        assertInstanceOf(CombinatorExpertRule.class, rule);
        CombinatorExpertRule comb = (CombinatorExpertRule) rule;
        assertEquals(CombinatorType.OR, comb.getCombinator());
        List<ExpertRule> rules = comb.getRules();
        assertEquals(2, rules.size());
        assertInstanceOf(StringExpertRule.class, rules.get(0));
        assertInstanceOf(StringExpertRule.class, rules.get(1));
        StringExpertRule r1 = (StringExpertRule) rules.get(0);
        StringExpertRule r2 = (StringExpertRule) rules.get(1);
        assertEquals(OperatorType.IN, r1.getOperator());
        assertEquals(OperatorType.IN, r2.getOperator());
        assertEquals(FieldType.SUBSTATION_ID_1, r1.getField());
        assertEquals(FieldType.SUBSTATION_ID_2, r2.getField());
        assertEquals(ids, r1.getValues());
        assertEquals(ids, r2.getValues());
    }

    @Test
    void testCreateRuleWithThreeFields() {
        List<String> ids = List.of("VL1");
        ExpertRule rule = FilterWithEquipmentTypesUtils.createRuleWithThreeFields(ids, FieldType.VOLTAGE_LEVEL_ID_1, FieldType.VOLTAGE_LEVEL_ID_2, FieldType.VOLTAGE_LEVEL_ID_3);
        assertInstanceOf(CombinatorExpertRule.class, rule);
        CombinatorExpertRule comb = (CombinatorExpertRule) rule;
        assertEquals(CombinatorType.OR, comb.getCombinator());
        List<ExpertRule> rules = comb.getRules();
        assertEquals(3, rules.size());
        StringExpertRule r1 = (StringExpertRule) rules.get(0);
        StringExpertRule r2 = (StringExpertRule) rules.get(1);
        StringExpertRule r3 = (StringExpertRule) rules.get(2);
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_1, r1.getField());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_2, r2.getField());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_3, r3.getField());
        assertEquals(ids, r1.getValues());
        assertEquals(ids, r2.getValues());
        assertEquals(ids, r3.getValues());
    }

    @Test
    void testCreateSubstationRuleByEquipmentType() {
        List<String> subs = List.of("S1");
        // LOAD -> one field (SUBSTATION_ID)
        ExpertRule loadRule = FilterWithEquipmentTypesUtils.createSubstationRuleByEquipmentType(IdentifiableType.LOAD, subs);
        assertInstanceOf(StringExpertRule.class, loadRule);
        assertEquals(FieldType.SUBSTATION_ID, ((StringExpertRule) loadRule).getField());
        assertEquals(subs, ((StringExpertRule) loadRule).getValues());
        // LINE -> two fields (SUBSTATION_ID_1, _2)
        ExpertRule lineRule = FilterWithEquipmentTypesUtils.createSubstationRuleByEquipmentType(IdentifiableType.LINE, subs);
        assertInstanceOf(CombinatorExpertRule.class, lineRule);
        CombinatorExpertRule comb = (CombinatorExpertRule) lineRule;
        assertEquals(2, comb.getRules().size());
        StringExpertRule r1 = (StringExpertRule) comb.getRules().get(0);
        StringExpertRule r2 = (StringExpertRule) comb.getRules().get(1);
        assertEquals(FieldType.SUBSTATION_ID_1, r1.getField());
        assertEquals(FieldType.SUBSTATION_ID_2, r2.getField());
        assertEquals(subs, r1.getValues());
        assertEquals(subs, r2.getValues());
    }

    @Test
    void testCreateVoltageLevelRuleByEquipmentType() {
        List<String> vls = List.of("VL1", "VL2");
        // LOAD -> one field (VOLTAGE_LEVEL_ID)
        ExpertRule loadRule = FilterWithEquipmentTypesUtils.createVoltageLevelRuleByEquipmentType(IdentifiableType.LOAD, vls);
        assertInstanceOf(StringExpertRule.class, loadRule);
        assertEquals(FieldType.VOLTAGE_LEVEL_ID, ((StringExpertRule) loadRule).getField());
        assertEquals(vls, ((StringExpertRule) loadRule).getValues());
        // LINE -> two fields
        ExpertRule lineRule = FilterWithEquipmentTypesUtils.createVoltageLevelRuleByEquipmentType(IdentifiableType.LINE, vls);
        assertInstanceOf(CombinatorExpertRule.class, lineRule);
        CombinatorExpertRule comb2 = (CombinatorExpertRule) lineRule;
        assertEquals(2, comb2.getRules().size());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_1, ((StringExpertRule) comb2.getRules().get(0)).getField());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_2, ((StringExpertRule) comb2.getRules().get(1)).getField());
        assertEquals(vls, ((StringExpertRule) comb2.getRules().get(0)).getValues());
        assertEquals(vls, ((StringExpertRule) comb2.getRules().get(1)).getValues());
        // THREE_WINDINGS_TRANSFORMER -> three fields
        ExpertRule twtRule = FilterWithEquipmentTypesUtils.createVoltageLevelRuleByEquipmentType(IdentifiableType.THREE_WINDINGS_TRANSFORMER, vls);
        assertInstanceOf(CombinatorExpertRule.class, twtRule);
        CombinatorExpertRule comb3 = (CombinatorExpertRule) twtRule;
        assertEquals(3, comb3.getRules().size());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_1, ((StringExpertRule) comb3.getRules().get(0)).getField());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_2, ((StringExpertRule) comb3.getRules().get(1)).getField());
        assertEquals(FieldType.VOLTAGE_LEVEL_ID_3, ((StringExpertRule) comb3.getRules().get(2)).getField());
        assertEquals(vls, ((StringExpertRule) comb3.getRules().get(0)).getValues());
        assertEquals(vls, ((StringExpertRule) comb3.getRules().get(1)).getValues());
        assertEquals(vls, ((StringExpertRule) comb3.getRules().get(2)).getValues());
    }

    @Test
    void testCreateFiltersForSubEquipmentsFromSubstation() {
        List<String> parentIds = List.of("S1", "S2");
        Set<IdentifiableType> subTypes = Set.of(IdentifiableType.LOAD, IdentifiableType.LINE);
        List<ExpertFilter> filters = FilterWithEquipmentTypesUtils.createFiltersForSubEquipments(EquipmentType.SUBSTATION, parentIds, subTypes);
        assertEquals(2, filters.size());
        for (ExpertFilter f : filters) {
            if (f.getEquipmentType() == EquipmentType.LOAD) {
                assertInstanceOf(StringExpertRule.class, f.getRule());
                assertEquals(FieldType.SUBSTATION_ID, ((StringExpertRule) f.getRule()).getField());
                assertEquals(parentIds, ((StringExpertRule) f.getRule()).getValues());
            } else if (f.getEquipmentType() == EquipmentType.LINE) {
                assertInstanceOf(CombinatorExpertRule.class, f.getRule());
                CombinatorExpertRule comb = (CombinatorExpertRule) f.getRule();
                assertEquals(CombinatorType.OR, comb.getCombinator());
                assertEquals(2, comb.getRules().size());
                assertEquals(FieldType.SUBSTATION_ID_1, ((StringExpertRule) comb.getRules().get(0)).getField());
                assertEquals(FieldType.SUBSTATION_ID_2, ((StringExpertRule) comb.getRules().get(1)).getField());
                assertEquals(parentIds, ((StringExpertRule) comb.getRules().get(0)).getValues());
                assertEquals(parentIds, ((StringExpertRule) comb.getRules().get(1)).getValues());
            } else {
                fail("Unexpected equipment type: " + f.getEquipmentType());
            }
        }
    }

    @Test
    void testCreateFiltersForSubEquipmentsFromVoltageLevel() {
        List<String> parentIds = List.of("VL1");
        Set<IdentifiableType> subTypes = Set.of(IdentifiableType.GENERATOR, IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        List<ExpertFilter> filters = FilterWithEquipmentTypesUtils.createFiltersForSubEquipments(EquipmentType.VOLTAGE_LEVEL, parentIds, subTypes);
        assertEquals(2, filters.size());
        for (ExpertFilter f : filters) {
            if (f.getEquipmentType() == EquipmentType.GENERATOR) {
                assertInstanceOf(StringExpertRule.class, f.getRule());
                assertEquals(FieldType.VOLTAGE_LEVEL_ID, ((StringExpertRule) f.getRule()).getField());
                assertEquals(parentIds, ((StringExpertRule) f.getRule()).getValues());
            } else if (f.getEquipmentType() == EquipmentType.THREE_WINDINGS_TRANSFORMER) {
                assertInstanceOf(CombinatorExpertRule.class, f.getRule());
                CombinatorExpertRule comb = (CombinatorExpertRule) f.getRule();
                assertEquals(3, comb.getRules().size());
                assertEquals(FieldType.VOLTAGE_LEVEL_ID_1, ((StringExpertRule) comb.getRules().get(0)).getField());
                assertEquals(FieldType.VOLTAGE_LEVEL_ID_2, ((StringExpertRule) comb.getRules().get(1)).getField());
                assertEquals(FieldType.VOLTAGE_LEVEL_ID_3, ((StringExpertRule) comb.getRules().get(2)).getField());
                assertEquals(parentIds, ((StringExpertRule) comb.getRules().get(0)).getValues());
                assertEquals(parentIds, ((StringExpertRule) comb.getRules().get(1)).getValues());
                assertEquals(parentIds, ((StringExpertRule) comb.getRules().get(2)).getValues());
            } else {
                fail("Unexpected equipment type: " + f.getEquipmentType());
            }
        }
    }

    @Test
    void testCreateFiltersForSubEquipmentsUnsupported() {
        List<String> filteredEquipmentIDs = List.of("X");
        Set<IdentifiableType> subEquipmentTypes = Set.of(IdentifiableType.LOAD);

        assertThrows(
            UnsupportedOperationException.class,
            () -> FilterWithEquipmentTypesUtils.createFiltersForSubEquipments(EquipmentType.LINE, filteredEquipmentIDs, subEquipmentTypes)
        );
    }
}
