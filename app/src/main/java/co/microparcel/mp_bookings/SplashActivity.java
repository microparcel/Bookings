package co.microparcel.mp_bookings;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private static int SPLASH_TIME_OUT = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseAuth = FirebaseAuth.getInstance();
                if(firebaseAuth.getCurrentUser() !=null){
                    // instance profile activity here
                    finish();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }

                else {
                    finish();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            }

        }, SPLASH_TIME_OUT);


    }
}
