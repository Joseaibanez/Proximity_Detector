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

public class RegisterActivity extends AppCompatActivity {

    EditText idUser;
    EditText nombre;
    EditText contraseña;
    Button registrar;
    Button remove;
    DatabaseReference rtDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicio de componentes
        rtDatabase = FirebaseDatabase.getInstance().getReference();
        idUser = (EditText) findViewById(R.id.idUser);
        nombre = (EditText) findViewById(R.id.editTextNombreReg);
        contraseña = (EditText) findViewById(R.id.editTextPassReg);
        registrar = (Button) findViewById(R.id.registerUserButton);
        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idUser.getText().toString();
                String name = nombre.getText().toString();
                String password = contraseña.getText().toString();
                if (name.isEmpty() || password.isEmpty() || id.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Por favor, rellene todos los campos", Toast.LENGTH_LONG).show();
                } else {
                    rtDatabase.child("usuarios").child("usuariosB").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(id)) {
                                Toast.makeText(getApplicationContext(), "El identificador ya está en uso", Toast.LENGTH_LONG).show();
                            } else {
                                rtDatabase.child("usuarios").child("usuariosB").child(id);
                                rtDatabase.child("usuarios").child("usuariosB").child(id).child("username").setValue(name);
                                rtDatabase.child("usuarios").child("usuariosB").child(id).child("password").setValue(password);
                                rtDatabase.child("usuarios").child("usuariosB").child(id).child("isConnected").setValue(false);
                                rtDatabase.child("usuarios").child("usuariosB").child(id).child("lat").setValue(5.55555);
                                rtDatabase.child("usuarios").child("usuariosB").child(id).child("lng").setValue(-5.55555);
                                rtDatabase.child("usuarios").child("usuariosB").child(id).child("isInside").setValue(false);
                                Toast.makeText(getApplicationContext(), "Usuario Registrado", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        });
        remove = (Button) findViewById(R.id.deleteUserButton);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = idUser.getText().toString();
                String password = contraseña.getText().toString();
                if (password.isEmpty() || id.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Por favor, rellene todos los campos", Toast.LENGTH_LONG).show();
                } else {
                    rtDatabase.child("usuarios").child("usuariosB").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(id)) {
                                String passwordDatabase = snapshot.child(id).child("password").getValue(String.class);
                                assert passwordDatabase != null;
                                if(passwordDatabase.equals(password)) {
                                    snapshot.child(id).getRef().setValue(null);
                                    Toast.makeText(getApplicationContext(), "Usuario eliminado con éxito", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Contraseña incorrecta", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "El usuario no existe", Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        });
    }
}