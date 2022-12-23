package com.example.demoregister.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demoregister.R;
import com.example.demoregister.EditMenuActivity;
import com.example.demoregister.Filter.Filterproduct;
import com.example.demoregister.model.CreateMenuModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterMenuAdmin extends RecyclerView.Adapter<AdapterMenuAdmin.HolderMenu>implements Filterable {

    private Context context;
    public ArrayList<CreateMenuModel> createMenuModelList, filterList;
    private Filterproduct filter;
    private EditText search;

    public AdapterMenuAdmin(Context context, ArrayList<CreateMenuModel> createMenuModelList) {
        this.context = context;
        this.createMenuModelList = createMenuModelList;
        this.filterList = createMenuModelList;
    }

    @NonNull
    @Override
    public HolderMenu onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_menu_admin, parent, false);

        return new HolderMenu(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderMenu holder, int position) {

        //get data
        CreateMenuModel modelMenu = createMenuModelList.get(position);
        String id = modelMenu.getMenuID();
        String name = modelMenu.getMenuName();
        String description = modelMenu.getDescription();
        String category = modelMenu.getCategory();
        String price = modelMenu.getPrice();
        String icon = modelMenu.getMenuImage();
        String uid = modelMenu.getEmpid();
        String availability = modelMenu.getAvailability();

        //set data
        holder.titleTv.setText(name);
        holder.descriptionTv.setText(description);
        holder.priceTv.setText(new StringBuilder("RM").append(price));
        holder.menuCategoryTv.setText(category);
        holder.availabilityTv.setText(availability);


        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_cart_pinkkelabu).into(holder.productIconIv);
        }
        catch (Exception e){
            holder.productIconIv.setImageResource(R.drawable.ic_cart_pinkkelabu);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //handle item clicks, show item details (in bottom sheet)
                detailsBottomSheet(modelMenu); //here modelMenu contains details of clicked product
            }
        });

    }

    private void detailsBottomSheet(CreateMenuModel modelMenu) {
        //bottom sheet
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        //inflate view for bottomsheet dekat xml bs_menu_details
        View view = LayoutInflater.from(context).inflate(R.layout.bs_menu_details_staff, null);
        //set view to bottomsheet
        bottomSheetDialog.setContentView(view);

        //init views of bottomSheet
        ImageButton backBtn = view.findViewById(R.id.backBtn);
        ImageButton deleteBtn = view.findViewById(R.id.btnDelete);
        ImageButton editBtn = view.findViewById(R.id.editBtn);
        ImageView productIconIv = view.findViewById(R.id.productIconIv);
        TextView nameTxt = view.findViewById(R.id.name);
        TextView descriptionTxt = view.findViewById(R.id.description);
        TextView priceTxt = view.findViewById(R.id.price);
        TextView categoryTxt = view.findViewById(R.id.category);
        TextView availabilityTxt = view.findViewById(R.id.availability);

        //get data dari database
        String id = modelMenu.getMenuID();
        String name = modelMenu.getMenuName();
        String description = modelMenu.getDescription();
        String category = modelMenu.getCategory();
        String price = modelMenu.getPrice();
        String icon = modelMenu.getMenuImage();
        String uid = modelMenu.getEmpid();
        String availability = modelMenu.getAvailability();

        //set data kpda variable nak view nanti
        nameTxt.setText(name);
        descriptionTxt.setText(description);
        priceTxt.setText(new StringBuilder("RM ").append(price));
        categoryTxt.setText(category);
        //kena edit dekat bottom sheet letak textview availability
        availabilityTxt.setText(availability);

        //get image
        try {
            Picasso.get().load(icon).placeholder(R.drawable.ic_add_cart_white).into(productIconIv);
        }
        catch (Exception e){
            productIconIv.setImageResource(R.drawable.ic_add_cart_white);
        }

        //show dialog
        bottomSheetDialog.show();

        //edit click
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open edit menu activity, pass id of menu
                bottomSheetDialog.dismiss();
                Intent intent = new Intent(context, EditMenuActivity.class);
                intent.putExtra("menuID", id);
                context.startActivity(intent);

            }
        });

        //delete click
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
                //show delete confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete")
                        .setMessage("Are you sure you want to delete this menu"+name+ "?")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //delete
                                deleteMenu(id); //id is the menu id
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
        });

        //back click
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dismiss bottom sheet
                bottomSheetDialog.dismiss();
            }
        });


    }

    private void deleteMenu(String id) {
        //delete product using its id

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Menu");
        reference.child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //menu deleted
                        Toast.makeText(context, "Menu deleted.....", Toast.LENGTH_SHORT).show();
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

    @Override
    public int getItemCount() {
        return createMenuModelList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            filter = new Filterproduct(this, filterList);
        }
        return filter;
    }


    class HolderMenu extends RecyclerView.ViewHolder{
        /*holds views of recyclerview*/

        private ImageView productIconIv;
        private TextView titleTv,priceTv,descriptionTv,menuCategoryTv,availabilityTv;

        public HolderMenu(@NonNull View itemView){
            super(itemView);

            productIconIv = itemView.findViewById(R.id.productIconIv);
            titleTv = itemView.findViewById(R.id.name);
            descriptionTv = itemView.findViewById(R.id.description);
            priceTv = itemView.findViewById(R.id.price);
            menuCategoryTv = itemView.findViewById(R.id.menucategory);
            availabilityTv = itemView.findViewById(R.id.availability);

        }

    }
}
