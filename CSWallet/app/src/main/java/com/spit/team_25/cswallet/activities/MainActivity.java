package com.spit.team_25.cswallet.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;

import com.spit.team_25.cswallet.BuildConfig;
import com.spit.team_25.cswallet.R;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    int in_index = 0;
    Adapter mAdapter = null;
    RecyclerView messageList;
    EditText messageText;
    ArrayList<Message> messages = null;
    ImageButton sendButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.sendButton = (ImageButton) findViewById(R.id.sendButton);
        this.sendButton.setOnClickListener(this);
        this.messageText = (EditText) findViewById(R.id.messageText);
        this.messages = new ArrayList<>();
        this.mAdapter = new MyAdapter(this, this.messages);
//        getSupportActionBar().setTitle(getResources().getString(R.string.app_name);
        this.messageList = (RecyclerView) findViewById(R.id.messageList);
        this.messageList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(1);
        this.messageList.setLayoutManager(llm);
        this.messageList.setAdapter(this.mAdapter);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendButton /*2131558507*/:
                String messString = this.messageText.getText().toString();
                if (!messString.equals(BuildConfig.FLAVOR)) {
                    this.messages.add(new Message(BuildConfig.FLAVOR, messString, true, new Date()));
                    this.mAdapter.notifyDataSetChanged();
                    sendMessage();
                    this.messageText.setText(BuildConfig.FLAVOR);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void sendMessage() {
        String[] incoming = new String[]{"Hey, How's it going?", "Super! Let's do lunch tomorrow", "How about Mexican?", "Great, I found this new place around the corner", "Ok, see you at 12 then!"};
        if (this.in_index < incoming.length) {
            this.messages.add(new Message("John", incoming[this.in_index], false, new Date()));
            this.in_index++;
        }
        this.messageList.scrollToPosition(this.messages.size() - 1);
        this.mAdapter.notifyDataSetChanged();
    }
}
