package com.cooliris.media;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL11;

import android.content.Context;

import com.cooliris.app.App;
import com.cooliris.app.Res;
import com.cooliris.media.RenderView.Lists;
import com.fuzz.android.ui.PhotoInterface;

@SuppressWarnings("static-access")
public class CaptionLayer extends Layer {
	private RenderView mView = null;
	PhotoInterface img;
	String last = "";
	public static final StringTexture.Config INFO_TEXT_STYLE = new StringTexture.Config();
	//StringTexture titleTexture;
	
	
	ArrayList<StringTexture> textures = new ArrayList<StringTexture>();
	
	static {
		INFO_TEXT_STYLE.fontSize = 12 * App.PIXEL_DENSITY;
		INFO_TEXT_STYLE.bold = false;
		INFO_TEXT_STYLE.sizeMode = StringTexture.Config.SIZE_EXACT;
		INFO_TEXT_STYLE.overflowMode = StringTexture.Config.OVERFLOW_ELLIPSIZE;
	}

	public CaptionLayer(Context context){
	}

	@Override
	public void generate(RenderView view, Lists lists) {
		// TODO Auto-generated method stub
		lists.updateList.add(this);
		lists.blendedList.add(this);
		//lists.opaqueList.add(this);
		mView = view;
		
	}

	public void renderBlended(RenderView view, GL11 gl) {
		//view.setAlpha(mAnimAlpha);
		if(img!=null){
			float measurewidth = StringTexture.computeTextWidthForConfig(img.getSubtitle().replace("\n", ""), INFO_TEXT_STYLE);
			float lines = (measurewidth / mWidth) + 1;
			HEIGHT = (lines*INFO_TEXT_STYLE.height);
			
			this.setPosition(0f, DHEIGHT - HEIGHT);
	        this.setSize(mWidth, HEIGHT);
	        
	        if(img.getSubtitle().compareTo("")==0){
	        	HEIGHT = 0;
	        	this.setPosition(0f, DHEIGHT);
		        this.setSize(mWidth, 0);
	        }
		}
		if(HEIGHT != 0){
		
		Texture background = view.getResource(Res.drawable.selection_menu_bg);
		float mY = DHEIGHT - HEIGHT;
//		int backgroundHeight = background.getHeight();
//        int menuHeight = (int) (HEIGHT * App.PIXEL_DENSITY + 0.5f);
//        @SuppressWarnings("unused")
//		int extra = backgroundHeight - menuHeight;
		view.draw2D(background, mX, mY - ((HEIGHT * App.PIXEL_DENSITY) - HEIGHT) + 20, mWidth, HEIGHT * App.PIXEL_DENSITY);

		if(img != null){
			if(((String) img.getID()).compareTo(last) != 0){
				last = (String) img.getID();
				textures.clear();
				//titleTexture = new StringTexture((String) img.getSource(), INFO_TEXT_STYLE, (int) mWidth , INFO_TEXT_STYLE.height);
				float measurewidth = StringTexture.computeTextWidthForConfig(img.getSubtitle(), INFO_TEXT_STYLE);
				float lines = (measurewidth / mWidth) + 1;
				StringTexture titleTexture = null;
				String temp[] =img.getSubtitle().replace("\n", "").split(" ");
				int cur = 0;
				for(int i=0; i<lines && cur<temp.length; i++){
					float lwidth = 0;
					String line = "";
					while(lwidth < mWidth && cur < temp.length){
						line += temp[cur++] + " ";
//						Log.v("Line",line);
						lwidth = StringTexture.computeTextWidthForConfig(line, INFO_TEXT_STYLE);
					}
					
					line = line.trim();
					if(lwidth > mWidth && cur<=temp.length && cur>0){
						cur--;
						line = line.substring(0, line.length()-(temp[cur].length()+1));
					}
					
					titleTexture = new StringTexture(line, INFO_TEXT_STYLE, (int) (mWidth-5), INFO_TEXT_STYLE.height);
					view.loadTexture(titleTexture);
					textures.add(titleTexture);
				}
			}
		}
		
		for(int i=0; i<textures.size(); i++){
			view.draw2D(textures.get(i), mX+5, mY + (i * (INFO_TEXT_STYLE.height)));
		}
		
		}
	}

	public void updatePhotoInfo(PhotoInterface imagedata) {
		// TODO Auto-generated method stub
		img = imagedata;
		if(img!=null){
//			float measurewidth = StringTexture.computeTextWidthForConfig((String) img.getSource(), INFO_TEXT_STYLE);
//			float lines = (measurewidth / mWidth) + 1;
//			HEIGHT = lines*INFO_TEXT_STYLE.height;
//			
//			this.setPosition(0f, DHEIGHT - (HEIGHT * App.PIXEL_DENSITY));
//	        this.setSize(mWidth, HEIGHT);
			
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
		//
		return HEIGHT*App.PIXEL_DENSITY;
	}

}
