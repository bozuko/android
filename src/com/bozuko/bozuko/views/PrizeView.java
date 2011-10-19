package com.bozuko.bozuko.views;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.bozuko.bozuko.R;
import com.bozuko.bozuko.datamodel.PrizeObject;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class PrizeView extends RelativeLayout{

	ImageView _image;
	TextView _title;
	TextView _subtitle;
	TextView _expired;
	TextView _won;
	TextView _loadMore;
	
	public PrizeView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		createUI(context);
	}
	
	private void createUI(Context mContext){
		
		_image = new ImageView(mContext);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(46*getResources().getDisplayMetrics().density),(int)(46*getResources().getDisplayMetrics().density));
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.addRule(RelativeLayout.CENTER_VERTICAL,1);
		params.setMargins(5, 0, 0, 0);
		_image.setLayoutParams(params);
		_image.setScaleType(ScaleType.CENTER);
		_image.setId(100);
		addView(_image);
		
		
		_loadMore = new TextView(mContext);
		_loadMore.setTextColor(Color.BLACK);
		_loadMore.setTypeface(Typeface.DEFAULT_BOLD);
		_loadMore.setTextSize(16);
		_loadMore.setSingleLine(true);
		_loadMore.setEllipsize(TruncateAt.END);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,(int)(44*getResources().getDisplayMetrics().density));
		_loadMore.setLayoutParams(params);
		addView(_loadMore);
		
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
		_subtitle.setTextSize(12);
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
		
		_expired = new TextView(mContext);
		_expired.setTextColor(Color.rgb(153, 153, 153));
		_expired.setTextSize(12);
		//_expired.setTypeface(Typeface.DEFAULT_BOLD);
		_expired.setSingleLine(true);
		_expired.setEllipsize(TruncateAt.END);
		_expired.setId(103);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.addRule(RelativeLayout.BELOW,102);
		params.setMargins(5, 0, 0, 0);
		_expired.setLayoutParams(params);
		addView(_expired);
		
		_won = new TextView(mContext);
		_won.setTextColor(Color.rgb(153, 153, 153));
		_won.setTextSize(12);
		//_won.setTypeface(Typeface.DEFAULT_BOLD);
		_won.setSingleLine(true);
		_won.setEllipsize(TruncateAt.END);
		_won.setId(104);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF,100);
		params.addRule(RelativeLayout.BELOW,103);
		params.setMargins(5, 0, 0, 0);
		_won.setLayoutParams(params);
		addView(_won);
	}

	public void display(PrizeObject page){
		if(page == null){
			_image.setVisibility(View.GONE);
			_title.setVisibility(View.GONE);
			_subtitle.setVisibility(View.GONE);
			_expired.setVisibility(View.GONE);
			_won.setVisibility(View.GONE);
			_loadMore.setVisibility(View.VISIBLE);
			_loadMore.setText("Load More");
			_loadMore.setGravity(Gravity.CENTER);
			return;
		}else{
			_image.setVisibility(View.VISIBLE);
			_title.setVisibility(View.VISIBLE);
			_subtitle.setVisibility(View.VISIBLE);
			_expired.setVisibility(View.VISIBLE);
			_won.setVisibility(View.VISIBLE);
			_loadMore.setVisibility(View.GONE);
		}
		
		//_image.setURL(page.requestInfo("image"));
		_title.setText(page.requestInfo("name"));
		_subtitle.setText(page.requestInfo("page_name"));
		
		//Thu Jun 16 2011 21:49:06 GMT-0400 (EDT)
		SimpleDateFormat dateParser = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z (z)");
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a");
		

		if(page.requestInfo("state").compareTo("redeemed") == 0){
			try{
				Date date = dateParser.parse(page.requestInfo("redeemed_timestamp"));
				_expired.setText(Html.fromHtml("<font color='#333333'><b>Redeemed:</b></font> " + dateFormat.format(date)));
			}catch(Throwable t){
				
			}
			_image.setImageResource(R.drawable.prizesiconb);
		}else if(page.requestInfo("state").compareTo("expired") == 0){
			try{
				Date date = dateParser.parse(page.requestInfo("expiration_timestamp"));
				_expired.setText(Html.fromHtml("<font color='#333333'><b>Expired:</b></font> " + dateFormat.format(date)));
			}catch(Throwable t){
				
			}
			_image.setImageResource(R.drawable.prizesiconr);
		}else{
			try{
				Date date = dateParser.parse(page.requestInfo("expiration_timestamp"));
				_expired.setText(Html.fromHtml("<font color='#333333'><b>Expires:</b></font> <font color='#cc0000'>" + dateFormat.format(date) + "</font>"));
			}catch(Throwable t){
				
			}
			_image.setImageResource(R.drawable.prizesicong);
		}
		try{
			Date date = dateParser.parse(page.requestInfo("win_time"));
			_won.setText(Html.fromHtml("<font color='#333333'><b>Won:</b></font> " + dateFormat.format(date)));
		}catch(Throwable t){
			
		}
	}
}
