package dave.cpo;

import booker.building_data.BookerObject;
import booker.building_data.NamespaceList;
import dave.hydronic.LiquidElement;
import willie.controls.Controller;
import willie.core.ElectricConsumer;
import willie.core.ReportWriter;
import willie.core.WillieObject;
import willie.output.Report;

public class Pump extends LiquidElement implements ReportWriter, ElectricConsumer {

	private String name;
	private double nominalFlow;
	private double nominalHead;
	private Controller controller;
	private double c1;
	private double c2;
	private double c3;
	private double c4;
	private double c5;
	private double c6;

	public Pump(String name) {
		super();
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		super.read(objectData, objectReferences);
		nominalFlow = objectData.getReal("Nominal Flow");
		nominalHead = objectData.getReal("Nominal Head");
		c1 = objectData.getReal("C1");
		c2 = objectData.getReal("C2");
		c3 = objectData.getReal("C3");
		c4 = objectData.getReal("C4");
		c5 = objectData.getReal("C5");
		c6 = objectData.getReal("C6");
		controller = (Controller) objectReferences.get(objectData.getAlpha("Controller"));
	}

	@Override
	public double electricPower() {
		return volumetricFlow() * pressureGain() / 3956.0 / efficiency() * 0.7456;
	}

	private double efficiency() {
		return c1 + c2 * (volumetricFlow() / c5 / controller.output() / nominalFlow)
				+ c3 * Math.pow(volumetricFlow() / c5 / controller.output() / nominalFlow, 2);
	}

	@Override
	public double volumetricFlow() {
		return c5 * controller.output() * nominalFlow
				* Math.pow(1 - pressureGain() / c4 / nominalHead / controller.output() / controller.output(), 1 / c6);
	}

	@Override
	public double heatGain() {
		return 0; // current CPO version does not include pump heat gain
		// return Conversions.kWToBtu(electricPower());
	}

	@Override
	public double volume() {
		return 25;
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 5);
		report.addDataHeader("Flow", "[GPM]");
		report.addDataHeader("Head", "[Ft.]");
		report.addDataHeader("Controller Output", "");
		report.addDataHeader("Electric Power", "[kW]");
		report.addDataHeader("Heat Gain", "[Btu/Hr]");
		// report.addDataHeader("Shutoff Head", "[Ft.]");

	}

	@Override
	public void addData(Report report) {
		report.putReal(volumetricFlow());
		report.putReal(pressureGain());
		report.putReal(controller.output());
		report.putReal(electricPower());
		report.putReal(heatGain());
		// report.putReal(shutoffHead());
	}

}