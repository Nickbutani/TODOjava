package com.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ToDoListGUI extends Application {
    private ListView<Task> taskListView;
    private final ArrayList<Task> taskList = new ArrayList<>();
    private final String FILE_NAME = "tasks.txt";
    private TextField searchField;
    private boolean isDueDateAscending = true;
    private boolean isPriorityAscending = true;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Modern To-Do List");

        // Load tasks from file
        loadTasks();

        // Main Layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));
        mainLayout.setStyle("-fx-background-color: #f4f4f4;");

        // Search Bar Section
        HBox searchSection = new HBox(10);
        searchSection.setPadding(new Insets(10));
        searchSection.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        searchField = new TextField();
        searchField.setPromptText("Search tasks...");
        searchField.setStyle("-fx-border-radius: 5;");
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterTasks(newValue));

        Button clearSearchButton = new Button("Clear Search");
        clearSearchButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-border-radius: 5;");
        clearSearchButton.setOnAction(e -> {
            searchField.clear();
            filterTasks("");
        });

        searchSection.getChildren().addAll(searchField, clearSearchButton);

        // Sort and Priority Section
        HBox sortSection = new HBox(10);
        sortSection.setPadding(new Insets(10));
        sortSection.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 5;");
        
        Button sortByDueDateButton = new Button("Sort by Due Date");
        sortByDueDateButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-border-radius: 5;");
        sortByDueDateButton.setOnAction(e -> {
            isDueDateAscending = !isDueDateAscending;
            sortTasksByDueDate();
        });

        Button sortByPriorityButton = new Button("Sort by Priority");
        sortByPriorityButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 5;");
        sortByPriorityButton.setOnAction(e -> {
            isPriorityAscending = !isPriorityAscending;
            sortTasksByPriority();
        });

        sortSection.getChildren().addAll(sortByDueDateButton, sortByPriorityButton);

        // Task Input Section
        HBox inputSection = new HBox(10);
        inputSection.setPadding(new Insets(10));
        inputSection.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        TextField taskField = new TextField();
        taskField.setPromptText("Enter a new task...");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select Due Date");

        // Category ComboBox
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("Work", "Personal", "Urgent", "Miscellaneous");
        categoryComboBox.setPromptText("Select Category");

        // Priority ComboBox
        ComboBox<String> priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll("High", "Medium", "Low");
        priorityComboBox.setPromptText("Select Priority");

        Button addButton = new Button("Add Task");
        addButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-border-radius: 5;");
        addButton.setOnAction(e -> {
            String description = taskField.getText().trim();
            String category = categoryComboBox.getValue();
            String priority = priorityComboBox.getValue();
            if (!description.isEmpty() && datePicker.getValue() != null && category != null && priority != null) {
                String dueDate = datePicker.getValue().toString();
                Task task = new Task(description, dueDate, category, priority);
                taskList.add(task);
                taskListView.getItems().add(task);
                taskField.clear();
                datePicker.setValue(null);
                categoryComboBox.setValue(null);
                priorityComboBox.setValue(null);
            } else {
                showAlert("Error", "Please enter a task, select a due date, and choose a category and priority.", Alert.AlertType.ERROR);
            }
        });

        inputSection.getChildren().addAll(taskField, datePicker, categoryComboBox, priorityComboBox, addButton);
        HBox.setHgrow(taskField, Priority.ALWAYS);

        // Task List Section
        taskListView = new ListView<>();
        taskListView.getItems().addAll(taskList);

        // Buttons Section
        HBox buttonSection = new HBox(10);
        buttonSection.setPadding(new Insets(10));
        buttonSection.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        Button deleteButton = new Button("Delete Task");
        deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-border-radius: 5;");
        deleteButton.setOnAction(e -> {
            Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                taskListView.getItems().remove(selectedTask);
                taskList.remove(selectedTask);
            } else {
                showAlert("Error", "Please select a task to delete.", Alert.AlertType.WARNING);
            }
        });

        Button markCompletedButton = new Button("Mark as Completed");
        markCompletedButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-border-radius: 5;");
        markCompletedButton.setOnAction(e -> {
            Task selectedTask = taskListView.getSelectionModel().getSelectedItem();
            if (selectedTask != null && !selectedTask.isCompleted()) {
                selectedTask.markAsCompleted();
                taskListView.refresh();
            } else {
                showAlert("Error", "Please select an incomplete task to mark as completed.", Alert.AlertType.WARNING);
            }
        });

        buttonSection.getChildren().addAll(markCompletedButton, deleteButton);

        // Add sections to main layout
        mainLayout.getChildren().addAll(searchSection, sortSection, inputSection, taskListView, buttonSection);

        // Set the scene
        Scene scene = new Scene(mainLayout, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Save tasks on exit
        primaryStage.setOnCloseRequest(event -> saveTasks());
    }

    private void loadTasks() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    boolean isCompleted = Boolean.parseBoolean(parts[0]);
                    String description = parts[1];
                    String dueDate = parts[2];
                    String category = parts[3];
                    String priority = parts[4];
                    Task task = new Task(description, dueDate, category, priority);
                    if (isCompleted) {
                        task.markAsCompleted();
                    }
                    taskList.add(task);
                }
            }
        } catch (IOException e) {
            System.out.println("No existing tasks found.");
        }
    }

    private void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Task task : taskList) {
                writer.write(task.isCompleted() + "|" + task.getDescription() + "|" + task.getDueDate() + "|" + task.getCategory() + "|" + task.getPriority());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterTasks(String query) {
        List<Task> filteredTasks = taskList.stream()
                .filter(task -> task.getDescription().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        taskListView.getItems().setAll(filteredTasks);
    }

    private void sortTasksByDueDate() {
        if (isDueDateAscending) {
            taskListView.getItems().sort(Comparator.comparing(Task::getDueDate));
        } else {
            taskListView.getItems().sort(Comparator.comparing(Task::getDueDate).reversed());
        }
    }

    private void sortTasksByPriority() {
        if (isPriorityAscending) {
            taskListView.getItems().sort(Comparator.comparing(Task::getPriority));
        } else {
            taskListView.getItems().sort(Comparator.comparing(Task::getPriority).reversed());
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
