package com.example.vinayak.whatchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private Button login_btn;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private Toolbar mToolbar;


    private ProgressDialog mLoginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Login");

        mLoginProgress=new ProgressDialog(this);


        email= findViewById(R.id.email_editText);
        password= findViewById(R.id.password_editText);
        login_btn= findViewById(R.id.login_btn);

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email_text=email.getText().toString();
                String password_text=password.getText().toString();
                if(!TextUtils.isEmpty(email_text) && !TextUtils.isEmpty(password_text) ){
                    mLoginProgress.setTitle("Loggin In");
                    mLoginProgress.setMessage("Please wait while we check your Credentials");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();

                    loginUser(email_text,password_text);
                }
            }
        });

    }

    private void loginUser(String email_text, String password_text) {
        mAuth.signInWithEmailAndPassword(email_text, password_text)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mLoginProgress.dismiss();
                            String current_user_id=mAuth.getCurrentUser().getUid();
                            String deviceToken= FirebaseInstanceId.getInstance().getToken();
                            Log.e("LoginActivity","Main activity about to be started");
                            mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
                            mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);

                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            /*Once logged in successfully, clear all the previous tasks so that just the back button doesn't
                             log you out */


                                        startActivity(mainIntent);

                                        finish();
                                    }
                                });


                        } else {
                            mLoginProgress.hide();
                            String task_result = task.getException().getMessage().toString();

                            Toast.makeText(LoginActivity.this, "Error : " + task_result, Toast.LENGTH_LONG).show();


                        }

                        // ...
                    }
                });
    }

}
