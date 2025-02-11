package org.example.model;

import org.example.enums.Box;
import org.example.utils.Coord;

import java.io.Serializable;

public class Bomb implements Serializable {
    private Matrix bombMap;
    private int totalBombs;

    public Bomb(int totalBombs) {
        this.totalBombs = totalBombs;
        FixBombsCount();
    }

    public void Start() {
        bombMap = new Matrix(Box.ZERO);
        for (int i = 0; i < totalBombs; i++) {
            PlaceBomb();
        }
    }

    public int GetTotalBombs() {
        return totalBombs;
    }

    public Box Get(Coord coord) {
        return bombMap.Get(coord);
    }

    private void FixBombsCount() {
        int maxBombs = Ranges.GetSize().x * Ranges.GetSize().y / 2;
        if (totalBombs > maxBombs) {
            totalBombs = maxBombs;
        }
    }

    private void PlaceBomb() {
        while (true) {
            Coord rCoord = Ranges.GetRandomCoord();
            if (bombMap.Get(rCoord) == Box.BOMB) continue;
            bombMap.Set(rCoord, Box.BOMB);
            IncNumbersAroundBomb(rCoord);
            break;
        }
    }

    private void IncNumbersAroundBomb(Coord coord) {
        Ranges.GetCoordsAround(coord).stream().filter((around) ->
                (Box.BOMB != bombMap.Get(around))).forEachOrdered((around) -> {
            bombMap.Set(around, bombMap.Get(around).GetNextNumberBox());
        });
    }
}

