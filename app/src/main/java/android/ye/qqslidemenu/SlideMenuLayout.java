package android.ye.qqslidemenu;

import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.ye.qqslidemenu.utils.ColorUtil;

import com.nineoldandroids.view.ViewHelper;

/**
 * Created by ye on 2016/11/16.
 */
public class SlideMenuLayout extends FrameLayout {

    private ViewDragHelper viewDragHelper;
    private View menuView;
    private View mainView;
    private float dragRange;
    private int width;
    private FloatEvaluator floatEvaluator;
    private IntEvaluator intEvaluator;

    public SlideMenuLayout(Context context) {
        super(context);
        init();
    }

    public SlideMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SlideMenuLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    //定义状态常量
    public enum DragState{
        Open,Close
    }
    //默认状态为关闭
    private DragState mCurrentState = DragState.Close;



    private void init() {
        viewDragHelper = ViewDragHelper.create(this, callback);
        floatEvaluator = new FloatEvaluator();
        intEvaluator = new IntEvaluator();
    }
    //获取当前状态
    public DragState getCurrentState(){
        return mCurrentState;
    }


    /**
     * 当SlideMenuLayout的xml布局的结束标签被读取完成会执行该方法，
     * 此时会知道自己有几个子View了 一般用来初始化子View的引用
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //限制布局只有2个View，若不是则抛异常
        if (getChildCount() != 2){
            throw new IllegalArgumentException("Slidemenu only have 2 children");
        }
        menuView = getChildAt(0);
        mainView = getChildAt(1);

    }





    @Override
    public boolean onTouchEvent(MotionEvent event) {
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }


    /**
     * 该方法在onMeasure执行完之后执行，那么可以在该方法中初始化自己和子View的宽高
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getMeasuredWidth();
        //设定拖动的范围
        dragRange = 0.6f* width;
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {

        /**
         * 用于判断是否捕获当前child的触摸事件
         * child: 当前触摸的子View
         * return: true:就捕获并解析 false：不处理
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {

            return child == menuView || child == mainView;
        }

        /**
         *
         * @param child 表示ViewDragHelper认为你想让当前child的left改变的值
         * @param left 控制child在水平方向的移动
         * @param dx 本次child水平方向移动的距离
         * @return 表示你真正想让child的left变成的值
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mainView){
                //限制左边
                if (left<0){
                    left =0;
                }
                //限制右边
                if (left>dragRange){
                    left = (int) dragRange;
                }
            }
            return left;
        }

        /**
         *
         * @param changedView
         * @param left
         * @param top 控制child在垂直方向的移动
         * @param dx
         * @param dy 本次child垂直方向移动的距离
         * 表示ViewDragHelper认为你想让当前child的top改变的值,top=chile.getTop()+dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == menuView){
                //固定menuView
                menuView.layout(0,0,menuView.getMeasuredWidth(),menuView.getMeasuredHeight());
                //让mainView能移动
                int newLeft = mainView.getLeft()+dx;
                //限制左边
                if (newLeft<0){
                    newLeft =0;
                }
                //限制右边
                if (newLeft>dragRange){
                    newLeft = (int) dragRange;
                }
                mainView.layout(newLeft,mainView.getTop()+dy,newLeft+mainView.getMeasuredWidth(),mainView.getBottom()+dy);
            }

            //计算滑动的百分比
            float fraction = mainView.getLeft()/dragRange;
            //根据移动的百分比执行伴随动画
            executeAnim(fraction);
            //判断状态
            if (fraction == 0 && mCurrentState != DragState.Close) {
                //更改状态为关闭，并回调关闭的方法
                mCurrentState = DragState.Close;
                if (listener != null) {
                    listener.onClose();
                }
            }else if (fraction == 1f && mCurrentState != DragState.Open){
                //更改状态为打开，并回调打开的方法
                    mCurrentState = DragState.Open;
                    if (listener != null){
                        listener.onOpen();
                    }
            }
            //将drag的fraction暴露给外界
            if (listener != null){
                listener.onDraging(fraction);
            }
        };

        /**
         *手指抬起的执行该方法
         * @param releasedChild 当前抬起的view
         * @param xvel x轴的移动速率
         * @param yvel y轴的移动速率
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
           if (mainView.getLeft()<dragRange/2){
               close();
           }else {
               open();
           }
            //处理用户的稍微滑动
            if (xvel>200 && mCurrentState != DragState.Open){
                open();
            }else if (xvel<-200 && mCurrentState != DragState.Close){
                close();
            }

        }

    };

    /**
     * h执行伴随动画
     * @param fraction 移动的百分比
     */
    private void executeAnim(float fraction) {
        //设置缩放，ViewHelper引用自nineoldandroids类库
        ViewHelper.setScaleX(mainView, floatEvaluator.evaluate(fraction, 1f, 0.8f));
        ViewHelper.setScaleY(mainView, floatEvaluator.evaluate(fraction, 1f, 0.8f));
        //移动menuView,需要int值
        ViewHelper.setTranslationX(menuView, intEvaluator.evaluate(fraction, -menuView.getMeasuredWidth() / 2, 0));
        //放大menuView
        ViewHelper.setScaleX(menuView,floatEvaluator.evaluate(fraction,0.5f,1f));
        ViewHelper.setScaleY(menuView, floatEvaluator.evaluate(fraction, 0.5f, 1f));

        //改变透明度
        ViewHelper.setAlpha(menuView,floatEvaluator.evaluate(fraction,0.3f,1f));

        //给SlideMenu的背景添加黑色的遮罩效果,SRC_OVER指覆盖在上面
        getBackground().setColorFilter((Integer) ColorUtil.evaluateColor(fraction, Color.BLACK,Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(SlideMenuLayout.this);
        }
    }


    /**
     * 打开菜单
     */
    private void open() {
        viewDragHelper.smoothSlideViewTo(mainView, (int) dragRange,mainView.getTop());
        ViewCompat.postInvalidateOnAnimation(SlideMenuLayout.this);
    }

    /**
     * 关闭菜单
     */
    public void close() {
        viewDragHelper.smoothSlideViewTo(mainView,0,mainView.getTop());
        ViewCompat.postInvalidateOnAnimation(SlideMenuLayout.this);

    }

    //建立状态改变的回调监听
    private OnDragStateChangeListener listener;
    public  void setOnDragStateChangeListener(OnDragStateChangeListener listener){
        this.listener = listener;
    }

    //回调接口
    public interface OnDragStateChangeListener {
        //打开回调
        void onOpen();
        //关闭回调
        void onClose();
        //正在拖拽中的回调
        void onDraging(float fraction);
    }
}
