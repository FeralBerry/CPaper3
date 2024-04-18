package pro.sky.telegrambot.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.sql.Timestamp;

@Entity(name = "notification_task")
public class NotificationTask {
    @Id
    private Long chatId;
    private String chatText;
    private Timestamp sendAt;
    public Long getChatId() {
        return chatId;
    }
    public String getChatText() {
        return chatText;
    }

    public Timestamp getSendAt() {
        return sendAt;
    }
    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setChatText(String chatText) {
        this.chatText = chatText;
    }

    public void setSendAt(Timestamp sendAt) {
        this.sendAt = sendAt;
    }
}