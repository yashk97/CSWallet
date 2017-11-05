package com.spit.team_25.cswallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.spit.team_25.cswallet.R;
import com.spit.team_25.cswallet.models.Transactions;

import java.text.DateFormat;
import java.util.ArrayList;

public class TransactionDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        Intent intent = getIntent();
        int pos = intent.getIntExtra("position", 0);
        Transactions transaction = ((ArrayList<Transactions>) intent.getSerializableExtra("transaction")).get(pos);

        TextView amt = (TextView) findViewById(R.id.tvAmtDetail);
        TextView status = (TextView) findViewById(R.id.tvStatusDetail);
        TextView withVendor = (TextView) findViewById(R.id.tvWithDetail);
        TextView TID = (TextView) findViewById(R.id.tvTID);
        TextView date = (TextView) findViewById(R.id.tvTransactionDateDetail);
        TextView time = (TextView) findViewById(R.id.tvTransactionTimeDetail);
        ImageView transDetail = (ImageView) findViewById(R.id.ivTransationDetail);

        amt.setText(transaction.getAmount());
        if(transaction.getStatus().equals("paid")){
            status.setText("Paid Successfully");
            transDetail.setImageResource(R.drawable.ic_paid);
        }else{
            status.setText("Received Successfully");
            transDetail.setImageResource(R.drawable.ic_receive);
        }
        withVendor.setText(transaction.getTransaction_with());
        TID.setText(transaction.getTID());

        date.setText(DateFormat.getDateInstance().format(Long.parseLong(transaction.getTimestamp())));
        time.setText(DateFormat.getTimeInstance().format(Long.parseLong(transaction.getTimestamp())));
    }
}
