package com.example.gametest.game

import androidx.lifecycle.MutableLiveData

class SudokuGame {
    var selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    var cellsLiveData = MutableLiveData<Array<Array<Cell>>>()
    var updatedCellLiveData = MutableLiveData<Cell>()
    var isTakingNotesLiveData = MutableLiveData<Boolean>()
    var isWinningLiveData = MutableLiveData<Boolean>()
    var isInit = false

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
        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(startingGrid)
        isTakingNotesLiveData.postValue(isTakingNotes)
    }

    fun initBoard(diff: String) {
        cleanBoard()
        solvedGrid = copyGrid(setGrid())
        setDifficulty(diff)
        isInit = true
        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(startingGrid)
        isTakingNotesLiveData.postValue(isTakingNotes)
    }

    private fun setDifficulty(diff: String) {
        when (diff) {
            "Easy" -> numRemoval = 30
            "Medium" -> numRemoval = 50
            "Hard" -> numRemoval = 60
        }
        gen.removeKDigits(numRemoval)
        startingGrid = copyGrid(gen.getGrid())
        grid = copyGrid(startingGrid)
    }

    private fun cleanBoard() {
        gen.clearGrid()
        selectedRow = -1
        selectedCol = -1
        highlightedNum = -1
        history.clear()
        isTakingNotes = false
    }

    fun handleInput(number: Int) {
        if (selectedRow == -1 || selectedCol == -1) {
            if (highlightedNum != number) {
                highlightedNum = number
                highlightNumbers()
                cellsLiveData.postValue(grid)
            } else {
                highlightedNum = -1
                highlightNumbers()
                cellsLiveData.postValue(grid)
            }
        } else {
            val cell = getCell(selectedRow, selectedCol)
            if (cell.isStarting) return
            updateNum(cell, number)
            setConflictingNotes(cell.row, cell.col, cell.value, true)
            cellsLiveData.postValue(grid)
            checkWin()
        }
    }

    fun updateSelectedCell(cell: Pair<Int, Int>) {
        if (grid[cell.first][cell.second].isStarting) return
        if (highlightedNum > 0) {
            val gridCell = getCell(cell.first, cell.second)
            updateNum(gridCell, highlightedNum)
            setConflictingNotes(cell.first, cell.second, gridCell.value, true)
            highlightNumbers()
            cellsLiveData.postValue(grid)
            checkWin()
        } else if (selectedRow == cell.first && selectedCol == cell.second) {
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
        val gridCell = getCell(undoCell.row, undoCell.col)
        if (gridCell.cellHistory.size == 0) return
        val action = undoCell.cellHistory.last[0]
        val type = undoCell.cellHistory.last[1]

        if (type == 0) {
            setConflictingNotes(gridCell.row, gridCell.col, gridCell.value, false)
            gridCell.value = gridCell.updateCellUndoHistory()
            highlightNumbers()
            cellsLiveData.postValue(grid)
        } else if (action == 0) {
            gridCell.notes.remove(gridCell.updateUndoNote())
            highlightNumbers()
            cellsLiveData.postValue(grid)
        } else if (action == 1) {
            gridCell.notes.add(gridCell.updateUndoNote())
            highlightNumbers()
            cellsLiveData.postValue(grid)
        }

    }

    fun restart() {
        grid = copyGrid(startingGrid)
        history.clear()
        cellsLiveData.postValue(grid)
    }

    private fun updateNum(cell: Cell, number: Int) {
        if (isTakingNotes) {
            handleNotes(cell, number)
        } else {
            cell.value = number
            if (!cell.checkCellHistory(number)) {
                cell.cellHistory.add(arrayOf(0, 0, number))
                history.add(Cell.copyCell(cell))
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

    private fun setConflictingNotes(row: Int, col: Int, number: Int, bool: Boolean) {
        for (i in 0..8) {
            for (j in 0..8) {
                if (grid[i][j].notes.contains(number) && grid[i][j].value == 0) {
                    if (grid[i][j].row == row || grid[i][j].col == col) {
                        grid[i][j].hideConflicting[number] = bool
                    } else if (grid[i][j].row / 3 == row / 3 && grid[i][j].col / 3 == col / 3) {
                        grid[i][j].hideConflicting[number] = bool
                    }
                }
            }
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

    private fun highlightNumbers() {
        for (i in 0..8) {
            for (j in 0..8) {
                if (grid[i][j].value == highlightedNum) {
                    grid[i][j].highlight = true
                } else if (grid[i][j].value == 0 && grid[i][j].notes.contains(highlightedNum) && !grid[i][j].hideConflicting[highlightedNum]) {
                    grid[i][j].highlight = true
                } else {
                    grid[i][j].highlight = false
                }
            }
        }
    }

    private fun checkWin(): Boolean {
        for (i in 0..8) {
            for (j in 0..8) {
                if (grid[i][j].value == 0) {
                    return false
                } else if (grid[i][j].value != solvedGrid[i][j].value) {
                    return false
                }
            }
        }
        isWinningLiveData.postValue(true)
        return true
    }

    private fun getCell(row: Int, col: Int): Cell {
        return grid[row][col]
    }
}