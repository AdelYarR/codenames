package ru.myapp.semester_work.utils;

import ru.myapp.semester_work.models.TeamType;
import ru.myapp.semester_work.models.WordData;

import java.util.ArrayList;
import java.util.List;

public class WordDataHelper {

    private WordDataHelper() {}

    public static String convertWordsToString(List<WordData> words) {
        if (words == null || words.isEmpty()) {
            return "";
        }

        StringBuilder wordsStringBuilder = new StringBuilder();
        for (WordData wordData : words) {
            wordsStringBuilder.append(wordData.getWord()).append(",")
                    .append(wordData.getTeamType()).append(",")
                    .append(wordData.isGuessed()).append(",")
                    .append(wordData.getPositionX()).append(",")
                    .append(wordData.getPositionY()).append("/");
        }

        wordsStringBuilder.setLength(wordsStringBuilder.length() - 1);
        return wordsStringBuilder.toString();
    }

    public static List<WordData> convertStringToWords(String wordStrings) {
        List<WordData> words = new ArrayList<>();

        if (wordStrings == null || wordStrings.trim().isEmpty()) {
            return words;
        }

        String[] wordStringsParts = wordStrings.split("/");
        for (String wordDataString : wordStringsParts) {
            String[] wordDataParts = wordDataString.split(",");
            WordData wordData = WordData.builder()
                    .word(wordDataParts[0])
                    .teamType(TeamType.valueOf(wordDataParts[1]))
                    .guessed(Boolean.parseBoolean(wordDataParts[2]))
                    .positionX(Integer.parseInt(wordDataParts[3]))
                    .positionY(Integer.parseInt(wordDataParts[4]))
                    .build();
            words.add(wordData);
        }

        return words;
    }
}
