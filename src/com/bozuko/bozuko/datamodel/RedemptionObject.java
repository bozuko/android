package com.bozuko.bozuko.datamodel;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.fuzz.android.datahandler.DataObject;

public class RedemptionObject extends DataObject {

	public RedemptionObject(JSONObject json){
		super(json);
		queryid = "redemptionid";
		tablename = "redemption";
	}

	public RedemptionObject(String string) {
		// TODO Auto-generated constructor stub
		super();
		map.put("redemptionid", string);
		queryid = "redemptionid";
		tablename = "redemption";
	}

	@SuppressWarnings("unchecked")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public RedemptionObject createFromParcel(Parcel in) {
			return new RedemptionObject(in);
		}

		public RedemptionObject[] newArray(int size) {
			return new RedemptionObject[size];
		}
	};

	protected RedemptionObject(Parcel in) {
		super(in);
		queryid = "redemptionid";
		tablename = "redemption";
	}
}
