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

import java.util.ArrayList;

import booker.building_data.BookerObject;
import booker.building_data.NamespaceList;
import willie.controls.Controller;
import willie.core.RequiresPostStepProcessing;
import willie.core.RequiresTimeManager;
import willie.core.TimeManager;
import willie.core.Timer;
import willie.core.WillieObject;

public class LoadBasedController implements WillieObject, Controller, RequiresPostStepProcessing, RequiresTimeManager {

	private String name;
	private ArrayList<Double> togglePoints;
	private BtuMeter btuMeter;
	private double output;
	private double previousOutput;
	private double minCycleTime;
	private Timer timer;
	private TimeManager timeManager;

	public LoadBasedController(String name) {
		this.name = name;
		output = 1;
		previousOutput = 1;
		timer = new Timer();
		timer.reset();
	}

	@Override
	public double output() {
		if(timer.getTime() >= minCycleTime){
			
			timer.reset();
		
			int loadRange = loadRange();
	
			if (isEven(loadRange)) {
				if (btuMeter.sensorOutput() > togglePoints.get(loadRange)) {
					output = 1.0;
				}
			} else {
				if (btuMeter.sensorOutput() < togglePoints.get(loadRange + 1)) {
					output = 0.0;
				}
			}
			return output;
		} else {
			return output;
		}
	}

	private boolean isEven(int x) {
		if ((x & 1) == 0) {
			return true;
		} else {
			return false;
		}
	}

	private int loadRange() {
		int loadRange = -1;
		for (Double togglePoint : togglePoints) {
			if (btuMeter.sensorOutput() > togglePoint) {
				loadRange++;
			}
		}
		return loadRange;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		btuMeter = (BtuMeter) objectReferences.get(objectData.getAlpha("Btu Meter"));
		togglePoints = new ArrayList<Double>();
		for (int i = 0; i < objectData.size("Toggle Points"); i++) {
			togglePoints.add(objectData.getReal("Toggle Points", i));
		}

		minCycleTime = objectData.getReal("Min Cycle Time");
		timer.setTime(minCycleTime);

	}

	@Override
	public void processPostStep() {
		previousOutput = output;
		timer.step(timeManager.dtHours());
	}

	@Override
	public void linkToTimeManager(TimeManager timeManager) {
		this.timeManager = timeManager;	
	}

}
