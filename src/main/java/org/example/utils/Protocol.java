package org.example.utils;

import org.example.enums.GameState;
import org.example.model.Game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Protocol {
    // Базовые команды
    public static final String LEFT_CLICK = "Left";
    public static final String RIGHT_CLICK = "Right";
    public static final String NEW_GAME = "NgPls";
    public static final String NEW_GAME_BROADCAST = "Ng Guys";
    public static final String EXIT = "Exit";
    public static final String OPPONENT_WIN = "OpponentWin"; // Новый флаг

    // Дополнительные команды
    public static final String JOIN = "JOIN"; // JOIN:PlayerName
    public static final String WAITING = "WAITING";
    public static final String GAME_STARTED = "GAME_STARTED";
    public static final String WINNER = "WINNER";
    public static final String DRAW = "DRAW";



    // Отправка команды о том, что оппонент выиграл
    public static void sendOpponentWin(ObjectOutputStream oos) throws IOException {
        oos.writeObject(OPPONENT_WIN);
        oos.flush();
    }

    // Отправка команды клика
    public static void sendClick(ObjectOutputStream oos, String action, Coord coord) throws IOException {
        oos.writeObject(clickRequest(action, coord));
        oos.writeObject(coord); // Координату пишем отдельно, чтобы клиент мог считать
        oos.flush();
    }

    // Отправка запроса "новая игра"
    public static void sendNewGameRequest(ObjectOutputStream oos) throws IOException {
        oos.writeObject(newGameRequest());
        oos.flush();
    }

    // Отправка команды выхода
    public static void sendExitRequest(ObjectOutputStream oos) throws IOException {
        oos.writeObject(exitRequest());
        oos.flush();
    }

    // Отправка новой игры всем клиентам
    public static void sendNewGameBroadcast(ObjectOutputStream oos, Game game) throws IOException {
        oos.writeObject(newGameBroadcast());
        oos.writeObject(game);
        oos.flush();
    }

    // Отправка статуса (например, WAITING)
    public static void sendStatus(ObjectOutputStream oos, String status) throws IOException {
        oos.writeObject(status);
        oos.flush();
    }
    public static void sendStatusGameState(ObjectOutputStream oos, GameState status) throws IOException {
        oos.writeObject(status);
        oos.flush();
    }

    // Отправка обновлённого состояния Game (при каждом ходе)
    public static void sendGameUpdate(ObjectOutputStream oos, Game game) throws IOException {
        oos.writeObject("GAME_UPDATE");
        oos.writeObject(game);
        oos.flush();

    }


    // Получение команды
    public static ParsedCommand receiveCommand(ObjectInputStream ois)
            throws IOException, ClassNotFoundException {
        // Пытаемся считать строку
        Object obj = ois.readObject();
        String message = null;
        Coord coord = null;
        if ((obj instanceof String)) {
            message = (String) obj;

            // Если следом есть Coord — пытаемся считать
            coord = null;
            if (message.startsWith(LEFT_CLICK) || message.startsWith(RIGHT_CLICK)) {
                Object cobj = ois.readObject();
                if (cobj instanceof Coord) {
                    coord = (Coord) cobj;
                }
            }
        } else if(obj instanceof GameState) {
            message = (String) obj;

            coord = null;
        }


        return parseCommand(message, coord);
    }



    private static String clickRequest(String action, Coord coord) {
        return action + ":" + coord.x + "," + coord.y;
    }

    private static String newGameRequest() {
        return NEW_GAME;
    }

    private static String exitRequest() {
        return EXIT;
    }

    private static String newGameBroadcast() {
        return NEW_GAME_BROADCAST;
    }


    private static ParsedCommand parseCommand(String message, Coord coord) {
        // JOIN:ИмяИгрока
        if (message.startsWith(JOIN)) {
            return new ParsedCommand(message, null);
        }

        if (message.equals(NEW_GAME)) {
            return new ParsedCommand(NEW_GAME, null);
        }
        if (message.equals(EXIT)) {
            return new ParsedCommand(EXIT, null);
        }
        if (message.startsWith(LEFT_CLICK) || message.startsWith(RIGHT_CLICK)) {
            // Клик
            return new ParsedCommand(message.startsWith(LEFT_CLICK) ? LEFT_CLICK : RIGHT_CLICK, coord);
        }


// прочие (WAITING, GAME_STARTED, WINNER, DRAW, ...) — обычно сервер шлёт как строку
        // но клиенту тоже может приходить
        return new ParsedCommand(message, null);
    }

    // Вспомогательный класс
    public static class ParsedCommand {
        public String action;  // что за команда
        public Coord coord;    // координата (может быть null)

        public ParsedCommand(String action, Coord coord) {
            this.action = action;
            this.coord = coord;
        }
    }
}

