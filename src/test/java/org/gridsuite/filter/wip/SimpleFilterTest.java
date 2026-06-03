package org.gridsuite.filter.wip;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleFilterTest {

    @Test
    void testSimpleFilterReturnsExpectedFilterType() {
        SimpleFilter simpleFilter = new SimpleFilter(EquipmentType.LINE);
        assertThat(simpleFilter.getFilterType()).isEqualTo(FilterType.EXPERT);
    }

    @ParameterizedTest
    @EnumSource(EquipmentType.class)
    void testSimpleFilterEvaluationReturnsExpectedEquipments(EquipmentType equipmentType) {
        Network network = TestNetworkUtils.createTestNetwork();
        List<Identifiable<?>> expectedIdentifiable = getExpectedIdentifiable(network, equipmentType, TopologyKind.BUS_BREAKER);
        SimpleFilter simpleFilter = new SimpleFilter(equipmentType);

        List<Identifiable<?>> actualIdentifiable = simpleFilter.evaluate(network);

        assertThat(actualIdentifiable).isEqualTo(expectedIdentifiable);
    }

    @Test
    void testSimpleFilterEvaluationWithTopologyKindReturnsExpectedEquipments() {
        Network network = TestNetworkUtils.createTestNetwork();
        List<Identifiable<?>> expectedIdentifiable = getExpectedIdentifiable(network, EquipmentType.BUS, TopologyKind.NODE_BREAKER);
        SimpleFilter simpleFilter = new SimpleFilter(EquipmentType.BUS);

        List<Identifiable<?>> actualIdentifiable = simpleFilter.evaluate(network, TopologyKind.NODE_BREAKER);

        assertThat(actualIdentifiable).isEqualTo(expectedIdentifiable);
    }

    private List<Identifiable<?>> getExpectedIdentifiable(Network network, EquipmentType equipmentType, TopologyKind topologyKind) {
        return switch (equipmentType) {
            case LINE -> network.getLineStream().collect(Collectors.toUnmodifiableList());
            case BOUNDARY_LINE -> network.getBoundaryLineStream().collect(Collectors.toUnmodifiableList());
            case HVDC_LINE -> network.getHvdcLineStream().collect(Collectors.toUnmodifiableList());
            case SUBSTATION -> network.getSubstationStream().collect(Collectors.toUnmodifiableList());
            case VOLTAGE_LEVEL -> network.getVoltageLevelStream().collect(Collectors.toUnmodifiableList());
            case BUSBAR_SECTION -> network.getBusbarSectionStream().collect(Collectors.toUnmodifiableList());
            case BUS -> getBusStream(network, topologyKind);
            case GENERATOR -> network.getGeneratorStream().collect(Collectors.toUnmodifiableList());
            case BATTERY -> network.getBatteryStream().collect(Collectors.toUnmodifiableList());
            case LOAD -> network.getLoadStream().collect(Collectors.toUnmodifiableList());
            case SHUNT_COMPENSATOR -> network.getShuntCompensatorStream().collect(Collectors.toUnmodifiableList());
            case STATIC_VAR_COMPENSATOR -> network.getStaticVarCompensatorStream().collect(Collectors.toUnmodifiableList());
            case TWO_WINDINGS_TRANSFORMER -> network.getTwoWindingsTransformerStream().collect(Collectors.toUnmodifiableList());
            case THREE_WINDINGS_TRANSFORMER -> network.getThreeWindingsTransformerStream().collect(Collectors.toUnmodifiableList());
            case LCC_CONVERTER_STATION -> network.getLccConverterStationStream().collect(Collectors.toUnmodifiableList());
            case VSC_CONVERTER_STATION -> network.getVscConverterStationStream().collect(Collectors.toUnmodifiableList());
        };
    }

    private List<Identifiable<?>> getBusStream(Network network, TopologyKind topologyKind) {
        return network.getVoltageLevelStream()
                .filter(vl -> topologyKind == null || vl.getTopologyKind() == topologyKind)
                .map(VoltageLevel::getBusBreakerView)
                .flatMap(VoltageLevel.BusBreakerView::getBusStream)
                .collect(Collectors.toUnmodifiableList());
    }

    private static final class SimpleFilter extends AbstractFilter {

        private SimpleFilter(EquipmentType equipmentType) {
            super(equipmentType);
        }

        @Override
        public FilterType getFilterType() {
            return FilterType.EXPERT;
        }

        @Override
        protected boolean evaluateFilterRule(Identifiable<?> identifiable) {
            return true;
        }
    }
}
