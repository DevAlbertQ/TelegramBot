package br.com.albert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import br.com.albert.services.BotService;
import jakarta.annotation.PostConstruct;

@SpringBootApplication
@ConfigurationProperties(prefix = "bot")
public class TelegramChatApiApplication {

	@Value("${env:bot.token}")
	private String token;

	@Value("${env:bot.username}")
	private String username;
	
	@Autowired
	private BotService telegramBot;
	
	public static void main(String[] args) {
		SpringApplication.run(TelegramChatApiApplication.class, args);
	}
	
	@PostConstruct
	public void registerBot() {
		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(telegramBot);
		} catch (TelegramApiException e) {
//			log.error("Failed to register bot", e);
		}
	}

}