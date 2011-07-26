package com.bozuko.bozuko.views;

import android.content.Context;

import com.bozuko.bozuko.R;

public class AnimationSequence {

	public static int[] goodLuck(Context mContext){
		int res[] = new int[120];
		for(int i = 0; i<120; i++){
			String s = "goodluck_00" + (i<100 ? "0" : "") + (i<10 ? "0" : "") + i;
			res[i] = mContext.getResources().getIdentifier(s, null, mContext.getPackageName());
			
		}
		return res;
	}
	
	public static int[] playAgain(Context mContext){
		int res[] = new int[112];
		for(int i = 0; i<112; i++){
			String s = "playagain_00" + (i<100 ? "0" : "") + (i<10 ? "0" : "") + i;
			res[i] = mContext.getResources().getIdentifier(s, null, mContext.getPackageName());
			
		}
		return res;
	}
	
	public static int[] freeSpin(Context mContext){
		int res[] = new int[109];
		for(int i = 0; i<109; i++){
			String s = "freespin_00" + (i<100 ? "0" : "") + (i<10 ? "0" : "") + i;
			res[i] = mContext.getResources().getIdentifier(s, null, mContext.getPackageName());
			
		}
		return res;
	}
	
	public static int[] youWin(Context mContext){
		int res[] = new int[106];
		for(int i = 0; i<106; i++){
			String s = "youwin_00" + (i<100 ? "0" : "") + (i<10 ? "0" : "") + i;
			res[i] = mContext.getResources().getIdentifier(s, null, mContext.getPackageName());
			
		}
		return res;
	}
	
	public static int[] youLost(Context mContext){
		int res[] = new int[116];
		for(int i = 0; i<116; i++){
			String s = "youlose_00" + (i<100 ? "0" : "") + (i<10 ? "0" : "") + i;
			res[i] = mContext.getResources().getIdentifier(s, null, mContext.getPackageName());
			
		}
		return res;
	}
	
	
	public final static int GOOD_LUCK[] = {
		R.drawable.goodluck_00020
	};
	
	public final static int PLAY_AGAIN[] = {
		R.drawable.playagain_00018
	};
	
	public final static int YOU_LOSE[] = {
		R.drawable.youlose_00020
	};
	
	public final static int YOU_WIN[] = {
		R.drawable.youwin_00026
	};
	
	public final static int SPIN_AGAIN[] = {
		R.drawable.freespin_00039
	};
	
	public final static int SCRATCH_MASK[] = {
		R.drawable.blank,
		R.drawable.scratchmask_0000,
		R.drawable.scratchmask_0001,
		R.drawable.scratchmask_0002,
		R.drawable.scratchmask_0003,
		R.drawable.scratchmask_0004,
		R.drawable.scratchmask_0005,
		R.drawable.scratchmask_0006,
		R.drawable.scratchmask_0007,
		R.drawable.scratchmask_0008,
		R.drawable.scratchmask_0009,
		R.drawable.scratchmask_0010,
		R.drawable.scratchmask_0011,
		R.drawable.scratchmask_0012,
		R.drawable.scratchmask_0013,
		R.drawable.scratchmask_0014,
		R.drawable.scratchmask_0015,
		R.drawable.scratchmask_0016,
		R.drawable.scratchmask_0017,
		R.drawable.scratchmask_0018,
		R.drawable.scratchmask_0019,
		R.drawable.scratchmask_0020,
		R.drawable.scratchmask_0021,
		R.drawable.scratchmask_0022,
		R.drawable.scratchmask_0023,
		R.drawable.scratchmask_0024
		
	};
}
