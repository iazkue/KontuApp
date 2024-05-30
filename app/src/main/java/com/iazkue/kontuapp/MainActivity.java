package com.iazkue.kontuapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private RecyclerView recyclerView;
    private AccountAdapter accountAdapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
