package com.example.demoregister.customer;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoregister.R;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderCustomer extends RecyclerView.Adapter<AdapterOrderCustomer.HolderOrderUser>{

    private Context context;
    private ArrayList<ModelOrderCustomer> orderCustomerList;

    public AdapterOrderCustomer(Context context, ArrayList<ModelOrderCustomer> orderCustomerList) {
        this.context = context;
        this.orderCustomerList = orderCustomerList;
    }

    @NonNull
    @Override
    public HolderOrderUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_user, parent, false);
        return new HolderOrderUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderUser holder, int position) {

        //get data
        ModelOrderCustomer modelOrderCustomer = orderCustomerList.get(position);
        String orderId = modelOrderCustomer.getOrderId();
        String orderTime = modelOrderCustomer.getOrderTime();
        String orderStatus = modelOrderCustomer.getOrderStatus();
        String orderCost = modelOrderCustomer.getOrderCost();
        String orderBy = modelOrderCustomer.getOrderBy();
        String orderTable = modelOrderCustomer.getOrderTable();

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
                Intent intent = new Intent(context, OrderDetailsCustomerActivity.class);
                intent.putExtra("orderId",orderId);
                context.startActivity(intent); // now get these values through OrdersDetailsActivity
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open order details, we need to keys there, orderID
                Intent intent = new Intent(context, OrderDetailsCustomerActivity.class);
                intent.putExtra("orderId",orderId);
                context.startActivity(intent); // now get these values through OrdersDetailsActivity
            }
        });

    }

    @Override
    public int getItemCount() {
        return orderCustomerList.size();
    }

    //view holder class
    class HolderOrderUser extends RecyclerView.ViewHolder{

        //view of layout
        private TextView orderIdTv,dateTv,timeTv,tableNoTv,amountTv,statusTv;
        private ImageView viewItem;


        public HolderOrderUser(@NonNull View itemView) {
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
