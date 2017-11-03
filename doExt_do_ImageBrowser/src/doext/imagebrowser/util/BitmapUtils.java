package doext.imagebrowser.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import core.helper.DoImageHandleHelper;

public class BitmapUtils {

	/**
	 * 根据传入的uniqueName获取硬盘缓存的路径地址。
	 */
	public static String getDiskCacheDir(Context context, String url, boolean isHttpUrl) {
		if (!isHttpUrl) {
			return url;
		}
		String cachePath;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
			cachePath = context.getExternalCacheDir().getPath();
		} else {
			cachePath = context.getCacheDir().getPath();
		}
		return cachePath + File.separator + hashKeyForDisk(url);
	}

	/**
	 * 使用MD5算法对传入的key进行加密并返回。
	 */
	private static String hashKeyForDisk(String key) {
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public static boolean downloadUrlToStream(String url, String cachePath) {
		HttpURLConnection urlConnection = null;
		BufferedOutputStream out = null;
		BufferedInputStream in = null;
		try {
			urlConnection = (HttpURLConnection) new URL(url).openConnection();
			in = new BufferedInputStream(urlConnection.getInputStream(), 8 * 1024);
			out = new BufferedOutputStream(new FileOutputStream(cachePath), 8 * 1024);
			int b;
			while ((b = in.read()) != -1) {
				out.write(b);
			}
			return true;
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static Bitmap revitionImageSize(String path, int maxWidth, int maxHeight) throws IOException {
//		BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)));
//		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inJustDecodeBounds = true;
//		BitmapFactory.decodeStream(in, null, options);
//		in.close();
//		int i = 0;
//		Bitmap bitmap = null;
//		while (true) {
//			if ((options.outWidth >> i <= maxWidth) && (options.outHeight >> i <= maxHeight)) {
//				in = new BufferedInputStream(new FileInputStream(new File(path)));
//				options.inSampleSize = (int) Math.pow(2.0D, i);
//				options.inJustDecodeBounds = false;
//				bitmap = BitmapFactory.decodeStream(in, null, options);
//				break;
//			}
//			i += 1;
//		}
//		return bitmap;
		return DoImageHandleHelper.resizeScaleImage(path, maxWidth, maxHeight);
	}
}
