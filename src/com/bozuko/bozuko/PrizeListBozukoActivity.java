package com.bozuko.bozuko;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.bozuko.bozuko.datamodel.GameObject;
import com.bozuko.bozuko.views.PrizeCell;

public class PrizeListBozukoActivity extends BozukoControllerActivity {

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		GameObject game = ((BozukoApplication)getApp()).currentGameObject;
		
		
		setContent(R.layout.prizelist);
		setHeader(R.layout.detailheader);
		
		LinearLayout layout = (LinearLayout)findViewById(R.id.prizelist);
		
		if(game.prizes.size() > 0){
			for(int i=0; i<game.prizes.size(); i++){
				PrizeCell movieView = new PrizeCell(getBaseContext());
				movieView.display(game.prizes.get(i));
				movieView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				layout.addView(movieView);
			}
		}else if(game.consoldationPrizes.size() > 0){
			for(int i=0; i<game.consoldationPrizes.size(); i++){
				PrizeCell movieView = new PrizeCell(getBaseContext());
				movieView.display(game.consoldationPrizes.get(i));
				movieView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				layout.addView(movieView);
			}
		}
	}
}
