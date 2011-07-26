package com.bozuko.bozuko;

import com.fuzz.android.globals.GlobalConstants;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

public class SocialMediaWebViewActivity extends InAppWebViewControllerActivity {

	public void onCreate(Bundle savedInstanceState) {
		Thread th = new Thread(){
			public void run(){
				removeCookies();
			}
		};
		th.start();
		
		super.onCreate(savedInstanceState);
		
		webview.setWebViewClient(new SocialWebClient());
	}
	
	public void onPause(){
		super.onPause();
		
	}
	
	public void removeCookies(){
		try{
			CookieSyncManager.createInstance(this);
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookie();
		}catch(Throwable t){
			
		}
	}
	
	protected class SocialWebClient extends CustomWebClient{

		public void onPageStarted (WebView view, String url, Bitmap favicon){
			super.onPageStarted(view, url, favicon);
			//Log.v("URL",url);
			if(url.startsWith(GlobalConstants.FACEBOOK_URL) || url.startsWith(GlobalConstants.FACEBOOK_URL2)){
				//Log.v("URL",url);
				String token = url.replace(GlobalConstants.FACEBOOK_URL, "");
				token = token.replace(GlobalConstants.FACEBOOK_URL2, "");
				//Log.v("TOKEN",token);
				SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(SocialMediaWebViewActivity.this);
				SharedPreferences.Editor edit = mprefs.edit();
				edit.putBoolean("facebook_login", true);
				edit.putBoolean("ReloadFavorites", true);
				edit.putBoolean("ReloadMap", true);
				edit.putBoolean("ReloadNearby", true);
				edit.putBoolean("ReloadPage", true);
				edit.putBoolean("ReloadPrizes", true);
				edit.putString("token", token);
				edit.commit();
				//TODO get user info
				((BozukoApplication)getApp()).getEntry();
				
				finish();
//				makeDialog("Welcome to Bozuko.","Success",new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						// TODO Auto-generated method stub
//						
//					}
//				});
			}
			if(url.startsWith("bozuko://webview.close")){
				SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(SocialMediaWebViewActivity.this);
				SharedPreferences.Editor edit = mprefs.edit();
				edit.putBoolean("ReloadPage", true);
				edit.commit();
				finish();
			}
		}
	}
}
