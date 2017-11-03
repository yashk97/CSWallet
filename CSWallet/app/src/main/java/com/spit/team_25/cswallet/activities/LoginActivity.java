package com.spit.team_25.cswallet.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spit.team_25.cswallet.R;
import com.spit.team_25.cswallet.models.User;

import org.json.JSONObject;

import static android.widget.Toast.makeText;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private TextView wrongCred;
    private Button mEmailSignInButton;
    private String email, password;
    TextView _signupLink, _forgetPasswordLink;

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private GoogleSignInOptions gso;
    public static GoogleApiClient mGoogleApiClient;
    private RelativeLayout googleSignIn;
    private JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                wrongCred.setVisibility(View.GONE);
                if (isOnline()) {
                    setEmail(mEmailView.getText().toString());
                    setPassword(mPasswordView.getText().toString());

                    if(validateLogin()) {
                        firebaseAppSignIn(getEmail(), getPassword());
                    }
                }
                else {
                    // Toast displays the message on the screen for a period of time
                    makeText(getApplicationContext(), "You are Offline! Turn on your network!", Toast.LENGTH_LONG).show();
                }
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });

        _forgetPasswordLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleForgetPassword();
            }
        });

        googleSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOnline())
                    firebaseGoogleSignIn();
                else
                    makeText(getApplicationContext(), "You are Offline! Turn on your network!", Toast.LENGTH_LONG).show();
            }
        });
    }

    // This method checks to see if the device is online. Returns true if online, else false
    private boolean isOnline() {
        // Connectivity manager gives you access to the current state of the connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    private void firebaseAppSignIn(String email, String password) {

        if (!validateLogin()) {
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Verifying! Please Wait...");
        dialog.show();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    //FirebaseUser user = mAuth.getCurrentUser();
                    makeText(LoginActivity.this, "Signed In Successfully.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    Intent intent = new Intent(getApplication(), MainActivity.class);
                    intent.putExtra("Login", "App");
                    startActivity(intent);
                    setLoginStatus(getApplicationContext(), "Firebase");
                    finishPrevActivities();
                } else {
                    wrongCred.setText(task.getException().getLocalizedMessage());
                    wrongCred.setError("");
                    dialog.dismiss();
                    wrongCred.setVisibility(View.VISIBLE);
                }
            }
        });
        // [END sign_in_with_email]
    }

    private boolean validateLogin() {
        boolean flag = true;

        if(getEmail().equals(""))
        {
            mEmailView.setError("Required");
            flag = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(getEmail()).matches()) {
            mEmailView.setError("Invalid Format");
            flag = false;
        }

        if(getPassword().equals("")) {
            mPasswordView.setError("Required");
            flag = false;
        }

        return flag;
    }

    private void handleForgetPassword() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.forget_password, null);
        final EditText etEmail = (EditText) alertLayout.findViewById(R.id.forget_email);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Reset Password");
        alert.setMessage("Enter your registered email used for account creation to reset password");
        alert.setView(alertLayout);
        //disallow cancel of AlertDialog on click of back button and outside touch
        //alert.setCancelable(false);
        alert.setNegativeButton("Cancel", null).setPositiveButton("Send Email", null);

        AlertDialog dialog = alert.create();
        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(etEmail.getText().toString().equals(""))
                            etEmail.setError("This field is required");
                        else if(!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches())
                            etEmail.setError("Enter email address correctly");
                        else {
                            FirebaseAuth.getInstance().sendPasswordResetEmail(etEmail.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            String message = "";
                                            if (task.isSuccessful())
                                                message = "Email Sent to Reset Password";
                                            else if(isOnline())
                                                message = "Error! No such email registered";
                                            else
                                                message = "You are Offline! Turn on your network!";

                                            Toast toast = makeText(getBaseContext(), message, Toast.LENGTH_LONG);
                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                            toast.show();
                                        }
                                    });
                            dialog.dismiss();
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    void init(){
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        _signupLink = (TextView)findViewById(R.id.signup_link);
        _forgetPasswordLink = (TextView) findViewById(R.id.tvForgotPassword);

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        googleSignIn = (RelativeLayout) findViewById(R.id.rlGoogleSignIn);
        wrongCred = (TextView) findViewById(R.id.wrong_cred);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                firebaseAuthWithGoogle(result.getSignInAccount());
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]

                // [END_EXCLUDE]
            }
        }
    }

    private void createGoogleUser(GoogleSignInAccount account) {
        User newUser = new User();
        newUser.setName(account.getDisplayName());
        newUser.setEmail(account.getEmail());
        newUser.setBalance("0");
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).setValue(newUser);
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Verifying! Please Wait...");
        dialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            makeText(LoginActivity.this, "Signed In Successfully.",
                                    Toast.LENGTH_SHORT).show();
                            createGoogleUser(acct);
                            dialog.dismiss();
                            Intent intent = new Intent(getApplication(), MainActivity.class);
                            intent.putExtra("Login", "Google");
                            startActivity(intent);
                            setLoginStatus(getApplicationContext(), "Google");
                            finishPrevActivities();
                        } else {
                            // If sign in fails, display a message to the user.
                            wrongCred.setText(task.getException().getLocalizedMessage());
                            wrongCred.setError("");
                            dialog.dismiss();
                            wrongCred.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    // [END auth_with_google]

    // [START signin]
    private void firebaseGoogleSignIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void setLoginStatus(Context context, String loginMethod) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString("status", "logged in");
        editor.putString("method", loginMethod);
        editor.apply();
    }

    private void finishPrevActivities() {
        LoginActivity.this.finish();
        finishAffinity();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}