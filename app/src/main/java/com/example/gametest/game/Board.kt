package com.example.gametest.game

class Board(val size: Int, var cells: List<Cell>) {
    fun getCell(row: Int, col: Int) = cells[row * size + col]
}