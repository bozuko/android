package com.fuzz.android.socialmedia;

import net.oauth.OAuthAccessor;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * Stand alone in app webview that handles logic for showing login pages for twitter and facebook
 * also takes care of callbacks and get the access token
 * 
 * Note: this class contain a try catch to get the SocialMediaHandler. if it uses the mediaapplication interface
 * or creates a new instance to save to.
 * @author cesaraguilar
 **/

public class SocialMediaWebView extends Activity{
	RelativeLayout loading;
	boolean done = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        RelativeLayout rel = new RelativeLayout(this);
        LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
        rel.setLayoutParams(params);
        setContentView(rel);
        
        Uri data = getIntent().getData();
      
        
        setTitle(getIntent().getStringExtra(Intent.EXTRA_TITLE));
        
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().startSync();
        final WebView webview = new WebView(this);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		webview.setLayoutParams(params2);
		webview.setBackgroundColor(Color.WHITE);
		rel.addView(webview);
		webview.getSettings().setBuiltInZoomControls(true);
		webview.setWebViewClient(new CustomWebClient());
		webview.getSettings().setJavaScriptEnabled(true);
		if(data == null){
	        Thread th = new Thread(){
	        	public void run(){
	        		SocialMediaHandler handler = ((SocialMediaApplication)getApplication()).getSocialMediaHandler();
	        		OAuthAccessor client = handler.requestTwitterAuthToken();
	        		webview.loadUrl(client.consumer.serviceProvider.userAuthorizationURL+"?oauth_token="+client.requestToken);
	        	}
	        };
	        th.start();
	    }else{
	    	webview.loadUrl(data.toString());
	    }
		
		
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

	private class CustomWebClient extends WebViewClient{
		@Override
		public boolean shouldOverrideUrlLoading (WebView view, String url){
			
			return false;
		}
		
		public void onPageFinished (WebView view, String url){
			loading.setVisibility(View.GONE);
		}
	
		public void onPageStarted (WebView view, String url, Bitmap favicon){
			loading.setVisibility(View.VISIBLE);
			
			if(url.startsWith("http://www.facebook.com/connect/login_success.html")){
				SocialMediaHandler media;
				try{
					media = ((SocialMediaApplication)getApplication()).getSocialMediaHandler();
				}catch(Throwable t){
					media = new SocialMediaHandler(view.getContext());
				}
				String temp[] = url.split("=");
				media.requestFBAccessToken(temp[temp.length-1], view.getContext());
				finish();
			}
			
			if(url.contains(SocialMediaHandler.kSocialMedia_ServiceProviderName) && !done){
				done = true;
				SocialMediaHandler media;
				try{
					media = ((SocialMediaApplication)getApplication()).getSocialMediaHandler();
				}catch(Throwable t){
					media = new SocialMediaHandler(view.getContext());
				}
				String temp[] = url.split("=");
				media.requestTwitterAccessToken(temp[temp.length-1], view.getContext());
				finish();
			}
			
			//Log.v("URL",url.toString());
			if(url.startsWith(FourSquareHandler.kFourSquare_ServiceProviderName) && !done){
				done = true;
				SocialMediaHandler media;
				try{
					media = ((SocialMediaApplication)getApplication()).getSocialMediaHandler();
				}catch(Throwable t){
					media = new FourSquareHandler(view.getContext());
				}
				if(media.getClass() == FourSquareHandler.class){
					String temp[] = url.split("=");
					((FourSquareHandler)media).requestFSquareAccessToken(temp[temp.length-1], view.getContext());
					finish();
				}
			}
		}
	}
}
