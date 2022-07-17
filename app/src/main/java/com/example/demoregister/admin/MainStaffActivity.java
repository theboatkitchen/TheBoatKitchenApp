package com.example.demoregister.admin;

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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoregister.LoginActivity;
import com.example.demoregister.R;
import com.example.demoregister.SettingsActivity;
import com.example.demoregister.model.CreateMenuModel;
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

    private TextView nameTxt, tabProductsTv, tabOrdersTv, filterProductTv;
    private TextView emailTxt;
    private EditText searchProductEt;
    private ImageButton logoutBtn,editProfileBtn, addProductBtn, filterProductBtn, settingBtn;
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

    //order
    private ArrayList<ModelOrderStaff> orderList;
    private AdapterOrderStaff adapterOrderStaff;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_admin);

        nameTxt = findViewById(R.id.name);
        emailTxt = findViewById(R.id.email);
        profileImage = findViewById(R.id.profileIv);

        tabProductsTv = findViewById(R.id.tabProductsTv);
        tabOrdersTv = findViewById(R.id.tabOrdersTv);
        searchProductEt = findViewById(R.id.searchProductEt);

        filterProductTv = findViewById(R.id.filerteredProductsTv);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);
        addProductBtn = findViewById(R.id.addProductBtn);
        //settingBtn = findViewById(R.id.settingsBtn);


        productsRL = findViewById(R.id.productsRL);
        ordersRL = findViewById(R.id.ordersRL);
        productsRV = findViewById(R.id.productsV);

        //order
        searchTv = findViewById(R.id.searchTv);
        filterOrderBtn = findViewById(R.id.filterOrderBtn);
        orderRv = findViewById(R.id.orderRv);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        loadAllProducts();
        loadAllOrders();

        showProductsUI();

        //start setting screen
        /*
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainStaffActivity.this, SettingsActivity.class));
            }
        });

         */

        //search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    adapterMenuStaff.getFilter().filter(s);

                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

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

        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open create menu activity
                startActivity(new Intent(MainStaffActivity.this, CreateMenu.class));
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

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainStaffActivity.this);
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
                                        loadFilteredProducts(selected);
                                    }
                            }
                        })
                        .show();
            }
        });

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //options to display in dialog
                String[] options ={"All", "In Progress", "Completed", "Canceled"};
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainStaffActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //handle item clicks
                                if (which==0){
                                    //All clicked
                                    searchTv.setText("Showing All Orders");
                                    adapterOrderStaff.getFilter().filter("");// show all orders
                                }
                                else {
                                    String optionClicked = options[which];
                                    searchTv.setText("Showing "+optionClicked+" Orders");//Showing Completed Orders
                                    adapterOrderStaff.getFilter().filter(optionClicked);
                                }
                            }
                        })
                        .show();

            }
        });

        subsctibeToTopic();
    }

    private void subsctibeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(MainStaffActivity.this, "Enable Push Notifications", Toast.LENGTH_SHORT).show();
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

    private void loadAllOrders() {
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
                    ModelOrderStaff modelOrderStaff = ds.getValue(ModelOrderStaff.class);
                    //add to list
                    orderList.add(modelOrderStaff);
                }
                //setup adapter
                adapterOrderStaff = new AdapterOrderStaff(MainStaffActivity.this, orderList);
                //set adapter to recylerview
                orderRv.setAdapter(adapterOrderStaff);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainStaffActivity.this, ""+ error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFilteredProducts(String selected) {
        productList = new ArrayList<>();
        String user = firebaseAuth.getUid();

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Menu");
        reference.orderByChild("empid").equalTo(user)
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
                        adapterMenuStaff = new AdapterMenuStaff(MainStaffActivity.this, productList);
                        //set adapter
                        productsRV.setAdapter(adapterMenuStaff);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadAllProducts() {
        productList = new ArrayList<>();

        String user = firebaseAuth.getUid();
        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Menu");
        reference.orderByChild("empid").equalTo(user)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //sebelum dapatkan update tentang product yang lain clear dulu list
                        productList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            //dapatkan value array dari kelas java
                            CreateMenuModel createMenuModel = ds.getValue(CreateMenuModel.class);
                            productList.add(createMenuModel);
                        }
                        //setup adapter untuk view menu list
                        adapterMenuStaff = new AdapterMenuStaff(MainStaffActivity.this, productList);
                        //set adapter value menu list
                        productsRV.setAdapter(adapterMenuStaff);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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

    private void showOrdersUI() {
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

    public void editProfile(View view) {
        //open edit profile activity
        startActivity(new Intent(MainStaffActivity.this, ProfileEditStaffActivity.class));
    }
}
