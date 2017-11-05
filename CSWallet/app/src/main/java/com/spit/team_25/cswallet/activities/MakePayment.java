package com.spit.team_25.cswallet.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.spit.team_25.cswallet.R;
import com.spit.team_25.cswallet.models.Transactions;
import com.spit.team_25.cswallet.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

interface CallbackUserCred {
    void onComplete(HashMap<String,User> list, String number, int Amount);
}
public class MakePayment extends AppCompatActivity implements OnClickListener, CallbackUserCred {
    //     mAuth;
    User user;
    Transactions transaction;
    EditText amt;

    EditText phone;
    TextView textView;
    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment);
        Integer Amount = 0;
        Button payButton = (Button) findViewById(R.id.btn_pay);
        payButton.setOnClickListener(this);
        amt = (EditText) findViewById(R.id.amount);
        phone = (EditText) findViewById(R.id.receiver_contact);
        textView = (TextView) findViewById(R.id.receiver_name);
        mAuth= FirebaseAuth.getInstance();
        user = (User) getIntent().getSerializableExtra("Currentuser");

        String[] params = getIntent().getStringArrayExtra("Extra");
        if (!params[0].equals("null")) {
            try {
                Amount = Integer.valueOf(params[0]);
            } catch (Exception e) {
                Log.e("parse error", e.toString());
            }

            amt.setText(Amount.toString());
        }
        if (!params[1].equals("null")) {
            String Receiver = params[1];


            textView.setText("Please enter " + Receiver + " 's Phone Number");
        }
    }

    @Override
    public void onComplete(HashMap<String,User> list, String number, int Amount) {
        for(final Map.Entry<String, User> u : list.entrySet()){
            if(number.equals(u.getValue().getPhone())){
                //Sender  TID, Status, Transaction_with, Amount, Timestamp
                ArrayList<Transactions> arrayList = new ArrayList<>();

                int temp = Integer.valueOf(user.getBalance()) - Amount;
                user.setBalance(Integer.toString(temp));
                Random r = new Random();
                String TID = Integer.toString(r.nextInt( Integer.MAX_VALUE ) + 1);
                transaction = new Transactions(TID, "paid", u.getValue().getName(), Integer.toString(Amount), Long.toString(System.currentTimeMillis()));
                mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("balance").setValue(user.getBalance());
                mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("transaction").child(transaction.getTID()).setValue(transaction);
//                mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).setValue(user);
                //Receiver
                arrayList.add(transaction);

                temp = Integer.valueOf(u.getValue().getBalance()) + Amount;
                u.getValue().setBalance(Integer.toString(temp));
                mDatabase.child("users").child(u.getKey()).child("balance").setValue(u.getValue().getBalance());
                transaction = new Transactions(TID, "Received", user.getName(), Integer.toString(Amount), Long.toString(System.currentTimeMillis()));
                mDatabase.child("users").child(u.getKey()).child("transaction").child(transaction.getTID()).setValue(transaction);

                Intent intent = new Intent(MakePayment.this, TransactionDetailActivity.class);
                intent.putExtra("transaction", arrayList);
                intent.putExtra("position", 0);
                intent.putExtra("Caller", "Payment");
                startActivity(intent);
                return;
            }
        }
        TextView error = (TextView) findViewById(R.id.payment_error);
        error.setText("invalid no.");
    }

    public void validateCredentials(String name, final String number, final int Amount, @NonNull final CallbackUserCred callback){
        mDatabase= FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child("users").orderByChild("name").equalTo(name);
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                HashMap<String,User> list = new HashMap<String, User>();
                for(DataSnapshot usr : dataSnapshot.getChildren()) {
                    list.put(usr.getKey(), usr.getValue(User.class));
                    Log.e("for loop", usr.getKey()+" "+usr.getValue(User.class).getName());
                }

                callback.onComplete(list, number, Amount);

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                TextView error = (TextView) findViewById(R.id.payment_error);
                error.setText("Invalid Name");
            }
        });
    }

    @Override
    public void onClick(View v) {
        String name = textView.getText().toString().split(" ")[2];
        Log.e("receiver",name);
        String number = phone.getText().toString();
        int Amount = Integer.valueOf(amt.getText().toString());

        int user_balance = Integer.valueOf(user.getBalance());
        if(Amount > user_balance){
            TextView error = (TextView) findViewById(R.id.payment_error);
            error.setText("InSufficient Balance");
        }
        else
            if(isOnline())
                validateCredentials(name, number,Amount,this);
            else
                Toast.makeText(getApplicationContext(), "You are Offline! Turn on your network!", Toast.LENGTH_LONG).show();
    }

    private boolean isOnline() {
        // Connectivity manager gives you access to the current state of the connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }
}