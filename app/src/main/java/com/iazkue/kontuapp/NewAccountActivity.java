package com.iazkue.kontuapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewAccountActivity extends AppCompatActivity {
    private AppDatabase db;
    private EditText editTextTotalCost, editTextParticipants;
    private Spinner spinnerSociety;
    private Button buttonSaveAccount;
    private List<Society> societies;
    private Society selectedSociety;
    private static final String ADD_SOCIETY_OPTION = "SOZIETATE BAT GEHITU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        db = AppDatabase.getDatabase(getApplicationContext());

        editTextTotalCost = findViewById(R.id.edit_text_total_cost);
        editTextParticipants = findViewById(R.id.edit_text_participants);
        spinnerSociety = findViewById(R.id.spinner_society);
        buttonSaveAccount = findViewById(R.id.button_save_account);

        loadSocieties();

        spinnerSociety.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                if (ADD_SOCIETY_OPTION.equals(selected)) {
                    showAddSocietyDialog();
                } else {
                    selectedSociety = societies.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSociety = null;
            }
        });

        spinnerSociety.setOnLongClickListener(v -> {
            int position = spinnerSociety.getSelectedItemPosition();
            if (position >= 0 && position < societies.size()) {
                showDeleteSocietyDialog(position);
            }
            return true;
        });

        buttonSaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccount();
            }
        });
    }

    private void loadSocieties() {
        societies = db.societyDao().getAllSocieties();
        List<String> societyNames = new ArrayList<>();
        for (Society society : societies) {
            societyNames.add(society.name);
        }
        societyNames.add(ADD_SOCIETY_OPTION);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, societyNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSociety.setAdapter(adapter);
    }

    private void showAddSocietyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sozietatea gehitu");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Gehitu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String societyName = input.getText().toString().trim();
                if (!societyName.isEmpty()) {
                    addSociety(societyName);
                } else {
                    Toast.makeText(NewAccountActivity.this, "Sozietatearen izenak ezin du hutsik egon", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Ezeztatu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                spinnerSociety.setSelection(0); // Reset selection to first item
            }
        });

        builder.show();
    }

    private void showDeleteSocietyDialog(int position) {
        Society societyToDelete = societies.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sozietatea ezabatu");
        builder.setMessage("Seguru zaude " + societyToDelete.name + " sozietatea ezabatu nahi duzulataz?");

        builder.setPositiveButton("Ezabatu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteSociety(societyToDelete);
            }
        });
        builder.setNegativeButton("Ezeztatu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addSociety(String societyName) {
        Society newSociety = new Society();
        newSociety.name = societyName;
        db.societyDao().insert(newSociety);

        loadSocieties(); // Reload societies to include the new one
        Toast.makeText(this, "Sozietatea gehitua izan da", Toast.LENGTH_SHORT).show();
    }

    private void deleteSociety(Society society) {
        db.societyDao().delete(society);
        loadSocieties(); // Reload societies to remove the deleted one
        Toast.makeText(this, "Sozietatea ezabatua izan da", Toast.LENGTH_SHORT).show();
    }

    private void saveAccount() {
        String totalCostStr = editTextTotalCost.getText().toString();
        String participants = editTextParticipants.getText().toString();

        if (totalCostStr.isEmpty() || participants.isEmpty() || selectedSociety == null) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalCost = Double.parseDouble(totalCostStr);

        Account account = new Account();
        account.totalCost = totalCost;
        account.participants = participants.replaceAll("\\s+", ""); // Guardar la cadena de participantes sin espacios
        account.societyId = selectedSociety.id; // Guardar el ID de la sociedad
        account.dateCreated = new Date(); // Establecer la fecha y hora de creación

        db.accountDao().insert(account);

        Toast.makeText(this, "Kontua zuzenki gordea", Toast.LENGTH_SHORT).show();

        // Finish this activity and return to the main activity
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSocieties(); // Reload societies when returning to this activity
    }
}
