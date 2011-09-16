package com.fuzz.android.ui;

import com.fuzz.android.globals.Res;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

@SuppressWarnings("static-access")
public class PageIndicator extends LinearLayout {
	protected int pages;
	protected ImageView[] dotRay = null;
	protected int crntPage = 0;

	public PageIndicator(Context c, int p) {
		super(c);
		pages = p;
		show();
	}

	public PageIndicator(Context c, AttributeSet attrs) {
		super(c, attrs);
		TypedArray a = c.obtainStyledAttributes(attrs,Res.styleable.PageIndicator);
		pages = a.getInt(Res.styleable.PageIndicator_pages, 0);
		show();
	}

	public PageIndicator(Context c, int p, int cp) {
		super(c);
		crntPage = cp;
		pages = p;
		show();
	}

	
	public void show() {
		dotRay = new ImageView[pages];

		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		param.setMargins((int)(6*getResources().getDisplayMetrics().density), 0, (int)(6*getResources().getDisplayMetrics().density), 0);
		this.setGravity(Gravity.CENTER);

		for (int i = 0; i < pages; i++) {
			ImageView dot = new ImageView(getContext());
			dot.setImageResource(Res.drawable.dotdim);
			dotRay[i] = dot;
			this.addView(dot, param);
		}
		dotRay[crntPage].setImageResource(Res.drawable.dotfull);
	}

	public void nextPage() {
		if (crntPage > (pages - 1)) {
			if (dotRay != null) {
				dotRay[crntPage].setImageResource(Res.drawable.dotdim);
				dotRay[crntPage + 1].setImageResource(Res.drawable.dotfull);
			}
			crntPage++;
		}
	}

	public void lastPage() {
		if (crntPage > 0) {
			if (dotRay != null) {
				dotRay[crntPage].setImageResource(Res.drawable.dotdim);
				dotRay[crntPage - 1].setImageResource(Res.drawable.dotfull);
			}
			crntPage--;
		}
	}

	public void setPage(int page) {
		if (dotRay != null) {
			dotRay[crntPage].setImageResource(Res.drawable.dotdim);
			crntPage = page;
			dotRay[crntPage].setImageResource(Res.drawable.dotfull);
		} else {
			crntPage = page;
		}
	}

	public void reset(int p) {
		if (dotRay != null) {
			for (int i = 0; i < dotRay.length - 1; i++) {
				this.removeView(dotRay[i]);
			}
		}
		pages = p;
		show();
	}

}
