package com.spit.team_25.cswallet.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.spit.team_25.cswallet.R;
import com.spit.team_25.cswallet.models.Transactions;

import java.text.DateFormat;
import java.util.ArrayList;

abstract class TransactionListViewHolder extends RecyclerView.ViewHolder {

    public TextView title, date, time, amount;
    public ImageView statusImg;

    TransactionListViewHolder(View view) {
        super(view);
        this.title = (TextView) view.findViewById(R.id.cardTitle);
        this.date = (TextView) view.findViewById(R.id.tvTransactionDate);
        this.time = (TextView) view.findViewById(R.id.tvTransactionTime);
        this.amount = (TextView) view.findViewById(R.id.tvTransactionAmt);
        this.statusImg = (ImageView) view.findViewById(R.id.ivMoneyDirection);
    }
}

public class TransactionListViewAdapter extends RecyclerView.Adapter<TransactionListViewHolder> {
    private ArrayList<Transactions> arrayList;


    public TransactionListViewAdapter(ArrayList<Transactions> arrayList) {
        this.arrayList = arrayList;
    }

    @Override
    public int getItemCount() {
        return (null != arrayList ? arrayList.size() : 0);
    }

    @Override
    public void onBindViewHolder(final TransactionListViewHolder holder, int position) {

        //Setting text over textview
        if(arrayList.get(position).getStatus().equals("paid")) {
            holder.title.setText("Paid to " + arrayList.get(position).getTransaction_with());
            holder.statusImg.setImageResource(R.drawable.ic_paid);
        }
        else {
            holder.title.setText("Received Cash From " + arrayList.get(position).getTransaction_with());
            holder.statusImg.setImageResource(R.drawable.ic_receive);
        }
        holder.date.setText(DateFormat.getDateInstance().format(Long.parseLong(arrayList.get(position).getTimestamp())));
        holder.time.setText(DateFormat.getTimeInstance().format(Long.parseLong(arrayList.get(position).getTimestamp())));
        holder.amount.setText(arrayList.get(position).getAmount());
    }

    @Override
    public TransactionListViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater mInflater = LayoutInflater.from(viewGroup.getContext());

        ViewGroup mainGroup = (ViewGroup) mInflater.inflate(R.layout.fragment_transaction_list_title, viewGroup, false);

        return new TransactionListViewHolder(mainGroup) {
            @Override
            public String toString() {
                return super.toString();
            }
        };
    }
}