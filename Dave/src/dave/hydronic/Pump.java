/*
 *
 *  Copyright (C) 2017 Aaron Powers
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package dave.hydronic;

import static java.lang.Math.pow;

import booker.building_data.BookerObject;
import booker.building_data.NamespaceList;
import willie.controls.Controller;
import willie.core.Conversions;
import willie.core.ElectricConsumer;
import willie.core.ReportWriter;
import willie.core.WillieObject;
import willie.output.Report;

public class Pump extends LiquidElement implements ReportWriter, ElectricConsumer {

	private String name;
	private double nominalFlow;
	private double nominalHead;
	private double nominalEfficiency;
	private double motorEfficiency;
	private Controller controller;
	private final double c1 = 1.35348296;
	private final double c2 = 0.0159317;
	private final double c3 = -0.36941442;
	private final double c4 = 0.36977392;
	private final double c5 = 0.84037501;
	private final double c6 = -0.21014881;

	public Pump(String name) {
		super();
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		super.read(objectData, objectReferences);
		nominalFlow = objectData.getReal("Nominal Flow");
		nominalHead = objectData.getReal("Nominal Head");
		nominalEfficiency = objectData.getReal("Nominal Efficiency");
		motorEfficiency = objectData.getReal("Motor Efficiency");
		controller = (Controller) objectReferences.get(objectData.getAlpha("Controller"));
	}

	@Override
	public double electricPower() {
		if (volumetricFlow() == 0) {
			return 0;
		} else {
			double maxSpeedFlowRatio = maxSpeedFlow() / nominalFlow;
			double maxSpeedPower = (c4 + c5 * maxSpeedFlowRatio + c6 * maxSpeedFlowRatio * maxSpeedFlowRatio)
					* nominalPower();
			return maxSpeedPower * controller.output() * controller.output() * controller.output();
		}
	}

	private double maxSpeedFlow() {
		double pressureRatio = pressureGain() / (nominalHead * controller.output() * controller.output());
		return (-c2 - pow((c2 * c2 - 4 * c3 * (c1 - pressureRatio)), 0.5)) / (2 * c3) * nominalFlow;
	}

	@Override
	public double volumetricFlow() {
		double fl;
		if (pressureGain() >= shutoffHead()) {
			fl =  0.0;
		} else {
			double pressureRatio = pressureGain() / (nominalHead * controller.output() * controller.output()); 
			fl = (-c2 - pow((c2 * c2 - 4 * c3 * (c1 - pressureRatio)), 0.5)) / (2 * c3) * nominalFlow
					* controller.output();
		}
		return fl;
	}

	@Override
	public double heatGain() {
		return Conversions.kWToBtu(electricPower() * motorEfficiency);
	}

	@Override
	public double volume() {
		return 25;
	}

	private double shutoffHead() {
		return c1 * nominalHead*controller.output()*controller.output();
	}

	private double nominalPower() {
		return 0.7456 * nominalFlow * nominalHead / nominalEfficiency / motorEfficiency / 3960.0;
	}
	
	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 6);
		report.addDataHeader("Flow", "[GPM]");
		report.addDataHeader("Head", "[Ft.]");
		report.addDataHeader("Controller Output", "");
		report.addDataHeader("Electric Power", "[kW]");
		report.addDataHeader("Heat Gain", "[Btu/Hr]");
		report.addDataHeader("Shutoff Head", "[Ft.]");
		
	}

	@Override
	public void addData(Report report) {
		report.putReal(volumetricFlow());
		report.putReal(pressureGain());
		report.putReal(controller.output());
		report.putReal(electricPower());
		report.putReal(heatGain());
		report.putReal(shutoffHead());
	}

}
