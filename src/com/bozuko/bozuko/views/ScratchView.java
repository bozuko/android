package com.bozuko.bozuko.views;

import java.util.Timer;
import java.util.TimerTask;
import com.bozuko.bozuko.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;

public class ScratchView extends Button {

	//HashMap<Integer,SoftReference<Bitmap>> resourcesLoaded = new HashMap<Integer,SoftReference<Bitmap>>();
	
	
	int current = 0;
	int resources[] = AnimationSequence.SCRATCH_MASK;
	Timer timer;
	ScratchListener mListener;
	String number = "";
	String prize = "";
	
	public int HEIGHT = 0;
	public int WIDTH = 0;
	
	private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	public void basicSetup(){
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		setBackgroundResource(R.drawable.blank);
		
		setGravity(Gravity.CENTER|Gravity.TOP);
		setTextColor(Color.BLACK);
		setTextSize(10);
	}
	
	public ScratchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		basicSetup();
	}

	public ScratchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		basicSetup();
	}

	public ScratchView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		basicSetup();
	}
	
	public void startSequence(){
		if(current == resources.length-1){
			if(mListener != null){
				mListener.ScratchCompleted(ScratchView.this);
			}
			return;
		}
		
		if(timer == null){
			timer = new Timer();
			timer.schedule(new SequencerTask(), 0, 25);
		}
	}
	
	public void clearTicket(){
		current = 0;
		setBackgroundResource(R.drawable.blank);
		invalidate();
	}
	
	public void stopSequence(){
		try{
			timer.cancel();
			timer.purge();
			timer = null;
		}catch(Throwable t){
			
		}
	}
	
	protected class SequencerTask extends TimerTask{
		public void run(){			
			post(runnable);
		}
	}
	
	Runnable runnable = new Runnable(){
		public void run(){
			//setBackgroundResource(resources[current++]);
			BitmapDrawable bitmap = new BitmapDrawable(ScratchCache.getSharedInstance().getBitmap(resources[current++], getResources()));
			setBackgroundDrawable(bitmap);
//			
//			current++;
			invalidate();
			if(current == resources.length){
				current = resources.length-1;
				if(mListener != null){
					mListener.ScratchCompleted(ScratchView.this);
				}
				stopSequence();
			}

			
		}
	};
	
	//Bitmap bitmap;
	public void onDraw(Canvas canvas){
		  int sc = canvas.saveLayer(0, 0, WIDTH, HEIGHT, null,
                  Canvas.MATRIX_SAVE_FLAG |
                  Canvas.CLIP_SAVE_FLAG |
                  Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
                  Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
                  Canvas.CLIP_TO_LAYER_SAVE_FLAG);
		
		super.onDraw(canvas);
		 Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
         paint.setFilterBitmap(false);
        
      
        
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.outHeight = HEIGHT;
		opts.outWidth = WIDTH;
		opts.inPurgeable = true;
        
        //Bitmap original = BitmapFactory.decodeResource(getResources(), resources[resources.length-1], opts);
        Rect dst = new Rect(0,0,WIDTH,HEIGHT);
        //canvas.drawBitmap(original, null, dst, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
       
        Bitmap bitmap = ScratchCache.getSharedInstance().getBitmap(resources[current], getResources());
        //bitmap  = BitmapFactory.decodeResource(getResources(), resources[current], opts);
        canvas.drawBitmap(bitmap, null, dst, paint);
        paint.setXfermode(null);
        canvas.restoreToCount(sc);
		
		
		
		//canvas.drawBitmap(bitmap, 0, 0, mPaint);
		
		//canvas.restore();
	}
	
	public void setScratchListener(ScratchListener inListener){
		mListener = inListener;
	}
	
	public interface ScratchListener{
		public void ScratchCompleted(ScratchView v);
		
	}

	
	public void setScratched() {
		// TODO Auto-generated method stub
		current = resources.length-1;
		
		setBackgroundResource(resources[current]);
		invalidate();
	}

	
	
	public void setNumber(String inNumber) {
		// TODO Auto-generated method stub
		number = inNumber;
	}
	
	public void setPrize(String inPrize) {
		// TODO Auto-generated method stub
		prize = inPrize;
	}
	
	public String getNumber(){
		return number;
	}
	
	public String getPrize(){
		return prize;
	}
}
