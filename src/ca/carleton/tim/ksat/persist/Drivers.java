package ca.carleton.tim.ksat.persist;

//javase imports
import java.util.Map;

//KSAT import
import ca.carleton.tim.ksat.client.DriverAdapter;

public class Drivers {

	protected Map<String, DriverAdapter> drivers;

	public Map<String, DriverAdapter> getDrivers() {
		return drivers;
	}
	public void setDrivers(Map<String, DriverAdapter> drivers) {
		this.drivers = drivers;
	}
	
}