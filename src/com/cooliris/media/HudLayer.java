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
import javax.microedition.khronos.opengles.GL11;
import android.content.Context;
import android.os.Handler;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.widget.Toast;
import com.cooliris.app.App;
import com.cooliris.app.Res;

@SuppressWarnings("static-access")
public final class HudLayer extends Layer{
    public static final int MODE_NORMAL = 0;
    public static final int MODE_SELECT = 1;

    private Context mContext;
    private GridLayer mGridLayer;
    private final ImageButton mTopRightButton = new ImageButton();
    private final ImageButton mZoomInButton = new ImageButton();
    private final ImageButton mZoomOutButton = new ImageButton();
    private PathBarLayer mPathBar;
   // private MenuBar.Menu[] mSingleViewIntentBottomMenu = null;
    private final MenuBar mFullscreenMenu;
    private final LoadingLayer mLoadingLayer = new LoadingLayer();
    private RenderView mView = null;
    Handler mHandler = new Handler();
    private int mMode = MODE_NORMAL;
    private final InfoLayer top;

    // Camera button - launches the camera intent when pressed.
    //private static final int CAMERA_BUTTON_ICON = Res.drawable.btn_camera;
    //private static final int CAMERA_BUTTON_ICON_PRESSED = Res.drawable.btn_camera_pressed;
    private static final int ZOOM_IN_ICON = Res.drawable.btn_hud_zoom_in_normal;
    private static final int ZOOM_IN_ICON_PRESSED = Res.drawable.btn_hud_zoom_in_pressed;
    private static final int ZOOM_OUT_ICON = Res.drawable.btn_hud_zoom_out_normal;
    private static final int ZOOM_OUT_ICON_PRESSED = Res.drawable.btn_hud_zoom_out_pressed;



    // Grid mode button - switches the media browser to grid mode.
    //private static final int GRID_MODE_ICON = Res.drawable.mode_stack;
    //private static final int GRID_MODE_PRESSED_ICON = Res.drawable.mode_stack;

    private final Runnable mZoomInButtonAction = new Runnable() {
        public void run() {
            mGridLayer.zoomInToSelectedItem();
            mGridLayer.markDirty(1);
        }
    };

    private final Runnable mZoomOutButtonAction = new Runnable() {
        public void run() {
            mGridLayer.zoomOutFromSelectedItem();
            mGridLayer.markDirty(1);
        }
    };

 

    /**
     * Stack mode button - switches the media browser to grid mode.
     */
  //  private static final int STACK_MODE_ICON = Res.drawable.mode_grid;
   // private static final int STACK_MODE_PRESSED_ICON = Res.drawable.mode_grid;
  
    private float mAlpha;
    private float mAnimAlpha;
    private boolean mAutoHide;
    private long mLastTimeFullOpacity;
    private String mCachedCaption;
    private String mCachedPosition;
    private String mCachedCurrentLabel;

    HudLayer(final Context context) {
        mAlpha = 1.0f;
        if (mPathBar == null) {
            mPathBar = new PathBarLayer();
        }
        mTopRightButton.setSize((int) (100 * App.PIXEL_DENSITY), (int) (94 * App.PIXEL_DENSITY));

        mZoomInButton.setSize(66.666f * App.PIXEL_DENSITY, 42 * App.PIXEL_DENSITY);
        mZoomOutButton.setSize(66.666f * App.PIXEL_DENSITY, 42 * App.PIXEL_DENSITY);
        mZoomInButton.setImages(ZOOM_IN_ICON, ZOOM_IN_ICON_PRESSED);
        mZoomInButton.setAction(mZoomInButtonAction);
        mZoomOutButton.setImages(ZOOM_OUT_ICON, ZOOM_OUT_ICON_PRESSED);
        mZoomOutButton.setAction(mZoomOutButtonAction);

        
        top = new InfoLayer(context);
        
        mFullscreenMenu = new MenuBar(context);
        mFullscreenMenu.setMenus(new MenuBar.Menu[] {
                new MenuBar.Menu.Builder("").icon(Res.drawable.excli)
                        .onSingleTapUp(new Runnable() {
                            public void run() {
                            	showToastForImage();
                            }
                        }).build(), /* new MenuBar.Menu.Builder("").build(), */
                new MenuBar.Menu.Builder("").icon(Res.drawable.icon_play)
                        .onSingleTapUp(new Runnable() {
                            public void run() {
                                if (getAlpha() == 1.0f)
                                    mGridLayer.startSlideshow();
                                else
                                    setAlpha(1.0f);
                            }
                        }).build(),
                new MenuBar.Menu.Builder("").icon(Res.drawable.icon_share)
                        .onSingleTapUp(new Runnable() {
                            public void run() {
                            	updateShareMenu();
                            }
                        }).build()});
    }

    public void setContext(Context context) {
        if (mContext != context) {
            mContext = context;
            top.setContext(context);
        }
        
    }

    protected void deleteSelection() {
        mGridLayer.deleteSelection();
    }

    void setGridLayer(GridLayer layer) {
        mGridLayer = layer;
        updateViews();
    }

    int getMode() {
        return mMode;
    }

    void setMode(int mode) {
        if (mMode != mode) {
            mMode = mode;
            updateViews();
        }
    }

    @Override
    protected void onSizeChanged() {
        final float width = mWidth;
        final float height = mHeight;
        closeSelectionMenu();


        top.setPosition(0f, height - (MenuBar.HEIGHT * App.PIXEL_DENSITY) - (top.getTextSize() * App.PIXEL_DENSITY));
        top.setDHeight(height - (MenuBar.HEIGHT * App.PIXEL_DENSITY));
        top.setSize(width, top.getTextSize());
        mFullscreenMenu.setPosition(0f, height - MenuBar.HEIGHT * App.PIXEL_DENSITY);
        mFullscreenMenu.setSize(width, MenuBar.HEIGHT * App.PIXEL_DENSITY);

        mPathBar.setPosition(0f, -4f * App.PIXEL_DENSITY);
        computeSizeForPathbar();

        mTopRightButton.setPosition(width - mTopRightButton.getWidth(), 0f);
        float zoomY = 20 * App.PIXEL_DENSITY;
        mZoomInButton.setPosition(30 + width - mZoomInButton.getWidth() , zoomY);
        mZoomOutButton.setPosition(30 + width - mZoomOutButton.getWidth(), zoomY + mZoomInButton.getHeight());
    }

    private void computeSizeForPathbar() {
        float pathBarWidth = mWidth
                - ((mGridLayer.getState() == GridLayer.STATE_FULL_SCREEN) ? 32 * App.PIXEL_DENSITY : 120 * App.PIXEL_DENSITY);
        mPathBar.setSize(pathBarWidth, FloatMath.ceil(39 * App.PIXEL_DENSITY));
        mPathBar.recomputeComponents();
    }

    public void setFeed(MediaFeed feed, int state, boolean needsLayout) {
    }

    public void onGridStateChanged() {
        updateViews();
    }

    private void updateViews() {
        if (mGridLayer == null)
            return;
        final int state = mGridLayer.getState();
        // Show the selection menu in selection mode.
        final boolean selectionMode = mMode == MODE_SELECT;
        final boolean fullscreenMode = state == GridLayer.STATE_FULL_SCREEN;
       // final boolean stackMode = state == GridLayer.STATE_MEDIA_SETS || state == GridLayer.STATE_TIMELINE;
//        mSelectionMenuTop.setHidden(!selectionMode || fullscreenMode);
//        mSelectionMenuBottom.setHidden(!selectionMode);
        mFullscreenMenu.setHidden(!fullscreenMode || selectionMode || hiddenhud); 
        mZoomInButton.setHidden(!fullscreenMode || selectionMode);
        mZoomOutButton.setHidden(!fullscreenMode || selectionMode);

        mTopRightButton.setHidden(true);
        mPathBar.setHidden(true);
    }

    public PathBarLayer getPathBar() {
        return mPathBar;
    }

    public GridLayer getGridLayer() {
        return mGridLayer;
    }

    @Override
    public boolean update(RenderView view, float frameInterval) {
        float factor = 1.0f;
        if (mAlpha == 1.0f) {
            // Speed up the animation when it becomes visible.
            factor = 4.0f;
        }
        mAnimAlpha = FloatUtils.animate(mAnimAlpha, mAlpha, frameInterval * factor);

        if (mAutoHide) {
            if (mAlpha == 1.0f && mMode != MODE_SELECT) {
                long now = System.currentTimeMillis();
                if (now - mLastTimeFullOpacity >= 5000) {
                    setAlpha(0);
                }
            }
        }

        return (mAnimAlpha != mAlpha);
    }

    public void renderOpaque(RenderView view, GL11 gl) {

    }

    public void renderBlended(RenderView view, GL11 gl) {
        view.setAlpha(mAnimAlpha);
    }

    public void setAlpha(float alpha) {
        float oldAlpha = mAlpha;
        mAlpha = alpha;
        if(alpha == 0){
        	top.setAlpha(alpha);
        }
        
        if (oldAlpha != alpha) {
            if (mView != null)
                mView.requestRender();
        }

        // We try to invoke update() again in 5 seconds to see if
        // auto hide is needed.
        if (alpha == 1.0f) {
            mLastTimeFullOpacity = System.currentTimeMillis();
            App.get(mContext).getHandler().postDelayed(new Runnable() {
                public void run() {
                    if (mView != null) {
                        mView.requestRender();
                    }
                }
            }, 5000);
        }
    }

    public float getAlpha() {
        return mAlpha;
    }

    public void setTimeBarTime(long time) {
        // mTimeBar.setTime(time);
    }

    @Override
    public void generate(RenderView view, RenderView.Lists lists) {
        lists.opaqueList.add(this);
        lists.blendedList.add(this);
        lists.hitTestList.add(this);
        lists.updateList.add(this);
        mTopRightButton.generate(view, lists);
        mZoomInButton.generate(view, lists);
        mZoomOutButton.generate(view, lists);
        mFullscreenMenu.generate(view, lists);
        mPathBar.generate(view, lists);
        top.generate(view, lists);
        // mLoadingLayer.generate(view, lists);
        mView = view;
    }

    @Override
    public boolean containsPoint(float x, float y) {
        return false;
    }

    public void cancelSelection() {
        closeSelectionMenu();
        setMode(MODE_NORMAL);
    }

    public void closeSelectionMenu() {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mMode == MODE_SELECT) {
            /*
             * setMode(MODE_NORMAL); ArrayList<MediaBucket> displayBuckets =
             * mGridLayer.getSelectedBuckets(); // use this list, and then clear
             * the items return true;
             */
            return false;
        } else {
            return false;
        }
    }

    public boolean isLoaded() {
        return mLoadingLayer.isLoaded();
    }

    void reset() {
        mLoadingLayer.reset();
    }

    public void fullscreenSelectionChanged(MediaItem item, int index, int count) {
        // request = new ReverseGeocoder.Request();
        // request.firstLatitude = request.secondLatitude = item.latitude;
        // request.firstLongitude = request.secondLongitude = item.longitude;
        // mGeo.enqueue(request);
        if (item == null)
            return;
        String location = index + "/" + count;
        mCachedCaption = item.mCaption;
        mCachedPosition = location;
        mCachedCurrentLabel = location;
        mPathBar.changeLabel(location);
    }

    private void showToastForImage() {
        // Get the first selected item. Wire this up to multiple-item intents
        // when we move
        // to Eclair.
    	Object datasource = mGridLayer.getDataSource();
    	//Log.v("DataSource", datasource.toString());
    	if(datasource.getClass() == MediaPhotoSource.class){
    		MediaPhotoSource p = (MediaPhotoSource)datasource;
    		int currentSelectedSlot = mGridLayer.getInputProcessor().getCurrentSelectedSlot();
    		imagedata = p.getPhotoInterfaceFor(currentSelectedSlot);
    		top.updatePhotoInfo(imagedata);
    		top.setAlpha(mAlpha);
    	}
    }
    
    public void updateInfoLayer(){
    	 // to Eclair.
    	Object datasource = mGridLayer.getDataSource();
    	//Log.v("DataSource", datasource.toString());
    	if(datasource.getClass() == MediaPhotoSource.class){
    		MediaPhotoSource p = (MediaPhotoSource)datasource;
    		int currentSelectedSlot = mGridLayer.getInputProcessor().getCurrentSelectedSlot();
    		imagedata = p.getPhotoInterfaceFor(currentSelectedSlot);
    		top.updatePhotoInfo(imagedata);
    	}
    }
    
    com.fuzz.android.ui.PhotoInterface imagedata;
    private void updateShareMenu() {
        // Get the first selected item. Wire this up to multiple-item intents
        // when we move
        // to Eclair.
    	Object datasource = mGridLayer.getDataSource();
    	//Log.v("DataSource", datasource.toString());
    	if(datasource.getClass() == MediaPhotoSource.class){
    		MediaPhotoSource p = (MediaPhotoSource)datasource;
    		int currentSelectedSlot = mGridLayer.getInputProcessor().getCurrentSelectedSlot();
    		imagedata = p.getPhotoInterfaceFor(currentSelectedSlot);
    		
    		mHandler.post(executeShare);
    	}
    }
    
    Runnable executeShare = new Runnable(){
    	public void run(){
    		shareMe();
    	}
    };
    
    public void shareMe(){
    
    }
    
    
    
    boolean sharing = false;
   

    Runnable executetoast = new Runnable(){
    	public void run(){
    		Toast.makeText(mContext, imagedata.getSubtitle(), Toast.LENGTH_LONG);
    	}
    };

    public void autoHide(boolean hide) {
        mAutoHide = hide;
    }

    public void swapFullscreenLabel() {
        mCachedCurrentLabel = (mCachedCurrentLabel == mCachedCaption || mCachedCaption == null) ? mCachedPosition : mCachedCaption;
        mPathBar.changeLabel(mCachedCurrentLabel);
    }

    public void clear() {

    }

    public void enterSelectionMode() {
        
    }

    public void computeBottomMenu() {
    }

    public Layer getMenuBar() {
        return mFullscreenMenu;
    }

    public void hideZoomButtons(boolean hide) {
        mZoomInButton.setHidden(hide);
        mZoomOutButton.setHidden(hide);
    }
    
    boolean hiddenhud = false;
    public void setHUDHidden(boolean val){
    	hiddenhud = val;
    }

	
    public void updateNumItemsSelected(int numItems) {
		// TODO Auto-generated method stub
    	//String items = " " + ((numItems == 1) ? mContext.getString(Res.string.item) : mContext.getString(Res.string.items));
        //Menu menu = new MenuBar.Menu.Builder(numItems + items).config(MenuBar.MENU_TITLE_STYLE_TEXT).build();
        
	}

	
}
