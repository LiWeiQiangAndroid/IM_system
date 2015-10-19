package com.wandou.sittingdetect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.wandou.lightctrl.WordToVoice;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SittingDetectService extends Service {

	public static final String NNet_Path = "/mnt/sdcard/data/com.ronoid.bluetoothcomm/nnet2.snet";
	public static final String MaxMinFile = "/mnt/sdcard/data/com.ronoid.bluetoothcomm/maxmin.txt";
 
	int[] max = { 0, 0, 0, 0 };
	int[] min = { 99999999, 99999999, 99999999, 99999999 };

	private BPNN myBPNN;
	private WordToVoice mTts;
	
	// 蓝牙设备
	private BluetoothDevice device = null;
	// 创建一个蓝牙串口服务对象
	private BluetoothCommService mCommService = null;

	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	private String mConnectedDeviceName = null;
	private int []dataReadtmp = new int[12]; 
	private int [] dataRead = new int[4]; 
	private double [][]normalData = new double[1][4];
	// Debugging
	private static final String TAG = "BluetoothComm";
	private static final boolean D = true;
	private int flag = 0;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	@Override
	public void onCreate() {
		super.onCreate();
		
		SpeechUtility.createUtility(this, SpeechConstant.APPID +"=55b71019");   
		// 初始化语音合成对象
		mTts = new WordToVoice(this, mInitListener);
		mTts.startSpeaking("欢迎来到坐姿检测系统");
		myBPNN = new BPNN();
		// myBPNN.Init_BPNN(NNet_Path, 4, 16, 2);
		// 这种方法应该是要浪费内存一些
		myBPNN.Init_BPNN(NNet_Path);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		BluetoothAdapter bluetooth = ((TransClass) getApplication())
				.getBluetooth();
		String address = ((TransClass) getApplication()).getAddr();
		device = bluetooth.getRemoteDevice(address);
		if (mCommService == null) {
			mCommService = new BluetoothCommService(mHandler);
			if (mCommService.getState() == BluetoothCommService.STATE_NONE) {
				// Start the Bluetooth services，开启监听线程
				mCommService.start();
			}
		}
		mCommService.connect(device);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Stop the Bluetooth chat services
		if (mCommService != null) {
			mCommService.stop();
		}
		if (D) {
			Log.e(TAG, "--- ON DESTROY ---");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				if (D) Log.d("TAG","初始化失败，错误码：" + code);
			}
		}
	};
	
	public void sittingDetect() throws IOException {
		int[][] result = new int[1][2];

		FileInputStream fis = new FileInputStream(new File(MaxMinFile));
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		String[] maxline = new String[4];
		String[] minline = new String[4];
		maxline = br.readLine().split(" ");
		minline = br.readLine().split(" ");
		for (int i = 0; i < 4; i++) {
			max[i] = Integer.parseInt(maxline[i]);
			min[i] = Integer.parseInt(minline[i]);
		}
		br.close();
		fis.close();

		//归一化数据
		normalData[0] = normalize(dataRead, max, min);
		Log.e("TAG", "fuck3:" + normalData[0][0] + " " + normalData[0][1] + " "
				+ normalData[0][2] + " " + normalData[0][3]);
		result = myBPNN.Test_BPNN(NNet_Path, normalData);
		if (result[0][0] == 1 && result[0][1] == 1) {
			mTts.startSpeaking("姿势正确");
			Toast.makeText(getApplicationContext(), "姿势正确",
					Toast.LENGTH_SHORT).show();
		} else{
			// currectSeatedEditText.setText("姿势错误，请保持正确姿势");
			mTts.startSpeaking("姿势错误");
			Toast.makeText(getApplicationContext(), "姿势错误，请保持正确姿势",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private double[] normalize(int[] dataRead,int []max,int []min ) {
		double []normal = {0.0,0.0,0.0,0.0};
	
		for (int i = 0; i < 4; i++) {
			normal[i] = (double)(dataRead[i]-min[i])/(max[i]-min[i]);
		}
		return normal;
	}
	
	/**
	 * Sends a message.
	 * @param message
	 *A string of text to send.
	 */
	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D) {
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				}
				switch (msg.arg1) {
				case BluetoothCommService.STATE_CONNECTED:			
					break;
				case BluetoothCommService.STATE_CONNECTING:
					break;
				case BluetoothCommService.STATE_LISTEN:
				case BluetoothCommService.STATE_NONE:
					break;
				}
				break;
			case MESSAGE_WRITE:
				// byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				// String writeMessage = new String(writeBuf);
				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			/************************************/

			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String result = bytesToHexString(readBuf);
				int rate = HexStringtoInt(result);
				
				Log.e("TAG","fuck1:"+rate);
				dataReadtmp[flag] = rate;

				if ( dataReadtmp[0] == 0xff) {
					flag++;
				} else {
					flag = 0;
				}
				if (flag == 12){
					dataRead = getReadData(dataReadtmp);
					Log.e("TAG","fuck2:"+dataRead[0]+" "+dataRead[1]+" "+dataRead[2]+" "+dataRead[3]);
					flag = 0;
					try {
						sittingDetect();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};
	
	
	private int[] getReadData(int[] dataReadtmp) {
		int []dataTmp = {0,0,0,0};
		dataTmp[0] = dataReadtmp[1]*256+dataReadtmp[2];
		dataTmp[1] = dataReadtmp[4]*256+dataReadtmp[5];
		dataTmp[2] = dataReadtmp[7]*256+dataReadtmp[8];
		dataTmp[3] = dataReadtmp[10]*256+dataReadtmp[11];			
		return dataTmp;
	}

	

	public static String bytesToHexString(byte[] bytes) {
		String result = "";
		for (int i = 0; i < bytes.length; i++) {
			String hexString = Integer.toHexString(bytes[i] & 0xFF);
			if (hexString.length() == 1) {
				hexString = '0' + hexString;
			}
			result += hexString.toUpperCase();
		}
		return result;
	}

	public static int HexStringtoInt(String hexString) {
		int a;
		a = Integer.valueOf(hexString, 16);
		return a;
	}
}