package org.example.client;

import org.example.enums.GameState;
import org.example.model.Game;
import org.example.model.Ranges;
import org.example.utils.Coord;
import org.example.utils.Protocol;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends Thread {
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private Game game;
    private JLabel label;
    private JPanel panel;
    public Socket socket;
    private boolean status;
    private int cols;
    private int rows;
    private String ip;
    private int port;

    // Имя игрока (вводится при старте)
    private String playerName;

    public static boolean canPlay = false; // признак, можем ли кликать по полю?

    public Client(String ip, int port, String playerName) {
        this.ip = ip;
        this.port = port;
        this.playerName = playerName;
    }

    public void SetGame(Game game) {
        this.game = game;
    }

    public Game GetGame() {
        return game;
    }

    public void SetLabel(JLabel label) {
        this.label = label;
    }

    public void SetPanel(JPanel panel) {
        this.panel = panel;
    }

    public void run() {
        try {
            WaitingForAction();
        } catch (Exception ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean IsConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    public void ClientStart() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 666);
            if (IsConnected()) {
                oos = new ObjectOutputStream(socket.getOutputStream());
                oos.flush();
                ois = new ObjectInputStream(socket.getInputStream());
                System.out.println("Client is started");

                // 1) Отправляем серверу JOIN:PlayerName
                oos.writeObject(Protocol.JOIN + ":" + playerName);
                oos.flush();

                // 2) Считываем от сервера cols, rows, затем объект Game
                cols = ois.readInt();
                rows = ois.readInt();
                Ranges.SetSize(new Coord(cols, rows));


                game = (Game) ois.readObject();
                System.out.println("Игра получена: " + game.toString());
                status = true;
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void NewGameRequest() {
        if (IsConnected()) {
            try {
                Protocol.sendNewGameRequest(oos);
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void WaitingForAction() throws Exception {
        while (status) {
            try {
                // Здесь может приходить либо строка, либо "GAME_UPDATE" + объект Game
                Object obj = ois.readObject();
                if (obj == null) {
                    break;
                }
                if (obj instanceof String) {
                    String message = (String) obj;
                    parseMessage(message);
                } else if (obj instanceof Game) {
                    // Это может быть полный объект Game
                    Game updatedGame = (Game) obj;
                    this.game = updatedGame;
                    RepaintPanel();
                } else {
                    System.out.println("Неизвестный тип сообщения от сервера");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Обработка входящих строк
    private void parseMessage(String msg) {
        if (msg.equals(Protocol.WAITING)) {
            label.setText("Ожидаем второго игрока...");
            canPlay = false;
            game.SetState(GameState.WAITING); // Обновляем состояние игры
        } else if (msg.startsWith(Protocol.GAME_STARTED)) {
            label.setText("Игра началась!");
            canPlay = true;
            game.SetState(GameState.PLAYED); // Обновляем состояние игры
        } else if (msg.startsWith(Protocol.WINNER)) {
            String[] parts = msg.split(":");
            if (parts.length == 2) {
                String winnerName = parts[1].trim();
                label.setText("Выиграл игрок " + winnerName);
                game.SetWinner(winnerName);
                game.SetState(GameState.BOMBED); // Обновляем состояние игры

            }
            canPlay = false;
        } else if (msg.equals(Protocol.DRAW)) {
            label.setText("Ничья!");
            game.SetState(GameState.WINNER); // Обновляем состояние игры
            canPlay = false;
        } else if (msg.equals(Protocol.OPPONENT_WIN)) {
            label.setText("Ваш соперник отключился. Вы победили!");
            canPlay = false;
        } else if (msg.startsWith("RATING:")) {
            System.out.println("Текущие рейтинги: " + msg.substring(7));
        } else if (msg.equals("REJECT")) {
            JOptionPane.showMessageDialog(null,
                    "Сервер переполнен, ваше подключение отклонено.",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            StopConnection();
        } else if (msg.equals("GAME_UPDATE")) {
            // Обрабатываем обновление игры
        } else {
            System.out.println("Неизвестное текстовое сообщение: " + msg);
        }

        RepaintPanel(); // Перерисовываем панель после обновления состояния
    }

    public void RepaintPanel() {
        SwingUtilities.invokeLater(() -> {
            label.setText(GameUI.GetMessage(IsConnected(), game));
            panel.repaint();
        });
    }

    // Метод отправки клика
    public void PressButton(String action, Coord coord) {
        if (IsConnected() && canPlay) {
            try {
                Protocol.sendClick(oos, action, coord);
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void StopConnection() {
        if (status) {
            status = false;
            try {
                Protocol.sendExitRequest(oos); // Отправка запроса на выход
                // Также можно информировать сервер, что игрок завершил игру
                Protocol.sendOpponentWin(oos);
                Thread.sleep(120);
                if (oos != null) oos.close();
                if (ois != null) ois.close();
                if (socket != null) socket.close();
            } catch (IOException | InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
