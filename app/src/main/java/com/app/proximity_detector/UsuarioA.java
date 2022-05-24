package com.app.proximity_detector;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class UsuarioA {

    Circle perimetro;
    Context context;
    ArrayList<LatLng> poligono;

    public UsuarioA(Context context) {
        this.context = context;
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
        perimetro = mapa.addCircle(cOptions);
        // Generar polígono
        drawPolygon(mapa, coordenates);
    }
}
