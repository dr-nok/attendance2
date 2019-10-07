package id.go.pajak.latih.attendance2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements LocationListener {
//https://www.android-examples.com/get-current-gps-coordinates-location-android-programmatically/

    public  static final int RequestPermissionCode  = 1 ;
    Button buttonEnable, buttonGet, btnLogin ;
    TextView textViewLongitude, textViewLatitude ;
    Context context;
    Intent intent1 ;
    Location location;
    LocationManager locationManager ;
    boolean GpsStatus = false ;
    Criteria criteria ;
    String myDomainString, Holder;
    String x_kantor, y_kantor, x_a, x_b, y_a, y_b, dist_a, dist_b;
    String nip_from_server, nip_a_from_server, hasilPresensi, comp_distance_s, nip, pwd, kantorTujuan;
    double x_kantor_d, y_kantor_d, x_device_d, y_device_d, min_distance_d, comp_distance_d;
    int indexCoordX, indexCoordY, indexComma, indexDistance, indexNip, comp_distance_int, comp_distance_point_int, sudahMasukRadius, indexHasilPresensi1, indexHasilPresensi2;

    private TextView mTextViewResult, mTextViewKoordKantorX, mTextViewKoordKantorY, mTextViewMinDistance, mTextViewCompDistance, mTextViewNIP;
    EditText username, password;
    Spinner kantorTujuanSpin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = (EditText) findViewById(R.id.user);
        password = (EditText) findViewById(R.id.password);
        //int selectedId = radioButtonNb;
        btnLogin = (Button) findViewById(R.id.btnLogin);

        mTextViewResult = findViewById(R.id.text_view_result);
        mTextViewKoordKantorX = findViewById(R.id.koord_x_kantor);
        //mTextViewKoordKantorY = findViewById(R.id.koord_y_kantor);
        mTextViewMinDistance = findViewById(R.id.min_distance);
        mTextViewCompDistance = findViewById(R.id.comp_distance);
        //mTextViewNIP = findViewById(R.id.text_nip_server);
        kantorTujuanSpin = findViewById(R.id.kantorTujuan);
        myDomainString = getResources().getString(R.string.myDomain);
        comp_distance_d = 1e6;
        sudahMasukRadius = 0; //1 ARTINYA SUDAH DINYATAKAN MASUK RADIUS YG DITENTUKAN DARI KANTOR TUJUAN
        EnableRuntimePermission();

        buttonEnable = (Button)findViewById(R.id.button);
        buttonGet = (Button)findViewById(R.id.button2);
        textViewLongitude = (TextView)findViewById(R.id.textView);
        textViewLatitude = (TextView)findViewById(R.id.textView2);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        Holder = locationManager.getBestProvider(criteria, false);
        context = getApplicationContext();
        CheckGpsStatus();

        buttonEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent1);
            }
        });


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Preferences.setLoggedInStatus(getBaseContext(), true);

                nip = username.getText().toString();
                pwd = password.getText().toString();
                kantorTujuan = kantorTujuanSpin.getSelectedItem().toString();

                //ambil dari web service
                OkHttpClient client = new OkHttpClient();
                String url = "https://" + myDomainString + "/getpoint2/bynip?_nip=" + nip + "&_pass=" + pwd + "&_pref=" + kantorTujuan;
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            final String myResponse = response.body().string();

                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //mTextViewResult.setText(myResponse);
                                    //x_kantor = myResponse.substring(1,10);
                                    //mTextViewKoordKantor.setText(x_kantor);
                                    indexCoordX = myResponse.lastIndexOf("[");
                                    //indexComma = myResponse.substring(1,2);
                                    indexCoordY = myResponse.indexOf("]");
                                    //mTextViewKoordKantor.setText(String.valueOf(indexCoordX)+" "+String.valueOf(indexCoordY));
                                    x_a = myResponse.substring(indexCoordX+1);
                                    x_b = x_a.substring(0,x_a.indexOf(","));
                                    y_b = x_a.substring((x_a.indexOf(",")+1),x_a.indexOf("]"));
                                    mTextViewKoordKantorX.setText("Koord kantor = (" + x_b + ", " + y_b + ")");

                                    //mTextViewKoordKantorY.setText("Koord y kantor = "+y_b);

                                    indexDistance = myResponse.indexOf("distance_m");
                                    dist_a = myResponse.substring(indexDistance+13);
                                    dist_b = dist_a.substring(0, dist_a.indexOf("}"));
                                    mTextViewMinDistance.setText("Ketentuan radius = " +dist_b + " meter");
                                    //mTextViewResult.setText("HAI HAI");
                                    x_kantor_d = Double.valueOf(x_b);
                                    y_kantor_d = Double.valueOf(y_b);
                                    min_distance_d = Double.valueOf(dist_b);

                                    indexNip = myResponse.indexOf("nip_18");
                                    nip_a_from_server = myResponse.substring(indexNip+11);
                                    nip_from_server = nip_a_from_server.substring(0, 18);
                                    //mTextViewNIP.setText("NIP = "+nip_from_server);

                                }
                            });
                        }
                    }
                });

            }});

        buttonGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CheckGpsStatus();

                if(GpsStatus == true) {
                    if (Holder != null) {
                        if (ActivityCompat.checkSelfPermission(
                                MainActivity.this,
                                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                &&
                                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        location = locationManager.getLastKnownLocation(Holder);
                        locationManager.requestLocationUpdates(Holder, 5000, 3, MainActivity.this);
                        //locationManager.requestLocationUpdates(Holder, 12000, 7, MainActivity.this);
                    }
                }else {

                    Toast.makeText(MainActivity.this, "Pastikan GPS aktif", Toast.LENGTH_LONG).show();

                }
            }
        });



    }

    @Override
    public void onLocationChanged(Location location) {

        textViewLongitude.setText("Koord x device = " + location.getLongitude());
        textViewLatitude.setText("Koord y device = " + location.getLatitude());
        //IF

        if (comp_distance_d > min_distance_d && sudahMasukRadius ==0) {
            x_device_d = Double.valueOf(location.getLongitude());
            y_device_d = Double.valueOf(location.getLatitude());
            comp_distance_d = Math.round((Math.sqrt(  Math.pow(x_device_d-x_kantor_d,2)+Math.pow(y_device_d-y_kantor_d,2)))*111320);    //1 DERAJAT DI EKUATOR = 111320 METER
            comp_distance_s = String.valueOf(comp_distance_d);
            comp_distance_point_int = comp_distance_s.indexOf(".");
            comp_distance_s = comp_distance_s.substring(0, comp_distance_point_int);
            comp_distance_int = Integer.valueOf(comp_distance_s);
            comp_distance_s = String.format("%,d", comp_distance_int);
            //mTextViewCompDistance.setText("Computed distance = "+String.valueOf(comp_distance_d) + " meter");

            //String strtest = String.format("%,d", 123123127);
            //mTextViewCompDistance.setText(strtest.replace(",","."));
            //mTextViewCompDistance.setText("Computed distance = "+ Double(comp_distance_d) + " meter");
            mTextViewCompDistance.setText("Jarak device ke kantor tujuan = "+comp_distance_s.replace(",",".") + " meter");

        }
        else if (sudahMasukRadius == 0) {
            Toast.makeText(MainActivity.this, "Presensi terekam", Toast.LENGTH_LONG).show();
            sudahMasukRadius =1;
            OkHttpClient client2 = new OkHttpClient();
            String url2 = "https://"+ myDomainString + "/ins2/add?_nip=" + nip_from_server + "&x=" + x_device_d + "&y=" + y_device_d;
            Request request2 = new Request.Builder()
                    .url(url2)
                    .build();
            client2.newCall(request2).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()) {
                        final String myResponse2 = response.body().string();

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //mTextViewResult.setText(myResponse2);
                                //Toast.makeText(MainActivity.this, myResponse2, Toast.LENGTH_LONG).show();
                                indexHasilPresensi1 = myResponse2.indexOf("NIP");
                                hasilPresensi = myResponse2.substring(indexHasilPresensi1);
                                indexHasilPresensi2 = hasilPresensi.indexOf("data");
                                hasilPresensi = hasilPresensi.substring(0, indexHasilPresensi2);
                                mTextViewResult.setText("Hasil = "+hasilPresensi);

                            }
                        });
                    }
                }
            });
        }

        else {
            //Toast.makeText(MainActivity.this, "MASUKK", Toast.LENGTH_LONG).show();
            sudahMasukRadius =1;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    public void CheckGpsStatus(){
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void EnableRuntimePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION))
        {
            Toast.makeText(MainActivity.this,"ACCESS_FINE_LOCATION permission allows us to Access GPS in app", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, RequestPermissionCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {
            case RequestPermissionCode:
                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,"Permission ke GPS diterima, aplikasi dapat mengakses data lokasi.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this,"Permission ke GPS ditolak, aplikasi tidak dapat mengakses data lokasi.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

}
