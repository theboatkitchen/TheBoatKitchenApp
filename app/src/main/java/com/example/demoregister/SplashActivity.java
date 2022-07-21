package com.example.demoregister;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demoregister.admin.MainAdminActivity;
import com.example.demoregister.admin.MainStaffActivity;
import com.example.demoregister.customer.MainCustomerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //make fullscreen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        firebaseAuth = FirebaseAuth.getInstance();

        //start login activity after 2 sec
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user == null){
                    //user not logged in start login activity
                    startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                }
                else {
                    //user is logger in, check user type
                    checkUserType();
                }
            }
        }, 2000);
    }

    private void checkUserType() {
        //if user is staff, start seller main screen
        //if user is customer, start user main screen

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.child(firebaseAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String accountType = ""+snapshot.child("accountType").getValue();
                        if (accountType.equals("Staff")){

                            //user is staff
                            startActivity(new Intent(SplashActivity.this, MainStaffActivity.class));
                            finish();
                        }
                        else if (accountType.equals("Admin")){

                            //user is staff
                            startActivity(new Intent(SplashActivity.this, MainAdminActivity.class));
                            finish();
                        }
                        else{

                            //user is customer
                            startActivity(new Intent(SplashActivity.this, MainCustomerActivity.class));
                            finish();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
