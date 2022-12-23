package com.example.demoregister.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoregister.FilterOrderAdmin;
import com.example.demoregister.R;
import com.example.demoregister.model.ModelOrderStaff;
import com.example.demoregister.OrderDetailsAdminActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderAdmin extends RecyclerView.Adapter<AdapterOrderAdmin.HolderOrderShop> implements Filterable {

    private Context context;
    public ArrayList<ModelOrderStaff> orderStaffList, filterList;
    private FilterOrderAdmin filter;

    public AdapterOrderAdmin(Context context, ArrayList<ModelOrderStaff> orderStaffList) {
        this.context = context;
        this.orderStaffList = orderStaffList;
        this.filterList = orderStaffList;
    }

    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_admin, parent, false);
        return new HolderOrderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position) {

        //get data at position
        ModelOrderStaff modelOrderStaff = orderStaffList.get(position);
        final String orderId = modelOrderStaff.getOrderId();
        String orderTime = modelOrderStaff.getOrderTime();
        String orderStatus = modelOrderStaff.getOrderStatus();
        String orderCost = modelOrderStaff.getOrderCost();
        final String orderBy = modelOrderStaff.getOrderBy();
        String orderTable = modelOrderStaff.getOrderTable();


        //set data
        holder.orderIdTv.setText("OrderID: "+orderId);
        holder.amountTv.setText("Total Amount: RM " +orderCost);
        holder.statusTv.setText(orderStatus);
        holder.tableNoTv.setText(orderTable);

        //change order status
        if (orderStatus.equals("Pending")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.teal_700));
        }
        if (orderStatus.equals("In Progress")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.blue));
        }
        else if (orderStatus.equals("Completed")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.green));
        }
        else if (orderStatus.equals("Cancelled")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.red));
        }

        //conver timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formateDate = DateFormat.format("dd/MM/yyyy",calendar).toString();

        holder.dateTv.setText(formateDate);

        //conver timestamp to time
        //ex date format 12.35 PM
        String time = DateFormat.format("hh:mm a",calendar).toString();

        holder.timeTv.setText(time);


        holder.viewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details, we need to keys there, orderID
                Intent intent = new Intent(context, OrderDetailsAdminActivity.class);
                intent.putExtra("orderId",orderId); //to load order info
                intent.putExtra("orderBy",orderBy); //to load info cust who placed the order
                context.startActivity(intent); // now get these values through OrdersDetailsActivity
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details, we need to keys there, orderID
                Intent intent = new Intent(context, OrderDetailsAdminActivity.class);
                intent.putExtra("orderId",orderId); //to load order info
                intent.putExtra("orderBy",orderBy); //to load info cust who placed the order
                context.startActivity(intent); // now get these values through OrdersDetailsActivity
            }
        });
    }


    @Override
    public int getItemCount() {
        return orderStaffList.size();

        //return size of list/ number of records
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            //init filter
            filter = new FilterOrderAdmin(this, filterList);
        }
        return filter;
    }


    //view holder class for row_order_staff
    class HolderOrderShop extends RecyclerView.ViewHolder{

        //ui views of row_order_staff.xml
        //view of layout
        private TextView orderIdTv,dateTv,timeTv,tableNoTv,amountTv,statusTv;
        private ImageView viewItem;

        public HolderOrderShop(@NonNull View itemView) {
            super(itemView);

            //init views of layout
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            tableNoTv = itemView.findViewById(R.id.tableNoTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTV);
            viewItem = itemView.findViewById(R.id.nextIv);

        }
    }
}
