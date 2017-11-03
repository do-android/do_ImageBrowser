package doext.imagebrowser.util;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Map;

import core.helper.DoIOHelper;
import core.helper.DoImageHandleHelper;
import core.helper.DoImageLoadHelper;
import doext.bean.Item;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class DownloadBitmapTask extends AsyncTask<Void, Void, Bitmap> {

	private static final int MAXWIDTH = 1080;
	private static final int MAXHEIGHT = 1920;

	private ProgressBar progressBar;
	private Context context;
	private ImageView imageView;
	private Item imageItem;
	private Map<String, SoftReference<Bitmap>> imageCache;

	public DownloadBitmapTask(Context ctx, ProgressBar pb, ImageView iv, Item ii, Map<String, SoftReference<Bitmap>> imageCache) {
		this.context = ctx;
		this.imageView = iv;
		this.progressBar = pb;
		this.imageItem = ii;
		this.imageCache = imageCache;
	}

	@Override
	protected void onPreExecute() {
		Bitmap bmp = null;
		if (imageItem.init != null) {
			bmp = DoImageLoadHelper.getInstance().loadLocal(imageItem.init, 256, 256);
		}

		if (bmp != null) {
			imageView.setImageBitmap(bmp);
		}

	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		String url = imageItem.source;
		String diskCacheDir = BitmapUtils.getDiskCacheDir(context, url, imageItem.isHttpUrl);
		Bitmap bmp = null;
		try {
			// 先从内存里面拿
			SoftReference<Bitmap> softBmp = imageCache.get(url);
			if (softBmp != null && softBmp.get() != null) {
				bmp = softBmp.get();
			} else {
				if (DoIOHelper.isAssets(diskCacheDir)) {
					bmp = DoImageHandleHelper.resizeScaleImage(DoIOHelper.readAllBytes(diskCacheDir), MAXWIDTH, MAXHEIGHT);
				} else {
					// 判断本地文件存不存在，存在直接加载，不存在就去网络下载
					File file = new File(diskCacheDir);
					if (!file.exists()) {
						BitmapUtils.downloadUrlToStream(url, diskCacheDir);
					}
					bmp = BitmapUtils.revitionImageSize(diskCacheDir, MAXWIDTH, MAXHEIGHT);
				}
				imageCache.put(url, new SoftReference<Bitmap>(bmp));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bmp;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if (result != null) {
			imageView.setImageBitmap(result);
		}

		if (progressBar != null) {
			progressBar.setVisibility(View.GONE);
		}

	}

}
