package com.example.parkit.Activities;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.example.parkit.R;


public class SplashActivity extends AppCompatActivity {
    private final int ANIMATION_DURATION = 2000;
    private LinearLayout splash_LAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splash_LAY = findViewById(R.id.splash_LAY);

        showView(splash_LAY);
    }

    private void startApp() {
        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void showView(final View view) {
        view.setScaleX(0.0f);
        view.setScaleY(0.0f);
        view.setAlpha(0.0f);

        // https://thoughtbot.com/blog/android-interpolators-a-visual-guide
        view.animate()
                .alpha(2.0f)
                .scaleY(2.0f)
                .scaleX(2.0f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        view.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        startApp();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) { }

                    @Override
                    public void onAnimationRepeat(Animator animator) { }
                });
    }
}
