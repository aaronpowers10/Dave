package dave.cpo;

import booker.building_data.BookerObject;
import booker.building_data.NamespaceList;
import dave.hydronic.LiquidElement;
import willie.core.ReportWriter;
import willie.core.WillieObject;
import willie.loads.Load;
import willie.output.Report;

public class LoadCoil extends LiquidElement implements ReportWriter{
	
	private String name;
	//private double nominalPressureDrop;
	private double c1;
	private double c2;
	private double c3;
	private double c4;
	private Load load;
	
	
	public LoadCoil(String name){
		super();
		this.name = name;
	}
	
	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		super.read(objectData, objectReferences);
		c1 = objectData.getReal("C1");
		c2 = objectData.getReal("C2");
		c3 = objectData.getReal("C3");
		c4 = objectData.getReal("C4");
		//nominalPressureDrop = objectData.getReal("Nominal Pressure Drop");
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public double volumetricFlow() {
		double loadSI = heatGain() / 3412.0;
		double tWaterSI = (inletTemperature() - 32) * 5 / 9.0;
		double flowSI = Math.exp(c1 + c2 * Math.pow(Math.log(loadSI) , 2) + c3 * Math.exp(c4 * tWaterSI));
		return flowSI*4.403*60*60;
	}

	@Override
	public double heatGain() {
		return load.sensibleLoad() + load.latentLoad();
	}

	@Override
	public double volume() {
		return 10.0;
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 5);
		report.addDataHeader("Flow", "[GPM]");
		report.addDataHeader("Pressure Drop", "[Ft.]");
		report.addDataHeader("Inlet Temperature", "[Deg-F]");
		report.addDataHeader("Outlet Temperature", "[Deg-F]");
		report.addDataHeader("Load", "[Btu/Hr]");
	}
	
	@Override
	public void addData(Report report) {
		report.putReal(volumetricFlow());
		report.putReal(pressureDrop());
		report.putReal(inletTemperature());
		report.putReal(outletTemperature());
		report.putReal(heatGain());
	}
}
