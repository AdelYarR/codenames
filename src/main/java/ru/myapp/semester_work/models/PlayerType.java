package ru.myapp.semester_work.models;

public enum PlayerType {
    COMMANDER("Командир"),
    MEMBER("Участник"),
    NONE("");

    private final String displayName;

    PlayerType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}