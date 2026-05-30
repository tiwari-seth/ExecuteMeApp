package com.executeme.telegram.bot;

import com.executeme.broker.angelone.service.AngelLoginLinkService;
import com.executeme.telegram.config.TelegramBotProperties;
import com.executeme.users.service.TelegramUserService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.starter.AfterBotRegistration;

@Component
@ConditionalOnExpression("'${telegram.bot.username:}' != '' && '${telegram.bot.token:}' != ''")
public class AngelTelegramBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(AngelTelegramBot.class);

    private final TelegramBotProperties properties;
    private final AngelLoginLinkService angelLoginLinkService;
    private final TelegramUserService telegramUserService;

    public AngelTelegramBot(TelegramBotProperties properties,
                            AngelLoginLinkService angelLoginLinkService,
                            TelegramUserService telegramUserService) {
        this.properties = properties;
        this.angelLoginLinkService = angelLoginLinkService;
        this.telegramUserService = telegramUserService;
    }

    @Override
    public String getBotUsername() {
        return properties.username();
    }

    @Override
    public String getBotToken() {
        return properties.token();
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Received Telegram update: updateId={}", update.getUpdateId());
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            log.debug("Ignoring Telegram update without text message: updateId={}", update.getUpdateId());
            return;
        }
        User from = update.getMessage().getFrom();
        long chatId = update.getMessage().getChatId();
        long telegramUserId = from.getId();
        log.info("Processing Telegram message: updateId={}, chatId={}, telegramUserId={}",
                update.getUpdateId(), chatId, telegramUserId);

        if (!properties.isAllowed(telegramUserId)) {
            log.warn("Rejected Telegram user not present in allowlist: telegramUserId={}", telegramUserId);
            sendText(chatId, "Access denied.");
            return;
        }

        telegramUserService.upsertAllowedUser(telegramUserId, from.getUserName(), fullName(from));

        String text = update.getMessage().getText();
        if ("/start".equalsIgnoreCase(text) || "/login".equalsIgnoreCase(text)) {
            sendLoginButton(chatId, telegramUserId, "Please login to Angel One for today's trading session.");
        } else {
            sendText(chatId, "Use /login to authenticate your Angel One account.");
        }
    }

    @AfterBotRegistration
    public void afterRegistration(BotSession botSession) {
        log.info("Telegram bot registered for long polling: username={}, sessionRunning={}",
                properties.username(), botSession.isRunning());
    }

    public void sendLoginButton(long chatId, long telegramUserId, String message) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Login to Angel One");
        button.setUrl(angelLoginLinkService.createLoginUrl(telegramUserId));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(button)));

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.setReplyMarkup(markup);
        executeSafely(sendMessage);
    }

    private void sendText(long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        executeSafely(sendMessage);
    }

    private void executeSafely(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException ex) {
            throw new IllegalStateException("Unable to send Telegram message", ex);
        }
    }

    private static String fullName(User user) {
        String lastName = user.getLastName() == null ? "" : " " + user.getLastName();
        return (user.getFirstName() + lastName).trim();
    }
}
