/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import com.powsybl.iidm.network.test.ShuntTestCaseFactory;
import com.powsybl.iidm.network.test.SvcTestCaseFactory;
import com.powsybl.iidm.network.test.ThreeWindingsTransformerNetworkFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.criteriafilter.BatteryFilter;
import org.gridsuite.filter.criteriafilter.BusBarSectionFilter;
import org.gridsuite.filter.criteriafilter.CriteriaFilter;
import org.gridsuite.filter.criteriafilter.DanglingLineFilter;
import org.gridsuite.filter.criteriafilter.GeneratorFilter;
import org.gridsuite.filter.criteriafilter.HvdcLineFilter;
import org.gridsuite.filter.criteriafilter.LccConverterStationFilter;
import org.gridsuite.filter.criteriafilter.LineFilter;
import org.gridsuite.filter.criteriafilter.LoadFilter;
import org.gridsuite.filter.criteriafilter.NumericalFilter;
import org.gridsuite.filter.criteriafilter.ShuntCompensatorFilter;
import org.gridsuite.filter.criteriafilter.StaticVarCompensatorFilter;
import org.gridsuite.filter.criteriafilter.SubstationFilter;
import org.gridsuite.filter.criteriafilter.ThreeWindingsTransformerFilter;
import org.gridsuite.filter.criteriafilter.TwoWindingsTransformerFilter;
import org.gridsuite.filter.criteriafilter.VoltageLevelFilter;
import org.gridsuite.filter.criteriafilter.VscConverterStationFilter;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.StringExpertRule;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilterEquipmentAttributes;
import org.gridsuite.filter.scriptfilter.ScriptFilter;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterServiceUtils;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.utils.FiltersUtils;
import org.gridsuite.filter.utils.RangeType;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    public void setUp() {
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
        // criteria filter
        SubstationFilter substationFilter = SubstationFilter.builder()
            .countries(new TreeSet<>(Set.of("FR", "IT")))
            .freeProperties(Map.of("region", List.of("north")))
            .build();

        assertFalse(substationFilter.isEmpty());
        assertEquals(EquipmentType.SUBSTATION, substationFilter.getEquipmentType());

        CriteriaFilter substationCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            substationFilter
        );
        assertEquals(FilterType.CRITERIA, substationCriteriaFilter.getType());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(substationCriteriaFilter, network, filterLoader);
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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.IS).value("P1").build());
        assertEquals(FilterType.EXPERT, expertFilter.getType());
        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("P1", identifiables.get(0).getId());
    }

    @Test
    void testVoltageLevelFilter() {
        // criteria filter
        VoltageLevelFilter voltageLevelFilter = VoltageLevelFilter.builder()
            .countries(new TreeSet<>(Set.of("FR", "IT")))
            .nominalVoltage(new NumericalFilter(RangeType.RANGE, 15., 30.))
            .build();

        assertFalse(voltageLevelFilter.isEmpty());
        assertEquals(EquipmentType.VOLTAGE_LEVEL, voltageLevelFilter.getEquipmentType());

        CriteriaFilter voltageLevelCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            voltageLevelFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(voltageLevelCriteriaFilter, network, filterLoader);
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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.IS).value("VLGEN").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("VLGEN", identifiables.get(0).getId());
    }

    @Test
    void testLineFilter() {
        // criteria filter
        LineFilter lineFilter = LineFilter.builder()
            .substationName1("P1")
            .substationName2("P2")
            .countries1(new TreeSet<>(Set.of("FR")))
            .countries2(new TreeSet<>(Set.of("FR")))
            .freeProperties1(Map.of("region", List.of("south")))
            .freeProperties2(Map.of("region", List.of("north")))
            .freeProperties(Map.of("region", List.of("south")))
            .nominalVoltage1(new NumericalFilter(RangeType.RANGE, 360., 400.))
            .nominalVoltage2(new NumericalFilter(RangeType.RANGE, 356.25, 393.75))
            .build();

        assertFalse(lineFilter.isEmpty());
        assertEquals(EquipmentType.LINE, lineFilter.getEquipmentType());

        CriteriaFilter lineCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            lineFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(lineCriteriaFilter, network, filterLoader);
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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.IS).value("NHV1_NHV2_2").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("NHV1_NHV2_2", identifiables.get(0).getId());
    }

    @Test
    void testTwoWindingsTransformerFilter() {
        // criteria filter
        TwoWindingsTransformerFilter twoWindingsTransformerFilter = TwoWindingsTransformerFilter.builder()
            .substationName("P2")
            .countries(new TreeSet<>(Set.of("FR")))
            .freeProperties(Map.of("region", List.of("south")))
            .nominalVoltage1(new NumericalFilter(RangeType.EQUALITY, 380., null))
            .nominalVoltage2(new NumericalFilter(RangeType.RANGE, 142.4, 157.5))
            .build();

        assertFalse(twoWindingsTransformerFilter.isEmpty());
        assertEquals(EquipmentType.TWO_WINDINGS_TRANSFORMER, twoWindingsTransformerFilter.getEquipmentType());

        CriteriaFilter twoWindingsTransformerCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            twoWindingsTransformerFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(twoWindingsTransformerCriteriaFilter, network, filterLoader);
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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.IS).value("NHV2_NLOAD").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("NHV2_NLOAD", identifiables.get(0).getId());
    }

    @Test
    void testThreeWindingsTransformerFilter() {
        // criteria filter
        ThreeWindingsTransformerFilter threeWindingsTransformerFilter = ThreeWindingsTransformerFilter.builder()
            .substationName("SUBSTATION")
            .countries(new TreeSet<>(Set.of("FR")))
            .nominalVoltage1(new NumericalFilter(RangeType.RANGE, 130., 140.))
            .nominalVoltage2(new NumericalFilter(RangeType.RANGE, 30., 40.))
            .nominalVoltage3(new NumericalFilter(RangeType.EQUALITY, 11., null))
            .build();

        assertFalse(threeWindingsTransformerFilter.isEmpty());
        assertEquals(EquipmentType.THREE_WINDINGS_TRANSFORMER, threeWindingsTransformerFilter.getEquipmentType());

        CriteriaFilter threeWindingsTransformerCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            threeWindingsTransformerFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(threeWindingsTransformerCriteriaFilter, network5, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("3WT", identifiables.get(0).getId());

        threeWindingsTransformerFilter.setNominalVoltage1(new NumericalFilter(RangeType.RANGE, 30., 40.));
        threeWindingsTransformerFilter.setNominalVoltage2(new NumericalFilter(RangeType.EQUALITY, 11., null));
        threeWindingsTransformerFilter.setNominalVoltage3(new NumericalFilter(RangeType.RANGE, 130., 140.));
        identifiables = FiltersUtils.getIdentifiables(threeWindingsTransformerCriteriaFilter, network5, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("3WT", identifiables.get(0).getId());

        threeWindingsTransformerFilter.setNominalVoltage1(new NumericalFilter(RangeType.EQUALITY, 11., null));
        threeWindingsTransformerFilter.setNominalVoltage2(new NumericalFilter(RangeType.RANGE, 130., 140.));
        threeWindingsTransformerFilter.setNominalVoltage3(new NumericalFilter(RangeType.RANGE, 30., 40.));
        identifiables = FiltersUtils.getIdentifiables(threeWindingsTransformerCriteriaFilter, network5, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("3WT", identifiables.get(0).getId());

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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.IS).value("3WT").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network5, filterLoader);
        assertEquals(0, identifiables.size());  // expert filter for 3WT not yet implemented
    }

    @Test
    void testGeneratorFilter() {
        // criteria filter
        GeneratorFilter generatorFilter = new GeneratorFilter(null, null, "P1", new TreeSet<>(Set.of("FR", "IT")),
            null, null, new NumericalFilter(RangeType.RANGE, 15., 30.), null);

        assertFalse(generatorFilter.isEmpty());
        assertEquals(EquipmentType.GENERATOR, generatorFilter.getEquipmentType());

        CriteriaFilter generatorCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            generatorFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(generatorCriteriaFilter, network, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("GEN", identifiables.get(0).getId());
        assertEquals("GEN2", identifiables.get(1).getId());

        generatorFilter.setNominalVoltage(new NumericalFilter(RangeType.GREATER_THAN, 50., null));
        identifiables = FiltersUtils.getIdentifiables(generatorCriteriaFilter, network, filterLoader);
        assertEquals(0, identifiables.size());

        generatorFilter.setNominalVoltage(new NumericalFilter(RangeType.GREATER_OR_EQUAL, 10., null));
        identifiables = FiltersUtils.getIdentifiables(generatorCriteriaFilter, network, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("GEN", identifiables.get(0).getId());
        assertEquals("GEN2", identifiables.get(1).getId());

        generatorFilter.setNominalVoltage(new NumericalFilter(RangeType.LESS_THAN, 12., null));
        identifiables = FiltersUtils.getIdentifiables(generatorCriteriaFilter, network, filterLoader);
        assertEquals(0, identifiables.size());

        generatorFilter.setNominalVoltage(new NumericalFilter(RangeType.LESS_OR_EQUAL, 40., null));
        identifiables = FiltersUtils.getIdentifiables(generatorCriteriaFilter, network, filterLoader);
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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.BEGINS_WITH).value("GEN").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("GEN", identifiables.get(0).getId());
        assertEquals("GEN2", identifiables.get(1).getId());
    }

    @Test
    void testLoadFilter() {
        // criteria filter
        LoadFilter loadFilter = LoadFilter.builder()
            .substationName("P2")
            .countries(new TreeSet<>(Set.of("FR")))
            .nominalVoltage(new NumericalFilter(RangeType.RANGE, 144., 176.))
            .build();

        assertFalse(loadFilter.isEmpty());
        assertEquals(EquipmentType.LOAD, loadFilter.getEquipmentType());

        CriteriaFilter loadCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            loadFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(loadCriteriaFilter, network, filterLoader);
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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.IS).value("LOAD").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("LOAD", identifiables.get(0).getId());
    }

    @Test
    void testBatteryFilter() {
        // criteria filter
        BatteryFilter batteryFilter = BatteryFilter.builder()
            .substationName("P2")
            .countries(new TreeSet<>(Set.of("FR")))
            .nominalVoltage(new NumericalFilter(RangeType.RANGE, 144., 176.))
            .build();

        assertFalse(batteryFilter.isEmpty());
        assertEquals(EquipmentType.BATTERY, batteryFilter.getEquipmentType());

        CriteriaFilter batteryCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            batteryFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(batteryCriteriaFilter, network, filterLoader);
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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.IS).value("battery1").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(0, identifiables.size());
    }

    @Test
    void testShuntCompensatorFilter() {
        // criteria filter
        ShuntCompensatorFilter shuntCompensatorFilter = ShuntCompensatorFilter.builder()
            .substationName("S1")
            .countries(new TreeSet<>(Set.of("FR")))
            .nominalVoltage(new NumericalFilter(RangeType.EQUALITY, 380., null))
            .build();

        assertFalse(shuntCompensatorFilter.isEmpty());
        assertEquals(EquipmentType.SHUNT_COMPENSATOR, shuntCompensatorFilter.getEquipmentType());

        CriteriaFilter shuntCompensatorCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            shuntCompensatorFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(shuntCompensatorCriteriaFilter, network4, filterLoader);
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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.IS).value("SHUNT").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network4, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("SHUNT", identifiables.get(0).getId());
    }

    @Test
    void testStaticVarCompensatorFilter() {
        // criteria filter
        StaticVarCompensatorFilter staticVarCompensatorFilter = StaticVarCompensatorFilter.builder()
            .substationName("S2")
            .countries(new TreeSet<>(Set.of("FR")))
            .nominalVoltage(new NumericalFilter(RangeType.EQUALITY, 380., null))
            .build();

        assertFalse(staticVarCompensatorFilter.isEmpty());
        assertEquals(EquipmentType.STATIC_VAR_COMPENSATOR, staticVarCompensatorFilter.getEquipmentType());

        CriteriaFilter staticVarCompensatorCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            staticVarCompensatorFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(staticVarCompensatorCriteriaFilter, network3, filterLoader);
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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.BEGINS_WITH).value("SVC").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network3, filterLoader);
        assertEquals(2, identifiables.size());
        assertEquals("SVC2", identifiables.get(0).getId());
        assertEquals("SVC3", identifiables.get(1).getId());
    }

    @Test
    void testDanglingLineFilter() {
        // criteria filter
        DanglingLineFilter danglingLineFilter = DanglingLineFilter.builder()
            .substationName("S2")
            .countries(new TreeSet<>(Set.of("FR")))
            .nominalVoltage(new NumericalFilter(RangeType.EQUALITY, 380., null))
            .build();

        assertFalse(danglingLineFilter.isEmpty());
        assertEquals(EquipmentType.DANGLING_LINE, danglingLineFilter.getEquipmentType());

        CriteriaFilter danglingLineCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            danglingLineFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(danglingLineCriteriaFilter, network, filterLoader);
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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.BEGINS_WITH).value("danglineLine1").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(0, identifiables.size());
    }

    @Test
    void testBusbarSectionFilter() {
        // criteria filter
        BusBarSectionFilter busbarSectionFilter = BusBarSectionFilter.builder()
            .substationName("S1")
            .countries(new TreeSet<>(Set.of("FR")))
            .nominalVoltage(new NumericalFilter(RangeType.EQUALITY, 380., null))
            .build();

        assertFalse(busbarSectionFilter.isEmpty());
        assertEquals(EquipmentType.BUSBAR_SECTION, busbarSectionFilter.getEquipmentType());

        CriteriaFilter busbarSectionCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            busbarSectionFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(busbarSectionCriteriaFilter, network, filterLoader);
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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.BEGINS_WITH).value("busbarSection1").build());

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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.IS).value("NGEN").build());

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("NGEN", identifiables.get(0).getId());
    }

    @Test
    void testLccConverterStationFilter() {
        // criteria filter
        LccConverterStationFilter lccConverterStationFilter = LccConverterStationFilter.builder()
            .substationName("S1")
            .countries(new TreeSet<>(Set.of("FR")))
            .nominalVoltage(new NumericalFilter(RangeType.EQUALITY, 380., null))
            .build();

        assertFalse(lccConverterStationFilter.isEmpty());
        assertEquals(EquipmentType.LCC_CONVERTER_STATION, lccConverterStationFilter.getEquipmentType());

        CriteriaFilter lccConverterStationCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            lccConverterStationFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(lccConverterStationCriteriaFilter, network2, filterLoader);
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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.IS).value("lcc1").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network, filterLoader);
        assertEquals(0, identifiables.size());
    }

    @Test
    void testVscConverterStationFilter() {
        // criteria filter
        VscConverterStationFilter vscConverterStationFilter = VscConverterStationFilter.builder()
            .substationName("S1")
            .countries(new TreeSet<>(Set.of("FR")))
            .nominalVoltage(new NumericalFilter(RangeType.EQUALITY, 400., null))
            .build();

        assertFalse(vscConverterStationFilter.isEmpty());
        assertEquals(EquipmentType.VSC_CONVERTER_STATION, vscConverterStationFilter.getEquipmentType());

        CriteriaFilter vscConverterStationCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            vscConverterStationFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(vscConverterStationCriteriaFilter, network2, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("C1", identifiables.get(0).getId());

        // identifier list filter
        List<IdentifierListFilterEquipmentAttributes> filterEquipmentAttributes = List.of(
            new IdentifierListFilterEquipmentAttributes("C1", 30.));

        IdentifierListFilter identifierListFilter = new IdentifierListFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.VSC_CONVERTER_STATION,
            filterEquipmentAttributes);

        identifiables = FiltersUtils.getIdentifiables(identifierListFilter, network2, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("C1", identifiables.get(0).getId());

        // expert filter
        ExpertFilter expertFilter = new ExpertFilter(
            UUID.randomUUID(),
            new Date(),
            EquipmentType.VSC_CONVERTER_STATION,
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.IS).value("C1").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network2, filterLoader);
        assertEquals(1, identifiables.size());
        assertEquals("C1", identifiables.get(0).getId());
    }

    @Test
    void testHvdcLineFilter() {
        // criteria filter
        HvdcLineFilter hvdcLineFilter = HvdcLineFilter.builder()
            .substationName1("S1")
            .substationName2("S2")
            .countries1(new TreeSet<>(Set.of("FR", "BE")))
            .countries2(new TreeSet<>(Set.of("FR", "IT")))
            .nominalVoltage(new NumericalFilter(RangeType.RANGE, 380., 420.))
            .build();

        assertFalse(hvdcLineFilter.isEmpty());
        assertEquals(EquipmentType.HVDC_LINE, hvdcLineFilter.getEquipmentType());

        CriteriaFilter hvdcLineCriteriaFilter = new CriteriaFilter(
            UUID.randomUUID(),
            new Date(),
            hvdcLineFilter
        );

        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(hvdcLineCriteriaFilter, network2, filterLoader);
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
            StringExpertRule.builder().combinator(CombinatorType.AND).field(FieldType.ID).operator(OperatorType.IS).value("L").build());

        identifiables = FiltersUtils.getIdentifiables(expertFilter, network2, filterLoader);
        assertEquals(0, identifiables.size());  // expert filter for HVDC line not yet implemented
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

        // with criteria filter
        LoadFilter loadFilter = LoadFilter.builder()
            .substationName("P2")
            .countries(new TreeSet<>(Set.of("FR")))
            .nominalVoltage(new NumericalFilter(RangeType.RANGE, 144., 176.))
            .build();
        UUID uuid2 = UUID.randomUUID();
        CriteriaFilter loadCriteriaFilter = new CriteriaFilter(
            uuid2,
            new Date(),
            loadFilter
        );

        FilterLoader filterLoader2 = uuids -> List.of(loadCriteriaFilter);

        filterEquipments = FilterServiceUtils.getFilterEquipmentsFromUuid(network, uuid2, filterLoader2);
        assertEquals(1, filterEquipments.size());
        assertEquals(uuid2, filterEquipments.get(0).getFilterId());
        assertEquals(1, filterEquipments.get(0).getIdentifiableAttributes().size());
        assertEquals("LOAD", filterEquipments.get(0).getIdentifiableAttributes().get(0).getId());
    }

    @Test
    void testEquipmentNameFilterNoMatch() {
        // criteria filter (in this network, vsc converter equipments have a name)
        VscConverterStationFilter vscConverterStationFilter = VscConverterStationFilter.builder()
                .equipmentName("unexisting name")
                .build();
        assertFalse(vscConverterStationFilter.isEmpty());
        assertEquals(EquipmentType.VSC_CONVERTER_STATION, vscConverterStationFilter.getEquipmentType());
        CriteriaFilter vscConverterStationCriteriaFilter = new CriteriaFilter(
                UUID.randomUUID(),
                new Date(),
                vscConverterStationFilter
        );
        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(vscConverterStationCriteriaFilter, network2, filterLoader);
        assertEquals(0, identifiables.size());
    }

    @Test
    void testEquipmentNameFilterWithMatch() {
        // criteria filter (in this network, vsc converter equipments have a name)
        VscConverterStationFilter vscConverterStationFilter = VscConverterStationFilter.builder()
                .equipmentName("Converter1")
                .build();
        assertFalse(vscConverterStationFilter.isEmpty());
        assertEquals(EquipmentType.VSC_CONVERTER_STATION, vscConverterStationFilter.getEquipmentType());
        CriteriaFilter vscConverterStationCriteriaFilter = new CriteriaFilter(
                UUID.randomUUID(),
                new Date(),
                vscConverterStationFilter
        );
        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(vscConverterStationCriteriaFilter, network2, filterLoader);
        assertEquals(1, identifiables.size());
    }

    @Test
    void testEquipmentNameFilterWithNullValueInEquipments() {
        // criteria filter
        VoltageLevelFilter voltageLevelFilter = VoltageLevelFilter.builder()
                .equipmentName("some name")
                .build();
        assertFalse(voltageLevelFilter.isEmpty());
        assertEquals(EquipmentType.VOLTAGE_LEVEL, voltageLevelFilter.getEquipmentType());
        CriteriaFilter voltageLevelCriteriaFilter = new CriteriaFilter(
                UUID.randomUUID(),
                new Date(),
                voltageLevelFilter
        );
        List<Identifiable<?>> identifiables = FiltersUtils.getIdentifiables(voltageLevelCriteriaFilter, network, filterLoader);
        // in this network, VL equipments have null name => no match
        assertEquals(0, identifiables.size());
    }
}
