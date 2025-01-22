package com.example.gametest.view

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.gametest.R
import com.example.gametest.databinding.StartMenuBinding
import com.example.gametest.databinding.SudokuGameLayoutBinding
import com.example.gametest.game.Cell
import com.example.gametest.viewmodel.PlaySudokuViewModel

/* TODO: Setup win screen
*        Setup board generation for new game
*        Add back arrow button to main menu
*        Clear notes from conflicting squares when number is entered (in progress)
*           highlighting still messed up a bit and it's hiding all same notes, not just the conflicting notes
*        Optional: Add numbers to buttons showing how many still need to be filled
*        Optional: setup timer + scoreboard */

class MainActivity : ComponentActivity(), SudokuBoardView.OnTouchListener {

    private lateinit var gameBinding: SudokuGameLayoutBinding
    private lateinit var menuBinding: StartMenuBinding
    private lateinit var sudokuBoardView: SudokuBoardView
    private lateinit var numberButtons: List<Button>
    private val viewModel: PlaySudokuViewModel by viewModels()
    private var difficulty: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        title = "Sudoku"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        menuBinding = StartMenuBinding.inflate(layoutInflater)
        setContentView(menuBinding.root)

        gameBinding = SudokuGameLayoutBinding.inflate(layoutInflater)
        sudokuBoardView = gameBinding.sudokuBoardView

        setupObservers()

        setupListeners()
    }

    private fun setupObservers() {
        viewModel.sudokuGame.selectedCellLiveData.observe(this) { updateSelectedCellUI(it) }
        viewModel.sudokuGame.updatedCellLiveData.observe(this) { updateCellsUI(it) }
        viewModel.sudokuGame.cellsLiveData.observe(this) { updateGridUI(it) }
        viewModel.sudokuGame.isTakingNotesLiveData.observe(this) { updateNoteTakingUI(it) }
        viewModel.sudokuGame.highlightLiveData.observe(this) { updateHighlightUI(it) }
    }

    private fun setupListeners() {
        sudokuBoardView.registerListener(this)

        menuBinding.newGame.setOnClickListener { changeScreen() }
        //menuBinding.resume.setOnClickListener { setContentView(sudokuBoardView.rootView) }

        gameBinding.notesButton.setOnClickListener { viewModel.sudokuGame.changeNoteState() }
        gameBinding.buttonX.setOnClickListener { viewModel.sudokuGame.delete() }
        gameBinding.undoButton.setOnClickListener { viewModel.sudokuGame.undo() }
        gameBinding.restartButton.setOnClickListener { viewModel.sudokuGame.restart() }

        numberButtons = listOf(gameBinding.button1, gameBinding.button2, gameBinding.button3, gameBinding.button4, gameBinding.button5, gameBinding.button6,
            gameBinding.button7, gameBinding.button8, gameBinding.button9)

        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.sudokuGame.handleInput(index + 1)
            }
        }

        menuBinding.difficultyButtons.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.radioButton -> {
                    difficulty = "Easy"
                }
                R.id.radioButton2 -> {
                    difficulty = "Medium"
                }
                R.id.radioButton3 -> {
                    difficulty = "Hard"
                }
            }
        }
    }

    private fun changeScreen() {
        viewModel.sudokuGame.setDifficulty(difficulty)
        setContentView(sudokuBoardView.rootView)
    }

    private fun updateCellsUI(cell: Cell) {
        sudokuBoardView.updateCellsUI(cell)
    }

    private fun updateSelectedCellUI(cell: Pair<Int, Int>) {
        sudokuBoardView.updateSelectedCellUI(cell)
    }

    private fun updateGridUI(grid: Array<Array<Cell>>) {
        sudokuBoardView.updateGridUI(grid)
    }

    private fun updateNoteTakingUI(isTakingNotes: Boolean) {
        if (isTakingNotes) {
            gameBinding.notesButton.background.colorFilter = BlendModeColorFilter(0, BlendMode.MULTIPLY)
        } else {
            gameBinding.notesButton.background.clearColorFilter()
        }
    }

    private fun updateHighlightUI(isHighlighting: Boolean) {
        sudokuBoardView.updateHighlightingUI(isHighlighting)
    }

    override fun onCellTouched(cell: Pair<Int, Int>) {
        viewModel.sudokuGame.updateSelectedCell(cell)
    }
}