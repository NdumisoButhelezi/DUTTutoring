// MyApp.java
package com.example.dutpeertutoring;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Must be called *before* any other FirebaseDatabase.getInstance() calls
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
