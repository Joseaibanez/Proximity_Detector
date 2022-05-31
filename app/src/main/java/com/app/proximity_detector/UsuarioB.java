package com.app.proximity_detector;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Looper;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.PolyUtil;

import java.io.IOException;

public class UsuarioB {

    Context context;
    MediaPlayer player;
    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest locRequest;
    LocationCallback locCallback;
    GoogleMap map;
    UsuarioA userA;
    private DatabaseReference rtDatabase;

    public UsuarioB(Context context, GoogleMap gMap, UsuarioA usuA) {
        this.context = context;
        map = gMap;
        userA = usuA;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locRequest = LocationRequest.create();
        locRequest.setInterval(4000);
        locRequest.setFastestInterval(2000);
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    //map.clear();
                    Location myLocation = locationResult.getLastLocation();
                    LatLng myCoordenates = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    // Se genera el punto de ubicacion
                    //map.addMarker(new MarkerOptions().position(myCoordenates).title("Usuario A"));
                    //float zoom = 20.0f;
                    //map.moveCamera(CameraUpdateFactory.newLatLngZoom(myCoordenates, zoom));
                    // Se obtiene el perimetro generado por el usuario A
                    Circle perim = userA.getCircle();
                    float[] distance = new float[2];
                    Location.distanceBetween( myLocation.getLatitude(), myLocation.getLongitude(),
                            perim.getCenter().latitude, perim.getCenter().longitude, distance);
                    // Comprobacion area
                    if( distance[0] < perim.getRadius() ){
                        Vibrator vib = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
                        //noinspection MissingPermission
                        vib.vibrate(1000);
                    }

                    //COMPROBACION POLIGONO
                    //LatLng myLocat = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    if(PolyUtil.containsLocation(myCoordenates, userA.getPoligono(), false)) {
                        if (!player.isPlaying()) {
                            player.start();
                            rtDatabase.child("usuarios").child("usuarioB").child("isInside").setValue(true);
                        }
                    }
                    else {
                        setIsOutside();
                        //rtDatabase.child("usuarios").child("usuarioB").child("isInside").setValue(false);
                        stopMediaPlayer();
                    }
                    //FIN POLÍGONO
                } else {
                    Toast.makeText(context, "No fue posible obtener su ubicación", Toast.LENGTH_LONG).show();
                }
            }
        };
        rtDatabase = FirebaseDatabase.getInstance().getReference();
        Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        player = MediaPlayer.create(context, ringtone);
    }

    public void startLocUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locRequest, locCallback, Looper.getMainLooper());
        map.setMyLocationEnabled(true);
    }

    public void stopLocUpdates() {
        fusedLocationClient.removeLocationUpdates(locCallback);
    }

    public void stopMediaPlayer() {
        if (player.isPlaying()) {
            player.stop();
            try {
                player.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void setIsOutside() {
        rtDatabase.child("usuarios").child("usuarioB").child("isInside").setValue(false);
    }

}