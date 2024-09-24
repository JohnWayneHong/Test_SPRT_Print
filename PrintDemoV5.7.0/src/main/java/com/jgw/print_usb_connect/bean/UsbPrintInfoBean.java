package com.jgw.print_usb_connect.bean;

import android.hardware.usb.UsbDevice;

import java.util.HashMap;

public class UsbPrintInfoBean {
    public String deviceName;
    public int vendorId;
    public int productId;
    //设备名称
    public String productName;

    //USB设备对象
    public UsbDevice mUSBDevice;

    public String vendorIdAndProductId;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public UsbDevice getUSBDevice() {
        return mUSBDevice;
    }

    public void setUSBDevice(UsbDevice mUSBDevice) {
        this.mUSBDevice = mUSBDevice;
    }

    public void setVendorIdAndProductId(String vendorIdAndProductId) {
        this.vendorIdAndProductId = vendorIdAndProductId;
    }

    public String getVendorIdAndProductId() {
        return "Vid: " + vendorId + "  Pid: " + productId;
    }
}
