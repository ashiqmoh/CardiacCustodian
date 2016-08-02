package de.hfu.ashiqmoh.cardiaccustodian.gcm;


import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

import de.hfu.ashiqmoh.cardiaccustodian.gcm.GCMRegistrationIntentService;

public class GCMTokenRefreshListenerService extends InstanceIDListenerService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, GCMRegistrationIntentService.class);
        startService(intent);
    }
}
