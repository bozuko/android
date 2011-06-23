package com.fuzz.android.socialmedia;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.OAuthServiceProvider;
import net.oauth.ParameterStyle;
import net.oauth.client.OAuthClient;
import net.oauth.client.OAuthResponseMessage;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.http.HttpResponseMessage;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

/**
 * SocialMediaHandler is twitter and facebook library that handles all aspects of logging in
 * logging out, posting to their websites and in the future other components of interest.
 * 
 * Dependencies
 * uses the following library http://code.google.com/p/oauth/
 * use Maven to compile library
 * one compiled jar required are 
 * Commons,HttpClient4,Consumer
 * 
 * @author cesaraguilar
 **/

public class SocialMediaHandler{
	/*
	 * Posts to main UI Thread
	 * Aways Create SocialMediaHandler in main ui Thread
	 */
	Handler mHandler = new Handler();

	/**
	 * @def kSocialMediaNotification_FacebookStatusUpdate
	 * This is the notification name that gets sent when a user has completed an attempt to update their
	 * Facebook status.
	 */
	public static final String kSocialMediaNotification_FacebookStatusUpdate = "FacebookStatusUpdate";

	/**
	 * @def kSocialMediaNotification_FacebookLike
	 * This is the notification name that gets sent when a facebook like has completed
	 * Both the userInfo and an NSNumber reperesenting success of the call will be sent.
	 */
	public static final String kSocialMediaNotification_FacebookLike = "FacebookLike";

	/**
	 * @def kSocialMediaNotification_TwitterStatusUpdate
	 * This is the notification name that gets sent when a user has completed an attempt to update their
	 * Twitter status.
	 */
	public static final String kSocialMediaNotification_TwitterStatusUpdate = "TwitterStatusUpdate";


	/**
	 * @def kSocialMedia_FacebookApiKey
	 * @def kSocialMedia_FacebookSecretKey
	 * Facebook API keys
	 **/
	private static final String kSocialMedia_FacebookApiKey = "793b4eff66e02f485cde24c5377e44c6";
	private static final String kSocialMedia_FacebookSecretKey = "bea1debe7e320118294c496b6df0784f";

	/**
	 * @def kSocialMedia_TwitterConsumerKey
	 * @def kSocialMedia_TwitterSecretKey
	 * Twitter API keys
	 **/
	private static final String kSocialMedia_TwitterConsumerKey = "l20WKUZg2Ky2Cv321k4r7A";
	private static final String kSocialMedia_TwitterSecretKey = "AsY0rGpMXnlH12uIbWvfb8M8GtkQ5eVVORYg6vZehS0";

	/**
	 * @def kSocialMedia_ServiceProviderName
	 * This is the url base for the redirect callbacks to the app.
	 * app name should be enough
	 * this app use SocialMediaHandler
	 */
	public static final String kSocialMedia_ServiceProviderName = "socialmediahandler";

	private static final String kSocialMedia_UserDefaultsTwitterTokenKey = "TwitterTokenKey";
	private static final String kSocialMedia_UserDefaultsFBTokenKey = "FacebookTokenKey";

	protected String fbAccessToken;
	protected String twitterAuthToken;
	protected String twitterTokenSecret;
	protected String twitterAccessToken;

	public SocialMediaHandler(Context context){
		init(context);

	}

	protected void init(Context mContext){
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		twitterAccessToken = mprefs.getString(kSocialMedia_UserDefaultsTwitterTokenKey, "");
		twitterTokenSecret = mprefs.getString(kSocialMedia_UserDefaultsTwitterTokenKey+"secret", "");
		fbAccessToken = mprefs.getString(kSocialMedia_UserDefaultsFBTokenKey, "");
	}

	/**
	 *Returns true if logged in false otherwise
	 **/
	public boolean isFacebookLoggedIn(){
		return (fbAccessToken.compareTo("") != 0);
	}

	/**
	 *Logs the user out of facebook
	 *
	 *@param mContext used to get an instance of the user preferences where tokens are saved
	 **/
	public void logoutFacebook(Context mContext){
		CookieSyncManager.createInstance(mContext);
		CookieSyncManager.getInstance().resetSync();
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor edit = mprefs.edit();
		edit.remove(kSocialMedia_UserDefaultsFBTokenKey);
		edit.commit();
		fbAccessToken = "";
	}

	/**
	 * brings up the SocialMediaWebVie if the user needs to be logged in
	 * @param mContext used for start the activity
	 */
	public void showFaceBookLogin(Context mContext){
		String url = String.format("https://graph.facebook.com/oauth/authorize?client_id=%s&redirect_uri=http://www.facebook.com/connect/login_success.html&display=touch&scope=publish_stream,offline_access", kSocialMedia_FacebookApiKey);
		Intent i = new Intent(mContext,SocialMediaWebView.class);
		i.putExtra(Intent.EXTRA_TITLE, "Facebook");
		i.setData(Uri.parse(url));
		mContext.startActivity(i);
	}

	/**
	 * get the users access token
	 * @param code the code return by authorize request
	 * @param mContext saves token to preference list
	 */
	public void requestFBAccessToken(String code,Context mContext){
		String url = String.format("https://graph.facebook.com/oauth/access_token?client_id=%s&redirect_uri=http://www.facebook.com/connect/login_success.html&client_secret=%s&code=%s", kSocialMedia_FacebookApiKey,kSocialMedia_FacebookSecretKey,code);
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
			String arr[] = resp.split("&");
			for(String s : arr){
				String temp[] = s.split("=");
				if(temp[0].compareTo("access_token")==0){
					fbAccessToken = temp[1];
				}
			}
		}catch(Throwable t){

		}

		if(fbAccessToken.compareTo("") != 0){
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			SharedPreferences.Editor edit = mprefs.edit();
			edit.putString(kSocialMedia_UserDefaultsFBTokenKey, fbAccessToken);
			edit.commit();
		}
	}

	/**
	 * Post a new update to facebook can take in attachments and photos as well
	 * @param mContext toast message
	 * @param newStatus
	 * @param pic
	 * @param link
	 * @param attachName
	 * @param attachDesc
	 */
	public void updateFBStatus(final Context mContext,String newStatus, String pic, String link, String attachName, String attachDesc){
		if(!isFacebookLoggedIn() || newStatus == null){
			return;
		}
		String url = String.format("https://graph.facebook.com/me/feed?access_token=%s", fbAccessToken);
		try{
			HashMap<String,String> params = new HashMap<String,String>();
			URL u= new URL(url);
			final HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			
			//final HttpRequest req = new HttpRequest(new URL(url));
			conn.setRequestMethod("POST");

			params.put("message",newStatus);
			if(pic != null)
				params.put("picture", pic);
			if(link != null)
				params.put("link", link);
			if(attachName != null)
				params.put("name", attachName);
			if(attachDesc != null)
				params.put("description", attachDesc);
			
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
					fetchPost(conn,n,mContext);
				}
			};
			th.start();
		}catch(Throwable t){
			createToast(mContext,"Facebook post failed. Try Again.");
		}
	}

	
	private void fetchPost(HttpURLConnection req,String n,Context mContext){
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
				ToastRunnable run = new ToastRunnable(mContext,"Facebook post success.");
				mHandler.post(run);
				return;
			}
			throw new Exception("Failed to post");
		}catch(Throwable e){
			ToastRunnable run = new ToastRunnable(mContext,"Facebook post failed. Try Again");
			mHandler.post(run);
		}
	}

	/**
	 * likes the fb item passed in
	 * @param mContext toast messages
	 * @param itemID
	 * @param userinfo
	 */
	public void likeFBItem(final Context mContext, String itemID, HashMap<String,String>userinfo){
		if(!isFacebookLoggedIn()){
			return;
		}
		String url = String.format("https://graph.facebook.com/%s/likes?access_token=%s", itemID,fbAccessToken);
		try{
			//final HttpRequest req = new HttpRequest(new URL(url));
			URL u= new URL(url);
			final HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setRequestMethod("POST");
			Thread th = new Thread(){
				public void run(){
					fetchRequest(conn,mContext);
				}
			};
			th.start();
		}catch(Throwable t){
			createToast(mContext,"Failed to like item. Try Again");
		}
	}

	private void fetchRequest(HttpURLConnection req, Context mContext){
		try{
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
				ToastRunnable run = new ToastRunnable(mContext,"Success. You know like this item");
				mHandler.post(run);
				return;
			}
			throw new Exception("Failed");
		}catch(Throwable t){
			ToastRunnable run = new ToastRunnable(mContext,"Failed to like item. Try Again");
			mHandler.post(run);
		}
	}

	protected void createToast(Context mContext,String message){
		Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
		toast.show();
	}

	/**
	 *@def isTwitterLoggedIn()
	 *Returns true if logged in false otherwise
	 **/
	public boolean isTwitterLoggedIn(){
		return (twitterAccessToken.compareTo("")!=0);
	}

	/**
	 *Logs the user out of twitter
	 *
	 *@param mContext used to get an instance of the user preferences where tokens are saved
	 **/
	public void logoutTwitter(Context mContext){
		CookieSyncManager.createInstance(mContext);
		CookieSyncManager.getInstance().resetSync();
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor edit = mprefs.edit();
		edit.remove(kSocialMedia_UserDefaultsTwitterTokenKey);
		edit.remove(kSocialMedia_UserDefaultsTwitterTokenKey + "Secret");
		edit.commit();
		twitterAccessToken = "";
	}

	/**
	 * brings up the SocialMediaWebVie if the user needs to be logged in
	 * @param mContext used for start the activity
	 */
	public void showTwitterLogin(Context mContext){
		Intent i = new Intent(mContext,SocialMediaWebView.class);
		i.putExtra(Intent.EXTRA_TITLE, "Twitter");
		mContext.startActivity(i);
	}

	/**
	 * request the twitter auth token for authorization
	 * @return
	 */
	public OAuthAccessor requestTwitterAuthToken(){
		OAuthConsumer consumer = new OAuthConsumer(kSocialMedia_ServiceProviderName // callback url
				, kSocialMedia_TwitterConsumerKey // consumer key
				, kSocialMedia_TwitterSecretKey // consumer secret
				, new OAuthServiceProvider( //
						"http://twitter.com/oauth/request_token", //
						"http://twitter.com/oauth/authorize", //
				"http://twitter.com/oauth/access_token"));
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		OAuthClient client = new OAuthClient(new HttpClient4());
		try { 
			client.getRequestToken(accessor);
			twitterAuthToken = accessor.requestToken;
			twitterTokenSecret = accessor.tokenSecret;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return accessor;
	}

	/**
	 * request the users access token
	 * @param oauthverifer as returned by twitter
	 * @param mContext
	 */
	public void requestTwitterAccessToken(String oauthverifer,Context mContext){
		OAuthConsumer consumer = new OAuthConsumer(kSocialMedia_ServiceProviderName // callback url
				, kSocialMedia_TwitterConsumerKey // consumer key
				, kSocialMedia_TwitterSecretKey // consumer secret
				, new OAuthServiceProvider( //
						"http://twitter.com/oauth/request_token", //
						"http://twitter.com/oauth/authorize", //
						"http://twitter.com/oauth/access_token"));
		
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		accessor.tokenSecret = twitterTokenSecret;
		accessor.requestToken = twitterAuthToken;
		OAuthClient client = new OAuthClient(new HttpClient4());
		ArrayList<Map.Entry<String, String>> params = new ArrayList<Map.Entry<String, String>>();
		params.add(new OAuth.Parameter("oauth_verifier", oauthverifer));
		try {
			OAuthMessage msg = client.getAccessToken(accessor, "GET", params);
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			SharedPreferences.Editor edit = mprefs.edit();
			twitterAccessToken = msg.getParameter("oauth_token");
			twitterTokenSecret = msg.getParameter("oauth_token_secret");
			edit.putString(kSocialMedia_UserDefaultsTwitterTokenKey,twitterAccessToken);
			edit.putString(kSocialMedia_UserDefaultsTwitterTokenKey+"secret", twitterTokenSecret);
			edit.commit();
		}catch (Throwable e){
			e.printStackTrace();
		}
	}
	
	/**
	 * updates the user twitter feed
	 * @param mContext toast messages
	 * @param newStatus
	 */
	public void updateTwitterStatus(final Context mContext,final String newStatus){
		Thread th = new Thread(){
			public void run(){
				fetchTwitterUpdate(mContext, newStatus);
			}
		};
		th.start();
		
	}
	
	private void fetchTwitterUpdate(Context mContext, String newStatus){
		OAuthConsumer consumer = new OAuthConsumer(null // callback url
				, kSocialMedia_TwitterConsumerKey // consumer key
				, kSocialMedia_TwitterSecretKey // consumer secret
				, null);
		
		OAuthAccessor accessor = new OAuthAccessor(consumer);
		accessor.accessToken = twitterAccessToken;
		accessor.tokenSecret = twitterTokenSecret;
		OAuthClient client = new OAuthClient(new HttpClient4());
		
		ArrayList<Map.Entry<String, String>> params = new ArrayList<Map.Entry<String, String>>();
		params.add(new OAuth.Parameter("status", newStatus));
		try{
			OAuthResponseMessage m = client.access(accessor.newRequestMessage(OAuthMessage.POST,
                    "http://api.twitter.com/1/statuses/update.xml", params), ParameterStyle.AUTHORIZATION_HEADER);
			int status = m.getHttpResponse().getStatusCode();
			if (status != HttpResponseMessage.STATUS_OK) {
                OAuthProblemException problem = m.toOAuthProblemException();
                if (problem.getProblem() != null) {
                    throw problem;
                }  
                ToastRunnable run = new ToastRunnable(mContext,"Twitter update failed. Try Again");
    			mHandler.post(run);
            }else{
            	ToastRunnable run = new ToastRunnable(mContext,"Twitter update success.");
            	mHandler.post(run);
            }
		}catch(Throwable t){
			//t.printStackTrace();
			ToastRunnable run = new ToastRunnable(mContext,"Twitter update failed. Try Again");
			mHandler.post(run);
		}
	}

	/**
	 * Toast Runnable a runnable class that displays a toast message after keyevents in the posting of facebook and twitter events
	 * run in foreground instead of background
	 * @author cesaraguilar
	 *
	 */
	class ToastRunnable implements Runnable{
		Context mContext;
		String message;
		public ToastRunnable(Context context,String text){
			mContext = context;
			message = text;
		}

		public void run(){
			createToast(mContext,message);
		}
	}

	protected static String encodeURIcomponent(String s)
	{
	    StringBuilder o = new StringBuilder();
	    for (char ch : s.toCharArray()) {
	        if (isUnsafe(ch)) {
	            o.append('%');
	            o.append(toHex(ch / 16));
	            o.append(toHex(ch % 16));
	        }
	        else o.append(ch);
	    }
	    return o.toString();
	}

	private static char toHex(int ch)
	{
	    return (char)(ch < 10 ? '0' + ch : 'A' + ch - 10);
	}

	private static boolean isUnsafe(char ch)
	{
	    if (ch > 128 || ch < 0)
	        return true;
	    return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
	}
}


