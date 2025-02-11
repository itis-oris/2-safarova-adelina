package org.example.model;

import org.example.enums.Box;
import org.example.utils.Coord;

import java.io.Serializable;

class Flag implements Serializable {
    private Matrix flagMap;
    private int countOfClosedBoxes;

    public void Start() {
        flagMap = new Matrix(Box.CLOSED);
        countOfClosedBoxes = Ranges.GetSize().x * Ranges.GetSize().y;
    }

    public Box Get(Coord coord) {
        return flagMap.Get(coord);
    }

    public void SetOpenedToBox(Coord coord) {
        flagMap.Set(coord, Box.OPENED);
        countOfClosedBoxes--;
    }

    public void ToggleFlagedToBox(Coord coord) {
        switch (flagMap.Get(coord)) {
            case FLAGED:
                SetClosedToBox(coord);
                break;
            case CLOSED:
                SetFlagedToBox(coord);
                break;
        }
    }

    public void SetFlagedToBox(Coord coord) {
        flagMap.Set(coord, Box.FLAGED);
    }

    public void SetClosedToBox(Coord coord) {
        flagMap.Set(coord, Box.CLOSED);
    }

    public int GetCountOfClosedBoxes() {
        return countOfClosedBoxes;
    }

    public void SetBombedToBox(Coord coord) {
        flagMap.Set(coord, Box.BOMBED);
    }

    public void SetOpenedToClosedBombBox(Coord coord) {
        if (flagMap.Get(coord) == Box.CLOSED) {
            flagMap.Set(coord, Box.OPENED);
        }
    }

    public void SetNoBombToFlagedSafeBox(Coord coord) {
        if (flagMap.Get(coord) == Box.FLAGED) {
            flagMap.Set(coord, Box.NOBOMB);
        }
    }

    public int GetCountOfFlagedBoxesAround(Coord coord) {
        int count = 0;
        for (Coord around : Ranges.GetCoordsAround(coord)) {
            if (flagMap.Get(around) == Box.FLAGED) {
                count++;
            }
        }
        return count;
    }
}
