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
import willie.controls.Sensor;
import willie.core.ReportWriter;
import willie.core.WillieObject;
import willie.output.Report;

public class BtuMeter implements WillieObject, Sensor, ReportWriter {
	
	private String name;
	private LiquidNode inletNode;
	private LiquidNode outletNode;
	private LiquidFlowSensor flowSensor;
	
	public BtuMeter(String name){
		this.name = name;
	}
	
	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		flowSensor = (LiquidFlowSensor)objectReferences.get(objectData.getAlpha("Flow Sensor"));
		inletNode = (LiquidNode)objectReferences.get(objectData.getAlpha("Inlet Node"));
		outletNode = (LiquidNode)objectReferences.get(objectData.getAlpha("Outlet Node"));
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public double sensorOutput() {
		return 500*flowSensor.sensorOutput()*(inletNode.temperature() - outletNode.temperature())/12000.0/100.0;
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 1);
		report.addDataHeader("Load", "[Btu/Hr]");
	}

	@Override
	public void addData(Report report) {
		report.putReal(sensorOutput());
	}
}
