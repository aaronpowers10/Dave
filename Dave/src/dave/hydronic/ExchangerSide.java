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
import willie.core.ReportWriter;
import willie.core.WillieObject;
import willie.output.Report;

public class ExchangerSide extends LiquidElement implements ReportWriter{
	
	private String name;
	private double nominalFlow;
	private double nominalPressureDrop;
	private double pressureExponent;
	private HeatExchanger heatExchanger;
	
	public ExchangerSide(String name){
		super();
		this.name = name;
	}
	
	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		super.read(objectData, objectReferences);
		nominalFlow = objectData.getReal("Nominal Flow");
		nominalPressureDrop = objectData.getReal("Nominal Pressure Drop");
		pressureExponent = objectData.getReal("Pressure Exponent");	
		heatExchanger = (HeatExchanger)objectReferences.get(objectData.getAlpha("Heat Exchanger"));
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public double volumetricFlow() {
		if (pressureDrop() < 0) {
			return -nominalFlow
					* pow((-pressureDrop() / nominalPressureDrop), 1 / pressureExponent);
		} else {
			return nominalFlow
					* pow((pressureDrop() / nominalPressureDrop), 1 / pressureExponent);
		}
	}
	
	public double nominalCapacityRate(){
		return 60/7.48052*density()*specificHeat()* nominalFlow;
	}

	@Override
	public double heatGain() {
		return heatExchanger.heatTransfer(this);
	}

	@Override
	public double volume() {
		return 10.0;
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 4);
		report.addDataHeader("Flow", "[GPM]");
		report.addDataHeader("Pressure Drop", "[Ft.]");
		report.addDataHeader("Inlet Temperature", "[Deg-F]");
		report.addDataHeader("Outlet Temperature", "[Deg-F]");
		
	}
	
	@Override
	public void addData(Report report) {
		report.putReal(volumetricFlow());
		report.putReal(pressureDrop());
		report.putReal(inletTemperature());
		report.putReal(outletTemperature());
		
	}
}
