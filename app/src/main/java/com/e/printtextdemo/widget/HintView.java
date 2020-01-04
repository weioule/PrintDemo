package com.e.printtextdemo.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.e.printtextdemo.R;
import com.e.printtextdemo.utils.Utility;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by weioule
 * on 2019/5/7.
 */
public class HintView extends RelativeLayout {
    private final String TAG = getClass().getSimpleName();
    private Context mContent;
    private boolean first = true;
    private int offsetX, offsetY;
    private int radius;
    private int outsideSpace = 5;
    private View targetView;
    private HashMap<View, Integer> map;
    private View textGuideView;
    private View customGuideView;
    private Paint mCirclePaint;
    private Paint mBackgroundPaint;
    private int[] center;
    private PorterDuffXfermode porterDuffXfermode;
    private Bitmap bitmap;
    private int backgroundColor;
    private Canvas temp;
    private Direction direction;
    private MyShape myShape, outsideShape;
    private boolean dotted = true;
    private int[] location;
    private OnClickCallback onclickListener;
    private boolean cancelable;
    private int screenWeight, screenHeight;


    public HintView(Context context) {
        super(context);
        this.mContent = context;

        screenWeight = Utility.getScreenWidth(mContent);
        screenHeight = Utility.getScreenHeight(mContent);
    }

    public int[] getLocation() {
        return location;
    }

    public void setLocation(int[] location) {
        this.location = location;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setOutsideSpace(int outsideSpace) {
        this.outsideSpace = outsideSpace;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setShape(MyShape shape) {
        this.myShape = shape;
    }

    public void setOutsideShape(MyShape outsideShape) {
        this.outsideShape = outsideShape;
    }

    public void setDotted(boolean dotted) {
        this.dotted = dotted;
    }

    public void setBgColor(int background_color) {
        this.backgroundColor = background_color;
    }

    public void setTargetView(View targetView) {
        this.targetView = targetView;
    }

    public void setMoreTransparentView(HashMap<View, Integer> map) {
        this.map = map;
    }

    public int[] getCenter() {
        return center;
    }

    public void setCenter(int[] center) {
        this.center = center;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public void setCustomGuideView(View customGuideView) {
        this.customGuideView = customGuideView;
        if (!first) {
            restoreState();
        }
    }

    public void setTextGuideView(View textGuideView) {
        this.textGuideView = textGuideView;
        if (!first) {
            restoreState();
        }
    }

    public void setOnclickListener(OnClickCallback onclickListener) {
        this.onclickListener = onclickListener;
    }

    private void setClickInfo() {
        if (cancelable) {
            customGuideView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                    if (onclickListener != null) {
                        onclickListener.onClickedGuideView();
                    }

                }
            });
        } else {
            customGuideView.findViewById(R.id.tv_know).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onclickListener != null) {
                        onclickListener.onClickedGuideView();
                    }
                    hide();
                }
            });
        }
    }

    public void show() {
        if (targetView == null)
            return;

        //盖在整个屏幕上的
        switch (direction) {
            case ABOVE:
                RelativeLayout.LayoutParams guideViewParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                if (null != customGuideView) {
                    this.removeAllViews();
                    this.addView(customGuideView, guideViewParams);
                }
                break;
            default:
                addFuzzyLayerView();
                break;
        }
        this.setBackgroundResource(R.color.transparent);
        this.bringToFront();  //设置在最上层
        ((FrameLayout) ((Activity) mContent).getWindow().getDecorView()).addView(this);
        first = false;
    }

    public void hide() {
        if (customGuideView != null || textGuideView != null) {
            this.removeAllViews();
            ((FrameLayout) ((Activity) mContent).getWindow().getDecorView()).removeView(this);
            restoreState();
        }
    }

    //文字图片和我知道啦图片一起放
    private void addFuzzyLayerView() {
        if (targetView.getWidth() <= 0) {
            hide();
            return;
        }
        // 获取targetView的中心坐标
        if (center == null) {
            // 获取右上角坐标
            location = new int[2];
            targetView.getLocationInWindow(location);
            center = new int[2];
            // 获取中心坐标
            center[0] = location[0] + targetView.getWidth() / 2;
            center[1] = location[1] + targetView.getHeight() / 2;
        }

        RelativeLayout.LayoutParams guideViewParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        if (direction != null) {
            int left = center[0] - targetView.getWidth() / 2;
            int top = center[1] - targetView.getHeight() / 2;
            int right = center[0] + targetView.getWidth() / 2;
            int bottom = center[1] + targetView.getHeight() / 2;

            Rect rect = new Rect();
            rect.left = left;
            rect.top = top;
            rect.right = right;
            rect.bottom = bottom;

            View img = customGuideView.findViewById(R.id.hint_img);
            RelativeLayout.LayoutParams params = (LayoutParams) img.getLayoutParams();

            switch (direction) {
                case TOP:
                case RIGHT_TOP:
                    params.setMargins(left + offsetX, 0, 0, screenHeight - top + offsetY);
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    break;
                case BOTTOM:
                case RIGHT_BOTTOM:
                    params.setMargins(left + offsetX, bottom + offsetY, 0, 0);
                    break;
                case LEFT:
                    params.setMargins(0, top + offsetY, screenWeight - left - offsetX, 0);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    break;
                case RIGHT:
                    params.setMargins(right + offsetX, top + offsetY, 0, 0);
                    break;
                case LEFT_TOP:
                    params.setMargins(0, 0, screenWeight - left - offsetX, screenHeight - top + offsetY);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    break;
                case LEFT_BOTTOM:
                    params.setMargins(0, bottom + offsetY, screenWeight - left - offsetX, 0);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    break;
            }

            img.setLayoutParams(params);

            if (null != customGuideView) {
                this.removeAllViews();
                this.addView(customGuideView, guideViewParams);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null != targetView && targetView.getWidth() > 0)
            drawBackground(canvas);
    }

    //作为箭头锚点的抠图区域
    private void drawBackground(Canvas canvas) {
        Log.v(TAG, "drawBackground");
        // 先绘制bitmap，再将bitmap绘制到屏幕
        bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        temp = new Canvas(bitmap);

        // 背景画笔
        Paint bgPaint = new Paint();
        if (backgroundColor != 0) {
            bgPaint.setColor(backgroundColor);
        } else {
            bgPaint.setColor(getResources().getColor(R.color.transparent_hint));
        }
        // 绘制屏幕背景
        temp.drawRect(0, 0, temp.getWidth(), temp.getHeight(), bgPaint);

        // targetView 的透明圆形画笔
        if (mCirclePaint == null) {
            mCirclePaint = new Paint();
        }

        //透明效果
        porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);    //SRC_OUT或者CLEAR都可以
        mCirclePaint.setXfermode(porterDuffXfermode);
        mCirclePaint.setAntiAlias(true);

        // targetView 的外虚线矩形 画笔
        if (mBackgroundPaint == null) {
            mBackgroundPaint = new Paint();
        }

        mBackgroundPaint.setAntiAlias(true);
        if (dotted)
            mBackgroundPaint.setPathEffect(new DashPathEffect(new float[]{6, 5}, 0)); //设置虚线效果
        mBackgroundPaint.setColor(getResources().getColor(R.color.white));
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setDither(true);

        if (myShape != null) {
            RectF rectF = new RectF();
            RectF oval = new RectF();
            switch (myShape) {
                case CIRCULAR://圆形
                    temp.drawCircle(center[0], center[1], radius, mCirclePaint);
                    if (outsideShape == MyShape.CIRCULAR)
                        temp.drawCircle(center[0], center[1], radius + outsideSpace, mBackgroundPaint);

                    break;
                case RECTANGULAR://圆角矩形
                    oval.left = location[0];
                    oval.top = center[1] - targetView.getHeight() / 2;
                    oval.right = location[0] + targetView.getWidth();
                    oval.bottom = center[1] + targetView.getHeight() / 2;

                    temp.drawRoundRect(oval, radius, radius, mCirclePaint);

                    rectF.left = oval.left - outsideSpace;
                    rectF.top = oval.top - outsideSpace;
                    rectF.right = oval.right + outsideSpace;
                    rectF.bottom = oval.bottom + outsideSpace;

                    //画外虚线矩形
                    if (outsideShape == MyShape.RECTANGULAR)
                        temp.drawRoundRect(rectF, radius, radius, mBackgroundPaint);

                    if (outsideShape == MyShape.OVAL)
                        temp.drawOval(rectF, mBackgroundPaint);

                    break;
            }

            //抠更多图位
            if (null != map && map.size() >= 0) {
                for (Map.Entry<View, Integer> entry : map.entrySet()) {
                    View view = entry.getKey();
                    Integer radius = entry.getValue();
                    if (view.getWidth() > 0 && view.getHeight() > 0) {
                        int[] location = new int[2];
                        view.getLocationInWindow(location);
                        int[] center = new int[2];
                        // 获取中心坐标
                        center[0] = location[0] + view.getWidth() / 2;
                        center[1] = location[1] + view.getHeight() / 2;

                        oval.left = location[0];
                        oval.top = center[1] - view.getHeight() / 2;
                        oval.right = location[0] + view.getWidth();
                        oval.bottom = center[1] + view.getHeight() / 2;

                        temp.drawRoundRect(oval, radius, radius, mCirclePaint);
                    }
                }
            }
        }

        // 绘制到屏幕
        canvas.drawBitmap(bitmap, 0, 0, bgPaint);
        bitmap.recycle();
    }

    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }


    /**
     * 定义GuideView相对于targetView的方位，
     */
    public enum Direction {
        LEFT, TOP, RIGHT, BOTTOM,
        LEFT_TOP, LEFT_BOTTOM,
        RIGHT_TOP, RIGHT_BOTTOM, ABOVE
    }

    /**
     * 定义目标控件的形状。圆形，矩形，椭圆形
     */
    public enum MyShape {
        CIRCULAR, RECTANGULAR, OVAL
    }

    /**
     * GuideView点击Callback
     */
    public interface OnClickCallback {
        void onClickedGuideView();
    }

    public static class Builder {
        static HintView hintView;
        static Builder instance = new Builder();
        Context mContext;

        private Builder() {
        }

        public Builder(Context ctx) {
            mContext = ctx;
        }

        public static Builder newInstance(Context ctx) {
            hintView = new HintView(ctx);
            return instance;
        }

        /**
         * 设置目标view
         */
        public Builder setTargetView(View target) {
            hintView.setTargetView(target);
            return instance;
        }

        /**
         * 设置更多需要抠出来的view
         */
        public Builder setMoreTransparentView(HashMap<View, Integer> map) {
            hintView.setMoreTransparentView(map);
            return instance;
        }

        /**
         * 设置蒙层颜色
         */
        public Builder setBgColor(int color) {
            hintView.setBgColor(color);
            return instance;
        }

        /**
         * 设置文字和图片View 在目标view的位置
         */
        public Builder setDirction(Direction dir) {
            hintView.setDirection(dir);
            return instance;
        }

        /**
         * 设置绘制形状
         */
        public Builder setShape(MyShape shape) {
            hintView.setShape(shape);
            return instance;
        }

        /**
         * 设置绘制外层形状的虚线或实线
         */
        public Builder setOutsideShape(MyShape outsideShape) {
            hintView.setOutsideShape(outsideShape);
            return instance;
        }

        /**
         * 设置绘制外层形状
         */
        public Builder setDotted(boolean dotted) {
            hintView.setDotted(dotted);
            return instance;
        }

        public Builder setRadius(int radius) {
            hintView.setRadius(radius);
            return instance;
        }

        public Builder setOutsideSpace(int outsideSpace) {
            hintView.setOutsideSpace(outsideSpace);
            return instance;
        }

        /**
         * 设置文字图片
         */
        public Builder setTextGuideView(View view) {
            hintView.setTextGuideView(view);
            return instance;
        }

        /**
         * 设置"我知道啦"图片
         */
        public Builder setCustomGuideView(View view) {
            hintView.setCustomGuideView(view);
            return instance;
        }

        /**
         * 设置图片的偏移量
         */
        public Builder setOffset(int x, int y) {
            hintView.setOffsetX(x);
            hintView.setOffsetY(y);
            return instance;
        }

        /**
         * 点击监听
         */
        public Builder setOnclickListener(final OnClickCallback callback) {
            hintView.setOnclickListener(callback);
            return instance;
        }

        public HintView build() {
            hintView.setClickInfo();
            return hintView;
        }

        public Builder setCancelable(boolean cancelable) {
            hintView.setCancelable(cancelable);
            return instance;
        }
    }

    public void restoreState() {
        offsetX = offsetY = 0;
        radius = 0;
        mCirclePaint = null;
        mBackgroundPaint = null;
        center = null;
        porterDuffXfermode = null;
        bitmap = null;
        temp = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}