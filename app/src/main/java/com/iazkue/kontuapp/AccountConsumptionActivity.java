package com.iazkue.kontuapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class AccountConsumptionActivity extends AppCompatActivity {
    private AppDatabase db;
    private LinearLayout participantsLayout;
    private Button buttonSaveConsumption;
    private int accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_consumption);

        db = AppDatabase.getDatabase(getApplicationContext());

        participantsLayout = findViewById(R.id.participants_layout);
        buttonSaveConsumption = findViewById(R.id.button_save_consumption);

        accountId = getIntent().getIntExtra("ACCOUNT_ID", -1);

        if (accountId != -1) {
            loadParticipants();
        }

        buttonSaveConsumption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConsumption();
            }
        });
    }

    private void loadParticipants() {
        participantsLayout.removeAllViews(); // Clear existing views before loading new ones
        List<AccountDetail> accountDetails = db.accountDetailDao().getAccountDetailsByAccount(accountId);
        if (accountDetails.isEmpty()) {
            // Create a default participant input if none exists
            addParticipantView("", 0);
        } else {
            for (AccountDetail detail : accountDetails) {
                addParticipantView(detail.participant, detail.quantity);
            }
        }
    }

    private void addParticipantView(String participant, int quantity) {
        View participantView = getLayoutInflater().inflate(R.layout.participant_consumption, participantsLayout, false);
        EditText editTextParticipant = participantView.findViewById(R.id.edit_text_participant);
        EditText editTextConsumption = participantView.findViewById(R.id.edit_text_consumption);

        editTextParticipant.setText(participant);
        editTextConsumption.setText(String.valueOf(quantity));

        participantsLayout.addView(participantView);
    }

    private void saveConsumption() {
        db.accountDetailDao().deleteAllByAccountId(accountId); // Clear previous details
        for (int i = 0; i < participantsLayout.getChildCount(); i++) {
            View participantView = participantsLayout.getChildAt(i);
            EditText editTextParticipant = participantView.findViewById(R.id.edit_text_participant);
            EditText editTextConsumption = participantView.findViewById(R.id.edit_text_consumption);

            String participant = editTextParticipant.getText().toString();
            int consumption = Integer.parseInt(editTextConsumption.getText().toString());

            AccountDetail detail = new AccountDetail();
            detail.accountId = accountId;
            detail.participant = participant;
            detail.quantity = consumption;

            db.accountDetailDao().insert(detail);
        }
        Toast.makeText(this, "Consumption saved successfully", Toast.LENGTH_SHORT).show();
        finish(); // This will close the activity and return to the main activity
    }
}
