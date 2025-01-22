package com.example.gametest.view

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.gametest.databinding.SudokuGameLayoutBinding
import com.example.gametest.game.Cell
import com.example.gametest.viewmodel.PlaySudokuViewModel

/* TODO: Setup start window, loading/launch screen, and win screen
*        Setup board generation for new game
*        Add numbers to buttons showing how many still need to be filled
*        Optional: setup timer + scoreboard */

class MainActivity : ComponentActivity(), SudokuBoardView.OnTouchListener {

    private lateinit var binding: SudokuGameLayoutBinding
    private lateinit var sudokuBoardView: SudokuBoardView
    private lateinit var numberButtons: List<Button>
    private val viewModel: PlaySudokuViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        title = "Sudoku"

        binding = SudokuGameLayoutBinding.inflate(layoutInflater)
        sudokuBoardView = binding.sudokuBoardView
        setContentView(sudokuBoardView.rootView)

        sudokuBoardView.registerListener(this)

        viewModel.sudokuGame.selectedCellLiveData.observe(this) { updateSelectedCellUI(it) }
        viewModel.sudokuGame.updatedCellLiveData.observe(this) { updateCellsUI(it) }
        viewModel.sudokuGame.cellsLiveData.observe(this) { updateGridUI(it) }
        viewModel.sudokuGame.isTakingNotesLiveData.observe(this) { updateNoteTakingUI(it) }
        viewModel.sudokuGame.highlightLiveData.observe(this) { updateHighlightUI(it) }

        binding.notesButton.setOnClickListener { viewModel.sudokuGame.changeNoteState() }
        binding.buttonX.setOnClickListener { viewModel.sudokuGame.delete() }
        binding.undoButton.setOnClickListener { viewModel.sudokuGame.undo() }
        binding.restartButton.setOnClickListener { viewModel.sudokuGame.restart() }

        numberButtons = listOf(binding.button1, binding.button2, binding.button3, binding.button4, binding.button5, binding.button6,
            binding.button7, binding.button8, binding.button9)

        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.sudokuGame.handleInput(index + 1)
            }
        }
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
            binding.notesButton.background.colorFilter = BlendModeColorFilter(0, BlendMode.MULTIPLY)
        } else {
            binding.notesButton.background.clearColorFilter()
        }
    }

    private fun updateHighlightUI(isHighlighting: Boolean) {
        sudokuBoardView.updateHighlightingUI(isHighlighting)
    }

    override fun onCellTouched(cell: Pair<Int, Int>) {
        viewModel.sudokuGame.updateSelectedCell(cell)
    }

}