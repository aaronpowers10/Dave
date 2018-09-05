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
import willie.core.WillieObject;

public abstract class LiquidElement implements WillieObject {
	
	private LiquidNode inletNode;
	private LiquidNode outletNode;
	private Fluid fluid;
	
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		LiquidNode inletNode = (LiquidNode)objectReferences.get(objectData.getAlpha("Inlet Node"));
		LiquidNodeLinker.linkToNodeOutlet(this, inletNode);
		LiquidNode outletNode = (LiquidNode)objectReferences.get(objectData.getAlpha("Outlet Node"));
		LiquidNodeLinker.linkToNodeInlet(this,outletNode);
	}
	
	public LiquidElement(){
		fluid = new Water();
	}
	
	public void setInletNode(LiquidNode inletNode){
		this.inletNode = inletNode;
	}
	
	public void setOutletNode(LiquidNode outletNode){
		this.outletNode = outletNode;
	}
	
	public double inletPressure(){
		return inletNode.pressure();
	}
	
	public double outletPressure(){
		return outletNode.pressure();
	}
	
	public double inletTemperature(){
		return inletNode.temperature();
	}
	
	public double outletTemperature(){
		return outletNode.temperature();
	}
	
	public double heatAdvection(){
		return 60/7.48052*fluid.density()*fluid.specificHeat()* volumetricFlow() * (outletTemperature() - inletTemperature());
	}
	
	public double heatCapacity(){
		return fluid.density()*fluid.specificHeat()*volume();
	}
	
	public double pressureDrop(){
		return inletNode.pressure() - outletNode.pressure();
	}
	
	public double pressureGain(){
		return outletNode.pressure() - inletNode.pressure();
	}
	
	public double density(){
		return fluid.density();
	}
	
	public double specificHeat(){
		return fluid.specificHeat();
	}
	
	public double heatCapacityRate(){
		return 60/7.48052*fluid.density()*fluid.specificHeat()* volumetricFlow();
	}
	
	public abstract double volumetricFlow();

	public abstract double heatGain();
	
	public abstract double volume();

}
