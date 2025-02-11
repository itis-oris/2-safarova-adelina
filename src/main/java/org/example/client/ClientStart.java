package org.example.client;

import javax.swing.*;

public class ClientStart {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(()->{

            StartPanel cp = new StartPanel();
        });

    }
}