package com.bozuko.bozuko;

import java.util.ArrayList;

import com.fuzz.android.ui.PagingScrollView;
import com.fuzz.android.ui.PagingLayout.PagingAdapter;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

public class AboutBozukoActivity extends BozukoControllerActivity implements OnClickListener {

	ArrayList<Integer> about = new ArrayList<Integer>();
	
	public void onDestroy(){
		super.onDestroy();
		about.clear();
	}
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContent(R.layout.about);
		setHeader(R.layout.detailsubmitheader);
		about.add(Integer.valueOf(R.drawable.helpscreen1));
		about.add(Integer.valueOf(R.drawable.helpscreen2));
		about.add(Integer.valueOf(R.drawable.helpscreen3));
		about.add(Integer.valueOf(R.drawable.helpscreen4));
		
		((Button)findViewById(R.id.submit)).setText("Close");
		findViewById(R.id.submit).setOnClickListener(this);
		
		PagingScrollView view = (PagingScrollView)findViewById(R.id.page_view);
		view.setAdapter(new CustomPaging()); 
		view.setVerticalScrollBarEnabled(false);
		view.setHorizontalScrollBarEnabled(false);
		view.setHorizontalFadingEdgeEnabled(false);
		
		
		HorizontalScrollView scrollbg = (HorizontalScrollView)findViewById(R.id.scrollbg);
		scrollbg.setEnabled(false);
		scrollbg.setVerticalScrollBarEnabled(false);
		scrollbg.setHorizontalScrollBarEnabled(false);
		scrollbg.setHorizontalFadingEdgeEnabled(false);
		
		HorizontalScrollView scrollclouds = (HorizontalScrollView)findViewById(R.id.scrollclouds);
		scrollclouds.setEnabled(false);
		scrollclouds.setVerticalScrollBarEnabled(false);
		scrollclouds.setHorizontalScrollBarEnabled(false);
		scrollclouds.setHorizontalFadingEdgeEnabled(false);
	}
	
	class CustomPaging implements PagingAdapter{
    	
		public CustomPaging() {
		}

		@Override
		public int getPageWidth() {
			// TODO Auto-generated method stub
			 DisplayMetrics metrics = new DisplayMetrics();
	        getWindowManager().getDefaultDisplay().getMetrics(metrics);
			return metrics.widthPixels;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return about.size();
		}

		@Override
		public View getView(int position,View ConvertView) {
			// TODO Auto-generated method stub
			ImageView view = null;
			if(ConvertView == null){
				//Log.v("MAKING VIEW", "VIEW WAS NULL SAD");
				view = new ImageView(AboutBozukoActivity.this);
				DisplayMetrics metrics = new DisplayMetrics();
		        getWindowManager().getDefaultDisplay().getMetrics(metrics);
		        LayoutParams params = new LayoutParams(metrics.widthPixels,LayoutParams.WRAP_CONTENT);
		        view.setLayoutParams(params);
			}else{
				//Log.v("GETTING VIEW", "VIEW BEING REUSED HAPPY");
				view = (ImageView)ConvertView;
			}
			view.setImageResource(about.get(position));
			return view;
		}

		@Override
		public int pagesPerWindow() {
			// TODO Auto-generated method stub
			return 1;
		}
    	
		public int preLoadedSize(){
			return 7;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return about.get(position);
		}

		@Override
		public long getItemID(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public void onScrollChanged(int l, int t, int oldl, int oldt) {
			// TODO Auto-generated method stub
			HorizontalScrollView scrollbg = (HorizontalScrollView)findViewById(R.id.scrollbg);
			scrollbg.setEnabled(false);
			
			HorizontalScrollView scrollclouds = (HorizontalScrollView)findViewById(R.id.scrollclouds);
			int diff = l - oldl;
			
			scrollclouds.setEnabled(false);
			scrollbg.scrollBy(diff/4, 0);
			scrollclouds.scrollBy(diff/4, 0);
		}
    }

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		finish();
	}
}
