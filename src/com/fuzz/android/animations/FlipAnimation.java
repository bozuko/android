package com.fuzz.android.animations;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class FlipAnimation  extends Animation {
	private final float mFromDegrees;
	private final float mToDegrees;
	private float mCenterX;
	private float mCenterY;
	private Camera mCamera;
	private int mAxis = YAXIS;
	
	public static int XAXIS = 1;
	public static int YAXIS = 0;

	public FlipAnimation(float fromDegrees, float toDegrees) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;
	}
	
	public FlipAnimation(float fromDegrees, float toDegrees,int inAxis) {
		mFromDegrees = fromDegrees;
		mToDegrees = toDegrees;
		mAxis = inAxis;
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		mCamera = new Camera();
		mCenterX = width/2;
		mCenterY = height/2;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final float fromDegrees = mFromDegrees;
		float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

		final float centerX = mCenterX;
		final float centerY = mCenterY;
		final Camera camera = mCamera;

		final Matrix matrix = t.getMatrix();

		camera.save();

		if(mAxis == YAXIS){
			camera.rotateY(degrees);
		}else{
			camera.rotateX(degrees);
		}
		camera.getMatrix(matrix);
		camera.restore();

		matrix.preTranslate(-centerX, -centerY);
		matrix.postTranslate(centerX, centerY);

	}

}