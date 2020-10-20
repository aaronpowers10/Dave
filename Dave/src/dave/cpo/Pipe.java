package dave.cpo;

import static java.lang.Math.pow;

import booker.building_data.BookerObject;
import booker.building_data.NamespaceList;
import dave.hydronic.LiquidElement;
import willie.core.ReportWriter;
import willie.core.WillieObject;
import willie.output.Report;

public class Pipe extends LiquidElement implements ReportWriter{
	
	private String name;
	private double nominalFlow;
	private double nominalPressureDrop;
	private double pressureExponent;
	
	public Pipe(String name){
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

	@Override
	public double volumetricFlow() {
		if (pressureDrop() < 0) {
			return -nominalFlow * pow((-pressureDrop() / nominalPressureDrop), (1 / pressureExponent));
		} else {
			return nominalFlow * pow((pressureDrop() / nominalPressureDrop), (1 / pressureExponent));
		}
	}

	@Override
	public double heatGain() {
		return 0; 
	}

	@Override
	public double volume() {
		return Math.PI*50;
	}
	
	public double averageFluidTemperature() {
		return (inletTemperature() + outletTemperature()) * 0.5;
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 4);
		report.addDataHeader("Flow", "[GPM]");
		report.addDataHeader("Pressure Drop", "[Ft.]");
		report.addDataHeader("Inlet Temperature", "[Deg-F]");
		report.addDataHeader("Outlet Temperature", "[Deg-F]");
		
	}

	@Override
	public void addData(Report report) {
		report.putReal(volumetricFlow());
		report.putReal(pressureDrop());
		report.putReal(inletTemperature());
		report.putReal(outletTemperature());
		
	}
}