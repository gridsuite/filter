/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.apache.commons.collections4.CollectionUtils;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.ExpertFilter;
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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.gridsuite.filter.utils.expertfilter.FieldType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
class FiltersUtilsTest {
    private Network network;
    private Network network2;

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
        filterLoader = uuids -> null;
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
}
