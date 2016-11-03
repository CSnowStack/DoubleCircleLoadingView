package csnowstack.doublecircleloadingview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by cqll on 2016/11/2.
 */

public class DoubleCircleLoadingView extends View {
    private static final float C = 0.551915024494f;
    private Paint mPaint;
    private int mRadiusBig=120,mRadiusSmall= (int) (mRadiusBig/2f),mWidth,mHeight, mMimWidth=(int) (mRadiusSmall*2*3)/*fill view mim width*/;
    private float mFraction=0,mFractionDegree=0/*degree*/,mLength, mDistanceBezier;
    private Path mPathCircle,mPathBezier;
    private ValueAnimator mValueAnimator;
    private float[] mPointData =new float[8];//4个数据点  顺时针排序，从左边开始
    private float[] mPointCtrl =new float[16];//8个控制点
    private float[] mPos=new float[2];
    private PathMeasure mPathMeasure;
    public DoubleCircleLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint=new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(0xFF7C191E);
        mPaint.setAntiAlias(true);


        mPathCircle =new Path();
        mPathBezier =new Path();


        mPathMeasure=new PathMeasure();

        mValueAnimator= ValueAnimator.ofFloat(0,1,0);
        mValueAnimator.setDuration(2000);
        mValueAnimator.setRepeatCount(Integer.MAX_VALUE);
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFraction= (float) animation.getAnimatedValue();
                mFractionDegree=animation.getAnimatedFraction();
                invalidate();
            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth= MeasureSpec.getSize(widthMeasureSpec);
        mHeight= MeasureSpec.getSize(heightMeasureSpec);
        int widthMode= MeasureSpec.getMode(widthMeasureSpec);
        int heightMode= MeasureSpec.getMode(heightMeasureSpec);
        if(widthMode!= MeasureSpec.AT_MOST&&heightMode!= MeasureSpec.AT_MOST){
            if(mWidth<mMimWidth)
                mWidth=mMimWidth;
            if(mHeight<mMimWidth)
                mHeight=mMimWidth;

        }else if(widthMeasureSpec!= MeasureSpec.AT_MOST){
            if(mWidth<mMimWidth)
                mWidth=mMimWidth;
        }else if(heightMeasureSpec!= MeasureSpec.AT_MOST){
            if(mHeight<mMimWidth)
                mHeight=mMimWidth;
        }
        setMeasuredDimension(mWidth,mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mWidth / 2, mHeight / 2);
        canvas.scale(1, -1);
        canvas.rotate(-360 * mFractionDegree);

        setDoubleCirClePath();
        canvas.drawPath(mPathCircle,mPaint);

        if(mFraction<(1/3f)){//缩小大圆
            setCirclePath();
            canvas.drawPath(mPathCircle,mPaint);
        }else if(mFraction<3/4f){//画贝塞尔曲线
            setBezierPath();
            canvas.drawPath(mPathBezier,mPaint);
        }else {//画分离
            setLastBezierPath();
            canvas.drawPath(mPathBezier,mPaint);
        }
    }

    // all
    private void setDoubleCirClePath() {
        mPathCircle.reset();
        if(mFraction<(1/3f)){
            mPathCircle.addCircle(-mRadiusSmall/2f*mFraction*3,0,mRadiusSmall, Path.Direction.CW);
            mPathCircle.addCircle(mRadiusSmall/2f*mFraction*3,0,mRadiusSmall, Path.Direction.CW);
        }else {
            float distance=(mFraction-1/3f)/(2/3f)*(mRadiusSmall*2+mRadiusSmall/2f);
            mPathCircle.addCircle(-mRadiusSmall/2f-distance,0,mRadiusSmall, Path.Direction.CW);
            mPathCircle.addCircle(mRadiusSmall/2f+distance,0,mRadiusSmall, Path.Direction.CW);
        }
    }

    // mFraction 0 ~ 1/3
    private void setCirclePath() {
        mPointData[0]=-mRadiusBig+mRadiusSmall/2f*mFraction*3f;
        mPointData[1]=0;

        mPointData[2]=0;
        mPointData[3]=mRadiusBig-mRadiusBig/2f*mFraction*3f;//0到1 的三分之一 用来给大圆做效果;

        mPointData[4]=mRadiusBig-mRadiusSmall/2f*mFraction*3f;
        mPointData[5]=0;

        mPointData[6]=mPointData[2];
        mPointData[7]=-mPointData[3];


        mPointCtrl[0]=mPointData[0];//x轴一样
        mPointCtrl[1]=mRadiusBig*C;//y轴向下的

        mPointCtrl[2]=mPointData[2]-mRadiusBig*C;
        mPointCtrl[3]=mPointData[3];//y轴一样

        mPointCtrl[4]=mPointData[2]+mRadiusBig*C;
        mPointCtrl[5]=mPointData[3];

        mPointCtrl[6]=mPointData[4];
        mPointCtrl[7]=mPointCtrl[1];

        mPointCtrl[8]=mPointData[4];
        mPointCtrl[9]=-mPointCtrl[1];

        mPointCtrl[10]=mPointCtrl[4];
        mPointCtrl[11]=mPointData[7];

        mPointCtrl[12]=mPointCtrl[2];
        mPointCtrl[13]=mPointData[7];

        mPointCtrl[14]=mPointData[0];
        mPointCtrl[15]=-mPointCtrl[1];

        mPathCircle.reset();
        mPathCircle.moveTo(mPointData[0],mPointData[1]);
        mPathCircle.cubicTo(mPointCtrl[0],mPointCtrl[1],mPointCtrl[2],mPointCtrl[3],mPointData[2],mPointData[3]);
        mPathCircle.cubicTo(mPointCtrl[4],mPointCtrl[5],mPointCtrl[6],mPointCtrl[7],mPointData[4],mPointData[5]);
        mPathCircle.cubicTo(mPointCtrl[8],mPointCtrl[9],mPointCtrl[10],mPointCtrl[11],mPointData[6],mPointData[7]);
        mPathCircle.cubicTo(mPointCtrl[12],mPointCtrl[13],mPointCtrl[14],mPointCtrl[15],mPointData[0],mPointData[1]);
    }


    //1/3 ~ 3/4
    private void setBezierPath() {
        mPathBezier.reset();

        float distance =(2*mRadiusSmall+mRadiusSmall/2f)*mFraction;
        float topY=mRadiusSmall*(1-0.6f*mFraction);
        float distanceBezier=topY-distance*C*(0.5f+0.5f*mFraction);
        if(mDistanceBezier!=0&&distanceBezier<(mDistanceBezier)){
            distanceBezier=mDistanceBezier;
        }
        mPathBezier.moveTo(-distance,topY);
        mPathBezier.cubicTo(-distance,distanceBezier,distance,distanceBezier,distance,topY);
        if(mDistanceBezier==0){

            mPathMeasure.setPath(mPathBezier,false);
            mLength=mPathMeasure.getLength();
            mPathMeasure.getPosTan(mLength/2,mPos,null);
            if(mPos[1]<=8){
                mDistanceBezier=distanceBezier;
                mPathBezier.reset();
                mPathBezier.moveTo(-distance,topY);
                mPathBezier.cubicTo(-distance,mDistanceBezier,distance,mDistanceBezier,distance,topY);
                mPathBezier.lineTo(distance,-topY);
                mPathBezier.cubicTo(distance,-mDistanceBezier,-distance,-mDistanceBezier,-distance,-topY);
                mPathBezier.close();
                return;
            }
        }

        mPathBezier.lineTo(distance,-topY);
        mPathBezier.cubicTo(distance,-distanceBezier,-distance,-distanceBezier,-distance,-topY);
        mPathBezier.close();

    }

    // 3/4 ~ 1
    private void setLastBezierPath() {
        float x=-mRadiusSmall/2f-(mFraction-1/3f)/(2/3f)*(mRadiusSmall*2+mRadiusSmall/2f);
        mPathBezier.reset();

        mPathBezier.moveTo(x,mRadiusSmall);
        mPathBezier.quadTo(x,0,x+mRadiusSmall+mRadiusSmall*(4-mFraction*4),0);
        mPathBezier.quadTo(x,0,x,-mRadiusSmall);
        mPathBezier.lineTo(x,mRadiusSmall);

        mPathBezier.moveTo(-x,mRadiusSmall);
        mPathBezier.quadTo(-x,0,-x-mRadiusSmall-mRadiusSmall*(4-mFraction*4),0);
        mPathBezier.quadTo(-x,0,-x,-mRadiusSmall);
        mPathBezier.lineTo(-x,mRadiusSmall);

        mPathBezier.close();
    }



    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(!mValueAnimator.isRunning())
            mValueAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mValueAnimator.isRunning())
            mValueAnimator.cancel();
    }
}
