package com.example.gametest.game

import kotlin.random.Random


class SudokuGenerator {

    private var grid: Array<Array<Cell>> = Array(9) { Array(9) { Cell(0,0,0,true) }}

    fun getGrid(): Array<Array<Cell>> {
        return grid
    }

    fun generate() {
        fillDiagonal()
        fillRemaining(0, 3)
    }

    private fun unUsedInBox(rowStart: Int, colStart: Int, num: Int): Boolean {
        for (i in 0..2) {
            for (j in 0..2) {
                if (grid[rowStart + i][colStart + j].value == num) {
                    return false
                }
            }
        }
        return true
    }

    private fun fillBox(row: Int, col: Int) {
        var num: Int
        for (i in 0..2) {
            for (j in 0..2) {
                do {
                    num = Random.nextInt(9) + 1
                } while (!unUsedInBox(row, col, num))
                grid[row + i][col + j].row = row + i
                grid[row + i][col + j].col = col + j
                grid[row + i][col + j].value = num
            }
        }
    }

    private fun unUsedInRow(i: Int, num: Int): Boolean {
        for (j in 0..8) {
            if (grid[i][j].value == num) {
                return false
            }
        }
        return true
    }

    private fun unUsedInCol(j: Int, num: Int): Boolean {
        for (i in 0..8) {
            if (grid[i][j].value == num) {
                return false
            }
        }
        return true
    }

    private fun checkIfSafe(i: Int, j: Int, num: Int): Boolean {
        return (unUsedInRow(i, num) && unUsedInCol(j, num) && unUsedInBox(i - i % 3, j - j % 3, num))
    }

    private fun fillDiagonal() {
        var i = 0
        while (i < 9) {
            fillBox(i, i)
            i += 3
        }
    }

    private fun fillRemaining(i: Int, j: Int): Boolean {
        var i = i
        var j = j
        if (j >= 9 && i < 8) {
            i += 1
            j = 0
        }
        if (i >= 9 && j >= 9) {
            return true
        }
        if (i < 3) {
            if (j < 3) {
                j = 3
            }
        } else if (i < 6) {
            if (j == (i / 3) * 3) {
                j += 3
            }
        } else {
            if (j == 6) {
                i += 1
                j = 0
                if (i >= 9) {
                    return true
                }
            }
        }

        for (num in 1..9) {
            if (checkIfSafe(i, j, num)) {
                grid[i][j].row = i
                grid[i][j].col = j
                grid[i][j].value = num
                if (fillRemaining(i, j + 1)) {
                    return true
                }
                grid[i][j].value = 0
            }
        }
        return false
    }

    fun removeKDigits(num: Int) {
        var k = num
        val rand = java.util.Random()
        while (k > 0) {
            val cellId = rand.nextInt(81)
            val i = cellId / 9
            val j = cellId % 9

            if (grid[i][j].value != 0) {
                grid[i][j].value = 0
                grid[i][j].isStarting = false
                k--
            }
        }
    }
}