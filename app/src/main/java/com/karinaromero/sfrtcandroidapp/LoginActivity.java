package com.karinaromero.sfrtcandroidapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

/**
 * Copyright 2018  Karina Betzabe Romero Ulloa
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

public class LoginActivity extends AppCompatActivity {
    Button btnLogin;
    TextView txtName;
    Button btnShowMirror;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtName = findViewById(R.id.textViewName);
        btnLogin= findViewById(R.id.buttonLogin);
        btnShowMirror= findViewById(R.id.btnShowMirror);

        btnLogin.setOnClickListener(v -> {
                    Intent i = new Intent(LoginActivity.this, DemoActivity.class);
                    String userName = txtName.getText().toString();
                    i.putExtra("UserName", userName);
                    startActivity(i);
                }
        );

        btnShowMirror.setOnClickListener(v -> {
                    Intent i = new Intent(LoginActivity.this, MirrorDemoActivity.class);
                    startActivity(i);
                }
        );
    }
}
