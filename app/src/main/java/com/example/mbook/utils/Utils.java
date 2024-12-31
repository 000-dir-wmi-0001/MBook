package com.example.mbook.utils;

import static android.app.PendingIntent.getActivity;
import static android.service.controls.ControlsProviderService.TAG;

import android.content.Intent;
import android.content.Context;

import com.example.mbook.DashboardActivity;
import com.example.mbook.LoginActivity;

import com.example.mbook.MainActivity;
import com.example.mbook.SignUpActivity;
import com.example.mbook.home;

public class Utils {

    // Static method to go to SignUpActivity
    public static void goToSignup(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }

    // Static method to go to LoginActivity
    public static void goToLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }
    public static void goToLanding(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }
    public static void HomePage(Context context){
        Intent intent = new Intent(context, home.class);
        context.startActivity(intent);
    }

    public static void AdminDashboard(Context context) {
        Intent intent = new Intent(context, DashboardActivity.class);
        context.startActivity(intent);
    }



}

