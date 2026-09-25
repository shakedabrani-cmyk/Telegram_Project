package models;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CommunityMember {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String DEFAULT_UNKNOWN_NAME = "Unknown";
    private static final String EMPTY_STRING = "";

    private final long chatId;
    private final String name;
    private final String username;
    private final String joinTime;

    public CommunityMember(long chatId, String name, String username) {
        this.chatId = chatId;
        this.name = (name != null) ? name : DEFAULT_UNKNOWN_NAME;
        this.username = (username != null) ? username : EMPTY_STRING;
        this.joinTime = LocalTime.now().format(TIME_FORMATTER);
    }

    public long getChatId() {
        return chatId;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getJoinTime() {
        return joinTime;
    }
}