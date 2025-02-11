package org.example.server;


import org.example.model.Game;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class MineSweeperServer {
    // Храним общий рейтинг игроков
    public static HashMap<String, Integer> playerRatings = new HashMap<>();
    private static final String RATINGS_FILE = "/Users/pro/IdeaProjects/MinesweeperHelper/ratings.txt";

    // Храним всех клиентов (ровно 2, но можно сделать ограничение)
    private static ArrayList<ClientHandler> clients = new ArrayList<>();

    // Основная «общая» игра
    private static Game game;

    // Флаг, что игра началась (2 игрока подключены)
    private static boolean gameStarted = false;

    private static final int MAX_PLAYERS = 2;

    public static void main(String[] args) {
        // Загрузим рейтинги при старте
        loadRatings();

        int port = 1777;
        int cols = 9;
        int rows = 9;
        int bombs = 12;

        // Инициализируем общее поле «game»
        game = new Game(cols, rows, bombs);
        game.StartSolo(); // Подготавливаем бомбы/матрицы

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            while (true) {
                System.out.println("Ожидание подключения клиента...");
                Socket clientSocket = serverSocket.accept();

                if (clients.size() >= MAX_PLAYERS) {
                    System.out.println("Сервер полон. Отклоняем подключение: " + clientSocket.getInetAddress());
                    ObjectOutputStream tempOos =
                            new ObjectOutputStream(clientSocket.getOutputStream());
                    tempOos.writeObject("REJECT");
                    tempOos.close();
                    clientSocket.close();
                    continue;
                }

                System.out.println("Клиент подключён: " + clientSocket.getInetAddress());
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                new Thread(handler).start();
            }

        } catch (Exception e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        } finally {
            // Сохраним рейтинги при завершении работы
            saveRatings();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Сервер завершает работу. Сохраняем рейтинг...");
            saveRatings();
        }));
    }


    public static void loadRatings() {
        File file = new File(RATINGS_FILE);
        if (!file.exists()) {
            // если файла нет — пропускаем
            return;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    playerRatings.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки рейтингов: " + e.getMessage());
        }
    }


    public static void saveRatings() {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RATINGS_FILE))) {
            for (HashMap.Entry<String, Integer> entry : playerRatings.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Ошибка сохранения рейтингов: " + e.getMessage());
        }
    }

    public static void updateRating(String playerName, int points) {
        playerRatings.put(playerName, playerRatings.getOrDefault(playerName, 0) + points);
        saveRatings();
    }

    public static int getRating(String playerName) {
        return playerRatings.getOrDefault(playerName, 0);
    }



    public static ArrayList<ClientHandler> getClients() {
        return clients;
    }

    public static Game getGame() {
        return game;
    }

    public static void setGame(Game newGame) {
        game = newGame;
    }

    public static boolean isGameStarted() {
        return gameStarted;
    }

    public static void setGameStarted(boolean started) {
        gameStarted = started;
    }
}