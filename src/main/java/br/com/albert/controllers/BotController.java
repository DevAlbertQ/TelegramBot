package br.com.albert.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.jayway.jsonpath.internal.JsonFormatter;

import br.com.albert.enums.UserPosition;
import br.com.albert.models.TelegramUser;
import br.com.albert.services.TelegramBotService;
import br.com.albert.services.TelegramUserService;

@RestController
@RequestMapping("telebot")
public class BotController {

	@Autowired
	private TelegramUserService userService;
	
	@Autowired
	private TelegramBotService botService;
	
	@PostMapping(value ="/send", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Message sendMsg(@RequestBody SendMessage message) {
		Message msg = botService.sendMessage(message);
		return msg;
	}
	
	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getJson() {
		String msg = botService.getJson();
		System.out.println("\n\n\n");
		System.out.println(JsonFormatter.prettyPrint(msg.toString()));
		return msg;
	}
	
	@GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<TelegramUser> listAllUsers(){
		return userService.listAllUsers();
	}
	
	@GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public TelegramUser getOneUser(@PathVariable(value = "userId") Long userId) {
		return userService.findOneUser(userId);
	}
	
	@PostMapping(value = "/sendtoall", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void sendMessageToAllUsers(@RequestBody String message) {
		botService.sendMessageToAllUsers(message);
	}
	
	@PostMapping(value = "/sendbyposition", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void sendMessageToPositionUsers(@RequestBody String message ,@RequestBody UserPosition position) {
		botService.sendMessageToPositionUsers(message, position);
	}
}
