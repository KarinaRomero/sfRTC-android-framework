package com.karinaromero.sfrtcandroidapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {
    Button btnLogin;
    TextView txtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtName = findViewById(R.id.textViewName);
        btnLogin= findViewById(R.id.buttonLogin);

        btnLogin.setOnClickListener(v -> {
                    Intent i = new Intent(LoginActivity.this, DemoActivity.class);
                    String userName = txtName.getText().toString();
                    i.putExtra("UserName", userName);
                    startActivity(i);
                }
        );
    }
}
