package com.example.uhf_bt.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSeekBar;

public class CircleSeekBar extends AppCompatSeekBar {

    /**
     * 绘制的类型 0为圆点，1为刻度
     */
    private int drawStyle = 1;

    /**
     * 是否绘制文字
     */
    private boolean drawText = false;

    /**
     * 刻度线画笔
     */
    private Paint mRulerPaint;
    /**
     * 圆点画笔
     */
    private Paint mCirclePaint;
    /**
     * 字体画笔
     */
    private Paint mTestPaint;

    /**
     * 绘制的个数,等分数等于刻度线的个数加1
     */
    private int mCount = 10;

    /**
     * 每条刻度线的宽度
     */
    private int mRulerWidth = 2;

    /**
     * 刻度线的颜色
     */
    private int mRulerColor = Color.WHITE;

    /**
     * 圆点的颜色
     */
    private int mCircleColor = Color.WHITE;

    /**
     * 字体的颜色
     */
    private int mTextColor = Color.WHITE;

    /**
     * 滑块上面是否要显示
     */
    private boolean isShowTopOfThumb = false;

    public CircleSeekBar(Context context) {
        super(context);
        init();
    }

    public CircleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        //创建绘制刻度线的画笔
        mRulerPaint = new Paint();
        mRulerPaint.setColor(mRulerColor);
        mRulerPaint.setAntiAlias(true);

        //创建绘制圆点的画笔
        mCirclePaint = new Paint();
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setAntiAlias(true);

        //创建绘制文字的画笔
        mTestPaint = new Paint();
        mTestPaint.setTextSize(35F);
        mTestPaint.setColor(mTextColor);
        mTestPaint.setAntiAlias(true);

        //Api21及以上调用，去掉滑块后面的背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSplitTrack(false);
        }
    }

    /**
     * 重写onDraw方法绘制刻度线
     *
     * @param canvas
     */
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //极限条件校验
        if (getWidth() <= 0 || mCount <= 0) {
            return;
        }
        //获取每一份的长度
        int length = 0;
        if (drawStyle==0){
            length = (getWidth()) / (mCount + 1);
        }else if (drawStyle==1){
            length = (getWidth() - getPaddingLeft() - getPaddingRight() - mCount * mRulerWidth) / (mCount + 1);
        }

        //计算刻度线的顶部坐标和底部坐标 注意绘制刻度的时候要设置minHeight
        int rulerTop = getHeight() / 2 - getMinimumHeight() / 2;
        int rulerBottom = rulerTop + getMinimumHeight();

        float drawY = (float) ((rulerTop+rulerBottom) / 2);
        float drawTestY = (float) ((rulerTop+rulerBottom) / 2) -50;
        //获取滑块的位置信息
        Rect thumbRect = null;
        if (getThumb() != null) {
            thumbRect = getThumb().getBounds();
        }

        int rulerLeft = 0 , rulerRight = 0;
        float drawX = 0 ;

        //绘制刻度线
        for (int i = 1; i <= mCount; i++) {
            //计算刻度线的左边坐标和右边坐标
            if (drawStyle==0){
                rulerLeft = i * length;
                rulerRight = rulerLeft ;
                drawX = (float) rulerLeft;
            }else if (drawStyle==1){
                rulerLeft = i * length + getPaddingLeft();
                rulerRight = rulerLeft + mRulerWidth;
                drawX = (float) ((rulerLeft+rulerRight)/2);
            }
            float drawTestX = (float) ((rulerLeft)-10);
            //判断是否需要绘制刻度线
            if (!isShowTopOfThumb && thumbRect != null && rulerLeft - getPaddingLeft() > thumbRect.left && rulerRight - getPaddingLeft() < thumbRect.right) {
                continue;
            }
            //进行绘制
            if (drawStyle==0){
                canvas.drawCircle(drawX, drawY, 8.0F, mCirclePaint);
            }else if (drawStyle==1){
                canvas.drawRect(rulerLeft, rulerTop, rulerRight, rulerBottom, mRulerPaint);
            }
            //是否绘制文字
            if (drawText){
                canvas.drawText(String.valueOf(i-1), drawTestX, drawTestY, mTestPaint);
            }
        }
    }

    /**
     * 设置绘制的个数
     *
     * @param mCount
     */
    public void setCount(int mCount) {
        this.mCount = mCount;
        requestLayout();
    }

    /**
     * 设置绘制类型
     *
     * @param mStyle
     */
    public void setDrawStyle (int mStyle) {
        this.drawStyle = mStyle;
        requestLayout();
    }

    /**
     * 设置是否绘制文字
     *
     * @param drawText
     */
    public void setDrawText (boolean drawText) {
        this.drawText = drawText;
        requestLayout();
    }

    /**
     * 设置刻度线的宽度，单位(px)
     *
     * @param mRulerWidth
     */
    public void setRulerWidth(int mRulerWidth) {
        this.mRulerWidth = mRulerWidth;
        requestLayout();
    }

    /**
     * 设置刻度线的颜色
     *
     * @param mRulerColor
     */
    public void setRulerColor(int mRulerColor) {
        this.mRulerColor = mRulerColor;
        if (mRulerPaint != null) {
            mRulerPaint.setColor(mRulerColor);
            requestLayout();
        }
    }
    /**
     * 设置圆点的颜色
     *
     * @param mCircleColor
     */
    public void setCircleColor(int mCircleColor) {
        this.mCircleColor = mCircleColor;
        if (mCirclePaint != null) {
            mCirclePaint.setColor(mCircleColor);
            requestLayout();
        }
    }

    /**
     * 设置字体的颜色
     *
     * @param TextColor
     */
    public void setTextColor(int TextColor) {
        this.mTextColor = TextColor;
        if (mTestPaint != null) {
            mTestPaint.setColor(mTextColor);
            requestLayout();
        }
    }

    /**
     * 设置整体颜色
     *
     * @param mCircleColor,mTextColor
     */
    public void setAllColor(int mCircleColor,int mTextColor) {
        //圆的颜色
        this.mCircleColor = mCircleColor;
        //字体的颜色
        this.mTextColor = mTextColor;
        if (mCirclePaint != null && mTestPaint != null) {
            mCirclePaint.setColor(mCircleColor);
            mTestPaint.setColor(mTextColor);
            requestLayout();
        }
    }

    /**
     * 滑块上面是否需要显示刻度线
     *
     * @param isShowTopOfThumb
     */
    public void setShowTopOfThumb(boolean isShowTopOfThumb) {
        this.isShowTopOfThumb = isShowTopOfThumb;
        requestLayout();
    }
}
