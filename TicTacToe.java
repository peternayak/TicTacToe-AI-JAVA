import java.io.*;
import java.util.Scanner;
import java.util.Stack;

// 1. Move Class: Stores row, column, and player mark
class Move {
    private int row;
    private int col;
    private char player;

    public Move(int row, int col, char player) {
        this.row = row;
        this.col = col;
        this.player = player;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public char getPlayer() {
        return player;
    }
}

// 2. Board Class: 2D array board, Stack for undo/backtracking, win/draw checks
class Board {
    private char[][] grid;
    private Stack<Move> moveStack;

    public Board() {
        grid = new char[3][3];
        moveStack = new Stack<>();
        clear();
    }

    public void clear() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                grid[i][j] = '.';
            }
        }
        moveStack.clear();
    }

    public boolean isValidMove(int row, int col) {
        return row >= 0 && row < 3 && col >= 0 && col < 3 && grid[row][col] == '.';
    }

    public void makeMove(int row, int col, char p) {
        grid[row][col] = p;
        moveStack.push(new Move(row, col, p));
    }

    public Move undoMove() {
        if (moveStack.isEmpty()) {
            return null;
        }
        Move lastMove = moveStack.pop();
        grid[lastMove.getRow()][lastMove.getCol()] = '.';
        return lastMove;
    }

    public char checkWinner() {
        for (int i = 0; i < 3; i++) {
            if (grid[i][0] != '.' && grid[i][0] == grid[i][1] && grid[i][1] == grid[i][2]) {
                return grid[i][0];
            }
            if (grid[0][i] != '.' && grid[0][i] == grid[1][i] && grid[1][i] == grid[2][i]) {
                return grid[0][i];
            }
        }
        if (grid[0][0] != '.' && grid[0][0] == grid[1][1] && grid[1][1] == grid[2][2]) {
            return grid[0][0];
        }
        if (grid[0][2] != '.' && grid[0][2] == grid[1][1] && grid[1][1] == grid[2][0]) {
            return grid[0][2];
        }
        return '.';
    }

    public boolean isFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[i][j] == '.') {
                    return false;
                }
            }
        }
        return true;
    }

    public void display() {
        System.out.println();
        for (int i = 0; i < 3; i++) {
            System.out.println("  " + grid[i][0] + " | " + grid[i][1] + " | " + grid[i][2]);
            if (i < 2) {
                System.out.println(" ---+---+---");
            }
        }
        System.out.println();
    }

    public char getCell(int row, int col) {
        return grid[row][col];
    }

    public void setCell(int row, int col, char p) {
        grid[row][col] = p;
    }

    public Stack<Move> getMoveStack() {
        return moveStack;
    }
}

// 3. AIPlayer Class: Minimax algorithm with backtracking
class AIPlayer {
    private char aiMark;
    private char humanMark;

    public AIPlayer(char aiMark) {
        this.aiMark = aiMark;
        this.humanMark = (aiMark == 'X') ? 'O' : 'X';
    }

    public Move getBestMove(Board board) {
        int bestScore = Integer.MIN_VALUE;
        Move bestMove = null;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board.isValidMove(i, j)) {
                    board.makeMove(i, j, aiMark);
                    int score = minimax(board, 0, false);
                    board.undoMove(); // Backtrack

                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new Move(i, j, aiMark);
                    }
                }
            }
        }
        return bestMove;
    }

    private int minimax(Board board, int depth, boolean isMax) {
        char winner = board.checkWinner();
        if (winner == aiMark) {
            return 10 - depth;
        }
        if (winner == humanMark) {
            return depth - 10;
        }
        if (board.isFull()) {
            return 0;
        }

        if (isMax) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board.isValidMove(i, j)) {
                        board.makeMove(i, j, aiMark);
                        bestScore = Math.max(bestScore, minimax(board, depth + 1, false));
                        board.undoMove();
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board.isValidMove(i, j)) {
                        board.makeMove(i, j, humanMark);
                        bestScore = Math.min(bestScore, minimax(board, depth + 1, true));
                        board.undoMove();
                    }
                }
            }
            return bestScore;
        }
    }
}

// 4. GameStorage Class: Save and load state in game_state.txt
class GameStorage {
    private static final String FILE_NAME = "game_state.txt";

    public boolean fileExists() {
        return new File(FILE_NAME).exists();
    }

    public void saveGame(Board board, char turn) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            writer.println("TURN=" + turn);

            StringBuilder boardStr = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    boardStr.append(board.getCell(i, j));
                }
                boardStr.append("|");
            }
            writer.println("BOARD=" + boardStr.toString());

            StringBuilder movesStr = new StringBuilder();
            for (Move m : board.getMoveStack()) {
                if (movesStr.length() > 0) {
                    movesStr.append(";");
                }
                movesStr.append(m.getRow()).append(" ")
                        .append(m.getCol()).append(" ")
                        .append(m.getPlayer());
            }
            writer.println("MOVES=" + movesStr.toString());
            System.out.println("Game saved to " + FILE_NAME);
        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage());
        }
    }

    public char loadGame(Board board) {
        try (Scanner fileScanner = new Scanner(new File(FILE_NAME))) {
            board.clear();
            char turn = 'X';

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.startsWith("TURN=")) {
                    turn = line.substring(5).charAt(0);
                } else if (line.startsWith("BOARD=")) {
                    String[] rows = line.substring(6).split("\\|");
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            board.setCell(i, j, rows[i].charAt(j));
                        }
                    }
                } else if (line.startsWith("MOVES=") && line.length() > 6) {
                    String[] moves = line.substring(6).split(";");
                    for (String m : moves) {
                        String[] parts = m.split(" ");
                        int r = Integer.parseInt(parts[0]);
                        int c = Integer.parseInt(parts[1]);
                        char p = parts[2].charAt(0);
                        board.getMoveStack().push(new Move(r, c, p));
                    }
                }
            }
            System.out.println("Saved game loaded successfully.");
            return turn;
        } catch (Exception e) {
            System.out.println("Saved file missing or damaged. Starting a new game.");
            board.clear();
            return 'X';
        }
    }
}

// 5. TicTacToeGame Class: Main class with console menu and game loop
public class TicTacToeGame {
    private Board board;
    private AIPlayer ai;
    private GameStorage storage;
    private char humanMark;
    private Scanner scanner;

    public TicTacToeGame() {
        board = new Board();
        storage = new GameStorage();
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        TicTacToeGame game = new TicTacToeGame();
        game.play();
    }

    public void play() {
        showInstructions();

        char currentTurn = 'X';
        boolean loaded = false;

        if (storage.fileExists()) {
            System.out.print("Saved game found. Load it? (y/n): ");
            String choice = readInput();
            if (choice.equalsIgnoreCase("y")) {
                currentTurn = storage.loadGame(board);
                System.out.print("Which mark were you playing as? (X/O): ");
                humanMark = readInput().toUpperCase().startsWith("O") ? 'O' : 'X';
                loaded = true;
            }
        }

        if (!loaded) {
            System.out.print("Choose your mark (X or O) [X plays first]: ");
            String markInput = readInput().toUpperCase();
            humanMark = markInput.startsWith("O") ? 'O' : 'X';
        }

        char aiMark = (humanMark == 'X') ? 'O' : 'X';
        ai = new AIPlayer(aiMark);

        while (true) {
            board.display();

            char winner = board.checkWinner();
            if (winner != '.') {
                if (winner == humanMark) {
                    System.out.println("You (" + humanMark + ") win!");
                } else {
                    System.out.println("AI (" + aiMark + ") wins!");
                }
                break;
            }
            if (board.isFull()) {
                System.out.println("Game draw.");
                break;
            }

            if (currentTurn == humanMark) {
                System.out.print("Player " + humanMark + " - enter row and column (0-2), 'u' to undo, or 's' to save: ");
                String input = readInput();

                if (input.equalsIgnoreCase("u")) {
                    if (board.getMoveStack().size() >= 2) {
                        board.undoMove();
                        board.undoMove();
                        System.out.println("Last round undone.");
                    } else {
                        System.out.println("Nothing to undo.");
                    }
                    continue;
                }

                if (input.equalsIgnoreCase("s")) {
                    storage.saveGame(board, currentTurn);
                    continue;
                }

                String[] parts = input.split("\\s+");
                if (parts.length != 2) {
                    System.out.println("Invalid input. Enter two numbers (e.g., 1 1).");
                    continue;
                }

                try {
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);

                    if (!board.isValidMove(row, col)) {
                        System.out.println("Cell occupied or out of range. Choose another cell.");
                        continue;
                    }

                    board.makeMove(row, col, humanMark);
                    System.out.println("Move accepted. Board updated.");
                    currentTurn = aiMark;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter numeric row and column.");
                }
            } else {
                Move bestMove = ai.getBestMove(board);
                board.makeMove(bestMove.getRow(), bestMove.getCol(), aiMark);
                System.out.println("AI chooses row " + bestMove.getRow() + ", column " + bestMove.getCol() + ".");
                currentTurn = humanMark;
            }
        }
        scanner.close();
    }

    private String readInput() {
        return scanner.nextLine().trim();
    }

    private void showInstructions() {
        System.out.println("=== TIC-TAC-TOE WITH AI (DSA PROJECT) ===");
        System.out.println("Enter row and column from 0 to 2 separated by a space (e.g., 1 1 for center).");
        System.out.println("Commands during your turn: 'u' to undo last move, 's' to save game.\n");
    }
}