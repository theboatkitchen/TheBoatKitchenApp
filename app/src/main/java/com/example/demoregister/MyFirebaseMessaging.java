package com.example.demoregister;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.demoregister.admin.OrderDetailsAdminActivity;
import com.example.demoregister.customer.OrderDetailsCustomerActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private static final String NOTIFICATION_CHANNEL_ID = "MY_NOTIFICATION_CHANNEL_ID";
    //required for android 0 and above

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    String accountType;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //all notifications will be received here

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();


        //get data from notification
        String notificationType = remoteMessage.getData().get("notificationType");

        if (notificationType.equals("NewOrder")) {
            //cust buat order baru notification akan dpat kat staff dripada cartPageActivity
            String customerId = remoteMessage.getData().get("customerId");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationDescription = remoteMessage.getData().get("notificationMessage");


            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.orderByChild("userid").equalTo(firebaseAuth.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren()) {
                                accountType = "" + ds.child("accountType").getValue();
                                //String staffId = "" + ds.child("userid").getValue();
                                String staffId = firebaseAuth.getUid();
                                //keluar notification new order dekat staff
                                if (firebaseAuth.getUid() != null && accountType.equals("Staff")) {
                                    //user is signed in as staff is same user to which notification is to be sent

                                    showNotification(orderId,staffId,customerId,notificationTitle,notificationDescription, notificationType);
                                    //Toast.makeText(MyFirebaseMessaging.this, ""+orderId+""+customerId,Toast.LENGTH_LONG).show();
                                }
                                //keluar notification new order dekat admin
                                if (firebaseAuth.getUid() != null && accountType.equals("Admin")) {
                                    //user is signed in as staff is same user to which notification is to be sent

                                    showNotification(orderId,staffId,customerId,notificationTitle,notificationDescription, notificationType);
                                    //Toast.makeText(MyFirebaseMessaging.this, ""+orderId+""+customerId,Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MyFirebaseMessaging.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
        if (notificationType.equals("OrderStatusChanged")) {
            String customerId = remoteMessage.getData().get("customerId");
            String staffId = remoteMessage.getData().get("staffId");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationDescription = remoteMessage.getData().get("notificationMessage");

            if (firebaseUser != null && firebaseAuth.getUid().equals(customerId)) {
                //user is signed in as staff is same user to which notification is to be sent
                showNotification(orderId,staffId,customerId,notificationTitle,notificationDescription, notificationType);
                //Toast.makeText(MyFirebaseMessaging.this, ""+orderId+""+customerId,Toast.LENGTH_LONG).show();

            }
        }
    }

        private void showNotification(String orderId, String staffId, String customerId, String notificationTitle, String notificationDescription, String notificationType){
            //notification
            NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

            //id for notification, random
            int notificationID = new Random().nextInt(3000);

            //check if android version is 0reo/0 or above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                setupNotificationChannel(notificationManager);
            }


            //handle notification click, start order activity
            Intent intent = null;

            //amibk kat comment utube

            if (intent == null){
                intent = new Intent();
            }
            if (notificationType.equals("NewOrder")){

                //open OrderDetailsStaffActivity
                intent = new Intent(this, OrderDetailsAdminActivity.class);
                intent.putExtra("orderId",orderId);
                intent.putExtra("orderBy",customerId);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            }
            else if (notificationType.equals("OrderStatusChanged")){
                //open OrderDetailsCustomerActivity

                intent = new Intent(this, OrderDetailsCustomerActivity.class);
                intent.putExtra("orderId",orderId);
                //intent.putExtra("orderT0",staffId);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            }

            PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

            //Large icon
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.logo);

            //sound of notification
            Uri notificationSounUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            notificationBuilder.setSmallIcon(R.drawable.logo)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(notificationTitle)
                    .setContentText(notificationDescription)
                    .setSound(notificationSounUri)
                    .setAutoCancel(true) //cancel/dismiss when clicked
                    .setContentIntent(pendingIntent); //add intent

            //show notification
            notificationManager.notify(notificationID,notificationBuilder.build());
        }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "Some Sample Text";
        String channelDescription = "Channel Description here";

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        if (notificationManager != null){
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}
