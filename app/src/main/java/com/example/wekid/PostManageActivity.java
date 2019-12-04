package com.example.wekid;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.HashMap;
import java.util.List;

public class PostManageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, View.OnClickListener {

    private final String title = "제목";

    private final String user = "작성자";

    private Spinner spTypes; // 클래스를 나타내는 spinner

    private ListView lvPostList; // 게시판 리스트를 보여주는 ListView

    private PostListAdapter adapter; // Post 리스트를 View 에다가 맞춰주는 adapter

    private PostListAdapter tempCopiedapter; // 검색 시 사용되는 temp adapter

    private ArrayList<String> postTypeSpinnerItems = new ArrayList<>(); // 클래스 이름을 저장하는 ArrayList

    private ArrayList<String> searchTypeSpinnerItems = new ArrayList<>(); // 검색 타입을 저장하는 ArrayList

    private HashMap<String, String> classCodeName = new HashMap<String, String>(); // 클래스 이름과 코드 번호를 매칭하는 HashMap

    private ArrayAdapter postTypeArrayAdapter; // 클래스 이름을 Spinner 에 대입해주는 ArrayAdapter

    private ArrayAdapter searchTypeArrayAdapter; // 검색 타입을 Spinner 에 대입해주는 ArrayAdapter

    private FloatingActionButton newPost; // 새 게시글 추가 버튼

    private String userId; // 현재 사용자 id

    private String name;

    private String kinderCode; // 현재 사용자의 kinderCode

    private String classCode; // 현재 사용자의 classCode

    private String selectedClassCode; // Spinner 에서 선택된 classCode (전체 또는 사용자의 classCode)

    private List<String> classCodeArray; // 클래스 코드가 저장되는 List

    private String userCode; // 사용자 코드 0: 학부모, 1: 교사, 2: 원장

    private Spinner spSearch; // 검색 타입 Spinner

    private EditText tvSearch; // 검색 입력 EditText

    private Button btnSearch; // 검색 버튼

    private String currentSearchCode; // Spinner 에서 선택된 검색 타입

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_manage);

        // 초기화
        classCodeArray = new ArrayList<>();

        adapter = new PostListAdapter();
        tempCopiedapter = new PostListAdapter();

        spTypes = findViewById(R.id.sp_types);
        lvPostList = findViewById(R.id.lv_post_list);
        newPost = findViewById(R.id.fab);
        newPost.setOnClickListener(this);

        spSearch = findViewById(R.id.sp_search);
        tvSearch = findViewById(R.id.tv_search);
        btnSearch = findViewById(R.id.btn_search);

        tvSearch.setOnClickListener(this);

        // EditText 에 키보드 숨김
        tvSearch.setInputType(0);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tvSearch.getWindowToken(), 0); // hide keyboard

        lvPostList.setAdapter(adapter);
        lvPostList.setOnItemClickListener(this);

        spTypes.setOnItemSelectedListener(this);
        spSearch.setOnItemSelectedListener(this);

        // 검색 Spinner 에 들어갈 아이템 입력
        searchTypeSpinnerItems.clear();
        searchTypeSpinnerItems.add(title);
        searchTypeSpinnerItems.add(user);
        searchTypeArrayAdapter = new ArrayAdapter<>(PostManageActivity.this, android.R.layout.simple_spinner_dropdown_item, searchTypeSpinnerItems);
        spSearch.setAdapter(searchTypeArrayAdapter); // Spinner 에 adapter 설정
        searchTypeArrayAdapter.notifyDataSetChanged();
        currentSearchCode = searchTypeSpinnerItems.get(0);
        btnSearch.setOnClickListener(this);

        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");
        name = intent.getStringExtra("name");
        kinderCode = intent.getStringExtra("kinderCode");
        userCode = intent.getStringExtra("userCode"); // userCode 0 은 학부모, 1 은 교사, 2 은 원장

        if (userCode.equals("0")) {

            newPost.hide();
        }

        if (intent.hasExtra("classCode")) {

            classCode = intent.getStringExtra("classCode");
            selectedClassCode = classCode;
            classCodeArray.add(classCode);
        } else if (intent.hasExtra("parentsKinderInfo")) {

            classCodeArray.clear();
            String info = intent.getStringExtra("parentsKinderInfo");
            try {
                JSONArray jsonArray = new JSONArray(info);
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject obj = jsonArray.getJSONObject(i);
                    classCodeArray.add(obj.getString("classCode"));
                }

                selectedClassCode = classCodeArray.get(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        searchClassList();
        searchPosts();
    }

    private void searchClassList() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            new JSONTaskClassList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Helper.ServerAddress + "getClassList"); //AsyncTask 시작시킴
        } else {

            new JSONTaskClassList().execute(Helper.ServerAddress + "getClassList"); //AsyncTask 시작시킴
        }
    }

    private void searchPosts() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            new JSONTaskPostList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Helper.ServerAddress + "getPostList"); //AsyncTask 시작시킴
        } else {

            new JSONTaskPostList().execute(Helper.ServerAddress + "getPostList"); //AsyncTask 시작시킴
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        // 포스트 리스트에서 하나 선택 시 해당 포스트의 상세 페이지 (PostMainActivity) 로 이동.
        PostItem selectedPost = (PostItem) adapter.getItem(i);

        Intent intent = new Intent(PostManageActivity.this, PostMainActivity.class);
        intent.putExtra("title", selectedPost.getTitle());
        intent.putExtra("texts", selectedPost.getTexts());
        intent.putExtra("date", selectedPost.getDate());
        intent.putExtra("name", selectedPost.getUser());
        intent.putExtra("userId", selectedPost.getUserId());
        intent.putExtra("hit", selectedPost.getHitCount());
        intent.putExtra("postId", selectedPost.getId());
        intent.putExtra("fileName", selectedPost.getFileName());
        intent.putExtra("fileData", selectedPost.getFileData());
        intent.putExtra("isAdd", false);
        intent.putExtra("kinderCode", kinderCode);
        intent.putExtra("classCode", selectedClassCode);

        String postUserId = selectedPost.getUserId();
        if (userId.equals(postUserId)) {

            intent.putExtra("isSameUser", true);
        } else {

            intent.putExtra("isSameUser", false);
        }

        // 새 Activity 실행. Result 코드로 123 입력
        startActivityForResult(intent, 123);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        if (view == null) {

            return;
        }

        int id = adapterView.getId();
        switch (id) {

            case R.id.sp_types:

                selectedClassCode = (String) classCodeName.keySet().toArray()[i];
                searchPosts();

                break;

            case R.id.sp_search:

                currentSearchCode = searchTypeSpinnerItems.get(i);

                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        switch (id) {

            case R.id.fab:

                // 새 게시글 생성
                Intent intent = new Intent(PostManageActivity.this, PostMainActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("name", name);
                intent.putExtra("kinderCode", kinderCode);
                intent.putExtra("classCodeArray", classCodeArray.toString());
                startActivityForResult(intent, 123);

                break;

            case R.id.tv_search:

                tvSearch.setInputType(1);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(tvSearch, 0); // hide keyboard

                break;
            case R.id.btn_search:

                String search = tvSearch.getText().toString();
                if (search == null || search.length() == 0) { // search all

                    adapter.clear();
                    for (int i = 0; i < tempCopiedapter.getCount(); i++) {

                        PostItem item = (PostItem) tempCopiedapter.getItem(i);
                        adapter.addItem(item);
                    }

                    adapter.notifyDataSetChanged();
                } else if (currentSearchCode.equals(title)) {

                    adapter.clear();
                    for (int i = 0; i < tempCopiedapter.getCount(); i++) {

                        PostItem item = (PostItem) tempCopiedapter.getItem(i);
                        if (item.getTitle().contains(search)) {

                            adapter.addItem(item);
                        }
                    }

                    adapter.notifyDataSetChanged();
                } else if (currentSearchCode.equals(user)) {

                    adapter.clear();
                    for (int i = 0; i < tempCopiedapter.getCount(); i++) {

                        PostItem item = (PostItem) tempCopiedapter.getItem(i);
                        if (item.getUser().contains(search)) {

                            adapter.addItem(item);
                        }
                    }

                    adapter.notifyDataSetChanged();
                }

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // PostMainActivity 에서 돌아오면, Post 리스트를 재 검색하여 업데이트
        if(requestCode == 123) {

            searchPosts();
        }
    }

    public class JSONTaskPostList extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {

                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                jsonObject.accumulate("kinderCode", kinderCode);     // kindercode를 서버로 보냄
                jsonObject.accumulate("classCode", selectedClassCode);     // selectedclasscode를 서버로 보냄
                jsonObject.accumulate("userCode", userCode);     // userCode 를 서버로 보냄

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

                adapter.clear();
                tempCopiedapter.clear();
                if(status > 0) {
                    JSONArray arrayObject = new JSONArray(jsonObject.get("result").toString());
                    for (int i = 0; i < arrayObject.length(); i++) {

                        JSONObject obj = new JSONObject(arrayObject.get(i).toString());
                        int id = Integer.parseInt(obj.get("id").toString());
                        String userId = obj.get("userId").toString();
                        String name = obj.get("userName").toString();
                        String title = obj.get("title").toString();
                        String texts = obj.get("texts").toString();
                        String date = obj.get("date").toString();
                        int hitCount = Integer.parseInt(obj.get("hitCount").toString());

                        PostItem item = new PostItem();
                        item.setId(id);
                        item.setUser(name);
                        item.setDate(date);
                        item.setHitCount(hitCount);
                        item.setTitle(title);
                        item.setTexts(texts);
                        item.setUserId(userId);

                        if (obj.has("result") == true) {

                            String resultStr = obj.getString("result");
                            JSONArray files = new JSONArray(resultStr);
                            for (int j = 0; j < files.length(); j++) {

                                JSONObject file = files.getJSONObject(j);
                                String fileName = file.getString("fileName");
                                String fileData = file.getString("fileData");

                                item.setFileName(fileName);
                                item.setFileData(fileData);
                            }
                        }

                        adapter.addItem(item);
                        tempCopiedapter.addItem(item); // backup
                    }
                }

                adapter.notifyDataSetChanged();
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
                    postTypeSpinnerItems.clear();
                    classCodeName.clear();

                    JSONArray arrayObject = new JSONArray(jsonObject.get("result").toString());
                    for (int i = 0; i < arrayObject.length(); i++) {

                        JSONObject obj = new JSONObject(arrayObject.get(i).toString());

                        if (obj.getString("kinderCode").equals(kinderCode)) {

                            String tClassName = obj.getString("className");
                            String tClassCode = obj.getString("classCode");

                            if(userCode.equals("2")){
                                postTypeSpinnerItems.add(obj.getString("className"));
                                classCodeName.put(obj.getString("classCode"), obj.getString("className"));
                                classCodeArray.add(obj.getString("classCode")); // 원장은 classCode 가 없으니 전체 classCode 를 추가
                            }
                            // 자기 자신 반만 포함하기
                            else if (classCodeArray.contains(tClassCode)) {

                                postTypeSpinnerItems.add(obj.getString("className"));
                                classCodeName.put(obj.getString("classCode"), obj.getString("className"));
                            }
                        }
                    }

                    postTypeArrayAdapter = new ArrayAdapter<>(PostManageActivity.this, android.R.layout.simple_spinner_dropdown_item, postTypeSpinnerItems);
                    spTypes.setAdapter(postTypeArrayAdapter);
                    postTypeArrayAdapter.notifyDataSetChanged();
                    spTypes.setSelection(1);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 여기까지 ------------------------------
        }
    }
}
