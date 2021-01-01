package com.example.parkit.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.parkit.R;
import com.example.parkit.Utils.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private MaterialButton register_BTN_register;
    private TextInputLayout register_EDT_email,register_EDT_password1,register_EDT_password2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViews();
        initViews();
    }

    private void findViews() {
        register_EDT_email = findViewById(R.id.register_EDT_email);
        register_EDT_password1 = findViewById(R.id.register_EDT_password1);
        register_EDT_password2 = findViewById(R.id.register_EDT_password2);
        register_BTN_register = findViewById(R.id.register_BTN_register);
    }

    private void initViews() {
        register_BTN_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        String email = register_EDT_email.getEditText().getText().toString();
        String password1 = register_EDT_password1.getEditText().getText().toString();
        String password2 = register_EDT_password2.getEditText().getText().toString();

        if(!(password1.equals(password2))){
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth = FirebaseAuth.getInstance();

        mAuth.createUserWithEmailAndPassword(email, password1)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("pttt", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            openLoginActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("pttt", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}