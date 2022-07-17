package com.example.demoregister;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demoregister.admin.RegisterStaffActivity;
import com.example.demoregister.admin.gender;
import com.example.demoregister.model.RegisterActivityJava;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    EditText custName,custGender,custAge,custPhone,custEmail,custPassword,conpassword,custIC;
    FirebaseAuth mAuth;
    FirebaseUser user;

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

    private ImageButton backBtn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        custName = (EditText) findViewById(R.id.BKname);
        custIC = (EditText)findViewById(R.id.BKIC);
        custGender = (EditText) findViewById(R.id.BKgender);
        custAge = (EditText)findViewById(R.id.BKage);
        custPhone = (EditText)findViewById(R.id.BKphone);
        custEmail = (EditText)findViewById(R.id.BKemail);
        custPassword = (EditText)findViewById(R.id.BKpassword);
        conpassword = (EditText)findViewById(R.id.Conpassword);
        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener((v) -> {onBackPressed();});
        custGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick gender type
                genderDialog();
            }

            private void genderDialog() {
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
                builder.setTitle("Gender Type")
                        .setItems(gender.genderType, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                String pickcategory = gender.genderType[which];

                                //set picked category
                                custGender.setText(pickcategory);
                            }
                        }).show();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    String custid,nameTxt,genderTxt,ageTxt,phoneTxt,emailTxt,passwordTxt,conpasswordTxt,IcTxt, accountType, online, custImage;
    public void createUser(View v){


        nameTxt = custName.getText().toString();
        IcTxt = custIC.getText().toString();
        genderTxt = custGender.getText().toString();
        ageTxt = custAge.getText().toString();
        phoneTxt = custPhone.getText().toString();
        emailTxt = custEmail.getText().toString();
        passwordTxt = custPassword.getText().toString();
        conpasswordTxt = conpassword.getText().toString();

        boolean pattern = phoneTxt.length()>=10&&phoneTxt.length()<=11;

        if(TextUtils.isEmpty(nameTxt)) {
            Toast.makeText(getApplicationContext(), "Please enter your Full Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(IcTxt)) {
            Toast.makeText(getApplicationContext(), "Please enter IC Number", Toast.LENGTH_SHORT).show();
            return;
        }
        if(IcTxt.length()<12){
            Toast.makeText(getApplicationContext(),"Please input correct IC number without '-'", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(genderTxt)){
            Toast.makeText(getApplicationContext(),"Gender cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(ageTxt)){
            Toast.makeText(getApplicationContext(),"Age cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(phoneTxt)){
            Toast.makeText(getApplicationContext(),"Phone Number cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        if(pattern == false){
            Toast.makeText(getApplicationContext(),"Please insert correct phone Number", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(emailTxt)){
            Toast.makeText(getApplicationContext(),"Email cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()){
            Toast.makeText(this,"Invalid email pattern...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(passwordTxt.length()<6){
            Toast.makeText(getApplicationContext(),"Password must be at least 6 character long...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!passwordTxt.equals(conpasswordTxt)){
            Toast.makeText(getApplicationContext(),"Password doesn't match...", Toast.LENGTH_SHORT).show();
            return;
        }

        addAccount();

    }

    private void addAccount() {

        progressDialog.setMessage("Creating Account..");
        progressDialog.show();

        //code utk create akaun guna email dgn password
        mAuth.createUserWithEmailAndPassword(emailTxt,passwordTxt)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account created
                        saveFirebaseData();

                    }

                    private void saveFirebaseData() {
                        progressDialog.setMessage("Saving Account Info..");

                        accountType = "customer";
                        custImage = "null";
                        online = "false";

                        user = mAuth.getCurrentUser();
                        custid = user.getUid();

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        //enter user data into database
                        RegisterActivityJava custDetails = new RegisterActivityJava(custid,nameTxt,genderTxt,ageTxt,phoneTxt,emailTxt,passwordTxt,IcTxt, accountType, online, custImage);



                        ref.child(custid).setValue(custDetails)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //db updated
                                        progressDialog.dismiss();
                                        startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed updating db
                                        progressDialog.dismiss();
                                        startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
                                        finish();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Failed creating account
                        progressDialog.dismiss();
                        Toast.makeText(RegistrationActivity.this,"" +e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }





    public void login(View view) {
        startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
    }
}
