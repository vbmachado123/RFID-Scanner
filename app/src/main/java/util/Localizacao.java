package util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;

import static androidx.core.content.ContextCompat.getSystemService;

public class Localizacao {

    private Context context;
    /* LOCALIZAÇÃO */
    private Location location;
    private LocationManager locationManager;
    private Address endereco;
    private double latitude = 0.0, longitude = 0.0;


    public Localizacao(Context context) {
        this.context = context;
    }

    public static void getLocalizacao(Context context) {
        /* LOCALIZAÇÃO */
        Location location;
        LocationManager locationManager;
        Address endereco;
        double latitude = 0.0, longitude = 0.0;

    /*    locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }*/
    }

}
