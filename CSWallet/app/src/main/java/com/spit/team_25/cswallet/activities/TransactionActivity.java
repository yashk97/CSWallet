package com.spit.team_25.cswallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.spit.team_25.cswallet.R;
import com.spit.team_25.cswallet.adapters.RecyclerItemClickListener;
import com.spit.team_25.cswallet.adapters.TransactionListViewAdapter;
import com.spit.team_25.cswallet.models.Transactions;

import java.util.ArrayList;

public class TransactionActivity extends AppCompatActivity {

    public static final long FIND_SUGGESTION_SIMULATED_DELAY = 250;

    private FloatingSearchView mSearchView;
    private AppBarLayout mAppBar;

    private boolean mIsDarkSearchTheme = false;

    private String mLastQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        TextView textView = (TextView) findViewById(R.id.tvBalance);
        textView.setText("5000");

        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        mAppBar = (AppBarLayout) findViewById(R.id.appBarLayout);

        setRecyclerView();
    }

    private void setRecyclerView() {

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvTransaction);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final ArrayList<Transactions> arrayList = ((ArrayList<Transactions>) getIntent().getSerializableExtra("transaction"));
//        Transactions transaction = new Transactions();
//        transaction.setTID("21312314");
//        transaction.setStatus("paid");
//        transaction.setTimestamp(Long.toString(System.currentTimeMillis()));
//        transaction.setAmount("500");
//        transaction.setTransaction_with("Yash");
//        arrayList.add(transaction);
//
//        transaction = new Transactions();
//        transaction.setTID("464647324");
//        transaction.setStatus("Recieved");
//        transaction.setTimestamp(Long.toString(System.currentTimeMillis()));
//        transaction.setAmount("5000");
//        transaction.setTransaction_with("Rahul");
//        arrayList.add(transaction);

        if (arrayList == null || arrayList.size() == 0)
            Toast.makeText(getApplicationContext(), "No Transactions Found!", Toast.LENGTH_LONG).show();
        else {
            TransactionListViewAdapter adapter = new TransactionListViewAdapter(arrayList);
            recyclerView.setAdapter(adapter);

            recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent intent = new Intent(TransactionActivity.this, TransactionDetailActivity.class);
                    intent.putExtra("transaction", arrayList);
                    intent.putExtra("position", position);
                    startActivity(intent);
                }
            }));
        }
    }
}