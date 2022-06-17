package com.app.proximity_detector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText identifyer;
    EditText contraseña;
    DatabaseReference rtDatabase;
    Button toRegister;
    Button login;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicio de componentes
        rtDatabase = FirebaseDatabase.getInstance().getReference();
        identifyer = (EditText) findViewById(R.id.idLoginText);
        contraseña = (EditText) findViewById(R.id.passwordLoginText);
        toRegister = (Button) findViewById(R.id.toRegisterButton);
        toRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent siguiente = new Intent(LoginActivity.this, RegisterActivity.class);
                siguiente.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(siguiente);
            }
        });
        login = (Button) findViewById(R.id.loginActivityButton);
        // pulsacion del boton
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = identifyer.getText().toString();
                String password = contraseña.getText().toString();

                // comprobación de usuario y contraseña
                rtDatabase.child("usuarios").child("usuariosB").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(id)) {
                            String passwordDatabase = snapshot.child(id).child("password").getValue(String.class);
                            String username = snapshot.child(id).child("username").getValue(String.class);
                            if(passwordDatabase.equals(password)) {
                                // Inicio de sesión con el Usuario B
                                Intent siguiente = new Intent(LoginActivity.this, MapsActivity.class);
                                siguiente.putExtra("userSelected",false);
                                siguiente.putExtra("username",username);
                                siguiente.putExtra("id",id);
                                siguiente.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(siguiente);
                                Toast.makeText(getApplicationContext(), "Sesión iniciada como un Usuario B", Toast.LENGTH_LONG).show();
                                rtDatabase.child("usuarios").child("usuariosB").child(id).child("isConnected").setValue(true);
                                finish();
                                // Fin
                            } else {
                                Toast.makeText(getApplicationContext(), "Contraseña incorrecta", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "No existe ese usuario", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }
}

