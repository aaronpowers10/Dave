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
import willie.core.WillieObject;

public class Evaporator extends LiquidElement {
	
	private String name;
	private double nominalFlow;
	private double nominalPressureDrop;
	private double pressureExponent;
	private Chiller chiller;
	
	public Evaporator(String name){
		super();
		this.name = name;
	}
	
	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		super.read(objectData, objectReferences);
		nominalFlow = objectData.getReal("Nominal Flow");
		nominalPressureDrop = objectData.getReal("Nominal Pressure Drop");
		pressureExponent = objectData.getReal("Pressure Exponent");	
	}

	@Override
	public String name() {
		return name;
	}
	
	public void setChiller(Chiller chiller){
		this.chiller = chiller;
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

	@Override
	public double heatGain() {
		return chiller.evaporatorHeat();
	}

	@Override
	public double volume() {
		return 20.0*Math.PI*3;
	}

}
