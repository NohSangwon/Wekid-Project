package com.example.wekid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private Button loginBtn;
    private Button joinBtn;
    private EditText inputId;
    private EditText inputPwd;
    private long lastTimeBackPressed; //

    RadioGroup userTypeGroup;
    RadioButton teacherBtn;
    RadioButton parentsBtn;
    RadioButton principalBtn;
    String checkUserType;


    ////////////////////// 서버에서 받아올 정보들 담는 변수 ///////////////////
    String status;        // 로그인 실패 = 0, 로그인 성공 = 1
    String id;
    String name ;         // 이름
    String phoneNum;
    String kinderName;    // 유치원 이름, 교사만 받아옴
    String className;     // 반 이름, 교사만 받아옴
    String kinderCode;
    String classCode;

    String parentsKinderInfo;
    //////////////////////////////////////////////////////////////////////



    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - lastTimeBackPressed < 1500){
            finish();
            return;
        }
        Toast.makeText(getApplicationContext(), "뒤로 버튼을 한 번 더 눌러 종료합니다.", Toast.LENGTH_SHORT).show();
        lastTimeBackPressed = System.currentTimeMillis();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginBtn = (Button) findViewById(R.id.loginBtn);
        inputId = (EditText) findViewById(R.id.inputId);
        inputPwd = (EditText) findViewById(R.id.inputPwd);
        joinBtn = (Button) findViewById(R.id.joinBtn);

        userTypeGroup = (RadioGroup)findViewById(R.id.userTypeGroup);
        teacherBtn = (RadioButton) findViewById(R.id.teacherBtn);
        parentsBtn = (RadioButton) findViewById(R.id.parentsBtn);
        principalBtn=(RadioButton) findViewById(R.id.principalBtn);

        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);

        //처음에는 SharedPreferences에 아무런 정보도 없으므로 값을 저장할 키들을 생성한다.
        // getString의 첫 번째 인자는 저장될 키, 두 번쨰 인자는 값입니다.
        // 첨엔 값이 없으므로 키값은 원하는 것으로 하시고 값을 null을 줍니다.
        id = auto.getString("id",null);
        name = auto.getString("name",null);
        phoneNum = auto.getString("phoneNum",null);
        kinderName = auto.getString("kinderName",null);
        className = auto.getString("className",null);
        checkUserType =auto.getString("UserType",null);
        kinderCode = auto.getString("kinderCode",null);
        classCode = auto.getString("classCode",null);
        parentsKinderInfo = auto.getString("parentsKinderInfo",null);

        if(id !=null ){  // 로그인하고 로그아웃을 안했을 때 저장된 정보를 보낸다.
            if(checkUserType.equals("0")){

                // teacherActivity로 넘김
                Intent intent = new Intent(getApplicationContext(), TeacherHomeActivity.class);

                Log.i("data : ", id + " " + name + " " + kinderName + " " + className + " " + phoneNum);
                // 데이터도 같이 전달
                intent.putExtra("id", id);
                intent.putExtra("name", name);
                intent.putExtra("kinderName", kinderName);
                intent.putExtra("className", className);
                intent.putExtra("phoneNum", phoneNum);
                intent.putExtra("kinderCode", kinderCode);
                intent.putExtra("classCode", classCode);

                startActivity(intent);
                finish();
            }
            else if(checkUserType.equals("1")){
                Intent intent = new Intent(getApplicationContext(), ParentsHomeActivity.class);

                // 데이터도 같이 전달
                intent.putExtra("name", name);
                intent.putExtra("id", id);
                intent.putExtra("phoneNum", phoneNum);
                intent.putExtra("parentsKinderInfo", parentsKinderInfo);
                startActivity(intent);
                finish();
            }
            else if(checkUserType.equals("2")){
                Intent intent = new Intent(getApplicationContext(), PrincipalHomeActivity.class);

                intent.putExtra("id", id);
                intent.putExtra("name", name);
                intent.putExtra("kinderName", kinderName);
                intent.putExtra("kinderCode", kinderCode);
                intent.putExtra("phoneNum", phoneNum);

                startActivity(intent);
                finish();
            }



        }

        else if(id ==null ) { // 저장된 로그인 정보가 없을 때 로그인한다.
            Toast.makeText(getApplicationContext(), "로그인이 되어있지않습니다.", Toast.LENGTH_SHORT).show();

            // 로그인 버튼 클릭 이벤트 ----------------
            loginBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    // 라디오 버튼 체크
                    if (teacherBtn.isChecked() == true) {
                        checkUserType = "0";
                        new JSONTask().execute(Helper.ServerAddress + "login"); //AsyncTask 시작시킴

                    } else if (parentsBtn.isChecked() == true) {
                        checkUserType = "1";
                        // 로그인 버튼을 클릭하면 AsyncTask를 실행함. 아래 url이 doInBackground의 파라미터로 들어감
                        new JSONTask().execute(Helper.ServerAddress + "login"); //AsyncTask 시작시킴
                    } else if (principalBtn.isChecked() == true) {
                        checkUserType = "2";
                        // 로그인 버튼을 클릭하면 AsyncTask를 실행함. 아래 url이 doInBackground의 파라미터로 들어감
                        new JSONTask().execute(Helper.ServerAddress + "login"); //AsyncTask 시작시킴
                    } else {
                        Toast.makeText(getApplicationContext(), "회원구분을 선택하세요.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        // 여기까지 ------------------------------

        // 회원가입 버튼 클릭 이벤트 ---------------
        joinBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 회원가입 activity로 넘어가기 위한 코드
                Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
                startActivity(intent);
            }
        });
        // 여기까지 ------------------------------
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                jsonObject.accumulate("inputId", inputId.getText().toString());     // inputId를 서버로 보냄
                jsonObject.accumulate("inputPwd", inputPwd.getText().toString());   // inputPwd를 서버로 보냄
                jsonObject.accumulate("checkUserType", checkUserType); // 라디오버튼으로 체크한 userType을 서버로 보냄

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
                status = jsonObject.get("status").toString();

                // 로그인 성공
                if(status.equals("1")) {
                    SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                    // 로그인 버튼을 클릭하면 AsyncTask를 실행함. 아래 url이 doInBackground의 파라미터로 들어감
                    SharedPreferences.Editor autoLogin = auto.edit();


                    id = jsonObject.get("id").toString();
                    name = jsonObject.get("name").toString();
                    phoneNum = jsonObject.get("phoneNum").toString();
                    autoLogin.putString("id", id);
                    autoLogin.putString("UserType",checkUserType);
                    autoLogin.putString("name",name);
                    autoLogin.putString("phoneNum",phoneNum);
                    if(principalBtn.isChecked() == true) {    // 원장인 경우
                        kinderName = jsonObject.get("kinderName").toString();
                        kinderCode = jsonObject.get("kinderCode").toString();
                        autoLogin.putString("kinderName", kinderName);
                        autoLogin.putString("kinderCode", kinderCode);
                    }
                    else if(teacherBtn.isChecked() == true) {    // 교사인 경우
                        kinderName = jsonObject.get("kinderName").toString();
                        className = jsonObject.get("className").toString();
                        kinderCode = jsonObject.get("kinderCode").toString();
                        classCode = jsonObject.get("classCode").toString();
                        autoLogin.putString("kinderName", kinderName);
                        autoLogin.putString("className", className);
                        autoLogin.putString("kinderCode", kinderCode);
                        autoLogin.putString("classCode", classCode);
                    }else if (parentsBtn.isChecked() == true) { // 학부모인 경우
                        parentsKinderInfo = jsonObject.get("parentsKinderInfo").toString();
                        autoLogin.putString("parentsKinderInfo", parentsKinderInfo);
                    }
                    autoLogin.commit();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 여기까지 ------------------------------

            // 로그인 상태 띄워주기 //
            if(status.equals("1")) {    // 로그인이 성공했다면
                //Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_LONG).show();


                if(principalBtn.isChecked()==true){
                    Intent intent = new Intent(getApplicationContext(), PrincipalHomeActivity.class);

                    intent.putExtra("id", id);
                    intent.putExtra("name", name);
                    intent.putExtra("kinderCode", kinderCode);
                    intent.putExtra("kinderName", kinderName);
                    intent.putExtra("phoneNum", phoneNum);

                    startActivity(intent);
                    finish();
                }
                else if(teacherBtn.isChecked() == true) {  // 로그인이 성공했는데 교사라면
                    // teacherActivity로 넘김
                    Intent intent = new Intent(getApplicationContext(), TeacherHomeActivity.class);

                    Log.i("data : ", id + " " + name + " " + kinderName + " " + className + " " + phoneNum);
                    // 데이터도 같이 전달
                    intent.putExtra("id", id);
                    intent.putExtra("name", name);
                    intent.putExtra("kinderName", kinderName);
                    intent.putExtra("className", className);
                    intent.putExtra("phoneNum", phoneNum);
                    intent.putExtra("kinderCode", kinderCode);
                    intent.putExtra("classCode", classCode);

                    startActivity(intent);
                    finish();
                }
                else if(parentsBtn.isChecked() == true) { // 로그인이 성공했는데 학부모라면
                    // parentsActivity로 넘김
                    Intent intent = new Intent(getApplicationContext(), ParentsHomeActivity.class);

                    // 데이터도 같이 전달
                    intent.putExtra("name", name);
                    intent.putExtra("id", id);
                    intent.putExtra("phoneNum", phoneNum);
                    intent.putExtra("parentsKinderInfo", parentsKinderInfo);

                    startActivity(intent);
                    finish();
                }

            } else {    // 로그인 실패
                Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show();
            }
        }
    }
}
