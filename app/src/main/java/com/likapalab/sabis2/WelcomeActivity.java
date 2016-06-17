package com.likapalab.sabis2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mehmet on 14.03.2016.
 */
public class WelcomeActivity extends Activity {

    TextView nameText,studentNoText,facultyText,departmentText;
    Button logoutButton;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();

        nameText = (TextView)findViewById(R.id.nameText);
        studentNoText = (TextView)findViewById(R.id.studentNoText);
        facultyText = (TextView)findViewById(R.id.facultyText);
        departmentText = (TextView)findViewById(R.id.departmentText);

        setStudent(this.getIntent().getStringExtra("response"), this.getIntent().getStringExtra("studentNo"));

        logoutButton = (Button)findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.clear();
                editor.commit();
                Intent i = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }


    public void setStudent(String response,String studentNo){
        int id = 0;
        String firstName="",lastName="",faculty="",department="";
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject student = jsonObject.getJSONObject("student");
            id = student.getInt("id");
            firstName = student.getString("firstName");
            lastName = student.getString("lastName");
            faculty = student.getString("faculty");
            department = student.getString("department");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        editor.putInt("id",id);
        editor.commit();

        nameText.setText(firstName + " " + lastName);
        studentNoText.setText(studentNo);
        facultyText.setText(faculty);
        departmentText.setText(department);
    }
}
