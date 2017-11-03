package doext.imagebrowser;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import core.helper.DoResourcesHelper;
import core.interfaces.DoIModuleTypeID;
import doext.app.do_ImageBrowser_App;
import doext.bean.Item;
import doext.imagebrowser.custom.HackyViewPager;
import doext.imagebrowser.custom.PhotoView;
import doext.imagebrowser.custom.PhotoViewAttacher.OnViewTapListener;
import doext.imagebrowser.util.DownloadBitmapTask;

public class ShowPictureViewActivity extends Activity implements DoIModuleTypeID {

	private HackyViewPager mViewPager;
	private ArrayList<Item> itmes;
	private TextView tv;
	private int selectPos;

	private SamplePagerAdapter pagerAdapter;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int imagebrowser_show_id = DoResourcesHelper.getIdentifier("imagebrowser_show", "layout", this);
		setContentView(imagebrowser_show_id);
		itmes = (ArrayList<Item>) getIntent().getSerializableExtra("itmes");
		selectPos = getIntent().getIntExtra("selectPos", 0);
		int tv_id = DoResourcesHelper.getIdentifier("tv", "id", this);
		tv = (TextView) findViewById(tv_id);
		tv.setText((selectPos + 1) + "/" + itmes.size());
		int viewpager_id = DoResourcesHelper.getIdentifier("viewpager", "id", this);
		mViewPager = (HackyViewPager) findViewById(viewpager_id);
		pagerAdapter = new SamplePagerAdapter();
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				tv.setText((position + 1) + "/" + itmes.size());
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		mViewPager.setCurrentItem(selectPos);
	}

	private class SamplePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return itmes.size();
		}

		@Override
		public View instantiateItem(ViewGroup container, final int position) {
			int imagebrowser_itme_id = DoResourcesHelper.getIdentifier("imagebrowser_itme", "layout", ShowPictureViewActivity.this);
			View childView = View.inflate(ShowPictureViewActivity.this, imagebrowser_itme_id, null);
			int pb_id = DoResourcesHelper.getIdentifier("pb", "id", ShowPictureViewActivity.this);
			ProgressBar pb = (ProgressBar) childView.findViewById(pb_id);
			int pv_id = DoResourcesHelper.getIdentifier("pv", "id", ShowPictureViewActivity.this);
			PhotoView iv = (PhotoView) childView.findViewById(pv_id);
			iv.setMaxScale(5.0f);

			iv.setOnViewTapListener(new OnViewTapListener() {
				@Override
				public void onViewTap(View view, float x, float y) {
					Intent data = new Intent();
					data.putExtra("index", position);
					setResult(1000, data);
					ShowPictureViewActivity.this.finish();
				}
			});

			Item _itme = itmes.get(position);
			new DownloadBitmapTask(ShowPictureViewActivity.this, pb, iv, _itme, imageCache).execute();
			container.addView(childView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			return childView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		int cacheSize = imageCache.size();
		if (null != imageCache && cacheSize > 0) {
			for (Entry<String, SoftReference<Bitmap>> entry : imageCache.entrySet()) {
				recycleImageBitmap(entry.getKey());
			}
			imageCache.clear();
		}
	}

	private void recycleImageBitmap(String url) {
		if (null == url)
			return;
		SoftReference<Bitmap> bitmap = imageCache.get(url);
		Bitmap result = bitmap.get();
		if (null != bitmap && null != result) {
			if (!result.isRecycled()) {
				result.recycle();
				result = null;
			}
		}
		System.gc();
	}

//	// 存放缓存图片
	private Map<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();

	@Override
	public String getTypeID() {
		return do_ImageBrowser_App.getInstance().getModuleTypeID();
	}
}