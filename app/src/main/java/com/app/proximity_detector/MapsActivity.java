package com.app.proximity_detector;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);***********************************************************************************************
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
        rtDatabase = FirebaseDatabase.getInstance().getReference();
        //rtDatabase.keepSynced(true);
        getPermisos();

        // Lector de datos para el usuario A
        valueListenerDataA = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!(boolean) snapshot.child("isConected").getValue()) {
                    finish();
                }
                Double lat = (Double) snapshot.child("lat").getValue();
                Double lng = (Double) snapshot.child("lng").getValue();
                userA.drawCircle(mMap, new LatLng(lat, lng), 20);
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
                System.out.println("*********************************************************************************************************************************************");
                System.out.println("LECTOR DE DATOS B CORRIENDO");
                System.out.println("*********************************************************************************************************************************************");
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


    private void clearApp() {
        if(isUserA) {
            userA.stopMediaPlayer();
            rtDatabase.child("usuarios").child("usuarioA").child("isConected").setValue(false);
            userA.stopLocUpdates();
            userA.clearMap(mMap);
            if(userBMark != null) {
                userBMark.remove();
                Toast.makeText(getBaseContext(), "SE HA ELIMINADO EL MARKER B", Toast.LENGTH_LONG).show();
            }
            //rtDatabase.keepSynced(true);
            userA.closeDatabase();
        } else {
            userB.stopMediaPlayer();
            userB.stopLocUpdates();
            userB.setIsOutside();
            rtDatabase.child("usuarios").child("usuarioB").child("isConected").setValue(false);
            rtDatabase.removeEventListener(valueListenerDataB);
            //rtDatabase.removeEventListener(getUserBConnectionListener);
            userB.closeDatabase();
        }
        if(userBMark != null) {
            userBMark.remove();
            Toast.makeText(getBaseContext(), "SE HA ELIMINADO EL MARKER B", Toast.LENGTH_LONG).show();
        }
        rtDatabase.removeEventListener(valueListenerDataA);
        if (valueListenerDataB != null) {
            rtDatabase.removeEventListener(valueListenerDataB);
        }
        //rtDatabase.removeEventListener(userAConnectionListener);
        //rtDatabase.removeEventListener(getUserBConnectionListener);
        mMap.clear();
        Toast.makeText(getBaseContext(), "SE HA RESETEADO EL MAPA", Toast.LENGTH_LONG).show();
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
        mMap.setIndoorEnabled(true);// Para la obtencion de ubicacion en interiores ***************************************************************************************************************NO FUNCIONA
        userA = new UsuarioA(this);
        userB = new UsuarioB(this, mMap, userA);
        if(isUserA) {
            Toast.makeText(this, "¡ES EL USUARIO A!", Toast.LENGTH_LONG).show();
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
                    //rtDatabase.child("usuarios").child("usuarioB").addValueEventListener(getUserBConnectionListener);
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