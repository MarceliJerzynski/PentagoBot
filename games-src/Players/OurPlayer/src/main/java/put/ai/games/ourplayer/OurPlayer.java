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
    public final static float valueOfCenterCross = 5.0f;
    public final static float valueOf2Combo = 20.0f;
    public final static float valueOf3Combo = 100.0f;
    public final static float valueOf4Combo = 1000.0f;
    public final static float valueOf5Combo = 100000.0f;

    private static class Constants {

    static public final String firstNameOfFirstAuthor = "Jakub";
    static public final String surnameOfFirstAuthor = "Pietrzak";
    static public final String indexOfFirstAuthor = "136612";
    static public final String firstNameOfSecondAuthor = "Marceli";
    static public final String surnameOfSecondAuthor = "Jerzynski";
    static public final String indexOfSecondAuthor = "136725";

    }

    private static class Logic {

        //MIN lub MAX
        public enum Type {
            MIN,
            MAX;
        }

        //Oblicza mina z floata i zwraca tego floata i Move'a
        static public GameTree.HeuristicValue min(GameTree.HeuristicValue x, GameTree.HeuristicValue y) {
            if (x.value <= y.value) {
                return x;
            }
            //System.out.println("Mamy nowa alphe!");
            return y;
        }

        //Oblicza maxa z floata i zwraca tego floata i Move'a
        static public GameTree.HeuristicValue max(GameTree.HeuristicValue x, GameTree.HeuristicValue y) {
            if (x.value >= y.value) {
                //System.out.println("Mamy nowa bete!");
                return x;
            }
            return y;
        }
        //ALPHA BETA ------------------------------------------------------------------------------------------------------
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
                    alpha = max(valueOfChildren, alpha);
                    if (alpha.value >= beta.value)  {
                        return beta;
                    }  //odciecie
                }
                return alpha;
                //MIN
            } else {
                for (GameTree child : children) {   //dla kazdego dziecka
                    GameTree.HeuristicValue valueOfChildren = AlphaBeta(child,depth -1,alpha,beta,Type.MAX);
                    beta = min(valueOfChildren, beta);
                    if (alpha.value >= beta.value) {
                        return alpha;
                    } //odciecie
                }
                return beta;
            }

        }
        //KONIEC ALPHY BETY --------------------------------------------------------------------------------------------------
    }

    //plansza + plansze ktore moga powstac z tej planszy
    public static class GameTree {

        //float + Move
        static public class HeuristicValue {
            public float value;
            public Move move;

            //Pusty konstruktor, po to zeby byl
            public HeuristicValue() {}
            //Konstruktor ktory przyjmuje value i Movea
            public HeuristicValue(float value, Move move) {
                this.value = value;
                this.move = move;
            }
        }

        //aktualna plansza
        private Board board;
        //plansze, ktora moga powstac z planszy powyzej
        private ArrayList<GameTree> children;
        //kolor ktory sprawil ze ta plansza powstala
        private Color color;
        //wartosc heurystyczna tej planszy ( float + Move, ktory ta plansze stworzyl )
        public HeuristicValue value;

        //Pusty konstruktor, po to zeby byl
        public GameTree() { //tego nie uzyje pewnie nigdy
            children = new ArrayList<>();
        }

        //Konstruktor z boardem i colorem ( wykorzystywany przy tworzeniu drzewka pustej planszy lub planszy ktorej move'a nie znamy)
        //(uzywany tylko raz w sumie, w nextMovie)
        public GameTree(Board board, Color color) { //tworzy rozgrywke aktualna
            children = new ArrayList<>();
            this.board = board;
            this.color = color;
        }

        //J.W, + Move ktory tą planszę stworzył
        public GameTree(Board board, Color color, Move move) {
            children = new ArrayList<>();
            this.board = board;
            this.color = color;
            this.value = new HeuristicValue();
            this.value.move = move;
        }

        //getter dzieci
        public ArrayList<GameTree> getChildren() {
            return children;
        }

        //Metoda tworzaca dzieci
        public void createChildren() {   //have sex
            List <Move> possibleMoves = board.getMovesFor(color);   //stworz mozliwe ruchy dla dzieci ( dziecko jest tworzone przez ruch innego przeciwnika
            for (Move move : possibleMoves) {   //dla kazdego ruchu
                board.doMove(move); //zrob ruch
                children.add(new GameTree(board, getOpponent(color), move));  //stworzone dziecko
                board.undoMove(move);   //cofnij ruch
            }
        }

        //Bool ktory zwraca czy dana plansza ma dzieci czy tez nie
        public boolean hasChildren() {
            return children.size() != 0;
        }

        //Dodaje pkt heurystyczne jesli na danej planszy na srodkach kwadratow player1 ma piona ( srodki sie nie obracaja wiec gitara)
        private float valueOfCenter( Board board, Color color) {
            float result = 0.0f;
            for(int k = 1; k < board.getSize(); k=k+3) {
                for( int l = 1; l < board.getSize(); l=l+3) {
                    if (board.getState(l, k) == getOpponent(color)) {
                        result += valueOfCenterCross;    //na srodku
                        //System.out.println("NA SRODKU");
                    }
                }
            }

            return result;
        }

        //Dodaje/Odejmuje pkt heurystyczne jesli na danej planszy gracz Color ma kilka pionow w rzedzie
        private float valueOfComboRows(Board board, Color color) {
            float result = 0.0f;
            for (int i = 0; i < board.getSize(); i++) {
                for (int j = 0; j < board.getSize(); j++) {
                    if (board.getState(i, j) == color
                            && board.getState(i, j) == board.getState(i, j + 1)
                            && board.getState(i, j) == board.getState(i, j + 2)) { //mamy 3 pod rząd
                        result += valueOf3Combo;
                        System.out.println("3 pod rzad!");
                        if (board.getState(i, j) == board.getState(i, j + 3)) {//4 pod rząd
                            result += valueOf4Combo;
                            System.out.println("4 pod rzad");
                            if (board.getState(i, j) == board.getState(i, j + 4)) { //5 pod rząd
                                result += valueOf5Combo;
                                System.out.println("5 pod rzad");
                            }
                        }
                    }
                }
            }
            return result;
        }

        //Dodaje/Odejmuje pkt heurystyczne jesli na danej planszy gracz Color ma kilka pionow w kolumnie
        private float valueOfComboColumns(Board board, Color color) {
            float result = 0.0f;
            for (int i = 0; i < board.getSize(); i++) {
                for(int j = 0; j < board.getSize(); j++) {
                    if (board.getState(j, i) == color
                            && board.getState(j, i) == board.getState(j + 1, i)
                            && board.getState(j, i) == board.getState(j + 2, i)) { //mamy 3 pod rząd
                        value.value += valueOf3Combo;
                        System.out.println("3 pod rzad!");
                        if (board.getState(j, i) == board.getState(j + 3, i)) {//4 pod rząd
                            value.value += valueOf4Combo;
                            System.out.println("4 pod rzad");
                            if (board.getState(j, i) == board.getState(j + 4, i)) { //5 pod rząd
                                value.value += valueOf5Combo;
                                System.out.println("5 pod rzad");
                            }
                        }
                    }
                }
            }
            return result;
        }

        //nasza heurystyka -----------------------------------------------------------------------------
        private HeuristicValue getValue() {
            //inicjalizacja zmiennej
            value.value = 0.0f;

            //jesli ruch sprawil, ze player1 jest na srodku kwadratu, daj 5pkt
            value.value += valueOfCenter(board, Color.PLAYER1);

            //jesli ruch sprawil, ze ma 3/4/5 w rzedzie, daj mu punkty
            value.value +=valueOfComboRows(board, Color.PLAYER1);
            value.value +=valueOfComboColumns(board, Color.PLAYER1);

            //jesli ruch sprawil, ze player2 ma 3/4/5 w rzedzie, zabierz mu punkty
            value.value -=valueOfComboRows(board,Color.PLAYER2);
            value.value -=valueOfComboRows(board,Color.PLAYER2);

            return value;
        }
        //koniec heurystyki ------------------------------------------------------------------------------
    }


    //Taki Main, metoda wykonywana przy kazdym nowym ruchu naszego zawodnika
    @Override
    public Move nextMove(Board board) {
        System.out.println("NEXT MOVE ! ------------------------------------------------------------------");
        //nasz kolor (jestesmy pierwsi czy drudzy )
        Color ourColor = getColor();

        //Lista mozliwych ruchow ( do stworzenia alphy i bety, pewnie mozna to pominac heh
        List<Move> possibleMoves = board.getMovesFor(ourColor);

        //Tworzymy ze gramy Mina lub Maxa w zaleznosci od koloru, da sie uproscic, ale po co
        Logic.Type ourType = Logic.Type.MIN;
        if (ourColor.equals(Color.PLAYER1)) ourType = Logic.Type.MAX;
        System.out.println("Type: " + ourType);

        //pierwsza alpha i beta, wartosci nigdy nieosiagalne
        GameTree.HeuristicValue alpha = new GameTree.HeuristicValue(Float.NEGATIVE_INFINITY, possibleMoves.get(possibleMoves.size() - 1));
        GameTree.HeuristicValue beta = new GameTree.HeuristicValue(Float.POSITIVE_INFINITY, possibleMoves.get(possibleMoves.size() - 1));

        //tworzenie obecnego drzewka gry
        GameTree currentGameTree = new GameTree(board, ourColor);   //obecne drzewko

        //Wywolanie metody AlphaBeta i zdobycie wyniku
        GameTree.HeuristicValue result = Logic.AlphaBeta(currentGameTree,
                depth,
                alpha,
                beta,
                ourType);

        //wypisanie srodkow kwadratow
        for(int k = 1; k < board.getSize(); k=k+3) {
            for( int l = 1; l < board.getSize(); l=l+3) {
                System.out.print(board.getState(l, k) + " ");
            }
            System.out.println();
        }

        //zwrocenie wyniku
        return result.move;
    }

    //Nasza nazwa, Kuba + index + Marceli + index (zmienne sa do gory)
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
