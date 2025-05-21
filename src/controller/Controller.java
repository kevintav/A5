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
    private volatile boolean simulationRunning = false;
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
                if (!simulationRunning) {
                    simulationRunning = true;
                    view.toggleStartStopButtons(false);
                    setupCourses();
                    runSimulation();
                    updateStatusList("Simulation started.");
                }
                break;

            case Stop:
                simulationRunning = false;
                view.toggleStartStopButtons(true);
                updateStatusList("Simulation stopped.");
                break;
        }
    }

    private void setupCourses() {
        regService.clear();
        regService.addCourse(new Course(0, "System", 10));
        regService.addCourse(new Course(1, "Nature", 10));
        regService.addCourse(new Course(2, "Engineer", 10));
    }

    private void runSimulation() {
        List<Course> courses = new ArrayList<>(regService.getCourses().values());
        Random random = new Random();

        // Registration loop
        CompletableFuture.runAsync(() -> {
            while (simulationRunning && studentCounter <= 100) {
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
            while (simulationRunning) {
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
        String[] lines = regService.getCourses().values().stream().map(course ->
                String.format("%s (%d/%d)", course.getName(), course.getEnrolledStudents().size(), course.getCapacity())
        ).toArray(String[]::new);

        SwingUtilities.invokeLater(() -> view.updateCourseInfo(lines));
    }

    public void updateView(String stringInfo) {
        SwingUtilities.invokeLater(() -> view.updateEventLog(stringInfo));
    }

    public void updateStatusList(String[] itemsToAdd, boolean clearList) {
        SwingUtilities.invokeLater(() -> view.updateStatusItemsList(itemsToAdd, clearList));
    }

    public void updateStatusList(String text) {
        SwingUtilities.invokeLater(() -> view.updateStatusItemsList(text));
    }
}
