package com.app.proximity_detector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button bUserA;
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = (Button) findViewById(R.id.loginButton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = connManager.getActiveNetworkInfo();
                if(netInfo != null) {
                    if (netInfo.isConnected()) {
                        Intent siguiente = new Intent(MainActivity.this, LoginActivity.class);
                        siguiente.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(siguiente);
                    } else {
                        Toast.makeText(getApplicationContext(), "No hay ninguna conexión disponible", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
        bUserA = (Button) findViewById(R.id.userABtt);
        bUserA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent siguiente = new Intent(MainActivity.this, MapsActivity.class);
                siguiente.putExtra("userSelected",true);
                siguiente.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(siguiente);
            }
        });
    }


}
