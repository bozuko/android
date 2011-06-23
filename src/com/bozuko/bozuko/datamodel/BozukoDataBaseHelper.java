package com.bozuko.bozuko.datamodel;

import android.content.Context;
import com.fuzz.android.datahandler.DataBaseHelper;

public class BozukoDataBaseHelper extends DataBaseHelper{

	private static BozukoDataBaseHelper dbh;
	
	public static BozukoDataBaseHelper getSharedInstance(Context mContext){
		if(dbh == null){
			dbh = new BozukoDataBaseHelper(mContext);
			dbh.openDataBase();
		}
		return dbh;
	}
	
	public static void closeSharedInstance(){
		if(dbh != null){
			dbh.close();
			dbh = null;
		}
	}
	
	public BozukoDataBaseHelper(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public static String distance(double lat1, double lat2, double lon1,double lon2) {
		double R = 6371; // km
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		lon1 = Math.toRadians(lon1);
		lon2 = Math.toRadians(lon2);
		double dLat = (lat2 - lat1);
		double dLon = (lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1)
		* Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = R * c;

		java.text.DecimalFormat df = new java.text.DecimalFormat("###.##");

		String ret = df.format(d) + " miles from ";
		return ret;
	}
	
	public static double distanceAsDouble(double lat1, double lat2, double lon1, double lon2) {
		double R = 6371; // km
		lat1 = Math.toRadians(lat1);
		lat2 = Math.toRadians(lat2);
		lon1 = Math.toRadians(lon1);
		lon2 = Math.toRadians(lon2);
		double dLat = (lat2 - lat1);
		double dLon = (lon2 - lon1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1)
		* Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = R * c;

		java.text.DecimalFormat df = new java.text.DecimalFormat("###.##");
		return Double.valueOf(df.format(d));
	}

}
