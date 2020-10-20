package dave.cpo;

import static java.lang.Math.pow;

import booker.building_data.BookerObject;
import booker.building_data.NamespaceList;
import dave.hydronic.LiquidElement;
import willie.controls.Controller;
import willie.core.ElectricConsumer;
import willie.core.Psychrometrics;
import willie.core.ReportWriter;
import willie.core.RequiresWeather;
import willie.core.Weather;
import willie.core.WillieObject;
import willie.output.Report;

public class CoolingTower extends LiquidElement implements RequiresWeather, ElectricConsumer, ReportWriter {

	private String name;
	private double nominalFlow;
	private double nominalAirflow;
	private double c1;
	private double c2;
	private double designFanPower;
	private double nominalFrictionHead;
	private double staticHead;
	private double pressureExponent;
	private Weather weather;
	private Controller controller;

	public CoolingTower(String name) {
		super();
		this.name = name;
	}

	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		super.read(objectData, objectReferences);
		nominalFlow = objectData.getReal("Nominal Water Flow");
		nominalAirflow = objectData.getReal("Nominal Airflow");
		c1 = objectData.getReal("C1");
		c2 = objectData.getReal("C2");
		designFanPower = objectData.getReal("Design Fan Power");
		nominalFrictionHead = objectData.getReal("Nominal Friction Head");
		staticHead = objectData.getReal("Static Head");
		pressureExponent = objectData.getReal("Pressure Exponent");
		controller = (Controller) objectReferences.get(objectData.getAlpha("Controller"));
	}

//	private double nominalCapacity(){
//		return 60/7.48052*density()*specificHeat() * nominalFlow * designRange;
//	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public double volumetricFlow() {
		if ((pressureDrop() - staticHead) < 0) {
			return 0;
		} else {
			return nominalFlow * pow(((pressureDrop() - staticHead) / nominalFrictionHead), 1 / pressureExponent);
		}
	}

	@Override
	public double heatGain() {
		double hSat;
		double hIn;
		double mDotA;
		double mDotW;
		double cAir;
		double cWat;
		double cMin;
		double cMax;
		double ntu;
		double cr;
		double effectiveness;
		double hSatSI;
		double hInSI;
		double mDotASI;
		double mDotWSI;
		double tWinSI;
		double wbSI;
		double cfm;
		double q;

		cfm = controller.output() * nominalAirflow;
		hSat = Psychrometrics.enthalpyFWetbulb(inletTemperature(), inletTemperature(), weather.pressure());
		hIn = weather.enthalpy();
		mDotA = 60 * 0.0796206209 * cfm; // [Lbs/Hr]
		mDotW = 0.133681 * 62.2407 * 60 * volumetricFlow(); // [Lbs/Hr]

		// Convert to SI
		hSatSI = hSat * 2.326;
		hInSI = hIn * 2.326;
		mDotASI = mDotA * 0.000125998;
		mDotWSI = mDotW * 0.000125998;
		tWinSI = (inletTemperature() - 32) * 5 / 9;
		wbSI = (weather.wetbulb() - 32) * 5 / 9;

		cAir = mDotASI * (hSatSI - hInSI);
		cWat = mDotWSI * 4.181 * (tWinSI - wbSI);
		cMin = Math.min(cAir, cWat);
		cMax = Math.max(cAir, cWat);

		ntu = c1 * Math.pow(cAir, c2) / Math.max(1, cMin);
		cr = Math.min(0.999, cMin / cMax);
		effectiveness = (1 - Math.exp(-ntu * (1 - cr))) / (1 - cr * Math.exp(-ntu * (1 - cr)));
		q = effectiveness * cMin;

		// Convert to tons
		return q * 0.947817 / 12000 * 60 * 60;
	}

	@Override
	public double volume() {
		return 20.0 * 20 * 10;
	}

	private double approach() {
		return outletTemperature() - weather.wetbulb();
	}

	private double range() {
		return inletTemperature() - outletTemperature();
	}

	@Override
	public void linkToWeather(Weather weather) {
		this.weather = weather;
	}

	@Override
	public double electricPower() {
		return designFanPower * pow(controller.output(), 3);
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 6);
		report.addDataHeader("Water Flow", "[GPM]");
		report.addDataHeader("Inlet Water Temperature", "[Deg-F]");
		report.addDataHeader("Outlet Water Temperature", "[Deg-F]");
		report.addDataHeader("Approach", "[Deg-F]");
		report.addDataHeader("Controller Output", "");
		report.addDataHeader("Fan Power", "kW");

	}

	@Override
	public void addData(Report report) {
		report.putReal(volumetricFlow());
		report.putReal(inletTemperature());
		report.putReal(outletTemperature());
		report.putReal(approach());
		report.putReal(controller.output());
		report.putReal(electricPower());
	}

}
