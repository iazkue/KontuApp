package com.iazkue.kontuapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountDetailActivity extends AppCompatActivity {
    private static final int ADD_ITEM_REQUEST_CODE = 1;
    private AppDatabase db;
    private LinearLayout participantsLayout;
    private int accountId;
    private int societyId;
    private TextView textViewItemSummary;
    private TextView textViewTotalDiff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_detail);

        db = AppDatabase.getDatabase(getApplicationContext());

        participantsLayout = findViewById(R.id.participants_layout);
        textViewItemSummary = findViewById(R.id.text_view_item_summary);
        textViewTotalDiff = findViewById(R.id.text_view_total_diff);

        accountId = getIntent().getIntExtra("ACCOUNT_ID", -1);
        if (accountId != -1) {
            Account account = db.accountDao().getAccountById(accountId);
            societyId = account.societyId;
            loadParticipants();
            loadItemSummary();
            loadTotalDiff();
        }
    }

    private void loadParticipants() {
        participantsLayout.removeAllViews(); // Clear existing views
        Account account = db.accountDao().getAccountById(accountId);
        if (account != null) {
            String[] participants = account.participants.split("[,-]"); // Split by commas or hyphens
            for (String participant : participants) {
                loadParticipantDetails(participant.trim());
            }
        }
    }

    private void loadParticipantDetails(String participant) {
        List<AccountDetail> accountDetails = db.accountDetailDao().getAccountDetailsByParticipant(accountId, participant);
        addParticipantView(participant, accountDetails);
    }

    private void addParticipantView(String participant, List<AccountDetail> details) {
        View participantView = getLayoutInflater().inflate(R.layout.participant_detail, participantsLayout, false);
        TextView textViewParticipantName = participantView.findViewById(R.id.text_view_participant_name);
        LinearLayout itemsLayout = participantView.findViewById(R.id.items_layout);
        TextView textViewTotal = participantView.findViewById(R.id.text_view_total);

        textViewParticipantName.setText(participant);
        double total = 0;

        textViewParticipantName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountDetailActivity.this, AddItemActivity.class);
                intent.putExtra("ACCOUNT_ID", accountId);
                intent.putExtra("PARTICIPANT", participant);
                startActivityForResult(intent, ADD_ITEM_REQUEST_CODE);
            }
        });

        for (AccountDetail detail : details) {
            Item item = db.itemDao().getItemById(detail.itemId);
            if (item != null) { // Ensure the item is not null
                ItemPrice itemPrice = db.itemPriceDao().getLastPrice(societyId, item.id);
                if (itemPrice != null) {
                    View itemView = getLayoutInflater().inflate(R.layout.item_detail, itemsLayout, false);
                    TextView textViewItemName = itemView.findViewById(R.id.text_view_item_name);
                    TextView textViewQuantity = itemView.findViewById(R.id.text_view_quantity);
                    TextView textViewItemTotal = itemView.findViewById(R.id.text_view_item_total);

                    double itemTotal = itemPrice.price * detail.quantity;
                    total += itemTotal;

                    textViewItemName.setText(item.name);
                    textViewQuantity.setText(String.valueOf(detail.quantity));
                    textViewItemTotal.setText(String.format("%.2f €", itemTotal));

                    itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            db.accountDetailDao().delete(detail);
                            itemsLayout.removeView(itemView);
                            double newTotal = recalculateTotal(participant);
                            textViewTotal.setText(String.format("Guztira: %.2f €", newTotal));
                            loadItemSummary();
                            loadTotalDiff();
                            Toast.makeText(AccountDetailActivity.this, "Zuzenki ezabatua", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                    });

                    itemsLayout.addView(itemView);
                } else {
                    Toast.makeText(this, "Errorea: ezin dira produktuen prezioak zuzenki kargatu", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Errorea: ezin dira produktuen ezaugarriak zuzenki kargatu", Toast.LENGTH_SHORT).show();
            }
        }

        textViewTotal.setText(String.format("Guztira: %.2f €", total));

        participantsLayout.addView(participantView);
    }

    private double recalculateTotal(String participant) {
        double total = 0;
        List<AccountDetail> accountDetails = db.accountDetailDao().getAccountDetailsByParticipant(accountId, participant);
        for (AccountDetail detail : accountDetails) {
            Item item = db.itemDao().getItemById(detail.itemId);
            if (item != null) {
                ItemPrice itemPrice = db.itemPriceDao().getLastPrice(societyId, item.id);
                if (itemPrice != null) {
                    total += itemPrice.price * detail.quantity;
                }
            }
        }
        return total;
    }

    private void loadItemSummary() {
        List<AccountDetail> accountDetails = db.accountDetailDao().getAccountDetailsByAccountId(accountId);
        Map<String, Integer> itemSummary = new HashMap<>();

        for (AccountDetail detail : accountDetails) {
            Item item = db.itemDao().getItemById(detail.itemId);
            if (item != null) {
                String itemName = item.name;
                int quantity = detail.quantity;
                if (itemSummary.containsKey(itemName)) {
                    itemSummary.put(itemName, itemSummary.get(itemName) + quantity);
                } else {
                    itemSummary.put(itemName, quantity);
                }
            }
        }

        StringBuilder summaryText = new StringBuilder();
        for (Map.Entry<String, Integer> entry : itemSummary.entrySet()) {
            summaryText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        textViewItemSummary.setText(summaryText.toString());
    }

    private void loadTotalDiff() {
        Account account = db.accountDao().getAccountById(accountId);
        if (account != null) {
            double totalCost = account.totalCost;
            double totalConsumed = 0;
            List<AccountDetail> accountDetails = db.accountDetailDao().getAccountDetailsByAccountId(accountId);
            for (AccountDetail detail : accountDetails) {
                Item item = db.itemDao().getItemById(detail.itemId);
                if (item != null) {
                    ItemPrice itemPrice = db.itemPriceDao().getLastPrice(societyId, item.id);
                    if (itemPrice != null) {
                        totalConsumed += itemPrice.price * detail.quantity;
                    }
                }
            }
            double totalDiff = totalCost - totalConsumed;
            textViewTotalDiff.setText(String.format("Faltan: %.2f - %.2f = %.2f €", totalConsumed, totalCost, -totalDiff));
            if (-totalDiff < 0) {
                textViewTotalDiff.setTextColor(Color.RED);
            } else {
                textViewTotalDiff.setTextColor(Color.GREEN);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_ITEM_REQUEST_CODE && resultCode == RESULT_OK) {
            loadParticipants(); // Reload participants to reflect the changes
            loadItemSummary(); // Reload item summary
            loadTotalDiff(); // Reload total difference
        }
    }
}
