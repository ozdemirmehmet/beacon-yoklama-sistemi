package com.likapalab.sabis2;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mehmet on 15.03.2016.
 */
public class LessonActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        String response = this.getIntent().getStringExtra("response");

        TextView classNameText = (TextView)findViewById(R.id.classNameText);
        TextView lessonNameText = (TextView)findViewById(R.id.lessonNameText);
        TextView termText = (TextView)findViewById(R.id.termText);
        TextView instructorDataText = (TextView)findViewById(R.id.instructorDataText);

        String className = "",lessonName = "", term = "", instructor = "";
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject lesson = jsonObject.getJSONObject("lesson");
            className = lesson.getString("className");
            lessonName = lesson.getString("name");
            term = lesson.getString("term");
            JSONObject instructorJson = lesson.getJSONObject("instructor");
            instructor = instructorJson.getString("rank")+instructorJson.getString("firstName")+" "+instructorJson.getString("lastName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        lessonNameText.setText(lessonName);
        classNameText.append("" + className);
        termText.append("" + term);
        instructorDataText.setText(instructor);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

}
