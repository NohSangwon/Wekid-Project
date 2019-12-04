package com.example.wekid;

import android.os.Parcel;
import android.os.Parcelable;

// 객체 자체를 전달하기 위해 implements Parcelable
public class TeacherDTO implements Parcelable {
    private String id;
    private String name;
    private String kinderName;
    private String kinderCode;
    private String className;
    private String phoneNum;
    private String classCode;

    public TeacherDTO() {}

    protected TeacherDTO(Parcel in) {
        id = in.readString();
        name = in.readString();
        kinderName = in.readString();
        kinderCode = in.readString();
        className = in.readString();
        classCode = in.readString();
        phoneNum = in.readString();
    }

    public static final Creator<TeacherDTO> CREATOR = new Creator<TeacherDTO>() {
        @Override
        public TeacherDTO createFromParcel(Parcel in) {
            return new TeacherDTO(in);
        }

        @Override
        public TeacherDTO[] newArray(int size) {
            return new TeacherDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // 생성자에서 읽어오는 순서와 기록하는 순서가 같아야 함.
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(kinderName);
        dest.writeString(className);
        dest.writeString(kinderCode);
        dest.writeString(classCode);
        dest.writeString(phoneNum);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKinderName() {
        return kinderName;
    }

    public void setKinderName(String kinderName) {
        this.kinderName = kinderName;
    }

    public String getKinderCode() { return kinderCode; }

    public void setKinderCode(String KinderCode) { this.kinderCode = KinderCode; }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public String getClassCode() { return classCode; }

    public void setClassCode(String classCode) { this.classCode = classCode; }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }
}
