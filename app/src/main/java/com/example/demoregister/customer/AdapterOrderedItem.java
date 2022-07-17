package com.example.demoregister.customer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoregister.R;
import com.example.demoregister.model.ModelCartItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterOrderedItem extends RecyclerView.Adapter<AdapterOrderedItem.HolderOrderedItem> {

    private Context context;
    private ArrayList<ModelOrderedItem> orderedItemArrayList;

    public AdapterOrderedItem(Context context, ArrayList<ModelOrderedItem> orderedItemArrayList) {
        this.context = context;
        this.orderedItemArrayList = orderedItemArrayList;
    }

    @NonNull
    @Override
    public HolderOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordered_item, parent, false);
        return new HolderOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedItem holder, int position) {

        //get data
        ModelOrderedItem modelOrderedItem = orderedItemArrayList.get(position);

        String menuID = modelOrderedItem.getMenuId();
        String menuName = modelOrderedItem.getMenuName();
        String image = modelOrderedItem.getImage();
        String customerID = modelOrderedItem.getCustomerID();
        String priceEach = modelOrderedItem.getPriceEach();
        String TotalPrice = modelOrderedItem.getTotalPrice();
        String quantity = modelOrderedItem.getQuantity();

        //set data
        holder.itemNameTv.setText(menuName);
        holder.itemPriceTv.setText("Total: RM "+TotalPrice);
        holder.itemPriceEachTv.setText("RM "+priceEach);
        holder.itemQuantityTv.setText(quantity);

        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_cart_pinkkelabu).into(holder.icon);
        }
        catch (Exception e){
            holder.icon.setImageResource(R.drawable.ic_cart_pinkkelabu);
        }
    }

    @Override
    public int getItemCount() {
        return orderedItemArrayList.size(); //return list size
    }


    //view holder class
    class HolderOrderedItem extends RecyclerView.ViewHolder{

        //views of row_ordered_item.xml
        private TextView itemNameTv,itemPriceTv,itemPriceEachTv,itemQuantityTv;
        private ImageView icon;

        public HolderOrderedItem(@NonNull View itemView) {
            super(itemView);

            //init views
            itemNameTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            icon = itemView.findViewById(R.id.imageView);

        }
    }
}
