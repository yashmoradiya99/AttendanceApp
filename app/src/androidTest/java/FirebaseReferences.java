import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseReferences {

    // Reference to the root node
    public static DatabaseReference rootReference = FirebaseDatabase.getInstance().getReference();

    // Reference to the "Teacher" node
    public static DatabaseReference teacherReference = rootReference.child("Teacher");

    // Reference to the "Classes" node under "Teacher"
    public static DatabaseReference classesReference = teacherReference.child("Classes");

    // Reference to the "Students" node under a specific class
    public static DatabaseReference studentsReference(String classId) {
        return classesReference.child(classId).child("Students");
    }
}
