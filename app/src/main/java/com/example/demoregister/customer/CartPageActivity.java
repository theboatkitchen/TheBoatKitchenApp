package com.example.demoregister.customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.example.demoregister.LoginActivity;
import com.example.demoregister.R;
import com.example.demoregister.admin.Constants;
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
        //subTotal = findViewById(R.id.sTotalTv);
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

            }
        });

        Button checkoutBtn = findViewById(R.id.checkoutBtn);

        Currentuser = firebaseAuth.getUid();

        loadAllCart();
        
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addConfirmCart();
            }
        });
    }

    private void addConfirmCart() {
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
        hashMap.put("orderStatus", ""+ "In Progress");
        hashMap.put("orderCost", ""+ cost);
        hashMap.put("orderBy", ""+ Currentuser);

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

    private void loadAllCart() {
        //init list
        cartItemList = new ArrayList<>();

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

                            }

                        }
                        else{
                            Toast.makeText(CartPageActivity.this,"Cart Empty",Toast.LENGTH_SHORT).show();}

                        //setup adapter untuk view menu list
                        adapterCartItem = new AdapterCartItem(CartPageActivity.this, cartItemList);
                        //set to recyclerview
                        cartItemsRv.setAdapter(adapterCartItem);
                        //subTotal.setText("RM" + String.format("%.2f",allTotalprice));
                        TotalPrice.setText("RM" + allTotalprice);
                        //totalPriceTv.setText(new StringBuilder("RM").append(modelCartItem.getTotalprice()));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CartPageActivity.this,""+ error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
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
