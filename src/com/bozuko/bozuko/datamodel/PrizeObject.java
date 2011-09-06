package com.bozuko.bozuko.datamodel;

import org.codehaus.jackson.JsonParser;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.fuzz.android.datahandler.DataObject;

public class PrizeObject extends DataObject {

	public PrizeObject(JSONObject json){
		super(json);
		queryid = "gameid";
		tablename = "prizes";
	}

	public PrizeObject(String string) {
		// TODO Auto-generated constructor stub
		super();
		map.put("gameid", string);
		queryid = "gameid";
		tablename = "prizes";
	}

	@SuppressWarnings("unchecked")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public PrizeObject createFromParcel(Parcel in) {
			return new PrizeObject(in);
		}

		public PrizeObject[] newArray(int size) {
			return new PrizeObject[size];
		}
	};

	protected PrizeObject(Parcel in) {
		super(in);
		queryid = "gameid";
		tablename = "prizes";
	}

	
	public PrizeObject(JsonParser jp) {
		// TODO Auto-generated constructor stub
		super(jp);
		queryid = "gameid";
		tablename = "prizes";
	}

	public PrizeObject(Cursor r) {
		super(r);
		queryid = "gameid";
		tablename = "prizes";
		// TODO Auto-generated constructor stub
	}
}
