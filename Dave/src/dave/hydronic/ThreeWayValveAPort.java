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
import willie.core.ReportWriter;
import willie.core.WillieObject;
import willie.output.Report;

public class ThreeWayValveAPort extends LiquidElement implements ReportWriter{
	
	private String name;
	private double nominalFlow;
	private double nominalPressureDrop;
	private double pressureExponent;
	private Controller controller;
	private double nominalConstant;
	
	public ThreeWayValveAPort(String name){
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
		nominalPressureDrop = objectData.getReal("Nominal Pressure Drop");
		pressureExponent = objectData.getReal("Pressure Exponent");
		controller = (Controller)objectReferences.get(objectData.getAlpha("Controller"));
		this.setConstant();		
	}
	
	private void setConstant(){
		nominalConstant = nominalFlow / pow(nominalPressureDrop,1/pressureExponent);
	}

	@Override
	public double volumetricFlow() {
		if(pressureDrop() < 0){
			return -controller.output()*nominalConstant*pow((-pressureDrop()),(1/pressureExponent));
		} else {
			return controller.output()*nominalConstant*pow((pressureDrop()),(1/pressureExponent));
		}
	}

	@Override
	public double heatGain() {
		return 0.0;
	}

	@Override
	public double volume() {
		return Math.PI*3;
	}
	
	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 2);
		report.addDataHeader("Flow", "[GPM]");
		report.addDataHeader("Pressure Drop", "[Ft.]");
		
	}

	@Override
	public void addData(Report report) {
		report.putReal(volumetricFlow());
		report.putReal(pressureDrop());
		
	}

}
