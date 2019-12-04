package com.example.wekid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PostListAdapter extends BaseAdapter {

    private ArrayList<PostItem> postList = new ArrayList<>();

    private Context context;

    private TextView tvPostTitle;

    private TextView tvPostUser;

    private TextView tvPostDate;

    private TextView tvPostHitCount;

    public void addItem(PostItem item) {

        postList.add(item);
    }

    public void clear() {

        postList.clear();
    }

    @Override
    public int getCount() {
        return postList.size();
    }

    @Override
    public Object getItem(int i) {
        return postList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        context = viewGroup.getContext();

        if(view == null) {
            // view가 null일 경우 커스텀 레이아웃을 얻어 옴
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = inflater.inflate(R.layout.post_list_item, viewGroup, false);
        }

        tvPostTitle = view.findViewById(R.id.tv_post_title);
        tvPostUser = view.findViewById(R.id.tv_post_user);
        tvPostDate = view.findViewById(R.id.tv_post_date);
        tvPostHitCount = view.findViewById(R.id.tv_post_hit_count);

        PostItem item = postList.get(i);

        tvPostTitle.setText(item.getTitle());
        tvPostUser.setText(item.getUser());
        tvPostDate.setText(item.getDate());
        tvPostHitCount.setText(String.valueOf(item.getHitCount()));

        return view;
    }
}
