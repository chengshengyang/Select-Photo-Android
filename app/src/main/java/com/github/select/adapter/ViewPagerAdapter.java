package com.github.select.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.github.select.R;
import com.github.select.SelectPhotoActivity;
import com.github.select.entity.AlbumInfo;
import com.github.select.entity.PhotoInfo;
import com.github.select.util.UniversalImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;

/**
 * ViewPager适配器，继承自PagerAdapter
 * @author chengsy
 *
 */
public class ViewPagerAdapter extends PagerAdapter {

	private Context mContext;
	private Activity mActivity;
	private List<PhotoInfo> mPhotoList;
	private boolean showToolbar = false;
	private ActionBar mActionBar;
	private LinearLayout mBottomBar;
	
	
	public ViewPagerAdapter(Context context, AlbumInfo info) {
		this.mContext = context;
		this.mPhotoList = info.getPhotoList();
		this.mActivity = (Activity) context;

		initView();
	}
	
	private void initView() {
		mActionBar = ((SelectPhotoActivity) mActivity).getSupportActionBar();
		mBottomBar = (LinearLayout) mActivity.findViewById(R.id.bottom_bar_linear);
	}

	@Override
	public int getCount() {
		return mPhotoList == null ? 0 : mPhotoList.size();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View imageLayout = LayoutInflater.from(mContext).inflate(R.layout.item_image_pager, null);
		assert imageLayout != null;

		final ProgressBar progressBar = (ProgressBar) imageLayout.findViewById(R.id.loading);
		PhotoView photoView = (PhotoView) imageLayout.findViewById(R.id.photoview);
//		photoView.setZoomable(false);
//		photoView.setClickable(false);

		PhotoInfo pInfo = mPhotoList.get(position);
		String uri = pInfo.getImageURI();
		
		UniversalImageLoader.displayImage(
				uri,
				photoView,
				new SimpleImageLoadingListener() {

					@Override
					public void onLoadingStarted(String imageUri, View view) {
						progressBar.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
						/*String message = null;
						switch (failReason.getType()) {
						case IO_ERROR:
							message = "Input/Output error";
							break;
						case DECODING_ERROR:
							message = "Image can't be decoded";
							break;
						case NETWORK_DENIED:
							message = "Downloads are denied";
							break;
						case OUT_OF_MEMORY:
							message = "Out Of Memory error";
							break;
						case UNKNOWN:
							message = "Unknown error";
							break;
						}
						Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();*/

						progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						progressBar.setVisibility(View.GONE);
					}
				});
		
		container.addView(imageLayout);

		photoView.setOnPhotoTapListener(new OnPhotoTapListener() {

			@Override
			public void onPhotoTap(View arg0, float arg1, float arg2) {
				if (showToolbar) {
					showToolBar();
				} else {
					hideToolBar();
				}
				showToolbar = !showToolbar;
			}

			@Override
			public void onOutsidePhotoTap() {

			}
		});
		
		return imageLayout;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}
	
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	private void showToolBar() {
		//mActionBar.show();
		mBottomBar.setVisibility(View.VISIBLE);
	}
	
	private void hideToolBar() {
		//mActionBar.hide();
		mBottomBar.setVisibility(View.GONE);
	}
}
