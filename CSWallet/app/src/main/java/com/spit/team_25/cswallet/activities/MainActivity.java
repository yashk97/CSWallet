package com.spit.team_25.cswallet.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spit.team_25.cswallet.R;
import com.spit.team_25.cswallet.adapters.BuildConfig;
import com.spit.team_25.cswallet.adapters.MyAdapter;
import com.spit.team_25.cswallet.models.GoogleSessionManager;
import com.spit.team_25.cswallet.models.Message;
import com.spit.team_25.cswallet.models.User;

import java.util.ArrayList;
import java.util.Date;

import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import static android.widget.Toast.makeText;
import static com.spit.team_25.cswallet.R.id.sendButton;

interface CallbackUserDetail {
    void onComplete(User user);
}

public class MainActivity extends AppCompatActivity implements OnClickListener, CallbackUserDetail {

    private int in_index = 0;
    private Adapter mAdapter = null;
    private RecyclerView messageList;
    private EditText messageText;
    private ArrayList messages = null;
    private boolean doubleBackToExitPressedOnce = false;
    private FirebaseAuth mAuth;
    private User user;
    private String ACCESS_TOKEN = "552c5f6b810d4aeb89880beead79ac11";
    private android.content.Context context;
    private AIDataService aiDataService;
    private AIRequest aiRequest;
    final AIConfiguration config = new AIConfiguration(ACCESS_TOKEN,
            AIConfiguration.SupportedLanguages.English,
            AIConfiguration.RecognitionEngine.System);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setTitle(R.string.chat_bot);
        loadUserData(this);

        Intent intent = new Intent(this, TransactionActivity.class);
        startActivity(intent);

        ImageButton sendButton = (ImageButton) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
        this.messageText = (EditText) findViewById(R.id.messageText);
        this.messages = new ArrayList();
        this.mAdapter = new MyAdapter(this, this.messages);


        this.messageList = (RecyclerView) findViewById(R.id.messageList);
        this.messageList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(1);
        this.messageList.setLayoutManager(llm);
        this.messageList.setAdapter(this.mAdapter);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case sendButton /*2131558507*/:
                String messString = this.messageText.getText().toString();
                if (!messString.equals(BuildConfig.FLAVOR)) {
                    this.messages.add(new Message(BuildConfig.FLAVOR, messString, true, new Date()));
                    this.mAdapter.notifyDataSetChanged();
                    sendMessage(messString);
                    this.messageText.setText(BuildConfig.FLAVOR);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void sendMessage(String messString) {
//        String[] incoming = new String[]{getBalance(), "Hey, How's it going?", "Super! Let's do lunch tomorrow", "How about Mexican?", "Great, I found this new place around the corner", "Ok, see you at 12 then!", getBalance()};
//        if (this.in_index < incoming.length) {
//            this.messages.add(new Message("John", incoming[this.in_index], false, new Date()));
//            this.in_index++;
//        }
//        this.messageList.scrollToPosition(this.messages.size() - 1);
//        this.mAdapter.notifyDataSetChanged();

//        https://github.com/dialogflow/dialogflow-android-client
        aiDataService = new AIDataService(getApplicationContext(), config);
        aiRequest = new AIRequest();
        aiRequest.setQuery(messString);
        AIResponse response= new AIResponse();
        new TextProcessor().execute(aiRequest);
//        try {
//            new TextProcessor().execute(aiRequest);
//        }catch(Exception e){
////        {   Log.e("status","dint work");
//            Log.e("error",e.toString());
//        }
//        finally {
//            processResponse(response);
//        }
    }

    public void processResponse(AIResponse response){
        final ai.api.model.Status status = response.getStatus();
        Log.e("Status",status.getErrorType());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            this.finish();
        } else {
            doubleBackToExitPressedOnce = true;
            makeText(this, "Press Again To Exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    private void loadUserData(@NonNull final CallbackUserDetail callback)
    {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user != null)
                    callback.onComplete(user);
                else
                    signOut();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    public void onComplete(User user) {
        this.user = user;
    }

    private void signOut() {
        if(isOnline()) {
            mAuth.signOut();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            if (sharedPreferences.getString("method", "").equals("Google")) {
                final GoogleApiClient mGoogleApiClient = new GoogleSessionManager(getApplicationContext()).mGoogleApiClient;
                mGoogleApiClient.connect();
                mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        if (mGoogleApiClient.isConnected())
                            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                                @Override
                                public void onResult(@NonNull Status status) {
                                    if (status.isSuccess()) {
                                        makeText(getApplicationContext(), "Logged Out!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(intent);
                                        setLoginStatus(getApplicationContext());
                                        MainActivity.this.finish();
                                    }
                                }
                            });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        makeText(getApplicationContext(), "Error! Please Try Again Later", Toast.LENGTH_LONG).show();
                    }
                });
            }

            makeText(getApplicationContext(), "Logged Out!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            setLoginStatus(this);
            MainActivity.this.finish();
        }
        else
            Toast.makeText(this, "You Are Offline", Toast.LENGTH_SHORT).show();

    }

    private void setLoginStatus(Context context) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("status", "");
        editor.putString("method", "");
        editor.apply();
    }

    // This method checks to see if the device is online. Returns true if online, else false
    private boolean isOnline() {
        // Connectivity manager gives you access to the current state of the connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    private String getBalance() {
        return user.getBalance();
    }


    private class TextProcessor extends AsyncTask<AIRequest, Void, AIResponse> {

        @Override
        protected AIResponse doInBackground(AIRequest... requests) {
            final AIRequest request = requests[0];
            try {
                return aiDataService.request(aiRequest);
            }catch (AIServiceException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(AIResponse aiResponse) {
            if (aiResponse != null) {
                //process aiResponse here
                final Result result = aiResponse.getResult();
            }
        }
    }
}