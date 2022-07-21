package com.example.demoregister.customer;

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

import com.example.demoregister.R;
import com.example.demoregister.model.ModelCartItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

public class OrderDetailsCustomerActivity extends AppCompatActivity {

    String orderId,orderBy;

    //ui views
    private ImageButton backBtn;
    private TextView orderIdTv,dateTv,orderStatusTv,amountTv,tableNoTv,totalItemsRv;

    private Button cancelOrder;
    private RecyclerView itemsRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemList;
    private AdapterOrderedItem  adapterOrderedItem;

    String orderStatus;

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
                onBackPressed();
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
                .setMessage("Are you sure you want to delete this order?")
                .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //delete
                        String checkOrder = orderStatusTv.getText().toString();

                        if(checkOrder.equals("Pending")) {
                            deleteOrder(); //id is the menu id
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

    private void deleteOrder() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Order");
        ref.child(orderId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //order deleted
                        //open customer main page
                        startActivity(new Intent(OrderDetailsCustomerActivity.this, MainCustomerActivity.class));
                        //Toast.makeText(OrderDetailsCustomerActivity.this, "Order deleted.....", Toast.LENGTH_SHORT).show();
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


}