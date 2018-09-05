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

import booker.building_data.BookerObject;
import booker.building_data.NamespaceList;
import willie.controls.Controller;
import willie.core.Conversions;
import willie.core.ElectricConsumer;
import willie.core.ReportWriter;
import willie.core.TwoVariableFunction;
import willie.core.WillieObject;
import willie.output.Report;

public class Chiller implements WillieObject, ElectricConsumer, ReportWriter {
	
	private String name;
	private double nominalCapacity;
	private double nominalEfficiency;
	private Evaporator evaporator;
	private Condenser condenser;
	private TwoVariableFunction capacityFChwEct;
	private TwoVariableFunction eirFChwEct;
	private TwoVariableFunction eirFPlrDt;
	private Controller controller;
	
	public Chiller(String name){
		this.name = name;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		nominalCapacity = objectData.getReal("Nominal Capacity");
		nominalEfficiency = objectData.getReal("Nominal Efficiency");
		capacityFChwEct = (TwoVariableFunction)objectReferences.get(objectData.getAlpha("Capacity f(CHW,ECT)"));
		eirFChwEct = (TwoVariableFunction)objectReferences.get(objectData.getAlpha("EIR f(CHW,ECT)"));
		eirFPlrDt = (TwoVariableFunction)objectReferences.get(objectData.getAlpha("EIR f(PLR,DT)"));
		controller = (Controller)objectReferences.get(objectData.getAlpha("Controller"));
		evaporator = (Evaporator)objectReferences.get(objectData.getAlpha("Evaporator"));
		condenser = (Condenser)objectReferences.get(objectData.getAlpha("Condenser"));
		evaporator.setChiller(this);
		condenser.setChiller(this);
	}
	
	public double operatingCapacity(){
		return capacityFChwEct.evaluate(evaporator.outletTemperature(), condenser.inletTemperature())*nominalCapacity*12000;
	}
	
	public double evaporatorHeat(){
		return -controller.output() * operatingCapacity();
	}
	
	public double condenserHeat(){
		return -evaporatorHeat() + Conversions.kWToBtu(electricPower());
	}
	
	private double partLoadRatio() {
		return -evaporatorHeat() / operatingCapacity();
	}

	private double nominalEir() {
		return Conversions.kWTToEir(nominalEfficiency);
	}
	
	private double dt(){
		return  ect() - evaporator.outletTemperature();
	}
	
	private double ect(){
		return condenser.inletTemperature() + (10 - (condenser.outletTemperature() -condenser.inletTemperature()));
	}

	private double operatingEfficiency() {
		if (evaporatorHeat() == 0) {
			return 0;
		} else {
			return Conversions.btuTokW(operatingCapacity()*
					eirFChwEct.evaluate(evaporator.outletTemperature(),ect())* 
							eirFPlrDt.evaluate(partLoadRatio(), dt())* nominalEir()) / (-Conversions.btuToTons(evaporatorHeat()));
		}
	}

	@Override
	public double electricPower() {
		return -evaporatorHeat()/12000.0 * operatingEfficiency();
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 7);
		report.addDataHeader("CHW Flow", "[GPM]");
		report.addDataHeader("CHWST", "[Deg-F]");
		report.addDataHeader("ECT", "[Deg-F]");
		report.addDataHeader("Heat Removed", "[Tons]");
		report.addDataHeader("PLR", "");
		report.addDataHeader("Operating Efficiency", "[kW/Ton]");
		report.addDataHeader("Electric Power", "[kW]");
	}


	@Override
	public void addData(Report report) {
		report.putReal(evaporator.volumetricFlow());
		report.putReal(evaporator.outletTemperature());
		report.putReal(condenser.inletTemperature());
		report.putReal(-evaporatorHeat()/12000);
		report.putReal(partLoadRatio());
		report.putReal(operatingEfficiency());
		report.putReal(electricPower());
		
	}
}
