package com.bozuko.bozuko.views;

import java.util.Timer;
import java.util.TimerTask;
import com.bozuko.bozuko.R;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SequencerImageView extends ImageView {

	//HashMap<Integer,SoftReference<BitmapDrawable>> resourcesLoaded = new HashMap<Integer,SoftReference<BitmapDrawable>>();
	
	int current = 0;
	int resources[];
	Timer timer;
	SequencerListener mListener;
	
	public SequencerImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public SequencerImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public SequencerImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void setResources(int inResources[]){
		//SequencerCache.getSharedInstance().setResources(inResources,getResources());
		//
		resources = inResources;
		current = 0;
		setImageResource(R.drawable.blank);
	}
	
	public int[] getResource(){
		return resources;
	}
	
	public void startSequence(){
		timer = new Timer();
		//timer.schedule(new SequencerTask(), 0, 100);
	}
	
	public void stopSequence(){
		try{
			timer.cancel();
			timer.purge();
			timer = null;
		}catch(Throwable t){
			
		}
		setImageResource(R.drawable.blank);
		current = 0;
	}
	
	protected class SequencerTask extends TimerTask{
		public void run(){	
			post(runnable);
		}
	}
	
	Runnable runnable = new Runnable(){
		public void run(){
			
			BitmapDrawable bitmap = new BitmapDrawable(SequencerCache.getSharedInstance().getBitmap(resources[current++], getResources()));
			setImageDrawable(bitmap);
			//setImageResource(resources[current++]);
			if(current == resources.length){
				current = 0;
				if(mListener != null){
					mListener.SequenceCompleted(SequencerImageView.this);
				}
			}
		}
	};
	
	public void setSequencerListener(SequencerListener inListener){
		mListener = inListener;
	}
	
	public interface SequencerListener{
		public void SequenceCompleted(SequencerImageView v);
		
	}
	
}
