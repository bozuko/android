package com.bozuko.bozuko.views;

import com.bozuko.bozuko.BozukoControllerActivity;
import com.bozuko.bozuko.SocialMediaWebViewActivity;
import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.EntryPointObject;
import com.bozuko.bozuko.datamodel.GameObject;
import com.bozuko.bozuko.datamodel.PageObject;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.ui.URLImageView;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GameHeaderView extends RelativeLayout{

	TextView _title;
	URLImageView _entryMethod;
	TextView _descrip;
	WebView _likeView;

	public GameHeaderView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		createUI(context);
	}

	private void createUI(Context mContext){
		CookieSyncManager.createInstance(mContext);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		_likeView = new WebView(mContext);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(50*getResources().getDisplayMetrics().density),(int)(20*getResources().getDisplayMetrics().density));
		_likeView.setId(99);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,1);
		_likeView.setLayoutParams(params);
		//_likeView.setVerticalScrollBarEnabled(false);
		//_likeView.setHorizontalScrollBarEnabled(false);
		_likeView.getSettings().setUserAgentString("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7");
		_likeView.getSettings().setJavaScriptEnabled(true);
		_likeView.setWebViewClient(new SocialWebClient());
		addView(_likeView);
		
		_title = new TextView(mContext);
		_title.setTextColor(Color.BLACK);
		_title.setTypeface(Typeface.DEFAULT_BOLD);
		_title.setTextSize(16);
		_title.setId(100);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT,1);
		params.addRule(RelativeLayout.LEFT_OF,99);
		_title.setLayoutParams(params);
		addView(_title);

		_entryMethod = new URLImageView(mContext);
		params = new RelativeLayout.LayoutParams((int)(32*getResources().getDisplayMetrics().density),(int)(32*getResources().getDisplayMetrics().density));
		params.addRule(RelativeLayout.BELOW,100);
		_entryMethod.setLayoutParams(params);
		_entryMethod.setId(101);
		addView(_entryMethod);
		
		
		_descrip = new TextView(mContext);
		_descrip.setTextColor(Color.rgb(51, 51, 51));
		_descrip.setTextSize(12);
		_descrip.setId(102);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW,100);
		params.addRule(RelativeLayout.RIGHT_OF,101);
		_descrip.setLayoutParams(params);
		addView(_descrip);
	}
	
	public void display(GameObject game,PageObject page){
		try{
		_title.setText(game.requestInfo("name"));
		_descrip.setText(game.requestInfo("entry_methoddescription"));
		_entryMethod.setURL(game.requestInfo("entry_methodimage"));
		_likeView.loadUrl(page.requestInfo("like_button_url"));
		}catch(Throwable t){
			
		}
		
		
	}

	protected class SocialWebClient extends WebViewClient{

		public boolean shouldOverrideUrlLoading (WebView view, String url){
			//TODO do like commands
			if(url.startsWith("bozuko://facebook/like_loaded")){
				
				return true;
			}
			if(url.startsWith("bozuko://facebook/liked")){
				Intent intent = new Intent("LIKECHANGED");
				getContext().sendBroadcast(intent);
				SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(getContext());
				SharedPreferences.Editor edit = mprefs.edit();
				edit.putBoolean("ReloadPage", true);
				edit.commit();
				return true;
			}
			if(url.startsWith("bozuko://facebook/unliked")){
				Intent intent = new Intent("LIKECHANGED");
				getContext().sendBroadcast(intent);
				SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(getContext());
				SharedPreferences.Editor edit = mprefs.edit();
				edit.putBoolean("ReloadPage", true);
				edit.commit();
				return true;
			}
			if(url.startsWith("https://www.facebook.com/connect")){
				SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(getContext());
				if(mprefs.getBoolean("facebook_login", false)){
					((BozukoControllerActivity)getContext()).makeDialog("Looks like you changed your Facebook password. Please log out of Bozuko and log back in. Thanks!","Password Changed",new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							((BozukoControllerActivity)getContext()).facebookSignOut();
							((BozukoControllerActivity)getContext()).finish();
						}
					});
				}else{
				EntryPointObject entry = new EntryPointObject("1");
		        entry.getObject("1", BozukoDataBaseHelper.getSharedInstance(getContext()));
		        TelephonyManager mTelephonyMgr = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);  
				String phone_id = mTelephonyMgr.getDeviceId(); // Requires
				String request = GlobalConstants.BASE_URL + entry.requestInfo("linkslogin") + "?mobile_version="+GlobalConstants.MOBILE_VERSION + "&phone_type=android&phone_id=" + phone_id;
				Intent intent = new Intent(getContext(),SocialMediaWebViewActivity.class);
				intent.setData(Uri.parse(request));
				intent.putExtra("FlurryEvent", "");
				getContext().startActivity(intent);
				}
				return true;
			}
			
			return false;
		}
	}
}