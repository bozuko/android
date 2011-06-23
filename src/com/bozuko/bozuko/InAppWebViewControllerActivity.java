package com.bozuko.bozuko;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class InAppWebViewControllerActivity extends BozukoControllerActivity{

	protected WebView webview;
	protected RelativeLayout loading;

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHeader(R.layout.detailheader);
		//setContent(R.layout.inappwebview);
		setTitle(getIntent().getStringExtra(Intent.EXTRA_TITLE));
		FLURRY_EVENT = getIntent().getStringExtra("FlurryEvent");
		String url = getIntent().getData().toString();
		
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		cookieManager.setAcceptCookie(true);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String sessionCookie = prefs.getString("full_cookie", "");
		
		if (sessionCookie.compareTo("") != 0) {
		    cookieManager.removeSessionCookie();
		    String cookieString = sessionCookie;
		    cookieManager.setCookie("superglued.com", cookieString);
		    CookieSyncManager.getInstance().sync();
		}   
		
		RelativeLayout rel = new RelativeLayout(this);
	    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
	    rel.setLayoutParams(params);
	    setContent(rel);
	    
	    webview = new WebView(this);
		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		webview.setLayoutParams(params2);
		webview.getSettings().setBuiltInZoomControls(true);
		webview.setWebViewClient(new CustomWebClient());
		//webview.getSettings().setUserAgentString("Mobile/Safari");
		webview.getSettings().setUserAgentString("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7");
		webview.getSettings().setJavaScriptEnabled(true);
		webview.loadUrl(url);
		webview.setBackgroundColor(Color.WHITE);
		rel.addView(webview);
	    
	    loading = new RelativeLayout(this);
		loading.setBackgroundColor(Color.argb(51,0,0,0));
		params2 = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		loading.setLayoutParams(params2);
		rel.addView(loading);
		
		ProgressBar bar = new ProgressBar(this);
		params2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params2.addRule(RelativeLayout.CENTER_IN_PARENT,1);
		bar.setLayoutParams(params2);
		loading.addView(bar);
	}
	
	public void onPause(){
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
				&& keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			// Take care of calling this method on earlier versions of
			// the platform where it doesn't exist.
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if(!webview.canGoBack()){
			finish();
		}else{
			webview.goBack();
		}
		return;
	}

	protected class CustomWebClient extends WebViewClient{
		@Override
		public boolean shouldOverrideUrlLoading (WebView view, String url){
			return false;
		}
		
		public void onPageFinished (WebView view, String url){
			loading.setVisibility(View.GONE);
		
		}
		
		public void onPageStarted (WebView view, String url, Bitmap favicon){
			loading.setVisibility(View.VISIBLE);
			//Log.v("URL",url);
		}
	}
}
