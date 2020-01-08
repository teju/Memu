package com.memu.etc;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iapps.libs.helpers.BaseHelper;
import com.memu.ActivityMain;
import com.memu.R;
import com.memu.modules.BeanGCM;


import java.util.List;

import static com.iapps.logs.com.pascalabs.util.log.helper.Helper.logEventLocal;

/**
 * Created by chanpyaeaung on 4/4/16.
 */
public class GcmService extends GcmListenerService {

    private LoggingService.Logger logger;
    private BeanGCM gcm;

    public GcmService() {
        logger = new LoggingService.Logger(this);
    }
//    Bundle[
//    {
//        google.sent_time=1486645776355,
//        tag={
//             "service_recommend_id":"5f5afcd1-68ab-473d-bd03-a0bcf5a7f998",
//            "service_type":"top_up",
//            "service_type_displayname":"Top Up",
//            "amount":"2",
//            "country_currency_code":"TK-IDR",
//            "fee":"2.00",
//            "user_name":null,
//            "address":"312 Shunfu Rd, Block 312, Singapore 570312",
//            "distance":0,
//            "message_type":"NearbyTellerServiceRecommend"
//        },
//        google.message_id=0:1486645776414855%d166883ef9fd7ecd,
//        message=SLIDE Teller: New Top Up request of Rp2,
//        collapse_key=do_not_collapse
//    }
//    ]
    /**
     * :android.permission.GET_TASKS
     *
     * @param context
     * @return
     */
    public boolean isApplicationBroughtToBackground(Context context) {
        ActivityManager am = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (tasks != null && !tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (!topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void onMessageReceived(String from, Bundle data) {

//        gcm = new BeanGCM();
//
//        try {
//            gcm.setMessage(data.getString(Keys.GCM_MESSAGE));
//        } catch (Exception e) {}
//
//        try {
//            gcm.setDt(data.getString(Keys.GCM_DT));
//        } catch (Exception e) {}
//
//        try {
//            gcm.setCollapse_key(data.getString(Keys.GCM_COLLAPSE_KEY));
//        } catch (Exception e) {}
//
//        try {
//            gcm.setT(data.getString(Keys.GCM_T));
//        } catch (Exception e) {}
//
//        try {
//            gcm.setAlert(data.getString(Keys.GCM_ALERT));
//        } catch (Exception e) {}
//
//        try {
//            gcm.setSound(data.getString(Keys.GCM_SOUND));
//        } catch (Exception e) {}
//
//        try {
//            gcm.setPushid(Integer.parseInt(data.getString(Keys.GCM_PUSHID)));
//        } catch (Exception e) {}
//
//        try {
//            gcm.setType(Integer.parseInt(data.getString(Keys.GCM_TYPE)));
//        } catch (Exception e) {}
//
//
//        sendNotification(gcm);

        String message = data.getString(Keys.GCM_MESSAGE);
        String tag = data.getString(Keys.TAG);

        logEventLocal(GcmService.this,
                "NOTIF", "onMessageReceived: tag->" + (!BaseHelper.isEmpty(tag)?tag : "N/A") + "\n\nmessage->" + (!BaseHelper.isEmpty(message)?message : "N/A"));
        //Helper.logMessage(this,"onMessageReceived: tag->" + (!Helper.isEmpty(tag)?tag : "N/A") + "\n\nmessage->" + (!Helper.isEmpty(message)?message : "N/A"));
        if(!isApplicationBroughtToBackground(getApplication())) {
//            if (TextUtils.isEmpty(tag)){
//                return;
//            }
            try {
                JsonParser parser = new JsonParser();
                JsonObject ob = (JsonObject) parser.parse(tag);
                if (ob.get(Keys.MESSAGE_TYPE).getAsString().contains(Keys.NEARBYTELLERSERVICERECOMMEND) || ob.get(Keys.MESSAGE_TYPE).getAsString().contains(Keys.NEARBYTELLERSERVICECOMPELETE)) {
                    Intent i = new Intent(Keys.UPDATE_SUPER_MAIN_ACTIVITY);
                    i.putExtra(Keys.SLIDE_TELLER_NEW_REQUEST_MESSAGE, message);
                    i.putExtra(Keys.SLIDE_TELLER_NEW_REQUEST_TAG, tag);
                    sendBroadcast(i);

                    return;
                }
            }catch (Exception e){

            }
        }
        sendNotification(message,tag);
    }


    @Override
    public void onDeletedMessages() {}

    @Override
    public void onMessageSent(String msgId) {

    }

    @Override
    public void onSendError(String msgId, String error) {

    }

//    private void sendNotification(final BeanGCM gcm) {
//
//
//        try {
//            logger.log(Log.INFO, gcm.toString());
//
//            Random random = new Random();
////            int NOTIFICATION_ID = gcmobj.getPushid();
//            int NOTIFICATION_ID = 1111;
//
//            Intent i = new Intent(this, PlacePredictionProgrammatically.class);
//            i.putExtra(Keys.TAG, Application.EXTRA_MESSAGE);
//            PendingIntent contentIntent = null;
//            contentIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, i, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            //custom notication
//            long when = System.currentTimeMillis();
//
//            Notification notification = null;
//            notification = new Notification(R.drawable.slide_logo, gcm.getMessage(), when);
//
//
//            NotificationManager mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//
//            RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.slide_custom_notification);
//            RemoteViews bigcontentView = new RemoteViews(getPackageName(), R.layout.slide_custom_longnotification);
//
//
//            contentView.setImageViewResource(R.id.image,R.drawable.slide_logo);
//            bigcontentView.setImageViewResource(R.id.image,R.drawable.slide_logo);
//
//            contentView.setTextViewText(R.id.message, gcm.getMessage());
//            notification.contentView = contentView;
//            notification.contentIntent = contentIntent;
//
//
//            bigcontentView.setTextViewText(R.id.message,gcm.getMessage());
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                notification.bigContentView = bigcontentView;
//            }
//            //notification.flags |= Notification.FLAG_NO_CLEAR; //Do not clear the notification
//            notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
//            notification.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
//            notification.defaults |= Notification.DEFAULT_SOUND; // Sound
//            notification.flags |= Notification.FLAG_AUTO_CANCEL;
//
//            ArrayList<BeanGCM> tracker;
//
//            if(!Helper.isEmpty(gcm.getMessage())) {
//                try {
//                    sendNotifAndCallVoiceOut(mNotificationManager,notification,NOTIFICATION_ID);
//                } catch (Exception e) {}
//            }
//        } catch (Exception e) {
//
//        }
//
//    }
//
//    public void sendNotifAndCallVoiceOut(NotificationManager mNotificationManager, Notification mNotification, int NOTIFICATION_ID) {
//        try {
//            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
//        } catch (Exception e) {}
//    }

    public void sendNotification(String message, String tag) {
        Intent intent = new Intent(this, ActivityMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        intent.setClass(this, PlacePredictionProgrammatically.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Keys.TAG, tag);
        intent.putExtra(Keys.SLIDE_TELLER_NEW_REQUEST_MESSAGE, message);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = null;

//        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.memu_logo)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
        ;

//        } else {
//            // Lollipop specific setColor method goes here.
//            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icn_user_notification_logo);
//            notificationBuilder = new NotificationCompat.Builder(this)
//                    .setSmallIcon(R.drawable.icn_user_notification_logo)
//                    .setLargeIcon(largeIcon)
//                    .setContentTitle(getResources().getString(R.string.app_name))
//                    .setContentText(message)
//                    .setAutoCancel(true)
//                    .setSound(defaultSoundUri)
//                    .setContentIntent(pendingIntent)
//                    .setColor(getColor(R.color.slide_primary_color))
//            ;
//        }


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify((int) System.currentTimeMillis() /* ID of notification */, notificationBuilder.build());
    }

}
