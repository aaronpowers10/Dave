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

import static dave.hydronic.LiquidElementMath.sumFlow;
import static dave.hydronic.LiquidElementMath.sumHeatAdvection;
import static dave.hydronic.LiquidElementMath.sumHeatCapacity;
import static dave.hydronic.LiquidElementMath.sumHeatGain;

import java.util.ArrayList;

import booker.building_data.BookerObject;
import booker.building_data.NamespaceList;
import willie.core.ReportWriter;
import willie.core.RequiresPostStepProcessing;
import willie.core.RequiresTimeManager;
import willie.core.Simulator;
import willie.core.TimeManager;
import willie.core.WillieObject;
import willie.output.Report;

public class VariableLiquidNode implements LiquidNode, Simulator,WillieObject, ReportWriter,RequiresTimeManager,RequiresPostStepProcessing{
	
	private String name;
	private double pressure;
	private double temperature;
	private double nextPressure;
	private double nextTemperature;
	private double alpha;
	private ArrayList<LiquidElement> inletElements;
	private ArrayList<LiquidElement> outletElements;
	private TimeManager timeManager;
	
	public VariableLiquidNode(String name){
		this.name = name;
		inletElements = new ArrayList<LiquidElement>();
		outletElements = new ArrayList<LiquidElement>();
	}
	
	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		pressure = objectData.getReal("Initial Pressure");
		temperature = objectData.getReal("Initial Temperature");
		alpha = objectData.getReal("Alpha");
	}

	@Override
	public double temperature() {
		return temperature;
	}

	@Override
	public double pressure() {
		return pressure;
	}
	
	@Override
	public void addInletElement(LiquidElement element){
		inletElements.add(element);
	}
	
	@Override
	public void addOutletElement(LiquidElement element){
		outletElements.add(element);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void linkToTimeManager(TimeManager timeManager) {
		this.timeManager = timeManager;		
	}

	@Override
	public void processPostStep() {
		temperature = nextTemperature;
		pressure = nextPressure;		
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 2);
		report.addDataHeader("Pressure", "[Ft.]");
		report.addDataHeader("Temperature", "[Deg-F]");
	}

	@Override
	public void addData(Report report) {
		report.putReal(pressure);
		report.putReal(temperature);		
	}
	
	@Override
	public void simulateStep1(){
		nextTemperature = temperature - timeManager.dt()/sumHeatCapacity(inletElements)*(sumHeatAdvection(inletElements) - sumHeatGain(inletElements));
		nextPressure = pressure() + timeManager.dt()*alpha*(sumFlow(inletElements)-sumFlow(outletElements));		
		//nextPressure = pressure() + pressure()*timeManager.dt()*alpha*(sumFlow(inletElements)-sumFlow(outletElements));	
		//nextPressure = pressure() + pressure()*timeManager.dt()*alpha*(sumFlow(inletElements));	
	}
}
