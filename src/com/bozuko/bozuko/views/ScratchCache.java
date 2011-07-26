package com.bozuko.bozuko.views;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ScratchCache {

	public static ScratchCache cache;
	
	HashMap<Integer,SoftReference<Bitmap>> resourcesLoaded = new HashMap<Integer,SoftReference<Bitmap>>();
	
	public static ScratchCache getSharedInstance(){
		if(cache == null){
			cache = new ScratchCache();
		}
		return cache;
	}
	
	public Bitmap getBitmap(int resource,Resources resources){
		SoftReference<Bitmap> reference;
		if(resourcesLoaded.containsKey(resource)){
			reference = resourcesLoaded.get(resource);
			if(reference.get() == null){
				Bitmap bit = BitmapFactory.decodeResource(resources, resource);
				reference = new SoftReference<Bitmap>(bit);
				resourcesLoaded.put(resource, reference);
			}
		}else{
			Bitmap bit = BitmapFactory.decodeResource(resources, resource);
			reference = new SoftReference<Bitmap>(bit);
			resourcesLoaded.put(resource, reference);
		}
		
		return reference.get();
	}
}
