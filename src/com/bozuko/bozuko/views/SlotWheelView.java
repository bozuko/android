package com.bozuko.bozuko.views;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.ImageView.ScaleType;

public class SlotWheelView extends FrameLayout {

	private static final float FAST_SPEED = 10;
	private float CURRENT_SPEED = FAST_SPEED;

	public SlotWheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		createUI(context);
	}

	int spinPosition = 0;
	public static int RESETPOSITION;

	ArrayList<Drawable> images = new ArrayList<Drawable>();
	LinearLayout layout;
	ScrollView listView;
	public int stopIndex = 0;
	boolean _shouldStop = false;
	boolean _isSpinning = false;
	boolean _isSlowingDown = false;
	boolean _isPopulated = false;
	boolean _isStopping = false;
	boolean _isSlowing = false;
	Timer _animateTimer;
	SpinnerListener mListener;

	int SPEED = 20;
	int STOPPING_SPEED = 0;


	public SlotWheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		createUI(context);
	}

	public SlotWheelView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		createUI(context);
	}


	private void createUI(Context mContext){
		RESETPOSITION = (int)(160*getResources().getDisplayMetrics().density);
		listView = new NoTouchScrollView(mContext);
		listView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		addView(listView);
		listView.setHorizontalScrollBarEnabled(false);
		listView.setVerticalScrollBarEnabled(false);
		listView.setEnabled(false);

		layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		listView.addView(layout);
		_animateTimer = new Timer();
	}

	public void setImages(ArrayList<Drawable> inList){
		images.clear();
		layout.removeAllViews();
		images.addAll(inList);
		int size = (int)(80*getResources().getDisplayMetrics().density);
		for(int i=0; i<images.size()*3; i++){

			ImageView image = new ImageView(getContext());
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size,size);
			if(i==0){
				params.setMargins(0, -(int)(33*getResources().getDisplayMetrics().density), 0, 0);
			}
			params.gravity = Gravity.CENTER_HORIZONTAL;
			image.setScaleType(ScaleType.FIT_START);
			image.setLayoutParams(params);
			layout.addView(image);

			if(i<4){
				Drawable url = inList.get(inList.size()-(4-i));
				image.setImageDrawable(url);
			}else if((i-3)>images.size()*2){
				Drawable url = inList.get((i-4) - inList.size()*2);
				image.setImageDrawable(url);
			}else if((i-3)>images.size()){
				Drawable url = inList.get((i-4) - inList.size());
				image.setImageDrawable(url);
			}else{
				Drawable url = inList.get(i-4);
				image.setImageDrawable(url);
			}
		}
		_isPopulated = true;
		randomizeScrollPosition();
	}

	public void randomizeScrollPosition(){
		listView.post(new Runnable(){
			public void run(){
				Random seedRandom = new Random();
				
				Random random = new Random(seedRandom.nextInt(1000000) + System.currentTimeMillis());
				int number = random.nextInt(images.size());
				int size = (int)(80*getResources().getDisplayMetrics().density);
				spinPosition = (int) (number*size);
				listView.scrollTo(0, spinPosition);
			}
		});

	}

	public void spin() {
		// TODO Auto-generated method stub
		if (_isSpinning == true || _isPopulated == false)
			return;

		if(_animateTimer == null){
			_animateTimer = new Timer();
			animateWheel = new AnimateWheel();
		}
		randomizeScrollPosition();
		
		CURRENT_SPEED = FAST_SPEED;
		SPEED = 10;
		STOPPING_SPEED = 0;
		_animateTimer.schedule(animateWheel, 0, SPEED + (STOPPING_SPEED/2));
		_isSpinning = true;
		_shouldStop = false;
		_isStopping = false;
		_isSlowing = false;
		_isSlowingDown = false;
		if(mListener != null){
			mListener.didStart(SlotWheelView.this);
		}
	}

	public void stop(){
		if (_isSpinning == false)
			return;

		_shouldStop = true;
	}

	public void pause(){
		_animateTimer.cancel();
		_animateTimer.purge();
		_animateTimer = null;
	}

	public void resume(){
		if(_animateTimer == null){
			_animateTimer = new Timer();
			animateWheel = new AnimateWheel();
		}
		_animateTimer.schedule(animateWheel, 0, SPEED+(STOPPING_SPEED/2));
	}

	AnimateWheel animateWheel = new AnimateWheel();
	public class AnimateWheel extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			int size = (int)(80*getResources().getDisplayMetrics().density);
			int tmpSpinStopPosition = size*(stopIndex);
			int tmpSpinSlowPosition = size*(stopIndex+8);
			if(_shouldStop){
				if(_isSlowing){
					if (spinPosition == tmpSpinStopPosition){
						_animateTimer.cancel();
						_animateTimer.purge();
						_animateTimer = null;
						_isSpinning = false;
						if(mListener != null){
							post(new Runnable(){
								public void run(){
									mListener.didStop(SlotWheelView.this);
								}
							});

						}
						return;
					}
					if (spinPosition <= tmpSpinSlowPosition){
						STOPPING_SPEED++;
						if(STOPPING_SPEED%4 == 0){
							animateWheel.cancel();
							animateWheel = new AnimateWheel();
							int newSpeed = SPEED+(STOPPING_SPEED/4);
							_animateTimer.schedule(animateWheel, newSpeed, newSpeed);
						}
					}
				}else{
					if (spinPosition == tmpSpinSlowPosition){
						STOPPING_SPEED++;
						if(STOPPING_SPEED%4 == 0){
							animateWheel.cancel();
							animateWheel = new AnimateWheel();
							int newSpeed = SPEED+(STOPPING_SPEED/4);
							_animateTimer.schedule(animateWheel, newSpeed, newSpeed);
						}
						_isSlowing = true;
					}
				}
			}

			spinPosition-=(int)(CURRENT_SPEED*getResources().getDisplayMetrics().density);
			if(spinPosition <= RESETPOSITION){
				spinPosition = size * ((images.size()*2)+2);
			}
			listView.post(new Runnable(){
				public void run(){
					listView.scrollTo(0, spinPosition);
					//listView.smoothScrollTo(0, spinPosition);
				}
			});
		}

	}

	public boolean isSpinning() {
		// TODO Auto-generated method stub
		return _isSpinning;
	}

	public void setSpinnerListener(SpinnerListener inListener){
		mListener = inListener;
	}

	public interface SpinnerListener{
		public void didStop(SlotWheelView view);
		public void didStart(SlotWheelView view);
		public void isSlowing(SlotWheelView view);
	}

	public boolean isSlowingDown() {
		// TODO Auto-generated method stub
		return _isSlowing;
	}

	
	public void clear() {
		// TODO Auto-generated method stub
		images.clear();
	}
}