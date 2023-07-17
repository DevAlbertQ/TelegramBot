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
import br.com.albert.util.RegexUtils;
import br.com.albert.util.UserStates;

@Service
public class BotService {

	private TelegramBot bot;
	
	@Autowired
	private TelegramUserRepository repository;

	
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
	
	public TelegramUser save(TelegramUser user) {
		return repository.save(user);
	}

	public boolean isRegistered(Long id) {
		return repository.existsById(id);
	}
	
	public void setBot(TelegramBot bot) {
		this.bot = bot;
	}
	
	public List<UserStates> getUserState(Long id) {
		TelegramUser user;
		List<UserStates> state = new ArrayList<>();
		if(repository.existsById(id)) {
			user = repository.findById(id).get();
			state.add(UserStates.REGISTER);
			if(!user.getFirstName().isBlank()) {
				state.add(UserStates.USER_FULL_NAME);
			}
			if(!user.getEmail().isBlank() && RegexUtils.isEmailValid(user.getEmail())) {
				state.add(UserStates.EMAIL);
			}
			if(user.getCellNumber() != null && RegexUtils.isCellNumberValid(user.getCellNumber().toString())) {
				state.add(UserStates.USER_PHONE);
			}
			if(!user.getUsername().isBlank()) {
				state.add(UserStates.USER_PHONE);
			}
		}
		
		return state;
	}
	
}
