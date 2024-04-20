package pro.sky.telegrambot.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.sql.Timestamp;

@Entity(name = "notification_task")
public class NotificationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long chatId;
    private String chatText;
    private Timestamp sendAt;
    public Long getChatId() {
        return chatId;
    }
    public Long getId() {
        return id;
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
    public void setId(Long id) {
        this.id = id;
    }

    public void setChatText(String chatText) {
        this.chatText = chatText;
    }

    public void setSendAt(Timestamp sendAt) {
        this.sendAt = sendAt;
    }
}