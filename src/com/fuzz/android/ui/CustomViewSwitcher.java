package com.fuzz.android.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ViewSwitcher;

public class CustomViewSwitcher extends ViewSwitcher {
	
	boolean mUserPresent = true;
	boolean mAutoStart = false;
	static final int DEFAULT_INTERVAL = 3000;
	int mFlipInterval = DEFAULT_INTERVAL;
	boolean mStarted = false;
	boolean mVisible = false;
	boolean mRunning = false;
	static final boolean LOGD = true;
	static final String TAG = "CustomViewSwitcher";
	SwitcherAdapter mAdapter = null;
	
	int whichChild = 0;

    public CustomViewSwitcher(Context context) {
        super(context);
    }

    public CustomViewSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mUserPresent = false;
                updateRunning();
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                mUserPresent = true;
                updateRunning();
            }
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Listen for broadcasts related to user-presence
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        getContext().registerReceiver(mReceiver, filter);

        if (mAutoStart) {
            // Automatically start when requested
            startFlipping();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;

        getContext().unregisterReceiver(mReceiver);
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    /**
     * How long to wait before flipping to the next view
     *
     * @param milliseconds
     *            time in milliseconds
     */
    //@android.view.RemotableViewMethod
    public void setFlipInterval(int milliseconds) {
        mFlipInterval = milliseconds;
    }

    /**
     * Start a timer to cycle through child views
     */
    public void startFlipping() {
        mStarted = true;
        updateRunning();
    }

    /**
     * No more flips
     */
    public void stopFlipping() {
        mStarted = false;
        updateRunning();
    }

    /**
     * Internal method to start or stop dispatching flip {@link Message} based
     * on {@link #mRunning} and {@link #mVisible} state.
     */
    private void updateRunning() {
        boolean running = mVisible && mStarted && mUserPresent;
        if (running != mRunning) {
            if (running) {
            	if(mAdapter != null){
            		//mAdapter.prepareViewForSwitch(getCurrentView(), whichChild);
            	}
                Message msg = mHandler.obtainMessage(FLIP_MSG);
                mHandler.sendMessageDelayed(msg, mFlipInterval);
            } else {
                mHandler.removeMessages(FLIP_MSG);
            }
            mRunning = running;
        }
        if (LOGD) {
            Log.d(TAG, "updateRunning() mVisible=" + mVisible + ", mStarted=" + mStarted
                    + ", mUserPresent=" + mUserPresent + ", mRunning=" + mRunning);
        }
    }

    /**
     * Returns true if the child views are flipping.
     */
    public boolean isFlipping() {
        return mStarted;
    }

    /**
     * Set if this view automatically calls {@link #startFlipping()} when it
     * becomes attached to a window.
     */
    public void setAutoStart(boolean autoStart) {
        mAutoStart = autoStart;
    }

    /**
     * Returns true if this view automatically calls {@link #startFlipping()}
     * when it becomes attached to a window.
     */
    public boolean isAutoStart() {
        return mAutoStart;
    }

    private final int FLIP_MSG = 1;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == FLIP_MSG) {
                if (mRunning) {
                    showNext();
                    msg = obtainMessage(FLIP_MSG);
                    sendMessageDelayed(msg, mFlipInterval);
                }
            }
        }
    };
    
    public void showNext(){
    	setInternalDisplayChild(whichChild+1);
    	super.showNext();
    }
    
    public void showPrevious(){
    	setInternalDisplayChild(whichChild-1);
    	super.showPrevious();
    }
    
    public void setInternalDisplayChild(int displayedChild){
    	if(mAdapter == null){
    		return;
    	}
    	
    	whichChild = displayedChild;
        if (displayedChild >= mAdapter.getChildCount()) {
        	whichChild = 0;
        } else if (displayedChild < 0) {
        	whichChild = mAdapter.getChildCount() - 1;
        }
    	if(mAdapter != null){
    		mAdapter.prepareViewForSwitch(getNextView(), whichChild);
    	}
    }
    
    public void setAdapter(SwitcherAdapter adapter){
    	mAdapter = adapter;
    }
    
    public interface SwitcherAdapter{
    	public void prepareViewForSwitch(View view,int index);
    	public int getChildCount();
    }

	public int getInternalDisplayedChild() {
		// TODO Auto-generated method stub
		return whichChild;
	}
}
