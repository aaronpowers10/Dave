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
import willie.core.ReportWriter;
import willie.core.WillieObject;
import willie.output.Report;

public class ConstantLiquidNode implements LiquidNode, WillieObject, ReportWriter {

	private String name;
	private double pressure;
	private double temperature;

	public ConstantLiquidNode(String name) {
		this.name = name;
	}

	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		pressure = objectData.getReal("Pressure");
		temperature = objectData.getReal("Temperature");

	}

	@Override
	public String name() {
		return name;
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
}