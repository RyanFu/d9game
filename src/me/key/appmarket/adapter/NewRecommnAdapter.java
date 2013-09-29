package me.key.appmarket.adapter;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.market.d9game.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import me.key.appmarket.MarketApplication;
import me.key.appmarket.MyListView;
import me.key.appmarket.ImageNet.AsyncImageLoader;
import me.key.appmarket.ImageNet.AsyncImageLoader.ImageCallback;
import me.key.appmarket.adapter.NewRankAdapter.ViewHolder;
import me.key.appmarket.tool.DownloadService;
import me.key.appmarket.tool.ToolHelper;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.AppUtils;
import me.key.appmarket.utils.Global;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.widgets.ProgressView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewRecommnAdapter extends BaseAdapter {

	private LinkedList<AppInfo> appInfos;
	private LayoutInflater lay;
	private File cache;
	private Context mContext;
	AsyncImageLoader asyncImageLoader;
	private ListView mylistView;
	// 是否暂停
	private boolean isPause;
	// 是否是下载状态
	private boolean isDownLoading;
	// 是否异步加载图片
	public boolean isAsyn;
	private Map<String, Drawable> drawMap = new HashMap<String, Drawable>();
	//设置ImageLoade初始化信息
	private DisplayImageOptions options = new DisplayImageOptions.Builder()  
    .showImageForEmptyUri(R.drawable.tempicon).showStubImage(R.drawable.tempicon)  
    .resetViewBeforeLoading(false) 
    .delayBeforeLoading(100)  
    .cacheInMemory(true)           
    .cacheOnDisc(true)              
    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
    .bitmapConfig(Bitmap.Config.RGB_565)               
    .build(); 

	private static final int TYPE_1 = 0;
	private static final int TYPE_2 = 1;
	

	public NewRecommnAdapter(LinkedList<AppInfo> appInfos, Context context,
			File cache, ListView mylistView) {
		super();
		this.appInfos = appInfos;
		this.cache = cache;
		this.mylistView = mylistView;
		mContext = context;
		lay = LayoutInflater.from(context);

		asyncImageLoader = new AsyncImageLoader();

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return appInfos.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return appInfos.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}
	@Override
	public int getItemViewType(int position) {
		int type = position % 3;
		if(type == 0) {
			return TYPE_1;
		} else {
			return TYPE_2;
		}
	}
	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public View getView(final int position, View convertvView, ViewGroup arg2) {
		final ViewHolder viewHolder;
		final ViewHolder2 viewHolder2;
		Drawable mDrawable;
		
		int type = getItemViewType(position);
		if (convertvView == null) {
		switch (type) {
		case TYPE_1:
			viewHolder2 = new ViewHolder2();
			convertvView = lay.inflate(R.layout.item_recomm_lisview, null);
		
			viewHolder2.progress_view = (ProgressView) convertvView
					.findViewById(R.id.recomm_progress_view);
			viewHolder2.recomm_bigiv = (ImageView) convertvView.findViewById(R.id.recomm_bigiv);
			viewHolder2.descr = (TextView) convertvView.findViewById(R.id.recomm_descr);
			viewHolder2.name = (TextView) convertvView.findViewById(R.id.recomm_name);
			viewHolder2.tvdown = (TextView) convertvView
					.findViewById(R.id.recomm_tv_down);
			convertvView.setTag(viewHolder2);
			break;
		case TYPE_2:
			viewHolder = new ViewHolder();
			convertvView = lay.inflate(R.layout.app_list_recomm_item, null);
			
			viewHolder.icon = (ImageView) convertvView.findViewById(R.id.icon2);
			viewHolder.name = (TextView) convertvView
					.findViewById(R.id.app_name2);
			viewHolder.size = (TextView) convertvView
					.findViewById(R.id.appsize2);
			viewHolder.tvdown = (TextView) convertvView
					.findViewById(R.id.tv_down2);
			viewHolder.progress_view = (ProgressView) convertvView
					.findViewById(R.id.progress_view2);
			convertvView.setTag(viewHolder);
			break;
		}
		
		
		} else {
			switch (type) {
			case TYPE_1:
				viewHolder2 = (ViewHolder2) convertvView.getTag();
				break;

			case TYPE_2:
				viewHolder = (ViewHolder) convertvView.getTag();
				break;
			}
		}switch (type) {
		case TYPE_1:
			final ViewHolder2 v2 = ((ViewHolder2) convertvView.getTag());
			setDownState(position, v2);
			break;

		case TYPE_2:
			final ViewHolder v1 = ((ViewHolder) convertvView.getTag());
			LogUtils.d("NewRecommn", appInfos.size()+"");
			
			v1.name.setText(appInfos.get(position).getAppName());
			v1.size.setText(ToolHelper.Kb2Mb(appInfos.get(position)
					.getAppSize()));
			ImageLoader.getInstance().displayImage(appInfos.get(position)
					.getIconUrl(),  v1.icon,options);
			ImageLoader.getInstance();
			setDownState(position, v1);
			break;
		}


		return convertvView;
	}

	public void setDownState(final int position, final BaseHolder v1) {
		Drawable mDrawable;
		v1.progress_view.setProgress(0);
		v1.progress_view.setVisibility(View.VISIBLE);
		File tempFile = new File(Environment.getExternalStorageDirectory(),
				"/market/" + appInfos.get(position).getAppName() + ".apk");
		SharedPreferences sp = mContext.getSharedPreferences("down",
				mContext.MODE_PRIVATE);
		boolean isDownLoaded = DownloadService.isDownLoaded(appInfos.get(
				position).getAppName());
		int idx = Integer.parseInt(appInfos.get(position).getIdx());
		isDownLoading = DownloadService.isDownLoading(idx);
		if (appInfos.get(position).isIspause()) {
			LogUtils.d("ture", appInfos.get(position).isIspause() + "");
			v1.tvdown.setText("暂停");
			v1.progress_view.setProgress(DownloadService
					.getPrecent(idx));
		} else {
			v1.tvdown.setText("下载中");

		}
		if (appInfos.get(position).isInstalled()) {
			v1.tvdown.setText("打开");

			v1.progress_view.setProgress(100);
			Drawable mDrawableicon = mContext.getResources().getDrawable(
					R.drawable.action_type_software_update);
			v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
					mDrawableicon, null, null);
		} else if (appInfos.get(position).isDown()) {
		
			v1.progress_view.setProgress(DownloadService
					.getPrecent(idx));
			LogUtils.d("ture", isDownLoading + "isDown");
		
			v1.tvdown.setText("下载中");
			Drawable mDrawableicon = mContext.getResources().getDrawable(
					R.drawable.downloading);
			v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
					mDrawableicon, null, null);
		} else if (isDownLoaded) {
			Drawable mDrawableicon = mContext.getResources().getDrawable(
					R.drawable.downloaded);
			v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
					mDrawableicon, null, null);
			v1.tvdown.setText("安装");
			v1.progress_view.setProgress(100);
		} else if (!isDownLoading) {
			v1.tvdown.setText("下载");
			mDrawable = mContext.getResources().getDrawable(
					R.drawable.downloading);
			v1.tvdown.setCompoundDrawablesWithIntrinsicBounds(null,
					mDrawable, null, null);
			// 获取将要下载的文件名，如果本地存在该文件，则取出该文件
			
			// LogUtils.d("sa", tempFile.getAbsolutePath());

			long length = sp.getLong(tempFile.getAbsolutePath(), 0);
			// LogUtils.d("sa", length+"");
			if (length != 0 && DownloadService.isExist(appInfos.get(
				position).getAppName())) {
				LogUtils.d("test", "已经存在");
				v1.tvdown.setText("暂停");
			
				long count = sp.getLong(tempFile.getAbsolutePath()+"precent", 0);
				v1.progress_view.setProgress(count);
			} else if(length != 0 && !DownloadService.isExist(appInfos.get(
					position).getAppName())){
				Editor edit = sp.edit();
				edit.remove(tempFile.getAbsolutePath());
				edit.commit();
			}
		}

		v1.tvdown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (appInfos.get(position).isInstalled()) {
					AppUtils.launchApp(mContext, appInfos.get(position)
							.getAppName());
				} else if (DownloadService.isDownLoading(Integer
						.parseInt(appInfos.get(position).getIdx()))) {
					LogUtils.d("test", "暂停");
					File tempFile = DownloadService.CreatFileName(appInfos.get(
							position).getAppName());
					Intent intent = new Intent();
					intent.setAction(tempFile.getAbsolutePath());
					mContext.sendBroadcast(intent);
					if (!appInfos.get(position).isIspause()) {
						v1.tvdown.setText("暂停");
						appInfos.get(position).setDown(false);
					} else {
						v1.tvdown.setText("下载中");
						appInfos.get(position).setDown(true);
					}
					LogUtils.d("down", appInfos.get(position).isDown() + "");
					LogUtils.d("test", appInfos.get(position).isIspause() + "1");
					appInfos.get(position).setIspause(
							!appInfos.get(position).isIspause());
					LogUtils.d("test", appInfos.get(position).isIspause() + "2");
				} else if (DownloadService.isDownLoaded(appInfos.get(position)
						.getAppName())) {
					// 已经下载
					DownloadService.Instanll(appInfos.get(position)
							.getAppName(), mContext);
				} else if (!appInfos.get(position).isInstalled()) {
					Log.e("tag",
							"appurl = " + Global.MAIN_URL
									+ appInfos.get(position).getAppUrl());
					Log.e("tag",
							"appIdx = "
									+ Integer.parseInt(appInfos.get(position)
											.getIdx()));
					/*
					 * Log.e("tag", "appname = " +
					 * appInfos.get(position).getAppName());
					 */
					SharedPreferences sp = mContext.getSharedPreferences(
							"down", mContext.MODE_PRIVATE);
					File tempFile = new File(Environment
							.getExternalStorageDirectory(), "/market/"
							+ appInfos.get(position).getAppName() + ".apk");

					long length = sp.getLong(tempFile.getAbsolutePath(), 0);
					/*
					 * DownloadService.downNewFile(appInfos.get(position)
					 * .getAppUrl(), Integer.parseInt(appInfos.get(
					 * position).getIdx()), appInfos.get(position)
					 * .getAppName(),length,0);
					 */
					DownloadService.downNewFile(appInfos.get(position), length,
							0, null);
					appInfos.get(position).setDown(true);
					Intent intent = new Intent();
					intent.setAction(MarketApplication.PRECENT);
					mContext.sendBroadcast(intent);

					Toast.makeText(mContext,
							appInfos.get(position).getAppName() + " 开始下载...",
							Toast.LENGTH_SHORT).show();
				}

			}
		});
	}

	private static class ViewHolder extends BaseHolder{
	}
	private static class ViewHolder2 extends BaseHolder{
	}
	
	static class BaseHolder {
		 ImageView recomm_bigiv;
		 TextView descr;
		 ImageView icon;
		 TextView name;
		 TextView size;
		 TextView tvdown;
		 ImageView recommTvdown;
		 ProgressView progress_view;
	}

	private void asyncloadImage(ImageView iv_header, String path) {
		AsyncImageTask task = new AsyncImageTask(iv_header);
		task.execute(path);
	}

	private final class AsyncImageTask extends AsyncTask<String, Integer, Uri> {

		private ImageView iv_header;

		public AsyncImageTask(ImageView iv_header) {
			this.iv_header = iv_header;
		}

		@Override
		protected Uri doInBackground(String... params) {
			try {
				return ToolHelper.getImageURI(params[0], cache);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		protected void onPostExecute(Uri result) {
			super.onPostExecute(result);
			try {
				if (iv_header != null && result != null) {
					// ContentResolver resolver = mContext.getContentResolver();
					// byte[] mContent =
					// readStream(resolver.openInputStream(Uri.parse(result.toString())));
					// //将字节数组转换为ImageView可调用的Bitmap对象
					// Bitmap myBitmap = getPicFromBytes(mContent, null);
					// ////把得到的图片绑定在控件上显示
					// iv_header.setImageBitmap(myBitmap);

					iv_header.setImageURI(result);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param newsitem
	 */
	public void addNewsItem(AppInfo newsitem) {
		appInfos.add(newsitem);
	}

	public Drawable getDrawable(AsyncImageLoader asyncImageLoader,
			String imageUrl, final ImageView imageView) {

		Drawable drawable = asyncImageLoader.loadDrawable(imageUrl,
				new ImageCallback() {
					@Override
					public void imageLoaded(Drawable imageDrawable,
							String imageUrl) {
						// 如果当前view的标记和draw的标记一致，则将图片设置
						if (imageDrawable != null
								&& imageView.getTag().equals(imageUrl))
							// imageView.setImageDrawable(imageDrawable);
							imageView.setImageBitmap(DownloadService
									.drawable2Bitmap(imageDrawable));
						/*
						 * else imageView.setImageResource(R.drawable.tempicon);
						 */
					}
				});
		drawMap.put(imageUrl, drawable);
		return drawable;
	}
}