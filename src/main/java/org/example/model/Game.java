package org.example.model;

import org.example.client.Client;
import org.example.enums.Box;
import org.example.enums.GameState;
import org.example.server.MineSweeperServer;
import org.example.utils.Coord;
import org.example.utils.Protocol;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Game implements Serializable {
    private Bomb bomb;
    private Flag flag;
    public static GameState gState;
    private String winner = null;

    public Game(int cols, int rows) {
        Ranges.SetSize(new Coord(cols, rows));


    }

    public Game(int cols, int rows, int bombs) {
        Ranges.SetSize(new Coord(cols, rows));
        bomb = new Bomb(bombs);
        flag = new Flag();
    }

    public Game() {
    }

    public void StartSolo() {
        bomb.Start();
        flag.Start();
        gState = GameState.PLAYED;
    }

    public void StartCoop() {
        gState = GameState.PLAYED;
    }

    public Bomb GetBomb() {
        return bomb;
    }

    public Flag GetFlag() {
        return flag;
    }

    public void SetState(GameState gState) {
        this.gState = gState;
    }

    public void SetBomb(Bomb bomb) {
        this.bomb = bomb;
    }

    public void SetFlag(Flag flag) {
        this.flag = flag;
    }

    public GameState GetState() {
        return gState;
    }

    public Box GetBox(Coord coord) {

        if (flag.Get(coord) == Box.OPENED) {
            return bomb.Get(coord);
        } else {
            return flag.Get(coord);
        }
    }

    public void PressLeftButton(Coord coord) {
        if (GameOver()) return;
        if (Client.canPlay) {
            OpenBox(coord);
            CheckWinner();
            System.out.println("После хода состояние игры: " + gState);

        }
        // flag.SetOpenedToBox(coord);
    }

    public void PressRightButton(Coord coord) {
        if (GameOver()) return;
        if (Client.canPlay) {
            flag.ToggleFlagedToBox(coord);
        }
    }

    public void CheckWinner() {
        if (gState == GameState.PLAYED) {
            if (flag.GetCountOfClosedBoxes() == bomb.GetTotalBombs()) {
                gState = GameState.WINNER;
            }
        }
    }

    public void SetWinner(String playerName) {
        if (playerName != null && !playerName.trim().isEmpty()) {
            this.winner = playerName;
        } else {
            this.winner = "Неизвестен"; // fallback, если имя пустое
        }
    }

    public String GetWinnerName() {
        return (winner != null && !winner.trim().isEmpty()) ? winner : "Неизвестен";
    }


    public void OpenBox(Coord coord) {
        switch (flag.Get(coord)) {
            case OPENED:
                SetOpenedToClosedBoxesAroundNumber(coord);
                return;
            case FLAGED:
                return;
            case CLOSED:
                switch (bomb.Get(coord)) {
                    case ZERO:
                        OpenBoxesAround(coord);
                        return;
                    case BOMB:
                        OpenBombs(coord);
                        return;
                    default:
                        flag.SetOpenedToBox(coord);
                        return;
                }
        }
    }

    private void OpenBombs(Coord coordBombed) {
        gState = GameState.BOMBED;
        flag.SetBombedToBox(coordBombed);
        for (Coord coord : Ranges.GetAllCoords()) {
            if (bomb.Get(coord) == Box.BOMB) {
                flag.SetOpenedToClosedBombBox(coord);
            } else {
                flag.SetNoBombToFlagedSafeBox(coord);
            }
        }
    }

    private void OpenBoxesAround(Coord coord) {
        flag.SetOpenedToBox(coord);
        for (Coord around : Ranges.GetCoordsAround(coord)) {
            OpenBox(around);
        }
    }

    private boolean GameOver() {
        if (gState == GameState.PLAYED) {
            return false;
        }
        System.out.println("Игра окончена");
        return true;
    }

    public void SetOpenedToClosedBoxesAroundNumber(Coord coord) {
        if (bomb.Get(coord) != Box.BOMB) {
            if (flag.GetCountOfFlagedBoxesAround(coord) == bomb.Get(coord).GetNumber()) {
                for (Coord around : Ranges.GetCoordsAround(coord)) {
                    if (flag.Get(around) == Box.CLOSED) {
                        OpenBox(around);
                    }
                }
            }
        }
    }
}


