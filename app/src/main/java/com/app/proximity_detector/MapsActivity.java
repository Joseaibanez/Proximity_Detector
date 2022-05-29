package com.app.proximity_detector;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
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
    MediaPlayer player;
    private DatabaseReference rtDatabase;
    private CancellationToken cToken;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Comprobación del usuario selecionado
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isUserA = extras.getBoolean("userSelected");
        }

        Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        player = MediaPlayer.create(getApplicationContext(), ringtone);
        rtDatabase = FirebaseDatabase.getInstance().getReference();
        getPermisos();
    }

    @Override
    protected void onStop() {
        if(isUserA) {
            if (player.isPlaying()) {
                player.stop();
                try {
                    player.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            rtDatabase.child("usuarios").child("usuarioA").child("isConected").setValue(false);
        } else {
            userB.stopMediaPlayer();
            userB.setIsOutside();
        }
        super.onStop();
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
        userA = new UsuarioA(this);
        userB = new UsuarioB(this);
        if(isUserA) {
            rtDatabase.child("usuarios").child("usuarioA").child("isConected").setValue(true);
            userA.setUserALocation();
        }
        // Generar zonas
        rtDatabase.child("usuarios").child("usuarioA").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                mMap.setIndoorEnabled(true);// Para la obtencion de ubicacion en interiores ***************************************************************************************************************NO FUNCIONA
                if((boolean) snapshot.child("isConected").getValue()) {
                    Double lat = (Double) snapshot.child("lat").getValue();
                    Double lng = (Double) snapshot.child("lng").getValue();
                    userA.drawCircle(mMap, new LatLng(lat, lng), 20);
                    // Comprobación de usuarios
                    if(!isUserA) {
                        // Parte del usuario B
                        try {
                            userB.checkLocation(mMap, userA, userA.getCircle());
                        } catch (AbstractMethodError a) {
                            Toast.makeText(getBaseContext(), "¡Active la ubicación de su dispositivo!", Toast.LENGTH_LONG).show();
                        } catch (NullPointerException n) {
                            System.out.println(n);
                        }
                    } else {
                        // Comprobacion del poligono para el usuario A
                        rtDatabase.child("usuarios").child("usuarioB").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if((boolean) snapshot.child("isInside").getValue()) {
                                    //HACER SONAR EL MOVIL
                                    if (!player.isPlaying()) {
                                        player.start();
                                    }
                                } else {
                                    if (player.isPlaying()) {
                                        player.stop();
                                        try {
                                            player.prepare();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        // FIN
                    }
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