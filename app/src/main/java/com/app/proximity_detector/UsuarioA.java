package com.app.proximity_detector;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
//import android.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UsuarioA {

    Circle perimetro;
    Context context;
    ArrayList<LatLng> poligono;
    private DatabaseReference rtDatabase;

    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest locRequest;
    LocationCallback locCallback;

    public UsuarioA(Context context) {
        this.context = context;
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
                    Location location = locationResult.getLastLocation();
                    subirDatos(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(context, "No fue posible obtener su ubicación", Toast.LENGTH_LONG).show();
                }
            }
        };
        rtDatabase = FirebaseDatabase.getInstance().getReference();
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
    }

    public void stopLocUpdates() {
        fusedLocationClient.removeLocationUpdates(locCallback);
    }

    private void subirDatos(double lat, double lng) {
        rtDatabase.child("usuarios").child("usuarioA").child("lat").setValue(lat);
        rtDatabase.child("usuarios").child("usuarioA").child("lng").setValue(lng);
    }

    public void drawPolygon(GoogleMap mapa, LatLng circle) {
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
        mapa.getUiSettings().setMyLocationButtonEnabled(true);
        ArrayList<LatLng> polygonList = new ArrayList<>();
        LatLng point1 = new LatLng(circle.latitude, circle.longitude-0.0001);
        LatLng point2 = new LatLng(circle.latitude, circle.longitude+0.0001);
        LatLng point3 = new LatLng(circle.latitude+0.0001, circle.longitude);
        LatLng point4 = new LatLng(circle.latitude-0.0001, circle.longitude);
        polygonList.add(point1);
        polygonList.add(point3);
        polygonList.add(point2);
        polygonList.add(point4);
        Polygon polygon1 = mapa.addPolygon(new PolygonOptions()
                .clickable(false)
                .add(
                        point1,
                        point3,
                        point2,
                        point4));
        polygon1.setTag("alpha");
        poligono = polygonList;
    }

    public ArrayList<LatLng> getPoligono() {return poligono;}

    public Circle getCircle() {
        return perimetro;
    }

    public void drawCircle(GoogleMap mapa, LatLng coordenates, int radio) {
        mapa.clear();
        CircleOptions cOptions = new CircleOptions();
        cOptions.center(coordenates);
        cOptions.radius(radio);
        cOptions.strokeColor(Color.BLACK);
        cOptions.fillColor(Color.parseColor("#2271cce7"));
        cOptions.strokeWidth(2);
        mapa.addMarker(new MarkerOptions().position(coordenates).title("Usuario A"));
        float zoom = 20.0f;
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenates, zoom));
        perimetro = mapa.addCircle(cOptions);
        // Generar polígono
        drawPolygon(mapa, coordenates);
    }
}
