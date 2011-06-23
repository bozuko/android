package com.cooliris.media;

import javax.microedition.khronos.opengles.GL11;
import android.content.Context;
import com.cooliris.app.App;
import com.cooliris.app.Res;
import com.cooliris.media.RenderView.Lists;
import com.fuzz.android.ui.PhotoInterface;

public class InfoLayer extends Layer {

	float mAlpha;
	private RenderView mView = null;
	private long mLastTimeFullOpacity;
	Context mContext;
	PhotoInterface img;

	private float mAnimAlpha;
	public static final StringTexture.Config INFO_TEXT_STYLE = new StringTexture.Config();

	static {
		INFO_TEXT_STYLE.fontSize = 15 * App.PIXEL_DENSITY;
		INFO_TEXT_STYLE.sizeMode = StringTexture.Config.SIZE_EXACT;
		INFO_TEXT_STYLE.overflowMode = StringTexture.Config.OVERFLOW_ELLIPSIZE;
	}

	public InfoLayer(Context context){
		mAlpha = 0.0f;
	}

	@Override
	public void generate(RenderView view, Lists lists) {
		// TODO Auto-generated method stub
		lists.updateList.add(this);
		lists.blendedList.add(this);
		lists.opaqueList.add(this);
		mView = view;
	}

	public void renderOpaque(RenderView view, GL11 gl) {

	}

	@SuppressWarnings("static-access")
	public void renderBlended(RenderView view, GL11 gl) {
		view.setAlpha(mAnimAlpha);

		Texture background = view.getResource(Res.drawable.selection_menu_bg);
		float mY = DHEIGHT - (HEIGHT);
		//int backgroundHeight = background.getHeight();
        //int menuHeight = (int) (HEIGHT * App.PIXEL_DENSITY + 0.5f);
        //int extra = backgroundHeight - menuHeight;
		view.draw2D(background, mX, mY-20, mWidth, HEIGHT+20);

		if(img != null){
			float measurewidth = StringTexture.computeTextWidthForConfig(img.getSubtitle(), INFO_TEXT_STYLE);
			float lines = (measurewidth / mWidth) + 1;
			StringTexture titleTexture = null;
			String temp[] =img.getSubtitle().split(" ");
			int cur = 0;
			for(int i=0; i<lines && cur<temp.length; i++){
				float lwidth = 0;
				String line = "";
				while(lwidth < mWidth && cur < temp.length){
					line += temp[cur++] + " ";
					lwidth = StringTexture.computeTextWidthForConfig(line, INFO_TEXT_STYLE);
				}
				
				line = line.trim();
				if(lwidth > mWidth){
					cur--;
					line = line.substring(0, line.length()-(temp[cur].length()+2));
				}
				
				titleTexture = new StringTexture(line, INFO_TEXT_STYLE, (int) (mWidth-5), INFO_TEXT_STYLE.height);
				view.loadTexture(titleTexture);
				view.draw2D(titleTexture, mX+5, mY + i * INFO_TEXT_STYLE.height );
			}
			
		}
	}

	@Override
	public boolean update(RenderView view, float frameInterval) {
		float factor = 1.0f;
		if (mAlpha == 1.0f) {
			// Speed up the animation when it becomes visible.
			factor = 4.0f;
		}
		mAnimAlpha = FloatUtils.animate(mAnimAlpha, mAlpha, frameInterval * factor);

		if (true) {
			if (mAlpha == 1.0f) {
				long now = System.currentTimeMillis();
				if (now - mLastTimeFullOpacity >= 5000) {
					setAlpha(0);
				}
			}
		}

		return (mAnimAlpha != mAlpha);
	}

	public void setAlpha(float alpha) {
		float oldAlpha = mAlpha;
		mAlpha = alpha;
		// TODO Auto-generated method stub
		if (oldAlpha != alpha) {
			if (mView != null)
				mView.requestRender();
		}

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

	public void setContext(Context context) {
		// TODO Auto-generated method stub
		if(context != mContext){
			mContext = context;
		}
	}

	public void updatePhotoInfo(PhotoInterface imagedata) {
		// TODO Auto-generated method stub
		img = imagedata;
		if(img!=null){
			float measurewidth = StringTexture.computeTextWidthForConfig(img.getSubtitle(), INFO_TEXT_STYLE);
			float lines = (measurewidth / mWidth) + 1;
			HEIGHT = lines*INFO_TEXT_STYLE.height;
			
			this.setPosition(0f, DHEIGHT - (HEIGHT * App.PIXEL_DENSITY));
	        this.setSize(mWidth, HEIGHT);
			
			if (mView != null)
				mView.requestRender();
			
		}
	}

	float DHEIGHT = 0;
	float HEIGHT = 60;
	
	public void setDHeight(float h){
		DHEIGHT = h;
	}
	
	public float getTextSize() {
		// TODO Auto-generated method stub
		
		return HEIGHT;
	}

}
