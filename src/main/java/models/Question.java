package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Question {

    private static final int MIN_OPTIONS = 2;
    private static final int MAX_OPTIONS = 4;

    private static final String ERROR_NULL_TEXT = "טקסט השאלה אינו יכול להיות ריק.";
    private static final String ERROR_INVALID_OPTIONS = "שאלה חייבת להכיל בין 2 ל-4 אפשרויות תשובה.";

    private final String text;
    private final List<String> options;

    public Question(String text, List<String> options) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(ERROR_NULL_TEXT);
        }
        if (options == null || options.size() < MIN_OPTIONS || options.size() > MAX_OPTIONS) {
            throw new IllegalArgumentException(ERROR_INVALID_OPTIONS);
        }

        this.text = text.trim();
        this.options = new ArrayList<>(options);
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return Collections.unmodifiableList(options);
    }
}