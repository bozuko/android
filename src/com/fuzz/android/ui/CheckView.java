package com.fuzz.android.ui;

import com.fuzz.android.globals.Res;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

@SuppressWarnings("static-access")
public class CheckView extends RelativeLayout {
	RelativeLayout _cell;
	FrameLayout _contentView;
	ImageView _imageView; 
	
	public CheckView(Context mContext) {
		super(mContext);
		// TODO Auto-generated constructor stub
		createUI(mContext);
	}
	
	public void createUI(Context mContext){
		//float density = mContext.getResources().getDisplayMetrics().density;
		
//		_cell = new RelativeLayout(mContext);
//		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
//		params.setMargins(0, 0, 0, 0);
//		_cell.setLayoutParams(params);
//		_cell.setId(99);
//		addView(_cell);
		
		_imageView = new ImageView(mContext);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0,0, 0);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,1);
		params.addRule(RelativeLayout.CENTER_VERTICAL,1);
		_imageView.setLayoutParams(params);
		_imageView.setId(100);
		addView(_imageView);
		
		_contentView = new FrameLayout(mContext);
		params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT,1);
		params.addRule(RelativeLayout.LEFT_OF,100);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP,1);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,1);
		//params.setMargins((int)(5*density),(int)(0*density),(int)(5*density),(int)(5*density));
		_contentView.setLayoutParams(params);
		addView(_contentView);
	}

	public void setContentView(View inView){
		_contentView.removeAllViews();
		_contentView.addView(inView);
	}
	
	public void setImage(int resource){
		_cell.setBackgroundResource(resource);
	}
	
	public View getContentView(){
		return _contentView.getChildAt(0);
	}
	
	public void showArrowWhite(boolean show){
		if(show){
			_imageView.setImageResource(Res.drawable.arrowwhite);
		}else{
			_imageView.setImageResource(0);
		}
	}
	
	public void showArrow(boolean show){
		if(show){
			_imageView.setImageResource(Res.drawable.arrowgrey);
		}else{
			_imageView.setImageResource(0);
		}
	}
	
	public void showCheckmark(boolean show){
		if(show){
			_imageView.setImageResource(Res.drawable.checkmark);
		}else{
			_imageView.setImageResource(0);
		}
	}
}
