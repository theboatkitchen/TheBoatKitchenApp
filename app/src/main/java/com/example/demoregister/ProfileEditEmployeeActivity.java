package com.example.demoregister;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demoregister.model.ImageModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProfileEditEmployeeActivity extends AppCompatActivity {

    private ImageButton backBtn;
    private EditText nameTxt,emailTxt,phoneTxt,ageTxt;
    private Button updateBtn;
    private ImageView imageView;
    private ProgressDialog progressDialog;

    Uri uri;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateprofile_staff);

        nameTxt = findViewById(R.id.name);
        ageTxt = findViewById(R.id.age);
        phoneTxt = findViewById(R.id.phone);
        //emailTxt = findViewById(R.id.email);
        imageView = findViewById(R.id.profileIcon);

        updateBtn = findViewById(R.id.editProfileBtn);
        backBtn = findViewById(R.id.backBtn);

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        //init ui views
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //begin update profile
                if(uri!=null){
                    updateImage(uri);
                }
                else{
                    inputData();
                }


            }
        });


    }

    private String nameS,ageS,phoneS,emailS,image2;
    private void inputData() {
        //input data
        nameS = nameTxt.getText().toString().trim();
        ageS = ageTxt.getText().toString().trim();
        phoneS = phoneTxt.getText().toString().trim();
        //emailS = emailTxt.getText().toString().trim();

        boolean pattern = phoneTxt.length()>=10&&phoneTxt.length()<=11;

        if(TextUtils.isEmpty(nameS)) {
            Toast.makeText(getApplicationContext(), "Please enter your Full Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(ageS)){
            Toast.makeText(getApplicationContext(),"Age cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(phoneS)){
            Toast.makeText(getApplicationContext(),"Phone Number cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }
        if(pattern == false){
            Toast.makeText(getApplicationContext(),"Please insert correct phone Number", Toast.LENGTH_SHORT).show();
            return;
        }
        /*
        if(TextUtils.isEmpty(emailS)){
            Toast.makeText(getApplicationContext(),"Email cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(emailS).matches()){
            Toast.makeText(this,"Invalid email pattern...", Toast.LENGTH_SHORT).show();
            return;
        }
        */
        updateProfile();

    }

    //without image
    private void updateProfile() {
        progressDialog.setMessage("Updating Profile...");
        progressDialog.show();

        //setup data to update
        HashMap<String, Object> hashMap =  new HashMap<>();
        hashMap.put("staffName",""+nameS);
        hashMap.put("staffAge",""+ageS);
        hashMap.put("staffPhone",""+phoneS);
       // hashMap.put("staffEmail",""+emailS);

        //update to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //updated
                        progressDialog.dismiss();
                        Toast.makeText(ProfileEditEmployeeActivity.this, "Profile Updated...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to update
                        Toast.makeText(ProfileEditEmployeeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void updateImage(Uri uri) {

        //ambik image file dari firebase storage
        StorageReference file = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(uri));
        file.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri){
                        //tukarkan image dari storage ke string nak store ke realtime db
                        ImageModel imageTxt = new ImageModel(uri.toString());
                        //process ambik url image dari imageTxt ke String image nak masuk dlam constructor nnti

                        //---- 1) Input Data
                        image2 = imageTxt.getImageurl();
                        //input data
                        nameS = nameTxt.getText().toString().trim();
                        ageS = ageTxt.getText().toString().trim();
                        phoneS = phoneTxt.getText().toString().trim();
                        //emailS = emailTxt.getText().toString().trim();

                        boolean pattern = phoneTxt.length()>=10&&phoneTxt.length()<=11;

                        if(TextUtils.isEmpty(nameS)) {
                            Toast.makeText(getApplicationContext(), "Please enter your Full Name", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(TextUtils.isEmpty(ageS)){
                            Toast.makeText(getApplicationContext(),"Age cannot be blank", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(TextUtils.isEmpty(phoneS)){
                            Toast.makeText(getApplicationContext(),"Phone Number cannot be blank", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(pattern == false){
                            Toast.makeText(getApplicationContext(),"Please insert correct phone Number", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        /*
                        if(TextUtils.isEmpty(emailS)){
                            Toast.makeText(getApplicationContext(),"Email cannot be blank", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(!Patterns.EMAIL_ADDRESS.matcher(emailS).matches()){
                            Toast.makeText(ProfileEditStaffActivity.this,"Invalid email pattern...", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        */

                        progressDialog.setMessage("Updating Profile...");
                        progressDialog.show();

                        //setup data to update
                        HashMap<String, Object> hashMap =  new HashMap<>();
                        hashMap.put("staffName",""+nameS);
                        hashMap.put("staffAge",""+ageS);
                        hashMap.put("staffPhone",""+phoneS);
                       // hashMap.put("staffEmail",""+emailS);
                        hashMap.put("staffImage",""+image2);

                        //update to db
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //updated
                                        progressDialog.dismiss();
                                        Toast.makeText(ProfileEditEmployeeActivity.this, "Profile Updated...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed to update
                                        Toast.makeText(ProfileEditEmployeeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileEditEmployeeActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
        else{
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        //load user info, and set to views
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("userid").equalTo(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String accountType = ""+ds.child("accountType").getValue();
                            String name = ""+ds.child("staffName").getValue();
                            String age = ""+ds.child("staffAge").getValue();
                            String phone = ""+ds.child("staffPhone").getValue();
                            String email = ""+ds.child("staffEmail").getValue();
                            String image = ""+ds.child("staffImage").getValue();

                            nameTxt.setText(name);
                            ageTxt.setText(age);
                            phoneTxt.setText(phone);
                            //emailTxt.setText(email);

                            try {
                                Picasso.get().load(image).placeholder(R.drawable.ic_baseline_person_24).into(imageView);
                            }
                            catch (Exception e){
                                imageView.setImageResource(R.drawable.ic_baseline_person_24);
                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //untuk dptkan image dari storage
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap map=MimeTypeMap.getSingleton();
        return  map.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //nak setkan url untuk image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==2 && resultCode==RESULT_OK && data!= null){
            uri=data.getData();
            imageView.setImageURI(uri);
        }
    }

}
