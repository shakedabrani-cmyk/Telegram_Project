package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SurveyParticipant {

    private static final int MIN_ANSWER_INDEX = 0;

    private static final String ERROR_NULL_MEMBER = "אובייקט חבר הקהילה אינו יכול להיות ריק.";
    private static final String ERROR_INVALID_INDEX = "אינדקס התשובה אינו תקין וחייב להיות חיובי.";

    private final CommunityMember member;
    private final List<Integer> answers;

    public SurveyParticipant(CommunityMember member) {
        if (member == null) {
            throw new IllegalArgumentException(ERROR_NULL_MEMBER);
        }
        this.member = member;
        this.answers = new ArrayList<>();
    }

    public CommunityMember getMember() {
        return member;
    }

    public List<Integer> getAnswers() {
        return Collections.unmodifiableList(answers);
    }

    public void addAnswer(int optionIndex) {
        if (optionIndex < MIN_ANSWER_INDEX) {
            throw new IllegalArgumentException(ERROR_INVALID_INDEX);
        }
        this.answers.add(optionIndex);
    }
}