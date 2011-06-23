/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fuzz.android.ui;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews.RemoteView;

@RemoteView
public class PagingLayout extends LinearLayout {  
	SparseIntArray mPageToChildIndex;
	HashMap<String,SoftReference<View>> pageReferences = new HashMap<String,SoftReference<View>>();
	
    public PagingLayout(Context context) {
        super(context);
    }

    public PagingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    View getVirtualChildAt(int index) {
    	//TODO add provider info
    	if(provider == null){
    		getChildAt(index);
    	}
    	
    	if(mPageToChildIndex == null){
    		mapPagesAndIndexs();
    	}
    	
    	final int deflectedIndex = mPageToChildIndex.get(index, -1);
        if (deflectedIndex != -1) {
            return getChildAt(deflectedIndex);
        }

        return null;
    }
    
    private void mapPagesAndIndexs() {
    	if(mPageToChildIndex == null){
            final int count = getVirtualChildCount();
    		mPageToChildIndex = new SparseIntArray();
    		final int childcount = getChildCount();
    		final SparseIntArray pageToChild = mPageToChildIndex;
    		for (int i = 0; i < count; i++) {
    			boolean found = false;
    			SoftReference<View> soft = pageReferences.get(getPageID(i));
    			View fromprovider = provider.getView(i,soft == null ? null : soft.get());
    			for(int j=0; j<childcount; j++){
    				View fromview = getChildAt(j);
    				if(fromview == fromprovider){
    					pageToChild.put(i, j);
    					found = true;
    					j = childcount;
    				}
    			}
    			if(!found){
    				//provider.clearView(i);
    			}
    		}
    	}
    }
    
    public String getPageID(int position){
    	if(provider==null)
    		return "" + position;
    
    	Object obj = provider.getItem(position);
    	if(obj != null)
    		return obj.toString() + provider.getItemID(position) + "" + position;
    	
    	return provider.getItemID(position) + "" + position;
    }

    int getVirtualChildCount() {
    	if(provider == null){
    		getChildCount();
    	}
        return provider.getCount();
    }
    
    int measureNullChild(int childIndex) {
    	if(provider == null){
    		return 0;
    	}
        return provider.getPageWidth();
    }
    
    PagingAdapter provider;
    
    public void setPagingProvider(PagingAdapter prov){
    	provider = prov;
    	removeExtraPageAroundPage(0);
    	invalidate();
    }  
    
    public void removeExtraPageAroundPage(int page){
    	int firstPageInWindow = page;
    	if(provider != null){
    		if(mPageToChildIndex == null){
    			mapPagesAndIndexs();
        	}
    		int pagesPerWindow = provider.pagesPerWindow();
    		int childcount = getChildCount();
    		int numOfPages = pagesPerWindow*provider.preLoadedSize();
    		int startAt = firstPageInWindow - (pagesPerWindow);
            final int count = getVirtualChildCount();
            int virtualcount = 0;
    		final SparseIntArray pageToChild = mPageToChildIndex;
    		int istart = startAt - pagesPerWindow;
    		if(istart < 0){
    			istart = 0;
    		}
    		int iend = startAt + numOfPages + pagesPerWindow;
    		if(iend > count){
    			iend = count;
    		}
        	for (int i = istart; i < iend; i++) {
        		boolean found = false;
        		SoftReference<View> soft = pageReferences.get(getPageID(i));
    			View fromprovider = provider.getView(i,soft == null ? null : soft.get());
        		for(int j=virtualcount; j<getChildCount(); j++){
        			View fromview = getChildAt(j);
        			if(fromview == fromprovider){
        				if(i < startAt || i>(startAt + numOfPages)){
        					pageToChild.put(i, -1);
        					removeView(fromprovider);
        				}else{
        					pageToChild.put(i, j);
        					found= true;
        				}
        				j=childcount;
        			}
        		}
        		if(!found){
        			if(i < startAt || i>(startAt + numOfPages)){
        				
        			}
        			else{
        				if(i> firstPageInWindow){
        					addView(fromprovider);
        					pageToChild.put(i, indexOfChild(fromprovider));
        					found = true;
        				}else{
        					addView(fromprovider,virtualcount++);
        					pageToChild.put(i, indexOfChild(fromprovider));
        					found = true;
        				}
        			}
        		}
        		
        		if(!found){
        			//provider.clearView(i);
        		}
        	}
    	}
    }

    /**
     * PagingProvider is in charge of feeding the views and paging information
     * to the paging scrollview/layout
     * @author cesaraguilar
     *
     */
	public interface PagingAdapter{
		/**
		 * returns the number of views in the provider 
		 * does not mean pages
		 * @return
		 */
		public int getCount();
		/**
		 * returns the item for position
		 * @param position
		 * @return
		 */
		public Object getItem(int position);
		/**
		 * get the page id associated with the specified position in the list
		 * @param position
		 * @return
		 */
		public long getItemID(int position);
		/**
		 * returns the view to be displayed for the position being sent in
		 * implementation note the view being returned should always be the same instance for a 
		 * position
		 * @param position
		 * @return
		 */	
		public View getView(int position,View convertView);
		/**
		 * returns the size of the page in width
		 * @return
		 */
		public int getPageWidth();
		/**
		 * returns the numbers of pages to display in a given window
		 * @return
		 */
		public int pagesPerWindow();
		/**
		 * returns the number of preloaded pages so if 3 the left and right page from current
		 * will be preloaded for quicker navigation. 3 is suggested return
		 * @return
		 */
		public int preLoadedSize();
		public void onScrollChanged(int l, int t, int oldl, int oldt);
	}
}