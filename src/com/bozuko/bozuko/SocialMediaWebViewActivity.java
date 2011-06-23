package com.bozuko.bozuko;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

public class SocialMediaWebViewActivity extends InAppWebViewControllerActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		webview.setWebViewClient(new SocialWebClient());
	}
	
	public void onPause(){
		super.onPause();
		Thread th = new Thread(){
			public void run(){
				removeCookies();
			}
		};
		th.start();
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
			if(url.startsWith("https://bonobo.bozuko.com:8005/user?token=")){
				Log.v("URL",url);
				String token = url.replace("https://bonobo.bozuko.com:8005/user?token=", "");
				Log.v("TOKEN",token);
				SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(SocialMediaWebViewActivity.this);
				SharedPreferences.Editor edit = mprefs.edit();
				edit.putBoolean("facebook_login", true);
				edit.putBoolean("ReloadFavorites", true);
				edit.putBoolean("ReloadMap", true);
				edit.putBoolean("ReloadNearby", true);
				edit.putString("token", token);
				edit.commit();
				//TODO get user info
				((BozukoApplication)getApp()).getEntry();
				
				makeDialog("Welcome to Bozuko.","Success",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				});
			}
		}
	}
}
