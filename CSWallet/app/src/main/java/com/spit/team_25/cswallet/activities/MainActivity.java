package com.spit.team_25.cswallet.activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.TextView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonElement;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.spit.team_25.cswallet.R;
import com.spit.team_25.cswallet.adapters.BuildConfig;
import com.spit.team_25.cswallet.adapters.MessageAdapter;
import com.spit.team_25.cswallet.database.MeassgeReaderContract.MessageEntry;
import com.spit.team_25.cswallet.database.MessageReaderDbHelper;
import com.spit.team_25.cswallet.models.GoogleSessionManager;
import com.spit.team_25.cswallet.models.Message;
import com.spit.team_25.cswallet.models.Transactions;
import com.spit.team_25.cswallet.models.User;

import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    private Adapter<RecyclerView.ViewHolder> mAdapter = null;
    private RecyclerView messageList;
    private EditText messageText;
    private ArrayList<Message> messages = null;
    private boolean doubleBackToExitPressedOnce = false;
    private FirebaseAuth mAuth;
    private User user;
    private MessageReaderDbHelper messageReaderDbHelper;
    private SQLiteDatabase db;
    private ContentValues values;

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
//        Intent intent = new Intent(this, TransactionActivity.class);
//        startActivity(intent);

        loadUserData(this);
        getSupportActionBar().setTitle(R.string.chat_bot);

        ImageButton sendButton = (ImageButton) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
        this.messageText = (EditText) findViewById(R.id.messageText);
        this.messages = new ArrayList<>();
        this.mAdapter = new MessageAdapter(this, this.messages);

        this.messageList = (RecyclerView) findViewById(R.id.messageList);
        this.messageList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(1);
        this.messageList.setLayoutManager(llm);
        this.messageList.setAdapter(this.mAdapter);

        loadUserChats();
    }

    public void onClick(View view) {
        if(view.getId() == sendButton) {
            if(isOnline()) {
                String messString = this.messageText.getText().toString();
                if (!messString.equals(BuildConfig.FLAVOR)) {
                    this.messages.add(new Message(BuildConfig.FLAVOR, messString, true, new Date()));
                    this.mAdapter.notifyDataSetChanged();
                    sendMessage(messString);
                    this.messageText.setText(BuildConfig.FLAVOR);
                }
            }
            else
                Toast.makeText(getApplicationContext(), "You are Offline! Turn on your network!", Toast.LENGTH_LONG).show();
        }
    }

    public void sendMessage(String messString) {
        insertInDatabase(messString, "user");

        aiDataService = new AIDataService(getApplicationContext(), config);
        aiRequest = new AIRequest();
        aiRequest.setQuery(messString);
        AIResponse response= new AIResponse();
        new TextProcessor().execute(aiRequest);
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

    private void loadUserChats(){
        SQLiteDatabase db = messageReaderDbHelper.getReadableDatabase();
        if (db != null){

            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setMessage("Loading Chats! Please Wait...");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            String[]  projection = {
                    MessageEntry.COLUMN_NAME_DATE,
                    MessageEntry.COLUMN_NAME_MESSAGE,
                    MessageEntry.COLUMN_NAME_FROM
            };

            String sortOrder = MessageEntry.COLUMN_NAME_DATE + " ASC";

            Cursor cursor = db.query(MessageEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);
            String dbDate, msg, from;
            Date date;

            while(cursor.moveToNext()) {
                dbDate = cursor.getString(cursor.getColumnIndex(MessageEntry.COLUMN_NAME_DATE));
                msg = cursor.getString(cursor.getColumnIndex(MessageEntry.COLUMN_NAME_MESSAGE));
                from = cursor.getString(cursor.getColumnIndex(MessageEntry.COLUMN_NAME_FROM));
                try {
                    date = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH).parse(dbDate);
                } catch (ParseException e) {
                    break;
                }

                if(from.equals("bot"))
                    messages.add(new Message(from, msg, false, date));
                else
                    messages.add(new Message(from, msg, true, date));
            }
            cursor.close();
            this.messageList.scrollToPosition(this.messages.size() - 1);
            this.mAdapter.notifyDataSetChanged();
            dialog.dismiss();
        }
    }

    private void loadUserData(@NonNull final CallbackUserDetail callback)
    {
        messageReaderDbHelper = new MessageReaderDbHelper(getApplicationContext());
        db = messageReaderDbHelper.getWritableDatabase();
        values = new ContentValues();

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

            this.deleteDatabase(MessageReaderDbHelper.DATABASE_NAME);

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

    private void getBalance() {
        this.messages.add(new Message("WALLY", "Your wallet balance is Rs. "+user.getBalance(), false, new Date()));
        insertInDatabase("Your wallet balance is Rs. "+user.getBalance(), "bot");
        this.messageList.scrollToPosition(this.messages.size() - 1);
        this.mAdapter.notifyDataSetChanged();
    }

    private void insertInDatabase(String messString, String from) {
        values.put(MessageEntry.COLUMN_NAME_DATE, new Date().toString());
        values.put(MessageEntry.COLUMN_NAME_MESSAGE, messString);
        values.put(MessageEntry.COLUMN_NAME_FROM, from);

        db.insert(MessageEntry.TABLE_NAME, null, values);
    }

    private void getHistory(){

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query =mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).child("transaction");
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                ArrayList<Transactions> list= new ArrayList<>();
                for(DataSnapshot usr : dataSnapshot.getChildren()) {
                    list.add(usr.getValue(Transactions.class));
                    Log.e("for loop", usr.getValue(Transactions.class).getTID());
                }
                Intent intent = new Intent(MainActivity.this, TransactionActivity.class);
                intent.putExtra("transactions", list);
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
    protected void onDestroy() {
        messageReaderDbHelper.close();
        super.onDestroy();
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
                final String speech = result.getFulfillment().getSpeech();

                switch (result.getAction())
                {
                    case "getBalance":getBalance();Log.e("result", "Speech: " + speech);break;

                    case "Pay":

                        String []s = new String[2];
                        Intent intent = new Intent(getApplicationContext(), MakePayment.class);
                        HashMap<String, JsonElement> params = result.getParameters();
                        Log.e("para", params.toString());
                        if (params != null && !params.isEmpty()) {
                            Log.e("Pay", "Parameters: ");
                            for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                                Log.e("Pay", String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                            }

                        }
                        if(params.get("number") != null)
                            s[0] = params.get("number").getAsString();
                        else s[0] = "null";

                        if(params.get("given-name") != null)
                            s[1] = params.get("given-name").getAsString();
                        else s[1] = "null";

                        intent.putExtra("Currentuser",user);
                        intent.putExtra("Extra", s);
                        startActivity(intent);
                        break;

					case "getFAQ"://http://en.wikipedia.org/w/api.php?action=opensearch&search=query&limit=10&namespace=0&format=json

                        String query;

                        params = result.getParameters();

                        if (params != null && !params.isEmpty()) {
                            Log.e("Pay", "Parameters: ");
                            for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                                Log.e("Pay", String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                            }
                        }
                        if(params.get("bank")!=null){
                            query = params.get("bank").getAsString();
//
                            AsyncHttpClient client = new AsyncHttpClient();
                            client.get("http://en.wikipedia.org/w/api.php?action=opensearch&search="+query+"&limit=10&namespace=0&format=json", new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONArray response) {
                                    super.onSuccess(statusCode, headers, response);
                                    String wiki_res=null;
                                    try {
                                        response = response.getJSONArray(2);
//                                        Log.e("json",response.get(0).toString());
                                        wiki_res = response.get(0).toString();

                                    }catch(Exception e){
                                        Log.e("json error",e.toString());
                                    }
                                    Log.e("wiki",wiki_res);
                                    messages.add(new Message("Wally",wiki_res, false, new Date()));
                                    messageList.scrollToPosition(messages.size() - 1);
                                    mAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, Throwable throwable, JSONArray errorResponse) {
                                    super.onFailure(statusCode, headers, throwable, errorResponse);
                                }
                            });
                        }
                        break;

                    default:
                        messages.add(new Message("WALLY", speech, false, new Date()));
                        insertInDatabase(speech, "bot");
                        messageList.scrollToPosition(messages.size() - 1);
                        mAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}