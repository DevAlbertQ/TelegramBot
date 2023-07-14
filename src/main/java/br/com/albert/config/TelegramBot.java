package br.com.albert.config;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.jayway.jsonpath.internal.JsonFormatter;

import br.com.albert.services.BotService;
import jakarta.annotation.PostConstruct;

@Configuration
public class TelegramBot extends TelegramLongPollingBot {

	public TelegramBot(@Value("${bot.token}") String token) {
		super(token);
	}

	private enum UserStates{
		USER_EMAIL,
		USER_PHONE,
		USER_NAME
	};
	
	private HashMap<UserStates, Long> userStates;
	
	@Value("${bot.token}")
	private String token;
	
	@Value("${bot.username}")
	private String username;
	
	@Autowired
	private BotService service;
	
	@PostConstruct
	public void init() {
		service.setBot(this);
	}
	
	
	private Update update = new Update();
	
	
	/**
	 *@implNote The method that controls the messages received by the bot 
	 **/
	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage() && update.getMessage().hasText()) {
			
			if(service.isRegistered(update.getMessage().getFrom().getId())) {
				register(update.getMessage().getFrom());
			}
			
			service.register(update.getMessage().getFrom());
			
			SendMessage sender = new SendMessage();
			
			sender.setChatId(update.getMessage().getChatId());
			String message = "";
			
			switch (update.getMessage().getText()) {
			case "/Opcao1": {
				message = "Larga mão de ser preguiçoso %s! Posso até te ajudar não fazer tudo!";
				break;
			}

			case "/Opcao2": {
				message = "Veja se eu tenho cara de copeiro!";
				break;
			}

			case "/Opcao3": {
				message = "Isso também não consigo! Não sou você!";
				break;
			}
			
			case "/Cadastrar":{
				
			}
			

			default:
				message = "Olá %s! Eu sou o bot do Albert, o que posso fazer por você hoje?\n"
						+ "/Opcao1: Fazer todo o seu trabalho!\n" 
						+ "/Opcao2: Fazer um café!\n" 
						+ "/Opcao3: Fazer nada!\n"
						+ "Escolha uma das 3 para continuar";
			}
			
			sender.setText(String.format(message,
					update.getMessage().getFrom().getFirstName()));
			System.out.println(JsonFormatter.prettyPrint(update.toString()));
			System.out.println(String.format(message,
					update.getMessage().getFrom().getFirstName()));

			try {
				execute(sender);
			} catch (TelegramApiException e) {
				// TODO: handle exception
			}
		}
		if(this.update.getUpdateId() == null || this.update.getUpdateId() < update.getUpdateId()) {
			this.update = update;
		}
			
	}

	private void register(User from) {
		SendMessage msg = new SendMessage();
		msg.setChatId(from.getId());
		msg.setText("Olá!"
				+ "\nvimos que o seu contato ainda não está em nossa base de dados. "
				+ "\nPara que possamos realizar o envio de mensagens precisamos fazer o cadastro."
				+ "\nPodemos começar?"
				+ "\nClique na alternativa desejada:"
				+ "\n/Cadastrar: Sim gostaria de me cadastrar!"
				+ "\n/Recusar: Não deixa para a próxima!");
		service.register(from);
	}

	public Message sendMsg(SendMessage message) {
		Message msg = new Message();
		try {
			msg = execute(message);
		} catch (TelegramApiException e) {
			// TODO: handle exception
		}
		return msg;
	}
	
	public Message sendPoolQuestion(SendPoll pool) {
		Message msg = new Message();
		if(pool != null) {
			try {
				msg = execute(pool);
			} catch (TelegramApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return msg;
	}
	@Override
	public String getBotUsername() {
		return this.username;
	}

	@Override
	public String getBotToken() {
		return this.token;
	}
	
	public BotService getBotService() {
		return this.service;
	}
}
