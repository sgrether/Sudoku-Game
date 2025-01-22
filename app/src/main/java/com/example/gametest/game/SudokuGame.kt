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
    private var numRemoval = 0
    private var isTakingNotes = false
    private var highlightedNum = -1
    private var history = mutableListOf<Cell>()
    private val gen = SudokuGenerator()

    private var solvedGrid: Array<Array<Cell>> = Array(9) { Array(9) { Cell(0,0,0,false) } }
    private var startingGrid: Array<Array<Cell>> = Array(9) { Array(9) { Cell(0,0,0,false) } }
    private var grid: Array<Array<Cell>> = Array(9) { Array(9) { Cell(0,0,0,false) } }

    init {
        solvedGrid = setGrid()
        startingGrid = copyGrid(solvedGrid)
        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(startingGrid)
        isTakingNotesLiveData.postValue(isTakingNotes)
    }

    fun setDifficulty(diff: String) {
        when (diff) {
            "Easy" -> numRemoval = 30
            "Medium" -> numRemoval = 50
            "Hard" -> numRemoval = 70
        }
        gen.removeKDigits(numRemoval)
        startingGrid = gen.getGrid()
        grid = copyGrid(startingGrid)
        cellsLiveData.postValue(grid)
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
            handleNotes(cell, number)
        } else {
            cell.value = number
            hideConflictingNotes(number)
            if (!cell.checkCellHistory(number)) {
                cell.cellHistory.add(arrayOf(0, 0, number))
                history.add(Cell.copyCell(cell))
            }
        }
        cellsLiveData.postValue(grid)
    }

    fun updateSelectedCell(cell: Pair<Int, Int>) {
        if (grid[cell.first][cell.second].isStarting) return
        if (highlightedNum > 0) {
            val hCell = grid[cell.first][cell.second]
            if(isTakingNotes) {
                handleNotes(hCell, highlightedNum)
            } else {
                hCell.value = highlightedNum
                hideConflictingNotes(highlightedNum)
                if (!hCell.checkCellHistory(highlightedNum)) {
                    hCell.cellHistory.add(arrayOf(0, 0, highlightedNum))
                    history.add(Cell.copyCell(hCell))
                }
            }

            highlightNumbers(hCell.value, true)
            cellsLiveData.postValue(grid)
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
            unhideConflictingNotes(undoCell.value)
            highlightNumbers(cell.value, false)
        } else if (action == 0) {
            cell.notes.remove(cell.updateUndoNote())
            highlightNumbers(cell.value, false)
        } else if (action == 1) {
            cell.notes.add(cell.updateUndoNote())
            highlightNumbers(cell.value, true)
        }
        cellsLiveData.postValue(grid)
    }

    fun restart() {
        grid = copyGrid(startingGrid)
        history.clear()
        cellsLiveData.postValue(grid)
    }

    private fun hideConflictingNotes(number: Int) {
        for (i in 0..8) {
            for (j in 0..8) {
                if (grid[i][j].notes.contains(number) && grid[i][j].value == 0) {
                    grid[i][j].hideConflicting[number] = true
                }
            }
        }
    }

    private fun unhideConflictingNotes(number: Int) {
        for (i in 0..8) {
            for (j in 0..8) {
                if (grid[i][j].notes.contains(number)) {
                    grid[i][j].hideConflicting[number] = false
                }
            }
        }
    }

    private fun handleNotes(cell: Cell, number: Int) {
        if (cell.notes.contains(number)) {
            cell.notes.remove(number)
            cell.cellHistory.add(arrayOf(1,1,number))
            history.add(Cell.copyCell(cell))
        } else {
            cell.notes.add(number)
            cell.cellHistory.add(arrayOf(0,1,number))
            history.add(Cell.copyCell(cell))
        }
    }

    private fun setGrid(): Array<Array<Cell>> {
        gen.generate()
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
                } else if (grid[i][j].value == 0 && grid[i][j].notes.contains(highlightedNum)) {
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