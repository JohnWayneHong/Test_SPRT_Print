package com.jgw.print_usb_connect;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.jgw.common_library.base.ui.BaseActivity;
import com.jgw.common_library.http.Resource;
import com.jgw.common_library.utils.LogUtils;
import com.jgw.common_library.utils.ResourcesUtils;
import com.jgw.common_library.utils.ToastUtils;
import com.jgw.common_library.utils.click_utils.ClickUtils;
import com.jgw.common_library.utils.click_utils.listener.OnItemSingleClickListener;
import com.jgw.print_usb_connect.adapter.UsbPrintRecyclerAdapter;
import com.jgw.print_usb_connect.bean.UsbPrintInfoBean;
import com.jgw.print_usb_connect.viewmodel.UsbPrintSettingViewModel;

import com.printer.demo.R;
import com.printer.demo.databinding.ActivityUsbPrintSettingBinding;
import com.printer.demo.global.GlobalContants;
import com.printer.demo.utils.PrefUtils;
import com.printer.sdk.PrinterConstants;
import com.printer.sdk.PrinterInstance;
import com.printer.sdk.utils.Utils;

/**
 * Created by XiongShaoWu
 * on 2020/4/9
 */
public class UsbPrintSettingActivity extends BaseActivity<UsbPrintSettingViewModel, ActivityUsbPrintSettingBinding> implements View.OnClickListener, OnItemSingleClickListener {
    private UsbPrintRecyclerAdapter mAdapter;
    public static PrinterInstance myPrinter;
    private UsbDevice mUSBDevice;
    private static final String ACTION_USB_PERMISSION = "com.android.usb.USB_PERMISSION";


    @Override
    public void initView() {
        mBindingView.rvcBluetoothConnection.setEmptyLayout(R.layout.item_empty);
    }

    @Override
    public void initData() {
        setTitle("USB打印机连接");
        setRight("搜索设备");

        mBindingView.tvBluetoothConnectionDevice.setText("无");
        mAdapter = new UsbPrintRecyclerAdapter();
        mBindingView.rvBluetoothConnection.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);

        starSearchUsb();

        if (SprtPrintUtils.getPrinter() != null && SprtPrintUtils.checkPrintStatus()) {
            mBindingView.tvBluetoothConnectionDevice.setText("已连接");
        }
    }



    @Override
    public void initLiveData() {
        mViewModel.getConnectStatusChangeLiveData().observe(this, resource -> {
            switch (resource.getLoadingStatus()) {
                case Resource.SUCCESS:

                    SprtPrintUtils.setPrinter(myPrinter);
                    mBindingView.tvBluetoothConnectionDevice.setText(mUSBDevice.getProductName());
//                    setResult(RESULT_OK);
//                    finish();
                    ToastUtils.showToast(resource.getErrorMsg());
                    break;
                case Resource.ERROR:
                    ToastUtils.showToast(resource.getErrorMsg());
                    break;
            }
        });

        mViewModel.getUsbDevicesChangeLiveData().observe(this, resource -> {
            switch (resource.getLoadingStatus()) {
                case Resource.LOADING:
                    mAdapter.notifyAddItem(resource.getData());
                    break;
                case Resource.SUCCESS:
                    ToastUtils.showToast(resource.getErrorMsg());
                    break;
            }
        });
    }

    @Override
    protected void initListener() {
        super.initListener();
        ClickUtils.register(this)
                .addOnClickListener()
                .addView(mBindingView.tvTestPrintCpcl, mBindingView.tvTestPrintTspl,mBindingView.tvTestPrintCheck,mBindingView.tvTestPrintBitmap)
                .submit();
    }

    @Override
    protected void clickRight() {
        super.clickRight();
        starSearchUsb();
    }


    private void starSearchUsb() {
        mViewModel.clearDeviceList();
        mAdapter.notifyRemoveListItem();

        mViewModel.getDiscoveryUsbDevices(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {

            }
        }
    }


    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (view == mBindingView.tvTestPrintCpcl) {
            SprtPrintUtils.doPrint(myPrinter,getResources());
        } else if (view == mBindingView.tvTestPrintTspl) {
            SprtPrintUtils.doPrintTSPL(myPrinter,this);
        } else if (view == mBindingView.tvTestPrintCheck) {
            SprtPrintUtils.checkPrintStatus();
        } else if (view == mBindingView.tvTestPrintBitmap) {
            SprtPrintUtils.doPrintTSPLBitmap(myPrinter,this);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        try {
            getUsbPrintConnect(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getUsbPrintConnect(int position) {
        UsbPrintInfoBean usbPrintInfoBean = mAdapter.getDataList().get(position);
        mUSBDevice = usbPrintInfoBean.mUSBDevice;
        myPrinter = PrinterInstance.getPrinterInstance(this, usbPrintInfoBean.mUSBDevice, mViewModel.getHandler());

        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (mUsbManager.hasPermission(mUSBDevice)) {
            myPrinter.openConnection();
        } else {
            // 没有权限询问用户是否授予权限
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
                    new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            registerReceiver(mUsbReceiver, filter);
            mUsbManager.requestPermission(mUSBDevice, pendingIntent); // 该代码执行后，系统弹出一个对话框
        }

    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @SuppressLint("NewApi")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    unregisterReceiver(mUsbReceiver);
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && mUSBDevice.equals(device)) {
                        myPrinter.openConnection();
                    } else {
                        mViewModel.getHandler().obtainMessage(PrinterConstants.Connect.FAILED).sendToTarget();
                        LogUtils.xswShowLog("授权失败!");
                    }
                }
            }
        }
    };

    public static void start(Activity context, int requestCode) {
        if (context == null) {
            return;
        }
        if (!context.isFinishing() && !context.isDestroyed()) {
            Intent intent = new Intent(context, UsbPrintSettingActivity.class);
            context.startActivityForResult(intent, requestCode);
        }
    }

    public static void start(Fragment fragment, int requestCode) {
        if (fragment == null || fragment.getActivity() == null) {
            return;
        }
        FragmentActivity context = fragment.getActivity();
        if (BaseActivity.isActivityNotFinished(context)) {
            Intent intent = new Intent(context, UsbPrintSettingActivity.class);
            context.startActivityForResult(intent, requestCode);
        }
    }

}
