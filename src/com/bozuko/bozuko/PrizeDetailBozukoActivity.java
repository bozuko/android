package com.bozuko.bozuko;

import com.bozuko.bozuko.datamodel.PrizeObject;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class PrizeDetailBozukoActivity extends BozukoControllerActivity {

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContent(R.layout.prizedetail);
		setHeader(R.layout.detailheader);
		
		PrizeObject prize = (PrizeObject)getIntent().getParcelableExtra("Package");
		//Log.v("Prize",prize.toString());
		TextView prizeName = (TextView)findViewById(R.id.prizename);
		prizeName.setText(prize.requestInfo("name"));
		
		TextView prizeDescrip = (TextView)findViewById(R.id.prizedescrip);
		prizeDescrip.setText(prize.requestInfo("description"));
	}
}
