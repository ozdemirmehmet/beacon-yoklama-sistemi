package com.likapalab.sabis2;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mehmet on 10.03.2016.
 */
public class BeaconApplication extends Application implements BootstrapNotifier,BeaconConsumer {

    private BeaconManager beaconManager;
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;

    private static int emptyCounter = 0;
    private static int studentId;
    private static String major, minor;

    private HttpRequestClass httpRequestClass;

    private Beacon firstSeenBeacon = null;
    private Calendar calendar;
    private static Date startTime = null,tempTime = null;
    private static int overflowMinutes = 3;
    private static String TAG = "BeaconApplication";

    public void onCreate(){
        super.onCreate();

        httpRequestClass = new HttpRequestClass();

        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        Region region = new Region("backgroundRegion",
                Identifier.parse("538c5ab2-4dba-43ba-53be-4eb041ad41b0"), null, null);
        regionBootstrap = new RegionBootstrap(this,region);

        backgroundPowerSaver = new BackgroundPowerSaver(this);

        beaconManager.bind(this);
    }

    @Override
    public void didEnterRegion(Region region) {

    }

    @Override
    public void didExitRegion(Region region) {

    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if(beacons.size() > 0){//Taramada bir yer bulunursa
                    Beacon firstBeacon = beacons.iterator().next();
                    emptyCounter = 0;
                    if(firstSeenBeacon != null && firstBeacon.toString().equals(firstSeenBeacon.toString())){
                        //Önceden bir beacon görülmüş ve o beaconla şuan görülen beacon birbirine eşit ise
                        //Yani kullanıcı hala aynı sınıfta ise
                        //O anın saatini alıp eşik değerine ulaşılıp ulaşılmadığını kontrol et
                        //Eşik değerine gelindiyse yoklamaya imza at
                        //İmza atma işlemi başarılı ise startTime ı şuanın zamanı yap
                        //Eğer eşik değerine gelinmediyse herhangi bir işlem yapma

                        //O anın zamanını al
                        calendar = Calendar.getInstance();
                        Date endTime = calendar.getTime();
                        if(getMinutesDif(startTime,endTime) >= overflowMinutes){//Eşik değerine gelindiyse
                            tempTime = endTime;
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            studentId = preferences.getInt("id",0);
                            if(studentId != 0 ) {
                                //Yoklama kontrol et
                                major = firstSeenBeacon.getId2() + "";
                                minor = firstSeenBeacon.getId3() + "";
                                new signControl().execute();
                                Log.d(TAG, "call signControl()");
                            }
                            else{
                                Log.d(TAG,"Login olunmamış");
                            }
                        }else{
                        //sendNotification("Aynı sınıftasın hala");
                            Log.d(TAG,"Aynı sınıfta devam ediliyor");
                        }
                    }
                    else{
                        //Eğer ilk defa bir beacon görülüyorsa veya önceden görülen beacon var ancak şuanki beacona eşit değilse
                        //Yani ilk defa sınıfa girildiyse veya sınıf değiştirildiyse
                        //firstSeenBeacon değerini şuan görülen beacon ile değiştir
                        //startTime değerini o anın değeri ile değiştir.
                        if(firstSeenBeacon == null) {
                            //sendNotification("İlk sefer sınıfa girdin");
                            Log.d(TAG,"İlk sefer bir sınıfa girildi");
                        }
                        else {
                            //sendNotification("Sınıf değiştin");
                            Log.d(TAG,"Sınıf değiştirdin");
                        }
                        //firtSeenBeacon değerini değiştir.
                        firstSeenBeacon = firstBeacon;
                        //startTime ı değiştir.
                        calendar = Calendar.getInstance();
                        startTime = calendar.getTime();
                    }
                }
                else{//Taramada bir yer bulunmamışsa
                    if(firstSeenBeacon != null) {
                        //Önceden bir sınıfa girilmiş ancak şuan herhangi bir sinyal alınmıyorsa
                        //Kullanıcı bir sınıfa girmiş ancak şuan herhangi bir sınıfta değil
                        //firstSeenBeacon değeri ve startTime değeri null olarak set edilir.

                        if(emptyCounter > 2) {//Tarama sıklığı ayarlarına bağlı olarak beacon dan anlık olarak veri alınmadığında sınıftan çıktı hatasını engellemek için kullanıldı
                            //sendNotification("Sınıftan çıktın ama yeteri kadar kalmadığın için imza atmadın");
                            Log.d(TAG, "Sınıftan çıktın herhangi bir işlem yapmadık");

                            //firstSeenBeacon ve startTime set null
                            firstSeenBeacon = null;
                            startTime = null;
                            emptyCounter = 0;
                        }
                        emptyCounter++;
                    }
                }
            }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    private int getMinutesDif(Date startTime, Date endTime) {
        if (startTime == null) {
            return 0;
        } else {
            long difTime = endTime.getTime() - startTime.getTime();
            int difMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(difTime);
            return difMinutes;
        }
    }

    public class signControl extends AsyncTask<Void,Void,Void> {
        String response;
        String responseStatus;

        @Override
        protected Void doInBackground(Void... params) {
            String url = "http://192.168.43.103:8080/sau/sign3.php";
            String parameters = "studentId="+studentId+"&major="+major+"&minor="+minor;
            response = httpRequestClass.httpRequest(url, "POST", parameters, 2000);
            Log.d(TAG,"Sending PHP POST request to=> "+url+"?"+parameters);
            Log.d(TAG, response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                responseStatus = jsonObject.getString("response");
            } catch (JSONException e) {
                Log.d(TAG,e.toString());
                e.printStackTrace();
            }
            Log.d(TAG, "PHP response => " + responseStatus);
            return  null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(responseStatus.equals("OK_SIGN")){
                //Kayıt başarılı
                sendNotification("Bu saat için kaydınız başarıyla tamamlandı",response);
                startTime = tempTime;
                Log.d(TAG,"İmza atıldı");
            }
            else if(!responseStatus.equals("ERROR_SIGN")) {
                startTime = tempTime;
                Log.d(TAG, "Sınıf yok/Ders yok/Ders alınmıyor/Yada bu saat için kaydınız daha önce alınmıştır.");
            }
            //responseStatus error_sign ise herhangi bie işlem yapılmayacak
        }
    }

    private void sendNotification(String msg,String response) {
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent i = new Intent(this,LessonActivity.class);
        i.putExtra("response",response);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                /*new Intent(this, LessonActivity.class)*/i, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Sabis")
                .setSmallIcon(R.drawable.photo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setDefaults(Notification.DEFAULT_SOUND);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());//Notification gosteriliyor.
    }
}
