package org.gridsuite.filter.wip;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import lombok.Getter;
import org.gridsuite.filter.wip.data.EquipmentType;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Getter
public abstract class AbstractFilter implements Filter {

    protected final EquipmentType equipmentType;

    protected AbstractFilter(EquipmentType equipmentType) {
        this.equipmentType = equipmentType;
    }

    public List<Identifiable<?>> evaluate(Network network) {
        return getConsideredEquipmentStream(network)
                .filter(this::evaluateFilterRule)
                .toList();
    }

    private Stream<Identifiable<?>> getConsideredEquipmentStream(Network network) {
        return switch (equipmentType) {
            case LINE -> network.getLineStream().map(line -> line);
            case GENERATOR -> network.getGeneratorStream().map(generator -> generator);
            case LOAD -> network.getLoadStream().map(load -> load);
            case SHUNT_COMPENSATOR -> network.getShuntCompensatorStream().map(sc -> sc);
            case STATIC_VAR_COMPENSATOR -> network.getStaticVarCompensatorStream().map(svc -> svc);
            case BATTERY -> network.getBatteryStream().map(battery -> battery);
            case BUS -> getBusStream(network);
            case BUSBAR_SECTION -> network.getBusbarSectionStream().map(busbarSection -> busbarSection);
            case BOUNDARY_LINE -> network.getBoundaryLineStream().map(boundaryLine -> boundaryLine);
            case LCC_CONVERTER_STATION -> network.getLccConverterStationStream().map(lccConverterStation -> lccConverterStation);
            case VSC_CONVERTER_STATION -> network.getVscConverterStationStream().map(vscConverterStation -> vscConverterStation);
            case TWO_WINDINGS_TRANSFORMER -> network.getTwoWindingsTransformerStream().map(twoWindingsTransformer -> twoWindingsTransformer);
            case THREE_WINDINGS_TRANSFORMER -> network.getThreeWindingsTransformerStream().map(threeWindingsTransformer -> threeWindingsTransformer);
            case HVDC_LINE -> network.getHvdcLineStream().map(hvdcLine -> hvdcLine);
            case SUBSTATION -> network.getSubstationStream().map(substation -> substation);
            case VOLTAGE_LEVEL -> network.getVoltageLevelStream().map(voltageLevel -> voltageLevel);
        };
    }

    protected abstract boolean evaluateFilterRule(Identifiable<?> identifiable);

    protected abstract Stream<Identifiable<?>> getBusStream(Network network);
}
