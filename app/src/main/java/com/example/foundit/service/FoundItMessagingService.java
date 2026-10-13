package com.example.foundit.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.foundit.MainActivity;
import com.example.foundit.R;
import com.example.foundit.api.RetrofitClient;
import com.example.foundit.model.ApiMessage;
import com.example.foundit.model.DeviceTokenRequest;
import com.example.foundit.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoundItMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "foundit_notifications";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            RetrofitClient.api().registerDeviceToken(session.authHeader(), new DeviceTokenRequest(token))
                    .enqueue(new Callback<ApiMessage>() {
                        @Override public void onResponse(Call<ApiMessage> c, Response<ApiMessage> r) {}
                        @Override public void onFailure(Call<ApiMessage> c, Throwable t) {}
                    });
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "FoundIT";
        String body = "You have a new update.";

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        createNotificationChannel();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        if (remoteMessage.getData() != null) {
            String type = remoteMessage.getData().get("type");
            String itemId = remoteMessage.getData().get("item_id");
            String conversationId = remoteMessage.getData().get("conversation_id");

            if (type != null) intent.putExtra("type", type);
            if (itemId != null) intent.putExtra("item_id", Integer.parseInt(itemId));
            if (conversationId != null) intent.putExtra("conversation_id", Integer.parseInt(conversationId));
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        try {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException ignored) {}
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "FoundIT Notifications";
            String description = "Notifications for FoundIT lost & found items and messages";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
