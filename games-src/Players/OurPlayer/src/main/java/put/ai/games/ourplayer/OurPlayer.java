/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.ai.games.ourplayer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import put.ai.games.game.Board;
import put.ai.games.game.Move;
import put.ai.games.game.Player;

public class OurPlayer extends Player {

    final int depth = 2;

    private static class Constants {

    static public final String firstNameOfFirstAuthor = "Jakub";
    static public final String surnameOfFirstAuthor = "Pietrzak";
    static public final String indexOfFirstAuthor = "136612";
    static public final String firstNameOfSecondAuthor = "Marceli";
    static public final String surnameOfSecondAuthor = "Jerzynski";
    static public final String indexOfSecondAuthor = "136725";

    }

    private static class Logic {

        public enum Type {
            MIN,
            MAX;
        }

        static public GameTree.HeuristicValue min(GameTree.HeuristicValue x, GameTree.HeuristicValue y) {
            if (x.value > y.value) {
                return y;
            }
            //System.out.println("Mamy nowa alphe!");
            return x;
        }

        static public GameTree.HeuristicValue max(GameTree.HeuristicValue x, GameTree.HeuristicValue y) {
            if (x.value >= y.value) {
                //System.out.println("Mamy nowa bete!");
                return x;
            }
            return y;
        }

        //public static Move move;
        static public GameTree.HeuristicValue AlphaBeta(GameTree gameTree, int depth, GameTree.HeuristicValue alpha, GameTree.HeuristicValue beta, Type type) {

            if ( depth == 0) {   //jesli glebokosc == 0
                return gameTree.getValue();    //zwroc wartosc heurystyki ( wartosc + ruch, ktory doprowadzil do tej heurystyki )
            }

            gameTree.createChildren();  //stworz dzieci
            if (!gameTree.hasChildren()) {      //jesli nie ma dzieci
                return gameTree.getValue(); //zwroc wartosc heurystyki ( wartosc + ruch, ktory doprowadzil do tej heurystyki )
            }
            ArrayList<GameTree> children = gameTree.getChildren();  //zapisz dzieci

            //MAX
            if (type.equals(Type.MAX)) {
                for (GameTree child : children) {   //dla kazdego dziecka
                    GameTree.HeuristicValue valueOfChildren = AlphaBeta(child,depth -1,alpha,beta,Type.MIN);
                    //alpha = max(valueOfChildren, alpha);
                    if (valueOfChildren.value >= alpha.value) {
                        alpha.value = valueOfChildren.value;
                        alpha.move = child.value.move;
                    }
//                    if (alpha.value >= beta.value)  {
//                        //System.out.println("Odciecie");
//                        return beta;
//                    }  //odciecie
                }
                return alpha;
                //MIN
            } else {
                for (GameTree child : children) {   //dla kazdego dziecka
                    GameTree.HeuristicValue valueOfChildren = AlphaBeta(child,depth -1,alpha,beta,Type.MAX);
                    //beta = min(valueOfChildren, beta);
                    if (valueOfChildren.value <= beta.value) {
                        //move = child.value.move;
                        beta.value = valueOfChildren.value;
                        beta.move = child.value.move;
                    }
//                    if (alpha.value >= beta.value) {
//                        //System.out.println("Odciecie");
//                        return alpha;
//                    } //odciecie
                }
                return beta;
            }

        }
    }

    public static class GameTree {

        static public class HeuristicValue {
            public float value;
            public Move move;
        }

        private final float addToValue = 1.0f;
        private final float addToCombo = 15.0f;
        private final float removeFromValue = 100.0f;
        private final float removeFromCombo = 5.0f;

        private Board board;    //kazde drzewo ma korzen
        private ArrayList<GameTree> children;   //kazde drzewo ma dzieci
        private Color color;
        public HeuristicValue value;

        public GameTree() { //tego nie uzyje pewnie nigdy
            children = new ArrayList<>();
        }

        public GameTree(Board board, Color color) { //tworzy rozgrywke aktualna
            children = new ArrayList<>();
            this.board = board;
            this.color = color;
        }

        //move ktory stworzyl ta tablice
        public GameTree(Board board, Color color, Move move) {
            children = new ArrayList<>();
            this.board = board;
            this.color = color;
            this.value = new HeuristicValue();
            this.value.move = move;
        }

        public ArrayList<GameTree> getChildren() {
            return children;
        }

        public void createChildren() {   //have sex
            List <Move> possibleMoves = board.getMovesFor(color);   //stworz mozliwe ruchy dla dzieci ( dziecko jest tworzone przez ruch innego przeciwnika
            for (Move move : possibleMoves) {   //dla kazdego ruchu
                board.doMove(move); //zrob ruch
                children.add(new GameTree(board, getOpponent(color), move));  //stworzone dziecko
                board.undoMove(move);   //cofnij ruch
            }
        }

        public boolean hasChildren() {
            return children.size() != 0;
        }

        private float valueOfCenter( Board board, Color color) {
            float result = 0.0f;
            for(int k = 1; k < board.getSize(); k=k+3) {
                for( int l = 1; l < board.getSize(); l=l+3) {
                    if (board.getState(l, k) == getOpponent(color)) {
                        result += 5.0f;    //na srodku
                        //System.out.println("NA SRODKU");
                    }
                }
            }

            return result;
        }

        private float valueOfComboRows(Board board, Color color) {
            float result = 0.0f;
            for (int i = 0; i < board.getSize(); i++) {
                for (int j = 0; j < board.getSize(); j++) {
                    if (board.getState(i, j) == color
                            && board.getState(i, j) == board.getState(i, j + 1)
                            && board.getState(i, j) == board.getState(i, j + 2)) { //mamy 3 pod rząd
                        result += 100.0f;
                        System.out.println("3 pod rzad!");
                        if (board.getState(i, j) == board.getState(i, j + 3)) {//4 pod rząd
                            result += 1000.0f;
                            System.out.println("4 pod rzad");
                            if (board.getState(i, j) == board.getState(i, j + 4)) { //5 pod rząd
                                result += 100000.0f;
                                System.out.println("5 pod rzad");
                            }
                        }
                    }
                }
            }
            return result;
        }

        private float valueOfComboColumns(Board board, Color color) {
            float result = 0.0f;
            for (int i = 0; i < board.getSize(); i++) {
                for(int j = 0; j < board.getSize(); j++) {
                    if (board.getState(j, i) == color
                            && board.getState(j, i) == board.getState(j + 1, i)
                            && board.getState(j, i) == board.getState(j + 2, i)) { //mamy 3 pod rząd
                        value.value += 100.0f;
                        System.out.println("3 pod rzad!");
                        if (board.getState(j, i) == board.getState(j + 3, i)) {//4 pod rząd
                            value.value += 1000.0f;
                            System.out.println("4 pod rzad");
                            if (board.getState(j, i) == board.getState(j + 4, i)) { //5 pod rząd
                                value.value += 100000.0f;
                                System.out.println("5 pod rzad");
                            }
                        }
                    }
                }
            }
            return result;
        }
        //nasza heurystyka kurwa
        private HeuristicValue getValue() {

            value.value = 0.0f;
            value.value += valueOfCenter(board, getOpponent(color));

            value.value +=valueOfComboRows(board,getOpponent(color));
            value.value +=valueOfComboColumns(board,getOpponent(color));

            value.value -=valueOfComboRows(board,color);
            value.value -=valueOfComboRows(board,color);

            if (value.value == 5.0f) {
                System.out.println("Value: " + value.value);
            }
            return value;
        }
    }

    @Override
    public Move nextMove(Board board) {
        System.out.println("NEXT MOVE ! ------------------------------------------------------------------");
        Color ourColor = getColor();    //nasz kolor (jestesmy pierwsi czy drudzy )
        //Color theirColor = getOpponent(ourColor);   //kolor przeciwnika
        List<Move> possibleMoves = board.getMovesFor(ourColor);
        Logic.Type ourType = Logic.Type.MIN;
        if (ourColor.equals(Color.PLAYER1)) ourType = Logic.Type.MAX;

        GameTree.HeuristicValue alpha = new GameTree.HeuristicValue();
        alpha.value = Float.NEGATIVE_INFINITY;
        if (Math.max(alpha.value, 3.0f) == 3.0f)
        alpha.move = possibleMoves.get(possibleMoves.size() - 1);

        GameTree.HeuristicValue beta = new GameTree.HeuristicValue();
        beta.value = Float.POSITIVE_INFINITY;
        beta.move = possibleMoves.get(possibleMoves.size() - 1);

        GameTree currentGameTree = new GameTree(board, ourColor);   //obecne drzewko
        System.out.println("Type: " + ourType);
        GameTree.HeuristicValue result = Logic.AlphaBeta(currentGameTree,
                depth,
                alpha,
                beta,
                ourType);


        for(int k = 1; k < board.getSize(); k=k+3) {
            for( int l = 1; l < board.getSize(); l=l+3) {
                System.out.print(board.getState(l, k) + " ");
            }
            System.out.println();
        }
        return result.move;
    }


    @Override
    public String getName() {

        return String.format("%s %s %s %s %s %s",
                Constants.firstNameOfFirstAuthor,
                Constants.surnameOfFirstAuthor,
                Constants.indexOfFirstAuthor,
                Constants.firstNameOfSecondAuthor,
                Constants.surnameOfSecondAuthor,
                Constants.indexOfSecondAuthor);
    }
}
