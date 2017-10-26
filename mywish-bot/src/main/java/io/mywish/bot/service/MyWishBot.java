package io.mywish.bot.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class MyWishBot extends TelegramLongPollingBot {
    @Autowired
    private TelegramBotsApi telegramBotsApi;

    private final ConcurrentHashMap<Long, AtomicInteger> chats = new ConcurrentHashMap<>();
    private final List<BigInteger> investments = new ArrayList<>();

    @Getter
    @Value("${io.mywish.bot.token}")
    private String botToken;
    @Getter
    @Value("${io.mywish.bot.name}")
    private String botUsername;

    @PostConstruct
    protected void init() {
        try {
            telegramBotsApi.registerBot(this);
        }
        catch (TelegramApiRequestException e) {
            log.error("Failed during the bot registration.", e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId;
        if (update.hasChannelPost()) {
            chatId = update.getChannelPost().getChatId();
        }
        else if (update.hasEditedChannelPost()) {
            chatId = update.getEditedChannelPost().getChatId();
        }
        else if (update.hasEditedMessage()) {
            chatId = update.getEditedMessage().getChatId();
        }
        else if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            if (update.getMessage().getChat().isUserChat()) {
                log.debug("Direct message received from {}.", update.getMessage().getFrom());
                repeatLatest(chatId);
            }
            else {
                log.debug("Bot mentioned in chat {}.", chatId);
                repeatLatest(chatId);
            }

        }
        else {
            return;
        }
        AtomicInteger previous = chats.putIfAbsent(
                chatId,
                new AtomicInteger()
        );
        if (previous == null) {
            log.info("Bot was added to the chat {}. Now he is in {} chats.", chatId, chats.size());
        }
    }

    public void onInvestment(final String sender, final BigInteger weiAmount) {
        log.info("Investment received from {}, amount {}.", sender, weiAmount);
        int last;
        synchronized (investments) {
            investments.add(weiAmount);
            last = investments.size();
        }

        sendMessage(last, weiAmount);
    }

    private void repeatLatest(long chatId) {
        BigInteger latest;
        synchronized (investments) {
            if (investments.size() > 0) {
                latest = investments.get(investments.size() - 1);
            }
            else {
                latest = null;
            }
        }

        String message;
        if (latest == null) {
            message = "No investment detected.";
        }
        else {
            String eth = toEth(latest);
            message = "The latest investment was: " + eth + " ETH";
        }

        try {
            execute(new SendMessage()
                    .setChatId(chatId)
                    .setText(message)
            );
        }
        catch (TelegramApiException e) {
            log.error("Sending message '{}' to chat '{}' was failed.", message, chatId, e);
        }
    }

    private void sendMessage(int index, BigInteger weiAmount) {
        String eth = toEth(weiAmount);
        final String message = "New investment: " + eth + " ETH";
        for (long chatId: chats.keySet()) {
            try {
                execute(new SendMessage()
                        .setChatId(chatId)
                        .setText(message)
                );
            }
            catch (TelegramApiException e) {
                log.error("Sending message '{}' to chat '{}' was failed.", message, chatId, e);
                chats.remove(chatId);
            }
        }
    }

    private static String toEth(BigInteger weiAmount) {
        BigInteger hundreds = weiAmount.divide(BigInteger.valueOf(10000000000000000L));
        BigInteger[] parts = hundreds.divideAndRemainder(BigInteger.valueOf(100));
        BigInteger eth = parts[0];
        int rem = parts[1].intValue();
        String sRem;
        if (rem == 0) {
            sRem = "";
        }
        else if (rem < 10) {
            sRem = ".0" + rem;
        }
        else {
            sRem = "." + rem;
        }
        return eth + sRem;
    }
}
