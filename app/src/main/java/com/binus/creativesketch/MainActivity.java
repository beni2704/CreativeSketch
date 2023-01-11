package com.binus.creativesketch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button btnDraw;
    private Button btnDrawBg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnDraw = findViewById(R.id.btnDraw);
        btnDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent
                        (MainActivity.this, DrawActivity.class));
                finish();
            }
        });

        btnDrawBg = findViewById(R.id.btnDrawBg);
        btnDrawBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent
                        (MainActivity.this, DrawActivity.class).putExtra("requestStoragePermission","requestStoragePermission"));
                finish();
            }
        });
    }
}