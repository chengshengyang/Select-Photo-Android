package com.github.select.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.select.R;
import com.github.select.SelectPhotoActivity;
import com.github.select.ui.ClipImageView;
import com.github.select.util.UniversalImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.ByteArrayOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditPhotoFragment extends BaseFragment {
    public static final int TAG_EDIT_PHOTO = 1000;
    @BindView(R.id.edit_photo_src)
    com.github.select.ui.ClipImageView mClipImageView;

    @BindView(R.id.loading)
    ProgressBar mProgressBar;

    private ActionBar mActionBar;
    private ImageView mActionBarSelectIv;
    private View mMenuItemView;
    private SelectPhotoActivity mActivity;
    private Context mContext;
    private String mImageUri;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFragment = inflater.inflate(R.layout.fragment_edit_photo, container, false);
        mActivity = (SelectPhotoActivity) getActivity();
        mContext = getActivity().getApplicationContext();

        mImageUri = getArguments().getString("image-uri");
        initView();
        ButterKnife.bind(this, mFragment);
        return mFragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initEvent();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem recordItem = menu.add(getString(R.string.action_menu_select));
        setActionViewAlways(recordItem, mMenuItemView);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                EditPhotoFragment fragment = (EditPhotoFragment) fm.findFragmentByTag(getTag());
                if (fragment == null) return false;
                ((SelectPhotoActivity) getActivity()).removeFragment(fragment);
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 设置图片MenuItem
     *
     * @param tag      点击事件标记
     * @param resId    显示图片资源id
     * @param listener 点击事件
     * @return
     */
    public View getIconMenuItem(int tag, int resId, OnClickListener listener) {
        View view = View.inflate(mContext, R.layout.actionbar_menu_item_view, null);
        mActionBarSelectIv = (ImageView) view.findViewById(R.id.icon);
        mActionBarSelectIv.setImageResource(resId);
        setViewBackground(view, tag, listener);
        return view;
    }

    /**
     * 设置MenuItem一直显示
     *
     * @param item
     * @param view
     */
    public void setActionViewAlways(MenuItem item, View view) {
        MenuItemCompat.setActionView(item, view);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
    }

    public void setViewBackground(View view, int tag, OnClickListener listener) {
        view.setBackgroundResource(R.drawable.actionbar_menu_selector);
        view.setMinimumWidth(SelectPhotoActivity.iAcionWidth);
        view.setMinimumHeight(SelectPhotoActivity.iActionHeight);
        view.setTag(tag);
        view.setOnClickListener(listener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void initView() {
        mMenuItemView = getIconMenuItem(TAG_EDIT_PHOTO, R.drawable.ic_selected, mOnClickListener);

        mActionBar = mActivity.getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("编辑");
    }

    @Override
    public void initEvent() {
        try {
            UniversalImageLoader.displayImage(
                    mImageUri,
                    mClipImageView,
                    new SimpleImageLoadingListener() {

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            mProgressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            mProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });
        } catch (OutOfMemoryError e) {
            Toast.makeText(getActivity(), "out of memory", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void invalidate() {
        mFragment.invalidate();
    }

    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int tag = (Integer) v.getTag();
            switch (tag) {
                case TAG_EDIT_PHOTO:
                    Bitmap bitmap = mClipImageView.clip();

                    // 由于Intent传递bitmap不能超过40k,此处使用二进制数组传递
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmapByte = baos.toByteArray();

                    Intent intent = new Intent();
                    intent.putExtra("bitmap", bitmapByte);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                    break;

                default:
                    break;
            }
        }
    };
}
