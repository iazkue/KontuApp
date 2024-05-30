package com.iazkue.kontuapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddSocietyActivity extends AppCompatActivity {
    private EditText editTextSocietyName;
    private Button buttonSaveSociety;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_society);

        db = AppDatabase.getDatabase(getApplicationContext());

        editTextSocietyName = findViewById(R.id.edit_text_society_name);
        buttonSaveSociety = findViewById(R.id.button_save_society);

        buttonSaveSociety.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSociety();
            }
        });
    }

    private void saveSociety() {
        String societyName = editTextSocietyName.getText().toString();
        if (societyName.isEmpty()) {
            Toast.makeText(this, "Please enter a society name", Toast.LENGTH_SHORT).show();
            return;
        }

        Society society = new Society();
        society.name = societyName;

        db.societyDao().insert(society);

        Toast.makeText(this, "Society saved successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
