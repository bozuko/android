package com.bozuko.bozuko;

import java.net.URL;
import org.json.JSONObject;
import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.PrizeObject;
import com.bozuko.bozuko.datamodel.RedemptionObject;
import com.bozuko.bozuko.datamodel.User;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class PrizeRedeemBozukoActivity extends BozukoControllerActivity implements OnClickListener {

	PrizeObject prize;
	RedemptionObject redemption;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContent(R.layout.redeemwrapper);
		setHeader(R.layout.detailheader);
		
		prize = (PrizeObject)getIntent().getParcelableExtra("Package");
		
		TextView priceName = (TextView)findViewById(R.id.prizename);
		priceName.setText(prize.requestInfo("name"));
		
		TextView tapRedeem = (TextView)findViewById(R.id.tapredeem);
		tapRedeem.setText(Html.fromHtml("Tap <b>\"Redeem\"</b> to continue"));
		
		TextView prizeDescrip = (TextView)findViewById(R.id.prizedescrip);
		prizeDescrip.setText(Html.fromHtml(prize.requestInfo("wrapper_message")));
		
		findViewById(R.id.save).setOnClickListener(this);
		findViewById(R.id.redeem).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
			case R.id.save:
				finish();
				break;
			case R.id.redeem:
				redeem();
				break;
		}
	}
	
	public void redeem(){
		if(prize.requestInfo("is_email").compareTo("true") == 0 || prize.requestInfo("is_barcode").compareTo("true") == 0){
			redeemPrize();
		}else{
			String tmpString = String.format("This prize will be permanently disabled after a %s second claim period. Press OK to continue or CANCEL to save this prize for later.", prize.requestInfo("redemption_duration"));
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getParent()!=null ? getParent() : this);
			builder.setMessage(tmpString)
			       .setCancelable(true)
			       .setTitle("ARE YOU SURE?")
			       .setPositiveButton("OK",new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                redeemPrize();
			           }
			       })
			       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});
			AlertDialog alert = builder.create();
			alert.show();
		}
		
	}
	
	public void redeemPrize(){
		progressRunnable(new Runnable(){
			public void run(){
				sendRequest();
			}
		},"Loading...",NOT_CANCELABLE);
	}
	
	public void progressRunnableComplete(){
		if(isFinishing()){
			return;
		}
		Intent intent = new Intent(this,PrizeBozukoActivity.class);
		intent.putExtra("Package", redemption);
		intent.putExtra("Prize", prize);
		startActivity(intent);
		setResult(RESULT_OK);
		finish();
	}
	
	public void progressRunnableError(){
		if(isFinishing()){
			return;
		}
		if(errorType.compareTo("facebook/auth")==0){
			makeDialog(errorMessage,errorTitle,new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					facebookSignOut();
					finish();
				}
			});
		}else{
			makeDialog(errorMessage,errorTitle,null);
		}
	}
	
	public void sendRequest(){
		if(!DataBaseHelper.isOnline(this,0)){
			errorMessage = "Unable to connect to the internet";
    		errorTitle = "No Connection";
    		RUNNABLE_STATE = RUNNABLE_FAILED;
    		return;
		}
		try {
			TelephonyManager mTelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);  
			String phone_id = mTelephonyMgr.getDeviceId(); // Requires
			User user = new User("1");
			user.getObject("1", BozukoDataBaseHelper.getSharedInstance(getBaseContext()));
			String url = GlobalConstants.BASE_URL + prize.requestInfo("linksredeem");
			Log.v("URL",url);
			Log.v("Prize",prize.toString());
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			HttpRequest req = new HttpRequest(new URL(url));
			req.setMethodType("POST");
			if(((CheckBox)findViewById(R.id.post)).isChecked()){
				req.add("share", "true");
				req.add("message", ((EditText)findViewById(R.id.postmessage)).getText().toString());
			}else{
				req.add("share", "false");
				req.add("message", "");
			}
			
			req.add("token", mprefs.getString("token", ""));
			req.add("phone_type", "android");
			req.add("phone_id", phone_id);
			req.add("mobile_version", GlobalConstants.MOBILE_VERSION);
			req.add("challenge_response", challengeResponse(url,user.requestInfo("challenge")));
			
			JSONObject json = req.AutoJSONError();
			try{
				errorTitle = json.getString("title");
				errorMessage = json.getString("message");
				errorType = json.getString("name");
				RUNNABLE_STATE = RUNNABLE_FAILED;
			}catch(Throwable t){
				Log.v("JSON",json.toString());
				redemption = new RedemptionObject(json);
				prize.processJson(json.getJSONObject("prize"), "");
				RUNNABLE_STATE = RUNNABLE_SUCCESS;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			mHandler.post(new DisplayThrowable(e));
			errorMessage = "Sorry, unable to redeem prize at this time.";
    		errorTitle = "Request Error";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}
}
