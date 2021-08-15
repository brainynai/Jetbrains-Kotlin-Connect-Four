package connectfour

import java.lang.IllegalArgumentException

class Board (val player1: String, val player2: String, val rows: Int, val cols: Int, val numGames: Int) {

    val gameBoard = List<List<Cell>>(rows) { List(cols) { Cell() } }
    var spacesAvailable = rows * cols
    var lastPiece = Pair(-1, -1)
    val scoreList = mutableListOf<Int>(0, 0)
    var gameNum = 0


    override fun toString(): String {
        return "$player1 VS $player2\n$rows X $cols board\n" +
                if (numGames == 1) "Single game" else "Total $numGames games"
    }

    fun drawBoard() {
        for (colNum in 1..cols) {
            print(" $colNum")
        }
        println("")

        for (rowInd in gameBoard.indices) {
            for (colInd in gameBoard[rowInd].indices) {
                print("║${gameBoard[rowInd][colInd].strVal()}")
            }
            println("║")
        }

        print("╚")
        for (col in 1 until cols) {
            print("═╩")
        }
        println("═╝")

    }

    fun getMove(turnCount: Int): Int {
        val inputFormat = Regex("\\d+")

        while (true) {
            print(if (turnCount % 2 == 0) player1 else player2)
            println("'s turn:")
            val resp = readLine()!!
            if (resp == "end") return -1 //Game ended/over

            if (!inputFormat.matches(resp)) {
                println("Incorrect column number")
                continue
            } else {
                val colNum = resp.toInt()
                if (colNum !in 1..cols) {
                    println("The column number is out of range (1 - $cols)")
                    continue
                } else {
                    return colNum
                }
            }
        }

    }

    fun playInto(colNum: Int, playerNum: Int): Boolean {
        for (rowInd in gameBoard.indices.reversed()) {
            if (gameBoard[rowInd][colNum - 1].isEmpty()) {
                gameBoard[rowInd][colNum - 1].setOwner(playerNum)
                spacesAvailable -= 1
                lastPiece = Pair(rowInd, colNum - 1)
                return true
            }
        }
        println("Column $colNum is full")
        return false
    }

    fun checkForWinOrDraw(turnCount: Int): Boolean {
        var matchCount = 1

        //Check vertical (down)
        if (rows - lastPiece.first >= 4) {
            for (i in 1..3) {
                if (gameBoard[lastPiece.first + i][lastPiece.second].equals(gameBoard[lastPiece.first][lastPiece.second])) {
                    matchCount++
                } else {
                    matchCount = 1
                    break
                }
            }

            if (matchCount == 4) {
                gameNum++
                this.printWinMessage(turnCount)
                return true
            }

        }
        //Check horizontal
        val leftCols = gameBoard[lastPiece.first].indices.filter { it < lastPiece.second && lastPiece.second - it < 4 }
        val rightCols = gameBoard[lastPiece.first].indices.filter { it > lastPiece.second && it - lastPiece.second < 4 }

        for (col in leftCols.sortedDescending()) {
            if (gameBoard[lastPiece.first][col].equals(gameBoard[lastPiece.first][lastPiece.second])) {
                matchCount++
            } else {
                break
            }
        }
        for (col in rightCols.sorted()){
            if (gameBoard[lastPiece.first][col].equals(gameBoard[lastPiece.first][lastPiece.second])) {
                matchCount++
            } else {
                break
            }
        }

        if (matchCount >= 4) {
            gameNum++
            this.printWinMessage(turnCount)
            return true
        } else {
            matchCount = 1
        }

        //Check diagonal /
        val lowerRows = gameBoard.indices.filter { it < lastPiece.first && lastPiece.first - it < 4 }
        val upperRows = gameBoard.indices.filter { it > lastPiece.first && it - lastPiece.first < 4 }

        for ((row, col) in lowerRows.sortedDescending().zip(rightCols.sorted())) {
            if (gameBoard[row][col].equals(gameBoard[lastPiece.first][lastPiece.second])) {
                matchCount++
            } else {
                break
            }
        }

        for ((row, col) in upperRows.sorted().zip(leftCols.sortedDescending())) {
            if (gameBoard[row][col].equals(gameBoard[lastPiece.first][lastPiece.second])) {
                matchCount++
            } else {
                break
            }
        }

        if (matchCount >= 4) {
            gameNum++
            this.printWinMessage(turnCount)
            return true
        } else {
            matchCount = 1
        }

        //Check diagonal \
        for ((row, col) in lowerRows.sortedDescending().zip(leftCols.sortedDescending())) {
            if (gameBoard[row][col].equals(gameBoard[lastPiece.first][lastPiece.second])) {
                matchCount++
            } else {
                break
            }
        }

        for ((row, col) in upperRows.sorted().zip(rightCols.sorted())) {
            if (gameBoard[row][col].equals(gameBoard[lastPiece.first][lastPiece.second])) {
                matchCount++
            } else {
                break
            }
        }

        if (matchCount >= 4) {
            gameNum++
            this.printWinMessage(turnCount)
            return true
        } else {
            matchCount = 1
        }

        if (spacesAvailable <= 0) {
            this.drawBoard()
            gameNum++
            updateScore(turnCount, wasDraw = true)
            println("It is a draw")
            if (numGames > 1) {
                println("Score")
                println("$player1: ${scoreList[0]} $player2: ${scoreList[1]}")
            }

            return true
        }

        return false
    }

    fun printWinMessage(turnCount: Int) {
        this.drawBoard()
        print("Player ")
        print(if (turnCount % 2 == 0) player1 else player2)
        println(" won")

        updateScore(turnCount)

        if (numGames > 1) {
            println("Score")
            println("$player1: ${scoreList[0]} $player2: ${scoreList[1]}")
        }

    }

    fun updateScore(turnCount: Int, wasDraw: Boolean = false) {
        if (wasDraw) {
            scoreList.replaceAll { it + 1 }
        } else {
            scoreList[turnCount % 2] += 2
        }
    }

    fun resetBoard(){
        gameBoard.forEach { thisList -> thisList.forEach { it.clear() }}
        lastPiece = Pair(-1, -1)
        spacesAvailable = rows * cols
    }

    companion object {
        fun getDims(): Pair<Int, Int>{
            var validInput = false
            val inFormat = Regex("^\\s*(\\d+)\\s*x\\s*(\\d+)\\s*\$", RegexOption.IGNORE_CASE)

            do {
                println("Set the board dimensions (Rows x Columns)\n" +
                        "Press Enter for default (6 x 7)")
                val userInput = readLine()!!
                val result = inFormat.matchEntire(userInput)

                if (result == null) {
                    if (userInput.isBlank()) {
                        return Pair(6, 7)
                    }
                    println("Invalid input")
                    continue
                } else {
                    val (rIn, cIn) = result.destructured.toList().map { it.toInt() }

                    if (rIn !in 5..9) {
                        println("Board rows should be from 5 to 9")
                    } else if (cIn !in 5..9) {
                        println("Board columns should be from 5 to 9")
                    } else {
                        return Pair(rIn, cIn)
                    }
                }
            } while (!validInput)

            return Pair(6,7)
        }

        fun getNumGames(): Int {
            val inputFormat = Regex("\\d+")

            while(true) {
                println(
                    "Do you want to play single or multiple games?\n" +
                            "For a single game, input 1 or press Enter\n" +
                            "Input a number of games:"
                )
                val resp = readLine()!!
                if (resp.isBlank()) {
                    return 1
                } else if(inputFormat.matches(resp)) {
                    if (resp.toInt() > 0) return resp.toInt() else println("Invalid input")
                } else {
                    println("Invalid input")
                }
            }
        }
    }
}

class Cell {
    var owner: Boolean? = null

    fun strVal(): String {
        return when (owner) {
            null -> " "
            true -> "ο"
            else -> "*"
        }
    }

    fun setOwner(playerNum: Int) {
        if (playerNum !in 0..1) throw IllegalArgumentException()

        owner = playerNum == 0
    }

    fun isEmpty(): Boolean {
        return owner == null
    }

    fun equals(other: Cell): Boolean {
        return this.owner?.equals(other.owner) ?: false
    }

    fun clear() {
        owner = null
    }
}

fun main() {
    println("Connect Four")
    println("First player's name:")
    val player1Name = readLine()!!
    println("Second player's name:")
    val player2Name = readLine()!!
    val (numRows, numCols) = Board.getDims()
    val numGames = Board.getNumGames()

    val myBoard = Board(player1Name, player2Name, numRows, numCols, numGames)
    println(myBoard.toString())

    var turnCount = 0

    for (gameNum in 0 until numGames) {
        turnCount = gameNum % 2
        if (numGames > 1) println("Game #${gameNum + 1}")

        while (true) {
            myBoard.drawBoard()

            do {
                val selectedCol = myBoard.getMove(turnCount)
                if (selectedCol < 0) {
                    println("Game over!")
                    return
                }

                val moveSuccess = myBoard.playInto(selectedCol, turnCount % 2)
            } while (!moveSuccess)

            if (myBoard.checkForWinOrDraw(turnCount)) {
                myBoard.resetBoard()
                break
            }

            turnCount++
        }
    }
    println("Game over!")



}