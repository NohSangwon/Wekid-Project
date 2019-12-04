package com.example.wekid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class InsertPrincipalActivity extends AppCompatActivity {

    private EditText inputId;
    private EditText inputPwd;
    private EditText inputName;
    private EditText inputPhoneNum;
    private Button joinBtn;



    String status;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insertprincipal);
        inputId=(EditText) findViewById(R.id.inputId);
        inputPwd=(EditText) findViewById(R.id.inputPwd);
        inputName = (EditText) findViewById(R.id.inputName);
        inputPhoneNum = (EditText) findViewById(R.id.inputPhoneNum);;
        joinBtn = (Button) findViewById(R.id.joinBtn);





        joinBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 버튼을 클릭하면 AsyncTask를 실행함. 아래 url이 doInBackground의 파라미터로 들어감
                if(inputId.getText().toString().length()==0 )
                {
                    Toast.makeText(getApplicationContext(), "ID를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else if(inputPwd.getText().toString().length()==0){
                    Toast.makeText(getApplicationContext(), "비밀번호 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else if(inputName.getText().toString().length()==0 ){
                    Toast.makeText(getApplicationContext(), "이름 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else if(inputPhoneNum.getText().length()==0){
                    Toast.makeText(getApplicationContext(), "전화번호 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else {
                    new InsertPrincipalActivity.JSONTask().execute(Helper.ServerAddress + "insertprincipal"); //AsyncTask 시작시킴
                }
            }
        });


    }

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                jsonObject.accumulate("inputId", inputId.getText().toString());     // inputid 서버로 보냄
                jsonObject.accumulate("inputPwd", inputPwd.getText().toString());     // inputPwd 서버로 보냄
                jsonObject.accumulate("inputName", inputName.getText().toString());     // inputName 서버로 보냄
                jsonObject.accumulate("inputPhoneNum", inputPhoneNum.getText().toString());     // inputPhoneNum 서버로 보냄
                jsonObject.accumulate("kinderCode", "0");

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
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

                // 회원가입 실패
                if(status.equals("0")) {

                    Toast.makeText(getApplicationContext(), "아이디 중복입니다.", Toast.LENGTH_LONG).show();
                }
                else{

                    Intent intent = new Intent(getApplicationContext(), InsertKinderActivity.class);
                    intent.putExtra("id",inputId.getText().toString());
                    startActivity(intent);
                    finish();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
