package com.example.demoregister.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoregister.R;
import com.example.demoregister.Filter.FilterProductUser;
import com.example.demoregister.ShopDetailsActivity;
import com.example.demoregister.model.CreateMenuModel;
import com.example.demoregister.model.ModelCartItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdapterMenuCustomer extends  RecyclerView.Adapter<AdapterMenuCustomer.HolderMenu>implements Filterable {

    private Context context;
    public ArrayList<CreateMenuModel> createMenuModelList, filterList;

    private FilterProductUser filter;
    private EditText search;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    ShopDetailsActivity shopDetailsActivity;

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Menu");
    DatabaseReference userCart = FirebaseDatabase.getInstance().getReference("Cart_Food");


    public AdapterMenuCustomer(Context context, ArrayList<CreateMenuModel> createMenuModelList) {
        this.context = context;
        this.createMenuModelList = createMenuModelList;
        this.filterList = createMenuModelList;
    }

    @NonNull
    @Override
    public HolderMenu onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //infalte layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_menu_customer,parent,false);
        return new HolderMenu(view);

    }

    @Override
    public void onBindViewHolder(@NonNull HolderMenu holder, int position) {

        //get data
        CreateMenuModel modelMenu = createMenuModelList.get(position);
        String menuId = modelMenu.getMenuID();
        String name = modelMenu.getMenuName();
        String description = modelMenu.getDescription();
        String category = modelMenu.getCategory();
        String price = modelMenu.getPrice();
        String icon = modelMenu.getMenuImage();
        //String uid = modelMenu.getCustid();
        String availability = modelMenu.getAvailability();

        //nk dapatkan masa klu cust place order
        //String timestamp = modelMenu.getTimeStamp();


        //set data menu untuk view kat page shopedetails

        holder.nameIv.setText(name);
        holder.descriptionIv.setText(description);
        holder.priceIv.setText(new StringBuilder("RM").append(price));

        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_cart_pinkkelabu).into(holder.productIconIv);
        }
        catch (Exception e){
            holder.productIconIv.setImageResource(R.drawable.ic_cart_pinkkelabu);
        }



        //customer tekan add to cart dekat page shope details
        holder.addToCartTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //add menu to cart
                addToCart(modelMenu);

            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show menu details
            }
        });

    }


    private double cost = 0;
    private double finalCost = 0;
    //private int quantity=0;

    String menuid,name,description,category,icon,availability,custid,cartid;
    float totalPrice,priceEach;
    int quantity;

    String getcartid;



    private void addToCart(CreateMenuModel modelMenu) {
        final String price;
        //get data from model
        //dapatkan menu id
        menuid = modelMenu.getMenuID();
        name = modelMenu.getMenuName();
        description = modelMenu.getDescription();
        category = modelMenu.getCategory();
        icon = modelMenu.getMenuImage();
        availability = modelMenu.getAvailability();
        //dapatkan current user
        custid = mAuth.getUid();
        //ambik nilai price
        price = modelMenu.getPrice();

        priceEach = Float.parseFloat(price);
        totalPrice =Float.parseFloat(price);
        quantity =1;

        //every time data stored the cartid will be unique
        cartid = userCart.push().getKey();
        getcartid = modelMenu.getMenuID();

        userCart.child(custid).child(getcartid)//daripada Cart_Food bawah custid=mauth.getUid();,bawah menu id
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) //if current customer dalam cart yg mmpunyai current cart id dh ada menuid yg dia nak add so just update quantity dengan total price shaja
                                {
                                    ModelCartItem cm = snapshot.getValue(ModelCartItem.class);
                                    final int Uquantity;
                                    float Utotal,UpriceEach;
                                    //just update quantity and total price
                                    Uquantity = cm.getQuantity()+1;
                                    UpriceEach = cm.getCost();
                                    Utotal = Uquantity*UpriceEach;

                                    Map<String,Object> updateData = new HashMap<>();
                                    updateData.put("quantity",Uquantity);
                                    updateData.put("totalprice", Utotal);

                                    userCart.child(custid).child(getcartid).updateChildren(updateData)

                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(context.getApplicationContext(),"cart updated...", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context.getApplicationContext(),""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });


                                }
                                else {

                                    ModelCartItem modelCartItem = new ModelCartItem(cartid, menuid, name, totalPrice, priceEach, quantity, icon, custid);

                                    userCart.child(custid).child(getcartid).setValue(modelCartItem)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    //dah berjaya masuk ke db
                                                    Toast.makeText(context, "Added to cart...", Toast.LENGTH_LONG).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }

                                //update cart count dekat shopeDetailsActivity
                                ((ShopDetailsActivity)context).countCartItem();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

    }


    private int itemId =1;

    @Override
    public int getItemCount() {
        return createMenuModelList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            filter = new FilterProductUser(this, filterList);
        }
        return filter;
    }

    class HolderMenu extends RecyclerView.ViewHolder {

        //ui views
        private ImageView productIconIv;
        private TextView nameIv,descriptionIv,priceIv,addToCartTv;
        private RelativeLayout menuRV;

        public HolderMenu(@NonNull View itemView) {

            super(itemView);

            //fx ni ambik nilai dari activity_shope_details


            productIconIv = itemView.findViewById(R.id.productIconIv);
            nameIv = itemView.findViewById(R.id.name);
            descriptionIv = itemView.findViewById(R.id.description);
            priceIv = itemView.findViewById(R.id.price);
            menuRV = itemView.findViewById(R.id.menuRV);


            addToCartTv = itemView.findViewById(R.id.addToCartTv);

        }
    }
}
