package dk.itu.moapd.gocaching.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import dk.itu.moapd.gocaching.R;

/* Source: https://stackoverflow.com/questions/5486789/how-do-i-make-a-splash-screen?page=1&tab=votes#tab-top*/
public class SplashActivity extends Activity {

    Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.splash_screen);

        Thread thread = new Thread(){
            public void run(){
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent in = new Intent(ctx,LoginActivity.class);
                startActivity(in);
                finish();
            }
        };
        thread.start();
    }
}