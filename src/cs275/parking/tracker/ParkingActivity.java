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

	private static final String TAG = "cloudmine";
	private Dialog addHourDialog;
	private Dialog addPriceDialog;
	ArrayList<HourRange> currenthours = new ArrayList<HourRange>();
	ArrayList<PriceRange> currentprices = new ArrayList<PriceRange>();
	private ArrayAdapter<HourRange> houradapter;
	private ArrayAdapter<PriceRange> priceadapter;
	CMStore store;
	ArrayList<ParkingLot> currentlots = new ArrayList<ParkingLot>();
	private ExpandableListAdapter lotsadapter;
	private ParkingLot currentlot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CMApiCredentials.initialize(APP_ID, API_KEY, getApplicationContext());
		store = CMStore.getStore();
		initializeMain();
		initializeHourDialog();
		initializePriceDialog();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnnew:
			initializeNewLot();
			break;
		case R.id.btnlookup:
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
			initializeLookupLocation();
			break;
		case R.id.btnadd:
			saveParkingLot();
		case R.id.btnreturn:
			currentlot = null;
			initializeMain();
			break;
		case R.id.btnaddhour:
			addHourDialog.show();
			break;
		case R.id.btnhouraccept:
			String day = ((Spinner) addHourDialog.findViewById(R.id.spinday)).getSelectedItem().toString();
			TimePicker timeopen = (TimePicker) addHourDialog.findViewById(R.id.timeopen);
			TimePicker timeclose = (TimePicker) addHourDialog.findViewById(R.id.timeclose);
			currenthours.add(new HourRange(day, timeopen.getCurrentHour(), timeopen.getCurrentMinute(), timeclose
					.getCurrentHour(), timeclose.getCurrentMinute()));
			houradapter.notifyDataSetChanged();
			Toast.makeText(this, "Hour Created", Toast.LENGTH_SHORT).show();
		case R.id.btnhourcancel:
			addHourDialog.hide();
			break;
		case R.id.btnaddprice:
			addPriceDialog.show();
			break;
		case R.id.btnpriceaccept:
			EditText hour = (EditText) addPriceDialog.findViewById(R.id.txthour);
			EditText price = (EditText) addPriceDialog.findViewById(R.id.txtprice);
			currentprices.add(new PriceRange(Float.parseFloat(hour.getText().toString()), Float.parseFloat(price
					.getText().toString())));
			priceadapter.notifyDataSetChanged();
			Toast.makeText(this, "Price Created", Toast.LENGTH_SHORT).show();
		case R.id.btnpricecancel:
			addPriceDialog.hide();
			break;
		default:
			break;
		}
	}

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

	private void saveParkingLot() {
		if (currentlot != null)
			currentlot.delete();
		currentlot = null;
		String name = ((EditText) findViewById(R.id.txtname)).getText().toString();
		double lon = Double.parseDouble(((EditText) findViewById(R.id.txtlongitude)).getText().toString());
		double lat = Double.parseDouble(((EditText) findViewById(R.id.txtlatitude)).getText().toString());
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

	private void initializeMain() {
		setContentView(R.layout.main);
		Button btnnew = (Button) findViewById(R.id.btnnew);
		btnnew.setOnClickListener(this);
		Button btnlookupall = (Button) findViewById(R.id.btnlookupall);
		btnlookupall.setOnClickListener(this);
		Button btnlookuplocation = (Button) findViewById(R.id.btnlookuplocation);
		btnlookuplocation.setOnClickListener(this);
	}

	private void initializeLookupLocation() {
		setContentView(R.layout.location);
		Button btnlookup = (Button) findViewById(R.id.btnlookup);
		btnlookup.setOnClickListener(this);
		Button btnreturn = (Button) findViewById(R.id.btnreturn);
		btnreturn.setOnClickListener(this);
	}

	private void initializeResults() {
		setContentView(R.layout.results);
		Button btnreturn = (Button) findViewById(R.id.btnreturn);
		btnreturn.setOnClickListener(this);
		ExpandableListView lots = (ExpandableListView) findViewById(R.id.lstresults);
		lotsadapter = new ExpandableListAdapter(R.layout.group, R.layout.child, currentlots, getLayoutInflater());
		lots.setAdapter(lotsadapter);
		lots.setOnItemLongClickListener(this);
	}

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

	public void initializePriceDialog() {
		addPriceDialog = new Dialog(this);
		addPriceDialog.setTitle("Add New Price Range");
		addPriceDialog.setContentView(R.layout.addprice);
		Button btnhouraccept = (Button) addPriceDialog.findViewById(R.id.btnpriceaccept);
		btnhouraccept.setOnClickListener(this);
		Button btnhourcancel = (Button) addPriceDialog.findViewById(R.id.btnpricecancel);
		btnhourcancel.setOnClickListener(this);
	}

	static {
		ClassNameRegistry.register(ParkingLot.CLASS_NAME, ParkingLot.class);
	}
}
