package com.app.proximity_detector;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UsuarioA userA;
    private UsuarioB userB;
    boolean isUserA;
    private DatabaseReference rtDatabase;
    private CancellationToken cToken;
    ValueEventListener valueListenerDataA;
    ValueEventListener valueListenerDataB;
    Marker userBMark;
    Button zoomButton;
    Button areaChooser500;
    Button areaChooser1;
    Button areaChooser20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        // Comprobación del usuario selecionado
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isUserA = extras.getBoolean("userSelected");
        }

        // Inicialización de la base de datos
        rtDatabase = FirebaseDatabase.getInstance().getReference();
        if (hasConnection()) {
            SharedPreferences sPref = getSharedPreferences("userAConnection", Context.MODE_PRIVATE);
            boolean connA = sPref.getBoolean("userAConn", true);
            boolean connB = sPref.getBoolean("userBConn", false);
            rtDatabase.child("usuarios").child("usuarioA").child("isConected").setValue(connA);
            rtDatabase.child("usuarios").child("usuarioB").child("isConected").setValue(connB);

            SharedPreferences.Editor editor = sPref.edit();
            editor.clear();
            editor.apply();

        }
        getPermisos();
        // Boton para hacer zoom en la localizacion del usuario
        zoomButton = (Button) findViewById(R.id.zoomButton);
        zoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoom();
            }
        });
        // Fin boton zoom
        // Botones para cambiar el tamaño del area
        areaChooser500 = (Button) findViewById(R.id.areaChooser500);
        areaChooser500.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rtDatabase.child("usuarios").child("usuarioA").child("radio").setValue(500);
                changeArea();
            }
        });
        areaChooser1 = (Button) findViewById(R.id.areaChooser1);
        areaChooser1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rtDatabase.child("usuarios").child("usuarioA").child("radio").setValue(1000);
                changeArea();
            }
        });
        areaChooser20 = (Button) findViewById(R.id.areaChooser20);
        areaChooser20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rtDatabase.child("usuarios").child("usuarioA").child("radio").setValue(20);
                changeArea();
            }
        });

        if (!isUserA) {
            areaChooser1.setVisibility(View.INVISIBLE);
            areaChooser500.setVisibility(View.INVISIBLE);
            areaChooser20.setVisibility(View.INVISIBLE);
        }
        // Fin cambio de area

        // Lector de datos para el usuario A
        valueListenerDataA = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!(boolean) snapshot.child("isConected").getValue()) {
                    finish();
                }
                if (!hasConnection()) {
                    Toast.makeText(getApplicationContext(), "Se ha perdido la conexión, inténtelo mas tarde", Toast.LENGTH_LONG).show();
                    finish();
                }
                changeArea();
                // Comprobacion del poligono para el usuario A
                checkIsInsidePoligon();
                // FIN
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        // Lector de datos para el usuario B
        valueListenerDataB = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!hasConnection()) {
                    Toast.makeText(getApplicationContext(), "Se ha perdido la conexión, inténtelo mas tarde", Toast.LENGTH_LONG).show();
                    finish();
                }
                // Comprobacion del poligono para el usuario B
                if ((boolean) snapshot.child("isConected").getValue()) {
                    LatLng userBLocation = new LatLng((Double) snapshot.child("lat").getValue(),(Double) snapshot.child("lng").getValue());
                    if(userBMark != null) {
                        userBMark.remove();
                    }
                    userBMark = mMap.addMarker(new MarkerOptions().position(userBLocation).title("Usuario B").
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    checkIsInsidePoligon();
                } else {
                    if(userBMark != null) {
                        userBMark.remove();
                    }
                }

                // FIN
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

    }
    // Fin onCreate

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


    // Método para actualizar el area Del usuario A
    private void changeArea() {
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
            rtDatabase.child("usuarios").child("usuarioB").addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void checkIsInsidePoligon() {
        rtDatabase.child("usuarios").child("usuarioB").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((boolean) snapshot.child("isInside").getValue()) {
                    //HACER SONAR EL MOVIL
                    if (isUserA) {
                        userA.startMediaPlayer();
                    }
                } else {
                    userA.stopMediaPlayer();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Método para resetear la actividad de mapa
    // elimina marcadores, cierra conexiones a la base
    // y detiene los listeners para datos de los usuarios
    private void clearApp() {
        if(isUserA) {
            userA.stopMediaPlayer();
            if(hasConnection()) {
                rtDatabase.child("usuarios").child("usuarioA").child("isConected").setValue(false);
            } else {
                SharedPreferences sPref = getSharedPreferences("userAConnection", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sPref.edit();
                editor.putBoolean("userAConn", false);
                editor.apply();
            }
            userA.stopLocUpdates();
            userA.clearMap(mMap);
            if(userBMark != null) {
                userBMark.remove();
            }
            userA.closeDatabase();
        } else {
            userB.stopMediaPlayer();
            userB.stopLocUpdates();
            userB.setIsOutside();
            if(hasConnection()) {
                rtDatabase.child("usuarios").child("usuarioB").child("isConected").setValue(false);
            } else {
                SharedPreferences sPref = getSharedPreferences("userBConnection", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sPref.edit();
                editor.putBoolean("userBConn", false);
                editor.apply();
            }
            rtDatabase.removeEventListener(valueListenerDataB);
            userB.closeDatabase();
            if(userBMark != null) {
                userBMark.remove();
            }
        }
        rtDatabase.removeEventListener(valueListenerDataA);
        if (valueListenerDataB != null) {
            rtDatabase.removeEventListener(valueListenerDataB);
        }
        mMap.clear();
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
        if(!hasConnection()) {
            Toast.makeText(this, "No hay ninguna conexión disponible", Toast.LENGTH_LONG).show();
            finish();
        }
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setIndoorEnabled(true);// Para la obtencion de ubicacion en interiores ***************************************************************************************************************NO FUNCIONA
        userA = new UsuarioA(this);
        userB = new UsuarioB(this, mMap, userA);
        if(isUserA) {
            rtDatabase.child("usuarios").child("usuarioA").child("isConected").setValue(true);
            userA.startLocUpdates();
        }
        // Generar zonas
        rtDatabase.child("usuarios").child("usuarioA").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((boolean) snapshot.child("isConected").getValue()) {
                    rtDatabase.child("usuarios").child("usuarioA").addValueEventListener(valueListenerDataA);
                    if(!isUserA) {
                        // Parte del usuario B
                        Toast.makeText(getApplicationContext(), "¡ES EL USUARIO B!", Toast.LENGTH_LONG).show();
                        rtDatabase.child("usuarios").child("usuarioB").child("isConected").setValue(true);
                        userB.startLocUpdates();
                    }
                    rtDatabase.child("usuarios").child("usuarioB").addValueEventListener(valueListenerDataB);

                } else {
                    Toast.makeText(getBaseContext(), "No hay ningun usuario A conectado", Toast.LENGTH_LONG).show();
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}