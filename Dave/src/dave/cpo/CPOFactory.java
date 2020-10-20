package dave.cpo;


import willie.core.ObjectFactory;
import willie.core.WillieObject;

public class CPOFactory implements ObjectFactory {

	@Override
	public WillieObject create(String type, String name) {
		if(type.equals("CPO Tower")){
			return new CoolingTower(name);
		} else if (type.equals("CPO Exchanger Side")) {
			return new ExchangerSide(name);
		} else if (type.equals("CPO Heat Exchanger")) {
			return new HeatExchanger(name);
		} else if (type.equals("CPO Load Coil")) {
			return new LoadCoil(name);
		} else if (type.equals("CPO Pipe")) {
			return new Pipe(name);
		} else if (type.equals("CPO Pump")) {
			return new Pump(name);
		} else if (type.equals("CPO Chiller")) {
			return new Chiller(name);
		} else {
			return null;
		}
	}
}