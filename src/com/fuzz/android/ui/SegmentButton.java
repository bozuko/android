package com.fuzz.android.ui;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SegmentButton extends LinearLayout implements OnCheckedChangeListener {

	private onSegmentChangeListener mListener;
	private int mLeftDrawable = 0;
	private int mRightDrawable = 0;
	private int mMidDrawable = 0;
	private ArrayList<ToggleButton> mSwitches = new ArrayList<ToggleButton>();
	private int mSelected = 0;
	
	public SegmentButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		setOrientation(LinearLayout.HORIZONTAL);
	}

	public SegmentButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setOrientation(LinearLayout.HORIZONTAL);
	}

	public void setNumberOfSegments(int num){
		mSwitches.clear();
		
		 DisplayMetrics metrics = new DisplayMetrics();
		 ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

		for(int i=0; i<num; i++){
			ToggleButton b = new ToggleButton(getContext());
			if(i==0){
				b.setBackgroundResource(mLeftDrawable);
				b.setEnabled(false);
	    		b.setChecked(true);
			}else if(i==num-1){
				b.setBackgroundResource(mRightDrawable);
			}else{
				b.setBackgroundResource(mMidDrawable);
			}
			
			b.setText("");
			b.setTextOff("");
			b.setTextOn("");
			b.setTextColor(Color.WHITE);
			b.setTypeface(Typeface.DEFAULT_BOLD);
			b.setMinimumWidth((int) (40 * metrics.density));
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER;
			params.weight = 0.1f;
			b.setLayoutParams(params);
			
			b.setOnCheckedChangeListener(this);
			
			
			mSwitches.add(b);
			addView(b);
		}
	}
	
	public int getNumberOfSegments(){
		return mSwitches.size();
	}

	public void addSegment(String title){
		DisplayMetrics metrics = new DisplayMetrics();
		 ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		ToggleButton b = new ToggleButton(getContext());
		b.setText(title);
		b.setTextOff(title);
		b.setTextOn(title);
		b.setTextColor(Color.WHITE);
		b.setTypeface(Typeface.DEFAULT_BOLD);
		b.setMinimumWidth((int) (40 * metrics.density));
		if(mSwitches.size() == 0){
			b.setBackgroundResource(mLeftDrawable);
			b.setEnabled(false);
    		b.setChecked(true);
		}else{
			if(mSwitches.size()>1){
				ToggleButton temp = mSwitches.get(mSwitches.size()-1);
				temp.setBackgroundResource(mMidDrawable);
			}
			
			b.setBackgroundResource(mRightDrawable);
		}
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.weight = 0.1f;
		params.gravity = Gravity.CENTER;
		b.setLayoutParams(params);
		
		b.setOnCheckedChangeListener(this);
		
		
		mSwitches.add(b);
		addView(b);
	}

	public void setSelectedSegment(int selected){
		 for(int index=0; index<mSwitches.size(); index++){
		    	if(index != selected){
		        	turnOn(mSwitches.get(index));
		    	}else{
		    		mSwitches.get(index).setEnabled(false);
		    		mSwitches.get(index).setChecked(true);
		    	}
		 }
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		int selected = 0;
		
		if(isChecked){
			buttonView.setEnabled(false);
		    for(int index=0; index<mSwitches.size(); index++){
		    	if(mSwitches.get(index) != buttonView){
		        	turnOn(mSwitches.get(index));
		    	}else{
		    		mSelected = index;
		    	}
		    }
		    
		    //TODO listener;
		    if(mListener != null){
				mListener.onSegmentChanged(mSelected, mSwitches.get(selected));
			}
		}
	}
	
	public int getSelected(){
		return mSelected;
	}
	
	private void turnOn(ToggleButton toggleButton){
		toggleButton.setEnabled(true);
		toggleButton.setChecked(false);
	}

	public void setBackgroundResourceFor(int index, int resid){
		mSwitches.get(index).setBackgroundResource(resid);
	}
	
	public void setBackgroundDrawableFor(int index, Drawable resid){
		mSwitches.get(index).setBackgroundDrawable(resid);
	}
	
	public void setOnSegmentChangeListener(onSegmentChangeListener listener){
		mListener = listener;
	}
	
	public interface onSegmentChangeListener{
		public void onSegmentChanged(int selected,ToggleButton segment);
	}

	public void setResourceDrawables(int left,int mid, int right){
		mLeftDrawable = left;
		mMidDrawable = mid;
		mRightDrawable = right;
	}
}


