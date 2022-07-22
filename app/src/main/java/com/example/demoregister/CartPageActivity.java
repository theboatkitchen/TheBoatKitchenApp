package com.example.demoregister;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.demoregister.adapter.AdapterCartItem;
import com.example.demoregister.Filter.Constants;
import com.example.demoregister.model.ModelCartItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartPageActivity extends AppCompatActivity {

    //need to access these views in adapter so making public
    public ImageView backBtn;
    public TextView subTotal;
    public TextView TotalPrice;

    private ProgressDialog progressDialog;


    Button buttonStoreDatabase;

    FirebaseAuth firebaseAuth;

    String Currentuser;

    Button checkoutBtn;

    //cart
    private ArrayList<ModelCartItem> cartItemList;
    private AdapterCartItem adapterCartItem;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_dialog_cart);

        FirebaseApp.initializeApp(this);
        firebaseAuth = FirebaseAuth.getInstance();

        backBtn = findViewById(R.id.btnBack);
        TotalPrice = findViewById(R.id.totalTv);

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //back
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //go previous activity
                //onBackPressed();
                startActivity(new Intent(CartPageActivity.this, ShopDetailsActivity.class));


                //MainCustomerActivity main = new MainCustomerActivity();

                //main.showOrdersUI();
            }
        });

        checkoutBtn = findViewById(R.id.checkoutBtn);

        Currentuser = firebaseAuth.getUid();

        loadAllCart();
        
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open dialog EDit Text Table No
                dialogTableNo();
            }
        });

    }


    private void dialogTableNo() {
        //inflate layout for dialog
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_table_no, null);

        //init layout views
        EditText DialogtableNo = (EditText) view.findViewById(R.id.tableNoET);
        Button confirmTableNo = view.findViewById(R.id.continueBtn);

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.show();


        //lepas isi table no then customer confirm order
        confirmTableNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data from layout
                String tableNo = DialogtableNo.getText().toString();

                //validate
                if(TextUtils.isEmpty(tableNo)) {
                    Toast.makeText(getApplicationContext(), "Please Enter Table No", Toast.LENGTH_SHORT).show();
                    return;
                }

                addConfirmCart(tableNo);
                dialog.dismiss();
            }
        });

    }

    private void addConfirmCart(String tableNo) {
        if (cartItemList.size() == 0){
            //cart is empty
            Toast.makeText(CartPageActivity.this, "No item in cart", Toast.LENGTH_SHORT).show();
        }

        //show  progress dialog
        progressDialog.setMessage("Placing Order....");
        progressDialog.show();

        //for order id and order time
        final String timestamp = ""+System.currentTimeMillis();

        String cost = TotalPrice.getText().toString().trim().replace("RM","");

        //setup order data
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", ""+timestamp);
        hashMap.put("orderTime", ""+timestamp);
        hashMap.put("orderStatus", ""+ "Pending");
        hashMap.put("orderCost", ""+ cost);
        hashMap.put("orderBy", ""+ Currentuser);
        hashMap.put("orderTable", ""+ tableNo);

        //add to db
        String user = firebaseAuth.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Order");
        DatabaseReference custOrder = FirebaseDatabase.getInstance().getReference("Users");
        DatabaseReference deletecart = FirebaseDatabase.getInstance().getReference("Cart_Food");

        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order info added now add order items

                        for (int i=0; i<cartItemList.size(); i++){
                            String menuID = cartItemList.get(i).getMenuId();
                            String menuName = cartItemList.get(i).getName();
                            String custID = cartItemList.get(i).getUserid();
                            float totalPrice = cartItemList.get(i).getTotalprice();
                            float priceEach = cartItemList.get(i).getCost();
                            int quantity = cartItemList.get(i).getQuantity();
                            String image = cartItemList.get(i).getImage();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("menuId",menuID);
                            hashMap1.put("menuName",menuName);
                            hashMap1.put("image",image);
                            hashMap1.put("priceEach",String.valueOf(priceEach));
                            hashMap1.put("quantity",String.valueOf(quantity));
                            hashMap1.put("TotalPrice",String.valueOf(totalPrice));
                            hashMap1.put("customerID",custID);

                            ref.child(timestamp).child("Items").child(menuID).setValue(hashMap1);
                            //delete cart_food kat db lepas masuk value
                            deletecart.child(custID).child(menuID).removeValue();

                        }
                        progressDialog.dismiss();
                        Toast.makeText(CartPageActivity.this, "Order Placed Successfully...", Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(timestamp);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed placing order
                        progressDialog.dismiss();
                        Toast.makeText(CartPageActivity.this, "" +e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    public double allTotalprice = 0.00;

    public void loadAllCart() {
        //init list
        cartItemList = new ArrayList<>();
        cartItemList.clear();

        //inflate cart layout
        //View view = LayoutInflater.from(this).inflate(R.layout.test_dialog_cart, null);

        //init views
        RecyclerView cartItemsRv = findViewById(R.id.cartItemsRv);

        //get all products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Cart_Food");
        reference.child(Currentuser)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //sebelum dapatkan update tentang product yang lain clear dulu list
                        cartItemList.clear();

                        if (dataSnapshot.exists())
                        {
                            for (DataSnapshot ds:dataSnapshot.getChildren())
                            {
                                //dapatkan value array dari kelas java
                                ModelCartItem modelCartItem = ds.getValue(ModelCartItem.class);
                                modelCartItem.getMenuId();
                                modelCartItem.getTotalprice();
                                //add ke array list
                                cartItemList.add(modelCartItem);

                                allTotalprice += modelCartItem.getTotalprice();

                                updateTotal(cartItemList);
                            }

                        }
                        else{
                            cartItemList.clear();
                            adapterCartItem = new AdapterCartItem(CartPageActivity.this, cartItemList);
                            cartItemsRv.setAdapter(adapterCartItem);
                            TotalPrice.setText(new StringBuilder("RM 0.00"));
                            Toast.makeText(CartPageActivity.this,"Cart Empty",Toast.LENGTH_SHORT).show();}

                        //setup adapter untuk view menu list
                        //adapterCartItem = new AdapterCartItem(CartPageActivity.this, cartItemList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CartPageActivity.this,""+ error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void updateTotal(ArrayList<ModelCartItem> cartItemList) {
        float updateTotal=0;
        //init views
        RecyclerView cartItemsRv = findViewById(R.id.cartItemsRv);
        for(ModelCartItem cm: cartItemList){

            //(Float.parseFloat(String.format("%.2f",modelCartItem.getQuantity()*(modelCartItem.getCost()))));
            //updateTotal+= Float.parseFloat(String.format("%.2f",cm.getTotalprice()));
            updateTotal+=cm.getTotalprice();
        }
        //modelCartItem.setTotalprice(Float.parseFloat(String.format("%.2f",updateTotal));
        TotalPrice.setText(new StringBuilder("RM ").append(Float.parseFloat(String.format("%.2f",updateTotal))));
        checkoutBtn.setText(new StringBuilder("CHECKOUT RM ").append(Float.parseFloat(String.format("%.2f",updateTotal))));
        adapterCartItem = new AdapterCartItem(CartPageActivity.this, cartItemList);
        cartItemsRv.setAdapter(adapterCartItem);
    }

    private void prepareNotificationMessage(String orderId){
        //when user places order, send notification to staff

        //prepare data for notifications
        String NOTIFICATION_TOPIC ="/topics/"+ Constants.FCM_TOPIC; //must be same as subscribed by user
        String NOTIFICATION_TITLE = "New Order"+orderId;
        String NOTIFICATION_MESSAGE = "Congratulations..! You have new order.";
        String NOTIFICATION_TYPE = "NewOrder";

        //prepare json (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try{
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            //cust yg skrng ni login so current user ambik cust id
            notificationBodyJo.put("customerId",firebaseAuth.getUid());
            //notificationBodyJo.put("staffId",firebaseAuth.getUid());
            notificationBodyJo.put("orderId",orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            //WHERE TO SEND
            notificationJo.put("to", NOTIFICATION_TOPIC); // TO ALL WHO SUBSCRIBES TO THIS TOPIC
            notificationJo.put("data",notificationBodyJo);
        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, String orderId) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //after sending fcm start order details activity
                //after placing order open order details page
                Intent intent = new Intent(CartPageActivity.this, OrderDetailsCustomerActivity.class);
                intent.putExtra("orderId", orderId);
                startActivity(intent);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if failed sending fcm still start order details activity

                Intent intent = new Intent(CartPageActivity.this, LoginActivity.class);
                intent.putExtra("orderId", orderId);
                startActivity(intent);

            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                //put required headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key="+ Constants.FCM_KEY);

                return headers;
            }
        };

        //enque the volley request
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}
