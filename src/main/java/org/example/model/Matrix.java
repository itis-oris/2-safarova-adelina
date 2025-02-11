package org.example.model;

import org.example.enums.Box;
import org.example.utils.Coord;

import java.io.Serializable;

class Matrix implements Serializable {
    private Box[][] matrix;

    public Matrix(Box defaultBox) {
        matrix = new Box[Ranges.GetSize().x][Ranges.GetSize().y];
        for (Coord coord : Ranges.GetAllCoords()) {
            matrix[coord.x][coord.y] = defaultBox;
        }
    }

    public Box Get(Coord coord) {
        if (Ranges.InRange(coord)) {
            return matrix[coord.x][coord.y];
        } else {
            return null;
        }
    }

    public void Set(Coord coord, Box box) {
        if (Ranges.InRange(coord)) {
            matrix[coord.x][coord.y] = box;
        }
    }
}
