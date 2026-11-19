package com.example.game

import kotlin.random.Random

enum class ArenaGameType(val title: String) {
    TICTACTOE("Tic-Tac-Toe"),
    CONNECT4("Connect-4"),
    CYBER_2048("Cyber 2048")
}

data class TicTacToeState(
    val board: List<String> = List(9) { "" }, // "", "X" (User), "O" (AI)
    val currentTurn: String = "X",
    val winner: String? = null, // "X", "O", "DRAW", or null
    val winningLine: List<Int> = emptyList()
)

data class Connect4State(
    val rows: Int = 6,
    val cols: Int = 7,
    val board: List<Int> = List(42) { 0 }, // 0=empty, 1=User, 2=AI
    val currentTurn: Int = 1,
    val winner: Int? = null // 1, 2, 0 (DRAW), or null
)

data class Cyber2048State(
    val grid: List<Int> = List(16) { 0 },
    val score: Int = 0,
    val gameOver: Boolean = false
)

object GameEngine {

    // --- Tic-Tac-Toe ---
    private val tttLines = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )

    fun tttValidMoves(state: TicTacToeState): List<String> =
        state.board.indices.filter { state.board[it].isEmpty() }.map { it.toString() }

    fun tttApplyMove(state: TicTacToeState, index: Int): TicTacToeState {
        if (state.winner != null || index !in 0..8 || state.board[index].isNotEmpty()) return state
        val nextBoard = state.board.toMutableList()
        nextBoard[index] = state.currentTurn
        for (line in tttLines) {
            val a = nextBoard[line[0]]
            if (a.isNotEmpty() && a == nextBoard[line[1]] && a == nextBoard[line[2]]) {
                return state.copy(board = nextBoard, winner = a, winningLine = line)
            }
        }
        if (nextBoard.none { it.isEmpty() }) {
            return state.copy(board = nextBoard, winner = "DRAW")
        }
        return state.copy(
            board = nextBoard,
            currentTurn = if (state.currentTurn == "X") "O" else "X"
        )
    }

    fun tttDescribe(state: TicTacToeState): String {
        val cells = state.board.mapIndexed { idx, v -> if (v.isEmpty()) idx.toString() else v }
        return """
            Turn: ${state.currentTurn} (You are ${state.currentTurn})
            Grid (indices 0-8):
            ${cells[0]} | ${cells[1]} | ${cells[2]}
            ${cells[3]} | ${cells[4]} | ${cells[5]}
            ${cells[6]} | ${cells[7]} | ${cells[8]}
        """.trimIndent()
    }

    // --- Connect-4 ---
    fun c4ValidMoves(state: Connect4State): List<String> =
        (0 until state.cols).filter { col -> state.board[col] == 0 }.map { it.toString() }

    fun c4ApplyMove(state: Connect4State, col: Int): Connect4State {
        if (state.winner != null || col !in 0 until state.cols) return state
        var targetRow = -1
        for (r in state.rows - 1 downTo 0) {
            if (state.board[r * state.cols + col] == 0) {
                targetRow = r
                break
            }
        }
        if (targetRow == -1) return state
        val next = state.board.toMutableList()
        val player = state.currentTurn
        next[targetRow * state.cols + col] = player

        if (c4HasWon(next, state.rows, state.cols, player)) {
            return state.copy(board = next, winner = player)
        }
        if (next.none { it == 0 }) {
            return state.copy(board = next, winner = 0)
        }
        return state.copy(board = next, currentTurn = if (player == 1) 2 else 1)
    }

    private fun c4HasWon(board: List<Int>, rows: Int, cols: Int, p: Int): Boolean {
        val dirs = listOf(0 to 1, 1 to 0, 1 to 1, 1 to -1)
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (board[r * cols + c] != p) continue
                for ((dr, dc) in dirs) {
                    var count = 1
                    for (step in 1..3) {
                        val nr = r + dr * step
                        val nc = c + dc * step
                        if (nr in 0 until rows && nc in 0 until cols && board[nr * cols + nc] == p) {
                            count++
                        } else break
                    }
                    if (count == 4) return true
                }
            }
        }
        return false
    }

    fun c4Describe(state: Connect4State): String {
        val sb = StringBuilder("Connect-4 (6 rows x 7 cols, columns 0-6). You are player ${state.currentTurn}.\n")
        for (r in 0 until state.rows) {
            for (c in 0 until state.cols) {
                sb.append(state.board[r * state.cols + c]).append(' ')
            }
            sb.append('\n')
        }
        return sb.toString()
    }

    // --- Cyber 2048 ---
    fun new2048(): Cyber2048State {
        var state = Cyber2048State()
        state = spawnTile(state)
        state = spawnTile(state)
        return state
    }

    fun move2048(state: Cyber2048State, direction: String): Cyber2048State {
        if (state.gameOver) return state
        val grid = Array(4) { r -> IntArray(4) { c -> state.grid[r * 4 + c] } }
        var gained = 0
        var changed = false

        fun slideLine(line: IntArray): IntArray {
            val nonZero = line.filter { it != 0 }.toMutableList()
            val merged = mutableListOf<Int>()
            var i = 0
            while (i < nonZero.size) {
                if (i + 1 < nonZero.size && nonZero[i] == nonZero[i + 1]) {
                    val valMerged = nonZero[i] * 2
                    merged.add(valMerged)
                    gained += valMerged
                    i += 2
                } else {
                    merged.add(nonZero[i])
                    i += 1
                }
            }
            while (merged.size < 4) merged.add(0)
            return merged.toIntArray()
        }

        when (direction.uppercase()) {
            "LEFT" -> for (r in 0..3) {
                val next = slideLine(grid[r])
                if (!next.contentEquals(grid[r])) changed = true
                grid[r] = next
            }
            "RIGHT" -> for (r in 0..3) {
                val rev = grid[r].reversedArray()
                val next = slideLine(rev).reversedArray()
                if (!next.contentEquals(grid[r])) changed = true
                grid[r] = next
            }
            "UP" -> for (c in 0..3) {
                val col = IntArray(4) { r -> grid[r][c] }
                val next = slideLine(col)
                if (!next.contentEquals(col)) changed = true
                for (r in 0..3) grid[r][c] = next[r]
            }
            "DOWN" -> for (c in 0..3) {
                val col = IntArray(4) { r -> grid[3 - r][c] }
                val next = slideLine(col)
                if (!next.contentEquals(col)) changed = true
                for (r in 0..3) grid[3 - r][c] = next[r]
            }
        }

        if (!changed) return state
        val flat = (0..15).map { idx -> grid[idx / 4][idx % 4] }
        val nextState = spawnTile(state.copy(grid = flat, score = state.score + gained))
        return nextState.copy(gameOver = !canMove2048(nextState.grid))
    }

    fun valid2048Moves(state: Cyber2048State): List<String> {
        return listOf("UP", "DOWN", "LEFT", "RIGHT").filter { dir ->
            move2048(state, dir).grid != state.grid
        }
    }

    fun describe2048(state: Cyber2048State): String {
        val rows = (0..3).joinToString("\n") { r ->
            (0..3).joinToString("\t") { c -> state.grid[r * 4 + c].toString() }
        }
        return "Score: ${state.score}\n4x4 Grid:\n$rows"
    }

    private fun spawnTile(state: Cyber2048State): Cyber2048State {
        val emptyIndices = state.grid.indices.filter { state.grid[it] == 0 }
        if (emptyIndices.isEmpty()) return state
        val pick = emptyIndices[Random.nextInt(emptyIndices.size)]
        val next = state.grid.toMutableList()
        next[pick] = if (Random.nextFloat() < 0.9f) 2 else 4
        return state.copy(grid = next)
    }

    private fun canMove2048(grid: List<Int>): Boolean {
        if (grid.any { it == 0 }) return true
        for (r in 0..3) {
            for (c in 0..3) {
                val v = grid[r * 4 + c]
                if (r < 3 && grid[(r + 1) * 4 + c] == v) return true
                if (c < 3 && grid[r * 4 + (c + 1)] == v) return true
            }
        }
        return false
    }
}
