package com.bozuko.bozuko.views;

import com.bozuko.bozuko.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class SlotBannerAnimator extends ImageView implements AnimationListener {

	private int mResource = R.drawable.blank;
	
	public SlotBannerAnimator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public SlotBannerAnimator(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SlotBannerAnimator(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public Animation getBannerAnimation(){
		Animation animation = AnimationFactory.getSlotBannerAnimation(getResources());
		animation.setAnimationListener(this);
		return animation;
	}

	public void playGoodLuck(){
		mResource = R.drawable.goodluck_00020;
		startAnimation(getBannerAnimation());
		
	}
	
	public void playPlayAgain(){
		mResource = R.drawable.playagain_00018;
		startAnimation(getBannerAnimation());
	}
	
	public void playYouWin(){
		mResource = R.drawable.youwin_00026;
		startAnimation(getBannerAnimation());
	}
	
	public void playYouLose(){
		mResource = R.drawable.youlose_00020;
		startAnimation(getBannerAnimation());
	}
	
	public void playFreeSpin(){
		mResource = R.drawable.freespin_00039;
		startAnimation(getBannerAnimation());
	}

	public void onResume(){
		startAnimation(getBannerAnimation());
	}
	
	public void onPause(){
		try{
			getAnimation().cancel();
		}catch(Throwable t){
			
		}
	}
	
	@Override
	public void onAnimationEnd(Animation arg0) {
		// TODO Auto-generated method stub
		setImageResource(R.drawable.blank);
		startAnimation(arg0);
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationStart(Animation arg0) {
		// TODO Auto-generated method stub
		setImageResource(mResource);
	}
}
