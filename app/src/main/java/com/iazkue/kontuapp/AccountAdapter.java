package com.iazkue.kontuapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<Account> accountList;
    private Context context;
    private AppDatabase db;

    public AccountAdapter(List<Account> accountList, Context context) {
        this.accountList = accountList;
        this.context = context;
        this.db = AppDatabase.getDatabase(context);
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {

        Account account = accountList.get(position);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = dateFormat.format(account.dateCreated);

        holder.accountName.setText(String.format("%s | %s", db.societyDao().getSocietyById(account.societyId).getName()  .toUpperCase() , formattedDate));
        holder.accountTotalCost.setText(String.valueOf(account.totalCost));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AccountDetailActivity.class);
            intent.putExtra("ACCOUNT_ID", account.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public void setAccountList(List<Account> accountList) {
        this.accountList = accountList;
        notifyDataSetChanged();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView accountName, accountTotalCost;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            accountName = itemView.findViewById(R.id.account_name);
            accountTotalCost = itemView.findViewById(R.id.account_total_cost);
        }
    }
}
