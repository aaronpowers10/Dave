package dave.cpo;

import static java.lang.Math.pow;

import booker.building_data.BookerObject;
import booker.building_data.NamespaceList;
import dave.hydronic.LiquidElement;
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
