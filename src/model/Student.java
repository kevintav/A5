package model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Student {
    private final int id;
    private final String name;
    private final List<Course> enrolledCourses;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
        this.enrolledCourses = new CopyOnWriteArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Course> getEnrolledCourses() {
        return enrolledCourses;
    }
}
