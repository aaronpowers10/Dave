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
import willie.controls.Setpoint;
import willie.core.ReportWriter;
import willie.core.WillieObject;
import willie.loads.Load;
import willie.output.Report;

public class CHWCoilFlowSetpoint implements WillieObject, Setpoint, ReportWriter {

	private String name;
	private Coil coil;
	private String airsideControl;
	private double nominalCHWST;
	private Load load;

	
	public CHWCoilFlowSetpoint(String name) {
		this.name = name;
	}

	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		coil = (Coil) objectReferences.get(objectData.getAlpha("Coil"));
		airsideControl = objectData.getAlpha("Airside Control");
		nominalCHWST = objectData.getReal("Nominal CHWST");
		load = (Load) objectReferences.get(objectData.getAlpha("Load"));
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public double getSetpoint() {
//		return loadRatio()*coil.nominalFlow();
//		if (airsideControl.equals("CAV")) {
//			return coil.nominalFlow() * curveCAV2();
//		} else {
//			return coil.nominalFlow() * curveVAV2();
//		}
		if (airsideControl.equals("CAV")) {
		return coil.nominalFlow() * curveCAV3();
	} else {
		return coil.nominalFlow() * curveVAV3() ;
	}
	}
	
	private double curveCAV3() {
		double c0 = 0.0;
		double c1 = -0.091549;
		double c2 = 1.1012826;
		return c0 + c1 * loadRatio() + c2 * loadRatio() * loadRatio()- dT()*0.05;
	}
	
	private double curveVAV3(){
		double c0 =                         0.0;
		double c1 =                        0.629428;
		double c2 =                         0.355645;
		return c0 + c1 * loadRatio() + c2 * loadRatio() * loadRatio()- dT()*0.05;
	}

	private double curveCAV2() {
		double c0 = 0.0;
		double c1 = -0.091549;
		double c2 = 1.1012826;
		return c0 + c1 * loadRatio() + c2 * loadRatio() * loadRatio();
	}
	
	private double curveVAV2(){
		double c0 =                         0.0;
		double c1 =                        0.629428;
		double c2 =                         0.355645;
		return c0 + c1 * loadRatio() + c2 * loadRatio() * loadRatio();
	}

	private double curveCAV() {
		double c0 = -0.138514124;
		double c1 = 0.079056343;
		double c2 = -0.008418919;
		double c3 = 0.000151082;
		double c4 = 1.8216873;
		double c5 = -4.032504534;
		double c6 = 3.420719869;
		double c7 = -0.457735423;
		double c8 = 0.57909603;
		double c9 = 0.022728018;
		double c10 = 0.005741866;

		return c0 + c1 * dT() + c2 * pow(dT(), 2) + c3 * pow(dT(), 3) + c4 * loadRatio() + c5 * pow(loadRatio(), 2)
				+ c6 * pow(loadRatio(), 3) + c7 * dT() * loadRatio() + c8 * pow(dT(), 2) * loadRatio()
				+ c9 * dT() * pow(loadRatio(), 2) + c10 * pow(dT(), 2) * pow(loadRatio(), 2);
	}

	private double curveVAV() {
		double c0 = -0.165111694;
		double c1 = 0.134323699;
		double c2 = -0.02815176;
		double c3 = 0.002076233;
		double c4 = 2.046441774;
		double c5 = -3.78741714;
		double c6 = 2.952342271;
		double c7 = -0.639774401;
		double c8 = 0.724787202;
		double c9 = 0.050079601;
		double c10 = 0.012554269;

		return c0 + c1 * dT() + c2 * pow(dT(), 2) + c3 * pow(dT(), 3) + c4 * loadRatio() + c5 * pow(loadRatio(), 2)
				+ c6 * pow(loadRatio(), 3) + c7 * dT() * loadRatio() + c8 * pow(dT(), 2) * loadRatio()
				+ c9 * dT() * pow(loadRatio(), 2) + c10 * pow(dT(), 2) * pow(loadRatio(), 2);
	}

	private double dT() {
		return nominalCHWST - coil.inletTemperature();
	}

	private double loadRatio() {
		return (load.sensibleLoad() + load.latentLoad()) / coil.nominalCapacity();
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 3);
		report.addDataHeader("DT", "[Deg-F]");
		report.addDataHeader("Load Ratio", "");
		report.addDataHeader("Setpoint", "[GPM]");

	}

	@Override
	public void addData(Report report) {
		report.putReal(dT());
		report.putReal(loadRatio());
		report.putReal(getSetpoint());
	}

	private static double test(double dT, double loadRatio) {
		double c0 = -0.138514124;
		double c1 = 0.079056343;
		double c2 = -0.008418919;
		double c3 = 0.000151082;
		double c4 = 1.8216873;
		double c5 = -4.032504534;
		double c6 = 3.420719869;
		double c7 = -0.457735423;
		double c8 = 0.57909603;
		double c9 = 0.022728018;
		double c10 = 0.005741866;

		return c0 + c1 * dT + c2 * pow(dT, 2) + c3 * pow(dT, 3) + c4 * loadRatio + c5 * pow(loadRatio, 2)
				+ c6 * pow(loadRatio, 3) + c7 * dT * loadRatio + c8 * pow(dT, 2) * loadRatio
				+ c9 * dT * pow(loadRatio, 2) + c10 * pow(dT, 2) * pow(loadRatio, 2);
	}

	public static void main(String[] args){
		System.out.println(test(1,0.8));
	}

}
