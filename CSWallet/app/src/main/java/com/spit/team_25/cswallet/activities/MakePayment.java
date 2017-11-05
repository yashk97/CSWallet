package com.spit.team_25.cswallet.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

interface CallbackUserCred {
    User onComplete(ArrayList<User> list, String number);
}
public class MakePayment extends AppCompatActivity implements OnClickListener, CallbackUserCred {
    private FirebaseAuth mAuth;
    private User user;
    private Transactions transactions;
    EditText amt;
    EditText phone;
    final TextView textView = (TextView) findViewById(R.id.receiver_name);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment);
        Integer Amount = 0;
        amt = (EditText) findViewById(R.id.amount);
        phone = (EditText) findViewById(R.id.receiver_contact);
        Button payButton = (Button) findViewById(R.id.btn_pay);
        payButton.setOnClickListener(this);

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
    public User onComplete(ArrayList<User> list, String number) {
        for(User u : list){
            if(number.equals(u.getPhone())){

            }
        }
        return null;
    }

    public void validateCredentials(String name, final String number, @NonNull final CallbackUserCred callback){
        DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        Query query = mFirebaseDatabaseReference.child("users").orderByChild("name").equalTo(name);
        query.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                ArrayList<User> list = new ArrayList<>();
                for(DataSnapshot usr : dataSnapshot.getChildren()){
                    list.add(usr.getValue(User.class));
                }
                callback.onComplete(list,number);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    public void onClick(View v) {

        String name = textView.getText().toString().split(" ")[2];
        String number = phone.getText().toString();
        int Amount = Integer.valueOf(amt.getText().toString());

        int user_balance = Integer.valueOf(user.getBalance());
        if(Amount < user_balance){
            TextView error = (TextView) findViewById(R.id.payment_error);
            error.setText("InSufficient Balance");
        }

        validateCredentials(name, number,this);
    }
}