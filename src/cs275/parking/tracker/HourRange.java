package cs275.parking.tracker;

import java.text.DateFormat;
import java.util.GregorianCalendar;

import com.cloudmine.api.CMObject;

public class HourRange extends CMObject {
	static DateFormat formatter = DateFormat.getTimeInstance(DateFormat.SHORT);

	public static final String CLASS_NAME = "HourRange";

	enum DAY {
		Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
	};

	private DAY day;
	private int openHour, openMinute, closeHour, closeMinute;

	public HourRange() {
	}

	public HourRange(String date, int hourstart, int minutestart, int hourend, int minuteend) {
		day = DAY.valueOf(date);
		openHour = hourstart;
		openMinute = minutestart;
		closeHour = hourend;
		closeMinute = minuteend;
	}

	public int getOpenHour() {
		return openHour;
	}

	public void setOpenHour(int openHour) {
		this.openHour = openHour;
	}

	public int getOpenMinute() {
		return openMinute;
	}

	public void setOpenMinute(int openMinute) {
		this.openMinute = openMinute;
	}

	public int getCloseHour() {
		return closeHour;
	}

	public void setCloseHour(int closeHour) {
		this.closeHour = closeHour;
	}

	public int getCloseMinute() {
		return closeMinute;
	}

	public void setCloseMinute(int closeMinute) {
		this.closeMinute = closeMinute;
	}

	public DAY getDay() {
		return day;
	}

	public void setDay(DAY day) {
		this.day = day;
	}

	@Override
	public String toString() {
		String open = formatter.format(new GregorianCalendar(0,0,0,openHour,openMinute).getTime());
		String close = formatter.format(new GregorianCalendar(0,0,0,closeHour,closeMinute).getTime());
		return day+" [ "+open+" - "+close+" ]";
	}

	@Override
	public String getClassName() {
		return CLASS_NAME;
	}
}
