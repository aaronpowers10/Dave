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
import willie.core.ElectricConsumer;
import willie.core.ReportWriter;
import willie.core.RequiresWeather;
import willie.core.Weather;
import willie.core.WillieObject;
import willie.output.Report;

public class CoolingTower extends LiquidElement implements RequiresWeather, ElectricConsumer,ReportWriter{
	
	private String name;
	private double nominalFlow;
	private double designWetbulb;
	private double designApproach;
	private double designRange;
	private double fanOffFlow;
	private double designFanPower;
	private double nominalFrictionHead;
	private double staticHead;
	private double pressureExponent;
	private Weather weather;
	private Controller controller;
	
	public CoolingTower(String name){
		super();
		this.name = name;
	}
	
	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		super.read(objectData, objectReferences);
		nominalFlow = objectData.getReal("Nominal Flow");
		designWetbulb = objectData.getReal("Design Wetbulb");
		designApproach = objectData.getReal("Design Approach");
		designRange = objectData.getReal("Design Range");
		fanOffFlow = objectData.getReal("Fan Off Flow Pct");
		designFanPower = objectData.getReal("Design Fan Power");
		nominalFrictionHead = objectData.getReal("Nominal Friction Head");
		staticHead = objectData.getReal("Static Head");
		pressureExponent = objectData.getReal("Pressure Exponent");
		controller = (Controller)objectReferences.get(objectData.getAlpha("Controller"));
	}
	
	private double nominalCapacity(){
		return 60/7.48052*density()*specificHeat() * nominalFlow * designRange;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public double volumetricFlow() {
		if ((pressureDrop() - staticHead) < 0) {
			return 0;
		} else {
			return nominalFlow * pow(((pressureDrop() - staticHead) / nominalFrictionHead), 1 / pressureExponent);
		}
	}

	@Override
	public double heatGain() {
		double airflow = Math.max(fanOffFlow, controller.output());
		return -(capacityFApprWb(approach(),weather.wetbulb()) * capacityFRangeWb(range(),weather.wetbulb()) * capacityFAirflow(airflow))
				/ designCapacityRatio() * nominalCapacity();
	}

	@Override
	public double volume() {
		return 20.0*20*10;
	}
	
	private double capacityFApprWb(double approach, double wetbulb){
		return 0.50061393 + 0.00588251*approach + 0.0002163*approach*approach+
				-0.01913189*wetbulb + 0.0002236*wetbulb*wetbulb +0.00106108*approach*wetbulb;
	}
	
	private double capacityFRangeWb(double range, double wetbulb){
		return 0.08352359 + 0.11247273*range + -0.00135847*range*range+
				0.00003417*wetbulb + 0.00003125*wetbulb*wetbulb + -0.00034001*range*wetbulb;
	}
	
	private double capacityFAirflow(double airflow){
		return 0.04976825 + 1.04669762*airflow + -0.09646816 *airflow*airflow;
	}
	
	private double designCapacityRatio() {
		return capacityFApprWb(designApproach, designWetbulb) * capacityFRangeWb(designRange, designWetbulb)
				* capacityFAirflow(1);
	}

	private double approach() {
		return outletTemperature() - weather.wetbulb();
	}

	private double range() {
		return inletTemperature() - outletTemperature();
	}

	@Override
	public void linkToWeather(Weather weather) {
		this.weather = weather;		
	}

	@Override
	public double electricPower() {
		return designFanPower * pow(controller.output(), 3);
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 6);
		report.addDataHeader("Water Flow", "[GPM]");
		report.addDataHeader("Inlet Water Temperature", "[Deg-F]");
		report.addDataHeader("Outlet Water Temperature", "[Deg-F]");
		report.addDataHeader("Approach", "[Deg-F]");
		report.addDataHeader("Controller Output", "");
		report.addDataHeader("Fan Power", "kW");
		
	}

	@Override
	public void addData(Report report) {
		report.putReal(volumetricFlow());
		report.putReal(inletTemperature());
		report.putReal(outletTemperature());
		report.putReal(approach());
		report.putReal(controller.output());
		report.putReal(electricPower());		
	}

}
