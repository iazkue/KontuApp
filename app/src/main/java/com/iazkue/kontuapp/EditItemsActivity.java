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
    private int societyId; // Ejemplo de societyId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_items);

        db = AppDatabase.getDatabase(getApplicationContext());

        autoCompleteTextViewItems = findViewById(R.id.auto_complete_text_view_items);
        editTextQuantity = findViewById(R.id.edit_text_quantity);
        editTextPrice = findViewById(R.id.edit_text_price);
        buttonSave = findViewById(R.id.button_save);

        // Obtener societyId de algún lugar, por ejemplo, a través de un Intent
        societyId = getIntent().getIntExtra("SOCIETY_ID", -1);

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
        String itemName = autoCompleteTextViewItems.getText().toString().trim();
        String quantityStr = editTextQuantity.getText().toString();
        String priceStr = editTextPrice.getText().toString();

        if (itemName.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Mesedez, bete eremu guztiak", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(quantityStr);
        double price = Double.parseDouble(priceStr);

        Item selectedItem = db.itemDao().getItemByName(itemName);
        if (selectedItem == null) {
            selectedItem = new Item(itemName);
            db.itemDao().insert(selectedItem);
            selectedItem = db.itemDao().getItemByName(itemName); // Obtener el item con el ID generado
        }

        ItemPrice itemPrice = new ItemPrice();
        itemPrice.societyId = societyId;
        itemPrice.itemId = selectedItem.id;
        itemPrice.price = price;

        try {
            db.itemPriceDao().upsert(itemPrice);
            Toast.makeText(this, "Produktua zuzenki gehitu da", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Errorea produktua gehitzean", Toast.LENGTH_SHORT).show();
        }
    }
}
