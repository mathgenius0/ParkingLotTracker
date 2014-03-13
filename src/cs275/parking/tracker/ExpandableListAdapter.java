/**
 * Custom class that represents an expandable list adapter.  This is used ONLY for processing results and is ONLY used for user-
 * friendly formatting purposes.
 */

package cs275.parking.tracker;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

	private int _grouplayout;
	private int _childlayout;
	private ArrayList<ParkingLot> _data;
	private LayoutInflater _inflater;

	public ExpandableListAdapter(int groupid, int childid, ArrayList<ParkingLot> trains, LayoutInflater inflater) {
		_grouplayout = groupid;
		_childlayout = childid;
		_data = trains;
		_inflater = inflater;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		//set the fields in my custom layout using the values from the current train object
		View rowView = _inflater.inflate(_grouplayout, parent, false);
		((TextView) rowView.findViewById(R.id.txtresultsname)).setText(_data.get(groupPosition).getName());
		((TextView) rowView.findViewById(R.id.txtresultslon)).setText(Double.toString(_data.get(groupPosition).getLocation().getLongitude()));
		((TextView) rowView.findViewById(R.id.txtresultslat)).setText(Double.toString(_data.get(groupPosition).getLocation().getLatitude()));
		return rowView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		//set the fields in my custom layout using the values from the max traininfo object
		View rowView = _inflater.inflate(_childlayout, parent, false);
		((TextView) rowView.findViewById(R.id.txtresultshours)).setText(_data.get(groupPosition).hoursString());
		((TextView) rowView.findViewById(R.id.txtresultsprices)).setText(_data.get(groupPosition).pricesString());
		return rowView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		//always display only 1 train info for each train
		return 1;
	}

	@Override
	public int getGroupCount() {
		//number of trains that have
		return _data.size();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		//unused
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		//unused
		return 0;
	}

	@Override
	public Object getGroup(int groupPosition) {
		//unused
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		//unused
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		//unused
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		//unused
		return false;
	}

}
