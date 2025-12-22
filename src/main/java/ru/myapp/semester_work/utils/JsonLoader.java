package ru.myapp.semester_work.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class JsonLoader {

    private static final String PATH = "src/main/resources/words.json";

    private JsonLoader() {}

    public static List<String> loadStringListFromJson() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(Path.of(PATH).toFile(),
                new TypeReference<List<String>>() {});
    }
}
