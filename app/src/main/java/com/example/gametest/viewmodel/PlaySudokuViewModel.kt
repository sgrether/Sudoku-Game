package com.example.gametest.viewmodel

import androidx.lifecycle.ViewModel
import com.example.gametest.game.SudokuGame

class PlaySudokuViewModel : ViewModel() {
    val sudokuGame = SudokuGame()
}