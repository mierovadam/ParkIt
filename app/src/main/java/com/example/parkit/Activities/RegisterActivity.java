package com.example.parkit.Activities;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.parkit.R;
import com.example.parkit.Utils.BaseActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private MaterialButton register_BTN_register,register_BTN_return;
    private TextInputLayout register_EDT_email,register_EDT_password1,register_EDT_password2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findViews();
        initViews();
    }

    private void findViews() {
        register_EDT_email       = findViewById(R.id.register_EDT_email);
        register_EDT_password1   = findViewById(R.id.register_EDT_password1);
        register_EDT_password2   = findViewById(R.id.register_EDT_password2);
        register_BTN_register    = findViewById(R.id.register_BTN_register);
        register_BTN_return      = findViewById(R.id.register_BTN_return);

        ImageView register_IMG_logo = findViewById(R.id.register_IMG_logo);
        Glide.with(this).load(R.drawable.login_logo).into(register_IMG_logo);
    }

    private void initViews() {
        register_BTN_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        register_BTN_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
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
                            Log.d("pttt", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("emails");
                            myRef.child(removeSpecialChars(user.getEmail())).setValue(user.getUid());

                            openLoginActivity();
                        } else {
                            Log.w("pttt", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
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