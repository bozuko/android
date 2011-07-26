package com.bozuko.bozuko.views;

import com.bozuko.bozuko.R;
import com.bozuko.bozuko.datamodel.User;
import com.fuzz.android.ui.URLImageView;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils.TruncateAt;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class UserView extends RelativeLayout{

	URLImageView _image;
	TextView _name;
	TextView _email;
	
	public UserView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		createUI(context);
	}
	
	private void createUI(Context mContext){
		LinearLayout image = new LinearLayout(mContext);
		image.setBackgroundResource(R.drawable.photoboarder);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(80*getResources().getDisplayMetrics().density),(int)(80*getResources().getDisplayMetrics().density));
		params.addRule(RelativeLayout.CENTER_VERTICAL,1);
		params.setMargins(5, 0, 0, 0);
		image.setLayoutParams(params);
		image.setId(100);
		addView(image);
		
		_image = new URLImageView(mContext);
		_image.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		_image.setScaleType(ScaleType.CENTER_CROP);
		_image.setPlaceHolder(R.drawable.defaultphoto);
		image.addView(_image);
		
		_name = new TextView(mContext);
		_name.setTextColor(Color.BLACK);
		_name.setTypeface(Typeface.DEFAULT_BOLD);
		_name.setTextSize(16);
		_name.setSingleLine(true);
		_name.setEllipsize(TruncateAt.END);
		_name.setId(102);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.addRule(RelativeLayout.ALIGN_TOP,100);
		params.setMargins(5, 0, 0, 0);
		_name.setLayoutParams(params);
		addView(_name);
		

		_email = new TextView(mContext);
		_email.setTextColor(Color.GRAY);
		_email.setTextSize(14);
		_email.setSingleLine(true);
		_email.setEllipsize(TruncateAt.END);
		_email.setId(103);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.addRule(RelativeLayout.BELOW,102);
		params.setMargins(5, 0, 0, 0);
		_email.setLayoutParams(params);
		addView(_email);
		
	}

	public void display(User page){
		//Log.v("USER",page.toString());
		_image.setURL(page.requestInfo("image"));
		_name.setText(page.requestInfo("name"));
		_email.setText(page.requestInfo("email"));
	}
}
