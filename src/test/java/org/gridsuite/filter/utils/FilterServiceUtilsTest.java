/*
  Copyright (c) 2026, RTE (http://www.rte-france.com)
  This Source Code Form is subject to the terms of the Mozilla Public
  License, v. 2.0. If a copy of the MPL was not distributed with this
  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.impl.NetworkFactoryImpl;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.CombinatorExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.EnumExpertRule;
import org.gridsuite.filter.identifierlistfilter.FilterAttributes;
import org.gridsuite.filter.identifierlistfilter.FilteredIdentifiables;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Kevin Le Saulnier <kevin.lesaulnier at rte-france.com>
 */
class FilterServiceUtilsTest {

    @Test
    void testEvaluateFilters() {
        UUID filterId = UUID.randomUUID();
        ArrayList<AbstractExpertRule> rules = new ArrayList<>();
        EnumExpertRule country1Filter = EnumExpertRule.builder().field(FieldType.COUNTRY_1).operator(OperatorType.IN)
            .values(new TreeSet<>(Set.of("FR"))).build();
        rules.add(country1Filter);
        CombinatorExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();

        ExpertFilter lineFilter = new ExpertFilter(filterId, new Date(), EquipmentType.LINE, parentRule);

        FilterAttributes filterAttributes = new FilterAttributes();
        filterAttributes.setId(filterId);
        FiltersWithEquipmentTypes filtersBody = new FiltersWithEquipmentTypes(List.of(filterAttributes), List.of());
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators(new NetworkFactoryImpl());
        FilteredIdentifiables result = FilterServiceUtils.evaluateFiltersWithEquipmentTypes(filtersBody, network, uuids -> List.of(lineFilter));
        List<IdentifiableAttributes> expected = new ArrayList<>();
        expected.add(new IdentifiableAttributes("NHV1_NHV2_1", IdentifiableType.LINE, null));
        expected.add(new IdentifiableAttributes("NHV1_NHV2_2", IdentifiableType.LINE, null));
        assertTrue(expected.size() == result.equipmentIds().size()
            && result.equipmentIds().containsAll(expected)
            && expected.containsAll(result.equipmentIds()));

    }

    @Test
    void testEvaluateFiltersWithEquipmentTypes() {
        // Create a SUBSTATION expert filter selecting substations NHV1 and NGEN
        UUID filterId = UUID.randomUUID();
        FilterAttributes filterAttributes = new FilterAttributes();
        filterAttributes.setId(filterId);

        ArrayList<AbstractExpertRule> rules = new ArrayList<>();
        EnumExpertRule countryFilter = EnumExpertRule.builder()
            .field(FieldType.COUNTRY)
            .operator(OperatorType.IN)
            .values(new TreeSet<>(Set.of("FR")))
            .build();
        rules.add(countryFilter);
        CombinatorExpertRule parentRule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(rules).build();

        ExpertFilter substationFilter = new ExpertFilter(filterId, new Date(), EquipmentType.SUBSTATION, parentRule);

        // Ask for sub-equipments: LINE and GENERATOR for this filter
        EquipmentTypesByFilterId equipmentTypesByFilterId = new EquipmentTypesByFilterId(filterId, Set.of(IdentifiableType.LINE, IdentifiableType.GENERATOR));
        FiltersWithEquipmentTypes filtersBody = new FiltersWithEquipmentTypes(List.of(filterAttributes), List.of(equipmentTypesByFilterId));
        Network network = EurostagTutorialExample1Factory.createWithMoreGenerators(new NetworkFactoryImpl());
        FilteredIdentifiables result = FilterServiceUtils.evaluateFiltersWithEquipmentTypes(filtersBody, network, uuids -> List.of(substationFilter));

        List<IdentifiableAttributes> expected = new ArrayList<>();
        // Lines connected to NHV1 substation in the sample network
        expected.add(new IdentifiableAttributes("NHV1_NHV2_1", IdentifiableType.LINE, null));
        expected.add(new IdentifiableAttributes("NHV1_NHV2_2", IdentifiableType.LINE, null));
        // Generators in substation NGEN (GEN and GEN2 exist on initial variant)
        expected.add(new IdentifiableAttributes("GEN", IdentifiableType.GENERATOR, null));
        expected.add(new IdentifiableAttributes("GEN2", IdentifiableType.GENERATOR, null));

        assertTrue(result.equipmentIds().containsAll(expected));
    }
}
