public class Student {
    private String name;
    private int roll;
    private String status;


    public Student(String name, int rollNumber,String status) {
        this.name = name;
        this.roll = roll;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public int getRollNumber() {
        return roll;
    }

    public String getStatus() {
        return status;
    }
}
