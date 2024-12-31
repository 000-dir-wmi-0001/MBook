package com.example.mbook;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase App
        FirebaseApp.initializeApp(this);
    }
}
