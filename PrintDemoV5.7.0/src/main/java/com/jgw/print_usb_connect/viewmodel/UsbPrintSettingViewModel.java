package com.jgw.print_usb_connect.viewmodel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jgw.common_library.base.viewmodel.BaseViewModel;
import com.jgw.common_library.http.Resource;
import com.jgw.common_library.livedata.ValueKeeperLiveData;
import com.jgw.common_library.utils.LogUtils;
import com.jgw.common_library.utils.ResourcesUtils;
import com.jgw.common_library.utils.ToastUtils;
import com.jgw.print_usb_connect.SprtPrintUtils;
import com.jgw.print_usb_connect.UsbPrintSettingActivity;
import com.jgw.print_usb_connect.bean.UsbPrintInfoBean;
import com.printer.demo.R;
import com.printer.sdk.PrinterConstants;
import com.printer.sdk.usb.USBPort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UsbPrintSettingViewModel extends BaseViewModel {

    private final MutableLiveData<Resource<String>> mConnectStatusChangeLiveData = new ValueKeeperLiveData<>();
    private final MutableLiveData<Resource<UsbPrintInfoBean>> mUsbDevicesChangeLiveData = new ValueKeeperLiveData<>();

    private final List<BluetoothDevice> bluetoothDeviceArrayList = new ArrayList<>();

    public UsbPrintSettingViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Resource<String>> getConnectStatusChangeLiveData() {
        return mConnectStatusChangeLiveData;
    }

    // 用于接受连接状态消息的 Handler
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @SuppressLint("ShowToast")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PrinterConstants.Connect.SUCCESS:
                    mConnectStatusChangeLiveData.setValue(new Resource<>(Resource.SUCCESS, "", "连接成功!"));
                    LogUtils.xswShowLog("ZL at SettingActivity Handler() 连接成功!");
                    break;
                case PrinterConstants.Connect.FAILED:
                    mConnectStatusChangeLiveData.setValue(new Resource<>(Resource.SUCCESS, "", "连接失败,请重试!"));
                    LogUtils.xswShowLog("ZL at SettingActivity Handler() 连接失败!");
                    break;
                case PrinterConstants.Connect.CLOSED:
                    LogUtils.xswShowLog("ZL at SettingActivity Handler() 连接关闭!");
                    break;
                case PrinterConstants.Connect.NODEVICE:
                    ToastUtils.showToast(ResourcesUtils.getString(R.string.conn_no));
                    break;
                default:
                    break;
            }

        }

    };


    public Handler getHandler() {
        return mHandler;
    }

    public void clearDeviceList() {
        bluetoothDeviceArrayList.clear();
    }


    public void getDiscoveryUsbDevices(UsbPrintSettingActivity context) {
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> devices = manager.getDeviceList();

        for (UsbDevice device : devices.values()) {
            if (USBPort.isUsbPrinter(device)) {
                UsbPrintInfoBean usbPrintInfoBean = new UsbPrintInfoBean();
                usbPrintInfoBean.deviceName = device.getDeviceName();
                usbPrintInfoBean.vendorId = device.getVendorId();
                usbPrintInfoBean.productId = device.getProductId();
                usbPrintInfoBean.productName = device.getProductName();
                usbPrintInfoBean.mUSBDevice = device;
                mUsbDevicesChangeLiveData.setValue(new Resource<>(Resource.LOADING, usbPrintInfoBean, ""));
            }
        }
    }


    public LiveData<Resource<UsbPrintInfoBean>> getUsbDevicesChangeLiveData() {
        return mUsbDevicesChangeLiveData;
    }


}
