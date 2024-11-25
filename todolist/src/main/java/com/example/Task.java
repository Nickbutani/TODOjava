package com.example;

public class Task {
    private String description;
    private String dueDate;
    private String category;
    private String priority;
    private boolean isCompleted;

    public Task(String description, String dueDate, String category, String priority) {
        this.description = description;
        this.dueDate = dueDate;
        this.category = category;
        this.priority = priority;
        this.isCompleted = false;
    }

    public String getDescription() {
        return description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void markAsCompleted() {
        this.isCompleted = true;
    }

    @Override
    public String toString() {
        return (isCompleted ? "[X] " : "[ ] ") + description + " (Due: " + dueDate + ") [Category: " + category + "] [Priority: " + priority + "]";
    }
}
