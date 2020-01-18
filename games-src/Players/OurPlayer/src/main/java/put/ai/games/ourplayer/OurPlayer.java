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

    final int depth = 1;

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
            return x;
        }

        static public GameTree.HeuristicValue max(GameTree.HeuristicValue x, GameTree.HeuristicValue y) {
            if (x.value > y.value) {
                return x;
            }
            return y;
        }


        static public GameTree.HeuristicValue AlphaBeta(GameTree gameTree, int depth, GameTree.HeuristicValue alpha, GameTree.HeuristicValue beta, Type type) {

            gameTree.createChildren();  //stworz mozliwe plansze po tej planszy
//            System.out.println("DZIECI ZROBIONE");
            if ( !gameTree.hasChildren() || depth == 0) {   //jesli jest na samym dole grafu lub glebokosc jest rowna 0
//                System.out.println("BRAK DZIECI, KTOS NIE PORUCHAL");

                return gameTree.getValue();    //zwroc wartosc heurystyki ( wartosc + ruch )
            }
//            System.out.println("GRATULUJEMY ZOSTANIA OJCEM I MATKA");
            ArrayList<GameTree> children = gameTree.getChildren();


            //MAX
            if (type.equals(Type.MAX)) {
                for (GameTree child : children) {   //dla kazdego dziecka
                    GameTree.HeuristicValue valueOfChildren = AlphaBeta(child,depth -1,alpha,beta,Type.MIN);
                    alpha = max(valueOfChildren, alpha);
                    if (alpha.value >= beta.value) return beta;  //odciecie
                }
                return alpha;
            } else {
                for (GameTree child : children) {   //dla kazdego dziecka
                    GameTree.HeuristicValue valueOfChildren = AlphaBeta(child,depth -1,alpha,beta,Type.MAX);
                    beta = min(valueOfChildren, beta);
                    if (alpha.value >= beta.value) return alpha; //odciecie
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
        private HeuristicValue value;

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
            List <Move> possibleMoves = board.getMovesFor(color);   //stworz mozliwe ruchy
            //System.out.println("mozliwe ruchy odjebane, jest ich:" + possibleMoves.size());
            int i = 0;
            for (Move move : possibleMoves) {   //dla kazdego ruchu
                board.doMove(move); //zrob ruch
                //System.out.println(i + ". Ruch odjebany");
                children.add(new GameTree(board, getOpponent(color), move));  //stworzone dziecko
                //System.out.println(i + ". Dzieciak odjebany");
                board.undoMove(move);   //cofnij ruch
                //System.out.println(i + ". Ruch cofniety");
                i++;
            }
        }

        public boolean hasChildren() {
            return children.size() != 0;
        }

        //nasza heurystyka kurwa
        private HeuristicValue getValue() {
            value.value = 0.0f;
            float combo = 0.0f;

//            for (int i = 0; i < board.getSize(); i++) {
//                for (int j = 0; j < board.getSize() ; j++) {
//
//                    // plusiki za zdobyte nasze pkt
//                    if (board.getState(i,j) == board.getState(i,j+1) && board.getState(i,j).equals(color)) {
//                        value.value += combo + addToValue;
//                        combo += addToCombo;
//                        System.out.println("DOBRZE SZMATO!");
//
//                    } else {
//                        combo = 0.0f;
//                    }
//
//                    if (board.getState(j,i) == board.getState(j+1,i) && board.getState(j,i).equals(color)) {
//                        value.value += combo + addToValue;
//                        combo += addToCombo;
//                        System.out.println("DOBRZE!!");
//
//                    } else {
//                        combo = 0.0f;
//                    }
//
////                    //jak przeciwnik zdobywa pkt to kara kurwa
////                    combo = 0;
////                    if (board.getState(i,j) == board.getState(i,j+1) && board.getState(i,j).equals(getOpponent(color))) {
////                        value.value = value.value - (combo + removeFromValue);
////                        combo += removeFromCombo;
////                        System.out.println("KARA!");
////
////                    } else {
////                        combo = 0.0f;
////                    }
////
////                    if (board.getState(j,i) == board.getState(j+1,i) && board.getState(j,i).equals(getOpponent(color))) {
////                        value.value = value.value - (combo + removeFromValue);
////                        combo += removeFromCombo;
////                        System.out.println("KARA!");
////
////                    } else {
////                        combo = 0.0f;
////                    }
//                }
//            }
            for(int i = 0; i < board.getSize(); i++) {
                for (int j = 0; j < board.getSize(); j++) {
                    if (board.getState(i,j) == board.getState(i,j+1)) { //sa 2
                        value.value = addToValue;
                        if (board.getState(i,j) == board.getState(i,j+2)) {
                            value.value = addToValue + addToCombo;
                            if (board.getState(i,j) == board.getState(i,j+3)) {
                                value.value = addToValue + addToCombo * 5;
                            }
                        }
                    }
                    if (board.getState(i,j) == board.getState(i+1,j)) { //sa 2
                        value.value = addToValue;
                        if (board.getState(i,j) == board.getState(i+2,j)) {
                            value.value = addToValue + addToCombo;
                            if (board.getState(i,j) == board.getState(i+3,j)) {
                                value.value = addToValue + addToCombo * 5;
                            }
                        }
                    }


                }
            }


            return value;
        }
    }

    @Override
    public Move nextMove(Board board) {

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

//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
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
