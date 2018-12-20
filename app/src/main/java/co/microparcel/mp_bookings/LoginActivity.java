package co.microparcel.mp_bookings;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText client_email_EditText, client_password_EditText;
    Button client_login_Button;
    TextView create_new_ac_TextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        client_email_EditText = findViewById(R.id.client_email_EditText);
        client_password_EditText = findViewById(R.id.client_password_EditText);
        client_login_Button = findViewById(R.id.client_login_Button);
        client_login_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callLogin();
            }
        });
        create_new_ac_TextView = findViewById(R.id.create_new_ac_TextView);
        create_new_ac_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
            }
        });

    }

    private void callLogin() {
        String email = client_email_EditText.getText().toString().trim();
        String password = client_password_EditText.getText().toString().trim();
        if(TextUtils.isEmpty(email))
        {
            client_email_EditText.setError("Please Enter Valid Email!");
            return;
        }
        if(TextUtils.isEmpty(password))
        {
            client_password_EditText.setError("Please Enter Password");
            return;
        }
        if(password.length()<6)
        {
            client_password_EditText.setError("Minimum Password Length should be 6 character");
            return;
        }

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Authentication succesfull", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                } else {
                    Toast.makeText(LoginActivity.this, "Authentication Unsuccessfull", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
}
