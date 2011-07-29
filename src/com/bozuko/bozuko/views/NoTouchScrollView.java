package com.bozuko.bozuko.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class NoTouchScrollView extends ScrollView {

	public NoTouchScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public NoTouchScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public NoTouchScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public boolean onInterceptTouchEvent (MotionEvent ev){
		return false;
	}

	public boolean onTouchEvent (MotionEvent event){
		return false;
	}
}
