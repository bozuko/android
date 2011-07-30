package com.bozuko.bozuko.views;

import java.net.URL;

import org.json.JSONObject;

import com.bozuko.bozuko.BozukoControllerActivity;
import com.bozuko.bozuko.R;
import com.bozuko.bozuko.SocialMediaWebViewActivity;
import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.EntryPointObject;
import com.bozuko.bozuko.datamodel.PageObject;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;
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
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

public class PageHeaderView extends RelativeLayout implements OnClickListener {

	URLImageView _image;
	TextView _title;
	TextView _category;
	TextView _address;
	ImageButton _star;
	PageObject page;
	ImageView check;
	WebView _likeView;

	public PageHeaderView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		createUI(context);
	}

	private void createUI(Context mContext){
		setPadding(0,0,0,10);
		//setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		LinearLayout image = new LinearLayout(mContext);
		image.setBackgroundResource(R.drawable.photoboarder);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(80*getResources().getDisplayMetrics().density),(int)(80*getResources().getDisplayMetrics().density));
		//params.addRule(RelativeLayout.ALIGN_PARENT_TOP,1);
		params.setMargins(5, 5, 0, 0);
		image.setLayoutParams(params);
		image.setId(100);
		addView(image);

		_image = new URLImageView(mContext);
		_image.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		_image.setScaleType(ScaleType.CENTER_CROP);
		_image.setPlaceHolder(R.drawable.defaultphoto);
		image.addView(_image);

		_star = new ImageButton(mContext);
		//params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params = new RelativeLayout.LayoutParams((int)(26*getResources().getDisplayMetrics().density),(int)(26*getResources().getDisplayMetrics().density));
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,1);
		params.addRule(RelativeLayout.ALIGN_TOP,100);
		params.setMargins(0, 0, 0, 0);
		_star.setScaleType(ScaleType.CENTER);
		_star.setBackgroundResource(R.drawable.transparent);
		_star.setImageResource(R.drawable.starbutton);
		_star.setLayoutParams(params);
		_star.setId(101);
		_star.setFocusable(false);
		_star.setOnClickListener(this);
		addView(_star);
		
		

		_title = new TextView(mContext);
		_title.setTextColor(Color.BLACK);
		_title.setTypeface(Typeface.DEFAULT_BOLD);
		_title.setTextSize(16);
		//_title.setSingleLine(true);
		//_title.setEllipsize(TruncateAt.END);
		_title.setId(102);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.addRule(RelativeLayout.LEFT_OF,101);
		params.addRule(RelativeLayout.ALIGN_TOP,100);
		params.setMargins(10, 0, 0, 0);
		_title.setLayoutParams(params);
		addView(_title);

		CookieSyncManager.createInstance(mContext);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		_likeView = new WebView(mContext);
		params = new RelativeLayout.LayoutParams((int)(50*getResources().getDisplayMetrics().density),(int)(20*getResources().getDisplayMetrics().density));
		_likeView.setId(99);
		params.setMargins(0, 10, 0, 0);
		params.addRule(RelativeLayout.BELOW,102);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,1);
		_likeView.setLayoutParams(params);
		_likeView.getSettings().setUserAgentString("Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7");
		_likeView.getSettings().setJavaScriptEnabled(true);
		_likeView.setWebViewClient(new SocialWebClient());
		_likeView.setFocusable(false);
		//_likeView.setVerticalScrollBarEnabled(false);
		//_likeView.setHorizontalScrollBarEnabled(false);
		addView(_likeView);
		
		
		_category = new TextView(mContext);
		_category.setTextColor(Color.GRAY);
		_category.setTextSize(12);
		_category.setSingleLine(true);
		_category.setEllipsize(TruncateAt.END);
		_category.setId(103);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.addRule(RelativeLayout.LEFT_OF,99);
		params.addRule(RelativeLayout.ALIGN_BOTTOM,99);
		params.setMargins(10, 0, 0, 0);
		_category.setLayoutParams(params);
		addView(_category);
		
		

		ImageView seperator = new ImageView(mContext);
		seperator.setImageResource(R.drawable.pxdividinglinewhite);
		seperator.setScaleType(ScaleType.FIT_XY);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,1);
		
		params.addRule(RelativeLayout.BELOW,103);
		params.setMargins(10, 5, 5, 0);
		seperator.setLayoutParams(params);
		seperator.setId(105);
		addView(seperator);

		_address = new TextView(mContext);
		_address.setTextColor(Color.DKGRAY);
		_address.setTextSize(12);
		_address.setTypeface(Typeface.DEFAULT_BOLD);
		//_address.setSingleLine(true);
		//_address.setEllipsize(TruncateAt.END);
		_address.setId(104);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.addRule(RelativeLayout.LEFT_OF,101);
		params.addRule(RelativeLayout.BELOW, 105);
		//params.setMargins(0, 0, 0, 50);
		//params.addRule(RelativeLayout.ALIGN_BOTTOM,100);
		params.setMargins(10, 10, 0, 0);
		_address.setLayoutParams(params);
		addView(_address);
		
		check = new ImageView(mContext);
		check.setImageResource(R.drawable.arrowgrey);
		check.setScaleType(ScaleType.CENTER);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,1);
		params.addRule(RelativeLayout.ALIGN_BOTTOM,104);
		params.setMargins(0, 0, 5, 0);
		check.setLayoutParams(params);
		addView(check);
	}

	public void display(PageObject inPage){
		page = inPage;
		_likeView.loadUrl(inPage.requestInfo("like_button_url"));
		_image.setURL(page.requestInfo("image"));
		_category.setText(page.requestInfo("category"));
		_title.setText(page.requestInfo("name"));
		_address.setText(page.requestInfo("locationstreet") + "\n" + page.requestInfo("locationcity") + ", " + page.requestInfo("locationstate"));
		if(page.requestInfo("favorite").compareTo("true")==0){
			_star.setSelected(true);
		}else{
			_star.setSelected(false);
		}
		
		if(page.requestInfo("is_place").compareTo("false")==0){
			check.setVisibility(View.GONE);
		}
		
		if(page.requestInfo("featured").compareTo("true")==0 || page.requestInfo("registered").compareTo("true")==0){
			_star.setVisibility(View.VISIBLE);
		}else{
			_star.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		//_star.setSelected(!_star.isSelected());
		SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		if(mprefs.getBoolean("facebook_login", false)){
			Thread th = new Thread(){
				public void run(){
					sendRequest();
				}
			};
			th.start();
		}else{
			Toast.makeText(getContext(), "You need to be logged in to add favorites", Toast.LENGTH_SHORT).show();
		}
	}

	public void sendRequest(){
		if(!DataBaseHelper.isOnline(getContext(),0)){
			post(new Runnable(){
				public void run(){
					Toast.makeText(getContext(), "Failed to add to favorites", Toast.LENGTH_SHORT).show();
				}
			});
			return;
		}
		try {
			String url = GlobalConstants.BASE_URL + page.requestInfo("linksfavorite");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(getContext());
			HttpRequest req = new HttpRequest(new URL(url));
			req.add("mobile_version", GlobalConstants.MOBILE_VERSION);
			req.add("token", mprefs.getString("token", ""));
			
			req.setMethodType("POST");
			JSONObject json = req.AutoJSONError();
			try{
			try{
				if(json.getBoolean("added")){
					page.add("favorite", "true");
				}
			}catch(Throwable t){
				if(json.getBoolean("removed")){
					page.add("favorite", "false");
				}
			}
			post(new Runnable(){
				public void run(){
					_star.setSelected(!_star.isSelected());
					
					SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(getContext());
					SharedPreferences.Editor edit = mprefs.edit();
					edit.putBoolean("ReloadFavorites", true);
					edit.putBoolean("ReloadNearby", true);
					edit.commit();
				}
			});
			}catch(Throwable t){
				String errorTitle = json.getString("title");
				String errorMessage = json.getString("message");
				String errorType = json.getString("name");
				((BozukoControllerActivity)getContext()).errorTitle = errorTitle;
				((BozukoControllerActivity)getContext()).errorMessage = errorMessage;
				((BozukoControllerActivity)getContext()).errorType = errorType;
				((BozukoControllerActivity)getContext()).progressRunnableError();
			}
		} catch (Throwable e) {
			//e.printStackTrace();
			post(new Runnable(){
				public void run(){
					Toast.makeText(getContext(), "Failed to add to favorites", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	protected class SocialWebClient extends WebViewClient{

		public boolean shouldOverrideUrlLoading (WebView view, String url){
			//TODO do like commands
			//Log.v("URL",url);
			if(url.startsWith("bozuko://facebook/liked")){
				Intent intent = new Intent("LIKECHANGED");
				getContext().sendBroadcast(intent);
				return true;
			}
			if(url.startsWith("bozuko://facebook/like_loaded")){
				
				return true;
			}
			if(url.startsWith("bozuko://facebook/unliked")){
				Intent intent = new Intent("LIKECHANGED");
				getContext().sendBroadcast(intent);
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
