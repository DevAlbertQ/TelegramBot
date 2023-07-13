package br.com.albert.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.User;

import com.jayway.jsonpath.internal.JsonFormatter;

import br.com.albert.config.TelegramBot;
import br.com.albert.models.TelegramUser;
import br.com.albert.repositories.TelegramUserRepository;

@Service
public class BotService {

//	private String botUsername;
//	
//	@Value("${bot.token}")
//	private String botToken;
	private TelegramBot bot;
	
	@Autowired
	private TelegramUserRepository repository;

	
	public BotService() {
		if(this.bot == null) {
			this.bot = new TelegramBot();
		}
	}
	



	public Message sendMessage(SendMessage message) {
		return bot.sendMsg(message);
	}
	
	public SendMessage getJsonMessage() {
		SendMessage message = new SendMessage();
		message.setChatId(1l);
		message.setText("Message");
		List<MessageEntity> entities = new ArrayList<>();
		MessageEntity entity = new MessageEntity();
		entity.setType("bot_command");
		entity.setOffset(0);
		entity.setLength(7);
		entity.setText("Message");
		entities.add(entity);
		message.setEntities(entities);
		System.out.println(JsonFormatter.prettyPrint(message.toString()));
		return message;
	}
	
	public String getJson() {
		String string = ""
				+ "{\r\n"
				+ "	\"chat_id\": \"856471610\",\r\n"
				+ "	\"text\": \"/Message is a command\",\r\n"
				+ "	\"entities\": [\r\n"
				+ "		{\r\n"
				+ "		   \"type\": \"bot_command\",\r\n"
				+ "		   \"offset\": \"0\",\r\n"
				+ "		   \"length\": \"7\",\r\n"
				+ "		   \"text\": \"/Message\"\r\n"
				+ "	   }\r\n"
				+ "	],\r\n"
				+ "	\"method\": \"sendmessage\"\r\n"
				+ "}";
		return JsonFormatter.prettyPrint(string);
	}




	public void register(User from) {
		if(!isRegistered(from.getId())) {
			TelegramUser user = new TelegramUser();
			user.setAddDate(new Date());
			user.setFirstName(from.getFirstName());
			user.setLastName(from.getLastName());
			user.setIdUser(from.getId());
			user.setUsername(from.getUserName());
			repository.save(user);
		}
		
	}

	public boolean isRegistered(Long id) {
		return repository.existsById(id);
	}
}
