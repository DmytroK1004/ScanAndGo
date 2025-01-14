package com.example.uhf_bt.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 万能的ListView GridView 适配器的ViewHolder 如果需要可以不断完善
 * @author sxf
 * 
 */
public class ViewHolder {
	private final SparseArray<View> mViews;
	private int mPosition;
	private View mConvertView;
	private Context mContext;

	private ViewHolder(Context context, ViewGroup parent, int layoutId, int position) {
		this.mPosition = position;
		this.mContext = context;
		this.mViews = new SparseArray<View>();
		mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,
				false);	
		// setTag
		mConvertView.setTag(this);
	}

	private ViewHolder(Context context, ViewGroup parent, View mConvertView, int position) {
		this.mPosition = position;
		this.mContext = context;
		this.mViews = new SparseArray<View>();
		this.mConvertView = mConvertView;
		// setTag
		this.mConvertView.setTag(this);
	}

	/**
	 * 拿到一个ViewHolder对象
	 * @param context
	 * @param convertView
	 * @param parent
	 * @param layoutId
	 * @param position
	 * @return
	 */
	public static ViewHolder get(Context context, View convertView, ViewGroup parent, int layoutId, int position) {
		if (convertView == null) {
			return new ViewHolder(context, parent, layoutId, position);
		}
		return (ViewHolder) convertView.getTag();
	}

	public static ViewHolder get(Context context, View convertView, ViewGroup parent, View mConvertView, int position) {
		if (convertView == null) {
			return new ViewHolder(context, parent, mConvertView, position);
		}
		return (ViewHolder) convertView.getTag();
	}

	/**
	 * 获取item
	 * @return
	 */
	public View getConvertView() {
		return mConvertView;
	}

	/**
	 * 通过控件的Id获取对于的控件，如果没有则加入views
	 * 
	 * @param viewId
	 * @return
	 */
	public <T extends View> T getView(int viewId) {
		View view = mViews.get(viewId);
		if (view == null) {
			view = mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T) view;
	}


	/**
	 * 为TextView设置字符串
	 * @param viewId
	 * @param text
	 * @return
	 */
	public ViewHolder setText(int viewId, CharSequence text) {
		TextView view = getView(viewId);
		view.setText(text);
		return this;
	}

	/**
	 * 为TextView设置字符串
	 * @param viewId
	 * @param resId
     * @return
     */
	public ViewHolder setText(int viewId, int resId) {
		return setText(viewId, mContext.getString(resId));
	}

	/**
	 * 为TextView设置字符串
	 * @param viewId
	 * @param text
	 * @param colorId
     * @return
     */
	public ViewHolder setText(int viewId, CharSequence text, int colorId) {
		return setText(viewId, text, colorId, -1);
	}

	/**
	 * 为TextView设置字符串
	 * @param viewId
	 * @param text
	 * @param colorId
	 * @return
	 */
	public ViewHolder setText(int viewId, CharSequence text, int colorId, int spSize) {
		TextView view = getView(viewId);
		view.setText(text);
		if(spSize > 0) {
			view.setTextSize(TypedValue.COMPLEX_UNIT_SP, spSize);
		}
		try {
			view.setTextColor(mContext.getResources().getColor(colorId));
		} catch (Resources.NotFoundException e) { }
		return this;
	}

	/**
	 * 为TextView设置字符串
	 * @param viewId
	 * @param resId
	 * @param colorId
     * @return
     */
	public ViewHolder setText(int viewId, int resId, int colorId) {
		return setText(viewId, mContext.getString(resId), colorId);
	}
	
	/**
	 * 为ImageView设置图片
	 * @param viewId
	 * @param drawableId
	 * @return
	 */
	public ViewHolder setImageResource(int viewId, int drawableId) {
		ImageView view = getView(viewId);
		view.setImageResource(drawableId);
		return this;
	}

	/**
	 * 为View设置背景
	 * @param viewId
	 * @param colorId
	 * @return
	 */
	public ViewHolder setBackgroundColor(int viewId, int colorId) {
		return setBackgroundColor(getView(viewId), colorId);
	}

	/**
	 * 为View设置背景
	 * @param view
	 * @param colorId
	 * @return
	 */
	public ViewHolder setBackgroundColor(View view, int colorId) {
		view.setBackgroundColor(mContext.getResources().getColor(colorId));
		return this;
	}

	/**
	 * 为View设置背景
	 * @param viewId
	 * @param drawableId
     * @return
     */
	public ViewHolder setBackground(int viewId, int drawableId) {
		return setBackground(getView(viewId), drawableId);
	}

	/**
	 * 为View设置背景
	 * @param view
	 * @param drawableId
	 * @return
	 */
	public ViewHolder setBackground(View view, int drawableId) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			view.setBackground(mContext.getDrawable(drawableId));
		} else {
			view.setBackgroundResource(drawableId);
		}
		return this;
	}

	/**
	 * 为View设置背景
	 * @param viewId
	 * @param drawableId
	 * @return
	 */
	public ViewHolder setViewBackgroundDrawable(int viewId, int drawableId) {
		View view = getView(viewId);
		view.setBackgroundResource(drawableId);
		return this;
	}

	/**
	 * 设置View的可见性
	 * @param resId
	 * @param visibility
	 * @return
	 */
	public ViewHolder setVisibility(int resId, int visibility) {
		getView(resId).setVisibility(visibility);
		return this;
	}
	
	/**
	 * 为textView 设置背景颜色
	 * @param viewId
	 * @param resid
	 * @return
	 */
	public ViewHolder setTextViewColor(int viewId, int resid) {
		TextView view = getView(viewId);
		view.setBackgroundResource(resid);
		return this;
	}

	/**
	 * 为ImageView设置图片
	 * @param viewId
	 * @param bmp
	 * @return
	 */
	public ViewHolder setImageBitmap(int viewId, Bitmap bmp) {
		ImageView view = getView(viewId);
		view.setImageBitmap(bmp);
		return this;
	}

	/**
	 * 返回当前位置
	 * @return
	 */
	public int getPosition() {
		return mPosition;
	}

	/**
	 * 根据id给view设置点击事件
	 * @param viewId
	 * @return
	 */
	public ViewHolder setViewClickListener(int viewId, OnClickListener listener){
		getView(viewId).setOnClickListener(listener);
		return this;
	}
}
