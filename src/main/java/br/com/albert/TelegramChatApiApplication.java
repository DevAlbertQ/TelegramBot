package br.com.albert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import br.com.albert.config.TelegramBot;
import jakarta.annotation.PostConstruct;

/*
@SpringBootApplication
public class TelegramChatApiApplication {

	@Value("${bot.token}")
	private static String token;
	
	@Value("${bot.username}")
	private static String username;
	
	public static void main(String[] args) {
		SpringApplication.run(TelegramChatApiApplication.class, args);
		
		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(new TelegramBot(token));
		} catch (TelegramApiException e) {
			e.printStackTrace();
			
		}
		
	}

}
*/

@SpringBootApplication
@ConfigurationProperties(prefix = "bot")
public class TelegramChatApiApplication {

	@Value("${env:bot.token}")
	private String token;

	@Value("${env:bot.username}")
	private String username;
	
	@Autowired
	private TelegramBot telegramBot;
	
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