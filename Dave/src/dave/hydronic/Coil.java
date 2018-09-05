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
import willie.loads.Load;
import willie.output.Report;

public class Coil extends LiquidElement implements ReportWriter {
	
	private String name;
	private double nominalFlow;
	private double nominalPressureDrop;
	private double pressureExponent;
	private Load load;
	private double nominalCapacity;
	
	public Coil(String name){
		super();
		this.name = name;
	}
	
	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		super.read(objectData, objectReferences);
		nominalFlow = objectData.getReal("Nominal Flow");
		nominalCapacity = objectData.getReal("Nominal Capacity");
		nominalPressureDrop = objectData.getReal("Nominal Pressure Drop");
		pressureExponent = objectData.getReal("Pressure Exponent");	
		load = (Load)objectReferences.get(objectData.getAlpha("Load"));
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
	
	public double nominalFlow(){
		return nominalFlow;
	}
	
	public double nominalCapacity(){
		return nominalCapacity;
	}
	
	public double load(){
		return Math.max(0,load.sensibleLoad() + load.latentLoad());
	}

	@Override
	public double heatGain() {
		return load();
	}

	@Override
	public double volume() {
		return 5.0;
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 5);
		report.addDataHeader("Load", "[Btu/Hr]");
		report.addDataHeader("Flow", "[GPM]");
		report.addDataHeader("Pressure Drop", "[Ft.]");
		report.addDataHeader("Inlet Temperature", "[Deg-F]");
		report.addDataHeader("Outlet Temperature", "[Deg-F]");
		
	}

	@Override
	public void addData(Report report) {
		report.putReal(load());
		report.putReal(volumetricFlow());
		report.putReal(pressureDrop());
		report.putReal(inletTemperature());
		report.putReal(outletTemperature());
		
	}

}
