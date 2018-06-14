package com.example.vinayak.whatchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.Objects;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;

    private Button change_status_btn;
    private Button change_img_btn;

    private static final int GALLERY_PICK=1;

    private StorageReference mImageStorage;

    private String UID;
    private String PICTURE_URL;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayImage=(CircleImageView)findViewById(R.id.settings_image);
        mName=(TextView)findViewById(R.id.settings_display_name);
        mStatus=(TextView)findViewById(R.id.settings_status);

        change_status_btn=(Button)findViewById(R.id.settings_status_btn);
        change_img_btn=(Button)findViewById(R.id.settings_image_btn);

        mImageStorage= FirebaseStorage.getInstance().getReference();

        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();



        change_status_btn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   Intent status_intent=new Intent(SettingsActivity.this,StatusActivity.class);
                   status_intent.putExtra("prevStatus", mStatus.getText().toString());
                   startActivity(status_intent);
               }
           });

        change_img_btn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {

                    Intent gallery_intent=new Intent();
                    gallery_intent.setType("image/*");
                    gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(gallery_intent,"Select Image"),GALLERY_PICK);

                  // start picker to get image for cropping and then use the image in cropping activity
                  /*CropImage.activity()
                          .setGuidelines(CropImageView.Guidelines.ON)
                          .start(SettingsActivity.this);*/
          }
          });

        mDisplayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent View_image_intent=new Intent(SettingsActivity.this,ViewImageActivity.class);
                View_image_intent.putExtra("ImageURL", PICTURE_URL);
                startActivity(View_image_intent);
            }
        });

        String current_uid = mCurrentUser.getUid();
        UID=current_uid;

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                final String image=dataSnapshot.child("image").getValue().toString();
                String thumb_image=dataSnapshot.child("thumb_image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
                PICTURE_URL=image;

                if(!image.equals("defaut") ){
                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.default_avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image)
                            .placeholder(R.drawable.default_avatar).into(mDisplayImage);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){
            Uri imageUri=data.getData();

            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(SettingsActivity.this);
            //Toast.makeText(SettingsActivity.this,imageUri,Toast.LENGTH_LONG).show();
        }
//
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressDialog=new ProgressDialog(SettingsActivity.this);
                mProgressDialog.setTitle("Uploading Image...");
                mProgressDialog.setMessage("Please Wait while we process and upload your Picture");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                Uri resultUri = result.getUri();
                File thumb_filePath=new File(resultUri.getPath());

                Bitmap thumb_Bitmap=null;/*Since its Initialization is done inside try block...
                it gives error after using it outside try-catch block*/


                try {
                    thumb_Bitmap = new Compressor(this)
                    .setMaxWidth(200)
                    .setMaxHeight(200)
                    .setQuality(75)
                    .compressToBitmap(thumb_filePath);


                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_Bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference filePath=mImageStorage.child("profile_images").child(UID+".jpg");
                final StorageReference thumb_fileFirebasePath=mImageStorage.child("profile_images").child("thumbs").child(UID+".jpg");

                final StorageReference storageRef = FirebaseStorage.getInstance().getReference();



                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            UploadTask.TaskSnapshot t=task.getResult();

                            final String download_url=null;
                            String image_path=task.getResult().getMetadata().getPath();
                            storageRef.child("profile_images/" + UID + ".jpg").getDownloadUrl().
                                    addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            Map update_hashMap=new HashMap();
                                            //update_hashMap.put("image",download_url);
                                            update_hashMap.put("image",uri.toString() );


                                            mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        //Toast.makeText(SettingsActivity.this, "Success!", Toast.LENGTH_LONG);
                                                    }
                                                }
                                            });
                                        }
                                    });

                            UploadTask uploadTask = thumb_fileFirebasePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> thumb_task) {
                                    String thumb_downloadUrl=thumb_task.getResult().getMetadata().getPath();

                                    storageRef.child("profile_images/thumbs/"+UID+".jpg").getDownloadUrl().
                                        addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            if(thumb_task.isSuccessful()){

                                                Map update_hashMap=new HashMap();
                                                //update_hashMap.put("image",download_url);
                                                update_hashMap.put("thumb_image",uri.toString() );


                                                mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            mProgressDialog.dismiss();
                                                            //Toast.makeText(SettingsActivity.this, "Success!", Toast.LENGTH_LONG);
                                                        }
                                                    }
                                                });
                                            }else{
                                                String task_result = thumb_task.getException().getMessage().toString();
                                                Toast.makeText(SettingsActivity.this,task_result,Toast.LENGTH_LONG);
                                                Log.e("error", task_result);

                                            }
                                        }
                                    });


                                }
                            });


                        }else{
                            String task_result = task.getException().getMessage().toString();
                            Toast.makeText(SettingsActivity.this,task_result,Toast.LENGTH_LONG);
                            Log.e("error", task_result);
                            mProgressDialog.dismiss();
                        }
                    }
                });


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("else-part of resultCode", "not working!" );
            }
        }

    }
}
