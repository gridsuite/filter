/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.*;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.*;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilterEquipmentAttributes;
import org.gridsuite.filter.scriptfilter.ScriptFilter;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static org.gridsuite.filter.utils.expertfilter.FieldType.*;
import static org.gridsuite.filter.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class FiltersUtilsTest {
    private Network network;
    private Network network2;
    private Network network3;
    private Network network4;
    private Network network5;

    private FilterLoader filterLoader;

    @BeforeEach
    void setUp() {
        network = EurostagTutorialExample1Factory.createWithMoreGenerators();
        network.getSubstation("P1").setProperty("region", "north");
        network.getSubstation("P2").setProperty("region", "south");
        network.getGenerator("GEN").setProperty("region", "north");
        network.getGenerator("GEN2").setProperty("region", "south");
        network.getLoad("LOAD").setProperty("region", "north");
        network.getTwoWindingsTransformer("NGEN_NHV1").setProperty("region", "south");
        network.getTwoWindingsTransformer("NHV2_NLOAD").setProperty("region", "south");
        network.getLine("NHV1_NHV2_1").setProperty("region", "south");
        network.getLine("NHV1_NHV2_2").setProperty("region", "south");

        network2 = HvdcTestNetwork.createVsc();
        network2.getSubstation("S2").setProperty("region", "north");
        network3 = SvcTestCaseFactory.createWithMoreSVCs();
        network4 = ShuntTestCaseFactory.create();
        network5 = ThreeWindingsTransformerNetworkFactory.create();
        filterLoader = uuids -> null;
    }

    @Test
    void testSubstationFilter() {

        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY).operator(IN).values(Set.of("FR", "IT")).build());
        rules.add(PropertiesExpertRule.builder().field(FREE_PROPERTIES).propertyName("region").operator(IN)
            .propertyValues(List.of("north")).build());
        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();

        ExpertFilter substationFilter = new ExpertFilter(UUID.randomUUID(), new Date(), EquipmentType.SUBSTATION, parentRule);

        assertNotNull(substationFilter.getRules());
        assertFalse(substationFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.SUBSTATION, substationFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(substationFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("P1", identifiables.get(0).getId());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("P1", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.SUBSTATION,
            filterEquipmentAttributes);
        assertEquals(FilterType.IDENTIFIER_LIST, identifierListFilter.getType());

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("P1", identifiables.get(0).getId());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.SUBSTATION,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.IS).value("P1").build());
        assertEquals(FilterType.EXPERT, expertFilter.getType());
        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("P1", identifiables.get(0).getId());
    }

    @Test
    void testVoltageLevelFilter() {

        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY).operator(IN).values(Set.of("FR", "IT")).build());
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE, 15., 30.));
        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();

        ExpertFilter voltageLevelFilter = new ExpertFilter(UUID.randomUUID(), new Date(),
            EquipmentType.VOLTAGE_LEVEL, parentRule);

        assertNotNull(voltageLevelFilter.getRules());
        assertFalse(voltageLevelFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.VOLTAGE_LEVEL, voltageLevelFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(voltageLevelFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("VLGEN", identifiables.get(0).getId());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("VLGEN", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.VOLTAGE_LEVEL,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("VLGEN", identifiables.get(0).getId());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.VOLTAGE_LEVEL,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.IS).value("VLGEN").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("VLGEN", identifiables.get(0).getId());
    }

    @Test
    void testLineFilter() {

        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY_1).operator(EQUALS).value("FR").build());
        rules.add(EnumExpertRule.builder().field(COUNTRY_2).operator(EQUALS).value("FR").build());
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE_1, 360., 400.));
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE_2, 356.25, 393.75));

        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter lineFilter = new ExpertFilter(UUID.randomUUID(), new Date(), EquipmentType.LINE, parentRule);

        assertNotNull(lineFilter.getRules());
        assertFalse(lineFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.LINE, lineFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(lineFilter, network, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("NHV1_NHV2_1", identifiables.get(0).getId());
        assertEquals("NHV1_NHV2_2", identifiables.get(1).getId());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("NHV1_NHV2_1", 30.),
            new IdentifierListFilterEquipmentAttributes("NHV1_NHV2_2", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.LINE,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("NHV1_NHV2_1", identifiables.get(0).getId());
        assertEquals("NHV1_NHV2_2", identifiables.get(1).getId());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.LINE,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.IS).value("NHV1_NHV2_2").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("NHV1_NHV2_2", identifiables.get(0).getId());
    }

    @Test
    void testTwoWindingsTransformerFilter() {

        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY).operator(EQUALS).value("FR").build());
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE_1, 380., null));
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE_2, 142.4, 157.5));

        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter twoWindingsTransformerFilter = new ExpertFilter(UUID.randomUUID(), new Date(),
            EquipmentType.TWO_WINDINGS_TRANSFORMER, parentRule);

        assertNotNull(twoWindingsTransformerFilter.getRules());
        assertFalse(twoWindingsTransformerFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(twoWindingsTransformerFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("NHV2_NLOAD", identifiables.get(0).getId());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("NHV2_NLOAD", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.TWO_WINDINGS_TRANSFORMER,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("NHV2_NLOAD", identifiables.get(0).getId());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.TWO_WINDINGS_TRANSFORMER,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.IS).value("NHV2_NLOAD").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("NHV2_NLOAD", identifiables.get(0).getId());
    }

    private NumberExpertRule nominalVoltageRuleExpert(FieldType field, Double value1, Double value2) {
        if (value1 == null) {
            return null;
        } else if (value2 == null) {
            return NumberExpertRule.builder().field(field).operator(EQUALS).value(value1).build();
        } else {
            return NumberExpertRule.builder().field(field).operator(BETWEEN).
                values(new TreeSet<>(Set.of(value1, value2))).build();
        }
    }

    @Test
    void testThreeWindingsTransformerFilter() {

        //Nominal voltage value for 3WT :
        // T1 : 132, T2 : 33, T3 : 11

        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY).operator(IN).values(new TreeSet<>(Set.of("FR"))).build());
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE_1, 130., 140.));
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE_2, 30., 40.));
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE_3, 11., null));

        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter threeWindingsTransformerFilter = new ExpertFilter(UUID.randomUUID(), new Date(),
            EquipmentType.THREE_WINDINGS_TRANSFORMER, parentRule);

        assertNotNull(threeWindingsTransformerFilter.getRules());
        assertFalse(threeWindingsTransformerFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(threeWindingsTransformerFilter, network5, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("3WT", identifiables.get(0).getId());

        rules.set(1, nominalVoltageRuleExpert(NOMINAL_VOLTAGE_1, 30., 40.));
        rules.set(2, nominalVoltageRuleExpert(NOMINAL_VOLTAGE_2, 11., null));
        rules.set(3, nominalVoltageRuleExpert(NOMINAL_VOLTAGE_3, 130., 140.));
        threeWindingsTransformerFilter.getRules().setRules(rules);

        identifiables = FiltersUtils.getIdentifiables(threeWindingsTransformerFilter, network5, filterLoader);
        assertEquals(0, identifiables.size());

        rules.set(1, nominalVoltageRuleExpert(NOMINAL_VOLTAGE_1, 11., null));
        rules.set(2, nominalVoltageRuleExpert(NOMINAL_VOLTAGE_2, 130., 140.));
        rules.set(3, nominalVoltageRuleExpert(NOMINAL_VOLTAGE_3, 30., 40.));
        threeWindingsTransformerFilter.getRules().setRules(rules);
        identifiables = FiltersUtils.getIdentifiables(threeWindingsTransformerFilter, network5, filterLoader);
        assertEquals(0, identifiables.size());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("3WT", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.THREE_WINDINGS_TRANSFORMER,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network5, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("3WT", identifiables.get(0).getId());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.THREE_WINDINGS_TRANSFORMER,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.IS).value("3WT").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network5, filterLoader);
        assertEquals(1, identifiables.size());
        assertInstanceOf(ThreeWindingsTransformer.class, identifiables.get(0));
        assertEquals("3WT", identifiables.get(0).getId());
    }

    @Test
    void testGeneratorFilter() {
        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY).operator(IN).values(new TreeSet<>(Set.of("FR", "IT"))).build());
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE, 15., 30.));

        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter generatorFilter = new ExpertFilter(UUID.randomUUID(), new Date(), EquipmentType.GENERATOR, parentRule);
        assertNotNull(generatorFilter.getRules());
        assertFalse(generatorFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.GENERATOR, generatorFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(generatorFilter, network, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("GEN", identifiables.get(0).getId());
        assertEquals("GEN2", identifiables.get(1).getId());

        rules.set(1, NumberExpertRule.builder().field(NOMINAL_VOLTAGE).operator(GREATER).value(50.).build());
        generatorFilter.getRules().setRules(rules);
        identifiables = FiltersUtils.getIdentifiables(generatorFilter, network, filterLoader);
        assertEquals(0, identifiables.size());

        rules.set(1, NumberExpertRule.builder().field(NOMINAL_VOLTAGE).operator(GREATER_OR_EQUALS).value(10.).build());
        generatorFilter.getRules().setRules(rules);
        identifiables = FiltersUtils.getIdentifiables(generatorFilter, network, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("GEN", identifiables.get(0).getId());
        assertEquals("GEN2", identifiables.get(1).getId());

        rules.set(1, NumberExpertRule.builder().field(NOMINAL_VOLTAGE).operator(LOWER).value(12.).build());
        generatorFilter.getRules().setRules(rules);
        identifiables = FiltersUtils.getIdentifiables(generatorFilter, network, filterLoader);
        assertEquals(0, identifiables.size());

        rules.set(1, NumberExpertRule.builder().field(NOMINAL_VOLTAGE).operator(LOWER_OR_EQUALS).value(40.).build());
        generatorFilter.getRules().setRules(rules);
        identifiables = FiltersUtils.getIdentifiables(generatorFilter, network, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("GEN", identifiables.get(0).getId());
        assertEquals("GEN2", identifiables.get(1).getId());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("GEN", 30.),
            new IdentifierListFilterEquipmentAttributes("GEN2", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.GENERATOR,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("GEN", identifiables.get(0).getId());
        assertEquals("GEN2", identifiables.get(1).getId());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.GENERATOR,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.BEGINS_WITH).value("GEN").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("GEN", identifiables.get(0).getId());
        assertEquals("GEN2", identifiables.get(1).getId());
    }

    @Test
    void testLoadFilter() {
        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY).operator(IN).values(new TreeSet<>(Set.of("FR", "IT"))).build());
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE, 144., 176.));

        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter loadFilter = new ExpertFilter(UUID.randomUUID(), new Date(), EquipmentType.LOAD, parentRule);

        assertNotNull(loadFilter.getRules());
        assertFalse(loadFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.LOAD, loadFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(loadFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("LOAD", identifiables.get(0).getId());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("LOAD", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.LOAD,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("LOAD", identifiables.get(0).getId());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.LOAD,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.IS).value("LOAD").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("LOAD", identifiables.get(0).getId());
    }

    @Test
    void testBatteryFilter() {

        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY).operator(IN).values(new TreeSet<>(Set.of("FR", "IT"))).build());
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE, 144., 176.));
        rules.add(PropertiesExpertRule.builder().field(SUBSTATION_PROPERTIES).propertyName("Name")
            .propertyValues(List.of("P2")).build());

        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter batteryFilter = new ExpertFilter(UUID.randomUUID(), new Date(), EquipmentType.BATTERY, parentRule);

        assertNotNull(batteryFilter.getRules());
        assertFalse(batteryFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.BATTERY, batteryFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(batteryFilter, network, filterLoader);
        assertEquals(0, identifiables.size());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("battery1", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.BATTERY,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
        assertEquals(0, identifiables.size());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.BATTERY,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.IS).value("battery1").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(0, identifiables.size());
    }

    @Test
    void testShuntCompensatorFilter() {

        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY).operator(IN).values(new TreeSet<>(Set.of("FR"))).build());
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE, 380., null));

        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter shuntCompensatorFilter = new ExpertFilter(UUID.randomUUID(), new Date(), EquipmentType.SHUNT_COMPENSATOR, parentRule);

        assertNotNull(shuntCompensatorFilter.getRules());
        assertFalse(shuntCompensatorFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.SHUNT_COMPENSATOR, shuntCompensatorFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(shuntCompensatorFilter, network4, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("SHUNT", identifiables.get(0).getId());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("SHUNT", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.SHUNT_COMPENSATOR,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network4, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("SHUNT", identifiables.get(0).getId());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.SHUNT_COMPENSATOR,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.IS).value("SHUNT").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network4, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("SHUNT", identifiables.get(0).getId());
    }

    @Test
    void testStaticVarCompensatorFilter() {

        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY).operator(IN).values(new TreeSet<>(Set.of("FR"))).build());
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE, 380., null));

        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter staticVarCompensatorFilter = new ExpertFilter(UUID.randomUUID(), new Date(), EquipmentType.STATIC_VAR_COMPENSATOR, parentRule);

        assertNotNull(staticVarCompensatorFilter.getRules());
        assertFalse(staticVarCompensatorFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.STATIC_VAR_COMPENSATOR, staticVarCompensatorFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(staticVarCompensatorFilter, network3, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("SVC2", identifiables.get(0).getId());
        assertEquals("SVC3", identifiables.get(1).getId());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("SVC2", 30.),
            new IdentifierListFilterEquipmentAttributes("SVC3", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.STATIC_VAR_COMPENSATOR,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network3, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("SVC2", identifiables.get(0).getId());
        assertEquals("SVC3", identifiables.get(1).getId());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.STATIC_VAR_COMPENSATOR,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.BEGINS_WITH).value("SVC").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network3, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("SVC2", identifiables.get(0).getId());
        assertEquals("SVC3", identifiables.get(1).getId());
    }

    @Test
    void testDanglingLineFilter() {
        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY).operator(IN).values(new TreeSet<>(Set.of("FR"))).build());
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE, 380., null));
        rules.add(PropertiesExpertRule.builder().field(SUBSTATION_PROPERTIES).propertyName("Name")
            .propertyValues(List.of("S2")).build());

        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter danglingLineFilter = new ExpertFilter(UUID.randomUUID(), new Date(), EquipmentType.DANGLING_LINE, parentRule);

        assertNotNull(danglingLineFilter.getRules());
        assertFalse(danglingLineFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.DANGLING_LINE, danglingLineFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(danglingLineFilter, network, filterLoader);
        assertEquals(0, identifiables.size());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("danglineLine1", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.DANGLING_LINE,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
        assertEquals(0, identifiables.size());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.DANGLING_LINE,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.BEGINS_WITH).value("danglineLine1").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(0, identifiables.size());
    }

    @Test
    void testBusbarSectionFilter() {
        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY).operator(IN).values(new TreeSet<>(Set.of("FR"))).build());
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE, 380., null));
        rules.add(PropertiesExpertRule.builder().field(SUBSTATION_PROPERTIES).propertyName("Name")
            .propertyValues(List.of("S1")).build());

        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter busbarSectionFilter = new ExpertFilter(UUID.randomUUID(), new Date(), EquipmentType.BUSBAR_SECTION, parentRule);

        assertNotNull(busbarSectionFilter.getRules());
        assertFalse(busbarSectionFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.BUSBAR_SECTION, busbarSectionFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(busbarSectionFilter, network, filterLoader);
        assertEquals(0, identifiables.size());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("busbarSection1", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.BUSBAR_SECTION,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
        assertEquals(0, identifiables.size());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.BUSBAR_SECTION,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.BEGINS_WITH).value("busbarSection1").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(0, identifiables.size());
    }

    @Test
    void testBusFilter() {
        // expert filter only for bus
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.BUS,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.IS).value("NGEN").build());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("NGEN", identifiables.get(0).getId());
    }

    @Test
    void testLccConverterStationFilter() {
        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY).operator(IN).values(new TreeSet<>(Set.of("FR"))).build());
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE, 380., null));
        rules.add(PropertiesExpertRule.builder().field(SUBSTATION_PROPERTIES).propertyName("Name")
            .propertyValues(List.of("S1")).build());

        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter lccConverterStationFilter = new ExpertFilter(UUID.randomUUID(), new Date(),
            EquipmentType.LCC_CONVERTER_STATION, parentRule);

        assertNotNull(lccConverterStationFilter);
        assertFalse(lccConverterStationFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.LCC_CONVERTER_STATION, lccConverterStationFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(lccConverterStationFilter, network2, filterLoader);
        assertEquals(0, identifiables.size());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("lcc1", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.LCC_CONVERTER_STATION,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
        assertEquals(0, identifiables.size());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.LCC_CONVERTER_STATION,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.IS).value("lcc1").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(0, identifiables.size());
    }

    @Test
    void testHvdcLineFilter() {
        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY_1).operator(IN).values(new TreeSet<>(Set.of("FR"))).build());
        rules.add(nominalVoltageRuleExpert(DC_NOMINAL_VOLTAGE, 400., null));

        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter hvdcLineFilter = new ExpertFilter(UUID.randomUUID(), new Date(), EquipmentType.HVDC_LINE, parentRule);

        assertNotNull(hvdcLineFilter.getRules());
        assertFalse(hvdcLineFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.HVDC_LINE, hvdcLineFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(hvdcLineFilter, network2, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("L", identifiables.get(0).getId());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("L", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.HVDC_LINE,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network2, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("L", identifiables.get(0).getId());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.HVDC_LINE,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.IS).value("L").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network2, filterLoader);
        assertEquals(1, identifiables.size());  // expert filter for HVDC line is now implemented
        assertEquals("L", identifiables.get(0).getId());
        assertInstanceOf(HvdcLine.class, identifiables.get(0));
    }

    @Test
    void testIdentifierListFilter() {
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("GEN", 30.),
            new IdentifierListFilterEquipmentAttributes("notFound1", 50.),
            new IdentifierListFilterEquipmentAttributes("GEN2", 50.),
            new IdentifierListFilterEquipmentAttributes("notFound2", 5.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.GENERATOR,
            filterEquipmentAttributes);

        assertEquals(FilterType.IDENTIFIER_LIST, identifierListFilter.getType());
        assertEquals(50., identifierListFilter.getDistributionKey("GEN2"), 0.001);

        List<IdentifiableAttributes> identifiableAttributes = List.of(
            new IdentifiableAttributes("GEN", IdentifiableType.GENERATOR, 50.),
            new IdentifiableAttributes("GEN2", IdentifiableType.GENERATOR, 70.));

        FilterEquipments filterEquipments = identifierListFilter.toFilterEquipments(identifiableAttributes);
        assertTrue(CollectionUtils.isEqualCollection(filterEquipments.getNotFoundEquipments(), List.of("notFound1", "notFound2")));

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("GEN", identifiables.get(0).getId());
        assertEquals("GEN2", identifiables.get(1).getId());
    }

    @Test
    void testScriptFilter() {
        ScriptFilter scriptFilter = new ScriptFilter(
            UUID.randomUUID(),
            new Date(),
            "");
        assertEquals(FilterType.SCRIPT, scriptFilter.getType());
        assertNull(scriptFilter.getEquipmentType());
    }

    @Test
    void testFilterLoader() {
        // with identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("GEN", 30.),
            new IdentifierListFilterEquipmentAttributes("GEN2", 30.));

        UUID uuid1 = UUID.randomUUID();
        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            uuid1,
            new Date(),
            EquipmentType.GENERATOR,
            filterEquipmentAttributes);

        FilterLoader filterLoader1 = uuids -> List.of(identifierListFilter);

        List<FilterEquipments> filterEquipments = FilterServiceUtils.getFilterEquipmentsFromUuid(network, uuid1, filterLoader1);
        assertEquals(1, filterEquipments.size());
        assertEquals(uuid1, filterEquipments.get(0).getFilterId());
        assertEquals(0, filterEquipments.get(0).getNotFoundEquipments().size());
        assertEquals(2, filterEquipments.get(0).getIdentifiableAttributes().size());
        assertEquals("GEN", filterEquipments.get(0).getIdentifiableAttributes().get(0).getId());
        assertEquals("GEN2", filterEquipments.get(0).getIdentifiableAttributes().get(1).getId());

        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(EnumExpertRule.builder().field(COUNTRY).operator(IN).values(new TreeSet<>(Set.of("FR"))).build());
        rules.add(nominalVoltageRuleExpert(NOMINAL_VOLTAGE, 144., 176.));

        UUID uuid2 = UUID.randomUUID();
        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter loadFilter = new ExpertFilter(uuid2, new Date(), EquipmentType.LOAD, parentRule);

        assertNotNull(loadFilter.getRules());
        assertFalse(loadFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.LOAD, loadFilter.getEquipmentType());

        FilterLoader filterLoader2 = uuids -> List.of(loadFilter);

        filterEquipments = FilterServiceUtils.getFilterEquipmentsFromUuid(network, uuid2, filterLoader2);
        assertEquals(1, filterEquipments.size());
        assertEquals(uuid2, filterEquipments.get(0).getFilterId());
        assertEquals(1, filterEquipments.get(0).getIdentifiableAttributes().size());
        assertEquals("LOAD", filterEquipments.get(0).getIdentifiableAttributes().get(0).getId());
    }

    @Test
    void testEquipmentNameFilterNoMatch() {
        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(StringExpertRule.builder().field(NAME).operator(IS).value("unexisting name").build());
        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter vscConverterStationFilter = new ExpertFilter(UUID.randomUUID(), new Date(),
            EquipmentType.VSC_CONVERTER_STATION, parentRule);

        assertNotNull(vscConverterStationFilter.getRules());
        assertFalse(vscConverterStationFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.VSC_CONVERTER_STATION, vscConverterStationFilter.getEquipmentType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(vscConverterStationFilter, network2, filterLoader);
        assertEquals(0, identifiables.size());
    }

    @Test
    void testEquipmentNameFilterWithMatch() {
        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(StringExpertRule.builder().field(NAME).operator(IS).value("Converter1").build());
        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter vscConverterStationFilter = new ExpertFilter(UUID.randomUUID(), new Date(),
            EquipmentType.VSC_CONVERTER_STATION, parentRule);

        assertNotNull(vscConverterStationFilter.getRules());
        assertFalse(vscConverterStationFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.VSC_CONVERTER_STATION, vscConverterStationFilter.getEquipmentType());
        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(vscConverterStationFilter, network2, filterLoader);
        assertEquals(1, identifiables.size());
    }

    @Test
    void testEquipmentNameFilterWithNullValueInEquipments() {
        List<AbstractExpertRule> rules = new ArrayList<>();
        rules.add(StringExpertRule.builder().field(NAME).operator(IS).value("some name").build());
        AbstractExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();
        ExpertFilter voltageLevelFilter = new ExpertFilter(UUID.randomUUID(), new Date(),
            EquipmentType.VOLTAGE_LEVEL, parentRule);

        assertNotNull(voltageLevelFilter.getRules());
        assertFalse(voltageLevelFilter.getRules().getRules().isEmpty());
        assertEquals(EquipmentType.VOLTAGE_LEVEL, voltageLevelFilter.getEquipmentType());
        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(voltageLevelFilter, network, filterLoader);
        // in this network, VL equipments have null name => no match
        assertEquals(0, identifiables.size());
    }
}
