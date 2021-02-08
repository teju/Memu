package com.memu.etc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;
import com.memu.callback.SmsListener;

/**
 * Created by ideamac on 01/03/18.
 */

public class SmsReceiver extends BroadcastReceiver {

    private static SmsListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("sender2122 message onReceive");

        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            System.out.println("sender2122 status "+status);

            switch(status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS:
                    // Get SMS message contents
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    System.out.println("sender2122 message "+message);

                    mListener.messageReceived(message);

                    String phone_number = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    break;
                case CommonStatusCodes.TIMEOUT:
                    break;
            }
        }
//        try {
//            Bundle data = intent.getExtras();
//
//            Object[] pdus = (Object[]) data.get("pdus");
//
//            for (int i = 0; i < pdus.length; i++) {
//                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdus[i]);
//
//                String sender = smsMessage.getDisplayOriginatingAddress();
//                System.out.println("sender2122 "+sender);
//                //You must check here if the sender is your provider and not another one with same text.
//                if(sender.equals("")) {
//                }
//                String messageBody = smsMessage.getMessageBody();
//
//                //Pass on the text to our listener.
//                mListener.messageReceived(messageBody);
//
//            }
//        } catch (Exception e) {
//
//        }

    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }
}
