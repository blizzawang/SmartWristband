package com.example.administrator.smartwristband;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;

import com.example.administrator.smartwristband.activity.MainActivity;
import com.example.administrator.smartwristband.utils.SpInfo;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.rl_root)
    RelativeLayout rlRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        //缩放
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        //渐变
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(3000);
        alphaAnimation.getFillAfter();

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(scaleAnimation);
        animationSet.addAnimation(alphaAnimation);
        rlRoot.startAnimation(animationSet);
        //动画的监听
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            private Intent intent;

            public void onAnimationStart(Animation animation) {

            }

            //动画结束，要跳转页面
            public void onAnimationEnd(Animation animation) {
                boolean isFirstEnter = SpInfo.getBoolean(SplashActivity.this, "is_first_enter", false);
                if (isFirstEnter) {
                    //第一次进入，就跳转到新手引导页面
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                } else {//如果不是第一次 ，就跳转到主页面
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                }
                startActivity(intent);
                finish();//结束当前页面

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
