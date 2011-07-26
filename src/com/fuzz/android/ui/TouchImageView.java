package com.fuzz.android.ui;

import com.fuzz.android.math.Vector3f;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

public class TouchImageView extends ImageView {
	public int CROP_REGION = 40;
	
	public int TYPE = NORMAL_IMAGE;
	public static final int NORMAL_IMAGE = 0;
	public static final int COVERFLOW_IMAGE = 1;
	public static final int THUMBNAIL_IMAGE = 2;
	public static final int CROSSFADE_IMAGE = 3;
	public boolean isTouchable = false;
	int mThisWidth = -1, mThisHeight = -1;
	Matrix matrix2 = new Matrix();
	GestureDetector gesturedetector = new GestureDetector(new MyGestureDetector());
	ScaleType lastScaleType = ScaleType.CENTER;
	Matrix savedMatrix = new Matrix();
	int last = 0;
	float oldDist = 1f;
	float scale = 1f;
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	
	float WIDTH = 0;
	float HEIGHT = 0;
	
	PointF start = new PointF();
	PointF mid = new PointF();
	
	public TouchImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public TouchImageView(Context context, AttributeSet attrs){
		super(context, attrs);
		//focus = R.drawable.green;
	}
	
	public boolean createReflectedImages() {
		//The gap we want between the reflection and the original image
		final int reflectionGap = 4;
		Drawable drawable = getDrawable();
		
		Bitmap originalImage = ((BitmapDrawable)drawable).getBitmap();


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
		if(drawable != null){
			drawable.setCallback(null);
			((BitmapDrawable)drawable).getBitmap().recycle();
			drawable = null;
		}
		drawable = new BitmapDrawable(bitmapWithReflection);
		return true;
	}
	
	public void getProperBaseMatrix(Drawable bitmap, Matrix matrix) {
		if(bitmap == null)
			return;

		float viewWidth = getWidth();
		float viewHeight = getHeight();

		
		if(WIDTH == 0){
			WIDTH = bitmap.getIntrinsicWidth();
		}
		if(HEIGHT == 0){
			HEIGHT = bitmap.getIntrinsicHeight();
		}
		matrix.reset();

		float widthScale = Math.min(viewWidth / WIDTH, 3.0f);
		float heightScale = Math.min(viewHeight / HEIGHT, 3.0f);
		float scale2 = Math.min(widthScale, heightScale);
		//scale = scale2;

		matrix.postScale(scale2, scale2);

		matrix.postTranslate(
				(viewWidth  - WIDTH * scale2) / 2F,
				(viewHeight - HEIGHT * scale2) / 2F);
	}
		
	public float maxZoom() {
		Drawable drawable = getDrawable();
		
		if (drawable == null) {
			return 1F;
		}

		if(WIDTH == 0){
			WIDTH = drawable.getIntrinsicWidth();
		}
		if(HEIGHT == 0){
			HEIGHT = drawable.getIntrinsicHeight();
		}
		
		Matrix temp = new Matrix();
		temp.setRotate(rotation, WIDTH/2, HEIGHT/2);
		RectF rect = new RectF(0,0,WIDTH,HEIGHT);
		temp.mapRect(rect);
		float width = rect.right - rect.left;
		float height = rect.bottom - rect.top;
		
		float fw = (float) width  / (float) mThisWidth;
		float fh = (float) height / (float) mThisHeight;
		float max = Math.max(fw, fh) * 2;
		//Log.v("Max Zoom", max + "");
		return max;
	}

	public float minZoom() {
		Drawable drawable = getDrawable();
		if (drawable == null) {
			return 1F;
		}
		
		float viewWidth = getWidth()-(CROP_REGION*2);
		float viewHeight = getHeight()-(CROP_REGION*2);

		if(WIDTH == 0){
			WIDTH = drawable.getIntrinsicWidth();
		}
		if(HEIGHT == 0){
			HEIGHT = drawable.getIntrinsicHeight();
		}
		
		Matrix temp = new Matrix();
		temp.setRotate(rotation, WIDTH/2, HEIGHT/2);
		RectF rect = new RectF(0,0,WIDTH,HEIGHT);
		temp.mapRect(rect);
		float width = rect.right - rect.left;
		float height = rect.bottom - rect.top;
		
		float widthScale = Math.min(viewWidth / width, 3.0f);
		float heightScale = Math.min(viewHeight / height, 3.0f);
		float scale = Math.min(widthScale, heightScale);

		float fw = (float) (mThisWidth-(CROP_REGION*2)) / (float) width;
		float fh = (float) (mThisHeight-(CROP_REGION*2)) / (float) height;
		float min = Math.min(fw, fh) * 1;
		// Log.v("Min Zoom", min + "");
		return (scale > min ? scale : min);
	}
	
	@Override
	public void setImageDrawable(Drawable d){
		WIDTH=0;
		HEIGHT=0;
		super.setImageDrawable(d);
	}
	
	public void setImageResource(int d){
		WIDTH=0;
		HEIGHT=0;
		super.setImageResource(d);
	}
	
	public void setImageBitmap(Bitmap d){
		WIDTH=0;
		HEIGHT=0;
		super.setImageBitmap(d);
	}

	public float getScale() {
		// TODO Auto-generated method stub
		float values[] = new float[9];
		getImageMatrix().getValues(values);
		return values[Matrix.MSCALE_X];
	}

	protected boolean postTranslate(float dx, float dy) {
		Matrix mSuppMatrix = new Matrix();
		mSuppMatrix.set(getImageMatrix());

		float values[] = new float[9];
		mSuppMatrix.getValues(values);
		float transx = values[Matrix.MTRANS_X];
		float transy = values[Matrix.MTRANS_Y];
		float scale = values[Matrix.MSCALE_X];
		
		ImageView v = this;
		Drawable d = v.getDrawable();
		
		if(WIDTH == 0){
			WIDTH = d.getIntrinsicWidth();
		}
		if(HEIGHT == 0){
			HEIGHT = d.getIntrinsicWidth();
		}
		float width = WIDTH * scale;
		float height = HEIGHT * scale;
		
		float copyx = dx;

		if((transx + dx) + width < v.getWidth()){
			dx = 0;
		}

		if((transy + dy) + height < v.getHeight()){
			dy = 0;
		}

		if((transy + dy) > 0){
			dy = 0;
		}

		if((transx + dx) > 0){
			dx = 0;
		}

		if(dx == 0 && copyx != 0){
			if(dy < 10){
				return false;
			}
		}
		//Log.v("panning by", dx + " " + dy);
		mSuppMatrix.postTranslate(dx, dy);
		setImageMatrix(mSuppMatrix);
		return true;
	}

	public void postTranslateCenter(float dx, float dy) {
		postTranslate(dx, dy);
	}

	protected void zoomTo(Matrix mSuppMatrix,float scale, float centerX, float centerY) {
		if (scale > maxZoom()) {
			scale = maxZoom();
		}

		float oldScale = getScale();

		if(oldScale > maxZoom()){
			scale = 1.0f;
		}

		mSuppMatrix.postScale(scale, scale, centerX, centerY);
		setImageMatrix(mSuppMatrix);
	}

	protected void zoomOut(Matrix mSuppMatrix,float scale, float centerX, float centerY) {
		if (scale < minZoom()) {
			scale = minZoom();
		}

		float oldScale = getScale();

		if(oldScale < minZoom()){
			scale = 1.0f;
		}

		mSuppMatrix.postScale(scale, scale, centerX, centerY);
		setImageMatrix(mSuppMatrix);
	}

	public void zoomTo(Matrix matrix,float scale) {
		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;

		zoomTo(matrix, scale, cx, cy);
	}

	public void zoomToPoint(Matrix matrix,float scale, float pointX, float pointY) {
		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;

		panBy(cx - pointX, cy - pointY);
		zoomTo(matrix,scale, cx, cy);
	}

	public boolean panBy(float dx, float dy) {
		return postTranslate(dx, dy);
	}

	public Matrix getBaseMatrix(Drawable bitmap, Matrix matrix) {
		// TODO Auto-generated method stub

		if(bitmap == null)
			return matrix;

		float viewWidth = getWidth();
		float viewHeight = getHeight();

		if(WIDTH == 0){
			WIDTH = bitmap.getIntrinsicWidth();
		}
		if(HEIGHT == 0){
			HEIGHT = bitmap.getIntrinsicHeight();
		}
		float w = WIDTH;
		float h = HEIGHT;
		matrix.reset();

		// We limit up-scaling to 3x otherwise the result may look bad if it's
		// a small icon.
		float widthScale = Math.min(viewWidth / w, 3.0f);
		float heightScale = Math.min(viewHeight / h, 3.0f);
		float scale2 = Math.min(widthScale, heightScale);
		//scale = scale2;
		//matrix.postConcat(bitmap.get);
		matrix.postScale(scale2, scale2);

		matrix.postTranslate(
				(viewWidth  - w * scale2) / 2F,
				(viewHeight - h * scale2) / 2F);
		return matrix;
	}

	public Matrix getMatrix() {
		// TODO Auto-generated method stub
		return matrix2;
	}

	public void zoomOut(Matrix matrix, float scale) {
		// TODO Auto-generated method stub
		float cx = getWidth() / 2F;
		float cy = getHeight() / 2F;
		zoomOut(matrix,scale,cx,cy);
	}

	public void setMatrix(Matrix matrix) {
		// TODO Auto-generated method stub
		matrix2 = matrix;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top,
			int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mThisWidth = right - left;
		mThisHeight = bottom - top;
		
		 if(getScaleType() == ScaleType.MATRIX){
	        	getProperBaseMatrix(getDrawable(),getImageMatrix());
	     }
	}

	public void setTouchable(boolean touchable){
		isTouchable = touchable;
		if(touchable){
			lastScaleType = getScaleType();
			setScaleType(ScaleType.MATRIX);
		}else{
			setScaleType(lastScaleType);
		}
	}

	private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener{

		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			float values[] = new float[9];
			matrix2.getValues(values);
			Matrix temp = new Matrix();
			temp.set(matrix2);
			float transx = values[Matrix.MTRANS_X];
			float transy = values[Matrix.MTRANS_Y];
			float scale = values[Matrix.MSCALE_X];
			Drawable d = getDrawable();
			
		
			if(d != null){
				if(WIDTH == 0){
					WIDTH = getDrawable().getIntrinsicWidth();
				}
				if(HEIGHT == 0){
					HEIGHT = getDrawable().getIntrinsicHeight();
				}
				
				float width = WIDTH * scale;
				float height = HEIGHT * scale;
			
				float originX = 0;
				float originY = 0;
				float bigWidth = getWidth();
				float bigHeight = getHeight();
				
				RectF rect = new RectF(0,0,WIDTH,HEIGHT);
				temp.mapRect(rect);
				
				transx = rect.left;
				transy = rect.top;
				width = rect.right - rect.left;
				height = rect.bottom - rect.top;
				
				if((transx + -distanceX) + width < bigWidth-CROP_REGION && -distanceX < 0 && (transx + -distanceX) < (originX+CROP_REGION)){
					distanceX = 0;
				}
				if((transy + -distanceY) + height < bigHeight-CROP_REGION && -distanceY < 0 && (transy + -distanceY) < (originY+CROP_REGION)){
					distanceY = 0;
				}
				if((transy + -distanceY) > (originY+CROP_REGION) && -distanceY > 0 && (transy + -distanceY) + height > bigHeight-CROP_REGION){
					distanceY = 0;
				}
				if((transx + -distanceX) > (originX+CROP_REGION) && -distanceX > 0 && (transx + -distanceX) + width > bigWidth-CROP_REGION){
					distanceX = 0;
				}
				
				matrix2.set(savedMatrix);
				matrix2.postTranslate(-distanceX, -distanceY);
				setImageMatrix(matrix2);
				return true;
			}
			return false;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2,
				float velocityX, float velocityY){
			return false;
		}

		public boolean onDoubleTap(MotionEvent e) {
			//TO-DO zoom in
			zoomTo(matrix2,1.25f);
			return true;
		}

		public boolean onSingleTapConfirmed(MotionEvent e) {
			return false;
		}
	}
	
	MotionEvent oldEvent;
	
	public boolean onTouchEvent (MotionEvent ev){	
		if(!isTouchable){
			return super.onTouchEvent(ev);
		}
		int action = ev.getAction();
		switch (ev.getPointerCount()) {
			case 1:
				matrix2.set(getImageMatrix());
				savedMatrix.set(matrix2);
				gesturedetector.onTouchEvent(ev);
				break;
			case 2:
				switch (action & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_POINTER_DOWN:
						oldDist = spacing(ev);
						//gesturedetector.onTouchEvent(ev);
						if (oldDist > 10f) {
							matrix2.set(getImageMatrix());
							oldEvent = ev;
							savedMatrix.set(matrix2);
							midPoint(mid, ev);
							mode = ZOOM;
						}
						break;
					case MotionEvent.ACTION_MOVE:
						float newDist = spacing(ev);
						if (newDist > 10f) {
							float values[] = new float[9];
							matrix2.set(getImageMatrix());
							matrix2.getValues(values);
							scale = values[Matrix.MSCALE_X];
							float scale2 = newDist / oldDist;
							oldDist = newDist;
							scale = scale2*scale;
							if (Math.abs(scale) >= maxZoom()) {
								scale2 = 1.0f;
							}
							if (Math.abs(scale) <= minZoom()) {
								scale2 = 1.0f;
							}
							matrix2.preScale(scale2, scale2, mid.x, mid.y);
							
							float degrees = getRotation(oldEvent,ev);
							rotation += degrees;
							rotation = rotation%360;
							
							matrix2.postRotate(degrees,getCenterX(),getCenterY());
							oldEvent = ev;
							setImageMatrix(matrix2);
						}
						break;
				}
				break;
		}
		return true;
	}
	
	private float getRotation(MotionEvent e1,MotionEvent e2){
		if(e2.getHistorySize() < 1){
			return 0;
		}
		
		float dx = e2.getX(1) - e2.getX(0);
	    float dy = e2.getY(1) - e2.getY(0);
	    float dx2 = e2.getHistoricalX(1,0) - e2.getHistoricalX(0,0);
	    float dy2 = e2.getHistoricalY(1,0) - e2.getHistoricalY(0,0);
	    
		Vector3f v1 = new Vector3f(dx,dy,0);
		Vector3f v2 = new Vector3f(dx2,dy2,0);
		
		v1 = v1.normalize();
		v2 = v2.normalize();
		
		float dot = v1.dot(v2);
		if(dot > 1){
			dot = 1;
		}
		if(dot < -1){
			dot = -1;
		}
		
		float line1Slope = dx/dy;
		float line2Slope = dx2/dy2;
		float degrees = (float) (Math.acos(dot)*(180/Math.PI));
		
		return line2Slope > line1Slope ? degrees : -degrees;
	}
	
	/** Determine the space between the first two fingers */
	private float spacing(MotionEvent event) {
		// ...
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {
		// ...
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	
	float rotation = 0;

	public void rotateClockWise() {
		rotateBy(-90);
	}

	public void rotateCClockWise() {
		rotateBy(90);
	}
	
	public void rotateBy(int degrees){
		rotation += degrees;
		rotation = rotation%360;
		try{
		
		float x = getCenterX();
		float y = getCenterY();
		
		
		
		matrix2.set(getImageMatrix());
		matrix2.postRotate(degrees,x,y);
		setImageMatrix(matrix2);
		}catch(Throwable t){
			
		}
	}
	
	Point center;
	
	public int getCenterX(){	
		if(center == null){
			center = new Point();
			float values[] = new float[9];
			matrix2.set(getImageMatrix());
			matrix2.getValues(values);
			float dx = values[Matrix.MTRANS_X];
			float dy = values[Matrix.MTRANS_Y];
			float scale = values[Matrix.MSCALE_X];
			if(WIDTH == 0){
				WIDTH = getDrawable().getIntrinsicWidth();
			}
			if(HEIGHT == 0){
				HEIGHT = getDrawable().getIntrinsicHeight();
			}
			float w = WIDTH * scale;
			float h = HEIGHT * scale;
			center.x = (int) (dx + (w/2));
			center.y = (int) (dy + (h/2));
		}
		
		return (int) center.x;
	}
	
	public int getCenterY(){
		if(center == null){
			center = new Point();
			float values[] = new float[9];
			matrix2.set(getImageMatrix());
			matrix2.getValues(values);
			float dx = values[Matrix.MTRANS_X];
			float dy = values[Matrix.MTRANS_Y];
			float scale = values[Matrix.MSCALE_X];
			if(WIDTH == 0){
				WIDTH = getDrawable().getIntrinsicWidth();
			}
			if(HEIGHT == 0){
				HEIGHT = getDrawable().getIntrinsicHeight();
			}
			float w = WIDTH * scale;
			float h = HEIGHT * scale;
			center.x = (int) (dx + (w/2));
			center.y = (int) (dy + (h/2));
		}
		
		return center.y;
	}
}
