package com.example.demoregister;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demoregister.admin.MainAdminActivity;
import com.example.demoregister.admin.MainStaffActivity;
import com.example.demoregister.admin.RegisterStaffActivity;
import com.example.demoregister.customer.MainCustomerActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    //UI views
    private EditText emailEt, passwordEt;
    private TextView forgotTv;
    private Button loginBtn;
    private ImageButton backBtn;

    private FirebaseAuth mAuth;
    private String UID;
    private FirebaseUser user;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //backBtn = findViewById(R.id.backBtn);
        emailEt = findViewById(R.id.email);
        passwordEt = findViewById(R.id.password);
        forgotTv = findViewById(R.id.forgot_password);
        loginBtn = findViewById(R.id.loginBtn);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //backBtn.setOnClickListener((v) -> {onBackPressed();});

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        forgotTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
            }
        });
    }

    private String emailTxt,passwordTxt;

    private void loginUser() {
        emailTxt = emailEt.getText().toString().trim();
        passwordTxt = passwordEt.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(emailTxt).matches()){
            Toast.makeText(this, "Invalid email pattern...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(passwordTxt)){
            Toast.makeText(this, "Enter password...",Toast.LENGTH_SHORT).show();
        }

        progressDialog.setMessage("Logging In..");
        progressDialog.show();

        mAuth.signInWithEmailAndPassword(emailTxt,passwordTxt)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //logged in succesfully
                        makeMeOnline();
                    }


                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed logging in
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void makeMeOnline() {
        //after logging in, make user online
        progressDialog.setMessage("Checking User...");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online","true");
        //String update;

        //updating value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //ref.orderByChild("custid").equalTo(mAuth.getUid()
        //ref.child("online").setValue("true");
        //ref.child("custid").child("online").setValue("true")
        //String update = FirebaseAuth.getInstance().getCurrentUser().getUid();

        user = mAuth.getCurrentUser();


        //yang asal
        ref.child(mAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update successfully
                        checkUserType();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void checkUserType() {
        //if user is staff, start seller main screen
        //if user is customer, start user main screen

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.orderByChild("userid").equalTo(mAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()){
                            String accountType = ""+ds.child("accountType").getValue();
                            if (accountType.equals("Staff")){
                                //login untuk staff
                                progressDialog.dismiss();
                                //user is staff
                                startActivity(new Intent(LoginActivity.this, MainStaffActivity.class));
                                finish();
                            }
                            else if (accountType.equals("Admin")){
                                //login untuk Admin
                                progressDialog.dismiss();
                                //user is staff
                                startActivity(new Intent(LoginActivity.this, MainAdminActivity.class));
                                finish();
                            }
                            else{
                                progressDialog.dismiss();
                                //user is customer
                                startActivity(new Intent(LoginActivity.this, MainCustomerActivity.class));
                                finish();

                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //nak link dengan page register
    public void register(View view) {
        startActivity(new Intent(LoginActivity.this,RegistrationActivity.class));
    }


    public void register_staff(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterStaffActivity.class));
    }
}


