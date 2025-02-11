package org.example.client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartPanel {
    public StartPanel() {

        JFrame mainFrame = new JFrame("Стартовое окно");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(300, 150);
        mainFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        JButton clientButton = new JButton("Войти в игру");
        JButton ratingButton = new JButton("Перейти к рейтингу");


        clientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GameUI gameUI = new GameUI();
            }
        });

        ratingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RatingUI ratingUI = new RatingUI();
            }
        });


        panel.add(clientButton);
        panel.add(ratingButton);

        mainFrame.add(panel);
        mainFrame.setVisible(true);
    }
}

