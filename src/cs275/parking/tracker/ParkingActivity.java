package cs275.parking.tracker;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.CMObject;
import com.cloudmine.api.persistance.ClassNameRegistry;
import com.cloudmine.api.rest.CMStore;
import com.cloudmine.api.rest.callbacks.CMObjectResponseCallback;
import com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;

public class ParkingActivity extends Activity implements OnClickListener, OnItemLongClickListener {
	// Find this in your developer console
	private static final String APP_ID = "4d3927c6d767464c93a4f63c05700dd0";
	// Find this in your developer console
	private static final String API_KEY = "ccb4343739da4cd7b3ef80090ea2b940";
	// The string tag needed for cloudmine
	private static final String TAG = "cloudmine";
	
	//Variables to represent the hour and price dialogs
	private Dialog addHourDialog;
	private Dialog addPriceDialog;
	//Array Lists to hold the hours and pricing information
	ArrayList<HourRange> currenthours = new ArrayList<HourRange>();
	ArrayList<PriceRange> currentprices = new ArrayList<PriceRange>();
	//Array Adapters to represent adapters for the hours array list and pricing array list
	private ArrayAdapter<HourRange> houradapter;
	private ArrayAdapter<PriceRange> priceadapter;
	//Needed for cloudmine
	CMStore store;
	//Array List to hold all of the parking lots
	ArrayList<ParkingLot> currentlots = new ArrayList<ParkingLot>();
	//List adapter for the parking lots
	private ExpandableListAdapter lotsadapter;
	//Variable to hold current parking lot
	private ParkingLot currentlot;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//SETUP CLOUDMINE ESSENTIAL INFORMATION
		CMApiCredentials.initialize(APP_ID, API_KEY, getApplicationContext());
		store = CMStore.getStore();
		//Initialize all of the necessary components for the app using the following methods
		initializeMain();
		initializeHourDialog();
		initializePriceDialog();
	}

	/**
	 * Override the onClick(View v) method of onClickListener interface.  This overridden method listens for any button clicks 
	 * and when a button is clicked the appropriate action is taken by utilizing a switch construct.
	 * @param View v: the view that was clicked
	 */
	@Override
	public void onClick(View v) {
		//switch that renders appropriate action when a specific button is clicked
		switch (v.getId()) {
		case R.id.btnnew:
			//Open the new parking lot layout
			initializeNewLot();
			break;
		case R.id.btnlookup:
			//Record all user input into strings and search for an appropriate destination at the specified latitude and longitude
			String lon = ((EditText) findViewById(R.id.txtsearchlongitude)).getText().toString();
			String lat = ((EditText) findViewById(R.id.txtsearchlatitude)).getText().toString();
			String miles = ((EditText) findViewById(R.id.txtmiles)).getText().toString();
			currentlots.clear();
			initializeResults();
			store.loadApplicationObjectsSearch("[location near (" + lon + ", " + lat + "), " + miles + "mi]",
					new CMObjectResponseCallback() {
						public void onCompletion(CMObjectResponse response) {
							for (CMObject object : response.getObjects())
								currentlots.add((ParkingLot) object);
							lotsadapter.notifyDataSetChanged();
						}
					});
			break;
		case R.id.btnlookupall:
			//Display all previously saved results from Cloudmine
			currentlots.clear();
			initializeResults();
			store.loadAllApplicationObjects(new CMObjectResponseCallback() {
				public void onCompletion(CMObjectResponse response) {
					for (CMObject object : response.getObjects())
						currentlots.add((ParkingLot) object);
					lotsadapter.notifyDataSetChanged();
				}
			});
			break;
		case R.id.btnlookuplocation:
			//Display the lookup location
			initializeLookupLocation();
			break;
		case R.id.btnadd:
			//Save parking lot into cloudmine
			saveParkingLot();
		case R.id.btnreturn:
			//Whenever the return button is pressed, forego all current user input and simple return to the main screen
			currentlot = null;
			initializeMain();
			break;
		case R.id.btnaddhour:
			//Display the add hour dialog box
			addHourDialog.show();
			break;
		case R.id.btnhouraccept:
			//Record the time that the user entered in the add hour dialog
			String day = ((Spinner) addHourDialog.findViewById(R.id.spinday)).getSelectedItem().toString();
			TimePicker timeopen = (TimePicker) addHourDialog.findViewById(R.id.timeopen);
			TimePicker timeclose = (TimePicker) addHourDialog.findViewById(R.id.timeclose);
			currenthours.add(new HourRange(day, timeopen.getCurrentHour(), timeopen.getCurrentMinute(), timeclose
					.getCurrentHour(), timeclose.getCurrentMinute()));
			houradapter.notifyDataSetChanged();
			Toast.makeText(this, "Hour Created", Toast.LENGTH_SHORT).show();
		case R.id.btnhourcancel:
			//Hide (not destroy) the current instance of the add hour dialog
			addHourDialog.hide();
			break;
		case R.id.btnaddprice:
			//Display the add price dialog box
			addPriceDialog.show();
			break;
		case R.id.btnpriceaccept:
			//Record the price that the user entered in the add price dialog
			EditText hour = (EditText) addPriceDialog.findViewById(R.id.txthour);
			EditText price = (EditText) addPriceDialog.findViewById(R.id.txtprice);
			currentprices.add(new PriceRange(Float.parseFloat(hour.getText().toString()), Float.parseFloat(price
					.getText().toString())));
			priceadapter.notifyDataSetChanged();
			Toast.makeText(this, "Price Created", Toast.LENGTH_SHORT).show();
		case R.id.btnpricecancel:
			//Hide (not destroy) the current instance of the add price dialog 
			addPriceDialog.hide();
			break;
		default:
			break;
		}
	}

	/**
	 * Override the onItemLongClick(AdapterView<?> parent, View v, int position, long id) method of the onItemLongClickListener 
	 * interface.  This overridden method listens for long clicks on specific list views to edit the contents of that list view.
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
		switch (parent.getId()) {
		case R.id.lstresults:
			initializeNewLot();
			currentlot = currentlots.get(position);
			((EditText) findViewById(R.id.txtname)).setText(currentlot.getName());
			((EditText) findViewById(R.id.txtlongitude)).setText(Double.toString(currentlot.getLocation()
					.getLongitude()));
			((EditText) findViewById(R.id.txtlatitude))
					.setText(Double.toString(currentlot.getLocation().getLatitude()));
			currenthours.addAll(currentlot.getHours());
			houradapter.notifyDataSetChanged();
			currentprices.addAll(currentlot.getPrices());
			priceadapter.notifyDataSetChanged();
			break;
		case R.id.lsthours:
			currenthours.remove(position);
			houradapter.notifyDataSetChanged();
			break;
		case R.id.lstprices:
			currentprices.remove(position);
			priceadapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * This method records the user's entered information and saves the data as a parking lot and saves the result into cloudmine.
	 */
	private void saveParkingLot() {
		if (currentlot != null)
			currentlot.delete();
		currentlot = null;
		//Parking lot name, longitude, and latitude that the user entered to be saved
		String name = ((EditText) findViewById(R.id.txtname)).getText().toString();
		double lon = Double.parseDouble(((EditText) findViewById(R.id.txtlongitude)).getText().toString());
		double lat = Double.parseDouble(((EditText) findViewById(R.id.txtlatitude)).getText().toString());
		//Save the object into cloudmine and display the appropriate result if parking lot save was successful or not
		new ParkingLot(name, lon, lat, currenthours, currentprices).save(new ObjectModificationResponseCallback() {
			public void onCompletion(ObjectModificationResponse response) {
				if (response.wasSuccess())
					Toast.makeText(ParkingActivity.this, "Parking Lot Saved", Toast.LENGTH_SHORT).show();
			}

			public void onFailure(Throwable e, String msg) {
				Log.v(TAG, "Failed to create parking lot", e);
			}
		});
	}

	/**
	 * This method initializes all content on the main layout for use when the user interacts with the views on this layout.
	 */
	private void initializeMain() {
		setContentView(R.layout.main);
		Button btnnew = (Button) findViewById(R.id.btnnew);
		btnnew.setOnClickListener(this);
		Button btnlookupall = (Button) findViewById(R.id.btnlookupall);
		btnlookupall.setOnClickListener(this);
		Button btnlookuplocation = (Button) findViewById(R.id.btnlookuplocation);
		btnlookuplocation.setOnClickListener(this);
	}

	/**
	 * This method initializes all content on the location layout for use when the user interacts with the views on this layout.
	 */
	private void initializeLookupLocation() {
		setContentView(R.layout.location);
		Button btnlookup = (Button) findViewById(R.id.btnlookup);
		btnlookup.setOnClickListener(this);
		Button btnreturn = (Button) findViewById(R.id.btnreturn);
		btnreturn.setOnClickListener(this);
	}
	
	/**
	 * This method initializes all content on the results layout for use when the user interacts with the views on this layout.
	 */
	private void initializeResults() {
		setContentView(R.layout.results);
		Button btnreturn = (Button) findViewById(R.id.btnreturn);
		btnreturn.setOnClickListener(this);
		ExpandableListView lots = (ExpandableListView) findViewById(R.id.lstresults);
		lotsadapter = new ExpandableListAdapter(R.layout.group, R.layout.child, currentlots, getLayoutInflater());
		lots.setAdapter(lotsadapter);
		lots.setOnItemLongClickListener(this);
	}

	/**
	 * This method initializes all content on the newlot layout for use when the user interacts with the views on this layout.
	 */
	private void initializeNewLot() {
		setContentView(R.layout.newlot);
		Button btnadd = (Button) findViewById(R.id.btnadd);
		btnadd.setOnClickListener(this);
		Button btnreturn = (Button) findViewById(R.id.btnreturn);
		btnreturn.setOnClickListener(this);
		Button btnaddhour = (Button) findViewById(R.id.btnaddhour);
		btnaddhour.setOnClickListener(this);
		Button btnaddprice = (Button) findViewById(R.id.btnaddprice);
		btnaddprice.setOnClickListener(this);
		currenthours.clear();
		ListView hours = (ListView) findViewById(R.id.lsthours);
		houradapter = new ArrayAdapter<HourRange>(this, android.R.layout.simple_list_item_1, currenthours);
		currentprices.clear();
		hours.setOnItemLongClickListener(this);
		hours.setAdapter(houradapter);
		ListView prices = (ListView) findViewById(R.id.lstprices);
		priceadapter = new ArrayAdapter<PriceRange>(this, android.R.layout.simple_list_item_1, currentprices);
		prices.setOnItemLongClickListener(this);
		prices.setAdapter(priceadapter);
	}

	/**
	 * This method initializes all content on the addhour (dialog) layout for use when the user interacts with the views on this 
	 * layout.
	 */
	public void initializeHourDialog() {
		addHourDialog = new Dialog(this);
		addHourDialog.setTitle("Add New Hour Range");
		addHourDialog.setContentView(R.layout.addhour);
		Spinner day = (Spinner) addHourDialog.findViewById(R.id.spinday);
		ArrayAdapter<HourRange.DAY> adapter = new ArrayAdapter<HourRange.DAY>(this,
				android.R.layout.simple_spinner_item, HourRange.DAY.values());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		day.setAdapter(adapter);
		TimePicker timeopen = (TimePicker) addHourDialog.findViewById(R.id.timeopen);
		timeopen.setCurrentHour(8);
		timeopen.setCurrentMinute(0);
		TimePicker timeclose = (TimePicker) addHourDialog.findViewById(R.id.timeclose);
		timeclose.setCurrentHour(17);
		timeclose.setCurrentMinute(0);
		Button btnhouraccept = (Button) addHourDialog.findViewById(R.id.btnhouraccept);
		btnhouraccept.setOnClickListener(this);
		Button btnhourcancel = (Button) addHourDialog.findViewById(R.id.btnhourcancel);
		btnhourcancel.setOnClickListener(this);
	}

	/**
	 * This method initializes all content on the addprice (dialog) layout for use when the user interacts with the views on this 
	 * layout.
	 */
	public void initializePriceDialog() {
		addPriceDialog = new Dialog(this);
		addPriceDialog.setTitle("Add New Price Range");
		addPriceDialog.setContentView(R.layout.addprice);
		Button btnhouraccept = (Button) addPriceDialog.findViewById(R.id.btnpriceaccept);
		btnhouraccept.setOnClickListener(this);
		Button btnhourcancel = (Button) addPriceDialog.findViewById(R.id.btnpricecancel);
		btnhourcancel.setOnClickListener(this);
	}

	/**
	 * This static field is needed for cloudmine use.
	 */
	static {
		ClassNameRegistry.register(ParkingLot.CLASS_NAME, ParkingLot.class);
	}
}
