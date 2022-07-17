package com.example.demoregister.admin;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demoregister.R;
import com.example.demoregister.Test.ImageModel;
import com.example.demoregister.model.CreateMenuModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditMenuActivity extends AppCompatActivity {

    EditText name;
    EditText description;
    EditText category;
    EditText price;
    EditText availability;
    //Menu image;
    ImageView imageView;
    ImageButton backBtn;
    Button savebtn;

    private ProgressDialog progressDialog;

    TextView currentUser;

    Button buttonStoreDatabase;

    FirebaseAuth firebaseAuth;


    Uri uri;

    DatabaseReference databaseMenu = FirebaseDatabase.getInstance().getReference("Users");
    //create storage firebase
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    private String menuID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_menu);

        FirebaseApp.initializeApp(this);

        //nak tarik value yg dh ada dari input XML
        name = (EditText) findViewById(R.id.name);
        description = (EditText) findViewById(R.id.description);
        category = (EditText) findViewById(R.id.category);
        price = (EditText) findViewById(R.id.price);
        //untuk bahagian image
        imageView = findViewById(R.id.productImage);
        savebtn = findViewById(R.id.updateProductBtn);
        backBtn = findViewById(R.id.backBtn);
        //availability untuk nanti nak view kat cust apa yg available utk hari tu je
         availability = findViewById(R.id.availability);


        //get id of the menu from intent
        menuID = getIntent().getStringExtra("menuID");

        firebaseAuth = FirebaseAuth.getInstance();
        loadMenuDetails(); //to set on views

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);

        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pick category
                categoryDialog();
            }

            private void categoryDialog() {
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(EditMenuActivity.this);
                builder.setTitle("Menu Category")
                        .setItems(Constants.menuCategories, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                String pickcategory = Constants.menuCategories[which];

                                //set picked category
                                category.setText(pickcategory);
                            }
                        }).show();
            }
        });

        availability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //availability
                availabilityDialog();
            }

            private void availabilityDialog() {
                //dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(EditMenuActivity.this);
                builder.setTitle("Menu Availability")
                        .setItems(Availability.menuAvailability, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                String pickavailability = Availability.menuAvailability[which];

                                //set picked category
                                availability.setText(pickavailability);
                            }
                        }).show();
            }
        });

        //kalau user tekan image icon on click listener
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 2);
            }
        });

        buttonStoreDatabase = (Button) findViewById(R.id.updateProductBtn);

        //untuk check image sblum masuk ke database
        buttonStoreDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uri != null) {
                    //kalau ada url image masuk dalam realtime
                    updateImage(uri);
                } else {
                    addMenu();
                }
            }
        });

        //tekan button pergi page sblum tu
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }



    private void loadMenuDetails() {

        String user = firebaseAuth.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Menu");
        reference.child(menuID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get data
                        String id = ""+dataSnapshot.child("menuID").getValue();
                        String nameG = ""+dataSnapshot.child("menuName").getValue();
                        String descriptionG = ""+dataSnapshot.child("description").getValue();
                        String categoryG = ""+dataSnapshot.child("category").getValue();
                        String priceG = ""+dataSnapshot.child("price").getValue();
                        String menuImageG = ""+dataSnapshot.child("menuImage").getValue();
                        String availabilityG = ""+dataSnapshot.child("availability").getValue();
                        String uid = ""+dataSnapshot.child("empid").getValue();

                        //set data to views

                        name.setText(nameG);
                        description.setText(descriptionG);
                        category.setText(categoryG);
                        price.setText(priceG);
                        availability.setText(availabilityG);

                        try {
                            Picasso.get().load(menuImageG).placeholder(R.drawable.ic_add_cart_white).into(imageView);
                        }
                        catch (Exception e){
                            imageView.setImageResource(R.drawable.ic_add_cart_white);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    String image2,nameTxt,priceTxt,descriptionTxt,categoryTxt,availabilityTxt;

    //setkan variable dgn value yg user baru masukan untuk edit nanti
    private void addMenu() {

        //get all attribute and convert to string from all edittext
        nameTxt = name.getText().toString().trim();
        priceTxt = price.getText().toString().trim();
        descriptionTxt = description.getText().toString().trim();
        categoryTxt = category.getText().toString().trim();
        availabilityTxt = availability.getText().toString().trim();

        //----2) Validate data
        if (TextUtils.isEmpty(priceTxt)){
            Toast.makeText(EditMenuActivity.this, "Price is required...", Toast.LENGTH_SHORT).show();
            return;//don't proceed further
        }
        if (TextUtils.isEmpty(descriptionTxt)){
            Toast.makeText(EditMenuActivity.this, "Description is required...", Toast.LENGTH_SHORT).show();
            return;//don't proceed further
        }
        if (TextUtils.isEmpty(categoryTxt)){
            Toast.makeText(EditMenuActivity.this, "Category is required...", Toast.LENGTH_SHORT).show();
            return;//don't proceed further
        }

        //nak update without image
        updateMenu();
    }


    //update menu without image
    private void updateMenu() {
        //show progress
        progressDialog.setMessage("Updating menu...");
        progressDialog.show();

        if(image2==null){

            //setup data in hashmap to update
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("menuName", "" +nameTxt);
            hashMap.put("description", "" +descriptionTxt);
            hashMap.put("category", "" +categoryTxt);
            hashMap.put("price", "" +priceTxt);
            hashMap.put("availability", "" +availabilityTxt);


            //update to db
            String user = firebaseAuth.getUid();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Menu");
            reference.child(menuID)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //menu dah updated
                            progressDialog.dismiss();
                            Toast.makeText(EditMenuActivity.this, "Menu Updated..", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //update failed
                            progressDialog.dismiss();
                            Toast.makeText(EditMenuActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }

    }

    //update menu with image
    private void updateImage(Uri uri) {

        //ambik image file dari firebase storage
        StorageReference file = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(uri));
        file.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                file.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri){
                        //tukarkan image dari storage ke string nak store ke realtime db
                        ImageModel imageTxt = new ImageModel(uri.toString());
                        //process ambik url image dari imageTxt ke String image nak masuk dlam constructor nnti

                        //---- 1) Input Data
                        image2 = imageTxt.getImageurl();
                        //get all attribute and convert to string from all edittext
                        nameTxt = name.getText().toString().trim();
                        priceTxt = price.getText().toString().trim();
                        descriptionTxt = description.getText().toString().trim();
                        categoryTxt = category.getText().toString().trim();
                        availabilityTxt=availability.getText().toString().trim();

                        //----2) Validate data
                        if (TextUtils.isEmpty(priceTxt)){
                            Toast.makeText(EditMenuActivity.this, "Price is required...", Toast.LENGTH_SHORT).show();
                            return;//don't proceed further
                        }
                        if (TextUtils.isEmpty(descriptionTxt)){
                            Toast.makeText(EditMenuActivity.this, "Description is required...", Toast.LENGTH_SHORT).show();
                            return;//don't proceed further
                        }
                        if (TextUtils.isEmpty(categoryTxt)){
                            Toast.makeText(EditMenuActivity.this, "Category is required...", Toast.LENGTH_SHORT).show();
                            return;//don't proceed further
                        }

                        //setup data in hashmap to update
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("menuName", "" +nameTxt);
                        hashMap.put("description", "" +descriptionTxt);
                        hashMap.put("category", "" +categoryTxt);
                        hashMap.put("price", "" +priceTxt);
                        hashMap.put("menuImage", "" +image2);
                        hashMap.put("availability", "" +availabilityTxt);

                        //update to db
                        String user = firebaseAuth.getUid();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Menu");
                        reference.child(menuID)
                                .updateChildren(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        //menu dah updated
                                        progressDialog.dismiss();
                                        Toast.makeText(EditMenuActivity.this, "Menu Updated..", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //update failed
                                        progressDialog.dismiss();
                                        Toast.makeText(EditMenuActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditMenuActivity.this, "Failled!", Toast.LENGTH_SHORT).show();
            }
        });

    }



    //untuk dptkan image dari storage
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap map=MimeTypeMap.getSingleton();
        return  map.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //nak setkan url untuk image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==2 && resultCode==RESULT_OK && data!= null){
            uri=data.getData();
            imageView.setImageURI(uri);
        }
    }

}
