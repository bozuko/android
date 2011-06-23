package com.fuzz.android.ui;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;

public class PhotoAdapter extends ArrayAdapter<PhotoInterface>{

	public PhotoAdapter(Context context, int textViewResourceId,ArrayList<PhotoInterface> items){
		super(context, textViewResourceId, items);
		//this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		//fix-me
		MediaView movieView = null;

		if (convertView == null) {
			// create the cell renderer
			Log.i(getClass().getSimpleName(), "creating a MovieView object");
			movieView = new MediaView(parent.getContext());
		}
		else {
			movieView = (MediaView) convertView;
		}

		// update the cell renderer, and handle selection state
		movieView.display(getItem(position));

		return movieView;

	}


	/** this class is responsible for rendering the data in the model, given the selection state */
	private class MediaView extends RelativeLayout {

		private URLImageView _imageView;
		private ProgressBar _progress;

		public MediaView(Context m) {

			super(m);

			_createUI(m);

		}

		/** create the ui components */
		private void _createUI(Context m) {
			DisplayMetrics metrics = new DisplayMetrics();
			((Activity) m).getWindowManager().getDefaultDisplay().getMetrics(metrics);
			//this.setLayoutParams(new GridView.LayoutParams((int) (100*metrics.density),(int)(100*metrics.density)));
			
			
			_imageView = new URLImageView(m);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,(int)(100*metrics.density));
			_imageView.setLayoutParams(params);
			_imageView.setScaleType(ScaleType.CENTER_CROP);
			_imageView.setId(100);
			this.addView(_imageView);

			_progress = new ProgressBar(m);
			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT,1);
			_progress.setLayoutParams(params);
			this.addView(_progress);
			
			_imageView.setProgressBar(_progress);
		}

		/** update the views with the data corresponding to selection index */
		public void display(PhotoInterface photoItem) {
			//_imageView.setVisible();
			_progress.setVisibility(View.VISIBLE);
			_imageView.setURL(photoItem.getImageThumb());
		}
	}
}
