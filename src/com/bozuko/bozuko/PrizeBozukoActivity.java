package com.bozuko.bozuko;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.bozuko.bozuko.datamodel.PrizeObject;
import com.fuzz.android.ui.URLImageView;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class PrizeBozukoActivity extends BozukoControllerActivity {

	PrizeObject prize;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setHeader(R.layout.detailheader);
		setContent(R.layout.prize);
		
		prize = (PrizeObject)getIntent().getParcelableExtra("Package");
		
		TextView prizeName = (TextView)findViewById(R.id.prizename);
		prizeName.setText(prize.requestInfo("name"));	
		
		TextView prizeCode = (TextView)findViewById(R.id.prizecode);
		prizeCode.setText(prize.requestInfo("code"));	
		
		SimpleDateFormat dateParser = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z (z)");
		SimpleDateFormat dateFormat = new SimpleDateFormat("h:mma MM/dd/yy");
		
		if(prize.requestInfo("state").compareTo("redeemed") == 0){
			ImageView prizeIcon = (ImageView)findViewById(R.id.prizeicon);
			prizeIcon.setImageResource(R.drawable.prizesiconb);
			TextView expired = (TextView)findViewById(R.id.expired);
			expired.setTextColor(Color.rgb(0, 96, 255));
			try{
				Date date = dateParser.parse(prize.requestInfo("redeemed_timestamp"));
				expired.setText("REDEEMED\n" + dateFormat.format(date));
			}catch(Throwable t){
				
			}
			TextView thanks = (TextView)findViewById(R.id.thankyou);
			thanks.setText(Html.fromHtml("This prize has been redeemed<BR><font size='+2' color='#333333'><b>Thank You</b></font>"));
		}else if(prize.requestInfo("state").compareTo("expired") == 0){
			ImageView prizeIcon = (ImageView)findViewById(R.id.prizeicon);
			prizeIcon.setImageResource(R.drawable.prizesiconr);
			TextView expired = (TextView)findViewById(R.id.expired);
			expired.setTextColor(Color.rgb(204, 0, 0));
			try{
				Date date = dateParser.parse(prize.requestInfo("redeemed_timestamp"));
				expired.setText("EXPIRED\n" + dateFormat.format(date));
			}catch(Throwable t){
				
			}
			TextView thanks = (TextView)findViewById(R.id.thankyou);
			thanks.setText(Html.fromHtml("Sorry, this prize has expired<BR><font size='+2' color='#333333'><b>Thank You</b></font>"));
			
		}else{
			ImageView prizeIcon = (ImageView)findViewById(R.id.prizeicon);
			prizeIcon.setImageResource(R.drawable.prizesicong);
			TextView expired = (TextView)findViewById(R.id.expired);
			expired.setTextColor(Color.rgb(0, 106, 54));
			try{
				Date date = dateParser.parse(prize.requestInfo("redeemed_timestamp"));
				expired.setText("EMAILED\n" + dateFormat.format(date));
			}catch(Throwable t){
				
			}
			
			TextView thanks = (TextView)findViewById(R.id.thankyou);
			thanks.setText(Html.fromHtml(""));
		}
		
		URLImageView userImage = (URLImageView)findViewById(R.id.userimage);
		userImage.setScaleType(ScaleType.CENTER_CROP);
		userImage.setPlaceHolder(R.drawable.defaultphoto);
		userImage.setURL(prize.requestInfo("user_img"));
		
		URLImageView pageImage = (URLImageView)findViewById(R.id.pageimage);
		pageImage.setScaleType(ScaleType.CENTER_CROP);
		pageImage.setPlaceHolder(R.drawable.defaultphoto);
		pageImage.setURL(prize.requestInfo("business_img"));
	}
}
