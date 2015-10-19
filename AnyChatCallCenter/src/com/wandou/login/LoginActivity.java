package com.wandou.login;

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.bussinesscenter.BussinessCenter;
import com.bairuitech.callcenter.HallActivity;
import com.bairuitech.callcenter.R;
import com.bairuitech.util.*;
import com.wandou.sittingdetect.AboutActivity;
import com.wandou.sittingdetect.BluetoothCommService;
import com.wandou.sittingdetect.ScanDeviceActivity;
import com.wandou.sittingdetect.SittingDetectService;
import com.wandou.sittingdetect.TransClass;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity implements AnyChatBaseEvent,
		OnClickListener {
	private static final String TAG = "LoginActivity";
	private static final boolean D = true;
	private Button configBtn;
	private Button loginBtn, regButton;
	private CheckBox mCheckRemember;
	private ConfigEntity configEntity;
	private EditText mEditAccount, mEditPwd;
	private ProgressDialog mProgressLogin;
	private Dialog dialog;
	private AnyChatCoreSDK anychat;
	private boolean bNeedRelease = false;

	// 本地蓝牙适配器
	private BluetoothAdapter bluetooth;

	// 请求开启蓝牙的requestCode
	static final int REQUEST_ENABLE_BT = 1;
	// 请求连接的requestCode
	static final int REQUEST_CONNECT_DEVICE = 2;
	// bluetoothCommService 传来的消息状态
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initSdk();
		intParams();
		initView();
		initLoginProgress();

		// 获得本地蓝牙设备
		bluetooth = BluetoothAdapter.getDefaultAdapter();
		if (bluetooth == null) {// 设备没有蓝牙设备
			Toast.makeText(this, "没有找到蓝牙适配器", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onStart() {
		if (!bluetooth.isEnabled()) {
			// 请求打开蓝牙设备
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		}
		super.onStart();
	}

	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bNeedRelease) {
			anychat.Logout();
			anychat.Release();
			android.os.Process.killProcess(android.os.Process.myPid());
		}
		BussinessCenter.getBussinessCenter().realseData();
	}

	protected void intParams() {
		configEntity = ConfigService.LoadConfig(this);
		BussinessCenter.getBussinessCenter();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		int tag = intent.getIntExtra("INTENT", BaseConst.AGAIGN_LOGIN);
		if (tag == BaseConst.AGAIGN_LOGIN) {
			if (anychat != null) {
				anychat.Logout();
				anychat.SetBaseEvent(this);
			}
		} else if (tag == BaseConst.APP_EXIT) {
			this.finish();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		return super.onKeyDown(keyCode, event);
	}

	private void initSdk() {
		if (anychat == null) {
			anychat = new AnyChatCoreSDK();
			anychat.SetBaseEvent(this);
			anychat.InitSDK(android.os.Build.VERSION.SDK_INT, 0);
			bNeedRelease = true;
		}
	}

	private void initView() {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		this.setContentView(R.layout.login_layout);

		mEditAccount = (EditText) findViewById(R.id.edit_account);
		mEditPwd = (EditText) findViewById(R.id.edit_pwd);
		mCheckRemember = (CheckBox) findViewById(R.id.check_issavepass);
		mCheckRemember.setTextColor(Color.BLACK);
		loginBtn = (Button) findViewById(R.id.btn_login);
		loginBtn.setOnClickListener(this);
		regButton = (Button) findViewById(R.id.btn_register);
		regButton.setOnClickListener(this);
		configBtn = (Button) findViewById(R.id.btn_setting);
		configBtn.setOnClickListener(this);
		if (configEntity.IsSaveNameAndPw) {
			mCheckRemember.setChecked(true);
			if (configEntity.name != null) {
				mEditAccount.setText(configEntity.name);
				mEditPwd.setText(configEntity.password);
			}
		} else
			mCheckRemember.setChecked(false);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_login:
			doLogin();
			break;
		case R.id.btn_setting:
			dialog = DialogFactory.getDialog(DialogFactory.DIALOGID_CONFIG,
					configEntity, this);
			dialog.show();
			break;
		case R.id.btn_register:
			dialog = DialogFactory.getDialog(DialogFactory.DIALOGID_REGISTER,
					null, this);
			dialog.show();
			break;
		}
	}

	private void initLoginProgress() {
		mProgressLogin = new ProgressDialog(this);
		mProgressLogin.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				loginBtn.setClickable(true);
			}
		});
		mProgressLogin.setMessage(this.getString(R.string.login_progress));
	}

	private void Login() {
		String strUserName = mEditAccount.getEditableText().toString();
		String strUserPwd = mEditPwd.getEditableText().toString();
		if (mCheckRemember.isChecked()) {
			configEntity.IsSaveNameAndPw = true;
			configEntity.name = strUserName;
			configEntity.password = strUserPwd;
		} else {
			configEntity.IsSaveNameAndPw = false;
		}
		ConfigService.SaveConfig(this, configEntity);
		if (mEditAccount.getText().length() == 0) {
			BaseMethod.showToast(
					this.getString(R.string.str_account_input_hint), this);
			return;
		}
		this.anychat.Connect(configEntity.ip, configEntity.port);
		this.anychat.Login(strUserName, strUserPwd);
		loginBtn.setClickable(false);
		mProgressLogin.show();
	}

	@Override
	public void OnAnyChatConnectMessage(boolean bSuccess) {
		if (!bSuccess) {
			BaseMethod
					.showToast(getString(R.string.server_connect_error), this);
			mProgressLogin.dismiss();
		} else {
		}
	}

	@Override
	public void onAttachedToWindow() {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {
		// 没有运行
	}

	@Override
	public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
		Log.e(TAG, "OnAnyChatLinkCloseMessage ");
	}

	@Override
	public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {
		if (dwErrorCode == 0) {
			BussinessCenter.selfUserId = dwUserId;
			BussinessCenter.selfUserName = mEditAccount.getText().toString();
			Intent intent = new Intent();
			intent.setClass(this, HallActivity.class);
			this.startActivity(intent);
		} else if (dwErrorCode == 200) {
			BaseMethod.showToast(getString(R.string.str_lggin_failed), this);
		} else if (dwErrorCode == 100) {
			BaseMethod.showToast(getString(R.string.str_verify_failed), this);
		}
		mProgressLogin.dismiss();
		Log.e(TAG, "OnAnyChatLoginMessage");
	}

	@Override
	public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {
		// 没有运行
	}

	@Override
	public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
		// 没有运行
	}

	private void doLogin() {
		ConnectivityManager con = (ConnectivityManager) getSystemService(LoginActivity.CONNECTIVITY_SERVICE);
		boolean wifi = con.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.isConnectedOrConnecting();
		boolean internet = con.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.isConnectedOrConnecting();
		if (wifi | internet) {
			Login();
		} else {
			Toast.makeText(getApplicationContext(), "亲，网络连了么？",
					Toast.LENGTH_LONG).show();
		}
	}

	// 创建菜单选项
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	// 菜单项被点击
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// Launch the ScanDeviceActivity to see devices and do scan
			Intent serverIntent = new Intent(this, ScanDeviceActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			ensureDiscoverable();
			return true;
		case R.id.about:
			Intent intent = new Intent(LoginActivity.this, AboutActivity.class);
			startActivity(intent);
			return true;
		case R.id.exit:
			finish();
			return true;
		}
		return false;
	}

	/**
	 * onActivityResult方法，当启动startActivityForResult返回之后调用， 根据用户的操作来执行相应的操作
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				if (D) {
					Log.d(TAG, "打开蓝牙设备");
				}
				Toast.makeText(this, "成功打开蓝牙", Toast.LENGTH_SHORT).show();
			} else {
				if (D) {
					Log.d(TAG, "不允许打开蓝牙设备");
				}
				Toast.makeText(this, "不能打开蓝牙", Toast.LENGTH_SHORT).show();
				finish();// 用户不打开设备，程序结束
			}
			break;
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {// 用户选择连接的设备
				// Get the device MAC address
				String address = data.getExtras().getString(
						ScanDeviceActivity.EXTRA_DEVICE_ADDRESS);
				((TransClass) getApplication()).setAddr(address);
				((TransClass) getApplication()).setBluetooth(bluetooth);

				Log.e("TAG", "fuck4");
				// 将bluetooth和蓝牙address传递给service
				Intent serviceIntent = new Intent(LoginActivity.this,
						SittingDetectService.class);
				startService(serviceIntent);
 
			}
			break;
		}
		return;
	}

	private void ensureDiscoverable() {
		if (bluetooth.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			// 最长可见时间为300s
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}
}