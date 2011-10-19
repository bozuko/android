package com.bozuko.bozuko;

import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.GameObject;
import com.bozuko.bozuko.datamodel.GameResult;
import com.bozuko.bozuko.datamodel.GameState;
import com.bozuko.bozuko.datamodel.PageObject;
import com.bozuko.bozuko.datamodel.PrizeObject;
import com.bozuko.bozuko.datamodel.User;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.globals.GlobalFunctions;
import com.fuzz.android.http.HttpRequest;
import com.fuzz.android.ui.URLImageView;
import com.bozuko.bozuko.views.AnimationSequence;
import com.bozuko.bozuko.views.SequencerCache;
import com.bozuko.bozuko.views.SlotBannerAnimator;
import com.bozuko.bozuko.views.SlotWheelView;
import com.bozuko.bozuko.views.SequencerImageView;
import com.bozuko.bozuko.views.SequencerImageView.SequencerListener;
import com.bozuko.bozuko.views.SlotWheelView.SpinnerListener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import com.fuzz.android.ui.URLBitmapDrawable;

public class SlotsGameBozukoActivity extends BozukoControllerActivity implements OnClickListener, SpinnerListener, SequencerListener {
	ArrayList<Drawable> bitmaps;
	public static final int LOAD_IMAGES = 2;
	public static final int GAME_ENTER = 0;
	public static final int GAME_RESULTS = 1;
	public int METHOD_TYPE = GAME_ENTER;
	public boolean imagesFailed = false;

	boolean sequencing = false;

	GameObject game;
	PageObject page;
	GameResult result;
	GameState gameState;
	PrizeObject prize;

	public void setupImages(){
		bitmaps = new ArrayList<Drawable>();
		for(int i=0; i<game.icons.size(); i++){
			String url = game.iconsImages.get(i);
			String image = game.icons.get(i);
			if(url.startsWith("http")){
				Drawable bitmap;
				try{
					bitmap = new URLBitmapDrawable(getResources().getIdentifier("slotitem"+image.toLowerCase(), null, null),url,this);
				}catch(Throwable t){
					bitmap = new URLBitmapDrawable(R.drawable.blank,url,this);

				}
				bitmaps.add(bitmap);
			}else{
				Drawable bitmap;
				try{
					//bitmap = getResources().getDrawable(getResources().getIdentifier("slotitem"+image, null, null));
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.outHeight = (int)(80*getResources().getDisplayMetrics().density);
					opts.outWidth = (int)(80*getResources().getDisplayMetrics().density);
					opts.inPurgeable = true;
					int resource = getResources().getIdentifier("slotitem"+image, "drawable", "com.bozuko.bozuko");
					if(resource == 0){
						throw new Exception("");
					}
					Bitmap bit = BitmapFactory.decodeResource(getResources(), resource,opts);
					bitmap = new BitmapDrawable(getResources(),bit);
					//bitmap = new URLBitmapDrawable(getResources().getIdentifier("slotitem"+image.toLowerCase(), null, null),game.requestInfo("configthemebase")+"/"+url,this);
				}catch(Throwable t){
					bitmap = new URLBitmapDrawable(R.drawable.blank,game.requestInfo("configthemebase")+"/"+url,this);	
				}
				bitmaps.add(bitmap);
			}
		}
		((SlotWheelView)findViewById(R.id.slotwheel1)).setImages(bitmaps,game.icons);
		((SlotWheelView)findViewById(R.id.slotwheel2)).setImages(bitmaps,game.icons);
		((SlotWheelView)findViewById(R.id.slotwheel3)).setImages(bitmaps,game.icons);
	}

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		game = ((BozukoApplication)getApp()).currentGameObject;
		page = ((BozukoApplication)getApp()).currentPageObject;
		gameState = game.gameState;
		setContent(R.layout.slots);
		setHeader(R.layout.detailheader);
		((URLImageView)findViewById(R.id.gameimage)).setScaleType(ScaleType.CENTER_CROP);
		((URLImageView)findViewById(R.id.gameimage)).setPlaceHolder(R.drawable.defaultphotolarge);
		((URLImageView)findViewById(R.id.gameimage)).setURL(page.requestInfo("image"));

		//setupImages();

		((SlotWheelView)findViewById(R.id.slotwheel1)).setEnabled(false);
		((SlotWheelView)findViewById(R.id.slotwheel2)).setEnabled(false);
		((SlotWheelView)findViewById(R.id.slotwheel3)).setEnabled(false);

		((SlotWheelView)findViewById(R.id.slotwheel1)).setSpinnerListener(this);
		((SlotWheelView)findViewById(R.id.slotwheel2)).setSpinnerListener(this);
		((SlotWheelView)findViewById(R.id.slotwheel3)).setSpinnerListener(this);

		((SequencerImageView)findViewById(R.id.sequencer)).setSequencerListener(this);

		((TextView)findViewById(R.id.credits)).setText(gameState.requestInfo("user_tokens"));
		findViewById(R.id.spin).setOnClickListener(this);

		if(gameState.requestInfo("button_action").compareTo("enter")==0){
			progressRunnable(new Runnable(){
				public void run(){
					enterGame();
				}
			},"Entering...",CLOSEABLE);
		}else{
			progressRunnable(new Runnable(){
				public void run(){
					loadImages();
				}
			},"Loading...",CLOSEABLE);
		}

		findViewById(R.id.prizestext).setOnClickListener(this);
		findViewById(R.id.officialrules).setOnClickListener(this);
	}
	
	public void loadImages(){
		METHOD_TYPE = LOAD_IMAGES;
		RUNNABLE_STATE = RUNNABLE_SUCCESS;
		
		bitmaps = new ArrayList<Drawable>();
		for(int i=0; i<game.icons.size(); i++){
			String url = game.iconsImages.get(i);
			String image = game.icons.get(i);
			if(url.startsWith("http")){
				Drawable bitmap;
				if(DataBaseHelper.isImageCached(URLBitmapDrawable.createFilePathFromCrc64(GlobalFunctions.Crc64Long(url),128),this)){
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.outHeight = (int)(80*getResources().getDisplayMetrics().density);
					opts.outWidth = (int)(80*getResources().getDisplayMetrics().density);
					opts.inPurgeable = true;
					Bitmap bit = BitmapFactory.decodeFile(URLBitmapDrawable.createFilePathFromCrc64(GlobalFunctions.Crc64Long(url),128), opts);
					bitmap = new BitmapDrawable(getResources(),bit);
					bitmaps.add(bitmap);
				}else{
					if(URLBitmapDrawable.downloadImage(url)){
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.outHeight = (int)(80*getResources().getDisplayMetrics().density);
						opts.outWidth = (int)(80*getResources().getDisplayMetrics().density);
						opts.inPurgeable = true;
						Bitmap bit = BitmapFactory.decodeFile(URLBitmapDrawable.createFilePathFromCrc64(GlobalFunctions.Crc64Long(url),128), opts);
						bitmap = new BitmapDrawable(getResources(),bit);
						bitmaps.add(bitmap);
					}else{
						imagesFailed = true;
					}
				}
			}else{
				Drawable bitmap;
				try{
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.outHeight = (int)(80*getResources().getDisplayMetrics().density);
					opts.outWidth = (int)(80*getResources().getDisplayMetrics().density);
					opts.inPurgeable = true;
					int resource = getResources().getIdentifier("slotitem"+image, "drawable", "com.bozuko.bozuko");
					if(resource == 0){
						throw new Exception("");
					}
					Bitmap bit = BitmapFactory.decodeResource(getResources(), resource,opts);
					bitmap = new BitmapDrawable(getResources(),bit);

					bitmaps.add(bitmap);
				}catch(Throwable t){
					if(DataBaseHelper.isImageCached(URLBitmapDrawable.createFilePathFromCrc64(GlobalFunctions.Crc64Long(game.requestInfo("configthemebase")+"/"+url),128),this)){
						BitmapFactory.Options opts = new BitmapFactory.Options();
						opts.outHeight = (int)(80*getResources().getDisplayMetrics().density);
						opts.outWidth = (int)(80*getResources().getDisplayMetrics().density);
						opts.inPurgeable = true;
						Bitmap bit = BitmapFactory.decodeFile(URLBitmapDrawable.createFilePathFromCrc64(GlobalFunctions.Crc64Long(game.requestInfo("configthemebase")+"/"+url),128), opts);
						bitmap = new BitmapDrawable(getResources(),bit);
						//bitmap = new URLBitmapDrawable(R.drawable.blank,game.requestInfo("configthemebase")+"/"+url,this);
						bitmaps.add(bitmap);
					}else{
						if(URLBitmapDrawable.downloadImage(game.requestInfo("configthemebase")+"/"+url)){
							BitmapFactory.Options opts = new BitmapFactory.Options();
							opts.outHeight = (int)(80*getResources().getDisplayMetrics().density);
							opts.outWidth = (int)(80*getResources().getDisplayMetrics().density);
							opts.inPurgeable = true;
							Bitmap bit = BitmapFactory.decodeFile(URLBitmapDrawable.createFilePathFromCrc64(GlobalFunctions.Crc64Long(game.requestInfo("configthemebase")+"/"+url),128), opts);
							bitmap = new BitmapDrawable(getResources(),bit);
							//bitmap = new URLBitmapDrawable(R.drawable.blank,game.requestInfo("configthemebase")+"/"+url,this);
							bitmaps.add(bitmap);
						}else{
							imagesFailed = true;
						}	
					}
					
				}
			}
		}
	}

	public void progressRunnableComplete(){
		if(isFinishing()){
			return;
		}
		if(imagesFailed){
			
			AlertDialog alert = makeDialog("Couldn't load game try again later.","Request Error",new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			});
			if(alert != null){
				alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						finish();
					}
				});
				}
		}
		if(!imagesFailed){
			if(METHOD_TYPE == LOAD_IMAGES){
				((SlotWheelView)findViewById(R.id.slotwheel1)).setImages(bitmaps,game.icons);
				((SlotWheelView)findViewById(R.id.slotwheel2)).setImages(bitmaps,game.icons);
				((SlotWheelView)findViewById(R.id.slotwheel3)).setImages(bitmaps,game.icons);
			}else if(METHOD_TYPE == GAME_ENTER){
			((SlotWheelView)findViewById(R.id.slotwheel1)).setImages(bitmaps,game.icons);
			((SlotWheelView)findViewById(R.id.slotwheel2)).setImages(bitmaps,game.icons);
			((SlotWheelView)findViewById(R.id.slotwheel3)).setImages(bitmaps,game.icons);
			
			if(gameState.requestInfo("user_tokens").compareTo("0") == 0){
				findViewById(R.id.spin).setEnabled(false);
				
				AlertDialog alert = makeDialog("Please come back later.","No More Plays",new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				});
				if(alert!=null){
					alert.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
							// TODO Auto-generated method stub
							finish();
						}
					});
				}
			}else{
				//setupImages();

				((TextView)findViewById(R.id.credits)).setText(gameState.requestInfo("user_tokens"));

			}
		}else if(METHOD_TYPE == GAME_RESULTS){

			try{

				((SlotWheelView)findViewById(R.id.slotwheel1)).stopIndex = ((SlotWheelView)findViewById(R.id.slotwheel1)).rIconsHardCopy.indexOf(result.results.get(0))+3;
				((SlotWheelView)findViewById(R.id.slotwheel2)).stopIndex = ((SlotWheelView)findViewById(R.id.slotwheel2)).rIconsHardCopy.indexOf(result.results.get(1))+3;
				((SlotWheelView)findViewById(R.id.slotwheel3)).stopIndex = ((SlotWheelView)findViewById(R.id.slotwheel3)).rIconsHardCopy.indexOf(result.results.get(2))+3;



				((SlotWheelView)findViewById(R.id.slotwheel1)).stop();
			}catch(Throwable t){
				((SlotWheelView)findViewById(R.id.slotwheel1)).stopIndex = ((int)(Math.random()*(bitmaps.size())))+3;
				((SlotWheelView)findViewById(R.id.slotwheel2)).stopIndex = ((int)(Math.random()*(bitmaps.size())))+3;
				((SlotWheelView)findViewById(R.id.slotwheel3)).stopIndex = ((int)(Math.random()*(bitmaps.size())))+3;

				((SlotWheelView)findViewById(R.id.slotwheel1)).stop();
				((SlotWheelView)findViewById(R.id.slotwheel2)).stop();
				((SlotWheelView)findViewById(R.id.slotwheel3)).stop();
				AlertDialog alert = makeDialog("Couldn't load game try again later.","Request Error",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				});
				
				if(alert != null){
					alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
						
						@Override
						public void onDismiss(DialogInterface dialog) {
							// TODO Auto-generated method stub
							finish();
						}
					});
					}
				findViewById(R.id.spin).setEnabled(true);
			}
		}
		}
	}

	@Override
	public void progressRunnableError(){
		if(isFinishing()){
			return;
		}
		if(imagesFailed){
			
			AlertDialog alert = makeDialog("Couldn't load game try again later.","Request Error",new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			});
			
			if(alert != null){
				alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						finish();
					}
				});
				}
		}
		
		if(!imagesFailed){
		if(METHOD_TYPE == GAME_ENTER){
			AlertDialog alert = null;
			findViewById(R.id.spin).setEnabled(false);
			if(errorType.compareTo("facebook/auth")==0){
				alert = makeDialog(errorMessage,errorTitle,new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						facebookSignOut();
						finish();
					}
				});
			}else if(errorType.compareTo("auth/mobile")==0){
				SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
				
				if(mprefs.getBoolean("facebook_login", false)){
					((BozukoApplication)getApp()).getUser();
				}
				
				alert = makeDialog(errorMessage,errorTitle,new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				});
			}else{
				alert = makeDialog(errorMessage,errorTitle,new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				});
			}
			
			if(alert != null){
				alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						finish();
					}
				});
				}
			
		}else if(METHOD_TYPE == GAME_RESULTS){
			((SlotWheelView)findViewById(R.id.slotwheel1)).stopIndex = ((int)(Math.random()*(bitmaps.size())))+3;
			((SlotWheelView)findViewById(R.id.slotwheel2)).stopIndex = ((int)(Math.random()*(bitmaps.size())))+3;
			((SlotWheelView)findViewById(R.id.slotwheel3)).stopIndex = ((int)(Math.random()*(bitmaps.size())))+3;

			((SlotWheelView)findViewById(R.id.slotwheel1)).stop();
			((SlotWheelView)findViewById(R.id.slotwheel2)).stop();
			((SlotWheelView)findViewById(R.id.slotwheel3)).stop();

			((SequencerImageView)findViewById(R.id.sequencer)).stopSequence();
			AlertDialog alert = null;
			if(errorType.compareTo("facebook/auth")==0){
				alert = makeDialog(errorMessage,errorTitle,new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						facebookSignOut();
						finish();
					}
				});
			}else if(errorType.compareTo("auth/mobile")==0){
				SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
				
				if(mprefs.getBoolean("facebook_login", false)){
					((BozukoApplication)getApp()).getUser();
				}
				
				alert = makeDialog(errorMessage,errorTitle,new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						finish();
					}
				});
			}else{
				alert = makeDialog(errorMessage,errorTitle,new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});

			}

			if(alert != null){
				alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						// TODO Auto-generated method stub
						finish();
					}
				});
				}

			findViewById(R.id.spin).setEnabled(true);
		}
		}
	}

	public void enterGame(){
		try{
		loadImages();
		}catch(Throwable t){
			//t.printStackTrace();
		}
		METHOD_TYPE = GAME_ENTER;
		if(!DataBaseHelper.isOnline(this,0)){
			errorTitle = "Connection Error";
			errorMessage = "Unable to reach the internet.";
			RUNNABLE_STATE = RUNNABLE_FAILED;
			return;
		}
		try {
			TelephonyManager mTelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);  
			String phone_id = mTelephonyMgr.getDeviceId(); // Requires
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			User user = new User("1");
			user.getObject("1", BozukoDataBaseHelper.getSharedInstance(this));
			String url = GlobalConstants.BASE_URL + gameState.requestInfo("linksgame_entry");
			HttpRequest req = new HttpRequest(new URL(url));
			req.setMethodType("POST");
			req.add("ll", String.format("%s,%s", mprefs.getString("clat", "0.00"),mprefs.getString("clon", "0.00")));
			req.add("token", mprefs.getString("token", ""));
			req.add("phone_type", "android");
			req.add("phone_id", phone_id);
			req.add("accuracy", mprefs.getString("acc", "0"));
			req.add("mobile_version", GlobalConstants.MOBILE_VERSION);
			req.add("challenge_response", challengeResponse(url,user.requestInfo("challenge")));
			String string = req.AutoPlain();
			try{
				JSONArray array = new JSONArray(string);
				for(int i=0; i<array.length(); i++){
					JSONObject json = array.getJSONObject(i);

					GameState gameStateTemp = new GameState(json);
					if(gameStateTemp.requestInfo("game_id").compareTo(gameState.requestInfo("game_id"))==0){
						gameState.processJson(json, "");
					}
				}
				RUNNABLE_STATE = RUNNABLE_SUCCESS;
			}catch(Throwable t){
				JSONObject json = new JSONObject(string);
				errorTitle = json.getString("title");
				errorMessage = json.getString("message");
				errorType = json.getString("name");
				RUNNABLE_STATE = RUNNABLE_FAILED;
			}
		}catch(Throwable t){
			t.printStackTrace();
			mHandler.post(new DisplayThrowable(t));
			errorTitle = "Request Error";
			errorMessage = "Couldn't load game try again later.";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}

	public void getGameResults(){
		METHOD_TYPE = GAME_RESULTS;
		if(!DataBaseHelper.isOnline(this,0)){
			errorTitle = "Connection Error";
			errorMessage = "Unable to reach the internet.";
			RUNNABLE_STATE = RUNNABLE_FAILED;
			return;
		}
		try {
			TelephonyManager mTelephonyMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);  
			String phone_id = mTelephonyMgr.getDeviceId(); // Requires
			SharedPreferences mprefs = PreferenceManager.getDefaultSharedPreferences(this);
			User user = new User("1");
			user.getObject("1", BozukoDataBaseHelper.getSharedInstance(this));
			if(!gameState.checkInfo("linksgame_result")){
				errorTitle = "No more plays";
				errorMessage = "No more plays";
				RUNNABLE_STATE = RUNNABLE_FAILED;
				return;
			}
			String url = GlobalConstants.BASE_URL + gameState.requestInfo("linksgame_result");
			HttpRequest req = new HttpRequest(new URL(url));
			req.setMethodType("POST");
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
				result = new GameResult(json);
				JSONObject state = json.getJSONObject("game_state");
				gameState.processJson(state, "");
				try{
					prize = new PrizeObject(json.getJSONObject("prize"));
				}catch(Throwable t1){

				}
				RUNNABLE_STATE = RUNNABLE_SUCCESS;
			}
		}catch(Throwable t){
			mHandler.post(new DisplayThrowable(t));
			errorTitle = "Request Error";
			errorMessage = "Couldn't load game try again later.";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}

	@Override
	public void onClick(View arg0) {
		if(R.id.spin == arg0.getId() && !findViewById(R.id.spin).isEnabled()){
			return;
		}
		findViewById(R.id.spin).setEnabled(false);
		
		// TODO Auto-generated method stub
		if(arg0.getId() == R.id.prizestext){
			if(!((SlotWheelView)findViewById(R.id.slotwheel3)).isSpinning()){
			findViewById(R.id.spin).setEnabled(true);
			}
			Intent intent = new Intent(this,PrizeListBozukoActivity.class);
			startActivity(intent);
		}else if(arg0.getId() == R.id.officialrules){
			if(!((SlotWheelView)findViewById(R.id.slotwheel3)).isSpinning()){
				findViewById(R.id.spin).setEnabled(true);
			}
			Intent intent = new Intent(this,OfficialBozukoActivity.class);
			startActivity(intent);
		}else{
			if(timer != null){
				timer.cancel();
				timer.purge();
				timer = null;
			}
			if(result != null){
				if(result.requestInfo("win").compareTo("true")==0){
					if(result.requestInfo("free_play").compareTo("true") != 0){
						goRedeem();
						result = null;
						findViewById(R.id.spin).setEnabled(true);
						return;
					}
				}else if(result.requestInfo("consolation").compareTo("true")==0){
					goRedeem();
					result = null;
					findViewById(R.id.spin).setEnabled(true);
					return;
				}
			}
			try{
				//((TextView)findViewById(R.id.credits)).setText(gameState.requestInfo("user_tokens"));

				int count = Integer.valueOf(((TextView)findViewById(R.id.credits)).getText().toString());
				((TextView)findViewById(R.id.credits)).setText((count - 1) + "");

			}catch(Throwable t){

			}

			findViewById(R.id.spin).setEnabled(false);
			((SlotWheelView)findViewById(R.id.slotwheel1)).spin(-1);
			//((SlotWheelView)findViewById(R.id.slotwheel2)).spin();
			//((SlotWheelView)findViewById(R.id.slotwheel3)).spin();

			((SequencerImageView)findViewById(R.id.sequencer)).stopSequence();
			((SequencerImageView)findViewById(R.id.sequencer)).setResources(AnimationSequence.GOOD_LUCK);
			((SequencerImageView)findViewById(R.id.sequencer)).startSequence();
			sequencing = true;
			result = null;

			((SlotBannerAnimator)findViewById(R.id.animator)).playGoodLuck();

			
		}

	}

	public void onResume(){
		super.onResume();
		if(game == null){
			game = ((BozukoApplication)getApp()).currentGameObject;
			page = ((BozukoApplication)getApp()).currentPageObject;
			gameState = game.gameState;
		}
		if(sequencing){
			((SlotBannerAnimator)findViewById(R.id.animator)).onResume();
			((SequencerImageView)findViewById(R.id.sequencer)).startSequence();
		}

		if(((SlotWheelView)findViewById(R.id.slotwheel1)).isSpinning()){
			((SlotWheelView)findViewById(R.id.slotwheel1)).resume();
		}
		if(((SlotWheelView)findViewById(R.id.slotwheel2)).isSpinning()){
			((SlotWheelView)findViewById(R.id.slotwheel2)).resume();
		}
		if(((SlotWheelView)findViewById(R.id.slotwheel3)).isSpinning()){
			((SlotWheelView)findViewById(R.id.slotwheel3)).resume();
		}

		if(!((SlotWheelView)findViewById(R.id.slotwheel3)).isSpinning()){
			if(gameState.requestInfo("button_action").compareTo("enter")!=0){
				if(gameState.requestInfo("user_tokens").compareTo("0") == 0){
					AlertDialog alert = makeDialog("Please come back later.","No More Plays",new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							finish();
						}
					});
					if(alert!=null){
						alert.setOnDismissListener(new DialogInterface.OnDismissListener() {

							@Override
							public void onDismiss(DialogInterface dialog) {
								// TODO Auto-generated method stub
								finish();
							}
						});
					}
				}
			}else{
				if(result != null){
					if(gameState.requestInfo("user_tokens").compareTo("0") == 0){
						AlertDialog alert = makeDialog("Please come back later.","No More Plays",new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								finish();
							}
						});
						if(alert!=null){
							alert.setOnDismissListener(new DialogInterface.OnDismissListener() {

								@Override
								public void onDismiss(DialogInterface dialog) {
									// TODO Auto-generated method stub
									finish();
								}
							});
						}
					}
				}
			}
		}
	}

	public void onStop(){
		super.onStop();
		
		
	}
	
	public void onDestroy(){
		((SlotWheelView)findViewById(R.id.slotwheel1)).clear();
		((SlotWheelView)findViewById(R.id.slotwheel2)).clear();
		((SlotWheelView)findViewById(R.id.slotwheel3)).clear();
		bitmaps.clear();
		super.onDestroy();
	}
	
	public void onPause(){
		super.onPause();
		SequencerCache.getSharedInstance().clearResources();
		if(sequencing){
			((SlotBannerAnimator)findViewById(R.id.animator)).onPause();
			((SequencerImageView)findViewById(R.id.sequencer)).stopSequence();
		}

		if(((SlotWheelView)findViewById(R.id.slotwheel1)).isSpinning()){
			((SlotWheelView)findViewById(R.id.slotwheel1)).pause();
		}
		if(((SlotWheelView)findViewById(R.id.slotwheel2)).isSpinning()){
			((SlotWheelView)findViewById(R.id.slotwheel2)).pause();
		}
		if(((SlotWheelView)findViewById(R.id.slotwheel3)).isSpinning()){
			((SlotWheelView)findViewById(R.id.slotwheel3)).pause();
		}
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

	@Override
	public void onBackPressed() {
		if(((SlotWheelView)findViewById(R.id.slotwheel3)).isSpinning()){
			return;
		}else{
			try{
				if(timer != null){
					timer.cancel();
					timer.purge();
					timer = null;
				}
			}catch(Throwable t){
				
			}
			finish();
		}
	}

	@Override
	public void didStart(SlotWheelView view) {
		// TODO Auto-generated method stub
		if(view.getId() == R.id.slotwheel1){
			((SlotWheelView)findViewById(R.id.slotwheel2)).spin(((SlotWheelView)findViewById(R.id.slotwheel1)).spinPosition);
		}else if(view.getId() == R.id.slotwheel2){
			((SlotWheelView)findViewById(R.id.slotwheel3)).spin(((SlotWheelView)findViewById(R.id.slotwheel2)).spinPosition);
		}else if(view.getId() == R.id.slotwheel3){
			unProgressRunnable(new Runnable(){
				public void run(){
					getGameResults();
				}
			});
		}
	}

	@Override
	public void didStop(SlotWheelView view) {
		// TODO Auto-generated method stub
		if(view.getId() == R.id.slotwheel1){
			((SlotWheelView)findViewById(R.id.slotwheel2)).stop();

		}
		if(view.getId() == R.id.slotwheel2){
			((SlotWheelView)findViewById(R.id.slotwheel3)).stop();
		}
		if(view.getId() == R.id.slotwheel3){
			((TextView)findViewById(R.id.credits)).setText(gameState.requestInfo("user_tokens"));
			thisMethodIsDone = false;
			if(gameState.requestInfo("user_tokens").compareTo("0") != 0){
				
				findViewById(R.id.spin).setEnabled(true);
			}
			if(result != null){
				((SequencerImageView)findViewById(R.id.sequencer)).stopSequence();
				if(result.requestInfo("win").compareTo("false")==0){
					if(result.requestInfo("free_play").compareTo("false") == 0){
						((SequencerImageView)findViewById(R.id.sequencer)).setResources(AnimationSequence.YOU_LOSE);
						((SlotBannerAnimator)findViewById(R.id.animator)).playYouLose();
					}else{
						((SequencerImageView)findViewById(R.id.sequencer)).setResources(AnimationSequence.SPIN_AGAIN);
						((SlotBannerAnimator)findViewById(R.id.animator)).playFreeSpin();
					}
				}else{
					if(result.requestInfo("free_play").compareTo("false") == 0){
						((SequencerImageView)findViewById(R.id.sequencer)).setResources(AnimationSequence.YOU_WIN);
						((SlotBannerAnimator)findViewById(R.id.animator)).playYouWin();
					}else{
						((SequencerImageView)findViewById(R.id.sequencer)).setResources(AnimationSequence.SPIN_AGAIN);
						((SlotBannerAnimator)findViewById(R.id.animator)).playFreeSpin();
					}

				}

				((SequencerImageView)findViewById(R.id.sequencer)).startSequence();
			}

			timer = new Timer();
			timer.schedule(new TimerTask(){
				public void run(){
					mHandler.post(new Runnable(){
						public void run(){
							if(result != null){
								resultsCheck();
							}
						}
					});
				}
			}, 4000);
		}
	}

	Timer timer;

	@Override
	public void isSlowing(SlotWheelView view) {
		// TODO Auto-generated method stub

	}

	int count = 0;
	@Override
	public void SequenceCompleted(SequencerImageView v) {
		// TODO Auto-generated method stub

	}

	boolean thisMethodIsDone = false;
	public void resultsCheck(){
		
		if(timer != null){
			timer.cancel();
			timer.purge();
			timer = null;
		}
		((SlotBannerAnimator)findViewById(R.id.animator)).playPlayAgain();
		if(result.requestInfo("win").compareTo("false")==0){
			if(result.requestInfo("consolation").compareTo("true")==0){
				makeDialog(result.requestInfo("message"),"",new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if(gameState.requestInfo("user_tokens").compareTo("0") == 0){
							Intent intent = new Intent(SlotsGameBozukoActivity.this,PrizeRedeemBozukoActivity.class);
							intent.putExtra("Package", prize);
							startActivityForResult(intent,666);
							finish();
						}else{
							Intent intent = new Intent(SlotsGameBozukoActivity.this,PrizeRedeemBozukoActivity.class);
							intent.putExtra("Package", prize);
							startActivityForResult(intent,666);
							((SequencerImageView)findViewById(R.id.sequencer)).stopSequence();
							((SequencerImageView)findViewById(R.id.sequencer)).setResources(AnimationSequence.PLAY_AGAIN);
							((SequencerImageView)findViewById(R.id.sequencer)).startSequence();
						}
					}
				});
			}else{
				if(result.requestInfo("free_play").compareTo("true") != 0){
				if(gameState.requestInfo("user_tokens").compareTo("0") == 0){
					AlertDialog alert = makeDialog("Please come back later.","No More Plays",new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							finish();
						}
					});
					if(alert!=null){
						alert.setOnDismissListener(new DialogInterface.OnDismissListener() {

							@Override
							public void onDismiss(DialogInterface dialog) {
								// TODO Auto-generated method stub
								finish();
							}
						});
					}

				}else{
					count=0;
					((SequencerImageView)findViewById(R.id.sequencer)).stopSequence();
					((SequencerImageView)findViewById(R.id.sequencer)).setResources(AnimationSequence.PLAY_AGAIN);
					((SequencerImageView)findViewById(R.id.sequencer)).startSequence();
				}
				}else{
					count=0;
					((SequencerImageView)findViewById(R.id.sequencer)).stopSequence();
					((SequencerImageView)findViewById(R.id.sequencer)).setResources(AnimationSequence.PLAY_AGAIN);
					((SequencerImageView)findViewById(R.id.sequencer)).startSequence();
				}
			}

		}else{
			if(result.requestInfo("free_play").compareTo("true") != 0){
				goRedeem();
			}else{
				count=0;
				((SequencerImageView)findViewById(R.id.sequencer)).stopSequence();
				((SequencerImageView)findViewById(R.id.sequencer)).setResources(AnimationSequence.PLAY_AGAIN);
				((SequencerImageView)findViewById(R.id.sequencer)).startSequence();
			}
		}
		result = null;
	}

	public void goRedeem(){
		if(thisMethodIsDone){
			//return;
		}
		thisMethodIsDone = true;
		if(timer != null){
			timer.cancel();
			timer.purge();
			timer = null;
		}
		if(gameState.requestInfo("user_tokens").compareTo("0") == 0){
			Intent intent = new Intent(this,PrizeRedeemBozukoActivity.class);
			intent.putExtra("Package", prize);
			startActivityForResult(intent,666);
			finish();
		}else{
			count=0;
			((SequencerImageView)findViewById(R.id.sequencer)).stopSequence();
			((SequencerImageView)findViewById(R.id.sequencer)).setResources(AnimationSequence.PLAY_AGAIN);
			((SequencerImageView)findViewById(R.id.sequencer)).startSequence();
			Intent intent = new Intent(this,PrizeRedeemBozukoActivity.class);
			intent.putExtra("Package", prize);
			startActivityForResult(intent,666);
		}
	}
}
