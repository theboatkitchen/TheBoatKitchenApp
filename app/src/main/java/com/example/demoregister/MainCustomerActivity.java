package com.example.demoregister;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoregister.adapter.AdapterMenuCustomer;
import com.example.demoregister.adapter.AdapterOrderCustomer;
import com.example.demoregister.Filter.Constants;
import com.example.demoregister.model.ModelCartItem;
import com.example.demoregister.model.ModelOrderCustomer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.nex3z.notificationbadge.NotificationBadge;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainCustomerActivity extends AppCompatActivity {

    private TextView nameTxt, tabProductsTv, tabOrdersTv;
    private TextView emailTxt;
    private ImageButton logoutBtn,editProfileBtn,settingBtn;
    private ImageView profileImage;
    private RelativeLayout productsRL, ordersRL;
    private Button startorderBtn;
    private NotificationBadge badge;
    private RecyclerView ordersRv;


    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private Context context;

    private ArrayList<ModelCartItem> creamenumodelList;
    private AdapterMenuCustomer adapterMenuCustomer;
    private ArrayList<ModelOrderCustomer> ordersList;
    private AdapterOrderCustomer adapterOrderCustomer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_customer);

        View grab = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_shope_details, null);
        badge = grab.findViewById(R.id.badge);
        badge.clear();


        nameTxt = findViewById(R.id.name);
        emailTxt = findViewById(R.id.email);
        profileImage = findViewById(R.id.profileIv);

        tabProductsTv = findViewById(R.id.tabProductsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);

        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        startorderBtn = findViewById(R.id.startorder);
        //settingBtn = findViewById(R.id.settingsBtn);


        productsRL = findViewById(R.id.productsRL);
        ordersRL = findViewById(R.id.ordersRL);
        ordersRv = findViewById(R.id.ordersRv);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        subsctibeToTopic();
        showProductsUI();

        /*
        //start setting screen
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainCustomerActivity.this, SettingsActivity.class));
            }
        });

         */
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //make offline
                //sign out

                //go to login activity
                makeMeOffline();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open edit profile activity
                startActivity(new Intent(MainCustomerActivity.this, ProfileEditCustomerActivity.class));
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open edit profile activity
                startActivity(new Intent(MainCustomerActivity.this, ProfileEditCustomerActivity.class));
            }
        });

        //bawak value dari table account type staff menu yg dipegangnya

        startorderBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               //open edit profile activity

               startActivity(new Intent(MainCustomerActivity.this, ShopDetailsActivity.class));
           }
        });

        tabProductsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load products
                showProductsUI();
            }
        });

        tabOrdersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load products
                showOrdersUI();

            }
        });


    }


    private void subsctibeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Toast.makeText(MainCustomerActivity.this, "Enable Push Notifications", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed subscribing
                        Toast.makeText(MainCustomerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showProductsUI() {
        //show products UI and hide order UI
        productsRL.setVisibility(View.VISIBLE);
        ordersRL.setVisibility(View.INVISIBLE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.black));
        tabProductsTv.setBackgroundResource(R.drawable.shape_rect04);

        tabOrdersTv.setTextColor(getResources().getColor(R.color.white));
        tabOrdersTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    public void showOrdersUI() {
        //show order UI and hide products UI
        ordersRL.setVisibility(View.VISIBLE);
        productsRL.setVisibility(View.INVISIBLE);

        tabProductsTv.setTextColor(getResources().getColor(R.color.white));
        tabProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabOrdersTv.setTextColor(getResources().getColor(R.color.black));
        tabOrdersTv.setBackgroundResource(R.drawable.shape_rect04);
    }

    private void makeMeOffline() {
        //after logging in, make user online
        progressDialog.setMessage("Logging out...");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online","false");

        //updating value to db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update successfully
                        firebaseAuth.signOut();
                        checkUser();
                        unSubcribeToTopic();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating
                        progressDialog.dismiss();
                        Toast.makeText(MainCustomerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unSubcribeToTopic() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed unsubscribing
                        Toast.makeText(MainCustomerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            startActivity(new Intent(MainCustomerActivity.this, LoginActivity.class));
            finish();
        }
        else {
            loadMyInfo();
        }
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("userid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String name = ""+ds.child("custName").getValue();
                            String email = ""+ds.child("custEmail").getValue();
                            String phone = ""+ds.child("custPhone").getValue();
                            String image = ""+ds.child("custImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();

                            nameTxt.setText(name);
                            emailTxt.setText(email);

                            try {
                                Picasso.get().load(image).placeholder(R.drawable.ic_baseline_person_24).into(profileImage);
                            }
                            catch (Exception e){
                                profileImage.setImageResource(R.drawable.ic_baseline_person_24);
                            }

                            //load shope
                            loadShops();
                            //load orders
                            loadOrders();
                            
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainCustomerActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOrders() {
        //init order list
        ordersList = new ArrayList<>();

        //get orders
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Order");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersList.clear();

                for(DataSnapshot ds: snapshot.getChildren()){

                    String orderID = ""+ds.child("orderId").getValue();
                    //String userID2 = ""+ds.child("").getValue();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Order");
                    ref.orderByChild("orderBy").equalTo(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ordersList.clear();
                                    if(snapshot.exists()){
                                        for(DataSnapshot ds: snapshot.getChildren()){
                                            ModelOrderCustomer modelOrderCustomer = ds.getValue(ModelOrderCustomer.class);

                                            //add to list
                                            ordersList.add(modelOrderCustomer);
                                        }
                                        //setup adapter
                                        adapterOrderCustomer = new AdapterOrderCustomer(MainCustomerActivity.this, ordersList);
                                        //set to recyclerview
                                        ordersRv.setAdapter(adapterOrderCustomer);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadShops() {
        //init list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Menu");
        ref.orderByChild("menuID")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String name = ""+ds.child("menuName").getValue();
                            String category = ""+ds.child("category").getValue();
                            String description = ""+ds.child("description").getValue();
                            String image = ""+ds.child("menuImage").getValue();
                            String price = ""+ds.child("price").getValue();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

}
