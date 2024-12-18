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
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.CombinatorExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.StringExpertRule;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilterEquipmentAttributes;
import org.gridsuite.filter.scriptfilter.ScriptFilter;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

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

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("P1", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.SUBSTATION,
            filterEquipmentAttributes);
        assertEquals(FilterType.IDENTIFIER_LIST, identifierListFilter.getType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
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

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("VLGEN", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.VOLTAGE_LEVEL,
            filterEquipmentAttributes);

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
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

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("NHV1_NHV2_1", 30.),
            new IdentifierListFilterEquipmentAttributes("NHV1_NHV2_2", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.LINE,
            filterEquipmentAttributes);

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
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

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("NHV2_NLOAD", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.TWO_WINDINGS_TRANSFORMER,
            filterEquipmentAttributes);

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
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

    @Test
    void testThreeWindingsTransformerFilter() {

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.THREE_WINDINGS_TRANSFORMER,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(ID).operator(OperatorType.IS).value("3WT").build());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(expertFilter, network5, filterLoader);
        assertEquals(1, identifiables.size());
        assertInstanceOf(ThreeWindingsTransformer.class, identifiables.get(0));
        assertEquals("3WT", identifiables.get(0).getId());
    }

    @Test
    void testGeneratorFilter() {

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("GEN", 30.),
            new IdentifierListFilterEquipmentAttributes("GEN2", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.GENERATOR,
            filterEquipmentAttributes);

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
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

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("LOAD", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.LOAD,
            filterEquipmentAttributes);

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
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

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("battery1", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.BATTERY,
            filterEquipmentAttributes);

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
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

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("SHUNT", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.SHUNT_COMPENSATOR,
            filterEquipmentAttributes);

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network4, filterLoader);
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

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("SVC2", 30.),
            new IdentifierListFilterEquipmentAttributes("SVC3", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.STATIC_VAR_COMPENSATOR,
            filterEquipmentAttributes);

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network3, filterLoader);
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

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("danglineLine1", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.DANGLING_LINE,
            filterEquipmentAttributes);

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
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

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("busbarSection1", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.BUSBAR_SECTION,
            filterEquipmentAttributes);

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
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

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("lcc1", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.LCC_CONVERTER_STATION,
            filterEquipmentAttributes);

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network, filterLoader);
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

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("L", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.HVDC_LINE,
            filterEquipmentAttributes);

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network2, filterLoader);
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
