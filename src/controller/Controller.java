package controller;

import model.Course;
import model.RegistrationService;
import model.Student;
import view.*;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Controller {
    private final RegistrationService regService = new RegistrationService();
    private final MainFrame view;
    private final List<Student> allStudents = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean isRunning = false;
    private int studentCounter = 1;

    public Controller() {
        view = new MainFrame(this);
        regService.setLogger(this::updateStatusList);
        displayInfo();
    }

    private void displayInfo() {
        updateCourseListInView();
    }

    public void buttonPressed(ButtonType button) {
        switch (button) {
            case Start:
                if (!isRunning) {
                    isRunning = true;
                    view.toggleStartStopButtons(false);

                    regService.clear();
                    allStudents.clear();
                    studentCounter = 1;

                    view.updateCourseInfo(new String[0]);
                    view.updateStatusItemsList(new String[0], true);

                    setupCourses();
                    runSimulation();
                    updateStatusList("Simulation started.");
                }
                break;

            case Stop:
                isRunning = false;
                view.toggleStartStopButtons(true);
                updateStatusList("Simulation stopped.");
                break;
        }
    }

    private void setupCourses() {
        regService.clear();
        regService.addCourse(new Course(0, "System", 30));
        regService.addCourse(new Course(1, "Nature", 25));
        regService.addCourse(new Course(2, "Engineer", 20));
    }

    private void runSimulation() {
        List<Course> courses = new ArrayList<>(regService.getCourses().values());
        Random random = new Random();

        // Registration loop
        CompletableFuture.runAsync(() -> {
            while (isRunning && studentCounter <= 100) {
                try {
                    Thread.sleep(random.nextInt(150) + 100);
                    int id = studentCounter++;
                    Student student = new Student(id, "Student " + id);
                    allStudents.add(student);
                    Course course = courses.get(random.nextInt(courses.size()));
                    CompletableFuture.runAsync(() -> regService.registerStudent(student, course.getId()));
                    updateCourseListInView();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        // Dropout loop
        CompletableFuture.runAsync(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(random.nextInt(3000) + 2000);
                    for (Course course : courses) {
                        List<Student> enrolled = course.getEnrolledStudents();
                        if (!enrolled.isEmpty()) {
                            Student student = enrolled.get(random.nextInt(enrolled.size()));
                            CompletableFuture.runAsync(() -> regService.dropStudent(student, course.getId()));
                            updateCourseListInView();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void updateCourseListInView() {
        Collection<Course> courses = regService.getCourses().values();
        String[] lines = new String[courses.size() + 1];
        int i = 0;
        int totalDropouts = 0;

        for (Course course : courses) {
            int enrolled = course.getEnrolledStudents().size();
            int capacity = course.getCapacity();
            int waiting = course.getWaitingList().size();
            int dropped = course.getDropouts();
            totalDropouts += dropped;

            lines[i++] = String.format("%s (%d/%d) | Waiting: %d | Dropped: %d", course.getName(), enrolled, capacity, waiting, dropped
            );
        }

        lines[i] = "Total dropouts: " + totalDropouts;

        SwingUtilities.invokeLater(() -> view.updateCourseInfo(lines));
    }

    public void updateStatusList(String text) {
        SwingUtilities.invokeLater(() -> view.updateStatusItemsList(text));
    }
}
