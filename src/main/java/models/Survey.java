package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Survey {

    private static final String ERROR_NULL_TOPIC = "נושא הסקר אינו יכול להיות ריק.";
    private static final String ERROR_NULL_QUESTIONS = "הסקר חייב להכיל לפחות שאלה אחת.";
    private static final String ERROR_NULL_COMMUNITY = "הסקר חייב לכלול לפחות משתתף אחד.";
    private static final String ERROR_INVALID_INDEX = "אינדקס השאלה חורג מגבולות הסקר.";

    private final String topic;
    private final List<Question> questions;
    private final List<SurveyParticipant> participants;

    public Survey(String topic, List<Question> questions, List<CommunityMember> currentCommunity) {
        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException(ERROR_NULL_TOPIC);
        }
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException(ERROR_NULL_QUESTIONS);
        }
        if (currentCommunity == null || currentCommunity.isEmpty()) {
            throw new IllegalArgumentException(ERROR_NULL_COMMUNITY);
        }

        this.topic = topic.trim();
        this.questions = new ArrayList<>(questions);
        this.participants = new ArrayList<>();

        for (CommunityMember member : currentCommunity) {
            if (member != null) {
                this.participants.add(new SurveyParticipant(member));
            }
        }
    }

    public String getTopic() {
        return topic;
    }

    public List<Question> getQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public Question getQuestionByIndex(int index) {
        if (index < 0 || index >= questions.size()) {
            throw new IndexOutOfBoundsException(ERROR_INVALID_INDEX);
        }
        return questions.get(index);
    }

    public List<SurveyParticipant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public int getTotalParticipants() {
        return participants.size();
    }
}