package de.hfu.ashiqmoh.cardiaccustodian;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class EndCallListener extends PhoneStateListener {

    private static final String TAG ="EndCallListener";

    private Context mContext;
    private boolean called = false;
    //protected static boolean backToMain = false;

    public EndCallListener(Context context) {
        this.mContext = context;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        if(TelephonyManager.CALL_STATE_RINGING == state) {
            Log.i(TAG, "RINGING, number: " + incomingNumber);
        }
        if(TelephonyManager.CALL_STATE_OFFHOOK == state) {
            called = true;
            Log.i(TAG, "OFFHOOK");
        }
        if(TelephonyManager.CALL_STATE_IDLE == state) {
            Log.i(TAG, "IDLE");
            if (called) {
                called = false;
                FirstAidActivity.mBackToMain = true;
                Intent intent = new Intent(mContext, FirstAidActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        }
    }
}
