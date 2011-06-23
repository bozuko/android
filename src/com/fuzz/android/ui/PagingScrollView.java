package com.fuzz.android.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

/**
 * PagingScrollView adds some methods for smoothing over transitions between pages
 * @author cesaraguilar
 *
 */

public class PagingScrollView extends HorizontalScrollView {
	 	private static final int SWIPE_MIN_DISTANCE = 50;
	    private static final int SWIPE_THRESHOLD_VELOCITY = 150;
	    private static final int SWIPE_VERT_MIN_DISTANCE = 100;
	    
	    private PagingLayout internalWrapper;
	    private GestureDetector mGestureDetector;
	    private int mActiveFeature = 0;
	 
	    /**
	     * Constructors for the PagingScrollView
	     */
	    public PagingScrollView(Context context, AttributeSet attrs, int defStyle) {
	        super(context, attrs, defStyle);
	    }
	 
	    public PagingScrollView(Context context, AttributeSet attrs) {
	        super(context, attrs);
	    }
	 
	    public PagingScrollView(Context context) {
	        super(context);
	    }
	    
	    /**
	     * sets the Adapter that provides the pages for the paging scrollview
	     * @param provider the paging provider for the scrollview
	     */
	    public void setAdapter(PagingLayout.PagingAdapter provider){
	    	if(internalWrapper == null){
	    		internalWrapper = new PagingLayout(getContext());
	  	        internalWrapper.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	  	        internalWrapper.setOrientation(LinearLayout.HORIZONTAL);
	  	        addView(internalWrapper);
	    	}
	        internalWrapper.setPagingProvider(provider);   
	        setOnTouchListener(new View.OnTouchListener() {
	            @Override
	            public boolean onTouch(View v, MotionEvent event) {
	                //If the user swipes
	                if (mGestureDetector.onTouchEvent(event)) {
	                    return true;
	                }
	                else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL ){
	                    int scrollX = getScrollX();
	                    int featureWidth = internalWrapper.provider.getPageWidth();
	                    mActiveFeature = ((scrollX + (featureWidth/2))/featureWidth);
	                    int scrollTo = mActiveFeature*featureWidth;
	                    smoothScrollTo(scrollTo, 0);
	                    internalWrapper.removeExtraPageAroundPage(mActiveFeature);
	                    return true;
	                }
	                else{
	                    return false;
	                }
	            }
	        });
	        mGestureDetector = new GestureDetector(new MyGestureDetector());
	    }
	    
	    @Override 
	    protected void onScrollChanged (int l, int t, int oldl, int oldt){
	    	super.onScrollChanged(l, t, oldl, oldt);
	    	
	    	if(internalWrapper!=null){
	    		internalWrapper.provider.onScrollChanged(l, t, oldl, oldt);
	    	}
	    }
	    
	    class MyGestureDetector extends SimpleOnGestureListener {
	        @Override
	        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	        	try {
	        		//right to left
	        		if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	        			int featureWidth = internalWrapper.provider.getPageWidth();
	        			mActiveFeature = (mActiveFeature < (internalWrapper.provider.getCount() - internalWrapper.provider.pagesPerWindow()))? mActiveFeature + internalWrapper.provider.pagesPerWindow():internalWrapper.provider.getCount()-1;
	        			smoothScrollTo(mActiveFeature*featureWidth, 0);
	        			internalWrapper.removeExtraPageAroundPage(mActiveFeature);
	        			return true;
	        		}
	        		//left to right
	        		else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	        			int featureWidth = internalWrapper.provider.getPageWidth();
	        			mActiveFeature = (mActiveFeature > 0)? mActiveFeature - internalWrapper.provider.pagesPerWindow():0;
	        			if(mActiveFeature <= 0)
	        				mActiveFeature = 0;
	        			smoothScrollTo(mActiveFeature*featureWidth, 0);
	        			internalWrapper.removeExtraPageAroundPage(mActiveFeature);
	        			return true;
	        		}
	        	} catch (Exception e) {
	        	}

	        	return false;
	        }
	    }
	    
	    private float startlocation = 0;
	    private float diffY = 0;
	    private boolean vertScroll = false;
	    
	    public boolean onInterceptTouchEvent (MotionEvent ev){
	    	boolean ret = super.onInterceptTouchEvent(ev);
	    	
	    	
	    	
	    	switch(ev.getAction()){
	    		case MotionEvent.ACTION_DOWN:
	    			startlocation = ev.getY();
	    			vertScroll = false;
	    			break;
	    		case MotionEvent.ACTION_MOVE:
	    			diffY = Math.abs(startlocation - ev.getY());
	    			if(diffY > SWIPE_VERT_MIN_DISTANCE)
	    				vertScroll = true;
	    			break;
	    		case MotionEvent.ACTION_UP:
	    			break;
	    	}
	    	
	    	if(ret){
	    		if(vertScroll)
	    			return false;
	    		else
	    			return true;
	    	}
	    	
	    	return ret;
	    }
}