package com.bozuko.bozuko.views;

import com.bozuko.bozuko.datamodel.PrizeObject;
import com.fuzz.android.ui.URLImageView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class PrizeCell extends RelativeLayout{

	URLImageView _image;
	TextView _title;

	public PrizeCell(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		createUI(context);
	}

	private void createUI(Context mContext){
	
		_image = new URLImageView(mContext);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,(int)(40*getResources().getDisplayMetrics().density));
		params.addRule(RelativeLayout.CENTER_VERTICAL,1);
		params.setMargins(5, 0, 0, 0);
		_image.setLayoutParams(params);
	
		_image.setScaleType(ScaleType.FIT_XY);
		_image.setId(100);
		addView(_image);
		
		_title = new TextView(mContext);
		_title.setTextColor(Color.BLACK);
		_title.setTypeface(Typeface.DEFAULT_BOLD);
		_title.setTextSize(16);
		_title.setId(101);
		_title.setGravity(Gravity.CENTER_VERTICAL);
		_title.setMinHeight((int)(44*getResources().getDisplayMetrics().density));
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.setMargins(5, 0, 0, 0);
		params.addRule(RelativeLayout.CENTER_VERTICAL,1);
		_title.setLayoutParams(params);
		addView(_title);
	}
	
	public void display(PrizeObject game){
		_title.setText(game.requestInfo("name"));
		
		if(game.checkInfo("result_image")){
			_image.setURL(game.requestInfo("result_image"));
			_image.setVisibility(View.VISIBLE);
		}else{
			_image.setVisibility(View.GONE);
		}
	}

}