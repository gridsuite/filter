/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.NetworkFactoryImpl;
import org.gridsuite.filter.utils.EquipmentType;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.fail;

public final class TestNetworkUtils {

    private static final Network NETWORK = createTestNetwork();

    private TestNetworkUtils() {
    }

    public static Identifiable<?> getEquipmentFromTestNetwork(EquipmentType equipmentType, String equipmentId) {
        return switch (equipmentType) {
            case VOLTAGE_LEVEL -> NETWORK.getVoltageLevel(equipmentId);
            case LINE -> NETWORK.getLine(equipmentId);
            case GENERATOR -> NETWORK.getGenerator(equipmentId);
            case LOAD -> NETWORK.getLoad(equipmentId);
            case SHUNT_COMPENSATOR -> NETWORK.getShuntCompensator(equipmentId);
            case BUSBAR_SECTION -> NETWORK.getBusbarSection(equipmentId);
            case BATTERY -> NETWORK.getBattery(equipmentId);
            case TWO_WINDINGS_TRANSFORMER -> NETWORK.getTwoWindingsTransformer(equipmentId);
            case STATIC_VAR_COMPENSATOR -> NETWORK.getStaticVarCompensator(equipmentId);
            case BOUNDARY_LINE -> NETWORK.getBoundaryLine(equipmentId);
            case THREE_WINDINGS_TRANSFORMER -> NETWORK.getThreeWindingsTransformer(equipmentId);
            case HVDC_LINE -> NETWORK.getHvdcLine(equipmentId);
            default -> fail("Unsupported equipment type: " + equipmentType);
        };
    }

    public static Network createTestNetwork() {
        Network network = new NetworkFactoryImpl().createNetwork(UUID.randomUUID().toString(), "test");

        // ===== Substations (3) =====
        Substation s1 = network.newSubstation()
                .setId("SUBSTATION_1").setCountry(Country.FR).add();
        Substation s2 = network.newSubstation()
                .setId("SUBSTATION_2").setCountry(Country.DE).add();
        Substation s3 = network.newSubstation()
                .setId("SUBSTATION_3").setCountry(Country.ES).add();
        s3.setProperty("myCustomProperty", "My custom value");

        // ===== Voltage Levels =====
        // 3 voltage levels in S1 (NODE_BREAKER) with different nominal voltages
        // so that a Three Windings Transformer can connect them.
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("VOLTAGE_LEVEL_1").setNominalV(400.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl1.setProperty("myCustomProperty", "My custom value");
        vl1.setName("Best voltage level ;)");
        VoltageLevel vl2 = s1.newVoltageLevel()
                .setId("VOLTAGE_LEVEL_2").setNominalV(225.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER).add();
        vl2.setName("");
        VoltageLevel vl3 = s1.newVoltageLevel()
                .setId("VOLTAGE_LEVEL_3").setNominalV(90.0)
                .setTopologyKind(TopologyKind.NODE_BREAKER).add();

        // Extra BUS_BREAKER voltage levels to host the remote ends of lines and HVDC links.
        VoltageLevel vlS2 = s2.newVoltageLevel()
                .setId("VL_S2").setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vlS2.getBusBreakerView().newBus().setId("BUS_S2").add();
        VoltageLevel vlS3 = network.getSubstation("SUBSTATION_3").newVoltageLevel()
                .setId("VL_S3").setName("other vl...")
                .setNominalV(400.0)
                .setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vlS3.getBusBreakerView().newBus().setId("BUS_S3").add();

        // ===== Busbar Sections (3) - at node 0 of each WANTED voltage level =====
        vl1.getNodeBreakerView().newBusbarSection()
                .setId("BUSBAR_SECTION_1").setNode(0).add();
        vl2.getNodeBreakerView().newBusbarSection()
                .setId("BUSBAR_SECTION_2").setNode(0).add();
        vl3.getNodeBreakerView().newBusbarSection()
                .setId("BUSBAR_SECTION_3").setNode(0).add();

        AtomicInteger nodeCounter1 = new AtomicInteger();
        AtomicInteger nodeCounter2 = new AtomicInteger();
        AtomicInteger nodeCounter3 = new AtomicInteger();

        // ===== Loads (3) =====
        for (int i = 1; i <= 3; i++) {
            vl1.newLoad()
                    .setId("LOAD_" + i)
                    .setNode(connect(vl1, nodeCounter1))
                    .setP0(10.0 * i).setQ0(3.0 * i)
                    .add();
        }

        // ===== Generators (3) =====
        for (int i = 1; i <= 3; i++) {
            vl1.newGenerator()
                    .setId("GENERATOR_" + i)
                    .setNode(connect(vl1, nodeCounter1))
                    .setEnergySource(EnergySource.NUCLEAR)
                    .setMinP(0.0).setMaxP(1000.0)
                    .setTargetP(100.0 * i).setTargetV(400.0)
                    .setTargetQ(i == 3 ? 100.0 : Double.NaN)
                    .setVoltageRegulatorOn(true)
                    .add();
        }

        // ===== Batteries (3) =====
        for (int i = 1; i <= 3; i++) {
            vl1.newBattery()
                    .setId("BATTERY_" + i)
                    .setNode(connect(vl1, nodeCounter1))
                    .setMinP(-100.0).setMaxP(100.0)
                    .setTargetP(10.0 * i).setTargetQ(5.0)
                    .add();
        }

        // ===== Shunt Compensators (3) =====
        for (int i = 1; i <= 3; i++) {
            vl1.newShuntCompensator()
                    .setId("SHUNT_COMPENSATOR_" + i)
                    .setNode(connect(vl1, nodeCounter1))
                    .setSectionCount(1)
                    .newLinearModel()
                    .setBPerSection(1e-5)
                    .setMaximumSectionCount(1)
                    .add()
                    .add();
        }

        // ===== Static Var Compensators (3) =====
        for (int i = 1; i <= 3; i++) {
            vl1.newStaticVarCompensator()
                    .setId("STATIC_VAR_COMPENSATOR_" + i)
                    .setNode(connect(vl1, nodeCounter1))
                    .setBmin(0.0002).setBmax(0.0008)
                    .setRegulationMode(StaticVarCompensator.RegulationMode.VOLTAGE)
                    .setRegulating(true)
                    .setVoltageSetpoint(400.0)
                    .add();
        }

        // ===== LCC Converter Stations (3 + 3 partners for HVDC lines) =====
        for (int i = 1; i <= 3; i++) {
            vl1.newLccConverterStation()
                    .setId("LCC_CONVERTER_STATION_" + i)
                    .setNode(connect(vl1, nodeCounter1))
                    .setLossFactor(1.1f).setPowerFactor(0.5f)
                    .add();
            vlS2.newLccConverterStation()
                    .setId("LCC_PARTNER_" + i)
                    .setConnectableBus("BUS_S2").setBus("BUS_S2")
                    .setLossFactor(1.1f).setPowerFactor(0.5f)
                    .add();
        }

        // ===== VSC Converter Stations (3) =====
        for (int i = 1; i <= 3; i++) {
            vl1.newVscConverterStation()
                    .setId("VSC_CONVERTER_STATION_" + i)
                    .setVoltageRegulatorOn(true)
                    .setNode(connect(vl1, nodeCounter1))
                    .setLossFactor(1.1f)
                    .setVoltageSetpoint(400.0)
                    .add();
        }

        // ===== Two Windings Transformers (3) - in S1 between vl1 (400 kV) and vl2 (225 kV) =====
        for (int i = 1; i <= 3; i++) {
            s1.newTwoWindingsTransformer()
                    .setId("TWO_WINDINGS_TRANSFORMER_" + i)
                    .setVoltageLevel1(vl1.getId()).setNode1(connect(vl1, nodeCounter1))
                    .setVoltageLevel2(vl2.getId()).setNode2(connect(vl2, nodeCounter2))
                    .setR(1.0).setX(10.0).setG(0.0).setB(0.0)
                    .setRatedU1(400.0).setRatedU2(225.0)
                    .add()
                    .newRatioTapChanger()
                    .setTapPosition(0)
                    .setLoadTapChangingCapabilities(true)
                    .beginStep()
                    .setR(1.0).setX(10.0).setG(0.0).setB(0.0).setRho(1.0)
                    .endStep()
                    .add();
        }

        // ===== Three Windings Transformers (3) - in S1 across vl1/vl2/vl3 =====
        for (int i = 1; i <= 3; i++) {
            s1.newThreeWindingsTransformer()
                    .setId("THREE_WINDINGS_TRANSFORMER_" + i)
                    .setRatedU0(400.0)
                    .newLeg1()
                    .setR(1.0).setX(10.0).setG(0.0).setB(0.0).setRatedU(400.0)
                    .setVoltageLevel(vl1.getId()).setNode(connect(vl1, nodeCounter1))
                    .add()
                    .newLeg2()
                    .setR(1.0).setX(10.0).setG(0.0).setB(0.0).setRatedU(225.0)
                    .setVoltageLevel(vl2.getId()).setNode(connect(vl2, nodeCounter2))
                    .add()
                    .newLeg3()
                    .setR(1.0).setX(10.0).setG(0.0).setB(0.0).setRatedU(90.0)
                    .setVoltageLevel(vl3.getId()).setNode(connect(vl3, nodeCounter3))
                    .add()
                    .add();
            network.getThreeWindingsTransformer("THREE_WINDINGS_TRANSFORMER_" + i).getLeg2()
                    .newRatioTapChanger()
                    .setRegulationMode(RatioTapChanger.RegulationMode.REACTIVE_POWER)
                    .setTapPosition(0)
                    .setLoadTapChangingCapabilities(true)
                    .beginStep()
                    .setR(1.0).setX(10.0).setG(0.0).setB(0.0).setRho(1.0)
                    .endStep()
                    .add();
            network.getThreeWindingsTransformer("THREE_WINDINGS_TRANSFORMER_" + i).getLeg3().newRatioTapChanger()
                    .setTapPosition(0)
                    .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                    .setLoadTapChangingCapabilities(true)
                    .beginStep()
                    .setR(1.0).setX(10.0).setG(0.0).setB(0.0).setRho(1.0)
                    .endStep()
                    .add();
        }
        network.getThreeWindingsTransformer("THREE_WINDINGS_TRANSFORMER_2").getLeg1()
                .newRatioTapChanger()
                .setRegulationMode(RatioTapChanger.RegulationMode.VOLTAGE)
                .setTapPosition(0)
                .setLoadTapChangingCapabilities(true)
                .setTargetV(400.0)
                .beginStep()
                .setR(1.0).setX(10.0).setG(0.0).setB(0.0).setRho(1.0)
                .endStep()
                .add();

        // ===== Lines (3) - between voltage levels of nominal V = 400 kV =====
        network.newLine()
                .setId("LINE_1")
                .setVoltageLevel1(vl1.getId()).setNode1(connect(vl1, nodeCounter1))
                .setVoltageLevel2(vlS2.getId()).setConnectableBus2("BUS_S2").setBus2("BUS_S2")
                .setR(1.0).setX(10.0).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0)
                .add();
        network.newLine()
                .setId("LINE_2")
                .setVoltageLevel1(vl1.getId()).setNode1(connect(vl1, nodeCounter1))
                .setVoltageLevel2(vlS3.getId()).setConnectableBus2("BUS_S3").setBus2("BUS_S3")
                .setR(1.0).setX(10.0).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0)
                .add();
        network.newLine()
                .setId("LINE_3")
                .setVoltageLevel1(vlS2.getId()).setConnectableBus1("BUS_S2").setBus1("BUS_S2")
                .setVoltageLevel2(vlS3.getId()).setConnectableBus2("BUS_S3").setBus2("BUS_S3")
                .setR(1.0).setX(10.0).setG1(0.0).setB1(0.0).setG2(0.0).setB2(0.0)
                .add();

        // ===== Boundary (Dangling) Lines (3) =====
        for (int i = 1; i <= 3; i++) {
            vl1.newBoundaryLine()
                    .setId("BOUNDARY_LINE_" + i)
                    .setNode(connect(vl1, nodeCounter1))
                    .setR(1.0).setX(10.0).setG(0.0).setB(0.0)
                    .setP0(10.0).setQ0(5.0)
                    .add();
        }

        // ===== HVDC Lines (3) =====
        for (int i = 1; i <= 3; i++) {
            network.newHvdcLine()
                    .setId("HVDC_LINE_" + i)
                    .setR(1.0).setNominalV(400.0)
                    .setMaxP(100.0).setActivePowerSetpoint(50.0)
                    .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER)
                    .setConverterStationId1("LCC_CONVERTER_STATION_" + i)
                    .setConverterStationId2("LCC_PARTNER_" + i)
                    .add();
        }

        return network;
    }

    /**
     * Allocate the next free node in a NODE_BREAKER voltage level and connect it
     * to the busbar section located at node 0 through a closed disconnector.
     */
    private static int connect(VoltageLevel vl, AtomicInteger nodeCounter) {
        int node = nodeCounter.incrementAndGet();
        vl.getNodeBreakerView().newDisconnector()
                .setId("DISC_" + vl.getId() + "_" + node)
                .setNode1(node)
                .setNode2(0)
                .setOpen(false)
                .add();
        return node;
    }
}
