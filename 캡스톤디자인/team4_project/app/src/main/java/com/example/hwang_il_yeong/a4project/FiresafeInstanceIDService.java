package com.example.hwang_il_yeong.a4project;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class FiresafeInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFiresafeIIDService";

    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();


    }

}
