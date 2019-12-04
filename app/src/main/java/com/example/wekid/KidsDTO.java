package com.example.wekid;

import android.os.Parcel;
import android.os.Parcelable;

// 아이 정보 담는 클래스42
// 객체 자체를 전달하기 위해 implements Parcelable
public class KidsDTO implements Parcelable {
    private String identifier;
    private String name;
    private String birth;
    private String address;
    private String kinderName;
    private String className;
    private String parentsId;
    private String kinderCode;
    private String classCode;

    public KidsDTO() {}

    protected KidsDTO(Parcel in) {
        identifier = in.readString();
        name = in.readString();
        birth = in.readString();
        address = in.readString();
        kinderName = in.readString();
        kinderCode = in.readString();
        className = in.readString();
        parentsId = in.readString();
        classCode = in.readString();
    }

    public static final Creator<KidsDTO> CREATOR = new Creator<KidsDTO>() {
        @Override
        public KidsDTO createFromParcel(Parcel in) {
            return new KidsDTO(in);
        }

        @Override
        public KidsDTO[] newArray(int size) {
            return new KidsDTO[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier);
        dest.writeString(name);
        dest.writeString(birth);
        dest.writeString(address);
        dest.writeString(kinderName);
        dest.writeString(kinderCode);
        dest.writeString(className);
        dest.writeString(classCode);
        dest.writeString(parentsId);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getClassCode() { return classCode; }

    public void setClassCode(String classCode) { this.classCode = classCode;}

    public String getParentsId() {
        return parentsId;
    }

    public void setParentsId(String parentsId) {
        this.parentsId = parentsId;
    }
}
