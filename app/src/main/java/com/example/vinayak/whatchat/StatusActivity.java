package com.example.vinayak.whatchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextView mStatus;
    private Button mSavebtn;

    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = (Toolbar) findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        String prevStatus = bundle.getString("prevStatus");

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid=mCurrentUser.getUid().toString();

        mStatusDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);



        mStatus=(TextView)findViewById(R.id.status_input);
        mSavebtn=(Button)findViewById(R.id.status_save_btn);

        mStatus.setText(prevStatus);

        mSavebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgress=new ProgressDialog(getApplicationContext());
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we save the Changes");

                String status=mStatus.getText().toString();
                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgress.dismiss();
                            Intent intent_settings=new Intent(StatusActivity.this,SettingsActivity.class);
                            startActivity(intent_settings);
                        }else{
                            String task_result = task.getException().getMessage().toString();
                            Log.e("task Result:", task_result);
                            Toast.makeText(getApplicationContext(),"Erorr in Saving Changes",Toast.LENGTH_LONG);
                        }
                    }
                });


            }
        });
    }
}
