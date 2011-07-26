package com.bozuko.bozuko.views;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class NoTouchScrollView extends ScrollView {

	public NoTouchScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public boolean onInterceptTouchEvent (MotionEvent ev){
		return false;
	}

	public boolean onTouchEvent (MotionEvent event){
		return false;
	}
}
