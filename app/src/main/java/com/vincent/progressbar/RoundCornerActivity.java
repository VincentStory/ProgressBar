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
    private HorizontalProgressBar progressBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_round_corner);
        initUI();
    }

    private void initUI() {
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_pause).setOnClickListener(this);
        findViewById(R.id.btn_resume).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        progressBar = findViewById(R.id.bar_progress);




    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:

                progressBar.startAnimation();
                break;

                case R.id.btn_pause:

                progressBar.pauseAnimation();
                break;

                case R.id.btn_resume:

                progressBar.resumeAnimation();
                break;
                case R.id.btn_cancel:

                progressBar.cancelAnimation();
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
                    }
                });
            }
        }
    }

}
