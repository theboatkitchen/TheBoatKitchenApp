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

import com.example.demoregister.Filter.Constants;
import com.example.demoregister.Filter.Gender;
import com.example.demoregister.model.RegisterEmployeeModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterStaffActivity extends AppCompatActivity {

    EditText accountTypeTv,staffName,staffGender,staffAge,staffPhone,staffEmail,staffPassword,conpassword,staffIC;
    FirebaseAuth mAuth;
    FirebaseUser user;

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

    private ImageButton backBtn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_staff);

        staffName = (EditText) findViewById(R.id.BKname);
        staffIC = (EditText)findViewById(R.id.BKIC);
        staffGender = (EditText) findViewById(R.id.BKgender);
        staffAge = (EditText)findViewById(R.id.BKage);
        staffPhone = (EditText)findViewById(R.id.BKphone);
        staffEmail = (EditText)findViewById(R.id.BKemail);
        staffPassword = (EditText)findViewById(R.id.BKpassword);
        conpassword = (EditText)findViewById(R.id.Conpassword);
        backBtn = findViewById(R.id.backBtn);
        accountTypeTv = findViewById(R.id.accountType);

        backBtn.setOnClickListener((v) -> {onBackPressed();});

        //pick gender
        staffGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick gender type
                genderDialog();
            }

            private void genderDialog() {
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterStaffActivity.this);
                builder.setTitle("Gender Type")
                        .setItems(Gender.genderType, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                String pickcategory = Gender.genderType[which];

                                //set picked category
                                staffGender.setText(pickcategory);
                            }
                        }).show();
            }
        });

        //pick accountType

        accountTypeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick gender type
                AccountTypeDialog();
            }

            private void AccountTypeDialog() {
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterStaffActivity.this);
                builder.setTitle("Account Type")
                        .setItems(Constants.accountType, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                String pickcategory = Constants.accountType[which];

                                //set picked category
                                accountTypeTv.setText(pickcategory);
                            }
                        }).show();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);


    }

    String accTypeTxt,staffid,nameTxt,genderTxt,ageTxt,phoneTxt,emailTxt,passwordTxt,ConpasswordTxt,IcTxt, accountType, online, staffImage;
    public void createUser(View v){

       nameTxt = staffName.getText().toString();
       IcTxt = staffIC.getText().toString();
       genderTxt = staffGender.getText().toString();
       ageTxt = staffAge.getText().toString();
       phoneTxt = staffPhone.getText().toString();
       emailTxt = staffEmail.getText().toString();
       passwordTxt = staffPassword.getText().toString();
       ConpasswordTxt = conpassword.getText().toString();
       accountType = accountTypeTv.getText().toString();

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
        if(TextUtils.isEmpty(accountType)){
            Toast.makeText(getApplicationContext(),"Account Type cannot be blank", Toast.LENGTH_SHORT).show();
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
        if(!passwordTxt.equals(ConpasswordTxt)){
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

                        //accountType = "Staff";
                        staffImage = "null";
                        online = "false";

                        user = mAuth.getCurrentUser();
                        staffid = user.getUid();

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        //enter user data into database
                        RegisterEmployeeModel staffDetails = new RegisterEmployeeModel(staffid,nameTxt,genderTxt,ageTxt,phoneTxt,emailTxt,passwordTxt,IcTxt, accountType, online, staffImage);


                        ref.child(staffid).setValue(staffDetails)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //db updated
                                        progressDialog.dismiss();
                                        startActivity(new Intent(RegisterStaffActivity.this, MainAdminActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed updating db
                                        progressDialog.dismiss();
                                        startActivity(new Intent(RegisterStaffActivity.this, LoginActivity.class));
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
                        Toast.makeText(RegisterStaffActivity.this,"" +e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    public void Login_Staff(View view) {
        startActivity(new Intent(RegisterStaffActivity.this, LoginActivity.class));
    }
}
