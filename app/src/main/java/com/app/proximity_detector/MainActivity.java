package com.app.proximity_detector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    Button bUserA;
    Button login;
    Button disconectUsers;
    DatabaseReference rtDatabase;
    LocationManager locManager;
    ConnectivityManager connManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        rtDatabase = FirebaseDatabase.getInstance().getReference();
        // Boton para desconectar a todos los usuarios
        disconectUsers = (Button) findViewById(R.id.disconectUsersButton);
        disconectUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rtDatabase.child("usuarios").child("usuarioA").child("isConnected").setValue(false);
                rtDatabase.child("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot databaseSnapshot : snapshot.child("usuariosB").getChildren()) {
                            String id = databaseSnapshot.getKey();
                            rtDatabase.child("usuarios").child("usuariosB").child(id).child("isConnected").setValue(false);
                            rtDatabase.child("usuarios").child("usuariosB").child(id).child("isInside").setValue(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        // Boton para ir a la pantalla de Login
        login = (Button) findViewById(R.id.loginButton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnection()) {
                    if (checkLocationSetting()) {
                        Intent siguiente = new Intent(MainActivity.this, LoginActivity.class);
                        siguiente.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(siguiente);
                    }
                }
            }
        });
        //Boton para conectarse como usuario A
        bUserA = (Button) findViewById(R.id.userABtt);
        bUserA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkConnection()) {
                    if (checkLocationSetting()) {
                        Intent siguiente = new Intent(MainActivity.this, MapsActivity.class);
                        siguiente.putExtra("userSelected",true);
                        siguiente.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(siguiente);
                    }
                }
            }
        });
    }

    private boolean checkLocationSetting() {
        boolean response = false;
        try {
            if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                response = true;
            } else {
                Toast.makeText(getApplicationContext(), "Por favor, active la ubicación de su dispositivo", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "Ha ocurrido un error con la red", Toast.LENGTH_LONG).show();
        }
        return response;
    }

    private boolean checkConnection() {
        boolean response = false;
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        if(netInfo != null) {
            if (netInfo.isConnected()) {
                response = true;
            } else {
                Toast.makeText(getApplicationContext(), "No hay ninguna conexión disponible", Toast.LENGTH_LONG).show();
            }
        }
        return response;
    }
}
