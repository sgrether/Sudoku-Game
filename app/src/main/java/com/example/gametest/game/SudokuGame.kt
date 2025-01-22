package com.example.gametest.game

import androidx.lifecycle.MutableLiveData

class SudokuGame {
    var selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    var cellsLiveData = MutableLiveData<Array<Array<Cell>>>()
    var updatedCellLiveData = MutableLiveData<Cell>()
    val isTakingNotesLiveData = MutableLiveData<Boolean>()
    val highlightLiveData = MutableLiveData<Boolean>()

    private var selectedRow = -1
    private var selectedCol = -1
    private var numRemoval = 20
    private var isTakingNotes = false
    private var highlightedNum = -1
    private var history = mutableListOf<Cell>()

    private var startingGrid: Array<Array<Cell>> = Array(9) { Array(9) { Cell(0,0,0,false) } }
    private var grid: Array<Array<Cell>> = Array(9) { Array(9) { Cell(0,0,0,false) } }

    init {
        startingGrid = setGrid(numRemoval)
        grid = copyGrid(startingGrid)
        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(grid)
        isTakingNotesLiveData.postValue(isTakingNotes)
    }

    fun handleInput(number: Int) {
        if (selectedRow == -1 || selectedCol == -1) {
            if (highlightedNum != number) {
                highlightedNum = number
                highlightNumbers(highlightedNum, true)
            } else {
                highlightedNum = -1
                highlightNumbers(highlightedNum, false)
            }
            return
        }
        val cell = getCell(selectedRow, selectedCol)
        if (cell.isStarting) return
        if (isTakingNotes) {
            if (cell.notes.contains(number)) {
                cell.notes.remove(number)
                cell.cellHistory.add(arrayOf(1,1,number))
                history.add(Cell.copyCell(cell))
            } else {
                cell.notes.add(number)
                cell.cellHistory.add(arrayOf(0,1,number))
                history.add(Cell.copyCell(cell))
            }
        } else {
            cell.value = number
            if (!cell.checkCellHistory(number)) {
                cell.cellHistory.add(arrayOf(0, 0, number))
                history.add(Cell.copyCell(cell))
            }
        }
        updatedCellLiveData.postValue(cell)
    }

    fun updateSelectedCell(cell: Pair<Int, Int>) {
        if (grid[cell.first][cell.second].isStarting) return
        if (highlightedNum > 0) {
            val hCell = grid[cell.first][cell.second]
            hCell.value = highlightedNum

            if (!hCell.checkCellHistory(highlightedNum)) {
                hCell.cellHistory.add(arrayOf(0, 0, highlightedNum))
                history.add(Cell.copyCell(hCell))
            }
            highlightNumbers(hCell.value, true)
            updatedCellLiveData.postValue(hCell)
            return
        }
        if (selectedRow == cell.first && selectedCol == cell.second) {
            selectedRow = -1
            selectedCol = -1
            selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        } else {
            selectedRow = cell.first
            selectedCol = cell.second
            selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        }
    }

    fun changeNoteState() {
        isTakingNotes = !isTakingNotes
        isTakingNotesLiveData.postValue(isTakingNotes)
    }

    fun delete() {
        if (selectedRow == -1 && selectedCol == -1) return
        val cell = getCell(selectedRow, selectedCol)
        history.add(Cell.copyCell(cell))
        grid[selectedRow][selectedCol].cellHistory.add(arrayOf(1,0,cell.value))
        cell.value = 0
        updatedCellLiveData.postValue(cell)
    }

    fun undo() {
        if (history.size == 0) return
        val undoCell = history.removeLast()
        val cell = getCell(undoCell.row, undoCell.col)
        if (cell.cellHistory.size == 0) return
        val action = undoCell.cellHistory.last[0]
        val type = undoCell.cellHistory.last[1]

        if (type == 0) {
            cell.value = cell.updateCellUndoHistory()
            highlightNumbers(cell.value, false)
        } else if (action == 0) {
            cell.notes.remove(cell.UpdateUndoNote())
        } else if (action == 1) {
            cell.notes.add(cell.UpdateUndoNote())
        }
        updatedCellLiveData.postValue(cell)
    }

    fun restart() {
        grid = copyGrid(startingGrid)
        history.clear()
        cellsLiveData.postValue(grid)
    }

    private fun setGrid(num: Int): Array<Array<Cell>> {
        val gen = SudokuGenerator()
        gen.generate(num)
        return gen.getGrid()
    }

    private fun copyGrid(sourceGrid: Array<Array<Cell>>): Array<Array<Cell>> {
        val newGrid = Array(9) { Array(9) { Cell(0,0,0,false) } }
        for (i in 0..8) {
            for (j in 0..8) {
                newGrid[i][j] = sourceGrid[i][j].createCopy()
            }
        }
        return newGrid
    }

    private fun highlightNumbers(num: Int, bool: Boolean) {
        for (i in 0..8) {
            for (j in 0..8) {
                if (grid[i][j].value == highlightedNum) {
                    grid[i][j].highlight = true
                } else {
                    grid[i][j].highlight = false
                }
            }
        }
        highlightLiveData.postValue(bool)
        cellsLiveData.postValue(grid)
    }

    private fun getCell(row: Int, col: Int): Cell {
        return grid[row][col]
    }
}