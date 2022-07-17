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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demoregister.R;
import com.example.demoregister.RegistrationActivity;
import com.example.demoregister.model.CreateMenuModel;
import com.example.demoregister.Test.ImageModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class CreateMenu extends AppCompatActivity {

    EditText name;
    EditText description;
    EditText category;
    EditText price;
    //Menu image;
    ImageView imageView;
    ImageButton backBtn;
    Button savebtn;

    private ProgressDialog progressDialog;

    TextView currentUser;

    Button buttonStoreDatabase;



    FirebaseAuth mAuth;
    //FirebaseUser user;
    Uri uri;

    DatabaseReference databaseMenu = FirebaseDatabase.getInstance().getReference("Users");
    //create storage firebase
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu);

        FirebaseApp.initializeApp(this);

        //Create database reference


        //nak tarik value yg dh ada dari input XML
        name = (EditText) findViewById(R.id.name);
        description = (EditText) findViewById(R.id.description);
        category = (EditText) findViewById(R.id.category);
        price = (EditText) findViewById(R.id.price);
        //untuk bahagian image
        imageView = findViewById(R.id.productImage);
        savebtn = findViewById(R.id.addProductBtn);
        backBtn = findViewById(R.id.backBtn);

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
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateMenu.this);
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



        mAuth = FirebaseAuth.getInstance();
        //user = mAuth.getCurrentUser();

       // currentUser.setText(user.getEmail());

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

        buttonStoreDatabase = (Button) findViewById(R.id.addProductBtn);

        //untuk check image sblum masuk ke database
        buttonStoreDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uri != null) {
                    //kalau ada url image masuk dalam realtime
                    addMenu(uri);
                } else {
                    Toast.makeText(CreateMenu.this, "Please Select Image", Toast.LENGTH_SHORT).show();
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

    //sama dengan uploadimagefirebase(Uri uri)
    private void addMenu(Uri uri) {

        //get all attribute and convert to string from all edittext

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
                        String image2 = imageTxt.getImageurl();
                        String nameTxt = name.getText().toString().trim();
                        String priceTxt = price.getText().toString().trim();
                        String descriptionTxt = description.getText().toString().trim();
                        String categoryTxt = category.getText().toString().trim();
                        String empidTxt = mAuth.getUid();
                        String availability = "Available";

                        //String currentUser1 = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                        //----2) Validate data
                        if (TextUtils.isEmpty(priceTxt)){
                            Toast.makeText(CreateMenu.this, "Price is required...", Toast.LENGTH_SHORT).show();
                            return;//don't proceed further
                        }
                        if (TextUtils.isEmpty(descriptionTxt)){
                            Toast.makeText(CreateMenu.this, "Description is required...", Toast.LENGTH_SHORT).show();
                            return;//don't proceed further
                        }
                        if (TextUtils.isEmpty(categoryTxt)){
                            Toast.makeText(CreateMenu.this, "Category is required...", Toast.LENGTH_SHORT).show();
                            return;//don't proceed further
                        }
                        if (TextUtils.isEmpty(image2)){
                            Toast.makeText(CreateMenu.this, "Please select image...", Toast.LENGTH_SHORT).show();
                            return;//don't proceed further
                        }

                        if (!TextUtils.isEmpty(empidTxt)) {
                            //if current user which is staff id is exits then save data to db

                            progressDialog.setMessage("Adding Menu...");
                            progressDialog.show();

                            //every time data stored the menuid will be unique
                            String menuid = databaseMenu.push().getKey();

                            //---3) Add data to db

                            CreateMenuModel mj = new CreateMenuModel(menuid, nameTxt, descriptionTxt, categoryTxt, priceTxt, image2, empidTxt, availability);

                            assert menuid != null;
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Menu");
                            //under staff or user id ada table menu, dlam ada menu id, n bwah menu id ada value
                            reference.child(menuid).setValue(mj)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            //dah berjaya masuk ke db
                                            progressDialog.dismiss();
                                            Toast.makeText(CreateMenu.this, "Menu created successfully", Toast.LENGTH_LONG).show();
                                            clearData();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(CreateMenu.this, ""+e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });

                        } else {
                            //if the empid is empty then display
                            Toast.makeText(CreateMenu.this, "Please login statff", Toast.LENGTH_LONG).show();
                           // Toast.makeText(CreateMenu.this, "Error"+ task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            return; //dont proceed further
                        }

                    }



                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CreateMenu.this, "Failled!", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void clearData() {
        //clear data after uploading menu
        name.setText("");
        description.setText("");
        category.setText("");
        price.setText("");
        imageView.setImageResource(R.drawable.ic_baseline_add_shopping_cart_24);
        uri = null;
    }
    @Override
    protected void onStart() {
        super.onStart();
        //attaching value event listener
        databaseMenu.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                //iterating through all the nodes
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    //getting artist
                    CreateMenuModel mj = postSnapshot.getValue(CreateMenuModel.class);
                    //adding artist to the list
                    //artistList.add(mj);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
