/**
 * This class serves as a parking lot that the user wishes to save, load, or edit.
 */

package cs275.parking.tracker;

import java.util.ArrayList;

import com.cloudmine.api.CMGeoPoint;
import com.cloudmine.api.CMObject;

public class ParkingLot extends CMObject {
	public static final String CLASS_NAME = "ParkingLot";

	private CMGeoPoint location;
	private String name;
	private ArrayList<HourRange> hours;
	private ArrayList<PriceRange> prices;

	public ParkingLot() {
		super();
	}

	public ParkingLot(String lotname, double longitude, double latitude, ArrayList<HourRange> hourranges,
			ArrayList<PriceRange> priceranges) {
		super();
		name = lotname;
		location = new CMGeoPoint(longitude, latitude);
		hours = hourranges;
		prices = priceranges;
	}

	/** This method is needed for cloudmine use */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<HourRange> getHours() {
		return hours;
	}

	public void setHours(ArrayList<HourRange> hours) {
		this.hours = hours;
	}

	public ArrayList<PriceRange> getPrices() {
		return prices;
	}

	public void setPrices(ArrayList<PriceRange> prices) {
		this.prices = prices;
	}

	public CMGeoPoint getLocation() {
		return location;
	}

	public void setLocation(CMGeoPoint location) {
		this.location = location;
	}

	@Override
	public String toString() {
		return name+" located at ("+location.getLongitude()+","+location.getLatitude()+")";
	}

	public String hoursString() {
		String retstr = "";
		for(HourRange curr : hours)
		{
			retstr += curr + "\n";
		}
		return retstr;
	}
	public String pricesString() {
		String retstr = "";
		for(PriceRange curr : prices)
		{
			retstr += curr + "\n";
		}
		return retstr;
	}
}
