package com.bozuko.bozuko;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import com.bozuko.bozuko.datamodel.BozukoDataBaseHelper;
import com.bozuko.bozuko.datamodel.GameObject;
import com.bozuko.bozuko.datamodel.GameResult;
import com.bozuko.bozuko.datamodel.GameState;
import com.bozuko.bozuko.datamodel.PageObject;
import com.bozuko.bozuko.datamodel.PrizeObject;
import com.bozuko.bozuko.datamodel.User;
import com.bozuko.bozuko.views.AnimationFactory;
import com.bozuko.bozuko.views.ScratchCache;
import com.bozuko.bozuko.views.ScratchView;
import com.bozuko.bozuko.views.ScratchView.ScratchListener;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalConstants;
import com.fuzz.android.http.HttpRequest;
import com.fuzz.android.ui.URLImageView;
import com.fuzz.android.ui.URLImageView.OnLoadListener;

public class ScratchGameBozukoActivity extends BozukoControllerActivity implements OnClickListener, ScratchListener, OnLoadListener {

	public static final int NUMBER_OF_SCRATCH_AREAS = 6;

	GameObject game;
	PageObject page;
	GameResult result;
	GameState gameState;
	PrizeObject prize;
	
	boolean requestDONE = false;
	boolean imageDONE = false;

	ArrayList<Integer> popped = new ArrayList<Integer>();
	HashMap<String,Integer> scoreCard = new HashMap<String,Integer>();

	@SuppressWarnings("unchecked")
	@Override
	public void progressRunnableComplete(){
		if(isFinishing()){
			if(result != null){
				result.add("scratchBitmap", popped.toString());
				//Log.v("SCRATCH",popped.toString());
				result.saveToDb(gameState.requestInfo("game_id"), BozukoDataBaseHelper.getSharedInstance(this));
				
				
				if(prize != null){
					prize.add("gameid", gameState.requestInfo("game_id"));
					//Log.v("Prize",prize.toString());
					prize.saveToDb(gameState.requestInfo("game_id"), BozukoDataBaseHelper.getSharedInstance(this));
				}
			}
			return;
		}
		
		requestDONE = true;
		closeDialogs();
		//Log.v("GAMESTATE",gameState.toString());
		if(gameState.requestInfo("user_tokens").compareTo("0") == 0 && result == null){
			allButtonsHaveBeenScratched();
		}else{
			
			
			if(((TextView)findViewById(R.id.credits)).getText().toString().compareTo("0")==0){
				if(result.requestInfo("free_play").compareTo("true") != 0){
					((TextView)findViewById(R.id.credits)).setText((Integer.valueOf(gameState.requestInfo("user_tokens"))+1) + "");
					//findViewById(R.id.credits).setVisibility(View.VISIBLE);
				}else{
					((TextView)findViewById(R.id.credits)).setText((Integer.valueOf(gameState.requestInfo("user_tokens")) + ""));
					//findViewById(R.id.credits).setVisibility(View.VISIBLE);
				}

			}
			boolean won = false;
			try{
			
			for(int i=0; i<result.results.size(); i++){
				HashMap<String,String> temp = (HashMap<String,String>)result.results.get(i);
				ScratchView view = (ScratchView)findViewById(i+1);
				view.clearTicket();
				SpannableString ss = new SpannableString(temp.get("number") + "\n" + temp.get("text"));
				ss.setSpan(new StyleSpan(Typeface.BOLD), 0, temp.get("number").length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				ss.setSpan(new AbsoluteSizeSpan(32,true), 0, temp.get("number").length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


				view.setText(ss);

				view.setEnabled(true);
				if(popped.contains(view.getId())){
					view.setEnabled(false);
					view.setScratched();
					if(scoreCard.containsKey(view.getText().toString())){
						//Log.v("CONTAINS",view.getText().toString());
						scoreCard.put(view.getText().toString(), scoreCard.get(view.getText().toString())+1);
					}else{
						//Log.v("NOTCONTAINS",view.getText().toString());
						scoreCard.put(view.getText().toString(), 1);
					}

					if(scoreCard.get(view.getText().toString())==3){
						won = true;
					}
				}

			}

			if(won){
				allButtonsHaveBeenScratched();
			}else if(popped.size() == NUMBER_OF_SCRATCH_AREAS){
				allButtonsHaveBeenScratched();
			}
			}catch(Throwable t){
				AlertDialog alert = null;
				alert = makeDialog("Could not load game try again later.","Request Failed",new DialogInterface.OnClickListener() {

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
		}
	}

	@Override
	public void progressRunnableError(){
		if(isFinishing()){
			return;
		}
		//requestDONE = true;
		super.closeDialogs();
		
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
	}

	public void setupTickets(int left,int top,int width,int height, int startX, int startY, int textSize){

		RelativeLayout scratchArea = (RelativeLayout)findViewById(R.id.scratcharea);
		for(int i=0; i<NUMBER_OF_SCRATCH_AREAS; i++){
			int tmpRow = (int) (i / (NUMBER_OF_SCRATCH_AREAS/2));

			ScratchView view = new ScratchView(this);
			view.setTextSize(textSize);
			view.WIDTH = width;
			view.HEIGHT = height;
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width,height);
			params.setMargins(((i % (NUMBER_OF_SCRATCH_AREAS / 2)) * (left/3 + 1)) + startX, (tmpRow * (top/2)) + startY, 0, 0);
			view.setLayoutParams(params);
			scratchArea.addView(view, 0);

			view.setId(i+1);
			view.setOnClickListener(this);
			view.setScratchListener(this);
			view.setEnabled(false);
		}
	}

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setHeader(R.layout.detailheader);

		game = ((BozukoApplication)getApp()).currentGameObject;
		page = ((BozukoApplication)getApp()).currentPageObject;
		gameState = game.gameState;
		//Log.v("GAMEFORPLAY",game.toString());
		//Log.v("URL", game.requestInfo("configthemebase")+"2x/"+game.requestInfo("configthemeimagesbackground"));

		if(getResources().getDisplayMetrics().density>=2){
			setContent(R.layout.scratch_two);
			findViewById(R.id.credits).setVisibility(View.GONE);
			((URLImageView)findViewById(R.id.ticket)).setOnLoadListener(this);
			((URLImageView)findViewById(R.id.ticket)).setURL(game.requestInfo("configthemebase")+"2x/"+game.requestInfo("configthemeimagesbackground"));
			setupTickets(550,460,184,230, 50 , 250, 12);
		}else if(getResources().getDisplayMetrics().density>1){
			setContent(R.layout.scratch_one);
			findViewById(R.id.credits).setVisibility(View.GONE);
			((URLImageView)findViewById(R.id.ticket)).setOnLoadListener(this);
			((URLImageView)findViewById(R.id.ticket)).setURL(game.requestInfo("configthemebase")+"2x/"+game.requestInfo("configthemeimagesbackground"));
			setupTickets(412,345,138,172, 37 , 187, 12);
		}else{
			setContent(R.layout.scratch);
			findViewById(R.id.credits).setVisibility(View.GONE);
			((URLImageView)findViewById(R.id.ticket)).setOnLoadListener(this);
			((URLImageView)findViewById(R.id.ticket)).setURL(game.requestInfo("configthemebase")+"/"+game.requestInfo("configthemeimagesbackground"));
			setupTickets(275,230,92,115, 25 , 125, 12);
		}

		((TextView)findViewById(R.id.credits)).setText("0");
		progressRunnable(new Runnable(){
			public void run(){
				loadGameState();
			}
		},"Loading...",CLOSEABLE);

		findViewById(R.id.prizestext).setOnClickListener(this);
		findViewById(R.id.officialrules).setOnClickListener(this);
	}

	public void onResume(){
		super.onResume();
		if(gameState.requestInfo("button_action").compareTo("enter")!=0){
			if(gameState.requestInfo("user_tokens").compareTo("0") == 0 && result == null){
				allButtonsHaveBeenScratched();
			}
		}

		if(result != null){
			if(isWinner){
				animationDone();
				isWinner = false;
				return;
			}
			
			boolean won = false;

			for(String key : scoreCard.keySet()){
				if(scoreCard.get(key)==3){
					won = true;
				}
			}
			if(won || popped.size()== NUMBER_OF_SCRATCH_AREAS){
				if(gameState.requestInfo("user_tokens").compareTo("0") != 0){
					progressRunnable(new Runnable(){
						public void run(){
							getGameResults();
						}
					},"Loading...",NOT_CANCELABLE);
				}
			}
		}
		
		
	}
	
	boolean isWinner = false;

	public void onPause(){
		super.onPause();
		try{
			timer.cancel();
			timer.purge();
			timer = null;
			
//			SlotBannerAnimator animator = (SlotBannerAnimator)findViewById(R.id.animator);
//			animator.setVisibility(View.GONE);
//			animator.stop();
			ImageView cardBackground = (ImageView)findViewById(R.id.cardBackground);
			ImageView cardText = (ImageView)findViewById(R.id.cardText);
			ImageView cardStars = (ImageView)findViewById(R.id.cardStars);

			try{
				cardBackground.clearAnimation();
				cardBackground.setAnimation(null);
				cardText.clearAnimation();
				cardText.setAnimation(null);
				cardStars.clearAnimation();
				cardStars.setAnimation(null);
			}catch(Throwable t){

			}
			cardText.setVisibility(View.GONE);
			cardBackground.setVisibility(View.GONE);
			cardStars.setVisibility(View.GONE);
			
			isWinner = true;
			
		}catch(Throwable t){

		}


		if(result != null){
			result.add("scratchBitmap", popped.toString());
			//Log.v("SCRATCH",popped.toString());
			result.saveToDb(gameState.requestInfo("game_id"), BozukoDataBaseHelper.getSharedInstance(this));
			
			
			if(prize != null){
				prize.add("gameid", gameState.requestInfo("game_id"));
				//Log.v("Prize",prize.toString());
				prize.saveToDb(gameState.requestInfo("game_id"), BozukoDataBaseHelper.getSharedInstance(this));
			}
		}
	}

	public void loadGameState(){
		popped.clear();
		scoreCard.clear();
		try{
			result = new GameResult(gameState.requestInfo("game_id"));
			//Log.v("SCRATCH",result.toString());
			result.getObject(gameState.requestInfo("game_id"), BozukoDataBaseHelper.getSharedInstance(this));
			//Log.v("SCRATCH",result.toString());
			try{
				prize = new PrizeObject(gameState.requestInfo("game_id"));
				prize.getObject(gameState.requestInfo("game_id"), BozukoDataBaseHelper.getSharedInstance(this));
				//Log.v("PRIZE",prize.toString());
			}catch(Throwable t){

			}
			if(result.checkInfo("scratchBitmap") && !result.checkInfo("isFinished")){
				

				if(result.requestInfo("scratchBitmap").compareTo("[]")!=0){
					String poppedresults = result.requestInfo("scratchBitmap");
					poppedresults = poppedresults.replace(" ", "");
					poppedresults = poppedresults.replace("[", "");
					poppedresults = poppedresults.replace("]", "");
					String poppedarray[] = poppedresults.split(",");
					for(int i=0; i<poppedarray.length; i++){

						String id = poppedarray[i];
						//Log.v("SCRATCHED",id);
						popped.add(Integer.valueOf(id));
					}
				}
				RUNNABLE_STATE = RUNNABLE_SUCCESS;
			}else{
				result.delete(gameState.requestInfo("game_id"), BozukoDataBaseHelper.getSharedInstance(this));
				prize.delete(gameState.requestInfo("game_id"), BozukoDataBaseHelper.getSharedInstance(this));
				getGameState();
			}
		}catch(Throwable t){
			//t.printStackTrace();
			getGameState();
		}
	}

	public void getGameState(){
		popped.clear();
		scoreCard.clear();
		if(gameState.requestInfo("button_action").compareTo("enter")==0){
			enterGame();
		}else{
			getGameResults();
		}
	}

	public void enterGame(){
		popped.clear();
		scoreCard.clear();
		if(!DataBaseHelper.isOnline(this,0)){
			result = null;
			prize = null;
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
			req.add("mobile_version", GlobalConstants.MOBILE_VERSION);
			req.add("challenge_response", challengeResponse(url,user.requestInfo("challenge")));
			String string = req.AutoPlain();

			try{
				JSONArray array = new JSONArray(string);
				for(int i=0; i<array.length(); i++){
					JSONObject json = array.getJSONObject(i);

					//Log.v("JSONENTER",json.toString());
					GameState gameStateTemp = new GameState(json);
					if(gameStateTemp.requestInfo("game_id").compareTo(gameState.requestInfo("game_id"))==0){
						gameState.processJson(json, "");
					}
				}
				getGameResults();
				RUNNABLE_STATE = RUNNABLE_SUCCESS;
			}catch(Throwable t){
				result = null;
				prize = null;
				JSONObject json = new JSONObject(string);
				errorTitle = json.getString("title");
				errorMessage = json.getString("message");
				errorType = json.getString("name");
				RUNNABLE_STATE = RUNNABLE_FAILED;
			}
		}catch(Throwable t){
			result = null;
			prize = null;
			mHandler.post(new DisplayThrowable(t));
			errorTitle = "Request Error";
			errorMessage = "Couldn't load game try again later.";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}

	public void getGameResults(){
		popped.clear();
		scoreCard.clear();
		if(!DataBaseHelper.isOnline(this,0)){
			result = null;
			prize = null;
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
			//Log.v("JSON",json.toString());
			try{
				result = null;
				prize = null;
				errorTitle = json.getString("title");
				errorMessage = json.getString("message");
				errorType = json.getString("name");
				RUNNABLE_STATE = RUNNABLE_FAILED;
				
			}catch(Throwable t){
				result = new GameResult(json);
				result.add("gameid", gameState.requestInfo("game_id"));
				JSONObject state = json.getJSONObject("game_state");
				gameState.processJson(state, "");
				try{
					prize = new PrizeObject(json.getJSONObject("prize"));
					prize.add("prizeid", prize.requestInfo("id"));
					prize.remove("id");
					prize.add("gameid", gameState.requestInfo("game_id"));
				}catch(Throwable t1){

				}
				RUNNABLE_STATE = RUNNABLE_SUCCESS;
			}
		}catch(Throwable t){
			mHandler.post(new DisplayThrowable(t));
			result = null;
			prize = null;
			errorTitle = "Request Error";
			errorMessage = "Couldn't load game try again later.";
			RUNNABLE_STATE = RUNNABLE_FAILED;
		}
	}

	@Override
	public synchronized void onClick(View v) {
		// TODO Auto-generated method stub
		if(v.getId() == R.id.cardText){
			if(v.isEnabled()){
				v.setEnabled(false);
				v.setClickable(false);
				animationDone();
			}
			return;
		}

		if(v.getId() == R.id.officialrules){
			Intent intent = new Intent(this,OfficialBozukoActivity.class);
			startActivity(intent);
			return;
		}
		if(v.getId() == R.id.prizestext){
			Intent intent = new Intent(this,PrizeListBozukoActivity.class);
			startActivity(intent);
			return;
		}

		((ScratchView)v).startSequence();
		v.setEnabled(false);
	}

	@Override
	public void ScratchCompleted(ScratchView v) {
		// TODO Auto-generated method stub

		int id = v.getId();
		boolean won = false;
		if(!popped.contains(id)){
			popped.add(id);
			ScratchView view = (ScratchView)findViewById(id);
			//Log.v("TEXT",view.getText().toString());
			if(scoreCard.containsKey(view.getText().toString())){
				//Log.v("CONTAINS",view.getText().toString());
				scoreCard.put(view.getText().toString(), scoreCard.get(view.getText().toString())+1);
			}else{
				//Log.v("NOTCONTAINS",view.getText().toString());
				scoreCard.put(view.getText().toString(), 1);
			}

			if(scoreCard.get(view.getText().toString())==3){
				won = true;
			}
		}
		v.setEnabled(false);
		if(popped.size() == NUMBER_OF_SCRATCH_AREAS){
			//CHECK IF WIN NEXT GAME
			allButtonsHaveBeenScratched();
		}else{
			if(won){
				allButtonsHaveBeenScratched();
			}	
		}
	}

	Timer timer = null;

	public void allButtonsHaveBeenScratched(){
		if(timer == null){
			if(result != null){
				result.add("isFinished", "1");
			}
			thisMethodIsDone=false;
			playEndingSequence();
			((TextView)findViewById(R.id.credits)).setText(gameState.requestInfo("user_tokens"));
			timer = new Timer();
			timer.schedule(new TimerTask(){
				public void run(){
					mHandler.post(new Runnable(){
						public void run(){
							animationDone();
						}
					});
				}
			}, 5000);
		};
	}

	public void playEndingSequence(){
		((ScratchView)findViewById(1)).stopSequence();
		((ScratchView)findViewById(2)).stopSequence();
		((ScratchView)findViewById(3)).stopSequence();
		((ScratchView)findViewById(4)).stopSequence();
		((ScratchView)findViewById(5)).stopSequence();
		((ScratchView)findViewById(6)).stopSequence();

		ImageView cardBackground = (ImageView)findViewById(R.id.cardBackground);
		ImageView cardText = (ImageView)findViewById(R.id.cardText);
		ImageView cardStars = (ImageView)findViewById(R.id.cardStars);
		//SlotBannerAnimator animator = (SlotBannerAnimator)findViewById(R.id.animator);
		
		//animator.setVisibility(View.VISIBLE);
		
		cardText.setEnabled(true);
		cardText.setClickable(true);
		if(result != null){
			if(result.requestInfo("win").compareTo("false")==0){
				if(gameState.requestInfo("user_tokens").compareTo("0") == 0){
					cardBackground.setImageResource(R.drawable.scratchnomoreplaysbg);
					cardText.setImageResource(R.drawable.scratchnomoreplaystxt);
					cardBackground.setVisibility(View.VISIBLE);
					cardText.setVisibility(View.VISIBLE);
					cardStars.setVisibility(View.GONE);
					//animator.playNoMore();
				}else{
					cardBackground.setImageResource(R.drawable.scratchyouloseplayagainbg);
					cardText.setImageResource(R.drawable.scratchyouloseplayagaintxt);
					cardBackground.setVisibility(View.VISIBLE);
					cardText.setVisibility(View.VISIBLE);
					cardStars.setVisibility(View.GONE);
					//animator.playLose();
				}
			}else{
				if(result.requestInfo("free_play").compareTo("true") == 0){
					cardBackground.setImageResource(R.drawable.scratchbonustixbg);
					cardText.setImageResource(R.drawable.scratchbonustixtxt);
					cardBackground.setVisibility(View.VISIBLE);
					cardText.setVisibility(View.VISIBLE);
					cardStars.setVisibility(View.VISIBLE);
					//animator.playBonus();
				}else{
					cardBackground.setImageResource(R.drawable.scratchyouwinbg);
					cardText.setImageResource(R.drawable.scratchyouwintxt);
					cardBackground.setVisibility(View.VISIBLE);
					cardText.setVisibility(View.VISIBLE);
					cardStars.setVisibility(View.VISIBLE);
					//animator.playWin();
				}
			}
		}else{
			cardBackground.setImageResource(R.drawable.scratchnomoreplaysbg);
			cardText.setImageResource(R.drawable.scratchnomoreplaystxt);
			cardBackground.setVisibility(View.VISIBLE);
			cardText.setVisibility(View.VISIBLE);
			cardStars.setVisibility(View.GONE);
			//animator.playNoMore();
			
		}

		cardText.setOnClickListener(this);

		//startAnimation

		
		try{
			if(result.requestInfo("win").compareTo("false")!=0){
				cardStars.startAnimation(AnimationFactory.getScratchRotationAnimation(getResources()));
			}
			cardBackground.startAnimation(AnimationFactory.getScaleAnimation(getResources()));
			cardText.startAnimation(AnimationFactory.getScaleReverseAnimation(getResources()));
		}catch(Throwable t){
			//t.printStackTrace();
		}
	}

	
	boolean thisMethodIsDone = false;
	public synchronized void animationDone(){
		if(thisMethodIsDone){
			return;
		}
		thisMethodIsDone = true;
		
		try{
			timer.cancel();
			try{timer.purge();}catch(Throwable t){}
			timer = null;
		}catch(Throwable t){
		}
		ImageView cardBackground = (ImageView)findViewById(R.id.cardBackground);
		ImageView cardText = (ImageView)findViewById(R.id.cardText);
		ImageView cardStars = (ImageView)findViewById(R.id.cardStars);

		try{
			cardBackground.clearAnimation();
			cardBackground.setAnimation(null);
			cardText.clearAnimation();
			cardText.setAnimation(null);
			cardStars.clearAnimation();
			cardStars.setAnimation(null);
		}catch(Throwable t){

		}
		
//		SlotBannerAnimator animator = (SlotBannerAnimator)findViewById(R.id.animator);
//		animator.setVisibility(View.GONE);
//		animator.stop();

		cardBackground.setVisibility(View.GONE);
		cardText.setVisibility(View.GONE);
		cardStars.setVisibility(View.GONE);

		if(result != null){
			if(result.requestInfo("win").compareTo("false")==0){
				if(result.requestInfo("consolation").compareTo("true")==0){
					makeDialog(result.requestInfo("message"),"",new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							if(gameState.requestInfo("user_tokens").compareTo("0") == 0){
								Intent intent = new Intent(ScratchGameBozukoActivity.this,PrizeRedeemBozukoActivity.class);
								intent.putExtra("Package", prize);
								startActivityForResult(intent,666);
								finish();
							}else{
								Intent intent = new Intent(ScratchGameBozukoActivity.this,PrizeRedeemBozukoActivity.class);
								intent.putExtra("Package", prize);
								startActivityForResult(intent,666);
							}
						}
					});
				}else{
					if(result.requestInfo("free_play").compareTo("true") != 0){
					if(gameState.requestInfo("user_tokens").compareTo("0") == 0){
						finish();
					}else{
						progressRunnable(new Runnable(){
							public void run(){
								getGameResults();
							}
						},"Loading...",NOT_CANCELABLE);
					}
					}else{
						progressRunnable(new Runnable(){
							public void run(){
								getGameResults();
							}
						},"Loading...",NOT_CANCELABLE);
					}
				}
			}else{
				if(result.requestInfo("free_play").compareTo("true") != 0){
					if(gameState.requestInfo("user_tokens").compareTo("0") == 0){
						Intent intent = new Intent(this,PrizeRedeemBozukoActivity.class);
						intent.putExtra("Package", prize);
						startActivityForResult(intent,666);
						finish();
					}else{
						Intent intent = new Intent(this,PrizeRedeemBozukoActivity.class);
						intent.putExtra("Package", prize);
						startActivityForResult(intent,666);
					}
				}else{
					progressRunnable(new Runnable(){
						public void run(){
							getGameResults();
						}
					},"Loading...",NOT_CANCELABLE);
				}
			}
		}
	}

	@Override
	public void imageDidFailLoad() {
		// TODO Auto-generated method stub
		imageDONE = true;
		closeDialogs();
		makeDialog("Couldn't load game try again later.","Request Error",new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	@Override
	public void imageDidLoad() {
		// TODO Auto-generated method stub
		imageDONE = true;
		closeDialogs();
		
		((URLImageView)findViewById(R.id.businessimage)).setScaleType(ScaleType.CENTER_CROP);
		RotateAnimation rotate = new RotateAnimation(0,-15,findViewById(R.id.businessimage).getWidth()/2, findViewById(R.id.businessimage).getHeight()/2);
		rotate.setFillAfter(true);
		rotate.setDuration(0);
		((URLImageView)findViewById(R.id.businessimage)).setAnimation(rotate);
		rotate.startNow();

		((URLImageView)findViewById(R.id.businessimage)).setPlaceHolder(R.drawable.defaultphoto);
		((URLImageView)findViewById(R.id.businessimage)).setURL(page.requestInfo("image"));
		
		//((TextView)findViewById(R.id.credits)).setText(gameState.requestInfo("user_tokens"));
	}
	
	public void closeDialogs(){
		if(imageDONE && requestDONE){
			findViewById(R.id.credits).setVisibility(View.VISIBLE);
			super.closeDialogs();
		}
	}
		
	public void onDestroy(){
//		Intent backToIntent = new Intent("destroyURLImage");
//		backToIntent.putExtra("parentClass",this.getClass().toString());
//		sendBroadcast(backToIntent);
		
		//Log.v("Bozuko","Scratch destroyed");
		((URLImageView)findViewById(R.id.ticket)).setURL(null);
		((URLImageView)findViewById(R.id.businessimage)).setURL(null);
		
		ScratchCache.getSharedInstance().clearResources();
		super.onDestroy();	
	}

}
