package com.app.proximity_detector;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UsuarioA userA;
    private UsuarioB userB;
    boolean isUserA;
    private DatabaseReference rtDatabase;
    ValueEventListener databaseListener;
    Button zoomButton;
    Button areaChooser500;
    Button areaChooser1;
    Button areaChooser20;
    String username;
    String idUserB;
    boolean isAnotherUserAConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!hasConnection()) {
            Toast.makeText(this, "No hay ninguna conexión disponible", Toast.LENGTH_LONG).show();
            finish();
        }
        isAnotherUserAConnected = false;
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        // Inicialización de la base de datos
        rtDatabase = FirebaseDatabase.getInstance().getReference();
        getPermisos();
        // Comprobación del usuario selecionado
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isUserA = extras.getBoolean("userSelected");
            if (!isUserA) {
                rtDatabase.child("usuarios").child("usuarioA").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!(boolean) snapshot.child("isConnected").getValue()) {
                            Toast.makeText(getApplicationContext(), "No hay ningún usuario A conectado", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                username = extras.getString("username");
                idUserB = extras.getString("id");
            } else {
                rtDatabase.child("usuarios").child("usuarioA").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if ((boolean) snapshot.child("isConnected").getValue()) {
                            Toast.makeText(getApplicationContext(), "Ya hay un usuario A conectado", Toast.LENGTH_LONG).show();
                            isAnotherUserAConnected = true;
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }
        // Boton para hacer zoom en la localizacion del usuario
        zoomButton = (Button) findViewById(R.id.zoomButton);
        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoom();
            }
        });
        // Botones para cambiar el tamaño del area
        areaChooser500 = (Button) findViewById(R.id.areaChooser500);
        areaChooser500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rtDatabase.child("usuarios").child("usuarioA").child("radio").setValue(500);
                changeSecurityArea();
            }
        });
        areaChooser1 = (Button) findViewById(R.id.areaChooser1);
        areaChooser1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rtDatabase.child("usuarios").child("usuarioA").child("radio").setValue(1000);
                changeSecurityArea();
            }
        });
        areaChooser20 = (Button) findViewById(R.id.areaChooser20);
        areaChooser20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rtDatabase.child("usuarios").child("usuarioA").child("radio").setValue(20);
                changeSecurityArea();
            }
        });
        // Si es el usuario B se deshabilitan los botones para cambiar el tamaño de area
        if (!isUserA) {
            areaChooser1.setVisibility(View.INVISIBLE);
            areaChooser500.setVisibility(View.INVISIBLE);
            areaChooser20.setVisibility(View.INVISIBLE);
        }
        // Lector de datos
        databaseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMap.clear();
                //Parte del UsuarioA
                if(!(boolean) snapshot.child("usuarioA").child("isConnected").getValue()) {
                    finish();
                }
                System.out.println("************************************************************************************");
                System.out.println("LISTENER AUN EN USO");
                System.out.println("************************************************************************************");
                changeSecurityArea();
                // Inicio de la parte del UsuarioB
                for(DataSnapshot databaseSnapshot : snapshot.child("usuariosB").getChildren()) {
                    if ((boolean) databaseSnapshot.child("isConnected").getValue()) {
                        String username = databaseSnapshot.child("username").getValue().toString();
                        Double lat = (Double) databaseSnapshot.child("lat").getValue();
                        Double lng = (Double) databaseSnapshot.child("lng").getValue();
                        LatLng userBcoordenates = new LatLng(lat,lng);
                        mMap.addMarker(new MarkerOptions().position(userBcoordenates).title(username).
                                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                        if ((boolean) databaseSnapshot.child("isInside").getValue()) {
                            if(isUserA) {
                                userA.startMediaPlayer();
                            } else {
                                userB.startMediaPlayer();
                            }

                        } else {
                            if(isUserA) {
                                userA.stopMediaPlayer();
                            } else {
                                if (userB != null) {
                                    userB.stopMediaPlayer();
                                }
                            }

                        }

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
    }
    // Fin onCreate

    // comprueba si el dispositivo está conectado a una red
    private boolean hasConnection() {
        boolean resultado = false;
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        if(netInfo != null) {
            if (netInfo.isConnected()) {
                resultado = true;
            }
        }
        return resultado;
    }


    // actualiza el area del Usuario A
    private void changeSecurityArea() {
        rtDatabase.child("usuarios").child("usuarioA").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double lat = (Double) snapshot.child("lat").getValue();
                Double lng = (Double) snapshot.child("lng").getValue();
                userA.drawCircle(mMap, new LatLng(lat, lng));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // centra y acerca la cámara del dispositivo sobre la ubicación actual
    private void zoom() {
        float zoom = 20.0f;
        if (isUserA) {
            rtDatabase.child("usuarios").child("usuarioA").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Double lat = (Double) snapshot.child("lat").getValue();
                    Double lng = (Double) snapshot.child("lng").getValue();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {
            rtDatabase.child("usuarios").child("usuariosB").child(idUserB).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Double lat = (Double) snapshot.child("lat").getValue();
                    Double lng = (Double) snapshot.child("lng").getValue();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    // Método para resetear la actividad de mapa
    // elimina marcadores, cierra conexiones a la base
    // y detiene los listeners para datos de los usuarios
    private void clearApp() {
        if(isUserA) {
            if (!isAnotherUserAConnected) {
                if (userA != null) {
                    userA.stopLocUpdates();
                    userA.stopMediaPlayer();
                }
                rtDatabase.child("usuarios").child("usuarioA").child("isConnected").setValue(false);
            }
        } else {
            rtDatabase.child("usuarios").child("usuariosB").child(idUserB).child("isConnected").setValue(false);
            rtDatabase.child("usuarios").child("usuariosB").child(idUserB).child("isInside").setValue(false);
            if (userB != null) {
                userB.stopLocUpdates();
                userB.stopMediaPlayer();
            }

        }
        rtDatabase.child("usuarios").removeEventListener(databaseListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearApp();
    }

    @Override
    protected void onStop() {
        super.onStop();
        clearApp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearApp();
    }

    private void getPermisos() {
        int permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permiso == PackageManager.PERMISSION_DENIED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setIndoorEnabled(true);
        userA = new UsuarioA(this);
        if(isUserA) {
            rtDatabase.child("usuarios").child("usuarioA").child("isConnected").setValue(true);
            userA.startLocUpdates();
        } else {
            Toast.makeText(getApplicationContext(), "Sesión iniciada como "+username, Toast.LENGTH_LONG).show();
            userB = new UsuarioB(getApplicationContext(), mMap, userA, idUserB);
            userB.startLocUpdates();
        }
        rtDatabase.child("usuarios").addValueEventListener(databaseListener);
    }

}