package com.example.wekid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TeacherManageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private TeacherListAdapter adapter;

    private String kinderCode;

    private ListView teacherListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_manage);

        adapter = new TeacherListAdapter();
        adapter.setContext(this);

        Intent intent = getIntent();
        kinderCode = intent.getStringExtra("kinderCode");

        teacherListView = findViewById(R.id.teacherList);
        teacherListView.setAdapter(adapter);
        teacherListView.setOnItemClickListener(this);

        searchClassList();
        searchTeacherList();
    }

    public void updateTeacher(String teacherId, String newClassCode) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            new TeacherManageActivity.JSONTaskUpdateTeacher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Helper.ServerAddress + "updateTeacher", teacherId, newClassCode); //AsyncTask 시작시킴
        } else {

            new TeacherManageActivity.JSONTaskUpdateTeacher().execute(Helper.ServerAddress + "updateTeacher", teacherId, newClassCode); //AsyncTask 시작시킴
        }
/*
        try {
            Thread.sleep(500); // 서버의 db 가 업데이트 되길 기다린다.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        searchTeacherList();*/

        Toast.makeText(getApplicationContext(), "정보가 업데이트 되었습니다.", Toast.LENGTH_LONG).show();
    }

    private void searchClassList() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            new TeacherManageActivity.JSONTaskClassList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Helper.ServerAddress + "getClassList"); //AsyncTask 시작시킴
        } else {

            new TeacherManageActivity.JSONTaskClassList().execute(Helper.ServerAddress + "getTeacherList"); //AsyncTask 시작시킴
        }
    }

    private void searchTeacherList() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            new TeacherManageActivity.JSONTaskTeacherList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Helper.ServerAddress + "getTeacherList"); //AsyncTask 시작시킴
        } else {

            new TeacherManageActivity.JSONTaskTeacherList().execute(Helper.ServerAddress + "getTeacherList"); //AsyncTask 시작시킴
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        adapter.setSelectedIndex(i, view);
    }

    public class JSONTaskUpdateTeacher extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... item) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                jsonObject.accumulate("kinderCode", kinderCode);     // userId를 서버로 보냄
                jsonObject.accumulate("id", item[1]);     // userId를 서버로 보냄
                jsonObject.accumulate("classCode", item[2]);     // userId를 서버로 보냄

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    //URL url = new URL("http://13.125.112.4:3000/login");  // aws url
                    URL url = new URL(item[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

                    //서버로 보내기위해서 스트림 만듦
                    OutputStream outStream = con.getOutputStream();
                    //버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();//버퍼를 받아줌
                    //----------------------------------- 데이터 보내기 끝 ----------------------------------------//

                    //------------------------------- 서버로부터 데이터를 받음 -------------------------------------//
                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌

                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // 서버 DB 가 업데이트 되면 teacher list 업데이트
            searchTeacherList();
            // 여기까지 ------------------------------
        }
    }


    public class JSONTaskTeacherList extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                jsonObject.accumulate("kinderCode", kinderCode);     // userId를 서버로 보냄

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    //URL url = new URL("http://13.125.112.4:3000/login");  // aws url
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

                    //서버로 보내기위해서 스트림 만듦
                    OutputStream outStream = con.getOutputStream();
                    //버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();//버퍼를 받아줌
                    //----------------------------------- 데이터 보내기 끝 ----------------------------------------//

                    //------------------------------- 서버로부터 데이터를 받음 -------------------------------------//
                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌

                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // 서버에서 받아온 json 가공 --------------
            try {
                JSONObject jsonObject = new JSONObject(result);
                int status = Integer.parseInt(jsonObject.get("status").toString());

                if(status > 0) {

                    adapter.clear();

                    JSONArray arrayObject = new JSONArray(jsonObject.get("result").toString());
                    for (int i = 0; i < arrayObject.length(); i++) {

                        JSONObject obj = new JSONObject(arrayObject.get(i).toString());

                        TeacherDTO teacher = new TeacherDTO();
                        teacher.setId(obj.getString("id"));
                        teacher.setName(obj.getString("name"));
                        teacher.setKinderName(obj.getString("kinderName"));
                        teacher.setClassName(obj.getString("className"));
                        teacher.setClassCode(obj.getString("classCode"));

                        adapter.addItem(teacher);
                    }

                    adapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 여기까지 ------------------------------
        }
    }

    public class JSONTaskClassList extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                org.json.JSONObject jsonObject = new org.json.JSONObject();

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    //URL url = new URL("http://13.125.112.4:3000/login");  // aws url
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

                    //서버로 보내기위해서 스트림 만듦
                    OutputStream outStream = con.getOutputStream();
                    //버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();//버퍼를 받아줌
                    //----------------------------------- 데이터 보내기 끝 ----------------------------------------//

                    //------------------------------- 서버로부터 데이터를 받음 -------------------------------------//
                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌

                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // 서버에서 받아온 json 가공 --------------
            try {
                JSONObject jsonObject = new JSONObject(result);
                int status = Integer.parseInt(jsonObject.get("status").toString());

                if(status > 0) {

                    JSONArray arrayObject = new JSONArray(jsonObject.get("result").toString());
                    for (int i = 0; i < arrayObject.length(); i++) {

                        JSONObject obj = new JSONObject(arrayObject.get(i).toString());

                        if (obj.getString("kinderCode").equals(kinderCode)) {

                            String tClassCode = obj.getString("classCode");

                            adapter.addClass(obj.getString("classCode"), obj.getString("className"));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 여기까지 ------------------------------
        }
    }
}
