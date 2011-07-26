package com.fuzz.android.ui;

import com.fuzz.android.globals.Res;
import com.fuzz.android.ui.MenuOption.MenuOptionType;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class OptionCell extends RelativeLayout {

	ImageView _image;
	TextView _title;
	ImageView _cell;
	ImageView _accessory;
	public MenuOption option;
	CheckBox _checkMark;
	
	
	public OptionCell(Context mContext) {
		super(mContext);
		// TODO Auto-generated constructor stub
		createUI(mContext);
	}
	
	private void createUI(Context mContext){
		RelativeLayout.LayoutParams params;
		_cell = new ImageView(mContext);
		float density = mContext.getResources().getDisplayMetrics().density;
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,(int)(44*density));
		params.setMargins((int)(10*density), 0, (int)(10*density), 0);
		_cell.setScaleType(ScaleType.FIT_XY);
		_cell.setLayoutParams(params);
		_cell.setId(99);
		addView(_cell);
		
		_image = new ImageView(mContext);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_VERTICAL,1);
		params.setMargins((int)(20*density), 0, 0, 0);
		_image.setLayoutParams(params);
		_image.setId(100);
		addView(_image);
		
		_accessory = new ImageView(mContext);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_VERTICAL,1);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,1);
		params.setMargins((int)(10*density), 0, (int)(20*density), 0);
		_accessory.setLayoutParams(params);
		_accessory.setId(101);
		addView(_accessory);
		
		_checkMark = new CheckBox(mContext);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_VERTICAL,1);
		params.addRule(RelativeLayout.LEFT_OF,101);
		_checkMark.setLayoutParams(params);
		_checkMark.setId(102);
		addView(_checkMark);
		
		_title = new TextView(mContext);
		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.setMargins((int)(10*density), 0, 0, 0);
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.addRule(RelativeLayout.CENTER_VERTICAL,1);
		//params.addRule(RelativeLayout.ALIGN_RIGHT,99);
		params.addRule(RelativeLayout.LEFT_OF,102);
		_title.setLayoutParams(params);
		_title.setTextColor(Color.BLACK);
		_title.setTextSize(14);
		_title.setGravity(Gravity.CENTER);
		_title.setTypeface(Typeface.DEFAULT_BOLD);
		_title.setMaxLines(1);
		_title.setId(103);
		addView(_title);
	}

	@SuppressWarnings("static-access")
	public void display(MenuOption inOption, int resource){
		option = inOption;
		_image.setImageResource(inOption.image);
		_title.setText(inOption.title);
		_cell.setImageResource(resource);
		
		if(inOption.arrow){
			_accessory.setVisibility(View.VISIBLE);
			_accessory.setImageResource(Res.drawable.arrowgrey);
		}else{
			_accessory.setVisibility(View.GONE);
			_accessory.setImageResource(0);
		}
		
		if(inOption.optionType == MenuOptionType.CheckMarkType){
			_checkMark.setVisibility(View.VISIBLE);
		}else{
			_checkMark.setVisibility(View.GONE);
		}
	}
}
