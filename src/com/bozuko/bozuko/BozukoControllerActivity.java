package com.bozuko.bozuko;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.fuzz.android.activities.ControllerActivity;
import com.fuzz.android.ui.GroupView;
import com.fuzz.android.ui.MenuOption;
import com.fuzz.android.ui.OptionCell;

public class BozukoControllerActivity extends ControllerActivity {

	public View getTitleView(String inString){
		TextView view = new TextView(this);
		//view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		view.setText(inString);
		view.setBackgroundResource(R.drawable.listheader);
		view.setTextSize(16);
		view.setMaxLines(1);
		view.setTypeface(Typeface.DEFAULT_BOLD);
		view.setTextColor(Color.WHITE);
		return view;
	}
	
	public View getGroupTitleView(String inString){
		TextView view = new TextView(this);
		//view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		view.setText(inString);
		view.setPadding(10, 5, 10, 5);
		//view.setBackgroundResource(R.drawable.listheader);
		view.setTextSize(16);
		view.setMaxLines(1);
		view.setTypeface(Typeface.DEFAULT_BOLD);
		view.setTextColor(Color.BLACK);
		return view;
	}

	public View getSpacer(){
		View tmpView = new View(this);
		tmpView.setLayoutParams(new ListView.LayoutParams(LayoutParams.FILL_PARENT,(int)(20*getResources().getDisplayMetrics().density)));
		return tmpView;
	}

	public View getCellView(String inString,int resource){
		GroupView groupView = new GroupView(this);
		
		TextView textView = new TextView(getBaseContext());
		textView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,(int)(44*getResources().getDisplayMetrics().density)));
		textView.setTextColor(Color.BLACK);
		textView.setTypeface(Typeface.DEFAULT_BOLD);
		textView.setTextSize(14);
		textView.setGravity(Gravity.CENTER);
		groupView.setContentView(textView);
		textView.setText(inString);
		
		groupView.setImage(resource);
		
		return groupView;
	}

	public View getOptionView(MenuOption option,int resource){
		OptionCell optionView = new OptionCell(getBaseContext());
		optionView.display(option,resource);
		return optionView;
	}
}
