package com.example.tripreminderapp.reminder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.tripreminderapp.HomeActivity;
import com.example.tripreminderapp.R;
import com.example.tripreminderapp.ui.trip_details.TripDetailsActivity;

import java.io.IOException;

public class MyService extends Service {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.y_sabah_el_ro3b);
        try {

            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mediaPlayer.prepare();//Let the Mediaplayer enter the ready state
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        String date_time =intent.getStringExtra("date_time");
//        List<Trip> trips= TripDatabase.getInstance(getApplicationContext()).tripDao().getTrip(date_time);
//        Trip trip = trips.get(0);

        RemoteViews customView =new RemoteViews(getPackageName(), R.layout.notification_reminder);
        Intent notificationIntent =new Intent(getApplicationContext(), HomeActivity.class);
        Intent hungupIntent =new Intent(getApplicationContext(), MyReceiver.class);
        Intent answerIntent = new Intent(this, TripDetailsActivity.class);
        //answerIntent.putExtra(UpcomingTripsFragment.UPCOMING_DETAILS_EXTRA,trip);

        customView.setTextViewText(R.id.tripEndPoint, "end");
        customView.setTextViewText(R.id.tripName, "name");
        //customView.setImageViewBitmap(R.id.photo, NotificationImageManager().getImageBitmap(intent.getStringExtra("user_thumbnail_image")))

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent hungupPendingIntent = PendingIntent.getBroadcast(this, 0, hungupIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent answerPendingIntent = PendingIntent.getActivity(this, 0, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        customView.setOnClickPendingIntent(R.id.btnStart, answerPendingIntent);
        customView.setOnClickPendingIntent(R.id.btnDismiss, hungupPendingIntent);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel =new NotificationChannel("IncomingCall",
                    "IncomingCall", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(this, "IncomingCall");
            notification.setContentTitle("reminder");
            notification.setTicker("Call_STATUS");
            notification.setContentText("IncomingCall");
            notification.setSmallIcon(R.drawable.add);
            notification.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
            notification.setCategory(NotificationCompat.CATEGORY_CALL);
            notification.setVibrate(null);
            notification.setOngoing(true);
            notification.setSound(soundUri);
            notification.setFullScreenIntent(pendingIntent, true);
            notification.setPriority(NotificationCompat.PRIORITY_HIGH);
            notification.setStyle(new NotificationCompat.DecoratedCustomViewStyle());
            notification.setCustomContentView(customView);
            notification.setCustomBigContentView(customView);

            startForeground(1124, notification.build());
        } else {
            NotificationCompat.Builder notification =new NotificationCompat.Builder(this);
            notification.setContentTitle("app_name");
            notification.setTicker("Call_STATUS");
            notification.setContentText("IncomingCall");
            notification.setSmallIcon(R.drawable.add);
            notification.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.add));
            notification.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
            notification.setVibrate(null);
            notification.setSound(soundUri);
            notification.setContentIntent(pendingIntent);
            notification.setOngoing(true);
            notification.setCategory(NotificationCompat.CATEGORY_CALL);
            notification.setPriority(NotificationCompat.PRIORITY_HIGH);
            NotificationCompat.Action hangupAction =new NotificationCompat.Action.Builder(android.R.drawable.sym_action_chat, "HANG UP", hungupPendingIntent)
                    .build();
            notification.addAction(hangupAction);
            startForeground(1124, notification.build());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer!=null)mediaPlayer.stop();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }
}