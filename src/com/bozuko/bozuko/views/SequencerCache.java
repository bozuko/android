package com.bozuko.bozuko.views;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class SequencerCache {

	public static SequencerCache cache;
	
	HashMap<Integer,SoftReference<Bitmap>> resourcesLoaded = new HashMap<Integer,SoftReference<Bitmap>>();
	//HashMap<Integer,Bitmap> resourcesLoaded = new HashMap<Integer,Bitmap>();
	ArrayList<Bitmap> arrayList;
	
	public static SequencerCache getSharedInstance(){
		if(cache == null){
			cache = new SequencerCache();
		}
		return cache;
	}
	
	public Bitmap getBitmap(int resource,Resources resources){
		SoftReference<Bitmap> reference;
		if(resourcesLoaded.containsKey(resource)){
			reference = resourcesLoaded.get(resource);
			if(reference.get() == null){
				int width = resources.getDisplayMetrics().widthPixels;
				int height = (int) (42*((float)(((float)width)/320.0)));
				 BitmapFactory.Options opts = new BitmapFactory.Options();
			        opts.outHeight = height;
					opts.outWidth = width;
					opts.inPurgeable = true;
				Bitmap bit = BitmapFactory.decodeResource(resources, resource,opts);
				reference = new SoftReference<Bitmap>(bit);
				resourcesLoaded.put(resource, reference);
			}
		}else{
			int width = resources.getDisplayMetrics().widthPixels;
			int height = (int) (42*((float)(((float)width)/320.0)));
			 BitmapFactory.Options opts = new BitmapFactory.Options();
		        opts.outHeight = height;
				opts.outWidth = width;
				opts.inPurgeable = true;
			Bitmap bit = BitmapFactory.decodeResource(resources, resource,opts);
			reference = new SoftReference<Bitmap>(bit);
			resourcesLoaded.put(resource, reference);
		}
		return reference.get();
	}
	
	public Bitmap getBitmap(int current){
		return arrayList.get(current);
	}

	public void setResources(final int[] inResources,final Resources resources) {
		// TODO Auto-generated method stub
		arrayList = new ArrayList<Bitmap>();
		Thread th = new Thread(){
			public void run(){
				for(int i=0; i<inResources.length; i++){
					Bitmap bit = BitmapFactory.decodeResource(resources, inResources[i]);
					arrayList.add(bit);
				}
			}
		};
		th.start();
	}

	public void clearResources() {
		// TODO Auto-generated method stub
		resourcesLoaded.clear();
	}
}
