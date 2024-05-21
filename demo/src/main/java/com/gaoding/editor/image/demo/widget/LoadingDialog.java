package com.gaoding.editor.image.demo.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gaoding.editor.image.demo.R;
import com.gaoding.editor.image.demo.utils.DisplayUtil;


/**
 * loading控件(转圈)
 * 1. 支持超时显示取消按钮（默认5秒显示，可自定义）
 * created by bajiao
 */
public class LoadingDialog extends Dialog {

    /**
     * 取消动画时间
     */
    private static final int DURATION = 300;

    /**
     * loading 取消按钮延迟出现时间
     */
    private static int mDelayTime = 5000;

    private final Context mContext;
    /**
     * 提示文字
     */
    private String mText;

    /**
     * 内容
     */
    private RelativeLayout mRlContent;

    /**
     * 提示文字控件
     */
    private TextView mTextView;

    /**
     * 圆角背景中间变化部分
     */
    private View mBg;

    /**
     * 取消按钮
     */
    private RelativeLayout mRlCancel;

    /**
     * contentview
     */
    private View mContentView;

    /**
     * 旋转视图
     */
    private MaterialProgressBar mProgressbarView;

    /**
     * 是否显示取消按钮
     */
    private boolean mShowCancel = false;

    /**
     * handler
     */
    private final Handler mHandler;
    /**
     * 显示取消按钮任务
     */
    private final Runnable mShowCancelRunnable = () -> {
        setCancelable(true);
        showCancel();
    };

    public LoadingDialog(Context context, String text) {
        this(context, R.style.GDLoadingDialogStyle, text);
    }

    public LoadingDialog(Context context, int textId) {
        this(context, R.style.GDLoadingDialogStyle, context.getString(textId));
    }

    public LoadingDialog(Context context, int themeResId, String text) {
        super(context, themeResId);
        this.mContext = context.getApplicationContext();
        this.mText = text;
        mHandler = new Handler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentView = getLayoutInflater().inflate(R.layout.layout_loading_new, null);
        this.setContentView(mContentView);
        this.setCanceledOnTouchOutside(false);
        this.setCancelable(false);

        initView();

        mProgressbarView.setVisibility(View.VISIBLE);
        //设置进度颜色 宽度
        int size = DisplayUtil.dip2px(getContext(), 4);
        mProgressbarView.setBarWidth(size);
        mProgressbarView.setRimWidth(size);
        mProgressbarView.setBarColor(mContext.getResources().getColor(R.color.gd_white));
        mProgressbarView.setRimColor(mContext.getResources().getColor(R.color.gd_white20));

        if (!TextUtils.isEmpty(mText)) {
            mTextView.setText(mText);
        } else {
            //默认内容
            mTextView.setText(R.string.gd_loading);
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mRlContent = findViewById(R.id.gd_content);
        mTextView = findViewById(R.id.gd_tv_text);
        mBg = findViewById(R.id.gd_bg);
        mProgressbarView = findViewById(R.id.gd_progressbar);
        mRlCancel = findViewById(R.id.gd_rl_cancel);
        mRlCancel.setVisibility(View.GONE);
        resetViewSize();
    }

    /**
     * 根据不同类型重置布局内控件宽高
     */
    private void resetViewSize() {
        //loading
        int contentHeight = DisplayUtil.dip2px(mContext, 172);
        int bgContentHeight = DisplayUtil.dip2px(mContext, 120);
        int textMargin = DisplayUtil.dip2px(mContext, 20);
        RelativeLayout.LayoutParams contentParams = (RelativeLayout.LayoutParams) mRlContent.getLayoutParams();
        LinearLayout.LayoutParams bgParams = (LinearLayout.LayoutParams) mBg.getLayoutParams();
        LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams) mTextView.getLayoutParams();
        contentParams.height = contentHeight;
        mRlContent.setLayoutParams(contentParams);
        bgParams.height = bgContentHeight;
        mBg.setLayoutParams(bgParams);
        textParams.topMargin = textMargin;
        mTextView.setLayoutParams(textParams);
    }

    public String getText() {
        return mText;
    }

    public void setText(String mText) {
        this.mText = mText;
        if (mTextView != null) {
            if (TextUtils.isEmpty(mText)) {
                mTextView.setVisibility(View.GONE);
            } else {
                mTextView.setVisibility(View.VISIBLE);
                mTextView.setText(mText);
            }
        }
    }

    /**
     * 设置是否显示取消按钮（默认不显示）
     *
     * @param showCancel 是否显示取消按钮
     */
    public void setShowCancel(boolean showCancel) {
        this.mShowCancel = showCancel;
    }

    /**
     * 设置取消按钮显示的延时时间
     *
     * @param timeMs 取消按钮显示的延时毫秒数
     */
    public void setDelayTime(int timeMs) {
        mDelayTime = timeMs;
    }

    @Override
    public void dismiss() {
        try {
            super.dismiss();
            mRlCancel.setVisibility(View.GONE);
            mHandler.removeCallbacks(mShowCancelRunnable);
        } catch (Exception e) {
            //防御 当要对dialog操作的同时 当前activity被销毁导致的崩溃 崩溃不用处理任何事情
            e.printStackTrace();
        }
    }

    /**
     * 延迟关闭
     *
     * @param ms 延时关闭的毫秒数
     */
    public void dismissDelayed(long ms) {
        mContentView.postDelayed(this::dismiss, ms);
    }

    @Override
    public void show() {
        try {
            super.show();
            if (mShowCancel) {
                int delayTime = mDelayTime;
                mHandler.postDelayed(mShowCancelRunnable, delayTime);
            }
        } catch (Exception e) {
            //防御 当要对dialog操作的同时 当前activity被销毁导致的崩溃 崩溃不用处理任何事情
            e.printStackTrace();
        }
    }

    /**
     * 显示取消按钮
     */
    private void showCancel() {
        final float scale = (mContentView.getHeight() - DisplayUtil.dip2px(getContext(), 12)) * 1.0f / mBg.getHeight();
        final int height = mBg.getHeight();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1.0f, scale);
        valueAnimator.setDuration(DURATION);
        valueAnimator.setTarget(mBg);
        valueAnimator.setInterpolator(new AccelerateInterpolator());
        valueAnimator.addUpdateListener(valueAnimator1 -> {
            float value = (float) valueAnimator1.getAnimatedValue();
            ViewGroup.LayoutParams lp = mBg.getLayoutParams();
            lp.height = (int) (height * value);
            mBg.setLayoutParams(lp);
            float alpha = (value - 1) / (scale - 1);
            // 避免还没展开到下面就显示出来了
            if (alpha > 0.5f) {
                mRlCancel.setAlpha(value);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mRlCancel.setVisibility(View.VISIBLE);
                mRlCancel.setAlpha(0);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mRlCancel.setOnClickListener(view -> cancel());
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        valueAnimator.start();
    }

}
