package com.fuzz.android.datahandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;

import com.fuzz.android.globals.GlobalEnum;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {

	protected static String DB_PATH = "/data/data/%s/databases/";

	protected static String TEMP_NAME = "NCMUberApp.sqlite";
	protected static String DB_NAME = "NCMUberApp.sqlite";

	protected SQLiteDatabase myDB;
	protected boolean done = false;

	protected Context myContext;

	public DataBaseHelper(Context context) {

		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}

	public void setContext(Context b) {
		this.myContext = b;
	}

	public void createDataBase() throws IOException {

		boolean dbExist = checkDataBase();

		if (dbExist) {
			// do nothing - database already exist
		} else {

			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are gonna be able to overwrite that
			// database with our database.
			this.getReadableDatabase();

			try {

				copyDataBase();
				//Log.v("DBHelper", "Copied");
			} catch (Throwable e) {
				//e.printStackTrace();
				throw new Error("Error copying database");

			}
		}

	}

	private boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {
			String myPath = String.format(DB_PATH, GlobalEnum.PACKAGE_NAME) + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);

		} catch (SQLiteException e) {

			// database does't exist yet.

		}

		if (checkDB != null) {

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	private void copyDataBase() throws IOException {

		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(TEMP_NAME);

		// Path to the just created empty db
		String outFileName = String.format(DB_PATH, GlobalEnum.PACKAGE_NAME) + DB_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	public synchronized void openDataBase() throws SQLException {

		// Open the database
		if (myDB == null) {
			try{
				String myPath = String.format(DB_PATH, GlobalEnum.PACKAGE_NAME) + DB_NAME;
				myDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE);
			}catch(SQLException e){
				myDB = getWritableDatabase();
			}
			
			if(myDB == null){
				myDB = getWritableDatabase();
			}
		}

	}

	@Override
	public synchronized void close() {

		if (myDB != null){
			myDB.close();
			myDB = null;
		}
		super.close();
	}

	public boolean isOpen() {
		// TODO Auto-generated method stub
		if (myDB == null) {
			return false;
		}

		return myDB.isOpen();
	}

	public SQLiteDatabase getDB() {
		// TODO Auto-generated method stub
		return myDB;
	}

	public void addTable() {
		//		myDB.beginTransaction();
		try {
			String sql = "CREATE TABLE appstate(name text, value text, td int)";
			myDB.execSQL(sql);
		} catch (SQLiteException e) {
			//e.printStackTrace();
		}
		//		myDB.setTransactionSuccessful();
		//		myDB.endTransaction();
	}

	public void setAppStateForDevice() {
		//Log.v("DB Helper", "Insert into appstate");
		//		myDB.beginTransaction();
		TelephonyManager mTelephonyMgr = (TelephonyManager) myContext
		.getSystemService(Context.TELEPHONY_SERVICE);
		String sql = "INSERT INTO appstate (name,value,td) VALUES ('deviceToken', '"
			+ mTelephonyMgr.getDeviceId() + "', 1)";
		//Log.v("DB Helper", sql);
		myDB.execSQL(sql);
		//		myDB.setTransactionSuccessful();
		//		myDB.endTransaction();
	}

	public Hashtable<String, String> getAppStateForKey(String string) {
		Hashtable<String, String> map = new Hashtable<String, String>();
		//		myDB.beginTransaction();
		String sql = "select name,value,td from appstate where name='" + string
		+ "' order by td desc limit 1";
		try {
			Cursor r = myDB.rawQuery(sql, null);
			while (r.moveToNext()) {
				//Log.v("Return SQL ", r.toString());
				map.put("name", r.getString(0));
				map.put("value", r.getString(1));
				map.put("td", String.valueOf(r.getInt(2)));
			}
			r.close();
		} catch (SQLiteException e) {

		}
		//		myDB.endTransaction();
		return map;
	}

	public static boolean isImageCached(String path, Context con) {
		// boolean cached = false;

		//		String file = localPathForUrl(path);
		File f = new File(path);
		// con.openFileInput(path);
		// AssetManager assets = con.getAssets();
		// InputStream i = assets.open(path);
		return f.exists();
	}

	public static String localPathForUrl(String url) {
		if (url == null) {
			return null;
		}
		String ar[] = url.split(".");
		String ext = "jpg";
		if (ar.length > 0) {

			ext = ar[ar.length - 1];
		}
		String p = "/data/data/com.ncm.movienight/cache/" + md5(url) + ext;
		// DebugLog(@"%@", p);
		return p;
	}

	public static String md5(String s) {
		// TODO Auto-generated method stub
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
			.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			//e.printStackTrace();
		}
		return "";
	}
	
	public static String sha1(String s) {
		// TODO Auto-generated method stub
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest
			.getInstance("SHA1");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++){
				//hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
				  int halfbyte = (messageDigest[i] >>> 4) & 0x0F;
		            int two_halfs = 0;
		            do { 
		                if ((0 <= halfbyte) && (halfbyte <= 9)) 
		                	hexString.append((char) ('0' + halfbyte));
		                else 
		                	hexString.append((char) ('a' + (halfbyte - 10)));
		                halfbyte = messageDigest[i] & 0x0F;
		            } while(two_halfs++ < 1);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			//e.printStackTrace();
		}
		return "";
	}

	public static String localFileForUrl(String url2) {
		// TODO Auto-generated method stub
		if (url2 == null) {
			return null;
		}
		String ar[] = url2.split(".");
		String ext = "jpg";
		if (ar.length > 0) {

			ext = ar[ar.length - 1];
		}
		String p = md5(url2) + ext;
		// DebugLog(@"%@", p);
		return p;
	}
	
	
	
	public static boolean isOnline(Context context,int currentTimeout) {
		ConnectivityManager cm = (ConnectivityManager) context
		.getSystemService(Context.CONNECTIVITY_SERVICE);
		//Log.v("database", cm.toString());
		NetworkInfo ni = cm.getActiveNetworkInfo();
		
		if (ni == null){
			if(currentTimeout > 20000){
				return false;
			}
			
			try {
				Thread.sleep(2000);
				return isOnline(context,currentTimeout+2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			return false;
		}
		return ni.isConnected() || ni.isConnectedOrConnecting();

	}

	public void eraseTable(String table) {
		// TODO Auto-generated method stub
		String sql = "delete from " + table;
		try{
			getDB().execSQL(sql);
		}catch(Throwable t){
			
		}
	}
	
	public void deleteDatabase() {
		// TODO Auto-generated method stub
		File dbFile = new File(DB_PATH + DB_NAME);
		try{
			dbFile.delete();
		}catch(Throwable t){
			
		}
	}
}
