package me.key.appmarket;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.key.appmarket.adapter.OnKeyAdapter;
import me.key.appmarket.utils.AppInfo;
import me.key.appmarket.utils.LocalUtils;
import me.key.appmarket.utils.LogUtils;
import me.key.appmarket.utils.ToastUtils;
import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.market.d9game.R;
/**
 * 免流专区
 * @author Administrator
 *
 */
public class NeceasyActivity extends FinalActivity {
	//返回键
	@ViewInject(id=R.id.back_onkey,click="onClick")
	private ImageView back_onkey;
	//标题
//	@ViewInject(id=R.id.titlename_onekey)
	private TextView titlename_onekey;
	//listview
	@ViewInject(id=R.id.recomapp_onkey)
	private ListView recomapp_onkey;
	//下一步按钮
	@ViewInject(id=R.id.nextoronkey,click="onClick")
	private TextView nextoronkey;
	//需要推荐的app
	@ViewInject(id=R.id.onkey_pb)
	private ProgressBar onkey_pb;
	private List<AppInfo> recomApp;
	//本地地址
	private String root;
	//数据适配器
	private OnKeyAdapter onekAdapter;
	//默认状态查询第一个即推荐应用
	private int tag = APP;
	private final static int APP = 0;
	private final static int GAME = 1;
	private final static int MOBILE = 2;
	
	private ArrayList<AppInfo> checkApp = new ArrayList<AppInfo>();
	private List<PackageInfo> packages;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.onkeyinstall);
		
		fillData("0");
	}
	//获得数据
	private void fillData(final String tag) {
		root = LocalUtils.getRoot(this);
		packages = NeceasyActivity.this.getPackageManager().getInstalledPackages(0);
		nextoronkey.setText("一键安装");
		titlename_onekey.setText("装机必备");
		new AsyncTask<Void, Void, Void>() {
			protected void onPreExecute() {
				onkey_pb.setVisibility(View.VISIBLE);
				recomapp_onkey.setVisibility(View.INVISIBLE);
			};
			@Override
			protected Void doInBackground(Void... params) {
		
				recomApp = LocalUtils.InitHomePager(tag,
						NeceasyActivity.this, root,packages);
				return null;
			}
			@Override
			protected void onPostExecute(Void result) {
				onkey_pb.setVisibility(View.GONE);
				recomapp_onkey.setVisibility(View.VISIBLE);
				onekAdapter = new OnKeyAdapter(NeceasyActivity.this, recomApp,checkApp);
				recomapp_onkey.setAdapter(onekAdapter);
				recomapp_onkey.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Boolean checked = onekAdapter.checkState.get(position);
						onekAdapter.checkState.set(position, !checked);
						if(checked) {
							checkApp.remove((AppInfo)onekAdapter.getItem(position));
						} else {
							checkApp.add((AppInfo)onekAdapter.getItem(position));
						}
						onekAdapter.notifyDataSetChanged();
					}
					
				});
				super.onPostExecute(result);
			}
		}.execute();
	
	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		//下一步
		case R.id.nextoronkey:
			++tag;
			for(AppInfo appInfo : checkApp) {
				LogUtils.d("OneKey", appInfo.getAppName());
				installApp(appInfo);
			}
			if(checkApp.size() == 0) {
				ToastUtils.show("您还没有选择任何应用");
			} else {
				finish();
			}
			break;
		case R.id.back_onkey :
				finish();
				break;
		}
	}
	/**
	 * 安装某个应用
	 * 
	 * @param mAppInfo
	 */
	private void installApp(AppInfo mAppInfo) {
		String str = mAppInfo.getApkName();
		String fileName = root + str;
		Intent intent = new Intent(Intent.ACTION_VIEW);
		Log.v("tag", "nano ROOT" + fileName);
		intent.setDataAndType(Uri.fromFile(new File(fileName)),
				"application/vnd.android.package-archive");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.startActivity(intent);
	}
}
