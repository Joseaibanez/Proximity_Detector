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
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import java.io.IOException;

public class UsuarioB {

    Context context;
    MediaPlayer player;
    Vibrator vib;
    private final FusedLocationProviderClient fusedLocationClient;
    LocationRequest locRequest;
    LocationCallback locCallback;
    GoogleMap map;
    UsuarioA userA;
    String id;
    private final DatabaseReference rtDatabase;

    public UsuarioB(Context context, GoogleMap gMap, UsuarioA usuA, String idUser) {
        this.context = context;
        map = gMap;
        userA = usuA;
        id = idUser;
        rtDatabase = FirebaseDatabase.getInstance().getReference();
        Uri ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        player = MediaPlayer.create(context, ringtone);
        vib = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        // obtencion de ubicacion actual
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locRequest = LocationRequest.create();
        locRequest.setInterval(4000);
        locRequest.setFastestInterval(2000);
        locRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY );
        locCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    Location myLocation = locationResult.getLastLocation();
                    rtDatabase.child("usuarios").child("usuariosB").child(id).child("lat").setValue(myLocation.getLatitude());
                    rtDatabase.child("usuarios").child("usuariosB").child(id).child("lng").setValue(myLocation.getLongitude());
                    LatLng myCoordenates = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    // Se obtiene el perimetro generado por el usuario A
                    Circle perim = userA.getCircle();
                    float[] distance = new float[2];
                    Location.distanceBetween( myLocation.getLatitude(), myLocation.getLongitude(),
                            perim.getCenter().latitude, perim.getCenter().longitude, distance);
                    // Comprobacion area
                    if( distance[0] < perim.getRadius() ){
                        vib.vibrate(1000);
                    } else {
                        vib.cancel();
                    }
                    //COMPROBACION POLIGONO
                    if(PolyUtil.containsLocation(myCoordenates, userA.getPoligono(), false)) {
                        rtDatabase.child("usuarios").child("usuariosB").child(id).child("isInside").setValue(true);
                    }
                    else {
                        setIsOutside();
                    }
                    //FIN POLÍGONO
                } else {
                    Toast.makeText(context, "No fue posible obtener su ubicación", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    // Comienza a actualizar la ubicación del usuario
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
    }

    // Detiene las actualizaciones de ubicación
    public void stopLocUpdates() {
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locCallback);
        }
    }
    public void startMediaPlayer() {
        if (!player.isPlaying()) {
            player.start();
        }
    }

    // Detiene el reproductor de sonido
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

    // Indica en la base de datos que el Usuario B se encuentra fuera del polígono del Usuario A
    public void setIsOutside() {
        rtDatabase.child("usuarios").child("usuariosB").child(id).child("isInside").setValue(false);
    }


    public String getId() {
        return id;
    }

}