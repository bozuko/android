package com.cooliris.media;

import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import com.cooliris.app.App;
import com.cooliris.app.Res;
import com.cooliris.media.RenderView.Lists;

public class ProgressLayer extends Layer {
	private String mText = "Photos";
	private String last = "";
	
	StringTexture titleTexture;
	public static final StringTexture.Config INFO_TEXT_STYLE = new StringTexture.Config();
	
	static {
		INFO_TEXT_STYLE.fontSize = 20 * App.PIXEL_DENSITY;
		INFO_TEXT_STYLE.bold = true;
		INFO_TEXT_STYLE.sizeMode = StringTexture.Config.SIZE_EXACT;
		INFO_TEXT_STYLE.overflowMode = StringTexture.Config.OVERFLOW_ELLIPSIZE;
	}

	public ProgressLayer(Context context){
	}

	@Override
	public void generate(RenderView view, Lists lists) {
		// TODO Auto-generated method stub
		lists.updateList.add(this);
		lists.blendedList.add(this);
	}

	@SuppressWarnings("static-access")
	public void renderBlended(RenderView view, GL11 gl) {
		//view.setAlpha(mAnimAlpha);

		Texture background = view.getResource(Res.drawable.selection_menu_bg);
		view.draw2D(background, mX, mY, mWidth, mHeight);

		if(mText.compareTo(last) != 0){
			last = mText;
			titleTexture = new StringTexture(mText, INFO_TEXT_STYLE, (int) (mWidth-5), INFO_TEXT_STYLE.height);
			view.loadTexture(titleTexture);
		}
		int width = StringTexture.computeTextWidthForConfig(mText, INFO_TEXT_STYLE);
		view.draw2D(titleTexture, mX + (mWidth/2) - (width/2), mY + 10 + (mHeight/2) - (INFO_TEXT_STYLE.height/2));
	}

	public void updateProgress(String text) {
		// TODO Auto-generated method stub
		mText = text;
	}

	float HEIGHT = 60;
	
	public float getHSize() {
		// TODO Auto-generated method stub
		//
		return HEIGHT*App.PIXEL_DENSITY;
	}

}
