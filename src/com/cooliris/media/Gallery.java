/*

 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cooliris.media;

import com.cooliris.app.App;
import com.cooliris.app.Res;
import com.cooliris.cache.CacheService;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import com.fuzz.android.globals.GlobalEnum;
import com.fuzz.android.ui.PhotoInterface;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("static-access")
public class Gallery extends Activity {
    public static final String REVIEW_ACTION = "com.cooliris.media.action.REVIEW";
    private static final String TAG = "Gallery";

    private App mApp = null;
    private RenderView mRenderView = null;
    public GridLayer mGridLayer;
    private WakeLock mWakeLock;
    @SuppressWarnings("unused")
	private HashMap<String, Boolean> mAccountsEnabled = new HashMap<String, Boolean>();
    private boolean mDockSlideshow = false;
    private Handler mPicasaHandler = null;
    private Handler mHandler = new Handler();
    private static final int GET_PICASA_ACCOUNT_STATUS = 1;
    private static final int UPDATE_PICASA_ACCOUNT_STATUS = 2;

    private static final int CHECK_STORAGE = 0;
    private static final int HANDLE_INTENT = 1;
	protected static final int INPUT_DIALOG = 1;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHECK_STORAGE:
//                    checkStorage();
                    break;
                case HANDLE_INTENT:
//                    initializeDataSource();
                    break;
            }
        }
    };

  

    Runnable execute = new Runnable(){
    	public void run(){
    		updateSlotInfo();
    	}
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = new App(Gallery.this);
        @SuppressWarnings("unused")
		final boolean imageManagerHasStorage = ImageManager.hasStorage();
        @SuppressWarnings("unused")
		boolean slideshowIntent = false;
        mRenderView = new RenderView(this);
        ArrayList<PhotoInterface> arr = getIntent().getParcelableArrayListExtra("LIST_OF_IMAGES");
        
        if(arr == null){
        	arr = new ArrayList<PhotoInterface>();
        	PhotoInterface image = new PhotoInterface(){

				@Override
				public String getCaption() {
					// TODO Auto-generated method stub
					return "0";
				}

				@Override
				public CharSequence getDate() {
					// TODO Auto-generated method stub
					return "10/10/10";
				}

				@Override
				public String getID() {
					// TODO Auto-generated method stub
					return "1";
				}

				@Override
				public String getImageLarge() {
					// TODO Auto-generated method stub
					return "android.resource://"+ GlobalEnum.PACKAGE_NAME +"/" + Res.drawable.photodefault;
				}

				@Override
				public String getImageThumb() {
					// TODO Auto-generated method stub
					return "android.resource://"+ GlobalEnum.PACKAGE_NAME +"/" + Res.drawable.photodefault;
				}

				@Override
				public CharSequence getSource() {
					// TODO Auto-generated method stub
					return "";
				}

				@Override
				public String getSourceImage() {
					// TODO Auto-generated method stub
					return "android.resource://"+ GlobalEnum.PACKAGE_NAME +"/" + Res.drawable.photodefault;
				}

				@Override
				public String getSubtitle() {
					// TODO Auto-generated method stub
					return "This photo set contains no photos";
				}

				@Override
				public int describeContents() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public void writeToParcel(Parcel dest, int flags) {
					// TODO Auto-generated method stub
					
				}
        		
        	};
        	arr.add(image);
        	
        	
        }
        
        mGridLayer = new GridLayer(
        			  this, 
        			  (int) (96.0f * App.PIXEL_DENSITY), 
        			  (int) (72.0f * App.PIXEL_DENSITY), 
        			  new GridLayoutInterface(4),
        			  mRenderView);
        mGridLayer.setDataSource(new MediaPhotoSource(this,arr));
//        mGridLayer.setRange(arr.size());
        
        mGridLayer.setHandler(new Handler());
        mRenderView.setRootLayer(mGridLayer);
        RelativeLayout r = new RelativeLayout(this);
        r.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        r.setId(666);
        setContentView(r);
        mRenderView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        r.addView(mRenderView);
     
        slot = getIntent().getIntExtra("Slot", 0);
        
        Thread th = new Thread(){
        	public void run(){
        		mGridLayer.tapGesture(0, false);
        		mGridLayer.getInputProcessor().setCurrentSelectedSlot(0);
        		try {
					sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		mHandler.post(execute);
        	}
        };	
        th.start();
        sendInitialMessage();

        
        //Load images
        Log.i(TAG, "onCreate");
    }

    private void sendInitialMessage() {
        
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handler.removeMessages(CHECK_STORAGE);
        handler.removeMessages(HANDLE_INTENT);

        sendInitialMessage();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    boolean ran = false;
    
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
    	
        super.onResume();
        if (mDockSlideshow) {
            if (mWakeLock != null) {
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            }
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "GridView.Slideshow.All");
            mWakeLock.acquire();
            return;
        }
        if (mRenderView != null) {
            mRenderView.onResume();
        }
        if (mApp.isPaused()) {
//            if (mPicasaHandler != null) {
//                mPicasaHandler.removeMessages(GET_PICASA_ACCOUNT_STATUS);
//                mPicasaHandler.sendEmptyMessage(UPDATE_PICASA_ACCOUNT_STATUS);
//            }
        	mApp.onResume();
        }
        
        
    }
    
    int slot;
    void updateSlotInfo(){
//    	int slot = getIntent().getIntExtra("Slot", 0);
    	for(int i=0; i<slot; i++){
    		mGridLayer.changeFocusToNextSlot(1.0f);
    	}
    	
        mGridLayer.getInputProcessor().setCurrentSelectedSlot(slot);
    }

    void updatePicasaAccountStatus() {
        // We check to see if the authenticated accounts have
        // changed, if so, reload the datasource.

        // TODO: This should be done in PicasaDataFeed
     
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRenderView != null)
            mRenderView.onPause();
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                mWakeLock.release();
            }
            mWakeLock = null;
        }

        LocalDataSource.sThumbnailCache.flush();
        LocalDataSource.sThumbnailCacheVideo.flush();

        if (mPicasaHandler != null) {
            mPicasaHandler.removeMessages(GET_PICASA_ACCOUNT_STATUS);
            mPicasaHandler.removeMessages(UPDATE_PICASA_ACCOUNT_STATUS);
        }
    	mApp.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGridLayer != null)
            mGridLayer.stop();

        // Start the thumbnailer.
        CacheService.startCache(this, true);
    }

   
	@Override
    public void onDestroy() {
        // Force GLThread to exit.

        // Remove any post messages.
        handler.removeMessages(CHECK_STORAGE);
        handler.removeMessages(HANDLE_INTENT);

//        mPicasaAccountThread.quit();
//        mPicasaAccountThread = null;
//        mPicasaHandler = null;

        if (mGridLayer != null) {
            DataSource dataSource = mGridLayer.getDataSource();
            if (dataSource != null) {
                dataSource.shutdown();
            }
            mGridLayer.shutdown();
        }
        if (mRenderView != null) {
            mRenderView.shutdown();
            mRenderView = null;
        }
        mGridLayer = null;
        mApp.shutdown();
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mGridLayer != null) {
            mGridLayer.markDirty(30);
        }
        if (mRenderView != null)
            mRenderView.requestRender();
        Log.i(TAG, "onConfigurationChanged");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mRenderView != null) {
            return mRenderView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @SuppressWarnings("unused")
	private boolean isPickIntent() {
        String action = getIntent().getAction();
        return (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action));
    }

    @SuppressWarnings("unused")
	private boolean isViewIntent() {
        String action = getIntent().getAction();
        return Intent.ACTION_VIEW.equals(action);
    }

    @SuppressWarnings("unused")
	private boolean isReviewIntent() {
        String action = getIntent().getAction();
        return REVIEW_ACTION.equals(action);
    }

    @SuppressWarnings("unused")
	private boolean isImageType(String type) {
        return type.contains("*/") || type.equals("vnd.android.cursor.dir/image") || type.equals("image/*");
    }

    @SuppressWarnings("unused")
	private boolean isVideoType(String type) {
        return type.contains("*/") || type.equals("vnd.android.cursor.dir/video") || type.equals("video/*");
    }

    @Override
    public void onLowMemory() {
        if (mRenderView != null) {
            mRenderView.handleLowMemory();
        }
    }

    @SuppressWarnings("unused")
	private void initializeDataSource() {
        
    }

	
    
    public void uploadPhoto() {
		// TODO Auto-generated method stub
		
	}
}
