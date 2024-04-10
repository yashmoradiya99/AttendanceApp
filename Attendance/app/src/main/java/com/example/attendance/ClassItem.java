package com.example.attendance;

public class ClassItem {
    String ClassName;

    public String getClassName() {
        return ClassName;
    }

    public void setClassName(String className) {
        ClassName = className;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    String subjectName;

    public ClassItem(String className, String subjectName) {
        this.ClassName = className;
        this.subjectName = subjectName;


    }
}
