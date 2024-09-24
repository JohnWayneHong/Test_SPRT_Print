package com.jgw.print_usb_connect;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jgw.common_library.utils.LogUtils;
import com.jgw.common_library.utils.ToastUtils;
import com.printer.demo.R;
import com.printer.demo.global.GlobalContants;
import com.printer.demo.utils.PrefUtils;
import com.printer.sdk.PrinterConstants;
import com.printer.sdk.PrinterInstance;
import com.printer.sdk.exception.ParameterErrorException;
import com.printer.sdk.exception.PrinterPortNullException;
import com.printer.sdk.exception.ReadException;
import com.printer.sdk.exception.WriteException;
import com.printer.sdk.utils.Utils;

/**
 * @author : hwj
 * @date : 2024/9/19
 * description : 思普瑞特打印工具类
 */
public class SprtPrintUtils {

    private static PrinterInstance mPrinter;

    public static PrinterInstance getPrinter() {
        return mPrinter;
    }

    public static void setPrinter(PrinterInstance mPrinter) {
        SprtPrintUtils.mPrinter = mPrinter;
    }

    public static boolean checkPrintStatus() {
        if (mPrinter == null) {
            ToastUtils.showToast("请先连接打印机设备");
            return false;
        }

        switch (mPrinter.getCurrentStatus()) {
            case 0:
                LogUtils.xswShowLog("打印状态正常");
                return true;
            case -1:
                ToastUtils.showToast("接收数据失败");
                return false;
            case -2:
                ToastUtils.showToast("打印机缺纸");
                return false;
            case -3:
                ToastUtils.showToast("打印机纸将尽");
                return false;
            case -4:
                ToastUtils.showToast("打印机开盖");
                return false;
            case -5:
                ToastUtils.showToast("发送数据失败");
                return false;
            default:
                ToastUtils.showToast("状态未知!");
                return false;
        }
    }

    public static void doPrint(final PrinterInstance iPrinter, final Resources resources) {
        iPrinter.pageSetup(PrinterConstants.LablePaperType.Size_58mm, 384, 540);
        iPrinter.drawText(0, 0, 200, 200, PrinterConstants.PAlign.START, PrinterConstants.PAlign.START, "扫一扫二维码连接打印机", PrinterConstants.LableFontSize.Size_48, 0,
                0, 0, 0, PrinterConstants.PRotate.Rotate_0);
        // iPrinter.drawQrCode(230, 0, "12345678", PRotate.Rotate_0, 6, 1);
        iPrinter.drawBarCode(1, 150, "12345678", PrinterConstants.PBarcodeType.CODE128, 2, 75, PrinterConstants.PRotate.Rotate_0);
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.ztl);
        iPrinter.drawGraphic(0, 240, Utils.zoomImage(bitmap, 384,0));
        iPrinter.print(PrinterConstants.PRotate.Rotate_0, 0);
    }

    public static void doPrintTSPL(final PrinterInstance iPrinter, final Context mContext) {
        int left = PrefUtils.getInt(mContext, "leftmargin", 0);
        int top = PrefUtils.getInt(mContext, "topmargin", 0);
        int numbers = PrefUtils.getInt(mContext, "printnumbers", 1);
        int isBeep = PrefUtils.getInt(mContext, "isBeep", 0);
        int isOpenCash = PrefUtils.getInt(mContext, "isOpenCash", 0);
        try {

            int statusCode = iPrinter.getPrinterStatusTSPL();
            if (statusCode != 0) {
                ToastUtils.showToast("打印机状态异常 错误码:" + statusCode);
                return;
            }

            // 设置标签纸大小
            iPrinter.pageSetupTSPL(PrinterConstants.SIZE_58mm, 56 * 8, 30 * 8);
            // 清除缓存区内容
            iPrinter.sendStrToPrinterTSPL("CLS\r\n");
            // 设置标签的参考坐标原点
            if (left == 0 || top == 0) {
                // 不做设置，默认
            } else {
                iPrinter.sendStrToPrinterTSPL("REFERENCE " + left * 8 + "," + top * 8 + "\r\n");
            }

            //TODO 设置打印机功能
            iPrinter.setPrinterTSPL(PrinterConstants.CommandTSPL.TEAR, 1);

            // 打印第一行的内容
            iPrinter.setPrinterTSPL(PrinterConstants.CommandTSPL.DENSITY, 10);
            iPrinter.drawTextTSPL(0, 0, 56 * 8, 8 * 8, PrinterConstants.PAlign.CENTER, PrinterConstants.PAlign.CENTER, true, 2, 2, null,
                    "蜜果蜜制鲜饮");
            // 打印第二行内容
            iPrinter.setPrinterTSPL(PrinterConstants.CommandTSPL.DENSITY, 1);
            iPrinter.drawTextTSPL(0, 8 * 8, 56 * 8, 8 * 8 * 2, PrinterConstants.PAlign.END, PrinterConstants.PAlign.CENTER, true, 2, 2, null,
                    "蜜果奶茶（中）");
            // 打印第三行内容
            iPrinter.draw2DBarCodeTSPL(20, 8 * 8 * 2, PrinterConstants.TwoDarCodeType.QR, 2, 4, null, "http://code.kf315.net/u/14/9000000293322198");

//            iPrinter.drawTextTSPL(20, 8 * 8 * 2, true, 2, 2, null, "价格减二");
            // 打印第四行内容
//            iPrinter.drawTextTSPL(0, 8 * 8 * 3, 56 * 8, 8 * 8 * 4, PrinterConstants.PAlign.CENTER, PrinterConstants.PAlign.CENTER, true, 2, 2,
//                    null, "￥6.00");
            // 打印第五行内容
//            iPrinter.drawTextTSPL(0, 8 * 8 * 4, 56 * 8, 8 * 8 * 5, PrinterConstants.PAlign.CENTER, PrinterConstants.PAlign.CENTER, true, 1, 1,
//                    null, PrefUtils.getSystemTime2());

            // 判断是否响应蜂鸣器
            if (isBeep == 1) {
                // 打印前响
                iPrinter.beepTSPL(1, 1000);
                Thread.sleep(3000);
                // 打印
                iPrinter.printTSPL(numbers, 1);
            } else if (isBeep == 2) {
                // 打印
                iPrinter.printTSPL(numbers, 1);
                // 打印后响
                // Thread.sleep(3000);
                iPrinter.beepTSPL(1, 300);
            } else {
                // 打印
                iPrinter.printTSPL(numbers, 1);
            }

        } catch (WriteException | PrinterPortNullException | ParameterErrorException | ReadException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void doPrintTSPLBitmap(final PrinterInstance iPrinter, final Context mContext) {
        int left = PrefUtils.getInt(mContext, "leftmargin", 0);
        int top = PrefUtils.getInt(mContext, "topmargin", 0);
        int numbers = PrefUtils.getInt(mContext, "printnumbers", 1);
        int isBeep = PrefUtils.getInt(mContext, "isBeep", 0);
        int isOpenCash = PrefUtils.getInt(mContext, "isOpenCash", 0);
        try {
            // 设置标签纸大小
            iPrinter.pageSetupTSPL(PrinterConstants.SIZE_58mm, 56 * 8, 45 * 8);
            // 清除缓存区内容
            iPrinter.sendStrToPrinterTSPL("CLS\r\n");
            // 设置标签的参考坐标原点
            if (left == 0 || top == 0) {
                // 不做设置，默认
            } else {
                iPrinter.sendStrToPrinterTSPL("REFERENCE " + left * 8 + "," + top * 8 + "\r\n");
            }

//            Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.my_monochrome_image);
//            if (bitmap1.getWidth() > PrinterConstants.paperWidth) {
//                bitmap1 = Utils.zoomImage(bitmap1, PrinterConstants.paperWidth, PrefUtils.getInt(this, GlobalContants.PRINTERTYPE, 0));
//            }


            Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.goodwork);
//            Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.my_monochrome_image);
            iPrinter.drawBitmapTSPL(0, 0, 0,bmp);


            // 判断是否响应蜂鸣器
            if (isBeep == 1) {
                // 打印前响
                iPrinter.beepTSPL(1, 1000);
                Thread.sleep(3000);
                // 打印
                iPrinter.printTSPL(numbers, 1);
            } else if (isBeep == 2) {
                // 打印
                iPrinter.printTSPL(numbers, 1);
                // 打印后响
                // Thread.sleep(3000);
                iPrinter.beepTSPL(1, 300);
            } else {
                // 打印
                iPrinter.printTSPL(numbers, 1);
            }

        } catch (WriteException e) {
            e.printStackTrace();
        } catch (PrinterPortNullException e) {
            e.printStackTrace();
        } catch (ParameterErrorException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
