package com.iazkue.kontuapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class AddItemActivity extends AppCompatActivity {
    private AutoCompleteTextView autoCompleteTextViewItems;
    private EditText editTextQuantity, editTextPrice;
    private Button buttonAddItem;
    private AppDatabase db;
    private int accountId;
    private String participant;
    private int societyId;
    private List<Item> items;
    private ArrayAdapter<Item> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        db = AppDatabase.getDatabase(getApplicationContext());

        autoCompleteTextViewItems = findViewById(R.id.auto_complete_text_view_items);
        editTextQuantity = findViewById(R.id.edit_text_quantity);
        editTextPrice = findViewById(R.id.edit_text_price);
        buttonAddItem = findViewById(R.id.button_add_item);

        accountId = getIntent().getIntExtra("ACCOUNT_ID", -1);
        participant = getIntent().getStringExtra("PARTICIPANT");
        Account account = db.accountDao().getAccountById(accountId);
        societyId = account.societyId;

        loadItems();

        autoCompleteTextViewItems.setOnItemClickListener((parent, view, position, id) -> {
            Item selectedItem = (Item) parent.getItemAtPosition(position);
            ItemPrice lastPrice = db.itemPriceDao().getLastPrice(societyId, selectedItem.id);
            if (lastPrice != null) {
                editTextPrice.setText(String.format("%.2f", lastPrice.price));
            } else {
                editTextPrice.setText("");
            }
        });

        autoCompleteTextViewItems.setOnLongClickListener(v -> {
            String itemName = autoCompleteTextViewItems.getText().toString().trim();
            if (!itemName.isEmpty()) {
                Item item = db.itemDao().getItemByName(itemName);
                if (item != null) {
                    showDeleteItemDialog(item);
                }
            }
            return true;
        });

        buttonAddItem.setOnClickListener(v -> addItem());
    }

    private void loadItems() {
        items = db.itemDao().getAllItems();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, items);
        autoCompleteTextViewItems.setAdapter(adapter);
    }

    private void addItem() {
        String itemName = autoCompleteTextViewItems.getText().toString().trim();
        String quantityStr = editTextQuantity.getText().toString();
        String priceStr = editTextPrice.getText().toString().replace("€", "").trim();

        if (itemName.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Mesedez, bete eremu guztiak", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = 1; // Default quantity
        if (!quantityStr.isEmpty()) {
            quantity = Integer.parseInt(quantityStr);
        }
        double price = Double.parseDouble(priceStr);

        try {
            Item selectedItem = db.itemDao().getItemByName(itemName);
            if (selectedItem == null) {
                selectedItem = new Item(itemName);
                db.itemDao().insert(selectedItem);
                selectedItem = db.itemDao().getItemByName(itemName); // Get the item with the generated ID
                items.add(selectedItem);
                adapter.notifyDataSetChanged();
            }

            ItemPrice itemPrice = new ItemPrice();
            itemPrice.societyId = societyId;
            itemPrice.itemId = selectedItem.id;
            itemPrice.price = price;
            db.itemPriceDao().upsert(itemPrice);

            AccountDetail accountDetail = new AccountDetail();
            accountDetail.accountId = accountId;
            accountDetail.itemId = selectedItem.id;
            accountDetail.quantity = quantity;
            accountDetail.participant = participant;
            db.accountDetailDao().insert(accountDetail);

            Toast.makeText(this, "Produktua zuzenki gehitu da", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Errorea: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showDeleteItemDialog(Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ezabatu produktua");
        builder.setMessage("Ziur zaude " + item.name + " produktua ezabatu nahi duzula?");

        builder.setPositiveButton("Ezabatu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem(item);
            }
        });
        builder.setNegativeButton("Utzi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void deleteItem(Item item) {
        db.itemDao().deleteItemById(item.id);
        db.itemPriceDao().deleteItemPricesByItemId(item.id);
        loadItems(); // Reload items to remove the deleted one
        Toast.makeText(this, "Produktua ezabatuta", Toast.LENGTH_SHORT).show();
    }
}
