package com.fuzz.android.socialmedia;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import org.json.JSONObject;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.webkit.CookieSyncManager;

public class FourSquareHandler extends SocialMediaHandler{

	private static final String kSocialMedia_UserDefaultsFSTokenKey = "FourSquareTokenKey";
	
	private static final String kSocialMedia_FourSquareApiKey = "J3X1CORNB0W4FAQXKTKPT444PIH333J1TDOAABQ4OXWGHNLP";
	private static final String kSocialMedia_FourSquareSecretKey = "ZTCBSBZJAZ1AW5TQGISAGD15MJHS2ASTBX4G4USE1MW0XMWV";
	
	/**
	 * @def kSocialMedia_ServiceProviderName
	 * This is the url base for the redirect callbacks to the app.
	 * app name should be enough
	 * this app use SocialMediaHandler
	 */
	public static final String kFourSquare_ServiceProviderName = "http://www.fuzzproductions.com/foursquarehandler";
	
	protected String FSquareToken;
	
	public FourSquareHandler(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void init(Context mContext){
		super.init(mContext);
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		FSquareToken = mprefs.getString(kSocialMedia_UserDefaultsFSTokenKey, "");
	}

	/**
	 *checks if user is logged into four square
	 * 
	 *Returns true if logged in false otherwise
	 **/
	public boolean isFourSquareLoggedIn(){
		return (FSquareToken.compareTo("") != 0);
	}
	
	/**
	 *Logs the user out of four square
	 *
	 *@param mContext used to get an instance of the user preferences where tokens are saved
	 **/
	public void logoutFourSquare(Context mContext){
		CookieSyncManager.createInstance(mContext);
		CookieSyncManager.getInstance().resetSync();
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor edit = mprefs.edit();
		edit.remove(kSocialMedia_UserDefaultsFSTokenKey);
		edit.commit();
		FSquareToken = "";
	}
	
	/**
	 * brings up the SocialMediaWebVie if the user needs to be logged in
	 * @param mContext used for start the activity
	 */
	public void showFourSquareLogin(Context mContext){
		String url = String.format("https://foursquare.com/oauth2/authenticate?client_id=%s&redirect_uri=%s&display=touch&response_type=code", kSocialMedia_FourSquareApiKey,kFourSquare_ServiceProviderName);
		Intent i = new Intent(mContext,SocialMediaWebView.class);
		i.putExtra(Intent.EXTRA_TITLE, "FourSquare");
		i.setData(Uri.parse(url));
		mContext.startActivity(i);
	}

	/**
	 * get the users access token
	 * @param code the code return by authorize request
	 * @param mContext saves token to preference list
	 */
	public void requestFSquareAccessToken(String code, Context mContext) {
		// TODO Auto-generated method stub
		String url = String.format("https://foursquare.com/oauth2/access_token?client_id=%s&redirect_uri=%s&client_secret=%s&code=%s&grant_type=authorization_code&display=touch", kSocialMedia_FourSquareApiKey,kFourSquare_ServiceProviderName,kSocialMedia_FourSquareSecretKey,code);
		try{
			URL u= new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			InputStream is = conn.getInputStream();
			if(is == null){
			}
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer(); 
			while((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\n');
			}
			rd.close();
			conn.disconnect();
			String resp = response.toString();
			JSONObject json = new JSONObject(resp);
			FSquareToken = json.getString("access_token");
		}catch(Throwable t){

		}

		if(FSquareToken.compareTo("") != 0){
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			SharedPreferences.Editor edit = mprefs.edit();
			edit.putString(kSocialMedia_UserDefaultsFSTokenKey, FSquareToken);
			edit.commit();
		}
	}
	
	/**
	 * Post a new update to facebook can take in attachments and photos as well
	 * @param mContext toast message
	 * @param venue
	 * @param venueId
	 * @param shout
	 * @param location
	 */
	public void checkIn(final Context mContext,String venue, String venueId,String shout, String ll, String llAcc, String alt, String altAcc){
		if(!isFourSquareLoggedIn() || (venue == null && venueId == null )){
			return;
		}
		String url = String.format("https://api.foursquare.com/v2/checkins/add?oauth_token=%s", FSquareToken);
		try{
			HashMap<String,String> params = new HashMap<String,String>();
			URL u= new URL(url);
			final HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			
			//final HttpRequest req = new HttpRequest(new URL(url));
			conn.setRequestMethod("POST");
			params.put("broadcast", "public");
			
			if(venue != null)
				params.put("venue", venue);
			if(venueId != null)
				params.put("venueId", venueId);
			if(shout != null)
				params.put("shout", shout);
			if(ll != null)
				params.put("ll", ll);
			if(llAcc != null)
				params.put("llAcc", llAcc);
			if(alt != null)
				params.put("alt", alt);
			if(altAcc != null)
				params.put("altAcc", altAcc);
			
			String par = "";
			int position = 1;
			for(String key : params.keySet()){
				par += encodeURIcomponent(key) + "=" + encodeURIcomponent(params.get(key));
				if(position < params.size()){
					par += "&";
				}
				position++;
			}
			final String n = par;
			byte[] data = null;
			if(conn.getRequestMethod().compareTo("POST")==0){
				try {
					data = par.getBytes("US-ASCII");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			if(data != null){
				conn.setRequestProperty("Content-Length", "" + Integer.toString(data.length));
				conn.setDoOutput(true);
			}	
			conn.setRequestProperty("Content-Language", "en-US");
		    conn.setRequestProperty("User-Agent", "Mobile/Safari");
		    conn.setUseCaches (false);
		    conn.setDoInput(true);
		    
		    
			Thread th = new Thread(){
				public void run(){
					fetchCheckin(conn,n,mContext);
				}
			};
			th.start();
		}catch(Throwable t){
			t.printStackTrace();
			createToast(mContext,"FourSqaure checkin failed. Try Again.");
		}
	}
	
	private void fetchCheckin(HttpURLConnection req,String n,Context mContext){
		try{
			OutputStreamWriter wr = new OutputStreamWriter(req.getOutputStream());
			if(wr == null){
			}
			wr.write(n);
			wr.flush();
			wr.close();
			
			InputStream is = req.getInputStream();
			if(is == null){
			}
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer(); 
			while((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\n');
			}
			rd.close();
			req.disconnect();
			String res = response.toString();
			if(res.compareTo("Failed")!=0){
				ToastRunnable run = new ToastRunnable(mContext,"FourSquare checkin success.");
				mHandler.post(run);
				return;
			}
			throw new Exception("Failed to post");
		}catch(Throwable e){
			e.printStackTrace();
			ToastRunnable run = new ToastRunnable(mContext,"FourSquare checkin failed. Try Again");
			mHandler.post(run);
		}
	}
}
