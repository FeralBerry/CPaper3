package pro.sky.telegrambot.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;


@Configuration
@EnableScheduling
@Data
public class BotConfig {
    @Value("${telegram.bot.token}")
    private String token;
    @Value("${telegram.bot.name}")
    private String botName;
}
