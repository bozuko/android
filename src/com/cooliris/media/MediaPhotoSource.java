package com.cooliris.media;

import java.util.ArrayList;

import com.fuzz.android.ui.PhotoInterface;

import android.content.Context;

public class MediaPhotoSource implements DataSource {
	@SuppressWarnings("unused")
	private static final String TAG = "MDataSource";
	ArrayList<PhotoInterface> media;
	Context mContext;

	public static final String URI_ALL_MEDIA = "INTERNET";
	public static final DiskCache sThumbnailCache = new DiskCache("media-thumbs");
	boolean mDone;
	
	public MediaPhotoSource(Context m,ArrayList<PhotoInterface> arr) {
		// TODO Auto-generated constructor stub
		media = arr;
		mContext = m;
		mDone = false;
	}

	@Override
	public DiskCache getThumbnailCache() {
		// TODO Auto-generated method stub
		return sThumbnailCache;
	}

	@Override
	public void loadItemsForSet(MediaFeed feed, MediaSet parentSet,
			int rangeStart, int rangeEnd) {
		// TODO Auto-generated method stub
		if (parentSet.mNumItemsLoaded > 0 && mDone) {
            return;
        }
		if(!mDone){
			for(int i=0; i<media.size(); i++){
				PhotoInterface photo = media.get(i);
				 final MediaItem item = new MediaItem();
	                item.mId = Long.parseLong(photo.getID());
	                item.mEditUri = photo.getImageLarge();
	                item.mMimeType = "image/*";
	                item.mThumbnailUri = photo.getImageThumb();
	                item.mScreennailUri = photo.getImageLarge();
	                item.mContentUri = "content://" + photo.getImageLarge();
	                item.mCaption = photo.getCaption();      
	                item.mWeblink = photo.getImageLarge();
	                item.mDescription = photo.getSubtitle();
	                item.mFilePath = item.mContentUri;
	                feed.addItemToMediaSet(item, parentSet);
			}
			mDone = true;
		}
	}

	@Override
	public void loadMediaSets(MediaFeed feed) {
		// TODO Auto-generated method stub
		 MediaSet set = null; // Dummy set.
		 
		 String name = (String) media.get(0).getSource();
         long id = Long.parseLong(media.get(0).getID());
         set = feed.addMediaSet(id, this);
         set.mName = name;
         set.mId = id;
         set.setNumExpectedItems(media.size());
         set.generateTitle(false);
         set.mPicasaAlbumId = Shared.INVALID;
	}

	@Override
	public boolean performOperation(int operation,
			ArrayList<MediaBucket> mediaBuckets, Object data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void refresh(MediaFeed feed, String[] databaseUris) {
		// TODO Auto-generated method stub
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	public String[] getDatabaseUris() {
        return new String[] { };
    }

	public PhotoInterface getPhotoInterfaceFor(int location) {
		// TODO Auto-generated method stub
		return media.get(location);
	}
	
}
