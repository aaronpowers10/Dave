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

import willie.core.ObjectFactory;
import willie.core.WillieObject;

public class HydronicFactory implements ObjectFactory {

	@Override
	public WillieObject create(String type, String name) {
		if(type.equals("A Port")){
			return new ThreeWayValveAPort(name);
		} else if (type.equals("B Port")) {
			return new ThreeWayValveBPort(name);
		} else if (type.equals("Btu Meter")) {
			return new BtuMeter(name);
		} else if (type.equals("Chiller")) {
			return new Chiller(name);
		} else if (type.equals("Coil")) {
			return new Coil(name);
		} else if (type.equals("Coil Flow Setpoint")) {
			return new CHWCoilFlowSetpoint(name);
		} else if (type.equals("Condenser")) {
			return new Condenser(name);
		} else if (type.equals("Constant Liquid Node")) {
			return new ConstantLiquidNode(name);
		} else if (type.equals("Constant Pressure Liquid Node")) {
			return new ConstantPressureLiquidNode(name);
		}else if (type.equals("Cooling Tower")) {
			return new CoolingTower(name);
		} else if (type.equals("DP Sensor")) {
			return new DPSensor(name);
		} else if (type.equals("Evaporator")) {
			return new Evaporator(name);
		} else if (type.equals("Exchanger Side")) {
			return new ExchangerSide(name);
		} else if (type.equals("Liquid Flow Sensor")) {
			return new LiquidFlowSensor(name);
		}  else if (type.equals("Load Based Controller")) {
			return new LoadBasedController(name);
		} else if (type.equals("Heat Exchanger")) {
			return new HeatExchanger(name);
		} else if (type.equals("Heat Sink")) {
			return new HeatSink(name);
		} else if (type.equals("Max Valve Position Sensor")) {
			return new MaxValvePositionSensor(name);
		}  else if (type.equals("Pipe")) {
			return new Pipe(name);
		}  else if (type.equals("Pump")) {
			return new Pump(name);
		}  else if (type.equals("Liquid Temperature Sensor")) {
			return new LiquidTemperatureSensor(name);
		} else if (type.equals("Two Way Valve")) {
			return new TwoWayValve(name);
		} else if (type.equals("Variable Liquid Node")) {
			return new VariableLiquidNode(name);
		}  else {
			return null;
		}
	}


}
