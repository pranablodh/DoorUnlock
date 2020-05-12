package com.pranab.doorunlock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity
{
    private Button new_reg;
    private Button unlock;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new_reg = (Button) findViewById(R.id.new_reg);
        unlock = (Button) findViewById(R.id.unlock);

        new_reg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                go_to_new_page();
            }
        });

        unlock.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                go_to_unlock_page();
            }
        });
    }

    private void go_to_new_page()
    {
        Intent go = new Intent(MainActivity.this, newFace.class);
        startActivity(go);
        finish();
    }

    private void go_to_unlock_page()
    {
        Intent go = new Intent(MainActivity.this, unlock.class);
        startActivity(go);
        finish();
    }
}
