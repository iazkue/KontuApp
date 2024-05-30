package com.iazkue.kontuapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class EditItemsActivity extends AppCompatActivity {
    private AutoCompleteTextView autoCompleteTextViewItems;
    private EditText editTextQuantity, editTextPrice;
    private Button buttonSave;
    private AppDatabase db;
    private int societyId = 1; // Example societyId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_items);

        db = AppDatabase.getDatabase(getApplicationContext());

        autoCompleteTextViewItems = findViewById(R.id.auto_complete_text_view_items);
        editTextQuantity = findViewById(R.id.edit_text_quantity);
        editTextPrice = findViewById(R.id.edit_text_price);
        buttonSave = findViewById(R.id.button_save);

        loadItems();

        autoCompleteTextViewItems.setOnItemClickListener((parent, view, position, id) -> {
            Item selectedItem = (Item) parent.getItemAtPosition(position);
            ItemPrice lastPrice = db.itemPriceDao().getLastPrice(societyId, selectedItem.id);
            if (lastPrice != null) {
                editTextPrice.setText(String.valueOf(lastPrice.price));
            } else {
                editTextPrice.setText("");
            }
        });

        buttonSave.setOnClickListener(v -> saveItem());
    }

    private void loadItems() {
        List<ItemPrice> itemPrices = db.itemPriceDao().getItemsBySociety(societyId);
        List<Item> items = new ArrayList<>();
        for (ItemPrice itemPrice : itemPrices) {
            Item item = db.itemDao().getItemById(itemPrice.itemId);
            if (item != null) {
                items.add(item);
            }
        }
        ArrayAdapter<Item> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, items);
        autoCompleteTextViewItems.setAdapter(adapter);
    }

    private void saveItem() {
        Item selectedItem = (Item) autoCompleteTextViewItems.getAdapter().getItem(autoCompleteTextViewItems.getListSelection());
        String quantityStr = editTextQuantity.getText().toString();
        String priceStr = editTextPrice.getText().toString();

        if (selectedItem == null || quantityStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(quantityStr);
        double price = Double.parseDouble(priceStr);

        // Save item price for the society
        ItemPrice itemPrice = new ItemPrice();
        itemPrice.societyId = societyId;
        itemPrice.itemId = selectedItem.id;
        itemPrice.price = price;
        db.itemPriceDao().insert(itemPrice);

        Toast.makeText(this, "Item saved successfully", Toast.LENGTH_SHORT).show();
    }
}
