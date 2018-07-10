package com.wangyang.infiniteshufflingviewpager.customview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 用于自动无限轮播的轮播图的ViewPager
 * 1.请求父控件及祖宗控件不要拦截事件；
 * 2.解决ScrollView和ViewPager的上下滑动冲突；
 * 3.当ViewPager填充的数据为1的时候，让其不能滑动；
 */
public class InfiniteShufflingViewPager extends ViewPager {
    private int startX;
    private int startY;
    private boolean isCanScroll = true;

    public InfiniteShufflingViewPager(Context context) {
        super(context);
    }

    public InfiniteShufflingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 控制ViewPager能否滑动
     * 默认可以滑动（true）
     *
     * @param isCanScroll ture可以滑动，false禁止滑动
     */
    public void setCanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    /**
     * 1.上下滑动需要拦截；
     * 2.向右滑动并且当前是第一个页面，需要拦截；
     * 3.向左滑动并且是最后一个页面，需要拦截；
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //先请求父控件及祖宗控件不要拦截事件
        getParent().requestDisallowInterceptTouchEvent(true);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) ev.getX();
                startY = (int) ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int endX = (int) ev.getX();
                int endY = (int) ev.getY();

                int dx = endX - startX;
                int dy = endY - startY;

                if (Math.abs(dx) > Math.abs(dy)) {
                    int currentItem = getCurrentItem();
                    //左右滑动
                    if (dx > 0) {//向右滑
                        if (currentItem == 0) {
                            //第一个页面，需要拦截
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    } else {//向左滑
                        //获取item总数
                        int count = getAdapter().getCount();
                        if (currentItem == count - 1) {
                            //最后一个页面，需要拦截
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    }
                } else {
                    // 上下滑动,需要拦截
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 当数据的大小为1的时候，禁止滑动
     *
     * @param x
     * @param y
     */
    @Override
    public void scrollTo(int x, int y) {
        if (isCanScroll) {
            super.scrollTo(x, y);
        }
    }
}
