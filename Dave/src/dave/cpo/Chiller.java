package dave.cpo;

import booker.building_data.BookerObject;
import booker.building_data.NamespaceList;
import willie.controls.Controller;
import willie.core.Conversions;
import willie.core.ElectricConsumer;
import willie.core.ReportWriter;
import willie.core.WillieObject;
import willie.output.Report;

public class Chiller implements WillieObject, ElectricConsumer, ReportWriter {
	
	private String name;
	private double nominalCapacity;
	private Evaporator evaporator;
	private Condenser condenser;
	private Controller controller;
	private double c1;
	private double c2;
	private double c3;
	
	public Chiller(String name){
		this.name = name;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		nominalCapacity = objectData.getReal("Nominal Capacity");
		c1 = objectData.getReal("C1");
		c2 = objectData.getReal("C2");
		c3 = objectData.getReal("C3");
		controller = (Controller)objectReferences.get(objectData.getAlpha("Controller"));
		evaporator = (Evaporator)objectReferences.get(objectData.getAlpha("Evaporator"));
		condenser = (Condenser)objectReferences.get(objectData.getAlpha("Condenser"));
		evaporator.setChiller(this);
		condenser.setChiller(this);
	}
	
	
	public double evaporatorHeat(){
		return -controller.output() * nominalCapacity;
	}
	
	public double condenserHeat(){
		return -evaporatorHeat() + Conversions.kWToBtu(electricPower());
	}
	
	private double partLoadRatio() {
		return -evaporatorHeat() / nominalCapacity;
	}

	private double operatingEfficiency() {
		if (evaporatorHeat() == 0) {
			return 0;
		} else {
			return -electricPower()/evaporatorHeat()*12000;
		}
	}

	@Override
	public double electricPower() {
		double loadSI = -evaporatorHeat()/3412.0;
		double chwstSI = Conversions.fahrenheitToKelvin(evaporator.outletTemperature());
		double lctSI = Conversions.fahrenheitToKelvin(condenser.outletTemperature());
		return loadSI * (lctSI * (1 + c1 * chwstSI / loadSI + c2 * (lctSI - chwstSI) / lctSI / loadSI) + c3 * loadSI - chwstSI) / (chwstSI - c3 * loadSI);
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 9);
		report.addDataHeader("CHW Flow", "[GPM]");
		report.addDataHeader("CHWST", "[Deg-F]");
		report.addDataHeader("CHWRT", "[Deg-F]");
		report.addDataHeader("ECT", "[Deg-F]");
		report.addDataHeader("LCT", "[Deg-F]");
		report.addDataHeader("Heat Removed", "[Tons]");
		report.addDataHeader("PLR", "");
		report.addDataHeader("Operating Efficiency", "[kW/Ton]");
		report.addDataHeader("Electric Power", "[kW]");
	}


	@Override
	public void addData(Report report) {
		report.putReal(evaporator.volumetricFlow());
		report.putReal(evaporator.outletTemperature());
		report.putReal(evaporator.inletTemperature());
		report.putReal(condenser.inletTemperature());
		report.putReal(condenser.outletTemperature());
		report.putReal(-evaporatorHeat()/12000);
		report.putReal(partLoadRatio());
		report.putReal(operatingEfficiency());
		report.putReal(electricPower());
		
	}
}
