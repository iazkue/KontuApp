package com.iazkue.kontuapp;

import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultUEH;
    private final AppCompatActivity activity;

    public CustomExceptionHandler(AppCompatActivity activity) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.activity = activity;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String errorDetails = timestamp + " - " + Log.getStackTraceString(ex);

        try {
            FileOutputStream fos = activity.openFileOutput("error_log.txt", Context.MODE_APPEND);
            fos.write((errorDetails + "\n").getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Pass the exception to the default handler after logging
        defaultUEH.uncaughtException(thread, ex);
    }
}
