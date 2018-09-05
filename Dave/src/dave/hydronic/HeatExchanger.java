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

import static java.lang.Math.exp;
import static java.lang.Math.log;

import booker.building_data.BookerObject;
import booker.building_data.NamespaceList;
import willie.core.ReportWriter;
import willie.core.WillieObject;
import willie.output.Report;

public class HeatExchanger implements WillieObject, ReportWriter {
	
	private String name;
	private double nominalCapacity;
	private double nominalInletDT;
	private double nominalOutletDT;
//	private double nominalEffectiveness;
	private String arrangement;
	private ExchangerSide side1;
	private ExchangerSide side2;

	public HeatExchanger(String name){
		this.name = name;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		arrangement = objectData.getAlpha("Arrangement");
		nominalCapacity = objectData.getReal("Nominal Capacity");
		nominalInletDT = objectData.getReal("Nominal Inlet DT");
		nominalOutletDT = objectData.getReal("Nominal Outlet DT");
		//nominalEffectiveness = objectData.getReal("Nominal Effectiveness");
		side1 = (ExchangerSide)objectReferences.get(objectData.getAlpha("Side 1"));
		side2 = (ExchangerSide)objectReferences.get(objectData.getAlpha("Side 2"));
	}
	
	private double nominalNtu(){
		return nominalCapacity/(Math.min(side1.nominalCapacityRate(), side2.nominalCapacityRate())*lmtd());
		
//		double cr = Math.min(side1.nominalCapacityRate()/side2.nominalCapacityRate(), 
//				side2.nominalCapacityRate()/side1.nominalCapacityRate());
//		if(arrangement.equals("Parallel")){
//			return -log(1-nominalEffectiveness*(1+cr))/(1+cr);
//		} else {
//			if(cr>0.9999){
//				return nominalEffectiveness/(1-nominalEffectiveness);
//			} else {
//				return 1/(cr-1)*log((nominalEffectiveness-1)/(nominalEffectiveness*cr-1));
//			}
//		}
	}
	
	private double lmtd(){
		return (nominalInletDT - nominalOutletDT)/log(nominalInletDT/nominalOutletDT);
	}
	
	private double ntu(){
		return nominalNtu();
	}
	
	private double effectiveness(){
		if(arrangement.equals("Parallel")){
		return (1-exp(-ntu()*(1-cr())))/(1-cr()*exp(-ntu()*(1-cr())));
		} else {
			if(cr()>0.9999){
				return ntu()/(1+ntu());
			} else {
				return (1-exp(-ntu()*(1-cr())))/(1-cr()*exp(-ntu()*(1-cr())));
			}
		}
	}
	
	private double cMin(){
		return Math.min(side1.heatCapacityRate(), side2.heatCapacityRate());
	}
	
	private double cMax(){
		return Math.max(side1.heatCapacityRate(),side2.heatCapacityRate());
	}
	
	private double cr(){
		return cMin()/cMax();
	}
	
	private double tH(){
		return Math.max(side1.inletTemperature(), side2.inletTemperature());
	}
	
	private double tC(){
		return Math.min(side1.inletTemperature(), side2.inletTemperature());
	}
	
	private ExchangerSide coldSide(){
		if(side1.inletTemperature() < side2.inletTemperature()){
			return side1;
		} else {
			return side2;
		}
	}
	
	private ExchangerSide hotSide(){
		if(side1.inletTemperature() > side2.inletTemperature()){
			return side1;
		} else {
			return side2;
		}
	}
	
	public double heatTransfer(ExchangerSide exchangerSide){
		if(exchangerSide.equals(coldSide())){
			return effectiveness()*cMin()*(tH()-tC());
		} else {
			return -effectiveness()*cMin()*(tH()-tC());
		}
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 2);
		report.addDataHeader("Effectiveness", "");
		report.addDataHeader("Heat Transfer", "[Btu/Hr]");
		
	}

	@Override
	public void addData(Report report) {
		report.putReal(effectiveness());
		report.putReal(heatTransfer(coldSide()));
		
	}	

}
