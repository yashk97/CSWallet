package com.spit.team_25.cswallet.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.spit.team_25.cswallet.R;
import com.spit.team_25.cswallet.models.User;

public class SignUpActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword, etPhone;
    TextView signin_link, etError;
    Button createAccountButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        init();

        signin_link.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        createAccountButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount(etEmail.getText().toString(), etPassword.getText().toString());
            }
        });
    }

    private void init() {
        signin_link = (TextView)findViewById(R.id.link_login);
        etName = (EditText) findViewById(R.id.input_name);
        etEmail = (EditText) findViewById(R.id.input_email);
        etPassword = (EditText) findViewById(R.id.input_password);
        etPhone = (EditText) findViewById(R.id.input_phone);
        etError = (TextView) findViewById(R.id.error);
        createAccountButton = (Button) findViewById(R.id.btn_signup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void createAccount(String email, String password) {

        if (!validateForm()) {
            return;
        }

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please Wait!");
        dialog.show();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                etError.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mAuth.getCurrentUser();
                    createNewUser(user);
                    Toast.makeText(SignUpActivity.this, "Account Created Successfully.",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    dialog.dismiss();
                    startActivity(intent);

                } else {
                    // If sign in fails, display a message to the user.
                    dialog.dismiss();
                    //Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    etError.setVisibility(View.VISIBLE);
                    etError.setError("");
                    etError.setText(task.getException().getMessage());
                }
            }
        });
        // [END create_user_with_email]
    }

    private void createNewUser(FirebaseUser user) {
        User newUser = new User();

        newUser.setName(etName.getText().toString());
        newUser.setEmail(etEmail.getText().toString());
        newUser.setPhone(etPhone.getText().toString());
        newUser.setBalance("0");

        mDatabase.child("users").child(user.getUid()).setValue(newUser);
    }

    private boolean validateForm() {
        boolean valid = true;

        if (TextUtils.isEmpty(etName.getText().toString())) {
            etName.setError("Required");
            valid = false;
        } else {
            etName.setError(null);
        }

        if (TextUtils.isEmpty(etEmail.getText().toString())) {
            etEmail.setError("Required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString()).matches()) {
            etEmail.setError("Invalid Format");
            valid = false;
        }
        else {
            etEmail.setError(null);
        }

        if (TextUtils.isEmpty(etPassword.getText().toString())) {
            etPassword.setError("Required");
            valid = false;
        } else {
            etPassword.setError(null);
        }

        if (TextUtils.isEmpty(etPhone.getText().toString())) {
            etPhone.setError("Required");
            valid = false;
        } else if (!(etPhone.getText().toString().length() == 10)) {
            etPhone.setError("Invalid Number");
            valid = false;
        }
        else {
            etPhone.setError(null);
        }

        return valid;
    }
}