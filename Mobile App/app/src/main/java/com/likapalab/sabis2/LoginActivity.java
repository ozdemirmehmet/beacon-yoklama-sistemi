package com.likapalab.sabis2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    private String username,password;

    Button loginButton;
    EditText usernameText,passwordText;

    HttpRequestClass httpRequestClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        httpRequestClass = new HttpRequestClass();

        getSharePrefs();

    }

    public class loginControl extends AsyncTask<Void,Void,Void> {

        String response,responseStatus;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(LoginActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Giriş Yapılıyor...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String url = "http://192.168.43.103:8080/sau/login.php";
            String parameters = "studentNo=" + username + "&password=" + password;
            response = httpRequestClass.httpRequest(url, "POST", parameters, 2000);

            Log.d("AAA",response);
            try {
                JSONObject jsonObject = new JSONObject(response);
                responseStatus = jsonObject.getString("response");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            if (responseStatus.equals("OK")) {//Giriş başarılı. Karşılama ekranına geç post isteğinden gelen cevabı bu ekrana aktar
                setSharePrefs();
                Intent i = new Intent(getApplicationContext(), WelcomeActivity.class);
                i.putExtra("studentNo", username);
                i.putExtra("response", response);
                startActivity(i);
                finish();
            } else {//Giriş başarısız. Hatalı kullanıcı adı veya şifre
                Toast.makeText(getApplicationContext(), "Hatalı giriş", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setSharePrefs() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("login",true);
        editor.putString("username", username);
        editor.putString("password",password);
        editor.commit();
    }

    private void getSharePrefs(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean login  = preferences.getBoolean("login", false);
        if(login){//Kayıt var
            username = preferences.getString("username","");
            password = preferences.getString("password","");
            new loginControl().execute();
        }
        else{//Kayıt yok ise
            setContentView(R.layout.activity_login);

            usernameText = (EditText)findViewById(R.id.usernameText);
            passwordText = (EditText)findViewById(R.id.passwordText);

            loginButton = (Button)findViewById(R.id.loginButton);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    username = usernameText.getText().toString();
                    password = passwordText.getText().toString();
                    if (username.equals("") && password.equals("")) {
                        Toast.makeText(getApplicationContext(), "Lütfen tüm alanları doldurunuz", Toast.LENGTH_LONG).show();
                    } else {
                        new loginControl().execute();
                    }
                }
            });
        }
    }
}
