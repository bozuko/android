package com.fuzz.android.ui;

import android.os.Parcelable;

public interface PhotoInterface extends Parcelable{

	public String getImageThumb();
	
	public String getImageLarge();
	
	public String getID();
	
	public String getSubtitle();

	public CharSequence getDate();

	public CharSequence getSource();

	public String getSourceImage();

	public String getCaption();
	
}
