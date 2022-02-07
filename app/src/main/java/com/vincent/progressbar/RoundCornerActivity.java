package com.vincent.progressbar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


/**
 * 圆角图片示例
 * Created by Rust on 2018/5/23.
 */
public class RoundCornerActivity extends AppCompatActivity implements View.OnClickListener {

    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private ProgressThread mProgressThread;
    private ProgressView progress_view;
    private HorizontalProgressBar progressBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_round_corner);
        initUI();
    }

    private void initUI() {
        findViewById(R.id.pop_dialog_btn).setOnClickListener(this);
        progress_view = findViewById(R.id.progress_view);
        progressBar = findViewById(R.id.bar_progress);


        progressBar.setDrawableIds(
                new int[]{R.drawable.img_1,
                        R.drawable.img_2,
                        R.drawable.img_3,
                        R.drawable.img_4,
                        R.drawable.img_5,
                        R.drawable.img_6,
                        R.drawable.img_7,
                        R.drawable.img_8,
                        R.drawable.img_9,
                        R.drawable.img_10}
        );

        progressBar.startAnimation();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pop_dialog_btn:
//                mProgressThread = new ProgressThread();
//                mProgressThread.start();

                progressBar.startAnimation();

                break;
        }
    }

    private class ProgressThread extends Thread {

        private int progress = 0;

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                progress++;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                if (progress > 100) {
                    progress = 0;
                }
                final int p = progress;
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
//                        mRoundCornerProgressDialog.updatePercent(p);
                        progress_view.updatePercent(p);
                    }
                });
            }
        }
    }

}
