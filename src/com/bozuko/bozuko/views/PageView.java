package com.bozuko.bozuko.views;

import java.net.URL;
import org.json.JSONObject;

import com.bozuko.bozuko.FavoriteGamesBozukoActivity;
import com.bozuko.bozuko.R;
import com.bozuko.bozuko.datamodel.PageObject;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;
import com.fuzz.android.ui.URLImageView;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

public class PageView extends RelativeLayout implements OnClickListener {

	URLImageView _image;
	TextView _title;
	TextView _address;
	TextView _distance;
	ImageButton _star;
	PageObject page;

	public PageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		createUI(context);
	}

	private void createUI(Context mContext){
		_star = new ImageButton(mContext);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_VERTICAL,1);
		params.setMargins(5, 0, 0, 0);
		_star.setScaleType(ScaleType.CENTER);
		_star.setBackgroundResource(R.drawable.starbutton);
		_star.setLayoutParams(params);
		_star.setId(100);
		_star.setFocusable(false);
		_star.setOnClickListener(this);
		addView(_star);

		LinearLayout image = new LinearLayout(mContext);
		image.setBackgroundResource(R.drawable.photoboarder);
		params = new RelativeLayout.LayoutParams((int)(46*getResources().getDisplayMetrics().density),(int)(46*getResources().getDisplayMetrics().density));
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.addRule(RelativeLayout.CENTER_VERTICAL,1);
		params.setMargins(5, 0, 0, 0);
		image.setLayoutParams(params);
		image.setId(99);
		addView(image);

		_image = new URLImageView(mContext);
		_image.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		_image.setScaleType(ScaleType.CENTER_CROP);
		_image.setPlaceHolder(R.drawable.defaultphoto);
		image.addView(_image);

		_title = new TextView(mContext);
		_title.setTextColor(Color.BLACK);
		_title.setTypeface(Typeface.DEFAULT_BOLD);
		_title.setTextSize(16);
		_title.setSingleLine(true);
		_title.setEllipsize(TruncateAt.END);
		_title.setId(102);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,99);
		params.setMargins(5, 0, 0, 0);
		_title.setLayoutParams(params);
		addView(_title);


		_address = new TextView(mContext);
		_address.setTextColor(Color.DKGRAY);
		_address.setTextSize(14);
		_address.setTypeface(Typeface.DEFAULT_BOLD);
		_address.setSingleLine(true);
		_address.setEllipsize(TruncateAt.END);
		_address.setId(103);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,99);
		params.addRule(RelativeLayout.BELOW,102);
		params.setMargins(5, 0, 0, 0);
		_address.setLayoutParams(params);
		addView(_address);

		_distance = new TextView(mContext);
		_distance.setTextColor(Color.GRAY);
		_distance.setTextSize(14);
		_distance.setSingleLine(true);
		_distance.setEllipsize(TruncateAt.END);
		_distance.setId(104);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,99);
		params.addRule(RelativeLayout.BELOW,103);
		params.setMargins(5, 0, 0, 0);
		_distance.setLayoutParams(params);
		addView(_distance);
	}

	public void display(PageObject inPage){
		page = inPage;
		Log.v("PAGE",page.toString());
		_image.setURL(page.requestInfo("image"));
		_distance.setText(page.requestInfo("distance"));
		_title.setText(page.requestInfo("name"));
		_address.setText(page.requestInfo("locationstreet"));
		if(page.requestInfo("favorite").compareTo("true")==0){
			_star.setSelected(true);
		}else{
			_star.setSelected(false);
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
		if(!DataBaseHelper.isOnline(getContext())){
			post(new Runnable(){
				public void run(){
					Toast.makeText(getContext(), "Failed to add to favorites", Toast.LENGTH_SHORT).show();
				}
			});
		}
		try {
			String url = GlobalConstants.BASE_URL + page.requestInfo("linksfavorite");
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(getContext());
			HttpRequest req = new HttpRequest(new URL(url + "?token=" + mprefs.getString("token", "")));
			req.setMethodType("POST");
			JSONObject json = req.AutoJSON();
			if(json.getBoolean("added")){
				page.add("favorite", "true");
			}else{
				page.add("favorite", "false");
			}
			post(new Runnable(){
				public void run(){
					_star.setSelected(!_star.isSelected());

					try{
						((FavoriteGamesBozukoActivity)getContext()).update(page);
					}catch(Throwable t){
						SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(getContext());
						SharedPreferences.Editor edit = mprefs.edit();
						edit.putBoolean("ReloadFavorites", true);
						edit.commit();
					}
				}
			});
		} catch (Throwable e) {
			e.printStackTrace();
			post(new Runnable(){
				public void run(){
					Toast.makeText(getContext(), "Failed to add to favorites", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}
