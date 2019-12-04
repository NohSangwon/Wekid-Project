package com.example.wekid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class KinderActivity extends AppCompatActivity {


    ListView kinderListView;
    EditText inputSearch;
    ImageButton searchBtn;
    InputMethodManager imm;
    String word;
    String name;
    String address;
    List<String> kinderList ;
    ArrayAdapter<String> adapter ;
    String status;
    Button okBtn;

    String kinderCode ;


    protected int clickPosition = -1;
    public  OnAsycnTaskEnd task = new OnAsycnTaskEnd() {


        @Override
        public void onTaskEnd2(List<Kinder> result) {
            kinderList.clear();
            for (int i = 0; i < result.size(); ++i)
                if (result.get(i).toString().split(" / ")[1].contains(word)) // 리스트중 검색창에 입력한 단어가 있다면 리스트뷰에 추가.
                    kinderList.add(result.get(i).toString());
            adapter.notifyDataSetChanged();

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kinder);

        okBtn=(Button) findViewById(R.id.okBtn);
        kinderListView = (ListView) findViewById(R.id.kinderListView);
        searchBtn = (ImageButton) findViewById(R.id.searchBtn);
        inputSearch = (EditText) findViewById(R.id.inputSearch);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        kinderList = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, kinderList);

        kinderListView.setAdapter(adapter);

        inputSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                kinderListView.setVisibility(View.INVISIBLE);
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), InsertTeacherActivity.class);
                intent.putExtra("kinderCode",kinderCode);
                intent.putExtra("kinderName",name);
                startActivity(intent);
                finish();



            }
        });
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                word = inputSearch.getText().toString();

                new JSONTask().execute(Helper.ServerAddress + "getKinderList");
                kinderListView.setBackgroundColor(Color.WHITE);
                imm.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);
                inputSearch.setText("");

                okBtn.setVisibility(View.VISIBLE);


                kinderListView.setVisibility(View.VISIBLE);
            }
        });


        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                okBtn.setEnabled(true);
                clickPosition = position;
                kinderCode = adapter.getItem(position).split(" ")[0];
                name = adapter.getItem(position).split(" / ")[1];
                address = adapter.getItem(position).split(" / ")[2];
            }
        };

        kinderListView.setOnItemClickListener(listener);

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
                    status = jsonObject.get("status").toString();

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

                    task.onTaskEnd2(kinderInfoResult);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }
    }
}
