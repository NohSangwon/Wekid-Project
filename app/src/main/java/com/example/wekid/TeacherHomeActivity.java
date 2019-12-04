package com.example.wekid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

public class TeacherHomeActivity extends AppCompatActivity {
    TextView kinderName;
    TextView className;
    TextView teacherName;
    Button deleteBtn;
    Button logoutBtn;
    Button  updateBtn;
    Button postBtn;
    String checkUserType="0";


    TeacherDTO teacherDTO;   // 담임 객체
    String status;           // 서버에서 담당 원아 명수 받아와서 저장. 없으면 0
    List<KidsDTO> kidsArray; // 담당 원아 정보 담을 리스트

    String kinderCode;
    String classCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_home);

        teacherDTO = new TeacherDTO();  // 담임 객체 초기화
        kidsArray = new ArrayList<KidsDTO>(); // 담당 원아 정보 담을 리스트 초기화

        kinderName = (TextView) findViewById(R.id.kinderName);      // 유치원 이름 띄워주는 텍스트뷰
        className = (TextView) findViewById(R.id.className);        // 반 이름 띄워주는 텍스트뷰
        teacherName = (TextView) findViewById(R.id.teacherName);    // 선생님 이름 띄워주는 텍스트뷰
        updateBtn = (Button) findViewById(R.id.updateBtn);
        deleteBtn=(Button) findViewById(R.id.deleteBtn);
        logoutBtn=(Button) findViewById(R.id.logoutBtn);
        postBtn = findViewById(R.id.postBtn);



        // MainActivity에서 보낸 데이터 받아옴 ---
        Intent intent = getIntent();

        teacherDTO.setId(intent.getExtras().getString("id"));
        teacherDTO.setName(intent.getExtras().getString("name"));
        teacherDTO.setKinderName(intent.getExtras().getString("kinderName"));
        teacherDTO.setKinderCode(intent.getExtras().getString("kinderCode"));
        teacherDTO.setClassName(intent.getExtras().getString("className"));
        teacherDTO.setClassCode(intent.getExtras().getString("classCode"));
        teacherDTO.setPhoneNum(intent.getExtras().getString("phoneNum"));

        kinderCode = intent.getExtras().getString("kinderCode");
        classCode = intent.getExtras().getString("classCode");

        kinderName.setText(teacherDTO.getKinderName());
        className.setText(teacherDTO.getClassName());
        teacherName.setText(teacherDTO.getName());
        // 여기까지 -----------------------------

        new TeacherHomeActivity.JSONTask().execute(Helper.ServerAddress + "getKidsListFromTeacher"); //AsyncTask 시작시킴
        updateBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UpdateMemberActivity.class);
                intent.putExtra("id",teacherDTO.getId());
                intent.putExtra("name",teacherDTO.getName());
                intent.putExtra("phoneNum",teacherDTO.getPhoneNum());
                intent.putExtra("checkUserType",checkUserType);
                startActivity(intent);
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showDialog1();
            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //SharedPreferences에 저장된 값들을 로그아웃 버튼을 누르면 삭제하기 위해
                //SharedPreferences를 불러옵니다. 메인에서 만든 이름으로
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = auto.edit();
                //editor.clear()는 auto에 들어있는 모든 정보를 기기에서 지웁니다.
                editor.clear();
                editor.commit();
                Toast.makeText(getApplicationContext(), "로그아웃.", Toast.LENGTH_SHORT).show();
                finish();


            }
        });
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 게시판 으로 들어감
                Intent intent = new Intent(TeacherHomeActivity.this, PostManageActivity.class);
                intent.putExtra("userId", teacherDTO.getId());                  // userId 전달
                intent.putExtra("name", teacherDTO.getName());                  // userId 전달
                intent.putExtra("kinderCode", kinderCode);                  // kinderCode 전달
                intent.putExtra("classCode", classCode);                  // classCode 전달
                intent.putExtra("userCode", "1"); // userCode 0 은 학부모, 1 은 교사
                startActivity(intent);
            }
        });
        // 게시판 버튼 클릭 이벤트 끝
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                jsonObject.accumulate("id", teacherDTO.getId());     // 담당하고 있는 원아를 찾기위해 교사의 id를 서버로 보냄

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

                // 담당 원아가 없는 경우
                if(status.equals("0")) {
                    Toast.makeText(getApplicationContext(), "담당 원아가 없습니다.", Toast.LENGTH_LONG).show();
                }
                // 담당 원아가 1명 이상 있는 경우
                else {
                    JSONArray jsonArray = (JSONArray)jsonObject.get("rows");
                    for(int i = 0; i < Integer.parseInt(status); i++) {
                        JSONObject returnObject = (JSONObject) jsonArray.get(i);
                        KidsDTO kidsDTO = new KidsDTO();
                        kidsDTO.setName(returnObject.get("name").toString());
                        kidsDTO.setBirth(returnObject.get("birth").toString());
                        kidsDTO.setAddress(returnObject.get("address").toString());
                        kidsDTO.setKinderName(returnObject.get("kinderName").toString());
                        kidsDTO.setKinderCode(returnObject.get("kinderCode").toString());
                        kidsDTO.setClassName(returnObject.get("className").toString());
                        kidsDTO.setClassCode(returnObject.get("classCode").toString());
                        kidsDTO.setParentsId(returnObject.get("parentsId").toString());
                        kidsArray.add(kidsDTO);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 여기까지 ------------------------------
        }
    }

    public void  showDialog1(){
        AlertDialog.Builder dig = new AlertDialog.Builder(this);

        dig.setTitle("회원탈퇴");
        dig.setMessage("탈퇴하시겠습니까?");
        dig.setIcon(R.drawable.ic_launcher_foreground).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext(), DeleteMemberActivity.class);
                intent.putExtra("id", teacherDTO.getId());
                intent.putExtra("checkUserType",checkUserType);
                startActivity(intent);
            }
        });//onClickListner : ok누르면 뭘할지,
        dig.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(),"NO",Toast.LENGTH_SHORT).show();



            }
        });//no버튼 누르면 뭐할지 _토스트를 하게 했다.//두가지 버전으로 가능.~.~; .~;
        dig.show();


    }
}
