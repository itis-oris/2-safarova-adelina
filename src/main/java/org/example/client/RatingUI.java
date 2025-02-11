package org.example.client;

import org.example.model.Ranges;
import org.example.server.MineSweeperServer;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class RatingUI extends JFrame {
    private DefaultListModel<String> ratingList;
    private JList<String> listRating;

    public RatingUI() {
        setTitle("Рейтинг игроков");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        ratingList = new DefaultListModel<>();
        listRating = new JList<>(ratingList);
        listRating.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(listRating), BorderLayout.CENTER);

        updateRatingUI(); // Заполняем список

        setVisible(true);
    }

    public void updateRatingUI() {
        ratingList.clear();  // Очистка перед обновлением
        MineSweeperServer.loadRatings(); // Обновление рейтинга из файла

        for (HashMap.Entry<String, Integer> entry : MineSweeperServer.playerRatings.entrySet()) {
            String playerEntry = entry.getKey() + ": " + entry.getValue();
            ratingList.addElement(playerEntry);
        }
    }
}



