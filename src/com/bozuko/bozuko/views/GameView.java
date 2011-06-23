package com.bozuko.bozuko.views;

import com.bozuko.bozuko.R;
import com.bozuko.bozuko.datamodel.GameObject;
import com.fuzz.android.ui.URLImageView;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class GameView extends RelativeLayout{

	URLImageView _image;
	TextView _title;
	TextView _subtitle;
	
	public GameView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		createUI(context);
	}
	
	private void createUI(Context mContext){
		
		_image = new URLImageView(mContext);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(46*getResources().getDisplayMetrics().density),(int)(46*getResources().getDisplayMetrics().density));
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.addRule(RelativeLayout.CENTER_VERTICAL,1);
		params.setMargins(5, 0, 0, 0);
		_image.setLayoutParams(params);
		_image.setPlaceHolder(R.drawable.defaultphoto);
		_image.setScaleType(ScaleType.CENTER_CROP);
		_image.setId(100);
		addView(_image);
		
		_title = new TextView(mContext);
		_title.setTextColor(Color.BLACK);
		_title.setTypeface(Typeface.DEFAULT_BOLD);
		_title.setTextSize(16);
		_title.setSingleLine(true);
		_title.setEllipsize(TruncateAt.END);
		_title.setId(101);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.setMargins(5, 0, 0, 0);
		_title.setLayoutParams(params);
		addView(_title);
		

		_subtitle = new TextView(mContext);
		_subtitle.setTextColor(Color.DKGRAY);
		_subtitle.setTextSize(14);
		_subtitle.setTypeface(Typeface.DEFAULT_BOLD);
		_subtitle.setSingleLine(true);
		_subtitle.setEllipsize(TruncateAt.END);
		_subtitle.setId(102);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.addRule(RelativeLayout.BELOW,101);
		params.setMargins(5, 0, 0, 0);
		_subtitle.setLayoutParams(params);
		addView(_subtitle);
		
	}

	public void display(GameObject page){
		Log.v("GAME",page.toString());
		_image.setURL(page.requestInfo("image"));
		_title.setText(page.requestInfo("type"));
		_subtitle.setText(page.requestInfo("list_message"));
	}
}
