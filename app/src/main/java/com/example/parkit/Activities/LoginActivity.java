package com.example.parkit.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.parkit.R;
import com.example.parkit.Utils.BaseActivity;
import com.example.parkit.Utils.MySP;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseActivity {
    public final static String SP_EMAIL_KEY = "rememberMeKey";

    private MaterialButton login_BTN_login,login_BTN_register;
    private TextInputLayout login_EDT_email,login_EDT_password;
    private CheckBox login_CB_remember;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        validateUser();

        MySP.init(this);
        findViews();
        initViews();

    }

    private void rememberMeCheck() {
        if(login_CB_remember.isChecked()){
            MySP.getInstance().putString(SP_EMAIL_KEY, login_EDT_email.getEditText().getText().toString());
        }else{
            MySP.getInstance().putString(SP_EMAIL_KEY, "");
        }
    }

    private void validateUser() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Intent myIntent = new Intent(this, MenuActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }
    }

    public void openMenuActivity() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    public void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }


    private void initViews() {
        login_BTN_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        login_BTN_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openRegisterActivity();
            }
        });
        login_EDT_email.getEditText().setText(MySP.getInstance().getString(SP_EMAIL_KEY, ""));
    }

    private void signIn() {
        String email = login_EDT_email.getEditText().getText().toString();
        String password = login_EDT_password.getEditText().getText().toString();

        if(email.equals("") || password.equals("")){
            Toast.makeText(this,"You must enter password and email!",Toast.LENGTH_LONG).show();
            Log.d("pttt", "signIn: ");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("pttt", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            rememberMeCheck();
                            openMenuActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("pttt", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void findViews() {
        login_BTN_login    = findViewById(R.id.login_BTN_login);
        login_BTN_register = findViewById(R.id.login_BTN_register);
        login_EDT_email    = findViewById(R.id.login_EDT_email);
        login_EDT_password = findViewById(R.id.login_EDT_password);
        login_CB_remember  = findViewById(R.id.login_CB_remember);

        ImageView login_IMG_logo = findViewById(R.id.login_IMG_logo);
        Glide.with(this).load(R.drawable.login_logo).into(login_IMG_logo);
    }

}
