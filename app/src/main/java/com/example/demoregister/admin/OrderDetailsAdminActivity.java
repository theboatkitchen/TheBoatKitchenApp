package com.example.demoregister.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.demoregister.R;
import com.example.demoregister.customer.AdapterOrderedItem;
import com.example.demoregister.customer.ModelOrderedItem;
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

public class OrderDetailsAdminActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn,editBtn;

    private TextView orderIdTv,dateTv,orderStatusTv,amountTv,tableNoTv,totalItemsRv,custNameTv;

    private RecyclerView itemsRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemList;
    private AdapterOrderedItem adapterOrderedItem;

    String orderId,orderBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_admin);


        //init views
        backBtn = findViewById(R.id.backBtn);
        editBtn = findViewById(R.id.editBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        amountTv = findViewById(R.id.amountTv);
        tableNoTv = findViewById(R.id.tableNoTv);
        totalItemsRv = findViewById(R.id.itemsTv);
        itemsRv = findViewById(R.id.itemsRv);
        custNameTv = findViewById(R.id.custNameTv);

        //get data from intent holder view items adapterorderstaff
        //get data from intent daripada MyFirebaseMessaging jugak at the same time
        Intent intent = getIntent();
        orderId = intent.getStringExtra("orderId");
        orderBy = intent.getStringExtra("orderBy");


        firebaseAuth = FirebaseAuth.getInstance();
        //loadMyInfo();
        loadCustInfo();
        loadOrderDetails();
        loadOrderedItems();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //edit order status: In Progress, Completed, Cancelled
                editOrderStatusDialog();
            }
        });
    }

    private void editOrderStatusDialog() {
        //options to display in dialog
        final String[] options = {"Pending","In Progress", "Completed", "Cancelled"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Status")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        //handle item clicks
                        String selectedOptions = options[i];
                        editOrderStatus(selectedOptions);
                    }
                }).show();
    }

    private void editOrderStatus(String selectedOptions) {
        //setup data to put in firebase db
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus",""+selectedOptions);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Order");
        ref.child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        String message = "Order is now "+selectedOptions;
                        //status updated
                        Toast.makeText(OrderDetailsAdminActivity.this, message,Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(orderId, message);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed updating status, show reason
                        Toast.makeText(OrderDetailsAdminActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
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
                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsAdminActivity.this, orderedItemList);
                        //set adapter
                        itemsRv.setAdapter(adapterOrderedItem);

                        //set items count
                        totalItemsRv.setText(""+snapshot.getChildrenCount());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderDetailsAdminActivity.this, ""+ error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOrderDetails() {
        //load detailed info of this order, based on order id
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Order");
        ref.child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //get data
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderCost").getValue();
                        String orderID = ""+snapshot.child("orderId").getValue();
                        String orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderTable = ""+snapshot.child("orderTable").getValue();

                        //conver timestamp to proper format
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        //ex date format 20/05/2022 12.35 PM
                        String formatedDate = DateFormat.format("dd/MM/yyyy hh:mm a",calendar).toString();


                        //change order status
                        if (orderStatus.equals("Pending")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.teal_700));
                        }
                        if (orderStatus.equals("In Progress")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.blue));
                        }
                        else if (orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.green));
                        }
                        else if (orderStatus.equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.red));
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
                        Toast.makeText(OrderDetailsAdminActivity.this, ""+ error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    //orderByChild("userid").equalTo(orderBy)
    private void loadCustInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("userid").equalTo(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            String name = ""+ds.child("custName").getValue();
                            String email = ""+ds.child("custEmail").getValue();
                            String phone = ""+ds.child("custPhone").getValue();
                            String image = ""+ds.child("custImage").getValue();
                            String accountType = ""+ds.child("accountType").getValue();

                            custNameTv.setText(name);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderDetailsAdminActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String staffEmail = ""+snapshot.child("staffEmail").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderDetailsAdminActivity.this, ""+ error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void prepareNotificationMessage(String orderId, String message){
        //when staff changes order status InProgress/Cancelled/Completed, send notification to customer

        //prepare data for notifications
        String NOTIFICATION_TOPIC ="/topics/"+ Constants.FCM_TOPIC; //must be same as subscribed by user
        String NOTIFICATION_TITLE = "Your Order"+orderId;
        String NOTIFICATION_MESSAGE = ""+message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";

        //prepare json (what to send and where to send)
        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try{
            //what to send
            notificationBodyJo.put("notificationType",NOTIFICATION_TYPE);
            notificationBodyJo.put("customerId",orderBy);
            //staff yg skrng ni login to change status so current user ambik staff id
            notificationBodyJo.put("staffId",firebaseAuth.getUid());
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

        sendFcmNotification(notificationJo);
    }

    private void sendFcmNotification(JSONObject notificationJo) {
        //send volley request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //notification sent
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //notification failed
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