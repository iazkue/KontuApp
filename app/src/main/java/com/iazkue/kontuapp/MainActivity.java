package com.iazkue.kontuapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_WRITE_STORAGE = 1;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private RecyclerView recyclerView;
    private AccountAdapter accountAdapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verificar y solicitar permisos de almacenamiento
        checkStoragePermissions();

        // Set the custom exception handler
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        recyclerView = findViewById(R.id.recycler_view_accounts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabase.getDatabase(getApplicationContext());

        FloatingActionButton fabAddAccount = findViewById(R.id.fab_add_account);
        fabAddAccount.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewAccountActivity.class);
            startActivity(intent);
        });

        loadAccounts();
    }

    private void checkStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showErrorLog() {
        StringBuilder log = new StringBuilder();
        try {
            FileInputStream fis = openFileInput("error_log.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            while ((line = reader.readLine()) != null) {
                log.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new AlertDialog.Builder(this)
                .setTitle("Error Log")
                .setMessage(log.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void loadAccounts() {
        List<Account> accountList = db.accountDao().getAllAccounts();
        accountAdapter = new AccountAdapter(accountList, this);
        recyclerView.setAdapter(accountAdapter);
    }

    private void checkForUpdate() {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        int minRequiredVersion = (int) mFirebaseRemoteConfig.getLong("min_required_version");
                        int currentVersion = BuildConfig.VERSION_CODE;

                        if (currentVersion < minRequiredVersion) {
                            showUpdateDialog();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Error fetching config", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showUpdateDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Nueva versión disponible")
                .setMessage("Debes actualizar la aplicación para continuar.")
                .setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String appPackageName = getPackageName(); // Obtener el nombre del paquete actual
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                })
                .setCancelable(false) // No permitir que el diálogo sea cancelado
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAccounts(); // Reload accounts when returning to this activity
    }
}
