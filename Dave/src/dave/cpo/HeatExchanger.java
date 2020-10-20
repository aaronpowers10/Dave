package dave.cpo;

import static java.lang.Math.exp;
import static java.lang.Math.log;

import booker.building_data.BookerObject;
import booker.building_data.NamespaceList;
import willie.core.ReportWriter;
import willie.core.WillieObject;
import willie.output.Report;

public class HeatExchanger implements WillieObject, ReportWriter {
	
	private String name;
	private double c1;
	private double c2;
	private double c3;
	private double c4;
	private double c5;
	private double c6;
	private double nominalUA;
	private ExchangerSide coldSide;
	private ExchangerSide hotSide;

	public HeatExchanger(String name){
		this.name = name;
	}
	
	@Override
	public String name() {
		return name;
	}

	@Override
	public void read(BookerObject objectData, NamespaceList<WillieObject> objectReferences) {
		nominalUA = objectData.getReal("Nominal UA");
		c1 = objectData.getReal("C1");
		c2 = objectData.getReal("C2");
		c3 = objectData.getReal("C3");
		c4 = objectData.getReal("C4");
		c5 = objectData.getReal("C5");
		c6 = objectData.getReal("C6");
		coldSide = (ExchangerSide)objectReferences.get(objectData.getAlpha("Cold Side"));
		hotSide = (ExchangerSide)objectReferences.get(objectData.getAlpha("Hot Side"));
	}
	
	private double dtA() {
		return Math.max(0.0001,hotSide.inletTemperature()-coldSide.outletTemperature());
	}
	
	private double dtB() {
		return Math.max(0.0001,hotSide.outletTemperature() - coldSide.inletTemperature());
	}
	
	private double lmtd(){
		if(Math.abs(dtA()-dtB()) < 0.0001) {
			return 0.5*(dtA()+ dtB());
		} else {
			return (dtB()-dtA())/Math.log(dtB()/dtA());
		}
	}
	
	private double ua() {
		double fixed = c1 / (c2 * Math.pow(Math.max(1.0, hotSide.flowFraction() - c3) , 2) + c2 * Math.pow(Math.max(1.0, coldSide.flowFraction() - c3) ,2));
		return nominalUA / (fixed + c4 / Math.pow(hotSide.flowFraction(), c5) + c4 / Math.pow( coldSide.flowFraction(), c6));
	}
	
	public double heatTransfer(ExchangerSide exchangerSide){
		if(exchangerSide.equals(coldSide)){
			return -ua()*lmtd();
		} else {
			return ua()*lmtd();
		}
	}

	@Override
	public void addHeader(Report report) {
		report.addTitle(name, 3);
		report.addDataHeader("UA [Btu/Hr-R]", "");
		report.addDataHeader("LMTD", "[Deg-R]");
		report.addDataHeader("Heat Transfer", "[Btu/Hr]");
		
	}

	@Override
	public void addData(Report report) {
		report.putReal(ua());
		report.putReal(lmtd());
		report.putReal(heatTransfer(hotSide));
		
	}	

}
