package br.com.albert.controllers;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.jayway.jsonpath.internal.JsonFormatter;

import br.com.albert.services.BotService;

@RestController
@RequestMapping("telebot")
public class BotController {
//
//	@Value("${bot.token}")
//	private String token;
//	
//	@Value("${bot.username}")
//	private String username;

//	private BotService bot = new BotService(username, token);
	private BotService bot = new BotService();
	
	@PostMapping(value ="/send", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Message sendMsg(@RequestBody SendMessage message) {
		Message msg = bot.sendMessage(message);
		return msg;
	}
	
	@GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getJson() {
		String msg = bot.getJson();
		System.out.println("\n\n\n");
		System.out.println(JsonFormatter.prettyPrint(msg.toString()));
		return msg;
	}
}
