package com.iazkue.kontuapp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Context context;
    private Thread.UncaughtExceptionHandler defaultUEH;

    public CustomExceptionHandler(Context context) {
        this.context = context;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String stackTrace = sw.toString();

        // Save the stack trace to a file
        File logFile = new File(context.getExternalFilesDir(null), "error_log.txt");
        try (FileOutputStream fos = new FileOutputStream(logFile)) {
            fos.write(stackTrace.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Call the default handler
        defaultUEH.uncaughtException(thread, throwable);
    }
}
