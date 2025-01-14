package com.example.uhf_bt.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.IntDef;

public class RadarBackgroundView extends View {
    private final String TAG = "RadarView";
    private Paint mLinePaint;
    private Paint mCirclePaint;
    private Paint mCirclePaint_Blue;
    private Paint mSectorPaint;
    private Paint mText1Paint;
    private Paint mText2Paint;
    private Shader mShader;
    private Matrix matrix;

    public final static int CLOCK_WISE = 1;
    public final static int ANTI_CLOCK_WISE = -1;

    public boolean isStart = false;
    private int viewSize;   // 控件尺寸
    private StartAngle startAngle = new StartAngle(0);  // 旋转效果起始角度
    private float textLen = 0;
    private int len30;      //角度30刻度线条长度
    private int len5;       //角度5刻度线条长度

    private int marginWidth = 0;
    private int marginHeight = 0;

    @IntDef({CLOCK_WISE, ANTI_CLOCK_WISE})
    public @interface RADAR_DIRECTION {
    }

    //默认为顺时针呢
    private final static int DEFAULT_DIERCTION = CLOCK_WISE;

    //设定雷达扫描方向
    private int direction = DEFAULT_DIERCTION;

    private boolean threadRunning = true;

    public RadarBackgroundView(Context context) {
        super(context);
        initPaint();
    }

    public RadarBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }


    private void initPaint() {
        setBackgroundColor(Color.TRANSPARENT);

        //宽度=5，抗锯齿，描边效果的白色画笔
        mLinePaint = new Paint();
        mLinePaint.setStrokeWidth(5);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(Color.WHITE);

        //宽度=5，抗锯齿，描边效果的浅绿色画笔
        mCirclePaint = new Paint();
        mCirclePaint.setStrokeWidth(5);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(0x99000000);

        //宽度=5，抗锯齿，描边效果的浅蓝色画笔
        mCirclePaint_Blue = new Paint();
        mCirclePaint_Blue.setStrokeWidth(5);
        mCirclePaint_Blue.setAntiAlias(true);
        mCirclePaint_Blue.setStyle(Paint.Style.FILL);
        mCirclePaint_Blue.setColor(0xFF92B4F4);

        //暗绿色的画笔
        mSectorPaint = new Paint();
        mSectorPaint.setColor(0x9D00ff00);
        mSectorPaint.setAntiAlias(true);
//        mShader = new SweepGradient(viewSize / 2, viewSize / 2, Color.TRANSPARENT, Color.GREEN);
//        mPaintSector.setShader(mShader);

        // 方位（N、E、S、W）画笔
        mText1Paint = new Paint();
        mText1Paint.setColor(Color.WHITE);
        mText1Paint.setAntiAlias(true);
        mText1Paint.setTextAlign(Paint.Align.CENTER);
        mText1Paint.setTextSize(40);

        // 数字画笔
        mText2Paint = new Paint();
        mText2Paint.setColor(Color.WHITE);
        mText2Paint.setAntiAlias(true);
        mText2Paint.setTextAlign(Paint.Align.CENTER);
        mText2Paint.setTextSize(30);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getSize(widthMeasureSpec);
        int height = getSize(heightMeasureSpec);
        viewSize = Math.min(width, height);
        marginWidth = (width - viewSize) / 2;
        marginHeight = (height - viewSize) / 2;
        textLen = mText1Paint.measureText("W");
        len30 = viewSize / 20;
        len5 = viewSize / 40;
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


    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        float r = (float) (viewSize / 2.0);
        canvas.drawCircle(r, r, viewSize / 2, mCirclePaint);
        canvas.drawCircle(r, r, viewSize / 2, mLinePaint);
        canvas.drawCircle(r, r, viewSize / 3, mLinePaint);
        canvas.drawCircle(r, r, viewSize / 6, mLinePaint);

        //绘制两条十字线
        canvas.drawLine(r, len30 + textLen, r, viewSize - len30 - textLen, mLinePaint);
        canvas.drawLine(len30 + textLen, r, viewSize - len30 - textLen, r, mLinePaint);

        canvas.drawCircle(r, r, viewSize / 6, mCirclePaint_Blue);

        // 绘制刻度
        canvas.save();
        canvas.translate(r, r);
        for (int i = 0; i < 360; i++) {
            if (i % 30 == 0) {
                canvas.drawLine(0, -r, 0, -r + len30, mLinePaint);
//                if (i % 90 != 0) {
                    canvas.drawText(String.valueOf(i), 0, -r + len30 + mText1Paint.measureText("0"), mText2Paint);
//                }
            } else if (i % 5 == 0) {
                canvas.drawLine(0, -r, 0, -r + len5, mLinePaint);
            }
            canvas.rotate(1);
        }

//        canvas.drawText("0", 0, -r + len30 + textLen - 4, mText1Paint);
//        canvas.rotate(90);
//        canvas.drawText("90", 0, -r + len30 + textLen - 4, mText1Paint);
//        canvas.rotate(90);
//        canvas.drawText("180", 0, -r + len30 + textLen - 4, mText1Paint);
//        canvas.rotate(90);
//        canvas.drawText("270", 0, -r + len30 + textLen - 4, mText1Paint);
        canvas.restore();

        mShader = new SweepGradient(r, r, Color.TRANSPARENT, Color.GREEN);
        mSectorPaint.setShader(mShader); //
        matrix = new Matrix();  // 根据matrix中设定角度，不断绘制shader,呈现出一种扇形扫描效果
        matrix.preRotate(direction * startAngle.angle, r, r);   // 设定旋转角度,制定进行转转操作的圆心
        canvas.concat(matrix);
        canvas.drawCircle(r, r, r, mSectorPaint);
        super.onDraw(canvas);
    }


    public void setDirection(@RADAR_DIRECTION int direction) {
        if (direction != CLOCK_WISE && direction != ANTI_CLOCK_WISE) {
            throw new IllegalArgumentException("Use @RADAR_DIRECTION constants only!");
        }
        this.direction = direction;
    }

    public void setStartAngle(StartAngle startAngle) {
        this.startAngle = startAngle;
        this.invalidate();
    }

    public void start() {
        ScanThread mThread = new ScanThread(this);
        mThread.setName("radar");
        mThread.start();
        threadRunning = true;
        isStart = true;
    }

    public void stop() {
        if (isStart) {
            threadRunning = false;
            isStart = false;
        }
    }

    public static class StartAngle {  //封装成对象，使引用者也能检测到其变化，用于状态保存
        public int angle;

        public StartAngle(int angle) {
            this.angle = angle;
        }
    }

    protected class ScanThread extends Thread {

        private final RadarBackgroundView view;

        public ScanThread(RadarBackgroundView view) {
            this.view = view;
        }

        @Override
        public void run() {
            while (threadRunning) {
                if (isStart) {
                    view.post(new Runnable() {
                        public void run() {
                            startAngle.angle = (startAngle.angle + 1) % 360;
//                            matrix = new Matrix();
//                            //设定旋转角度,制定进行转转操作的圆心
//                            // matrix.postRotate(start, viewSize / 2, viewSize / 2);
//                            // matrix.setRotate(start,viewSize/2,viewSize/2);
//                            float r = (float) (viewSize / 2.0);
//                            matrix.preRotate(direction * startAngle.angle, r, r);
                            view.invalidate();
                        }
                    });
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
