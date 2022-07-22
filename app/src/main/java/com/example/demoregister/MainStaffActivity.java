package com.example.demoregister;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoregister.adapter.AdapterMenuStaff;
import com.example.demoregister.adapter.AdapterOrderInKitchen;
import com.example.demoregister.adapter.AdapterOrderToCook;
import com.example.demoregister.Filter.Constants;
import com.example.demoregister.model.CreateMenuModel;
import com.example.demoregister.model.ModelOrderStaff;
import com.example.demoregister.model.ModelOrderToCook;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MainStaffActivity extends AppCompatActivity {
    private TextView nameTxt, tabTooCookTv, tabInKitchenTv, filterProductTv;
    private TextView emailTxt;
    private EditText searchProductEt;
    private ImageButton logoutBtn,editProfileBtn, addProductBtn, filterProductBtn;
    private ImageView profileImage;
    private RelativeLayout productsRL, ordersRL;

    //order
    private TextView searchTv;
    private ImageButton filterOrderBtn;
    private RecyclerView orderRv;


    private RecyclerView productsRV;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<CreateMenuModel> productList;
    private AdapterMenuStaff adapterMenuStaff;

    //orderToCook
    private AdapterOrderToCook adapterOrderToCook;
    private ArrayList<ModelOrderToCook> orderCookList;

    //orderInKitchen
    private ArrayList<ModelOrderStaff> orderList;
    private AdapterOrderInKitchen adapterOrderInKitchen;
    //private AdapterOrderAdmin adapterOrderAdmin;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_staff);

        nameTxt = findViewById(R.id.name);
        emailTxt = findViewById(R.id.email);
        profileImage = findViewById(R.id.profileIv);

        tabTooCookTv = findViewById(R.id.tabProductsTv);
        tabInKitchenTv = findViewById(R.id.tabOrdersTv);
        //searchProductEt = findViewById(R.id.searchProductEt);

        //filterProductTv = findViewById(R.id.filerteredProductsTv);
        //filterProductBtn = findViewById(R.id.filterProductBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);

        productsRL = findViewById(R.id.productsRL);
        ordersRL = findViewById(R.id.ordersRL);


        //order
        //searchTv = findViewById(R.id.searchTv);
        //filterOrderBtn = findViewById(R.id.filterOrderBtn);
        //recyclerview
        orderRv = findViewById(R.id.orderRv);
        productsRV = findViewById(R.id.productsV);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadTooCook();
        loadInKitchen();
        subsctibeToTopic();
        showTooCookUI();


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
                startActivity(new Intent(MainStaffActivity.this, ProfileEditStaffActivity.class));
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open edit profile activity
                startActivity(new Intent(MainStaffActivity.this, ProfileEditStaffActivity.class));
            }
        });

        tabTooCookTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load products
                showTooCookUI();
            }
        });

        tabInKitchenTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //load products
                showInKitchenUI();
            }
        });


    }

    private void subsctibeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                       // Toast.makeText(MainStaffActivity.this, "Enable Push Notifications", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed subscribing
                        Toast.makeText(MainStaffActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //load order dekat In Kitchen orderstatus = In Progress
    private void loadInKitchen() {
        //init array list
        orderList = new ArrayList<>();

        //load orders of customer
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Order");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before adding new data in it
                orderList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){


                    String OrderStatus = ""+ds.child("orderStatus").getValue();

                    //if selected category matches product category then add in list
                    if (OrderStatus.equals("In Progress")){

                        ModelOrderStaff modelOrderStaff = ds.getValue(ModelOrderStaff.class);

                        //nak kira jumlah brpa bnyak recycler view
                        int countStatus =0;

                        //add to list
                        orderList.add(modelOrderStaff);
                        if(orderList.size() != 0){
                            countStatus += orderList.size();
                            tabInKitchenTv.setText("In Kitchen [ "+countStatus+" ]");
                        }
                        else{
                            //kalau tadak order yang berstatus In Progress display biasa je
                            tabInKitchenTv.setText("In Kitchen [ 0 ]");
                        }

                    }
                }
                //setup adapter
                adapterOrderInKitchen = new AdapterOrderInKitchen(MainStaffActivity.this, orderList);
                //set adapter to recylerview
                orderRv.setAdapter(adapterOrderInKitchen);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainStaffActivity.this, ""+ error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    //load order dekat To Cook orderstatus = Pending
    private void loadTooCook() {
        //init array list
        orderCookList = new ArrayList<>();

        //load orders of customer
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Order");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //clear list before adding new data in it
                orderCookList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){


                    String OrderStatus = ""+ds.child("orderStatus").getValue();

                    //if selected category matches product category then add in list
                    if (OrderStatus.equals("Pending")){

                        ModelOrderToCook modelOrderToCook = ds.getValue(ModelOrderToCook.class);

                        //nak kira jumlah brpa bnyak recycler view
                        int countStatus =0;

                        //add to list
                        orderCookList.add(modelOrderToCook);
                        //nak kira jumlah brpa bnyak recycler view
                        if(orderList.size() != 0){
                            countStatus += orderCookList.size();
                            tabTooCookTv.setText("To Cook [ "+countStatus+" ]");
                        }
                        else{
                            //kalau tadak order yang berstatus In Progress display biasa je
                            tabTooCookTv.setText("To Cook [ "+countStatus+" ]");
                        }

                    }
                }
                //setup adapter
                adapterOrderToCook = new AdapterOrderToCook(MainStaffActivity.this, orderCookList);
                //set adapter to recylerview
                productsRV.setAdapter(adapterOrderToCook);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainStaffActivity.this, ""+ error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTooCookUI() {
        //show To Cook UI and hide In Kitchen UI
        productsRL.setVisibility(View.VISIBLE);
        ordersRL.setVisibility(View.INVISIBLE);

        tabTooCookTv.setTextColor(getResources().getColor(R.color.black));
        tabTooCookTv.setBackgroundResource(R.drawable.shape_rect04);

        tabInKitchenTv.setTextColor(getResources().getColor(R.color.white));
        tabInKitchenTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        loadTooCook();
    }

    private void showInKitchenUI() {
        //show In Kitchen UI and hide  To Cook UI
        ordersRL.setVisibility(View.VISIBLE);
        productsRL.setVisibility(View.INVISIBLE);

        tabTooCookTv.setTextColor(getResources().getColor(R.color.white));
        tabTooCookTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        tabInKitchenTv.setTextColor(getResources().getColor(R.color.black));
        tabInKitchenTv.setBackgroundResource(R.drawable.shape_rect04);

        loadInKitchen();
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
                        Toast.makeText(MainStaffActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainStaffActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            startActivity(new Intent(MainStaffActivity.this, LoginActivity.class));
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
                            String name = ""+ds.child("staffName").getValue();
                            String email = ""+ds.child("staffEmail").getValue();
                            String image = ""+ds.child("staffImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();

                            nameTxt.setText(name);
                            emailTxt.setText(email);

                            try {
                                Picasso.get().load(image).placeholder(R.drawable.ic_baseline_person_24).into(profileImage);
                            }
                            catch (Exception e){
                                profileImage.setImageResource(R.drawable.ic_baseline_person_24);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
