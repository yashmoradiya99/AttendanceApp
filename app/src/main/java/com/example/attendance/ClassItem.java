package com.example.attendance;
public class ClassItem {
    private String className;
    private String subjectName;
    private String key;


    public ClassItem() {

    }

    public ClassItem(String className, String subjectName,String key) {
        this.key=key;
        this.className = className;
        this.subjectName = subjectName;
    }
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
}
