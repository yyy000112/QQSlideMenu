package android.ye.qqslidemenu;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;


/**
 * 当slideMenu打开的时候，拦截并消费掉触摸事件
 * Created by ye on 2016/11/16.
 */
public class MyLinearLayout extends LinearLayout {
    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private SlideMenuLayout slideMenuLayout;
    public void setSlideMenuLayout(SlideMenuLayout slideMenuLayout){
        this.slideMenuLayout = slideMenuLayout;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(slideMenuLayout!=null && slideMenuLayout.getCurrentState()== SlideMenuLayout.DragState.Open){
            //拦截该事件
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (slideMenuLayout!=null && slideMenuLayout.getCurrentState() == SlideMenuLayout.DragState.Open){
            if (event.getAction() == MotionEvent.ACTION_UP){
                //抬起则应该关闭slideMenu
                slideMenuLayout.close();

            }
            //如果slideMenu打开则应该拦截并消费掉事件
            return true;

        }
        return super.onTouchEvent(event);
    }
}
