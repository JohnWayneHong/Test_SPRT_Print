package com.jgw.print_usb_connect.adapter;


import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.jgw.common_library.base.adapter.CustomRecyclerAdapter;
import com.jgw.common_library.utils.click_utils.ClickUtils;
import com.jgw.print_usb_connect.bean.UsbPrintInfoBean;
import com.printer.demo.R;
import com.printer.demo.databinding.ItemUsbPrintInfoBinding;

public class UsbPrintRecyclerAdapter extends CustomRecyclerAdapter<UsbPrintInfoBean> {

    @Override
    public ContentViewHolder<?> onCreateCustomViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_CONTENT1) {
            return new ContentType1ViewHolder(DataBindingUtil.inflate(mLayoutInflater
                    , R.layout.item_usb_print_info, parent, false));
        }
        return null;
    }

    @Override
    public void onBindCustomViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ContentType1ViewHolder) {
            UsbPrintInfoBean bean = mList.get(position);
           ((ContentType1ViewHolder) holder).mBindingView.setData(bean);
        }
    }

    private class ContentType1ViewHolder extends ContentViewHolder<ItemUsbPrintInfoBinding> {

        ContentType1ViewHolder(ItemUsbPrintInfoBinding view) {
            super(view);
            ClickUtils.register(this)
                    .addView(view.llUsbInfoItem)
                    .addItemClickListener()
                    .submit();
        }

        
    }
}
