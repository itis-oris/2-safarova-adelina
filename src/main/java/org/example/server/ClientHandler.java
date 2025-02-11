package org.example.server;

import org.example.enums.GameState;
import org.example.model.Game;
import org.example.model.Ranges;
import org.example.utils.Coord;
import org.example.utils.Protocol;
import org.springframework.security.core.parameters.P;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    public Socket clientSocket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private boolean status = true;
    private String playerName;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(clientSocket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(clientSocket.getInputStream());

            Protocol.ParsedCommand joinCommand = Protocol.receiveCommand(ois);
            if (joinCommand == null || !joinCommand.action.startsWith(Protocol.JOIN)) {
                System.out.println("Клиент не прислал имя. Отключаем...");
                stopConnection();
                return;
            }

            this.playerName = joinCommand.action.substring(Protocol.JOIN.length());
            this.playerName = this.playerName.replace(":", "").trim();
            System.out.println("Игрок присоединился: " + this.playerName);

            Game game = MineSweeperServer.getGame();
            oos.writeInt(Ranges.GetSize().x);
            oos.writeInt(Ranges.GetSize().y);
            oos.writeObject(game);
            oos.flush();

            if (MineSweeperServer.getClients().size() < 2) {
                Protocol.sendStatus(oos, Protocol.WAITING);
            } else {
                MineSweeperServer.setGameStarted(true);
                broadcastToAll(Protocol.GAME_STARTED + ":" + "Игра началась!");
            }

            while (status) {
                Protocol.ParsedCommand command = Protocol.receiveCommand(ois);
                if (command == null) {
                    break;
                }
                switch (command.action) {
                    case Protocol.LEFT_CLICK:
                    case Protocol.RIGHT_CLICK:
                        // Игрок может делать действия только если игра началась
                        if (!MineSweeperServer.isGameStarted()) {
                            System.out.println("Игрок кликает до старта. Игнорируем.");
                            break;
                        }
                        game.PressLeftButton(command.coord);
                        processMove(command.action, command.coord);
                        break;
                    case Protocol.NEW_GAME:
                        System.out.println("Игрок " + playerName + " запросил новую игру");
                        break;
                    case Protocol.EXIT:
                        stopConnection();
                        break;
                    default:
                        System.out.println("Неизвестное сообщение: " + command.action);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка обработки клиента " + playerName + ": " + e.getMessage());
        } finally {
            stopConnection();
        }
    }

    // Обработка нажатия (хода)
    private void processMove(String action, Coord coord) throws IOException, ClassNotFoundException {
        Game game = MineSweeperServer.getGame();
        if (game == null) return;

        if (action.equals(Protocol.LEFT_CLICK)) {
            game.PressLeftButton(coord);

        } else {
            game.PressRightButton(coord);
        }


        GameState state = game.GetState();
//        Protocol.ParsedCommand parsedCommand = Protocol.receiveCommand(ois);
//        state = GameState.valueOf(parsedCommand.action);
        System.out.println("Состояние после хода: " + state);


        if (game.GetState() == GameState.BOMBED) {
            // Текущий клиент (this) подорвался, значит проиграл
            handleGameOver(false);
        } else if (game.GetState() == GameState.WINNER) {
            // Все бомбы обошли — ничья
            handleGameOver(true);
        } else {
            // Игра продолжается, всем отправляем обновлённое поле
            broadcastUpdatedGame(game);

        }
    }

    // Метод завершения игры
    private void handleGameOver(boolean isAllSafe) {
        Game game = MineSweeperServer.getGame();

        // Найдём оппонента
        ClientHandler opponent = null;
        for (ClientHandler ch : MineSweeperServer.getClients()) {
            if (ch != this) {
                opponent = ch;
                break;
            }
        }

        if (opponent == null) {
            // Если почему-то нет второго
            broadcastToAll("ERROR: нет второго игрока...");
            return;
        }

        if (isAllSafe) {
            // НИЧЬЯ: каждому +100
            MineSweeperServer.updateRating(this.playerName, 100);
            MineSweeperServer.updateRating(opponent.playerName, 100);
            broadcastToAll(Protocol.DRAW);

        } else {
            // this — проиграл, opponent — выиграл
            MineSweeperServer.updateRating(this.playerName, 0);
            MineSweeperServer.updateRating(opponent.playerName, 200);
            game.SetWinner(opponent.playerName);
            broadcastToAll(Protocol.WINNER + ":" + opponent.playerName);

        }

        // Рассылаем рейтинги обоим
        String ratingThis = this.playerName + "=" + MineSweeperServer.getRating(this.playerName);
        String ratingOpp = opponent.playerName + "=" + MineSweeperServer.getRating(opponent.playerName);
        broadcastToAll("RATING:" + ratingThis + ";" + ratingOpp);
        MineSweeperServer.saveRatings();

//         Сбрасываем игру
        int cols = Ranges.GetSize().x;
        int rows = Ranges.GetSize().y;
        int bombs = game.GetBomb().GetTotalBombs();

        Game newGame = new Game(cols, rows, bombs);
        newGame.StartSolo();

        MineSweeperServer.setGame(newGame);
        MineSweeperServer.setGameStarted(false);

        // Рассылаем новое поле, если хотим сразу начать
        broadcastNewGame(newGame);
    }

    private void broadcastUpdatedGame(Game game) {
        for (ClientHandler ch : MineSweeperServer.getClients()) {
            if (ch.status) {
                try {
                    Protocol.sendGameUpdate(ch.oos, game);
                } catch (IOException e) {
                    System.err.println("Ошибка отправки обновлённой игры: " + e.getMessage());
                }
            }
        }
    }

    private void broadcastToAll(String message) {
        for (ClientHandler ch : MineSweeperServer.getClients()) {
            if (ch.status) {
                try {
                    ch.oos.writeObject(message);
                    ch.oos.flush();
                } catch (IOException e) {
                    System.err.println("Ошибка broadcast: " + e.getMessage());
                }
            }
        }
    }

    private void broadcastNewGame(Game newGame) {
        for (ClientHandler ch : MineSweeperServer.getClients()) {
            if (ch.status) {
                try {
                    Protocol.sendNewGameBroadcast(ch.oos, newGame);
                } catch (IOException e) {
                    System.err.println("Ошибка отправки новой игры: " + e.getMessage());
                }
            }
        }
    }


    private void stopConnection() {
        if (status) {
            status = false;
            try {
                if (oos != null) oos.close();
                if (ois != null) ois.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Ошибка закрытия сокета у " + playerName + ": " + e.getMessage());
            }

            // Удаляем игрока из списка подключённых клиентов
            MineSweeperServer.getClients().remove(this);
            System.out.println("Игрок " + playerName + " отключился.");

//             Проверяем, остался ли второй игрок
            if (MineSweeperServer.getClients().size() == 1) {
                ClientHandler opponent = MineSweeperServer.getClients().get(0);
                System.out.println("updateRating(" + playerName +") вызывается!");
                MineSweeperServer.updateRating(this.playerName, 0);
                MineSweeperServer.updateRating(opponent.playerName, 200);

                try {
                    System.out.println("Выиграл игрок " + opponent.playerName + " (оппонент отключился)");
                    Protocol.sendOpponentWin(opponent.oos); // Отправляем оповещение о победе оставшемуся игроку
                } catch (IOException e) {
                    System.err.println("Ошибка отправки выигрыша оставшемуся игроку: " + e.getMessage());
                }
            }
        }

    }
}
