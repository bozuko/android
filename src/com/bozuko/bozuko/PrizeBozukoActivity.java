package com.bozuko.bozuko;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.PrizeObject;
import com.bozuko.bozuko.datamodel.RedemptionObject;
import com.bozuko.bozuko.datamodel.User;
import com.fuzz.android.ui.URLImageView;
import com.fuzz.android.ui.URLImageView.OnLoadListener;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class PrizeBozukoActivity extends BozukoControllerActivity implements OnLoadListener {

	RedemptionObject redemption;
	PrizeObject prize;
	Timer timer;
	boolean countDown = false;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setHeader(R.layout.detailheader);
		//System.gc();
		
		Object object = getIntent().getParcelableExtra("Package");
		if(object.getClass() == PrizeObject.class){
			prize = (PrizeObject)object;
			if(prize.requestInfo("is_barcode").compareTo("true") == 0){
				setContent(R.layout.prizebarcode);
				setupBasic();
				((URLImageView)findViewById(R.id.barcode)).setProgressBar((ProgressBar)findViewById(R.id.progressBar1));
				((URLImageView)findViewById(R.id.barcode)).setOnLoadListener(this);
				((URLImageView)findViewById(R.id.barcode)).setURL(prize.requestInfo("barcode_image"));
			}else{
				setContent(R.layout.prize);
				setupBasic();
			}
		}else{
			redemption = (RedemptionObject)object;
			prize = (PrizeObject)getIntent().getParcelableExtra("Prize");
			//Log.v("Prize",prize.toString());
			//Log.v("Redemption",redemption.toString());
			if(prize.requestInfo("is_barcode").compareTo("true") == 0){
				setContent(R.layout.prizebarcode);
				setupBasic();
				((URLImageView)findViewById(R.id.barcode)).setProgressBar((ProgressBar)findViewById(R.id.progressBar1));
				((URLImageView)findViewById(R.id.barcode)).setOnLoadListener(this);
				((URLImageView)findViewById(R.id.barcode)).setURL(prize.requestInfo("barcode_image"));
			}else if(prize.requestInfo("is_email").compareTo("true") == 0){
				setContent(R.layout.prize);
				setupBasic();
			}else{
				setContent(R.layout.prizecount);
				((TextView)findViewById(R.id.countdown)).setText(prize.requestInfo("redemption_duration"));
				timer = new Timer();
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
				Date date = new Date();
				((TextView)findViewById(R.id.clock)).setText(dateFormat.format(date));
				timer.schedule(new ClockTimerTask(), 1000, 1000);
				
				((URLImageView)findViewById(R.id.securityimage)).setOnLoadListener(this);
				((URLImageView)findViewById(R.id.securityimage)).setURL(redemption.requestInfo("security_image"));
			}
		}
		
		TextView prizeName = (TextView)findViewById(R.id.prizename);
		prizeName.setText(prize.requestInfo("name"));	
		
		TextView prizeCode = (TextView)findViewById(R.id.prizecode);
		prizeCode.setText(prize.requestInfo("code"));	
		
		
		URLImageView userImage = (URLImageView)findViewById(R.id.userimage);
		userImage.setScaleType(ScaleType.CENTER_CROP);
		userImage.setPlaceHolder(R.drawable.defaultphoto);
		userImage.setURL(prize.requestInfo("user_img"));
		
		URLImageView pageImage = (URLImageView)findViewById(R.id.pageimage);
		pageImage.setScaleType(ScaleType.CENTER_CROP);
		pageImage.setPlaceHolder(R.drawable.defaultphoto);
		pageImage.setURL(prize.requestInfo("business_img"));
	}
	
	public void setupBasic(){
		SimpleDateFormat dateParser = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z (z)");
		SimpleDateFormat dateFormat = new SimpleDateFormat("h:mma MM/dd/yy");
		
		if(prize.requestInfo("state").compareTo("redeemed") == 0){
			ImageView prizeIcon = (ImageView)findViewById(R.id.prizeicon);
			prizeIcon.setImageResource(R.drawable.prizesiconb);
			TextView expired = (TextView)findViewById(R.id.expired);
			expired.setTextColor(Color.rgb(0, 96, 255));
			try{
				if(prize.requestInfo("is_email").compareTo("true") == 0){
					//expired.setTextColor(Color.rgb(0, 106, 54));
					Date date = dateParser.parse(prize.requestInfo("redeemed_timestamp"));
					expired.setText("EMAILED\n" + dateFormat.format(date));
				}else{
					Date date = dateParser.parse(prize.requestInfo("redeemed_timestamp"));
					//date = dateParser.parse("Fri Jul 29 2011 16:13:06 GMT-0400 (EDT)");
					expired.setText("REDEEMED\n" + dateFormat.format(date));
				}
			}catch(Throwable t){
				//t.printStackTrace();
				expired.setText("REDEEMED\n");
			}
			try{
				if(prize.requestInfo("is_email").compareTo("true") == 0){
					User user = new User("1");
					
					try{
						user.getObject("1", BozukoDataBaseHelper.getSharedInstance(this));
					}catch(Throwable t){
						//t.printStackTrace();
					}
					TextView thanks = (TextView)findViewById(R.id.thankyou);
					thanks.setText(Html.fromHtml("This prize has been emailed to:<BR><font size='+2' color='#333333'><b>"+user.requestInfo("email")+"</b></font>"));
					
				}else{
					TextView thanks = (TextView)findViewById(R.id.thankyou);
					thanks.setText(Html.fromHtml("This prize has been redeemed<BR><font size='+2' color='#333333'><b>Thank You</b></font>"));
					
				}
			}catch(Throwable t){
				//t.printStackTrace();
			}
		}else if(prize.requestInfo("state").compareTo("expired") == 0){
			ImageView prizeIcon = (ImageView)findViewById(R.id.prizeicon);
			prizeIcon.setImageResource(R.drawable.prizesiconr);
			TextView expired = (TextView)findViewById(R.id.expired);
			expired.setTextColor(Color.rgb(204, 0, 0));
			try{
				Date date = dateParser.parse(prize.requestInfo("expiration_timestamp"));
				expired.setText("EXPIRED\n" + dateFormat.format(date));
			}catch(Throwable t){
				//t.printStackTrace();
				expired.setText("EXPIRED\n");
			}
			
			try{
				TextView thanks = (TextView)findViewById(R.id.thankyou);
				thanks.setText(Html.fromHtml("Sorry, this prize has expired<BR><font size='+2' color='#333333'><b>Thank You</b></font>"));
			}catch(Throwable t){
				//t.printStackTrace();
			}
		}else{
			ImageView prizeIcon = (ImageView)findViewById(R.id.prizeicon);
			prizeIcon.setImageResource(R.drawable.prizesicong);
			TextView expired = (TextView)findViewById(R.id.expired);
			expired.setTextColor(Color.rgb(0, 106, 54));
			try{
				Date date = dateParser.parse(prize.requestInfo("redeemed_timestamp"));
				expired.setText("EMAILED\n" + dateFormat.format(date));
			}catch(Throwable t){
				//t.printStackTrace();
				expired.setText("EMAILED\n");
			}
			
			try{
				TextView thanks = (TextView)findViewById(R.id.thankyou);
				thanks.setText(Html.fromHtml(""));
			}catch(Throwable t){
				//t.printStackTrace();
			}
		}
	}
	
	public void onResume(){
		super.onResume();
//		if(timer != null){
//			try{
//				timer.cancel();
//				timer = new Timer();
//			}catch(Throwable t){
//				
//			}
//			SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
//			Date date = new Date();
//			((TextView)findViewById(R.id.clock)).setText(dateFormat.format(date));
//			timer.schedule(new ClockTimerTask(), 1000, 1000);
//		}
	}
	
	public class ClockTimerTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			mHandler.post(updateTimer);
		}
		
	}
	
	Runnable updateTimer = new Runnable(){
		public void run(){
			SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
			Date date = new Date();
			Date newDate = null;
			try{
				newDate = dateFormat.parse(((TextView)findViewById(R.id.clock)).getText().toString());
			}catch(Throwable t){
				
			}
			
			((TextView)findViewById(R.id.clock)).setText(dateFormat.format(date));
			
			try{
				date = dateFormat.parse(((TextView)findViewById(R.id.clock)).getText().toString());
			}catch(Throwable t){
				
			}
			
			if(countDown){
				int now = Integer.parseInt(((TextView)findViewById(R.id.countdown)).getText().toString());
				//now--;
				if(newDate != null){
					if(date.getTime() > newDate.getTime()-(5*1000)){
						long diff = date.getTime() - newDate.getTime();
						int seconds = (int) (diff/1000);
						now -= seconds;
					}else{
						now--;
					}
				}else{
					now--;
				}
				
				if(now < 0){
					try{
						timer.cancel();
						timer.purge();
						timer = new Timer();
					}catch(Throwable t){
						//t.printStackTrace();
					}
					
					Intent intent = new Intent(PrizeBozukoActivity.this,PrizeBozukoActivity.class);
					intent.putExtra("Package", prize);
					startActivity(intent);
					
					finish();
				}else{
					//String string = "<font color=\'333333\'>Expires in</font><h1>"+now+"<h1/><font color=\'999999\'>Seconds</font>";
					
					((TextView)findViewById(R.id.countdown)).setText(now+"");
				}
			}
		}
	};

	@Override
	public void imageDidFailLoad() {
		// TODO Auto-generated method stub
		if(findViewById(R.id.barcode) != null){
			makeDialog("Your barcode cannot be retrieved. Please check your prize screen later.","Uh-oh!",null);
		}else{
			makeDialog("Failed to retreive image.","Uh-oh!",null);
		}
		
	}

	@Override
	public void imageDidLoad() {
		// TODO Auto-generated method stub
		countDown = true;
	}
	
	public void onBackPressed(){
		try{
			timer.cancel();
			timer = new Timer();
		}catch(Throwable t){
			//t.printStackTrace();
		}
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
	            && keyCode == KeyEvent.KEYCODE_BACK
	            && event.getRepeatCount() == 0) {
	        // Take care of calling this method on earlier versions of
	        // the platform where it doesn't exist.
	        onBackPressed();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	public boolean onCreateOptionsMenu(Menu menu){
		if(redemption == null){
			//menu.add(0, R.drawable.icongames, 0, "Games").setIcon(R.drawable.icongames);
			//menu.add(0, R.drawable.iconprizes, 0, "Prizes").setIcon(R.drawable.iconprizes);
			//menu.add(0, R.drawable.iconbozuko, 0, "Bozuko").setIcon(R.drawable.iconbozuko);
		}
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.drawable.icongames:
				Intent games = new Intent(this,GamesTabController.class);
				games.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(games);
				finish();
				break;
			case R.drawable.iconprizes:
				Intent prizes = new Intent(this,PrizesBozukoActivity.class);
				prizes.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(prizes);
				finish();
				break;
			case R.drawable.iconbozuko:
				Intent bozuko = new Intent(this,SettingsBozukoActivity.class);
				bozuko.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(bozuko);
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
