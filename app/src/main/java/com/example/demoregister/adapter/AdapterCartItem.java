package com.example.demoregister.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoregister.R;
import com.example.demoregister.CartPageActivity;
import com.example.demoregister.model.ModelCartItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem> {

    private Context context;
    private Context countpage;
    private ArrayList<ModelCartItem> cartItems;

    CartPageActivity cartPageActivity;

    public AdapterCartItem(Context context,ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        //this.countpage = countpage;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_cart_item.xml
        //tukar jadi layout test_cart
        View view = LayoutInflater.from(context).inflate(R.layout.test_cart,parent,false);
        //View viewCountPage = LayoutInflater.from(countpage).inflate(R.layout.row_menu_customer,parent,false);
        return new HolderCartItem(view);
        //return new HolderCartItem(viewCountPage);
    }


    private float TotalP =0;
    private float cost =0;
    int quantity =0;
    String custid,cartid,menuId;
    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, int position) {
        //get data
        ModelCartItem modelCartItem = cartItems.get(position);
        cartid = modelCartItem.getCartid();
        menuId = modelCartItem.getMenuId();
        String title = modelCartItem.getName();
        cost = modelCartItem.getCost();
        TotalP = modelCartItem.getTotalprice();
        quantity = modelCartItem.getQuantity();
        //image
        String icon = modelCartItem.getImage();
        custid = modelCartItem.getUserid();

        //tukarkan float kepada string nak view
        String priceE = String.valueOf(cost);

        //set data
        holder.nameTv.setText(""+title);
        holder.totalPriceTv.setText(new StringBuilder("RM").append(TotalP));
        holder.QuantityTv.setText(""+quantity);
        holder.itemPriceEachTv.setText(new StringBuilder("RM").append(priceE));

        //try catch image menu
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_cart_pinkkelabu).into(holder.icon);
        }
        catch (Exception e){
            holder.icon.setImageResource(R.drawable.ic_cart_pinkkelabu);
        }

        /*
        holder.RemoveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dia akan create table baru kalau tak wujud lagi data, but in that case mesti kena wujud
                EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                        .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_name", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                        .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1,id);//column no 1 is item_id
                Toast.makeText(context,"Removed from cart....", Toast.LENGTH_SHORT).show();

                //refresh list
                int actualPosition = holder.getAdapterPosition();
                cartItems.remove(actualPosition);
                notifyItemChanged(actualPosition);
                notifyDataSetChanged();

                double tx = Double.parseDouble((((ShopDetailsActivity)context).allTotalPriceTv.getText().toString().replace("RM","")));
               // double totalPrice = tx - Double.parseDouble(cost.replace("RM", ""));
                //double sTotalPrice = Double.parseDouble(String.format("%.2f", totalPrice));
                ((ShopDetailsActivity)context).allTotalprice=0.00;
               // ((ShopDetailsActivity)context).sTotalTv.setText("RM"+String.format("%.2f", sTotalPrice));
                //((ShopDetailsActivity)context).allTotalPriceTv.setText("RM"+String.format("%.2f",Double.parseDouble(String.format("%.2f", totalPrice))));
                //double tax = Double.parseDouble((((ShopDetailsActivity)context).tax.replace("RM","")));

            }
        });

         */


        holder.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //finalCost = finalCost + cost;
                //quantity++;

                //setiap kali tekan button plus value dari db akan bertmbah
                //UpdateQuantity++;
                modelCartItem.setQuantity(modelCartItem.getQuantity()+1);
                //buat pengiraan sub total untuk each menu guna kaedah count++
                //TotalP = TotalP + cost;// 0+15
                //double sTotalPrice = Double.parseDouble(String.format("%.2f", totalPrice));
                modelCartItem.setTotalprice(Float.parseFloat(String.format("%.2f",modelCartItem.getQuantity()*(modelCartItem.getCost()))));
                //modelCartItem.setTotalprice(modelCartItem.getQuantity()*(modelCartItem.getCost()));


                //update quantity
                holder.QuantityTv.setText(new StringBuilder().append(modelCartItem.getQuantity()));
                // ((ShopDetailsActivity)context).sTotalTv.setText("RM"+String.format("%.2f", sTotalPrice));
                //holder.totalPriceTv.setText("RM"+String.format("%.2f", modelCartItem.getTotalprice()));
                holder.totalPriceTv.setText(new StringBuilder("RM").append(modelCartItem.getTotalprice()));

                updateFirebase(modelCartItem);

            }
        });

        holder.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(modelCartItem.getQuantity() >=1){
                    //setiap kali tekan minus value dari db akan tolak
                    modelCartItem.setQuantity(modelCartItem.getQuantity()-1);
                    //UpdateQuantity = quantity-1;
                    //buat pengiraan sub total untuk each menu guna kaedah count++
                    modelCartItem.setTotalprice(Float.parseFloat(String.format("%.2f",modelCartItem.getQuantity()*(modelCartItem.getCost()))));
                    //modelCartItem.setTotalprice(modelCartItem.getQuantity()*(modelCartItem.getCost()));
                    //TotalP = TotalP - cost;// 30 - 15

                    holder.QuantityTv.setText(new StringBuilder().append(modelCartItem.getQuantity()));
                    //holder.totalPriceTv.setText("RM"+String.format("%.2f", modelCartItem.getTotalprice()));
                    holder.totalPriceTv.setText(new StringBuilder("RM").append(modelCartItem.getTotalprice()));

                    if(modelCartItem.getQuantity()<=0){
                        deleteCart(modelCartItem.getMenuId());
                    }
                    else {
                        updateFirebase(modelCartItem);
                    }

                }

            }

        });
        //handle remove click listener, delete item from cart
        holder.RemoveTv.setOnClickListener(view -> {
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle("Delete item")
                    .setMessage("Do you really want to delete item")
                    .setNegativeButton("CANCEL", (dialog1, which) -> dialog1.dismiss())
                    .setPositiveButton("OK", (dialog12, which) -> {

                        //Temp remove
                        //AdapterCartItem.this.notifyItemRemoved(position);
                        //AdapterCartItem.this.deleteFromFirebase(cartItems.get(position));

                        //dialog12.dismiss();

                        //refresh list
                        //int actualPosition = holder.getAdapterPosition();
                        //cartItems.remove(actualPosition);
                       // notifyItemChanged(actualPosition);
                        //notifyDataSetChanged();
                        deleteCart(modelCartItem.getMenuId());

                    }).create();
            dialog.show();
        });
    }

    private void deleteCart(String menuId) {
        //remove menu using its id
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Cart_Food");
        reference.child(custid).child(menuId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //menu deleted
                        Toast.makeText(context, "Removed from cart....", Toast.LENGTH_SHORT).show();
                        ((CartPageActivity)context).loadAllCart();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed deleting menu
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateFirebase(ModelCartItem modelCartItem) {
        FirebaseDatabase.getInstance()
                .getReference("Cart_Food")
                .child(custid) // nanti ambik customer id letak dekat sini sbgai foreign key
                .child(modelCartItem.getMenuId())
                .setValue(modelCartItem)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Toast.makeText(context.getApplicationContext(),"Berjaya update quantity dan total",Toast.LENGTH_SHORT).show();
                        ((CartPageActivity)context).loadAllCart();
                    }

                });


    }

    @Override
    public int getItemCount() {
        return cartItems.size();//return number of records
    }

    //view holder class
    public class HolderCartItem extends RecyclerView.ViewHolder{

        //ui views of row_Cartitems.xml
        //ui views of test_cart.xml
        private TextView nameTv, totalPriceTv, itemPriceEachTv, QuantityTv, RemoveTv;
        private ImageView icon,btnMinus,btnPlus;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            //init views
            nameTv = itemView.findViewById(R.id.itemTitleTv);
            totalPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            QuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            icon = itemView.findViewById(R.id.imageView);

            RemoveTv = itemView.findViewById(R.id.itemRemoveTv);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}
