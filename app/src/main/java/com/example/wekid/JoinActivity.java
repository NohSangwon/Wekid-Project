package com.example.wekid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
public class JoinActivity extends AppCompatActivity {

    ImageButton teacherBtn;
    ImageButton parentBtn;
    ImageButton principalBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        teacherBtn = (ImageButton) findViewById(R.id.teacherBtn);
        parentBtn = (ImageButton)findViewById(R.id.parentBtn);
        principalBtn = (ImageButton)findViewById(R.id.principalBtn);

        teacherBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), KinderActivity.class);
                startActivity(intent);
                finish();
            }
        });

        parentBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InsertParentActivity.class);
                startActivity(intent);
                finish();
            }
        });
        principalBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InsertPrincipalActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
