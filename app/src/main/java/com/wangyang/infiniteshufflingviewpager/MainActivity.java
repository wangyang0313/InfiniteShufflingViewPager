package com.wangyang.infiniteshufflingviewpager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wangyang.infiniteshufflingviewpager.customview.InfiniteShufflingViewPager;
import com.wangyang.infiniteshufflingviewpager.util.DensityUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Android 自动无限轮播的轮播图（通过InfiniteShufflingViewPager解决冲突），
 * 主要功能包括：通过Handler实现自动无限轮播、小圆点背景选择器、InfiniteShufflingViewPager解决冲突、
 *              轮播图数量为1时禁止滑动、Adapter的特殊处理等。
 */
public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.vp)
    InfiniteShufflingViewPager vp;
    @InjectView(R.id.ll_point_container)
    LinearLayout llPointContainer;
    private int[] img = {R.drawable.infinite_shuffling_0, R.drawable.infinite_shuffling_1,
            R.drawable.infinite_shuffling_2};
    private int previousSelectedPosition;
    private long startTime;
    private long endTime;
    private Handler mHandler;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        context = this;

        initPoint();
        initViewPager();
    }

    /**
     * 初始化小圆点
     */
    private void initPoint() {
        if (img != null && img.length > 0) {
            for (int i = 0; i < img.length; i++) {
                //创建指示器（小白点）
                View pointView = new View(context);
                //设置背景选择器
                pointView.setBackgroundResource(R.drawable.selector_bg_point);
                //设置宽高
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DensityUtil.dip2px(5, context), DensityUtil.dip2px(5, context));
                //设置左外间距（除了第一个）
                if (i != 0) {
                    params.leftMargin = DensityUtil.dip2px(10, context);
                }

                llPointContainer.addView(pointView, params);
                //设置都不可用(默认是可用的，即亮的)
                pointView.setEnabled(false);
            }
        }
    }

    /**
     * 初始化ViewPager
     */
    private void initViewPager() {
        //异常处理
        if (img == null || img.length <= 0) {
            return;
        }

        //设置数据适配器
        vp.setAdapter(infiniteShufflingViewPagerAdapter);
        if (img.length > 1) {
            if (llPointContainer.getChildCount() > 0) {
                //设置到某个位置，使左右都可无限滑动
                vp.setCurrentItem(50000 + llPointContainer.getChildCount() - 50000 % llPointContainer.getChildCount());
                //设置第一个圆点默认是可用的
                llPointContainer.getChildAt(0).setEnabled(true);
                previousSelectedPosition = 0;
            }

            vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    int newPosition = position % img.length;

                    //指示器：将之前的禁用，把最新的启用，更新指示器
                    llPointContainer.getChildAt(newPosition).setEnabled(true);
                    llPointContainer.getChildAt(previousSelectedPosition).setEnabled(false);

                    //记录之前的位置和时间
                    previousSelectedPosition = newPosition;
                    startTime = System.currentTimeMillis();
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

            //通过Handler使ViewPager无限轮播
            if (mHandler == null) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        endTime = System.currentTimeMillis();

                        if (endTime - startTime >= 2000) {
                            vp.setCurrentItem(vp.getCurrentItem() + 1);
                            //继续发送延时3秒的消息，形成内循环
                            mHandler.sendEmptyMessageDelayed(0, 3000);
                        }
                    }
                };

                new Thread() {
                    @Override
                    public void run() {
                        //启动自动轮播逻辑（保证只执行一次）
                        mHandler.sendEmptyMessageDelayed(0, 3000);
                    }
                }.start();
            }

            //当手指按住viewpager时，停止自动轮播；
            //当手指松开viewpager时，开启自动轮播；
            vp.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //停止广告自动轮播，删除handler的所有消息
                            mHandler.removeCallbacksAndMessages(null);
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            //启动广告
                            mHandler.sendEmptyMessageDelayed(0, 3000);
                            break;
                        case MotionEvent.ACTION_UP:
                            //启动广告
                            mHandler.sendEmptyMessageDelayed(0, 3000);
                            break;
                    }
                    return false;
                }
            });
        } else {
            //当轮播图数量为1时，禁止ViewPager滑动
            vp.setCanScroll(false);
        }
    }

    /**
     * 无限轮播的ViewPager的adapter
     * 1.将数量设置为Integer.MAX_VALUE；
     * 2.重新计算position（int newPosition = position % img.length;）
     */
    PagerAdapter infiniteShufflingViewPagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            //因为要无限轮循播放
            final int newPosition = position % img.length;

            ImageView imageView = new ImageView(context);
            imageView.setImageResource(img[newPosition]);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    };
}
