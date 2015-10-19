package com.wandou.sittingdetect;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 *Title:
 *@author 豌豆先生 jitsiang@163.com
 *@date 2015年9月8日
 *@version 
 */
public class TransClass extends Application {

	private BluetoothAdapter bluetooth;
	private String addr;
	
	public BluetoothAdapter getBluetooth() {
		return bluetooth;
	}

	public void setBluetooth(BluetoothAdapter bluetooth) {
		this.bluetooth = bluetooth;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	
	
}
