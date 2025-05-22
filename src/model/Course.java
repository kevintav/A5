package model;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Course {
    private final int id;
    private final String name;
    private final int capacity;
    private final List<Student> enrolledStudents;
    private final Queue<Student> waitingList;
    private int dropouts = 0;

    public Course(int id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.enrolledStudents = new CopyOnWriteArrayList<>();
        this.waitingList = new ConcurrentLinkedQueue<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<Student> getEnrolledStudents() {
        return enrolledStudents;
    }

    public Queue<Student> getWaitingList() {
        return waitingList;
    }

    public void incrementDropouts() {
        dropouts++;
    }

    public int getDropouts() {
        return dropouts;
    }
}
