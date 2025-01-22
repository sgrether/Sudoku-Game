package com.example.gametest.game

class Cell(
    var row: Int,
    var col: Int,
    var value: Int,
    var isStarting: Boolean,
    var notes: MutableSet<Int> = mutableSetOf(),
    var cellHistory: ArrayList<Array<Int>> = ArrayList(),
    var highlight: Boolean = false,
    var hideConflicting: Array<Boolean> = Array<Boolean>(9) { false }
    ) {
    //cellHistory: 0=add, 1=delete; 0=not note, 1=note; value

    fun equals(cell: Cell): Boolean {
        return this.row == cell.row && this.col == cell.col
    }

    fun createCopy(): Cell {
        return Cell(this.row, this.col, this.value, this.isStarting)
    }

    fun updateCellUndoHistory(): Int {
        this.cellHistory.removeLast()
        if (this.cellHistory.size > 0) {
            var i = this.cellHistory.size - 1
            do {
                if (this.cellHistory[i][1] == 0) {
                    return this.cellHistory.last[2]
                } else {
                    i--
                }
            } while (i >= 0)
        } else {
            return 0
        }
        return 0
    }

    fun checkCellHistory(num: Int): Boolean {
        if (this.cellHistory.size == 0) return false
        if (this.cellHistory.size > 0) {
            var i = this.cellHistory.size - 1
            do {
                if (this.cellHistory[i][1] == 0 && this.cellHistory[i][2] == num) {
                    return true
                } else {
                    i--
                }
            } while (i >= 0)
        } else {
            return false
        }
        return false
    }

    fun updateUndoNote(): Int {
        val num = this.cellHistory.last[2]
        this.cellHistory.removeLast()
        return num
    }

    companion object {
        fun copyCell(cell: Cell): Cell {
            return Cell(
                cell.row,
                cell.col,
                cell.value,
                cell.isStarting,
                cell.notes,
                cell.cellHistory
            )
        }
    }

}