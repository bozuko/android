package com.bozuko.bozuko;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.fuzz.android.globals.GlobalConstants;

public class SocialMediaWebViewActivity extends InAppWebViewControllerActivity {

	public void onCreate(Bundle savedInstanceState) {
		removeCookies();
		
		super.onCreate(savedInstanceState);
		CookieSyncManager.createInstance(this);
		CookieManager cookieMonster = CookieManager.getInstance();
		cookieMonster.setAcceptCookie(true);
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
			if(url.startsWith(GlobalConstants.FACEBOOK_URL) || url.startsWith(GlobalConstants.FACEBOOK_URL2)){
				
//				CookieSyncManager.createInstance(this);
//				 CookieSyncManager.getInstance().sync();
				 
				String token = url.replace(GlobalConstants.FACEBOOK_URL, "");
				token = token.replace(GlobalConstants.FACEBOOK_URL2, "");
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
				CookieSyncManager.getInstance().sync();
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
				CookieSyncManager.getInstance().sync();
				finish();
			}
		}
		
		
	}
}
