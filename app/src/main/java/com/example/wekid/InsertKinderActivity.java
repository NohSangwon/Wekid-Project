package com.example.wekid;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;



public class InsertKinderActivity extends AppCompatActivity {

    Button cancleBtn;
    Button insertBtn;

    EditText inputName;
    EditText inputAdd;
    EditText inputPN;
    TextView txtView;


    String kinderCode;
    static String lastKinderCode;
    String id;


    String status;
    private final static int MAX_LENGTH = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_kinder);

        insertBtn = (Button)findViewById(R.id.InsertBtn);
        cancleBtn = (Button)findViewById(R.id.kinderCancleBtn);

        inputName = (EditText)findViewById(R.id.inputName);
        inputAdd = (EditText)findViewById(R.id.inputAdd);
        inputPN = (EditText)findViewById(R.id.inputPN);
        txtView = (TextView)findViewById(R.id.insertKinderlogo);

        new InsertKinderActivity.JSONTask().execute(Helper.ServerAddress + "getKinderList");
        InputFilter filterNum = new InputFilter() { // 숫자만 입력할 수 있는 필터 적용
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern ps = Pattern.compile("^[0-9]*$");
                if (!ps.matcher(source).matches()) {
                    return "";
                }
                return null;
            }
        };

        Intent intent = getIntent();

        id = intent.getExtras().getString("id");


        Toast.makeText(getApplicationContext(), lastKinderCode, Toast.LENGTH_SHORT).show();


        inputPN.setFilters(new InputFilter[] { filterNum, new InputFilter.LengthFilter(MAX_LENGTH)});

        insertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int tmp;
                tmp = (Integer.parseInt(lastKinderCode) + 1);
                kinderCode = Integer.toString(tmp);

                    new InsertKinderTask().execute(Helper.ServerAddress + "insertKinder");
                    finish();

            }
        });


        cancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public class InsertKinderTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                jsonObject.accumulate("kinderCode", kinderCode);
                jsonObject.accumulate("kinderName", inputName.getText().toString());
                jsonObject.accumulate("address", inputAdd .getText().toString());
                jsonObject.accumulate("PhoneNum", inputPN.getText().toString());
                jsonObject.accumulate("id", id);

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


                if(status.equals("0")) {

                    Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_LONG).show();
                }
                else{

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    public  class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("GET");
                    con.connect();

                    //------------------------------- 서버로부터 데이터를 받음 -------------------------------------//
                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }

                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        if (reader != null) {
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
            if(result!=null) {

                // 서버에서 받아온 json 가공 --------------
                try {
                    JSONObject jsonObject = new JSONObject(result);

                    JSONArray array = (JSONArray) jsonObject.get("rows");

                    List<Kinder> kinderInfoResult = new ArrayList<>();


                    for (int i = 0; i < array.length(); i++) {
                        kinderInfoResult.add(new Kinder(
                                ((JSONObject) (array).get(i)).getString("kinderCode"),
                                ((JSONObject) (array).get(i)).getString("kinderName"),
                                ((JSONObject) (array).get(i)).getString("address"),
                                ((JSONObject) (array).get(i)).getString("PhoneNum")
                        ));
                    }


                    lastKinderCode=kinderInfoResult.get((kinderInfoResult.size()-1)).toString().split(" / ")[0];

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }




        }
    }


}
