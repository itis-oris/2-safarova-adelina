package org.example.client;

import org.example.enums.Box;
import org.example.enums.GameState;
import org.example.model.Game;
import org.example.model.Ranges;
import org.example.server.ClientHandler;
import org.example.server.MineSweeperServer;
import org.example.utils.Coord;
import org.example.utils.Protocol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class GameUI extends JFrame {
    private static Game game;
    private final int IMAGE_SIZE = 50;
    private Client client;

    private JButton jButton1;
    private JDialog jDialog1;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JTextField jTextField1;
    private JTextField jTextField2;
    private JTextField jTextField3;
    private Timer flagAnimationTimer;
    private int flagFrame = 0;
    private boolean isServerFull = false;


    public GameUI() {
        InitDialog();
        jDialog1.pack();
        jDialog1.setLocationRelativeTo(null);
        jDialog1.setVisible(true);

        jDialog1.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );
        setResizable(true);


    }


    private void StartFlagAnimation() {
        flagAnimationTimer = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flagFrame = (flagFrame + 1) % Box.flagImages.length;
                jPanel1.repaint();  // Перерисовываем панель
            }
        });
        flagAnimationTimer.start();
    }

    public void InitDialog() {
        jDialog1 = new JDialog((Dialog) null);
        jPanel3 = new JPanel();
        jTextField1 = new JTextField();
        jLabel3 = new JLabel();
        jTextField2 = new JTextField();
        jLabel4 = new JLabel();
        jLabel5 = new JLabel();
        jTextField3 = new JTextField(); // Поле для ввода имени
        jLabel6 = new JLabel("Имя игрока:");
        jPanel4 = new JPanel();
        jLabel2 = new JLabel();
        jButton1 = new JButton();
        jPanel1 = new JPanel() {
            @Override
            public String toString() {
                return "$classname{}";
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);


                for (Coord coord : Ranges.GetAllCoords()) {
                    Image img;
                    if (game.GetBox(coord) == Box.FLAGED) {
                        img = Box.flagImages[flagFrame];  // Используем текущий кадр флага
                    } else {
                        img = (Image) game.GetBox(coord).image;
                    }

                    g.drawImage(img, coord.x * IMAGE_SIZE, coord.y * IMAGE_SIZE, this);
                }

            }
        };
        jPanel2 = new JPanel();
        jLabel1 = new JLabel();
        jTextField1.setHorizontalAlignment(JTextField.CENTER);
        jTextField1.setText("localhost");
        jLabel3.setText(":");

        jTextField2.setHorizontalAlignment(JTextField.CENTER);
        jTextField2.setText("1777");
        jLabel4.setText("Адрес");
        jLabel5.setText("Порт");
        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, 131, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel3)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(44, 44, 44)
                                        .addComponent(jLabel4)
                                        .addGap(83, 83, 83)
                                        .addComponent(jLabel5))
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel6)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jTextField3, GroupLayout.PREFERRED_SIZE, 131, GroupLayout.PREFERRED_SIZE)))
                        .addGap(30, 30, 30))
        );
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel4)
                                .addComponent(jLabel5))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel3)
                                .addComponent(jTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel6)
                                .addComponent(jTextField3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(34, Short.MAX_VALUE))
        );

        jLabel2.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel2.setText("Подключение к серверу");
        jLabel2.setToolTipText("");
        GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING,
                        jPanel4Layout.createSequentialGroup()
                                .addContainerGap(31, Short.MAX_VALUE)
                                .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 196, GroupLayout.PREFERRED_SIZE)
                                .addGap(31, 31, 31))
        );
        jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 14, Short.MAX_VALUE)
                        .addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE))
        );
        jButton1.setText("Подключиться");
        jButton1.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        GroupLayout jDialog1Layout = new GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(jDialog1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jDialog1Layout.createSequentialGroup()
                        .addContainerGap(46, Short.MAX_VALUE)
                        .addGroup(jDialog1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(45, 45, 45))
                .addGroup(jDialog1Layout.createSequentialGroup()
                        .addGap(116, 116, 116)
                        .addComponent(jButton1)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jDialog1Layout.setVerticalGroup(jDialog1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jDialog1Layout.createSequentialGroup()
                        .addContainerGap(46, Short.MAX_VALUE)
                        .addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jButton1)
                        .addGap(31, 31, 31))
        );

    }

    private void initComponents() {
        StartFlagAnimation(); // Запуск анимации флага
        setMaximumSize(new Dimension(800, 910));
        setMinimumSize(new Dimension(450, 560));
        setPreferredSize(new Dimension((Ranges.GetSize().x * IMAGE_SIZE) + 20, (Ranges.GetSize().y * IMAGE_SIZE) + 110));
        setResizable(false);
        setSize(new Dimension(0, 0));

        jPanel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                int x = event.getX() / IMAGE_SIZE;
                int y = event.getY() / IMAGE_SIZE;
                Coord coord = new Coord(x, y);
                if (game.GetState() == GameState.PLAYED) {
                    if (event.getButton() == MouseEvent.BUTTON1) {
                        if (client.IsConnected()) {
                            client.PressButton("Left", coord);
                            game.PressLeftButton(coord);

                        }
                    }
                    if (event.getButton() == MouseEvent.BUTTON3) {
                        if (client.IsConnected()) {
                            client.PressButton("Right", coord);
                            game.PressRightButton(coord);

                        }
                    }

                }
                jLabel1.setText(GetMessage(client.IsConnected(), game));
                jLabel1.repaint();


                if (event.getButton() == MouseEvent.BUTTON2) {
                    if (game.GetState() == GameState.READY) {
                        if (client.IsConnected()) {
                            game = client.GetGame();
                            game.StartCoop();
                            jLabel1.setText(GetMessage(client.IsConnected(), game));
                            jPanel1.repaint();
                        }
                    }
                }
            }
        });


        jPanel1.setMaximumSize(new Dimension(800, 800));
        jPanel1.setMinimumSize(new Dimension(450, 450));
        jPanel1.setName("panel");
        jPanel1.setPreferredSize(new Dimension(Ranges.GetSize().x * IMAGE_SIZE, Ranges.GetSize().y * IMAGE_SIZE));
        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGap(0, 800, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGap(0, 800, Short.MAX_VALUE)
        );
        jLabel1.setFont(new Font("Trebuchet MS", 0, 18));

        jLabel1.setText("Hello World Saper");

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, 800, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(32, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(null);
    }


    private void
    jButton1ActionPerformed(ActionEvent evt) {
        String playerName = jTextField3.getText().trim();
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(jDialog1, "Введите имя!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return; // Прекращаем выполнение метода
        }

        client = new Client(jTextField1.getText(), Integer.parseInt(jTextField2.getText()), playerName);
        client.SetLabel(jLabel1);
        client.SetPanel(jPanel1);
        client.ClientStart();
        if (client.IsConnected()) {
            jDialog1.dispose();
            if (client != null) {
                game = client.GetGame();
                if (game != null) { // Проверяем, что игра успешно получена
                    game.StartCoop();
                    client.start();
                    SetImages();
                    initComponents();
                    setVisible(true);
                    client.SetPanel(jPanel1);
                    client.SetLabel(jLabel1);

                    this.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            client.StopConnection();
                            System.exit(0);
                        }
                    });
                    setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Не удалось получить игру от сервера.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }

        } else {
            jLabel2.setText("Ошибка подключения");
        }
    }


    public static String GetMessage(boolean isConnected, Game game) {
        if (isConnected) {
            switch (game.GetState()) {
                case PLAYED:
                    return "Игра идёт...";
                case BOMBED:
                    return "Игра окончена";
                case WINNER:
                    return "Игра окончена. Ничья";
                case READY:
                    return "Новая игра готова. Нажмите СКМ.";
                case WAITING:
                    return "Ожидаем второго игрока...";
                default:
                    return "Hello World";
            }
        } else {
            return "Потеряно соединение с сервером";
        }
    }

    private Image GetImage(String name) {
        String filename = "/Users/pro/IdeaProjects/MinesweeperHelper/src/main/resources/assets/" + name + ".png";
        System.out.println(filename);
        ImageIcon icon = new ImageIcon(filename);
        return icon.getImage().getScaledInstance(55, 55, Image.SCALE_SMOOTH);
    }

    public JPanel GetPanel() {
        return jPanel1;
    }

    private void SetImages() {
        for (Box box : Box.values()) {
            box.image = GetImage(box.name().toLowerCase());
        }
        Box.flagImages = new Image[]{
                GetImage("flaged"),
                GetImage("flaged1"),

        };
    }
}