package com.example.demoregister;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.demoregister.Filter.Constants;
import com.example.demoregister.adapter.AdapterOrderedItem;
import com.example.demoregister.model.ModelOrderedItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class OrderDetailsCustomerActivity extends AppCompatActivity {

    String orderId,orderBy;

    //ui views
    private ImageButton backBtn;
    private TextView orderIdTv,dateTv,orderStatusTv,amountTv,tableNoTv,totalItemsRv;

    private Button cancelOrder;
    private RecyclerView itemsRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemList;
    private AdapterOrderedItem adapterOrderedItem;

    String orderStatus;
    String selectedOptions;

    MainCustomerActivity mainCustomerActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_customer);

        //init views
        backBtn = findViewById(R.id.backBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        amountTv = findViewById(R.id.amountTv);
        tableNoTv = findViewById(R.id.tableNoTv);
        totalItemsRv = findViewById(R.id.itemsTv);
        itemsRv = findViewById(R.id.itemsRv);
        //only show cancel order if orderstatus is pending only means that the kitchen tak accept lagi customer punya order
        cancelOrder = findViewById(R.id.cancelOrder);


        //get data from intent daripada MyFirebaseMessaging jugak at the same time
        //get data intent orderDetailsCustomerActivity
        //final Intent intent = getIntent();
        //orderId = intent.getStringExtra("orderId");
        Bundle bundle = new Bundle();
        bundle = getIntent().getExtras();
        orderId = bundle.getString("orderId");

        firebaseAuth = FirebaseAuth.getInstance();
        
        loadOrderDetails();
        loadOrderedItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OrderDetailsCustomerActivity.this, MainCustomerActivity.class));
            }
        });

        //cancelOrder
        cancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelOrderComfirmation();
            }
        });

    }

    private void cancelOrderComfirmation() {
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete")
                .setMessage("Are you sure you want to cancel this order?")
                .setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //delete
                        String checkOrder = orderStatusTv.getText().toString();

                        if(checkOrder.equals("Pending")) {
                            cancelOrder(); //id is the menu id
                        }
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //cancel,dimiss
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //change the order status from pending to cancel order
    private void cancelOrder() {

        //setup data to put in firebase db
        selectedOptions="Cancelled";
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus",""+selectedOptions);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Order");
        ref.child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order status is now cancel
                        loadOrderDetails();
                        loadOrderedItems();

                        String message = "Order is now "+selectedOptions;
                        //status updated
                        Toast.makeText(OrderDetailsCustomerActivity.this, message,Toast.LENGTH_SHORT).show();


                        //notify the staff of cancelling order

                        prepareNotificationMessage(orderId, message);

                        startActivity(new Intent(OrderDetailsCustomerActivity.this, MainCustomerActivity.class));

                        //open customer main page
                        //startActivity(new Intent(OrderDetailsCustomerActivity.this, ShopDetailsActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed deleting menu
                        Toast.makeText(OrderDetailsCustomerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOrderedItems() {
        //init list
        orderedItemList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Order");
        ref.child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderedItemList.clear(); //before loading itemes clear list
                        for (DataSnapshot ds:snapshot.getChildren()){
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                            //add to list
                            orderedItemList.add(modelOrderedItem);
                        }
                        //all items added to list
                        //setup adpter
                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsCustomerActivity.this, orderedItemList);
                        //set adapter
                        itemsRv.setAdapter(adapterOrderedItem);

                        //set items count
                        totalItemsRv.setText(""+snapshot.getChildrenCount());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        //load order details
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Order");
        ref.child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //get data
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderCost").getValue();
                        String orderID = ""+snapshot.child("orderId").getValue();
                        orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderTable = ""+snapshot.child("orderTable").getValue();

                        //conver timestamp to proper format
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        //ex date format 20/05/2022 12.35 PM
                        String formatedDate = DateFormat.format("dd/MM/yyyy hh:mm a",calendar).toString();


                        //change order status color
                        if (orderStatus.equals("Pending")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.teal_700));
                            cancelOrder.setVisibility(View.VISIBLE);
                        }
                        if (orderStatus.equals("In Progress")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.blue));
                            cancelOrder.setVisibility(View.INVISIBLE);
                        }
                        else if (orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.green));
                            cancelOrder.setVisibility(View.INVISIBLE);
                        }
                        else if (orderStatus.equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.red));
                            cancelOrder.setVisibility(View.INVISIBLE);
                        }

                        //set data
                        orderIdTv.setText(orderID);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("RM "+orderCost);
                        dateTv.setText(formatedDate);
                        tableNoTv.setText(orderTable);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderDetailsCustomerActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void prepareNotificationMessage(String orderId, String message){
        //when cust changes order status InProgress/Cancelled/Completed, send notification to staff

        //prepare data for notifications
        String NOTIFICATION_TOPIC ="/topics/"+ Constants.FCM_TOPIC; //must be same as subscribed by user
        String NOTIFICATION_TITLE = "The Status of Order "+orderId;
        String NOTIFICATION_MESSAGE = ""+message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";

        //prepare json (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try{
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            //cust yg skrng ni login so current user ambik cust id
            notificationBodyJo.put("customerId",firebaseAuth.getUid());
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
                Intent intent = new Intent(OrderDetailsCustomerActivity.this, OrderDetailsCustomerActivity.class);
                intent.putExtra("orderId", orderId);
                startActivity(intent);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //if failed sending fcm still start order details activity

                Intent intent = new Intent(OrderDetailsCustomerActivity.this, LoginActivity.class);
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