package com.fuzz.android.ui;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import com.fuzz.android.concurrent.WorkQueue;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalEnum;
import com.fuzz.android.globals.GlobalFunctions;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.ColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.graphics.Paint;

public class URLBitmapDrawable extends Drawable {
	private Bitmap mBitmap;
	private BitmapState mBitmapState;
	private String mURL;
	final Handler mHandler = new Handler();
	private Context mContext;
	boolean mRebuildShader;
	private int mBitmapWidth;
    private int mBitmapHeight;
    private int mTargetDensity;
    private double mTargetDPI;
    
    public int TYPE = NORMAL_IMAGE;
	public static final int NORMAL_IMAGE = 0;
	public static final int COVERFLOW_IMAGE = 1;
	public static final int THUMBNAIL_IMAGE = 2;
	public static final int CROSSFADE_IMAGE = 3;
    
    GetImageOperation imageopt;
    GetImageFromWeb imageweb;

	public URLBitmapDrawable(int rid, String url,Context context) {
		mBitmapState = new BitmapState((Bitmap) null);
		setTargetDensity(context.getResources().getDisplayMetrics());
		try{
			mBitmap = BitmapFactory.decodeResource(context.getResources(),rid);
		}catch(Throwable t){
			mBitmap = Bitmap.createBitmap(10,10,Bitmap.Config.ARGB_8888);
		}
		computeBitmapSize();
		mURL = url;
		mContext = context;
		if(DataBaseHelper.isImageCached(createFilePathFromCrc64(GlobalFunctions.Crc64Long(mURL),128),mContext)){
			imageopt = new GetImageOperation(mURL);
			WorkQueue.getInstance().execute(imageopt);
		}else{
			imageweb = new GetImageFromWeb(mURL);
			WorkQueue.getInstance().execute(imageweb);
		}
		mRebuildShader = true;
	}
	
	public URLBitmapDrawable(Bitmap placeHolder, String url,
			Context context) {
		// TODO Auto-generated constructor stub
		mBitmapState = new BitmapState((Bitmap) null);
		setTargetDensity(context.getResources().getDisplayMetrics());
		mBitmap = placeHolder;
		computeBitmapSize();
		mURL = url;
		mContext = context;
		if(DataBaseHelper.isImageCached(createFilePathFromCrc64(GlobalFunctions.Crc64Long(mURL),128),mContext)){
			imageopt = new GetImageOperation(mURL);
			WorkQueue.getInstance().execute(imageopt);
		}else{
			imageweb = new GetImageFromWeb(mURL);
			WorkQueue.getInstance().execute(imageweb);
		}
		mRebuildShader = true;
	}
	
	public URLBitmapDrawable(Bitmap placeHolder, String url,
			Context context,int type) {
		// TODO Auto-generated constructor stub
		TYPE = type;
		mBitmapState = new BitmapState((Bitmap) null);
		setTargetDensity(context.getResources().getDisplayMetrics());
		mBitmap = placeHolder;
		computeBitmapSize();
		mURL = url;
		mContext = context;
		if(DataBaseHelper.isImageCached(createFilePathFromCrc64(GlobalFunctions.Crc64Long(mURL),128),mContext)){
			imageopt = new GetImageOperation(mURL);
			WorkQueue.getInstance().execute(imageopt);
		}else{
			imageweb = new GetImageFromWeb(mURL);
			WorkQueue.getInstance().execute(imageweb);
		}
		mRebuildShader = true;
	}
	
	public Bitmap createReflectedImages(Bitmap inBitmap) {
		//The gap we want between the reflection and the original image
		final int reflectionGap = 4;
		
		Bitmap originalImage = inBitmap;


		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		//Create a Bitmap with the flip matrix applied to it.
		//We only want the bottom half of the image
		Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height/2, width, height/2, matrix, false);


		//Create a new bitmap with same width but taller to fit reflection
		Bitmap bitmapWithReflection = Bitmap.createBitmap(width 
				, (height + height/2), Config.ARGB_8888);

		//Create a new Canvas with the bitmap that's big enough for
		//the image plus gap plus reflection
		Canvas canvas = new Canvas(bitmapWithReflection);
		//Draw in the original image
		canvas.drawBitmap(originalImage, 0, 0, null);
		//Draw in the gap
		Paint deafaultPaint = new Paint();
		canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);
		//Draw in the reflection
		canvas.drawBitmap(reflectionImage,0, height + reflectionGap, null);

		//Create a shader that is a linear gradient that covers the reflection
		Paint paint = new Paint(); 
		LinearGradient shader = new LinearGradient(0, originalImage.getHeight(), 0, 
				bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff, 
				TileMode.CLAMP); 
		//Set the paint to use this shader (linear gradient)
		paint.setShader(shader); 
		//Set the Transfer mode to be porter duff and destination in
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN)); 
		//Draw a rectangle using the paint with our linear gradient
		canvas.drawRect(0, height, width, 
				bitmapWithReflection.getHeight() + reflectionGap, paint); 

		reflectionImage.recycle();
		return bitmapWithReflection;
	}
	
	 public void setTargetDensity(DisplayMetrics metrics) {
	        mTargetDensity = metrics.densityDpi;
	        mTargetDPI = metrics.density;
	        if (mBitmap != null) {
	            computeBitmapSize();
	        }
	    }

	public void setTargetDensity(Canvas canvas) {
        setTargetDensity(canvas.getDensity());
    }	
	
	public void setTargetDensity(int density) {
        mTargetDensity = density == 0 ? DisplayMetrics.DENSITY_DEFAULT : density;
        
        switch(mTargetDensity){
        	case DisplayMetrics.DENSITY_DEFAULT:
        		mTargetDPI = 1.0f;
        		break;
        	case DisplayMetrics.DENSITY_LOW:
        		mTargetDPI = 0.75f;
        		break;
        	case DisplayMetrics.DENSITY_HIGH:
        		mTargetDPI = 1.5f;
        		break;
        }
        if (mBitmap != null) {
            computeBitmapSize();
        }
    }
	
	@Override
	public void draw(Canvas canvas) {
		
		if(mBitmap == null){
			return;
		}
		if(mBitmap.isRecycled()){
			return;
		}
		setTargetDensity(canvas);
		Rect bounds = getBounds();
		final BitmapState state = mBitmapState;
		if (mRebuildShader) {
			Shader.TileMode tmx = state.mTileModeX;
			Shader.TileMode tmy = state.mTileModeY;

			if (tmx == null && tmy == null) {
				state.mPaint.setShader(null);
			} else {
				Shader s = new BitmapShader(mBitmap,
						tmx == null ? Shader.TileMode.CLAMP : tmx,
								tmy == null ? Shader.TileMode.CLAMP : tmy);
				state.mPaint.setShader(s);
			}
			mRebuildShader = false;
			copyBounds(bounds);
		}
		canvas.drawBitmap(mBitmap, null, bounds, state.mPaint);
		//canvas.drawBitmap(mBitmap, -mBitmap.getWidth()/2, -mBitmap.getHeight(), null);
	}
	
	private void computeBitmapSize() {
		if(mBitmap == null){
			mBitmapWidth = (int) 80;
	        mBitmapHeight = (int) 80;
	        setBounds(getBounds().left, getBounds().top,getBounds().left+mBitmapWidth, getBounds().top+mBitmapHeight);
		}
		
        mBitmapWidth = (int) (mBitmap.getWidth()*mTargetDPI);
        mBitmapHeight = (int) (mBitmap.getHeight()*mTargetDPI);
        setBounds(getBounds().left, getBounds().top,getBounds().left+mBitmapWidth, getBounds().top+mBitmapHeight);
    }

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha(int alpha) {
		mBitmapState.mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mBitmapState.mPaint.setColorFilter(cf);
	}

	@Override
	public int getIntrinsicWidth() {
		if(mBitmap == null){
			return 0;
		}
		if(mBitmap.isRecycled()){
			return 0;
		}
		return mBitmapWidth;
		//return mBitmap.getWidth();
	}

	@Override
	public int getIntrinsicHeight() {
		if(mBitmap == null){
			return 0;
		}
		if(mBitmap.isRecycled()){
			return 0;
		}
		return mBitmapHeight;
		//return mBitmap.getHeight();
	}

	public final Bitmap getBitmap() {
		return mBitmap;
	}
	
	Runnable run = new Runnable(){
		public void run(){
			invalidateSelf();
		}
	};
	
	final static String URI_CACHE = GlobalEnum.URI_CACHE_BASE;
	public static final String createFilePathFromCrc64(long crc64, int maxResolution) {
		String ret = URI_CACHE + crc64 + "_" + maxResolution + ".cache";
		return ret;
	}

	private class GetImageOperation implements Runnable{
		String url2;
		public GetImageOperation(String u){
			url2 = u;
		}
		@Override
		public void run() {
			imageopt = null;
			try{
				
				Bitmap copy = mBitmap;
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inPurgeable=true;
				opts.inScaled = false;

				Bitmap newB = BitmapFactory.decodeFile(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),128), opts);
				if(newB!= null){
					switch(TYPE){
						case NORMAL_IMAGE:
							mBitmap = newB;
							break;
						case COVERFLOW_IMAGE:
							mBitmap = createReflectedImages(newB);
							break;
					}
					computeBitmapSize();
					copy.recycle();
				}
				mRebuildShader = true;
				mHandler.post(run);
				
			}catch(Throwable e){
				//e.printStackTrace();
			}

		}
	}

	private class GetImageFromWeb implements Runnable{
		String url2;
		public GetImageFromWeb(String u){
			url2 = u;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			imageweb = null;
			
			try{
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inPurgeable=true;
				opts.inScaled = false;
				Bitmap copy = mBitmap;
				Bitmap newB = BitmapFactory.decodeFile(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),128), opts);
				if(newB!= null){
					
					switch(TYPE){
						case NORMAL_IMAGE:
							mBitmap = newB;
							break;
						case COVERFLOW_IMAGE:
							mBitmap = createReflectedImages(newB);
							break;
					}
					
					computeBitmapSize();
					copy.recycle();
					mRebuildShader = true;
					mHandler.post(run);
					
				}else{
					throw new Exception("FAILED");
				}
				
			}catch(Exception e1){
				//e1.printStackTrace();
				try {
					URL u = new URL(url2);
					URLConnection conn = u.openConnection();
					conn.setRequestProperty("Content-Language", "en-US");
					conn.setRequestProperty("User-Agent", "Mobile/Safari");
					conn.setUseCaches(false);
					conn.setDoInput(true);
					InputStream inputStream = conn.getInputStream();
					FileOutputStream out = new FileOutputStream(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),128));
					byte buf[] = new byte[1024];
					int len;
					while ((len = inputStream.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					out.close();
					inputStream.close();
					Bitmap copy = mBitmap;
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inPurgeable=true;
					opts.inScaled = false;
					Bitmap newB = BitmapFactory.decodeFile(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),128), opts);
					if(newB!= null){
						switch(TYPE){
							case NORMAL_IMAGE:
								mBitmap = newB;
								break;
							case COVERFLOW_IMAGE:
								mBitmap = createReflectedImages(newB);
								break;
						}
						computeBitmapSize();
						copy.recycle();
					}
					mRebuildShader = true;
					mHandler.post(run);
					
				}catch(Throwable e){
					//e.printStackTrace();
				}
			}
		}

	}

	private static final int DEFAULT_PAINT_FLAGS =
		Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG;
	final static class BitmapState extends ConstantState {
		Bitmap mmBitmap;
		int mChangingConfigurations;
		int mGravity = Gravity.FILL;
		Paint mPaint = new Paint(DEFAULT_PAINT_FLAGS);
		Shader.TileMode mTileModeX;
		Shader.TileMode mTileModeY;
		int mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;

		BitmapState(Bitmap bitmap) {
			mmBitmap = bitmap;
		}

		BitmapState(BitmapState bitmapState) {
			this(bitmapState.mmBitmap);
			mChangingConfigurations = bitmapState.mChangingConfigurations;
			mGravity = bitmapState.mGravity;
			mTileModeX = bitmapState.mTileModeX;
			mTileModeY = bitmapState.mTileModeY;
			mTargetDensity = bitmapState.mTargetDensity;
			mPaint = new Paint(bitmapState.mPaint);
		}

		@Override
		public Drawable newDrawable() {
			return new URLBitmapDrawable(this, null);
		}

		@Override
		public Drawable newDrawable(Resources res) {
			return new URLBitmapDrawable(this, res);
		}

		@Override
		public int getChangingConfigurations() {
			return mChangingConfigurations;
		}
	}

	private URLBitmapDrawable(BitmapState state, Resources res) {
		mBitmapState = state;
	}

	public void stopRequest() {
		// TODO Auto-generated method stub
		if(imageopt != null){
			WorkQueue.getInstance().remove(imageopt);
			imageopt = null;
		}
		
		if(imageweb != null){
			WorkQueue.getInstance().remove(imageweb);
			imageweb = null;
		}
	}
}
