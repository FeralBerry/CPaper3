package pro.sky.telegrambot.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.scheduling.annotation.Scheduled;
import pro.sky.telegrambot.configuration.BotConfig;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.model.NotificationTaskRepository;
import pro.sky.telegrambot.model.User;
import pro.sky.telegrambot.model.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

// Slf4j - аннотация для использования логов из библиотеки lombok
@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationTaskRepository notificationTaskRepository;
    final BotConfig config;
    static final String HELP_TEXT = "This bot is created to demonstrate Spring capabilities.\n\n" +
            "You can execute commands from the main menu on the left or by typing a command:\n\n" +
            "Type /start to see a welcome message\n\n" +
            "Type /help to see this message again";
    public TelegramBot(BotConfig config){
        this.config = config;
        // создаем список команд для меню
        List<BotCommand> botCommandList = new ArrayList<>();
        botCommandList.add(new BotCommand("/start","get welcome message"));
        botCommandList.add(new BotCommand("/help","info how to use this bot"));
        // создаем кнопку с меню и обрабатываем ошибку
        try{
            this.execute(new SetMyCommands(botCommandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e){
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }
    @Override
    public String getBotUsername(){
        return config.getBotName();
    }
    @Override
    public String getBotToken(){
        return config.getToken();
    }
    @Override
    public void onUpdateReceived(org.telegram.telegrambots.meta.api.objects.Update update) {
        // проверяем есть ли отправленные сообщения
        if(update.hasMessage() && update.getMessage().hasText()){
            // получаем отправленное сообщение
            String message = update.getMessage().getText();
            // получаем id чата
            long chatId = update.getMessage().getChatId();
            // сверяем полученное сообщение и выполняем команду
            switch (message){
                case "/start":
                    // передаем информацию в метод регистрации пользователя и выводим приветственное сообщение
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:
                    Pattern p = Pattern.compile("\\d{2}.\\d{2}.\\d{4} \\d{2}:\\d{2} \\w");
                    if(p.matcher(message).matches()){
                        try {
                            sendMessage(chatId, "Your notification " + message + " saved");
                            saveNotification(update.getMessage().getChatId(),message);
                        } catch (ParseException e) {
                            log.error("Error occurred: " + e.getMessage());
                        }
                    } else {
                        sendMessage(chatId, "Sorry, command was not recognized");
                    }
            }
        }
    }
    @Scheduled(cron = "${cron.telegram.notification.send.scheduled}")
    private void sendNotification(){
        var notifications = notificationTaskRepository.findAll();
        SimpleDateFormat timeStamp = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        for (NotificationTask task : notifications) {
            if(task.getSendAt().after(timeStamp.get2DigitYearStart())){
                sendMessage(task.getChatId(),task.getChatText());
                notificationTaskRepository.delete(task);
            }
        }
    }
    private void saveNotification(Long chatId, String message) throws ParseException {
        // убираем из строки все 2-3 и более пробелов подряд
        String trimMessage = message.replaceAll("( )+", " ");
        // создаем объект форматом даты для парсера
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        String[] msg = trimMessage.split(" ");
        Date parsedDate = dateFormat.parse(msg[0] + " " + msg[1]);
        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
        NotificationTask notificationTask = new NotificationTask();
        notificationTask.setSendAt(timestamp);
        notificationTask.setChatId(chatId);
        notificationTask.setChatText(message);
        notificationTaskRepository.save(notificationTask);
        log.info("notificationTask saved: " + notificationTask);
    }
    // метод регистрации нового пользователя в базу данных
    private void registerUser(@NotNull Message message) {
        if(userRepository.findById(message.getChatId()).isEmpty()){
            // получаем информацию ИД чата и информацию о пользователе
            var chatId = message.getChatId();
            var chat = message.getChat();
            User user = new User();
            // записываем в объект информацию о пользователе
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));
            // регистрируем пользователя, записываем в БД
            userRepository.save(user);
            log.info("user saved: " + user);
        }
    }

    // метод отправляющий сообщение для команды /start
    private void startCommandReceived(long chatId, String name){
        String answer = "Hi, " + name + "!";
        log.info("Replied to user " + name);
        sendMessage(chatId,answer);
    }
    // метод для отправки сообщения ботом
    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        // обрабатываем ошибку отправки
        try {
            execute(message);
        } catch (TelegramApiException e){
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
