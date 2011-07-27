package com.bozuko.bozuko.views;

import com.fuzz.android.animations.FlipAnimation;

import android.content.res.Resources;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class AnimationFactory {
	
	public static int ANIMATION_SPEED = 500;
	
	public static Animation getScratchBgAnimation(Resources resources){
		AnimationSet setAnimation = new AnimationSet(true);
		
		RotateAnimation rotateAnimation = new RotateAnimation(0,360,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		rotateAnimation.setDuration(ANIMATION_SPEED*4);
		rotateAnimation.setInterpolator(new LinearInterpolator());
		setAnimation.addAnimation(rotateAnimation);
		
		ScaleAnimation scaleAnimation2 = new ScaleAnimation(0.0f,1.0f,0.0f,1.0f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		scaleAnimation2.setDuration(ANIMATION_SPEED*4);
		setAnimation.addAnimation(scaleAnimation2);
		scaleAnimation2.setInterpolator(new BounceInterpolator());
		
		ScaleAnimation scaleAnimation1 = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		scaleAnimation1.setDuration(ANIMATION_SPEED*4);
		scaleAnimation1.setStartOffset(ANIMATION_SPEED*4);
		setAnimation.addAnimation(scaleAnimation1);
		scaleAnimation1.setInterpolator(new BounceInterpolator());
		
		RotateAnimation rotateAnimation1 = new RotateAnimation(0,360,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		rotateAnimation1.setDuration(ANIMATION_SPEED*4);
		rotateAnimation1.setStartOffset(ANIMATION_SPEED*4);
		rotateAnimation1.setInterpolator(new LinearInterpolator());
		setAnimation.addAnimation(rotateAnimation1);
		
		return setAnimation;
	}
	
	public static Animation getScratchTextAnimation(Resources resources){
		AnimationSet setAnimation = new AnimationSet(true);

		float density = resources.getDisplayMetrics().density;
		TranslateAnimation setAnimation1 = new TranslateAnimation(Animation.ABSOLUTE,(float)(1.0f*density),Animation.ABSOLUTE,(float)(10.0f*density),Animation.ABSOLUTE,(float)(11.0f*density),Animation.ABSOLUTE,(float)(20.0f*density));
		setAnimation.addAnimation(setAnimation1);
		setAnimation1.setDuration(ANIMATION_SPEED);
		setAnimation1.setRepeatCount(Animation.INFINITE);
		setAnimation1.setRepeatMode(Animation.REVERSE);
		setAnimation1.setInterpolator(new BounceInterpolator());

//		ScaleAnimation setAnimation2 = new ScaleAnimation(1.06f,1.0f,1.06f,1.0f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
//		setAnimation.addAnimation(setAnimation2);
//		setAnimation2.setDuration(ANIMATION_SPEED);
//		setAnimation2.setRepeatCount(Animation.INFINITE);
//		setAnimation2.setRepeatMode(Animation.REVERSE);
//		setAnimation2.setInterpolator(new BounceInterpolator());

		setAnimation.setFillBefore(true);
		return setAnimation;
	}

	public static Animation getSlotBannerAnimation(Resources resources) {
		AnimationSet setAnimation = new AnimationSet(true);
		
		ScaleAnimation scaleAnimation = new ScaleAnimation(0.0f,1.0f,0.0f,1.0f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		setAnimation.addAnimation(scaleAnimation);
		scaleAnimation.setFillBefore(true);
		scaleAnimation.setDuration(ANIMATION_SPEED*2);
		scaleAnimation.setInterpolator(new BounceInterpolator());
		
		FlipAnimation flipAnimation = new FlipAnimation(0,360,FlipAnimation.XAXIS);
		setAnimation.addAnimation(flipAnimation);
		flipAnimation.setStartOffset(ANIMATION_SPEED*2 + 100);
		flipAnimation.setDuration(ANIMATION_SPEED*2);
		flipAnimation.setInterpolator(new BounceInterpolator());
		
		FlipAnimation flipAnimation2 = new FlipAnimation(0,360,FlipAnimation.YAXIS);
		setAnimation.addAnimation(flipAnimation2);
		flipAnimation2.setStartOffset(ANIMATION_SPEED*4 + 200);
		flipAnimation2.setDuration(ANIMATION_SPEED*2);
		flipAnimation2.setInterpolator(new BounceInterpolator());
		
		ScaleAnimation scaleAnimation2 = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
		setAnimation.addAnimation(scaleAnimation2);
		scaleAnimation2.setStartOffset(ANIMATION_SPEED*6);
		scaleAnimation2.setDuration(ANIMATION_SPEED*2);
		scaleAnimation2.setFillAfter(true);
		scaleAnimation2.setInterpolator(new BounceInterpolator());
		
		//setAnimation.setRepeatCount(Animation.INFINITE);
		//setAnimation.setRepeatMode(Animation.RESTART);
		//setAnimation.setFillEnabled(false);
		return setAnimation;
	}
}
