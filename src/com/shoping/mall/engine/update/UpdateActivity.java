﻿package com.shoping.mall.engine.update;

import java.io.File;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.shoping.mall.ConstantValue;
import com.shoping.mall.MainActivity;
import com.shoping.mall.R;
import com.shoping.mall.engine.listener.ResponseResultListener1;
import com.shoping.mall.util.LogUtil;
import com.shoping.mall.util.NetUtil;

public class UpdateActivity extends Activity implements ResponseResultListener1<UpdateResult<Update>>{

	protected static final String TAG = "SplashActivity";
	protected static final int SHOW_UPDATE_DIALOG = 0;
	protected static final int ENTER_HOME = 1;
	protected static final int URL_ERROR = 2;
	protected static final int NETWORK_ERROR = 3;
	protected static final int JSON_ERROR = 4;
	private TextView tv_splash_version;
	private String description;
	private TextView tv_update_info;
	/**
	 * 新版本的下载地址
	 */
	private String apkurl;
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash1);
		sp = getSharedPreferences("config", MODE_PRIVATE);
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_splash_version.setText("版本号" + getVersionName());
		tv_update_info = (TextView) findViewById(R.id.tv_update_info);
		boolean update = sp.getBoolean("update", false);
		if(update){
			// 检查升级
			checkUpdate();
		}else{
			//自动升级已经关闭
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					//进入主页面
					enterHome();
					
				}
			}, 2000);
			
		}
	
		AlphaAnimation aa = new AlphaAnimation(0.2f, 1.0f);
		aa.setDuration(500);
		findViewById(R.id.rl_root_splash).startAnimation(aa);
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOW_UPDATE_DIALOG:// 显示升级的对话框
				Log.i(TAG, "显示升级的对话框");
				showUpdateDialog();
				break;
			case ENTER_HOME:// 进入主页面
				enterHome();
				break;

			case URL_ERROR:// URL错误
				enterHome();
				Toast.makeText(getApplicationContext(), "URL错误", 0).show();

				break;

			case NETWORK_ERROR:// 网络异常
				enterHome();
				Toast.makeText(UpdateActivity.this, "网络异常", 0).show();
				break;

			case JSON_ERROR:// JSON解析出错
				enterHome();
				Toast.makeText(UpdateActivity.this, "JSON解析出错", 0).show();
				break;

			default:
				break;
			}
		}

	};

	/**
	 * 检查是否有新版本，如果有就升级
	 */
	private void checkUpdate() {

		
		NetUtil.get(ConstantValue.SERVER_URL + ConstantValue.UPDATE, new AsyncHttpResponseHandler(){

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				// 得到服务器的版本信息
				String result = new String(arg2);
				// json解析
				JSONObject obj;
				try {
					obj = new JSONObject(result);
					String version = (String) obj.get("version");
					description = (String) obj.get("description");
					apkurl = (String) obj.get("apkurl");
					// 校验是否有新版本
					if (getVersionName().equals(version)) {
						// 版本一致，没有新版本，进入主页面
						enterHome();
					} else {
						// 有新版本，弹出一升级对话框
						Log.i(TAG, "显示升级的对话框");
						showUpdateDialog();
						
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


			}
			
			@Override
			public void onFinish() {
				super.onFinish();
			}
			
		});
	}

	/**
	 * 弹出升级对话框
	 */
	protected void showUpdateDialog() {
		//this = Activity.this
		AlertDialog.Builder builder = new Builder(UpdateActivity.this);
		builder.setTitle("提示升级");
//		builder.setCancelable(false);//强制升级
		builder.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				//进入主页面
				enterHome();
				dialog.dismiss();
				
			}
		});
		builder.setMessage(description);
		builder.setPositiveButton("立刻升级", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				downloadApkAndInstall();
			}

			private void downloadApkAndInstall() {
				// TODO Auto-generated method stub
				// 下载APK，并且替换安装
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// sdcard存在
					// afnal
					FinalHttp finalhttp = new FinalHttp();
					finalhttp.download(apkurl, Environment
							.getExternalStorageDirectory().getAbsolutePath()+"/mobilesafe2.0.apk",
							new AjaxCallBack<File>() {

								@Override
								public void onFailure(Throwable t, int errorNo,
										String strMsg) {
									t.printStackTrace();
									Toast.makeText(getApplicationContext(), "下载失败", 1).show();
									super.onFailure(t, errorNo, strMsg);
								}

								@Override
								public void onLoading(long count, long current) {
									// TODO Auto-generated method stub
									super.onLoading(count, current);
									tv_update_info.setVisibility(View.VISIBLE);
									//当前下载百分比
									int progress = (int) (current * 100 / count);
									tv_update_info.setText("下载进度："+progress+"%");
								}

								@Override
								public void onSuccess(File t) {
									// TODO Auto-generated method stub
									super.onSuccess(t);
									installAPK(t);
								}
								/**
								 * 安装APK
								 * @param t
								 */
								private void installAPK(File t) {
								  Intent intent = new Intent();
								  intent.setAction("android.intent.action.VIEW");
								  intent.addCategory("android.intent.category.DEFAULT");
								  intent.setDataAndType(Uri.fromFile(t), "application/vnd.android.package-archive");
								  startActivity(intent);
								}
								
							
							});
				} else {
					Toast.makeText(getApplicationContext(), "没有sdcard，请安装上在试",
							0).show();
					return;
				}
			}
		});
		builder.setNegativeButton("下次再说", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				enterHome();// 进入主页面
			}
		});
		builder.show();

	}

	protected void enterHome() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		// 关闭当前页面
		finish();

	}

	/**
	 * 得到应用程序的版本名称
	 */

	private String getVersionName() {
		// 用来管理手机的APK
		PackageManager pm = getPackageManager();

		try {
			// 得到知道APK的功能清单文件
			PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}

	}
	
	private UpdateRequestInterface mUpdateRequestInterface = new UpdateController(this, this);

	@Override
	public void updateResult(UpdateResult<Update> result) {
		
		if(null != result){
			int resultCode = result.getResultCode();
			boolean success = result.isSuccess();
			Update update = result.getContent();
			switch (resultCode) {
			case UpdateResult.UPDATE_DATA_SUCCESS:
				if(success){
					if(null != update){
						LogUtil.i("更新数据获取成功：" + update.toString());
						String apkUrlStr = update.getApkurl();
						if(!TextUtils.isEmpty(apkUrlStr)){
							mUpdateRequestInterface.downloadApk(apkUrlStr, "");
						}
					}
				}
				break;
			case UpdateResult.UPDATE_DATA_FAIL:
				if(null != update){
					LogUtil.i("更新数据获取失败："+update.toString());
				}
				break;
			case UpdateResult.DOWNLOAD_APK_SUCC:
				if(null != update){
					LogUtil.i("下载最新apk成功：" + update.toString());
					File apkFile = update.getApkFile();
					if(null != apkFile){
						installAPK(apkFile);
					}
				}
				break;
			case UpdateResult.DOWNLOAD_APK_LOADING:
				if(null != update){
					LogUtil.i("正在下载最新apk：" + update.toString());
				}
				break;
			case UpdateResult.DOWNLOAD_APK_FAIL:
				if(null != update){
					LogUtil.i("下载最新apk失败：" + update.toString());
				}
				break;
			default:
				break;
			}
		}
		
	}
	
	
	/**
	 * 安装APK
	 * 
	 * @param t
	 */
	private void installAPK(File t) {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setDataAndType(Uri.fromFile(t),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}


}
