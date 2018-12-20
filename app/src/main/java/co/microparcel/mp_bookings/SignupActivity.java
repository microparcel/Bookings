package co.microparcel.mp_bookings;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    private EditText signup_email_EditText, signup_password_EditText, signup_name_EditText, signup_mobile_EditText;
    private Button signup_Button;
    private TextView signup_gotologin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signup_email_EditText = findViewById(R.id.signup_email_EditText);
        signup_password_EditText = findViewById(R.id.signup_password_EditText);
        signup_name_EditText = findViewById(R.id.signup_name_EditText);
        signup_mobile_EditText = findViewById(R.id.signup_mobile_EditText);
        signup_Button = findViewById(R.id.signup_Button);
        signup_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSignup();
            }
        });
        signup_gotologin = findViewById(R.id.signup_gotologin);



    }

    private void callSignup() {
        String email = signup_email_EditText.getText().toString().trim();
        String password = signup_password_EditText.getText().toString().trim();
        final String name = signup_name_EditText.getText().toString().trim();
        final String mobile = signup_mobile_EditText.getText().toString().trim();
        if(TextUtils.isEmpty(email)){
            signup_email_EditText.setError("Please Enter Email!");
            return;
        }
        if(TextUtils.isEmpty(password)){
            signup_password_EditText.setError("Please Enter Password!");
            return;
        }
        if(TextUtils.isEmpty(name)){
            signup_name_EditText.setError("Please Enter Name!");
            return;
        }
        if(TextUtils.isEmpty(mobile)){
            signup_mobile_EditText.setError("Please Enter Mobile Number!");
            return;
        }
        if(password.length()<6){
            signup_password_EditText.setError("Minimum Password length Should be 6 Character!");
            return;
        }
        if(mobile.length()!=10){
            signup_mobile_EditText.setError("Please Enter Valid Mobile Number!");
            return;
        }

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    Toast.makeText(SignupActivity.this, "Creating user...", Toast.LENGTH_SHORT).show();
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    assert firebaseUser != null;
                    String username = firebaseUser.getUid();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("mp_cms").child("user_details");
                    UserData ud = new UserData(name, mobile);
                    userRef.child(username).setValue(ud);
                    Toast.makeText(SignupActivity.this, "User Created...", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(SignupActivity.this, "User not created...", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}
