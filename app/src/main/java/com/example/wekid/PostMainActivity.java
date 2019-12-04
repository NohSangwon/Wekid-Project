package com.example.wekid;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostMainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Spinner spTypes;

    private EditText etPostTitle;

    private LinearLayout llInfo;

    private TextView tvPostUser;

    private TextView tvPostDate;

    private TextView tvPostHitCount;

    private EditText etPostTexts;

    private Button btnAddFile;

    private Button btnAddPost;

    private Button btnUpdatePost;

    private Button btnDeletePost;

    private TextView tvFileName;

    private Button btnDeleteFile;

    private boolean isPostAdd = true;

    private int postId;

    private String userId;

    private String name;

    private String kinderCode;

    private String classCode;

    private List<String> classCodeArray;

    private String filePath;

    private String fileName;

    private String fileData;

    private ArrayList<String> spinnerItems = new ArrayList<>();

    private HashMap<String, String> classCodeName = new HashMap<String, String>();

    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_main);

        classCodeArray = new ArrayList<>();

        spTypes = findViewById(R.id.sp_types);
        etPostTitle = findViewById(R.id.tv_post_title);
        etPostTexts = findViewById(R.id.tv_post_texts);
        llInfo = findViewById(R.id.ll_info);
        tvPostUser = findViewById(R.id.tv_post_user);
        tvPostDate = findViewById(R.id.tv_post_date);
        tvPostHitCount = findViewById(R.id.tv_post_hit_count);
        btnAddFile = findViewById(R.id.btn_add_file);
        btnAddPost = findViewById(R.id.btn_add_post);
        btnUpdatePost = findViewById(R.id.btn_update_post);
        btnDeletePost = findViewById(R.id.btn_delete_post);
        btnDeleteFile = findViewById(R.id.btn_delete_file);
        tvFileName = findViewById(R.id.tv_file_name);
        tvFileName.setVisibility(View.GONE);
        btnDeleteFile.setVisibility(View.GONE);

        tvFileName.setOnClickListener(this);
        btnDeleteFile.setOnClickListener(this);

        Intent intent = getIntent();
        isPostAdd = intent.getBooleanExtra("isAdd", true);
        boolean isSameUser = intent.getBooleanExtra("isSameUser", true);
        postId = intent.getIntExtra("postId", 0);
        userId = intent.getStringExtra("userId");
        name = intent.getStringExtra("name");
        kinderCode = intent.getStringExtra("kinderCode");
        classCode = intent.getStringExtra("classCode");

        fileName = intent.getStringExtra("fileName");
        fileData = intent.getStringExtra("fileData");

        spTypes.setOnItemSelectedListener(this);

        filePath = null;

        if (isPostAdd == true) {

            llInfo.setVisibility(View.GONE);
            btnUpdatePost.setVisibility(View.GONE);
            btnDeletePost.setVisibility(View.GONE);
            tvFileName.setVisibility(View.GONE);
            btnDeleteFile.setVisibility(View.GONE);

            String classCodeArrayStr = intent.getStringExtra("classCodeArray");
            classCodeArray = Arrays.asList(classCodeArrayStr.substring(1, classCodeArrayStr.length() - 1).split(", "));

            classCode = classCodeArray.get(0);

            btnAddPost.setOnClickListener(this);
        } else {

            etPostTitle.setText(intent.getStringExtra("title"));
            etPostTexts.setText(intent.getStringExtra("texts"));

            tvPostUser.setText(intent.getStringExtra("name"));
            tvPostDate.setText(intent.getStringExtra("date"));

            int hitCount = intent.getIntExtra("hit", 0);

            btnAddPost.setVisibility(View.GONE);

            if (isSameUser == false) {

                btnUpdatePost.setVisibility(View.GONE);
                btnDeletePost.setVisibility(View.GONE);
                btnAddFile.setVisibility(View.GONE);
                spTypes.setEnabled(false);
                etPostTexts.setEnabled(false);
                etPostTitle.setEnabled(false);
                btnDeleteFile.setVisibility(View.GONE);

                tvPostHitCount.setText(String.valueOf(hitCount + 1));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

                    new JSONTaskCountPostHitCount().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Helper.ServerAddress + "countPostHitCount"); //AsyncTask 시작시킴
                } else {

                    new JSONTaskCountPostHitCount().execute(Helper.ServerAddress + "countPostHitCount"); //AsyncTask 시작시킴
                }
            } else {

                btnUpdatePost.setOnClickListener(this);
                btnDeletePost.setOnClickListener(this);
                tvPostHitCount.setText(String.valueOf(hitCount));
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

                new JSONTaskGetAttachedFile().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Helper.ServerAddress + "getPostAttachFile"); //AsyncTask 시작시킴
            } else {

                new JSONTaskGetAttachedFile().execute(Helper.ServerAddress + "getPostAttachFile"); //AsyncTask 시작시킴
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            new JSONTaskClassList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Helper.ServerAddress + "getClassList"); //AsyncTask 시작시킴
        } else {

            new JSONTaskClassList().execute(Helper.ServerAddress + "getClassList"); //AsyncTask 시작시킴
        }

        btnAddFile.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        switch (id) {

            case R.id.tv_file_name:

                byte[] byteArray = fileData.getBytes();
                try {
                    System.out.println("클릭");
                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    String documentPath = dir.getPath();
                    File saveFile = new File(documentPath); // 저장 경로
                    boolean bResult = false;
                    // 폴더 생성
                    if(!saveFile.exists()){ // 폴더 없을 경우
                        bResult = saveFile.mkdirs(); // 폴더 생성
                    }

                    File file = new File(documentPath, fileName);
                    if (!file.exists()) {
                        bResult = file.createNewFile();
                    }

                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(documentPath + fileName, true));
                    bos.write(byteArray);
                    bos.flush();
                    bos.close();

                    Toast.makeText(this, "파일이 저장되었습니다.", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {

                    e.printStackTrace();
                }

                break;

            case R.id.btn_delete_file:

                filePath = null;
                fileName = null;
                fileData = null;

                tvFileName.setText("");
                tvFileName.setVisibility(View.GONE);
                btnDeleteFile.setVisibility(View.GONE);

                break;

            case R.id.btn_add_file:

                requestReadExternalStoragePermission();

                break;
            case R.id.btn_add_post:

                String title = etPostTitle.getText().toString();
                String postTexts = etPostTexts.getText().toString();

                if (title.trim().length() == 0) {

                    return;
                }

                if (postTexts.trim().length() == 0) {

                    return;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

                    new JSONTaskAdd().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Helper.ServerAddress + "newPost"); //AsyncTask 시작시킴
                } else {

                    new JSONTaskAdd().execute(Helper.ServerAddress + "newPost"); //AsyncTask 시작시킴
                }

                setResult(123);
                finish();

                break;
            case R.id.btn_update_post:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        this);

                // 제목셋팅
                alertDialogBuilder.setTitle("게시글 수정");

                alertDialogBuilder
                        .setMessage("수정하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("수정",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

                                            new JSONTaskUpdate().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Helper.ServerAddress + "updatePost"); //AsyncTask 시작시킴
                                        } else {

                                            new JSONTaskUpdate().execute(Helper.ServerAddress + "updatePost"); //AsyncTask 시작시킴
                                        }
                                        setResult(123);
                                        finish();
                                    }
                                })
                        .setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        // 다이얼로그를 취소한다
                                        dialog.cancel();
                                    }
                                });

                // 다이얼로그 생성
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                break;
            case R.id.btn_delete_post:

                AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                        this);

                // 제목셋팅
                alertDialogBuilder1.setTitle("게시글 삭제");

                alertDialogBuilder1
                        .setMessage("삭제하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("삭제",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

                                            new JSONTaskDelete().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Helper.ServerAddress + "deletePost"); //AsyncTask 시작시킴
                                        } else {

                                            new JSONTaskDelete().execute(Helper.ServerAddress + "deletePost"); //AsyncTask 시작시킴
                                        }

                                        setResult(123);
                                        finish();
                                    }
                                })
                        .setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {
                                        // 다이얼로그를 취소한다
                                        dialog.cancel();
                                    }
                                });

                // 다이얼로그 생성
                AlertDialog alertDialog1 = alertDialogBuilder1.create();
                alertDialog1.show();

                break;
        }
    }

    private void requestReadExternalStoragePermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        123);
                // MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {

            showFileChooser();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 123 : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    showFileChooser();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void showFileChooser() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");

        startActivityForResult(intent, 123);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123 && resultCode == RESULT_OK) {

            try {

                String path = Helper.getPathFromURI(this, data.getData());

                File file = new File(path);

                // 파일 크기 확인
                // 5 MB 이상이면 첨부 불가
                long fileSizeInBytes = file.length();
                long fileSizeInKB = fileSizeInBytes / 1024;
                long fileSizeInMB = fileSizeInKB / 1024;
                if (fileSizeInMB >= 5) {

                    Toast.makeText(this, "파일 크기가 10 MB 를 넘어서 첨부가 안됩니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                filePath = file.getAbsolutePath() ; //The uri with the location of the file

                tvFileName.setText(file.getName());
                tvFileName.setVisibility(View.VISIBLE);
                btnDeleteFile.setVisibility(View.VISIBLE);
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
    }

    private byte[] toByteArray(String filePath) {

        byte[] bytes = null;
        try {
            File file = new File(filePath);
            bytes = new byte[(int) file.length()];

            FileInputStream fis = new FileInputStream(file);
            fis.read(bytes); //read file into bytes[]
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        classCode = (String) classCodeName.keySet().toArray()[i];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public class JSONTaskAdd extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                jsonObject.accumulate("userId", userId);
                jsonObject.accumulate("name", name);
                String title = etPostTitle.getText().toString();
                jsonObject.accumulate("title", etPostTitle.getText().toString());
                jsonObject.accumulate("texts", etPostTexts.getText().toString());
                jsonObject.accumulate("date", format.format(currentTime));
                jsonObject.accumulate("kinderCode", kinderCode);
                jsonObject.accumulate("classCode", classCode);

                if (filePath != null) {

                    File file = new File(filePath);
                    byte[] bytesArray = toByteArray(filePath);
                    String encodedString = new String(bytesArray, StandardCharsets.ISO_8859_1);

                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("fileName", file.getName());
                    jsonObject1.put("fileData", encodedString);

                    jsonArray.put(jsonObject1);

                    jsonObject.put("files", jsonArray);
                }

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

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 여기까지 ------------------------------
        }
    }

    public class JSONTaskUpdate extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                jsonObject.accumulate("id", postId);     // postId를 서버로 보냄
                jsonObject.accumulate("title", etPostTitle.getText().toString());     // title을 서버로 보냄
                jsonObject.accumulate("texts", etPostTexts.getText().toString());     // texts를 서버로 보냄
                jsonObject.accumulate("date", format.format(currentTime));     // texts를 서버로 보냄

                if (filePath != null && filePath.length() > 0) {

                    File file = new File(filePath);
                    byte[] bytesArray = toByteArray(filePath);
                    String encodedString = new String(bytesArray, StandardCharsets.ISO_8859_1);

                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("fileName", file.getName());
                    jsonObject1.put("fileData", encodedString);

                    jsonArray.put(jsonObject1);

                    jsonObject.put("files", jsonArray);
                }

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

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 여기까지 ------------------------------
        }
    }

    public class JSONTaskDelete extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                Date currentTime = Calendar.getInstance().getTime();
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                jsonObject.accumulate("id", postId);     // postId를 서버로 보냄

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

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 여기까지 ------------------------------
        }
    }

    public class JSONTaskGetAttachedFile extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                Date currentTime = Calendar.getInstance().getTime();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                jsonObject.accumulate("postId", postId);     // postId를 서버로 보냄

                if (filePath != null) {

                    File file = new File(filePath);
                    byte[] bytesArray = toByteArray(filePath);
                    String encodedString = new String(bytesArray, StandardCharsets.ISO_8859_1);

                    JSONArray jsonArray = new JSONArray();
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("fileName", file.getName());
                    jsonObject1.put("fileData", encodedString);

                    jsonArray.put(jsonObject1);

                    jsonObject.put("files", jsonArray);
                }

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

                    if (jsonObject.has("result") == true) {

                        String resultStr = jsonObject.getString("result");
                        JSONArray files = new JSONArray(resultStr);
                        for (int j = 0; j < files.length(); j++) {

                            JSONObject file = files.getJSONObject(j);
                            fileName = file.getString("fileName");
                            fileData = file.getString("fileData");

                            if (fileName != null && fileName.length() > 0) {

                                tvFileName.setText(fileName);
                                tvFileName.setVisibility(View.VISIBLE);
                            }
                        }
                    }
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
                    spinnerItems.clear();
                    classCodeName.clear();

                    JSONArray arrayObject = new JSONArray(jsonObject.get("result").toString());
                    for (int i = 0; i < arrayObject.length(); i++) {

                        JSONObject obj = new JSONObject(arrayObject.get(i).toString());

                        if (obj.getString("kinderCode").equals(kinderCode)) {

                            String tClassName = obj.getString("className");
                            String tClassCode = obj.getString("classCode");

                            // 전체 공지와 자기 자신 반만 포함하기
                            if (classCodeArray.contains(tClassCode)) {

                                spinnerItems.add(obj.getString("className"));
                                classCodeName.put(obj.getString("classCode"), obj.getString("className"));
                            }
                        }
                    }

                    arrayAdapter = new ArrayAdapter<>(PostMainActivity.this, android.R.layout.simple_spinner_dropdown_item, spinnerItems);
                    spTypes.setAdapter(arrayAdapter);

                    int index = 0;
                    for(Map.Entry<String, String> entry : classCodeName.entrySet()) {
                        String key = entry.getKey();
                        if (key.equals(classCode)) {

                            break;
                        } else {

                            index++;
                        }
                    }

                    spTypes.setSelection(index);
                    arrayAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 여기까지 ------------------------------
        }
    }

    public class JSONTaskCountPostHitCount extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                jsonObject.accumulate("postId", postId);     // postId를 서버로 보냄

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
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 여기까지 ------------------------------
        }
    }
}
