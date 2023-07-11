package br.com.albert.config;

import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.jayway.jsonpath.internal.JsonFormatter;

@Configuration
public class TelegramBot extends TelegramLongPollingBot {

	public TelegramBot(String token) {
		super(token);
	}
	
	@SuppressWarnings("deprecation")
	public TelegramBot() {
		
	}
	
	private Update update = new Update();

	@Override
	public void onUpdateReceived(Update update) {

		if (update.hasMessage() && update.getMessage().hasText()) {
			
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
		return BotConfigStrings.BOT_USERNAME;
	}

	@Override
	public String getBotToken() {
		return BotConfigStrings.BOT_TOKEN;
	}
}
