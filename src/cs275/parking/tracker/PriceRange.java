/**
 * This class serves as a price range object that the user enters for a specific duration of time.
 */

package cs275.parking.tracker;

import java.text.NumberFormat;

import com.cloudmine.api.CMObject;

public class PriceRange extends CMObject {
	public static final String CLASS_NAME = "PriceRange";

	static NumberFormat dateformatter = NumberFormat.getNumberInstance();
	static NumberFormat moneyformatter = NumberFormat.getCurrencyInstance();
	float hour, price;

	public PriceRange() {
	}

	public PriceRange(float hour, float price) {
		super();
		this.hour = hour;
		this.price = price;
	}

	public float getHour() {
		return hour;
	}

	public void setHour(float hour) {
		this.hour = hour;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return dateformatter.format(hour) + " - " + moneyformatter.format(price);
	}

	/** This method is needed for cloudmine use */
	@Override
	public String getClassName() {
		return CLASS_NAME;
	}
}
