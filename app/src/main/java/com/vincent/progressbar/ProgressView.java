package com.vincent.progressbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;

/**
 * @author : wangwenbo
 * @date : 2021/12/31
 * Desc :
 */
public class ProgressView extends FrameLayout {
    private int mDialogWid = -1;
    private int mDialogHeight = -1;
    int mPercent = 0; // 百分比
    private TextView mPercentTv;
    private RoundCornerImageView mProgressIv;
    private AppCompatImageView iv_person;
    private ImageView mBotIv;

    public ProgressView(@NonNull Context context) {
        this(context, null);
    }

    public ProgressView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View view = inflate(context, R.layout.view_round_corner_progress, this);
        initView(view);
    }

    private void initView(View view) {
        mPercentTv = view.findViewById(R.id.percent_tv);
        mProgressIv = view.findViewById(R.id.p_cover_iv);
        iv_person = view.findViewById(R.id.iv_person);
        mBotIv = view.findViewById(R.id.p_bot_iv);
        mProgressIv.setRadiusDp(5);

        Glide.with(getContext()).
                load(R.drawable.ic_person_gif).centerCrop().into(iv_person);

    }


    public void updatePercent(int percent) {
        mPercent = percent;
        mPercentTv.setText(percent + "%");
        float percentFloat = mPercent / 100.0f;
        final int ivWidth = mBotIv.getWidth();
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mProgressIv.getLayoutParams();
        ConstraintLayout.LayoutParams iv_personLayoutParams = (ConstraintLayout.LayoutParams) iv_person.getLayoutParams();

        int marginEnd = (int) ((1 - percentFloat) * ivWidth);
        lp.width = ivWidth - marginEnd;

        iv_personLayoutParams.leftMargin = ivWidth - marginEnd - iv_person.getWidth();

        mProgressIv.setLayoutParams(lp);
        mProgressIv.postInvalidate();

        iv_person.setLayoutParams(iv_personLayoutParams);
        iv_person.postInvalidate();
    }


}
