package ru.myapp.semester_work.models;

public class WordData {
    private String word;
    private TeamType teamType;
    private boolean guessed;
    private int positionX;
    private int positionY;

    public WordData(String word, TeamType teamType, boolean guessed, int positionX, int positionY) {
        this.word = word;
        this.teamType = teamType;
        this.guessed = guessed;
        this.positionX = positionX;
        this.positionY = positionY;
    }

    private WordData(Builder builder) {
        this.word = builder.word;
        this.teamType = builder.teamType;
        this.guessed = builder.guessed;
        this.positionX = builder.positionX;
        this.positionY = builder.positionY;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getWord() {
        return word;
    }

    public TeamType getTeamType() {
        return teamType;
    }

    public boolean isGuessed() {
        return guessed;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setGuessed(boolean guessed) {
        this.guessed = guessed;
    }

    public static class Builder {
        private String word;
        private TeamType teamType;
        private boolean guessed;
        private int positionX;
        private int positionY;

        public Builder() {
            this.guessed = false;
            this.positionX = 0;
            this.positionY = 0;
        }

        public Builder word(String word) {
            this.word = word;
            return this;
        }

        public Builder teamType(TeamType teamType) {
            this.teamType = teamType;
            return this;
        }

        public Builder guessed(boolean guessed) {
            this.guessed = guessed;
            return this;
        }

        public Builder positionX(int positionX) {
            this.positionX = positionX;
            return this;
        }

        public Builder positionY(int positionY) {
            this.positionY = positionY;
            return this;
        }

        public Builder position(int x, int y) {
            this.positionX = x;
            this.positionY = y;
            return this;
        }

        public WordData build() {
            if (word == null || word.trim().isEmpty()) {
                throw new IllegalStateException("Слово не может быть пустым");
            }
            if (teamType == null) {
                throw new IllegalStateException("Слово не может быть без типа команды");
            }

            return new WordData(this);
        }
    }
}