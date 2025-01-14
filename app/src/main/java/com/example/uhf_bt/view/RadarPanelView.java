package com.example.uhf_bt.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.rscja.deviceapi.entity.RadarLocationEntity;

import java.util.List;

/**
 * 标签画板
 */
public class RadarPanelView extends View {
    private final String TAG = "RadarPanelView";
    private final Context mContext;
    private int viewSize; // 面板控件尺寸（长宽相等,且取长宽中的最小值）
    private int labelRadius; // 标签圆点半径

    private Paint mPaintYellow;
    private Paint mPaintBlue;
    private Paint mPaintWhite;
    private Paint mPaintBlack;

    private List<RadarLocationEntity> pointList;
    private String targetEpc = "";   // 目标EPC

    public RadarPanelView(Context context) {
        super(context);
        mContext = context;
        initPaint();
    }

    public RadarPanelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getSize(widthMeasureSpec);
        int height = getSize(heightMeasureSpec);
        viewSize = Math.min(width, height);
        labelRadius = viewSize / 40;
        mPaintWhite.setStrokeWidth((float) (viewSize / 200.0));

        setMeasuredDimension(viewSize, viewSize);
    }

    private int getSize(int measureSpec) {
        int mySize = 100;   // 默认值100
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        if (mode == MeasureSpec.UNSPECIFIED) mySize = 100;
        else if (mode == MeasureSpec.AT_MOST) mySize = size;
        else if (mode == MeasureSpec.EXACTLY) mySize = size;

        return mySize;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (pointList != null) {
            float r = (float) (viewSize / 2.0);
            canvas.translate(r, r);

            // 第一次遍历，绘制蓝色的圆圈
            for (int i = 0; i < pointList.size(); i++) {
                RadarLocationEntity info = pointList.get(i);
                canvas.save();
                canvas.rotate(info.getAngle());
                float distance = (float) (info.getValue() / 100.0 * r);
                canvas.drawCircle(0, distance - r + 10, labelRadius, mPaintBlue);
                canvas.drawCircle(0, distance - r + 10, labelRadius, mPaintWhite);
                canvas.restore();
            }

            // 第二次遍历，绘制黄色的圆圈
            for (int i = 0; i < pointList.size(); i++) {
                RadarLocationEntity info = pointList.get(i);
                if (!targetEpc.equals("") && info.getTag().equals(targetEpc)) {
                    canvas.save();
                    canvas.rotate(info.getAngle());
                    float distance = (float) (info.getValue() / 100.0 * r);
                    canvas.drawCircle(0, distance - r + 10, labelRadius, mPaintYellow);
                    canvas.drawCircle(0, distance - r + 10, labelRadius, mPaintWhite);
                    canvas.restore();
                }
            }
        }

        super.onDraw(canvas);
    }


    private void initPaint() {
        setBackgroundColor(Color.TRANSPARENT);

        //宽度=5，抗锯齿，描边效果的黄色画笔
        mPaintYellow = new Paint();
        mPaintYellow.setStrokeWidth(5);
        mPaintYellow.setAntiAlias(true);
        mPaintYellow.setStyle(Paint.Style.FILL);
        mPaintYellow.setColor(0xFFFFD700);

        //宽度=5，抗锯齿，描边效果的蓝色画笔
        mPaintBlue = new Paint();
        mPaintBlue.setStrokeWidth(5);
        mPaintBlue.setAntiAlias(true);
        mPaintBlue.setStyle(Paint.Style.FILL);
        mPaintBlue.setColor(0xFF0083FF);

        mPaintBlack = new Paint();
        mPaintBlack.setStrokeWidth(5);
        mPaintBlack.setAntiAlias(true);
        mPaintBlack.setStyle(Paint.Style.FILL);
        mPaintBlack.setColor(Color.BLUE);

        mPaintWhite = new Paint();
        //宽度=4，抗锯齿，线条模式的白色画笔
        mPaintWhite.setColor(Color.WHITE);
        mPaintWhite.setAntiAlias(true);
        mPaintWhite.setStyle(Paint.Style.STROKE);
        mPaintWhite.setStrokeWidth(4);

    }


    /**
     * 绑定数据
     *
     * @param pointList 标签集合
     * @param targetEpc 目标标签
     */
    public void bindingData(List<RadarLocationEntity> pointList, String targetEpc) {
        this.pointList = pointList;
        this.targetEpc = targetEpc;
        this.invalidate();
    }

    /**
     * 清空画布
     */
    public void clearPanel() {
        if (pointList != null) pointList.clear();
        this.invalidate();
    }

}
