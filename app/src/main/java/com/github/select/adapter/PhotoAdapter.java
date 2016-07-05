package com.github.select.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.github.select.Constants;
import com.github.select.R;
import com.github.select.entity.AlbumInfo;
import com.github.select.entity.PhotoInfo;
import com.github.select.ui.RotateImageViewAware;
import com.github.select.util.BitmapCache;
import com.github.select.util.BitmapCache.ImageCallback;
import com.github.select.util.ThumbnailsUtil;
import com.github.select.util.UniversalImageLoader;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 图片列表适配器
 *
 * @author chengsy
 */
public class PhotoAdapter extends BaseAdapter {

    private Context mContext;
    private List<PhotoInfo> mPhotoList;
    private ViewHolder mHolder;
    private onSelectedPhotoChangeListener mListener = null;
    private String mToastFormat;
    private int mMaxCount = Constants.MAX_SELECT_COUNT;
    private BitmapCache mBitmapCache;
    private int mEditTag = 0;

    public interface onSelectedPhotoChangeListener {
        public void onChangedListener(List<PhotoInfo> photoList);
    }

    public void setOnSelectedPhotoChangeListener(onSelectedPhotoChangeListener listener) {
        this.mListener = listener;
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(mContext, String.format(mToastFormat, mMaxCount), Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }
    };

    ImageCallback mCallback = new ImageCallback() {

        @Override
        public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params) {
            if (imageView != null && bitmap != null) {
                String url = (String) params[0];
                if (url != null && url.equals((String) imageView.getTag())) {
                    imageView.setImageBitmap(bitmap);
                } else {
                    Log.e("TAG", "------callback, bmp not match");
                }
            } else {
                Log.e("TAG", "------callback, bmp null");
            }
        }
    };

    public PhotoAdapter(Context context, AlbumInfo mInfo, int tag) {
        super();
        this.mContext = context;
        this.mPhotoList = mInfo.getPhotoList();
        this.mEditTag = tag;
        this.mToastFormat = mContext.getString(R.string.toast_max_count);

        mBitmapCache = new BitmapCache();
    }

    public int getPhotoBoundSize() {
        DisplayMetrics dMetrics = mContext.getResources().getDisplayMetrics();
        return dMetrics.widthPixels / 3;
    }

    public int getSelectedCount() {
        int selectedCount = 0;
        for (int i = 0; i < mPhotoList.size(); i++) {
            if (mPhotoList.get(i).isSelected) {
                selectedCount++;
            }
        }
        return selectedCount;
    }

    @Override
    public int getCount() {
        return (mPhotoList == null ? 0 : mPhotoList.size());
    }

    @Override
    public Object getItem(int position) {
        return (mPhotoList == null ? null : mPhotoList.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_image_grid, null);
            mHolder = new ViewHolder(convertView);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        LayoutParams layoutParams = (LayoutParams) mHolder.iv_photo.getLayoutParams();
        layoutParams.width = getPhotoBoundSize();
        layoutParams.height = getPhotoBoundSize();
        mHolder.iv_photo.setLayoutParams(layoutParams);
        mHolder.iv_photo.setScaleType(ScaleType.CENTER_CROP);

        final PhotoInfo photoInfo = mPhotoList.get(position);
        if (photoInfo != null) {
            String thumbnailPath = photoInfo.getThumbnailPath();
            String thumbnailURI = "file://" + thumbnailPath;
            String sourcePath = photoInfo.getImagePath();
            mHolder.iv_photo.setTag(sourcePath);

            if (!TextUtils.isEmpty(thumbnailPath)) {
                File file = new File(thumbnailPath);
                if (file.exists()) {
                    UniversalImageLoader.displayLocalImage(
                            ThumbnailsUtil.MapgetHashValue(thumbnailURI, thumbnailURI),
                            new RotateImageViewAware(mHolder.iv_photo, sourcePath));
                } else {
                    mBitmapCache.displayBitmap(
                            mHolder.iv_photo,
                            sourcePath,
                            mCallback);
                }
            } else {
                mBitmapCache.displayBitmap(
                        mHolder.iv_photo,
                        sourcePath,
                        mCallback);
            }
        }

        if (photoInfo.isSelected) {
            mHolder.iv_check.setImageResource(R.drawable.gou_selected);
        } else {
            mHolder.iv_check.setImageResource(R.drawable.gou_normal);
        }

        mHolder.iv_check.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int selectedCount = getSelectedCount();
                if (selectedCount < mMaxCount) {
                    photoInfo.isSelected = !photoInfo.isSelected;
                    if (photoInfo.isSelected) {
                        mHolder.iv_check.setImageResource(R.drawable.gou_selected);
                    } else if (!photoInfo.isSelected) {
                        mHolder.iv_check.setImageResource(R.drawable.gou_normal);
                    }
                } else if (selectedCount >= mMaxCount) {
                    if (photoInfo.isSelected == true) {
                        photoInfo.isSelected = !photoInfo.isSelected;
                        mHolder.iv_check.setImageResource(R.drawable.gou_normal);
                    } else {
                        Message message = Message.obtain(mHandler, 0);
                        message.sendToTarget();

//						for (int i = 0; i < mPhotoList.size(); i++) {
//							if (mPhotoList.get(i).isSelected) {
//								mPhotoList.get(i).isSelected = false;
//								break;
//							}
//						}
//						
//						photoInfo.isSelected = !photoInfo.isSelected;
//						mHolder.iv_check.setImageResource(R.drawable.gou_selected);
                    }
                }

                if (mListener != null) {
                    mListener.onChangedListener(mPhotoList);
                }

                notifyDataSetChanged();
            }
        });

        if (mEditTag == 0) {
            mHolder.iv_check.setVisibility(View.VISIBLE);
        } else {
            mHolder.iv_check.setVisibility(View.GONE);
        }

        return convertView;
    }

    class ViewHolder {
        @BindView(R.id.item_grid_image)
        ImageView iv_photo;

        @BindView(R.id.item_grid_select)
        ImageView iv_check;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
