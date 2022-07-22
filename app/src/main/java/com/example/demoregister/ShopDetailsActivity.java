package com.example.demoregister;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;


import com.example.demoregister.adapter.AdapterCartItem;
import com.example.demoregister.adapter.AdapterMenuCustomer;
import com.example.demoregister.Filter.Constants;
import com.example.demoregister.model.CreateMenuModel;
import com.example.demoregister.model.ModelCartItem;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nex3z.notificationbadge.NotificationBadge;

import java.util.ArrayList;

public class ShopDetailsActivity extends AppCompatActivity {

    //declare ui views
    private TextView filterProductTv;
    private EditText searchProductEt;
    private ImageButton cartBtn,backBtn,logoutBtn,filterProductBtn,viewcart;
    private RelativeLayout productsRL, ordersRL;
    private NotificationBadge badge;


    private RecyclerView productsRV;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private ArrayList<CreateMenuModel> productList;
    private AdapterMenuCustomer adapterMenuCustomer;

    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;



    String Currentuser;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shope_details);

        //init ui viewa

        searchProductEt = findViewById(R.id.searchProductEt);
        filterProductTv = findViewById(R.id.filerteredProductsTv);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        //logoutBtn = findViewById(R.id.logoutBtn);
        backBtn = findViewById(R.id.backBtn);
        viewcart = findViewById(R.id.cartButton);

        productsRL = findViewById(R.id.productsRL);
        ordersRL = findViewById(R.id.ordersRL);
        productsRV = findViewById(R.id.productsV);

        //notification of cart
        badge = findViewById(R.id.badge);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();
        //get current user
        Currentuser = firebaseAuth.getUid();

        //Do all this process
        checkUser();
        loadAllProducts();



        //back
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go previous activity
                //onBackPressed();
                startActivity(new Intent(ShopDetailsActivity.this, MainCustomerActivity.class));

            }
        });

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterMenuCustomer.getFilter().filter(s);

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category:")
                        .setItems(Constants.menuCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                //get selected item
                                String selected = Constants.menuCategories1[which];
                                filterProductTv.setText(selected);
                                if (selected.equals("All")){
                                    //load all
                                    loadAllProducts();
                                }
                                else{
                                    //load filtered
                                    //loadFilteredProducts(selected);
                                    adapterMenuCustomer.getFilter().filter(selected);
                                }
                            }
                        })
                        .show();
            }
        });

        viewcart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCart(); // akan pergi ke CartPageActivity.java
            }
        });

        //countcart item untuk nak dapatkan notification nanti
        countCartItem();
    }

    private void addCart() {
        //open cartpage activity
        startActivity(new Intent(ShopDetailsActivity.this, CartPageActivity.class));

    }

    private void loadFilteredProducts(String selected) {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Menu")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //before getting rest List
                        productList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            String productCategory = ""+ds.child("category").getValue();

                            //if selected category matches product category then add in list
                            if (selected.equals(productCategory)){

                                CreateMenuModel createMenuModel = ds.getValue(CreateMenuModel.class);
                                productList.add(createMenuModel);

                            }
                        }
                        //setup adapter
                        adapterMenuCustomer = new AdapterMenuCustomer(ShopDetailsActivity.this, productList);
                        //set adapter
                        productsRV.setAdapter(adapterMenuCustomer);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ShopDetailsActivity.this,""+ error.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Menu");

        reference.orderByChild("menuID")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //sebelum dapatkan update tentang product yang lain clear dulu list
                        productList.clear();

                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            //dapatkan value array dari kelas java

                            CreateMenuModel createMenuModel = ds.getValue(CreateMenuModel.class);

                            //parsing value to list if makanan tu available pada hari itu
                            if(createMenuModel.getAvailability().equals("Available")){
                                productList.add(createMenuModel);
                            }

                        }
                        //setup adapter untuk view menu list
                        adapterMenuCustomer = new AdapterMenuCustomer(ShopDetailsActivity.this, productList);
                        //set adapter value menu list
                        productsRV.setAdapter(adapterMenuCustomer);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ShopDetailsActivity.this,""+ error.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }

    public void countCartItem() {
        cartItemList = new ArrayList<>();

        //cartItemList.clear();
        DatabaseReference cartReference = FirebaseDatabase.getInstance().getReference("Cart_Food");
        cartReference.child(Currentuser)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    //dapatkan value array dari kelas java
                    ModelCartItem modelCartItem = ds.getValue(ModelCartItem.class);
                    modelCartItem.getMenuId();
                    //add ke array list
                    cartItemList.add(modelCartItem);
                }
                        countNoti(cartItemList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //cartLoadListener.onCartLoadFailed(error.getMessage());
                Toast.makeText(ShopDetailsActivity.this,""+ error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void countNoti(ArrayList<ModelCartItem> cartItemList) {

        int cartSum =0;

        for (ModelCartItem cm: cartItemList)

            //cartsum nilai 0 = 0+quantity (15)
            //cartsum 15 = 15+20
            //cartsum 35 = 35 + ....
            cartSum += cm.getQuantity(); //akan tmbah notification kalau user add menu yg baru dri recycle view

        badge.setNumber(cartSum);
    }

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user == null){
            startActivity(new Intent(ShopDetailsActivity.this, LoginActivity.class));
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
                            String accountType = ""+ds.child("accountType").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ShopDetailsActivity.this,""+ error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
