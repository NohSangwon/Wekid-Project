package com.example.wekid;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherListAdapter extends BaseAdapter implements View.OnClickListener {

    private ArrayList<TeacherDTO> teacherList = new ArrayList<>();

    private Context context;

    private TextView tvId;

    private TextView tvName;

    private Spinner spClass;

    private Button btnChange;


    private ArrayList<String> postTypeSpinnerItems = new ArrayList<>(); // 클래스 이름을 저장하는 ArrayList

    private HashMap<String, String> classCodeName = new HashMap<String, String>(); // 클래스 이름과 코드 번호를 매칭하는 HashMap

    private List<String> classCodeArray = new ArrayList<>(); // 클래스 코드가 저장되는 List

    private int selectedIndex;

    private View selectedView;



    public void setContext(Context context) {

        this.context = context;
    }

    public void setSelectedIndex(int index, View view) {

        selectedIndex = index;
        selectedView = view;
    }

    public void addClass(String classCode, String className) {

        postTypeSpinnerItems.add(className);
        classCodeName.put(classCode, className);

        classCodeArray.add(classCode);
    }

    public void clear() {

        teacherList.clear();
    }

    public void addItem(TeacherDTO teacher) {

        teacherList.add(teacher);
    }

    @Override
    public int getCount() {
        return teacherList.size();
    }

    @Override
    public Object getItem(int i) {
        return teacherList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if(view == null) {
            // view가 null일 경우 커스텀 레이아웃을 얻어 옴
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.teacher_manage_list_item, viewGroup, false);
        }

        tvId = view.findViewById(R.id.tvId);
        tvName = view.findViewById(R.id.tvName);
        spClass = view.findViewById(R.id.spClass);
        btnChange = view.findViewById(R.id.btnChange);

        if (teacherList.size() == 0) {

            return view;
        }

        TeacherDTO teacher = teacherList.get(i);

        tvId.setText(teacher.getId());
        tvName.setText(teacher.getName());

        ArrayAdapter postTypeArrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, postTypeSpinnerItems);
        spClass.setAdapter(postTypeArrayAdapter);

        int index = classCodeArray.indexOf(teacher.getClassCode());
        spClass.setSelection(index);

        postTypeArrayAdapter.notifyDataSetChanged();

        btnChange.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {

        selectedView = (View) view.getParent();
        int id = view.getId();

        switch (id) {

            case R.id.btnChange:

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // 제목셋팅
                alertDialogBuilder.setTitle("반 변경");

                // AlertDialog 셋팅
                alertDialogBuilder
                        .setMessage("담당 반을 변경하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("변경",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog, int id) {

                                        spClass = selectedView.findViewById(R.id.spClass);
                                        tvId = selectedView.findViewById(R.id.tvId);

                                        String tId = tvId.getText().toString();

                                        String newClassName = spClass.getSelectedItem().toString();

                                        for (Map.Entry<String, String> item : classCodeName.entrySet()) {

                                            if (item.getValue().equals(newClassName)) {

                                                ((TeacherManageActivity) context).updateTeacher(tvId.getText().toString(), item.getKey());
                                                break;
                                            }
                                        }
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

                // 다이얼로그 보여주기
                alertDialog.show();

                break;
        }
    }
}
