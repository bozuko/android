package com.fuzz.android.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import com.fuzz.android.concurrent.WorkQueue;
import com.fuzz.android.datahandler.DataBaseHelper;
import com.fuzz.android.globals.GlobalEnum;
import com.fuzz.android.globals.GlobalFunctions;
import com.fuzz.android.globals.Res;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ProgressBar;

@SuppressWarnings("static-access")
public class URLImageView extends TouchImageView{

	boolean loading = false;
	final Handler mHandler = new Handler();
	Drawable drawable = null;
	GetImageOperation imageopt;
	GetImageFromWeb imageweb;
	String url;
	ProgressBar progress;
	int placeholder = 0;
	public int attempts = 0;
	
	public static final int FOREGROUND = 0;
	public static final int BACKGROUND = 1;
	private int LOAD_TYPE = FOREGROUND;

	public void setPlaceHolder(int resource){
		placeholder = resource;
	}

	public URLImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public URLImageView(Context context, AttributeSet attrs){
		super(context, attrs);
		//focus = R.drawable.green;
	}

	public void setURL(String u){
		if(TYPE == COVERFLOW_IMAGE){
			if(drawable != null){
				return;
			}
		}

		if(loading){
			if(imageopt != null){
				WorkQueue.getInstance().remove(imageopt);
			}
			if(imageweb != null){
				WorkQueue.getInstance().remove(imageweb);
			}
		}

		if(u == null){
			setToDefault();
			return;
		}
		
		if(url != null){
			if(url.compareToIgnoreCase(u)==0){
				return;
			}
		}
		
		url = u;

		boolean ret = false;
		if(TYPE == CROSSFADE_IMAGE){
			ret = DataBaseHelper.isImageCached(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url),1024),this.getContext());
			
		}else{
			ret = DataBaseHelper.isImageCached(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url),128),this.getContext());
		}

		//Log.v("Logging Set URL", u);
		if(ret){
			setToDefault();
			imageopt = new GetImageOperation(this,url);
			WorkQueue.getInstance().execute(imageopt);
			loading = true;	
		}else{
			setToDefault();
			imageweb = new GetImageFromWeb(this,url);
			WorkQueue.getInstance().execute(imageweb);
			loading = true;		
		}

	}

	public void setToDefault(){
		
		switch(LOAD_TYPE){
			case FOREGROUND:
				setImageDrawable(null);
				break;
			case BACKGROUND:
				setBackgroundDrawable(null);
				break;
		}
		if(drawable != null){
			drawable.setCallback(null);
			if(TYPE == COVERFLOW_IMAGE || TYPE == THUMBNAIL_IMAGE){
				((BitmapDrawable)drawable).getBitmap().recycle();
			}else if(TYPE == CROSSFADE_IMAGE){
				//((CrossFadeDrawable)drawable).getStart().recycle();
				//((CrossFadeDrawable)drawable).getEnd().recycle();
			}else{
				((BitmapDrawable)drawable).getBitmap().recycle();
			}
			drawable = null;
		}
		switch(LOAD_TYPE){
			case FOREGROUND:
				setImageDrawable(null);
				break;
			case BACKGROUND:
				setBackgroundDrawable(null);
				break;
		}
		
		if(placeholder != 0){
			try{
				switch(LOAD_TYPE){
					case FOREGROUND:
						setImageResource(placeholder);
						break;
					case BACKGROUND:
						setBackgroundResource(placeholder);
						break;
				}
				
			}catch(java.lang.OutOfMemoryError e){
				//e.printStackTrace();
			}
		}
	}

	final Runnable setImageFailedToLoad = new Runnable() {
		public void run() {	
			loading = false;
			if(mListener != null){
				mListener.imageDidFailLoad();
			}
			if(imageopt != null){
				imageopt = null;
			}
			if(TYPE == CROSSFADE_IMAGE)
				setURL(url);
		}
	};

	final Runnable setImageFailedToDownload = new Runnable() {
		public void run() {	
			loading = false;
			if(imageweb != null){
				imageweb = null;
			}
			if(mListener != null){
				mListener.imageDidFailLoad();
			}
			if(TYPE == CROSSFADE_IMAGE)
				setURL(url);
		}
	};

	boolean doFit = false;
	public void setDoFit(boolean inDoFit){
		doFit = inDoFit;
	}
	
	// Setup the base matrix so that the image is centered and scaled properly.
	final Runnable setImageLoaded = new Runnable() {
		public void run() {	
			loading = false;
			if(progress != null){
				progress.setVisibility(View.GONE);
			}

			if(doFit && ScaleType.FIT_CENTER == getScaleType() && drawable != null){
				float h = (40*getResources().getDisplayMetrics().density);
				float dh = drawable.getIntrinsicHeight();
				float dw = drawable.getIntrinsicWidth();
				int w = Math.round((dw/dh) * h);
				setMinimumWidth(w);
				setMaxWidth(w);
				invalidate();
			}
			
			if(drawable != null){
				switch(LOAD_TYPE){
					case FOREGROUND:
						setImageDrawable(drawable);
						break;
					case BACKGROUND:
						setBackgroundDrawable(drawable);
						break;
				}
			}
			
			if(TYPE == CROSSFADE_IMAGE){
				((CrossFadeDrawable)drawable).startTransition(2000);
			}

			//mMaxZoom=maxZoom();
			if(imageopt != null){
				imageopt = null;
			}

			if(mListener != null){
				if(drawable != null){
					mListener.imageDidLoad();
				}else{
					mListener.imageDidFailLoad();
				}
			}
		}
	};

	final Runnable setImageDownloaded = new Runnable() {
		public void run() {	
			loading = false;
			if(progress != null){
				progress.setVisibility(View.GONE);
			}


			if(doFit && ScaleType.FIT_CENTER == getScaleType() && drawable != null){
				float h = (40*getResources().getDisplayMetrics().density);
				float dh = drawable.getIntrinsicHeight();
				float dw = drawable.getIntrinsicWidth();
				int w = Math.round((dw/dh) * h);
				setMinimumWidth(w);
				setMaxWidth(w);
				invalidate();
			}

			if(drawable != null){
				switch(LOAD_TYPE){
					case FOREGROUND:
						setImageDrawable(drawable);
						break;
					case BACKGROUND:
						setBackgroundDrawable(drawable);
						break;
				}
			}

			if(TYPE == CROSSFADE_IMAGE){
				((CrossFadeDrawable)drawable).startTransition(2000);
			}

			if(imageweb != null){
				imageweb = null;
			}
			if(mListener != null){
				if(drawable != null){
					mListener.imageDidLoad();
				}else{
					mListener.imageDidFailLoad();
				}
			}
		}
	};

	final static String URI_CACHE = GlobalEnum.URI_CACHE_BASE;
	
	public static final String createFilePathFromCrc64(long crc64, int maxResolution) {
		String ret = URI_CACHE + crc64 + "_" + maxResolution + ".cache";
		//		Log.v("cacheurl",ret);
		return ret;
	}

	private class GetImageOperation implements Runnable{
		//URLImageView imgV;
		String url2;
		public GetImageOperation(URLImageView im, String u){
			//imgV = im;
			url2 = u;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//Drawable drawable;
			//File cache = imgV.getContext().getCacheDir();
			try{
				if(TYPE == CROSSFADE_IMAGE){
					Bitmap end = BitmapFactory.decodeFile(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url),1024), null);
					Bitmap start = BitmapFactory.decodeResource(getContext().getResources(), Res.drawable.black);
					drawable = new CrossFadeDrawable(start,end);
					((CrossFadeDrawable)drawable).setCrossFadeEnabled(true);

				}else{
					drawable = Drawable.createFromPath(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),128));
				}
				
				
				if(drawable == null){
					try{
						File f = new File(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),128));
						f.delete();
					}catch(Throwable t){
					}
					try{
						File f = new File(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),1024));
						f.delete();
					}catch(Throwable t){
						
					}
				}
				mHandler.post(setImageLoaded);
			}catch(Throwable e){
				mHandler.post(setImageFailedToLoad);
			}

		}
	}

//	private void DEBUGCONNECTION(HttpURLConnection conn){
//		try {
//			Log.v("Response Message",conn.getResponseMessage());
//			Log.v("Response Code",conn.getResponseCode() + "");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		Map<String, List<String>> headers = conn.getHeaderFields();
//		for(String s : headers.keySet()){
//			List<String> header = headers.get(s);
//			for(int i=0; i<header.size(); i++){
//				Log.v("HeaderInfo","Header: " + s + " Field: " + header.get(i));
//			}
//		}
//	}
	
	private class GetImageFromWeb implements Runnable{

		//URLImageView imgV;
		String url2;
		public GetImageFromWeb(URLImageView im, String u){
			//imgV = im;
			url2 = u;
		}

		public void downloadNow(int time){
			try{
			String tmpURL = url2;
			URL u;
			//File cache = imgV.getContext().getCacheDir();
			URLConnection conn;
			do{
				u = new URL(tmpURL);
				conn = u.openConnection();
				conn.setRequestProperty("Content-Language", "en-US");
				conn.setRequestProperty("User-Agent", "Android/SuperGlued");
				conn.setUseCaches(false);
				conn.setDoInput(true);
				conn.connect();
				tmpURL = conn.getHeaderField("location");
			} while(conn.getHeaderField("location")!= null);
			
			
			InputStream inputStream = conn.getInputStream();
			
			//cache.
			//cache.
			FileOutputStream out;
			if(TYPE == CROSSFADE_IMAGE || TYPE == COVERFLOW_IMAGE || TYPE == THUMBNAIL_IMAGE){
				out = new FileOutputStream(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),1024));
			}else{
				out = new FileOutputStream(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),128));
			}
			//OutputStream out = imgV.getContext().openFileOutput(DataBaseHelper.localFileForUrl(url2), 0);
			byte buf[] = new byte[1024];
			int len;
			while ((len = inputStream.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			inputStream.close();
			}catch(Throwable t){
				if(time < 20000){
					try{
						Thread.sleep(2000);
						downloadNow(time+2000);
					}catch(Throwable t2){
						
					}
				}
			}
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				//Log.v("Adding Image to Collection", url2);
				downloadNow(0);
				if(TYPE==COVERFLOW_IMAGE){
					BitmapFactory.Options opts = new BitmapFactory.Options();
					DisplayMetrics metrics = new DisplayMetrics();
					((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

					opts.outHeight = (int)(150*metrics.density);
					opts.outWidth = (int)(150*metrics.density);
					//opts.inScreenDensity = metrics.densityDpi;
					opts.inPurgeable = true;
					opts.inSampleSize = 4;
					Bitmap bitmap = BitmapFactory.decodeFile(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url),1024), opts);
					FileOutputStream out2 = new FileOutputStream(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),128));
					bitmap.compress(CompressFormat.PNG, 100, out2);
					out2.flush();
					out2.close();
					drawable = Drawable.createFromPath(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),128));
					createReflectedImages();
				}else if(TYPE == THUMBNAIL_IMAGE){
					BitmapFactory.Options opts = new BitmapFactory.Options();
					DisplayMetrics metrics = new DisplayMetrics();
					((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

					opts.outHeight = (int)(80*metrics.density);
					opts.outWidth = (int)(80*metrics.density);
					//opts.in
					//opts.inScreenDensity = metrics.densityDpi;
					opts.inPurgeable = true;
					opts.inSampleSize = 4;
					Bitmap bitmap = BitmapFactory.decodeFile(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url),1024), opts);
					FileOutputStream out2 = new FileOutputStream(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),128));
					bitmap.compress(CompressFormat.PNG, 100, out2);
					out2.flush();
					out2.close();

					drawable = Drawable.createFromPath(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),128));

				}else if(TYPE == CROSSFADE_IMAGE){
					Bitmap end = BitmapFactory.decodeFile(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url),1024), null);
					Bitmap start = BitmapFactory.decodeResource(getContext().getResources(), Res.drawable.black);
					drawable = new CrossFadeDrawable(start,end);
					((CrossFadeDrawable)drawable).setCrossFadeEnabled(true);

				}else{
					drawable = Drawable.createFromPath(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),128));
					
				}
				
				if(drawable == null){
					try{
						File f = new File(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),128));
						f.delete();
					}catch(Throwable t){
					}
					try{
						File f = new File(createFilePathFromCrc64(GlobalFunctions.Crc64Long(url2),1024));
						f.delete();
					}catch(Throwable t){
						
					}
				}
				
				mHandler.post(setImageDownloaded);
			}catch(Throwable e){
				//e.printStackTrace();
				mHandler.post(setImageFailedToDownload);
			}
		}

	}

	public void setProgressBar(ProgressBar v) {
		// TODO Auto-generated method stub
		progress = v;
	}

	public void stopLoading() {
		// TODO Auto-generated method stub
		if(loading){
			if(imageopt != null){
				WorkQueue.getInstance().remove(imageopt);
			}
			if(imageweb != null){
				WorkQueue.getInstance().remove(imageweb);
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top,
			int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		 if(getScaleType() == ScaleType.MATRIX){
	        	Matrix matrix = new Matrix();
	        	matrix.set(getImageMatrix());
	        	matrix.postRotate(-30, mThisWidth/2, mThisHeight/2);
	        	setImageMatrix(matrix);
	     }
	}
	
	public void setLoadType(int load_type) {
		// TODO Auto-generated method stub
		LOAD_TYPE = load_type;
	}

	
	OnLoadListener mListener;
	public void setOnLoadListener(OnLoadListener listener) {
		// TODO Auto-generated method stub
		mListener = listener;
	}

	public interface OnLoadListener{
		public void imageDidLoad();
		public void imageDidFailLoad();
	}
}
