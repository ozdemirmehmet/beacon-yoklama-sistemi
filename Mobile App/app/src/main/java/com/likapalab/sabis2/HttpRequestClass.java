package com.likapalab.sabis2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Mehmet on 19.02.2016.
 */
public class HttpRequestClass {

    static InputStream data;
    static String data_string;

    public String httpRequest(String theUrl, String method, String urlParameters, int timeOut) {

        //theUrl = http isteği gönderilecek adres
        //method = Hangi http isteği yapılacak (GET, POST)
        //urlParamaters = Yapılacak istekte bulunacak parametreler
        //timeOut = Sunucudan cevap gelmezse isteğin zaman aşımı süresi

        try {

            if (method == "POST") { // Eğer metod post ise buraya girilecek

                URL url = new URL(theUrl);//url i oluştur.
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //connection.setConnectTimeout(timeOut);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setDoOutput(true);
                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(urlParameters);
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();
                data = connection.getInputStream();
            } else if (method == "GET") {//Eğer metod get ise buraya girilecek

                URL url = new URL(theUrl + "?" + urlParameters);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(timeOut);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                int responseCode = connection.getResponseCode();
                data = connection.getInputStream();
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(data), 8);
            String line = "";
            StringBuilder responseOutput = new StringBuilder();
            while ((line = br.readLine()) != null) {
                responseOutput.append(line);
            }
            br.close();
            data_string = responseOutput.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data_string;
    }
}
