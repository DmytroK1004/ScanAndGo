package com.example.uhf_bt.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 万能的ListView GridView 适配器 
 * 被用于：PatientInfoActivity、MyBingRenActivity、YiZhuChaXunActivity
 * @author sxf
 * @param <T>
 */
public abstract class CommonAdapter<T> extends BaseAdapter {
    protected LayoutInflater mInflater;
    protected Context mContext;
    protected List<T> mDatas;
    protected int mItemLayoutId;

    protected View mConvertView;

    private boolean bgItemColorflag;

    public CommonAdapter(Context context, T[] mDatas, int itemLayoutId) {
        this(context, Arrays.asList(mDatas), itemLayoutId);
    }

    public CommonAdapter(Context context, List<T> mDatas, int itemLayoutId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mDatas = initNullDatas(mDatas);
        this.mItemLayoutId = itemLayoutId;
    }

    public void updateData(List<T> mDatas) {
    	this.mDatas = initNullDatas(mDatas);
    	notifyDataSetChanged();
    }
  
    @Override
    public int getCount()  
    {  
        return mDatas.size();  
    }  
  
    @Override
    public T getItem(int position) {
        return mDatas.get(position);
    }
  
    @Override
    public long getItemId(int position)  
    {  
        return position;  
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder = getViewHolder(position, convertView,  
                parent);  
        convert(viewHolder, getItem(position), position);  // 执行逻辑
        setItemColor(viewHolder, position);
        return viewHolder.getConvertView();  
  
    }  
  
    public abstract void convert(ViewHolder helper, T item, int position);
  
    private ViewHolder getViewHolder(int position, View convertView, ViewGroup parent) {
        if(mConvertView != null) {
            return ViewHolder.get(mContext, convertView, parent, mConvertView, position);
        }
        return ViewHolder.get(mContext, convertView, parent, mItemLayoutId, position);  
    }

    private void setItemColor(ViewHolder viewHolder, int position) {
        if(bgItemColorflag) {
            View converView = viewHolder.getConvertView();
            if(position % 2 == 0) {
                if (evenColorId == 0)
                    converView.setBackgroundColor(Color.parseColor(defEvenColor));
                 else
                    converView.setBackgroundColor(ContextCompat.getColor(mContext, evenColorId));
            } else {
                if(oddColorId == 0)
                    converView.setBackgroundColor(Color.parseColor(defOddColor));
                else
                    converView.setBackgroundColor(ContextCompat.getColor(mContext, oddColorId));
            }
        }
    }

    /**
     * 刷新指定position的view
     * @param adapterView
     * @param position
     */
    public void updateSingleView(AdapterView adapterView, int position) {
        int firstVisiblePosition = adapterView.getFirstVisiblePosition();
        int lastVisiblePosition = adapterView.getLastVisiblePosition();
        if (position < mDatas.size() && position >= firstVisiblePosition && position <= lastVisiblePosition) {
            View view = adapterView.getChildAt(position - firstVisiblePosition);
            getView(position, view, adapterView);
        }
    }

    /**
     * 刷新可见部分的View
     * @param adapterView
     */
    public void updateVisibleView(AdapterView adapterView) {
        int firstVisiblePosition = adapterView.getFirstVisiblePosition();
        int lastVisiblePosition = adapterView.getLastVisiblePosition();
        int position = firstVisiblePosition;
        while (position <= lastVisiblePosition && position < mDatas.size()) {
            View view = adapterView.getChildAt(position - firstVisiblePosition); // 从第一个可见的View开始刷新
            getView(position, view, adapterView);
            position++;
        }
    }

    private List<T> initNullDatas(List<T> datas) {
        if(datas == null) {
            datas = new ArrayList<>();
        }
        return datas;
    }

    public String ifNull(String value) {
        return ifNull(value, "");
    }
    public String ifNull(String value, String defVal) {
        return TextUtils.isEmpty(value) ? defVal : value;
    }

    public String getString(int resId) {
        return mContext.getString(resId);
    }

    public boolean isBgItemColorflag() {
        return bgItemColorflag;
    }

    public void setBgItemColorflag(boolean bgItemColorflag) {
        this.bgItemColorflag = bgItemColorflag;
    }

    private int evenColorId, oddColorId;
    private String defEvenColor = "#f0f0f0", defOddColor = "#ffffff";
    public void setItemColors(int evenColorId, int oddColorId) {
        bgItemColorflag = true;
        this.evenColorId = evenColorId;
        this.oddColorId = oddColorId;
    }
}
