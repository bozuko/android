package com.fuzz.android.ui;

import java.lang.reflect.Method;

public class MenuOption {

	public enum MenuOptionType {
		   CheckMarkType, NoneType
	}
	
	public int image;
	public String title;
	public String value;
	public boolean arrow;
	public Method method;
	public MenuOptionType optionType;
	
	public MenuOption(int inImage, String inTitle, String inValue, boolean inArrow,Method inMethod){
		super();
		image = inImage;
		title = inTitle;
		value = inValue;
		arrow = inArrow;
		method = inMethod;
	}
	
	public MenuOption(int inImage, String inTitle, String inValue, boolean inArrow,Method inMethod,MenuOptionType inType){
		super();
		image = inImage;
		title = inTitle;
		value = inValue;
		arrow = inArrow;
		method = inMethod;
		optionType = inType;
	}
	
}
