package ru.myapp.semester_work.models;

import ru.myapp.semester_work.utils.JsonLoader;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.*;

public class SessionState {

    private static final int FIRST_STARTING_COUNT = 9;
    private static final int SECOND_STARTING_COUNT = 8;

    private SocketChannel redCommander;
    private SocketChannel redMember;
    private SocketChannel blueCommander;
    private SocketChannel blueMember;

    private int redCount;
    private int blueCount;

    private List<String> redMessages;
    private List<String> blueMessages;

    private List<WordData> words;

    private SocketChannel currentTurn;
    private boolean isStarted;

    public SessionState() {
        redMessages = new ArrayList<>();
        blueMessages = new ArrayList<>();
        isStarted = false;
    }

    public void start() {
        if (redCommander == null || redMember == null || blueCommander == null || blueMember == null) {
            throw new IllegalStateException("Не все игроки присоединились к командам");
        }

        currentTurn = Math.random() < 0.5 ? redCommander : blueCommander;
        if (currentTurn == redCommander) {
            redCount = FIRST_STARTING_COUNT;
            blueCount = SECOND_STARTING_COUNT;
        } else {
            blueCount = FIRST_STARTING_COUNT;
            redCount = SECOND_STARTING_COUNT;
        }

        words = generateWords();

        isStarted = true;
    }

    private List<WordData> generateWords() {
        try {
            List<String> wordStrings = JsonLoader.loadStringListFromJson();
            if (wordStrings.size() < 25) {
                throw new IllegalStateException("В файле недостаточно слов. Требуется минимум 25, а доступно: " + wordStrings.size());
            }

            Collections.shuffle(wordStrings);

            List<String> selectedWords = wordStrings.subList(0, 25);

            List<TeamType> colorDistribution = new ArrayList<>();
            for (int i = 0; i < FIRST_STARTING_COUNT; i++) {
                colorDistribution.add(currentTurn == redCommander ? TeamType.RED : TeamType.BLUE);
            }

            for (int i = 0; i < SECOND_STARTING_COUNT; i++) {
                colorDistribution.add(currentTurn == redCommander ? TeamType.BLUE : TeamType.RED);
            }

            for (int i = 0; i < 25 - FIRST_STARTING_COUNT - SECOND_STARTING_COUNT; i++) {
                colorDistribution.add(TeamType.NONE);
            }

            Collections.shuffle(colorDistribution);

            List<WordData> generatedWords = new ArrayList<>();

            for (int i = 0; i < 25; i++) {
                int positionY = i / 5;
                int positionX = i % 5;

                WordData wordData = new WordData(
                        selectedWords.get(i),
                        colorDistribution.get(i),
                        false,
                        positionX,
                        positionY
                );

                generatedWords.add(wordData);
            }

            return generatedWords;
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось считать JSON файл со словами", e);
        }
    }

    public TeamType getTeamTypeByClientChannel(SocketChannel clientChannel) {
        if (clientChannel == null) return TeamType.NONE;

        if (clientChannel.equals(redCommander) || clientChannel.equals(redMember)) {
            return TeamType.RED;
        } else if (clientChannel.equals(blueCommander) || clientChannel.equals(blueMember)) {
            return TeamType.BLUE;
        } else {
            return TeamType.NONE;
        }
    }

    public PlayerType getPlayerTypeByClientChannel(SocketChannel clientChannel) {
        if (clientChannel == null) return PlayerType.NONE;

        if (clientChannel.equals(redCommander) || clientChannel.equals(blueCommander)) {
            return PlayerType.COMMANDER;
        } else if (clientChannel.equals(redMember) || clientChannel.equals(blueMember)) {
            return PlayerType.MEMBER;
        } else {
            return PlayerType.NONE;
        }
    }

    public void clearClientType(TeamType teamType, PlayerType playerType) {
        switch (teamType) {
            case RED -> {
                switch (playerType) {
                    case COMMANDER -> redCommander = null;
                    case MEMBER -> redMember = null;
                }
            }
            case BLUE -> {
                switch (playerType) {
                    case COMMANDER -> blueCommander = null;
                    case MEMBER -> blueMember = null;
                }
            }
        }
    }

    public void setClientType(SocketChannel clientChannel, TeamType teamType, PlayerType playerType) {
        switch (teamType) {
            case RED -> {
                switch (playerType) {
                    case COMMANDER -> redCommander = clientChannel;
                    case MEMBER -> redMember = clientChannel;
                }
            }
            case BLUE -> {
                switch (playerType) {
                    case COMMANDER -> blueCommander = clientChannel;
                    case MEMBER -> blueMember = clientChannel;
                }
            }
        }
    }

    public void switchTurn(TeamType teamType, PlayerType playerType) {
        switch (teamType) {
            case RED:
                switch (playerType) {
                    case COMMANDER:
                        currentTurn = redMember;
                        return;
                    case MEMBER:
                        currentTurn = blueCommander;
                        return;
                    default:
                        return;
                }
            case BLUE:
                switch (playerType) {
                    case COMMANDER:
                        currentTurn = blueMember;
                        return;
                    case MEMBER:
                        currentTurn = redCommander;
                        return;
                    default:
                        return;
                }
            default:
        }
    }

    public WordData getWordDataByIndex(int index) {
        return words.get(index);
    }

    public int getRedCount() {
        return redCount;
    }

    public int getBlueCount() {
        return blueCount;
    }

    public List<String> getRedMessages() {
        return redMessages;
    }

    public List<String> getBlueMessages() {
        return blueMessages;
    }

    public SocketChannel getCurrentTurn() {
        return currentTurn;
    }

    public List<WordData> getWords() {
        return words;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setRedCount(int redCount) {
        this.redCount = redCount;
    }

    public void setBlueCount(int blueCount) {
        this.blueCount = blueCount;
    }
}
