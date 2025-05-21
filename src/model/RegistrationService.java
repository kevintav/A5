package model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

public class RegistrationService {
    private final Map<Integer, Course> courses = new ConcurrentHashMap<>();
    private final Map<Integer, Semaphore> locks = new ConcurrentHashMap<>();
    private Consumer<String> logger = System.out::println;

    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    public void addCourse(Course course) {
        courses.put(course.getId(), course);
        locks.put(course.getId(), new Semaphore(1));
    }

    public Map<Integer, Course> getCourses() {
        return courses;
    }

    public void clear() {
        courses.clear();
        locks.clear();
    }

    public void registerStudent(Student student, int courseId) {
        Course course = courses.get(courseId);
        Semaphore lock = locks.get(courseId);

        try {
            lock.acquire();

            if (course.getEnrolledStudents().contains(student)) {
                return;
            }

            if (course.getEnrolledStudents().size() < course.getCapacity()) {
                course.getEnrolledStudents().add(student);
                student.getEnrolledCourses().add(course);
                logger.accept(student.getName() + " enrolled in " + course.getName());
            } else {
                course.getWaitingList().add(student);
                logger.accept(student.getName() + " added to waitlist for " + course.getName());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.release();
        }
    }

    public void dropStudent(Student student, int courseId) {
        Course course = courses.get(courseId);
        Semaphore lock = locks.get(courseId);

        try {
            lock.acquire();

            if (course.getEnrolledStudents().remove(student)) {
                student.getEnrolledCourses().remove(course);
                logger.accept(student.getName() + " dropped from " + course.getName());

                Student next = course.getWaitingList().poll();
                if (next != null) {
                    course.getEnrolledStudents().add(next);
                    next.getEnrolledCourses().add(course);
                    logger.accept(next.getName() + " promoted from waitlist to " + course.getName());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.release();
        }
    }
}
