/*
 * Copyright (C) 2017  Danijel Askov
 *
 * This file is part of Expectiminimax Backgammon.
 *
 * Expectiminimax Backgammon is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Expectiminimax Backgammon is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package askov.schoolprojects.ai.expectiminimaxbackgammon.gameplay.game;

import askov.schoolprojects.ai.expectiminimaxbackgammon.ExpectiminimaxBackgammon;
import askov.schoolprojects.ai.expectiminimaxbackgammon.gameplay.player.move.CheckerRelocation;
import askov.schoolprojects.ai.expectiminimaxbackgammon.gameplay.player.move.Move;
import askov.schoolprojects.ai.expectiminimaxbackgammon.gameplay.player.HumanPlayer;
import askov.schoolprojects.ai.expectiminimaxbackgammon.sprites.Board;
import askov.schoolprojects.ai.expectiminimaxbackgammon.sprites.Checker;
import askov.schoolprojects.ai.expectiminimaxbackgammon.sprites.CheckerStack;
import askov.schoolprojects.ai.expectiminimaxbackgammon.sprites.Die;
import askov.schoolprojects.ai.expectiminimaxbackgammon.sprites.CheckerStackIndexOutOfBoundsException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import askov.schoolprojects.ai.expectiminimaxbackgammon.util.Util;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;

/**
 * @author  Danijel Askov
 */
public class HumanVsHumanGame extends Game {

    private Checker pickedUpChecker;
    private Checker prevPickedUpChecker;
    private CheckerStack sourceCheckerStack;
    private CheckerStack prevSourceCheckerStack;
    private List<CheckerStack> destinationCheckerStacks = new ArrayList<>();
    private List<CheckerStack> prevDestinationCheckerStacks = new ArrayList<>();

    public HumanVsHumanGame(double boardWidth, double boardHeight) {
        super(boardWidth, boardHeight, new HumanPlayer(Checker.CheckerColor.WHITE), new HumanPlayer(Checker.CheckerColor.BLACK));

        for (Checker checker : checkers) {
            checker.setOnMouseEntered(mouseEvent -> {
                boolean checkerIsMovable = false;
                if (checker.getCheckerColor() == currentPlayer.getCheckerColor()) {
                    if (getBoard().getBar(checker.getCheckerColor()).isEmpty()) {
                        List<Move> possibleMovesForCurrentPlayer = currentPlayer.getPossibleMoves();
                        for (Move move : possibleMovesForCurrentPlayer) {
                            for (CheckerRelocation checkerRelocation : move.getCheckerRelocations()) {
                                if (checkerRelocation.getSourceCheckerStack().peekChecker() == checker) {
                                    checkerIsMovable = true;
                                    break;
                                }
                            }
                            if (checkerIsMovable) break;
                        }
                    } else checkerIsMovable = getBoard().getBar(checker.getCheckerColor()).peekChecker() == checker;
                }
                checker.setCursor(checkerIsMovable ? Cursor.HAND : Cursor.DEFAULT);
            });
            checker.setOnMouseClicked(new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {
                    if (pickedUpChecker == checker)
                        return;
                    if (checker.getCheckerColor() != currentPlayer.getCheckerColor())
                        return;
                    boolean foundOnTop = false;
                    for (int j = 0; j <= Board.NUM_POINTS + 1; j++) {
                        try {
                            CheckerStack checkerStack = board.getCheckerStack(currentPlayer.getCheckerColor(), j);
                            if (checkerStack.peekChecker() == checker) {
                                foundOnTop = true;
                                break;
                            }
                        } catch (CheckerStackIndexOutOfBoundsException ex) {
                            Logger.getLogger(HumanVsHumanGame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!foundOnTop)
                        return;
                    if (pickedUpChecker == null) {
                        List<Move> moves = currentPlayer.getPossibleMoves();
                        for (Move move : moves) {
                            List<CheckerRelocation> relocations = move.getCheckerRelocations();
                            if (relocations.size() > 0) {
                                CheckerRelocation firstRelocation = relocations.get(0);
                                if (firstRelocation.getChecker() == checker) {
                                    if (sourceCheckerStack == null) {
                                        sourceCheckerStack = firstRelocation.getSourceCheckerStack();
                                    }
                                    CheckerStack destinationCheckerStack = firstRelocation.getDestinationCheckerStack();
                                    destinationCheckerStack.animateSelected(true);
                                    destinationCheckerStacks.add(destinationCheckerStack);
                                    Checker topDestinationChecker = firstRelocation.getDestinationCheckerStack().peekChecker();
                                    if (topDestinationChecker != null) {
                                        topDestinationChecker.animateBlot(firstRelocation.removesBlot());
                                    }
                                }
                            }
                        }
                        if (sourceCheckerStack != null) {
                            pickedUpChecker = checker;
                            checker.animateSelected(true);
                            if (prevPickedUpChecker != null) {
                                prevPickedUpChecker.animateSelected(false);
                            }
                            prevPickedUpChecker = null;
                            for (CheckerStack checkerStack : prevDestinationCheckerStacks) {
                                if (!destinationCheckerStacks.contains(checkerStack)) {
                                    checkerStack.animateSelected(false);
                                    Checker topChecker = checkerStack.peekChecker();
                                    if (topChecker != null)
                                        topChecker.animateBlot(false);
                                }
                            }
                            prevDestinationCheckerStacks.clear();
                            prevSourceCheckerStack = null;
                        } else {
                            pickedUpChecker = prevPickedUpChecker;
                            prevPickedUpChecker = null;
                            destinationCheckerStacks = prevDestinationCheckerStacks;
                            prevDestinationCheckerStacks = new ArrayList<>();
                            sourceCheckerStack = prevSourceCheckerStack;
                            prevSourceCheckerStack = null;
                        }
                    } else {
                        prevPickedUpChecker = pickedUpChecker;
                        prevDestinationCheckerStacks = destinationCheckerStacks;
                        prevSourceCheckerStack = sourceCheckerStack;
                        pickedUpChecker = null;
                        destinationCheckerStacks = new ArrayList<>();
                        sourceCheckerStack = null;
                        handle(event);
                    }
                }
            });

        }

        for (int i = 1; i <= Board.NUM_POINTS; i++) {
            try {
                CheckerStack checkerStack = board.getCheckerStack(Checker.CheckerColor.WHITE, i);
                checkerStack.setOnMouseEntered(mouseEvent -> {
                    boolean checkerStackIsPossibleDestination = false;
                    if (pickedUpChecker != null && destinationCheckerStacks.contains(checkerStack)) {
                        List<Move> possibleMovesForCurrentPlayer = currentPlayer.getPossibleMoves();
                        for (Move move : possibleMovesForCurrentPlayer) {
                            for (CheckerRelocation checkerRelocation : move.getCheckerRelocations()) {
                                if (checkerRelocation.getSourceCheckerStack().peekChecker() == pickedUpChecker && checkerRelocation.getDestinationCheckerStack() == checkerStack) {
                                    checkerStackIsPossibleDestination = true;
                                    break;
                                }
                            }
                            if (checkerStackIsPossibleDestination) break;
                        }
                    }
                    checkerStack.setCursor(checkerStackIsPossibleDestination ? Cursor.HAND : Cursor.DEFAULT);
                });
                checkerStack.setOnMouseClicked(new CheckerStackClickedHandler(checkerStack));
            } catch (CheckerStackIndexOutOfBoundsException ex) {
                Logger.getLogger(ExpectiminimaxBackgammon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        CheckerStack checkerStack;
        checkerStack = board.getHome(Checker.CheckerColor.WHITE);
        checkerStack.setOnMouseClicked(new CheckerStackClickedHandler(checkerStack));
        checkerStack = board.getHome(Checker.CheckerColor.BLACK);
        checkerStack.setOnMouseClicked(new CheckerStackClickedHandler(checkerStack));

        addAndShowDice(currentPlayer.getDice());
        board.update();
    }

    private void addAndShowDice(Die... dice) {
        int i = 0;
        for (Die die : dice) {
            die.setTranslateX((0.20 + i++ * 0.40) * board.getWidth() + Util.generateRandomDouble(0., 0.20 * board.getWidth()));
            die.setTranslateY(0.50 * board.getHeight() - die.getSize() / 2);
            die.setRotate(Util.generateRandomDouble(0., 360));
            board.getChildren().add(die);
            die.update();
            die.show();
        }
    }

    private class CheckerStackClickedHandler implements EventHandler<MouseEvent> {

        private final CheckerStack checkerStack;

        public CheckerStackClickedHandler(CheckerStack checkerStack) {
            this.checkerStack = checkerStack;
        }

        @Override
        public void handle(MouseEvent event) {
            if (pickedUpChecker != null && destinationCheckerStacks.contains(checkerStack)) {
                CheckerRelocation checkerRelocation = new CheckerRelocation(sourceCheckerStack, checkerStack, pickedUpChecker);
                boolean relocationFound = false;
                for (Move possibleMove : currentPlayer.getPossibleMoves()) {
                    for (CheckerRelocation cR : possibleMove.getCheckerRelocations()) {
                        if (cR.equals(checkerRelocation)) {
                            if (!relocationFound) {
                                Move move = new Move();
                                move.addCheckerRelocation(cR);
                                currentPlayer.make(move);
                                cR.getDie().hide();

                                pickedUpChecker.animateSelected(false);
                                for (CheckerStack cS : destinationCheckerStacks) {
                                    cS.animateSelected(false);
                                    Checker topChecker = cS.peekChecker();
                                    if (topChecker != null)
                                        topChecker.animateBlot(false);
                                }
                                board.update();
                                destinationCheckerStacks.clear();
                                pickedUpChecker = null;
                                sourceCheckerStack = null;
                                relocationFound = true;
                                break;
                            }
                        }
                        if (relocationFound) {
                            break;
                        }
                    }
                }
                if (relocationFound) {
                    try {
                        currentPlayer.generatePossibleMoves();
                    } catch (BoardNotSpecifiedException ex) {
                        Logger.getLogger(HumanVsHumanGame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            if (currentPlayer.getPossibleMoves().isEmpty()) {
                if (getWinner() != null) {
                    for (Checker checker : checkers)
                        checker.setOnMouseClicked(e -> {});
                    for (int i = 1; i <= Board.NUM_POINTS; i++) {
                        try {
                            board.getCheckerStack(Checker.CheckerColor.WHITE, i).setOnMouseClicked(e -> {});
                        } catch (CheckerStackIndexOutOfBoundsException ex) {
                            Logger.getLogger(ExpectiminimaxBackgammon.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    alertAndExit(Alert.AlertType.INFORMATION, "Expectiminimax Backgammon", "Game Over", getWinner() + " wins!");
                } else {
                    int numUsedDice = numUsedDiceForCurrentPlayer();
                    if (numUsedDice == 0) {
                        alert(Alert.AlertType.WARNING, "Expectiminimax Backgammon", "End of the Turn", "No possible moves for the given dice combination for the " + currentPlayer);
                    } else if (numUsedDice == 1) {
                        alert(Alert.AlertType.WARNING, "Expectiminimax Backgammon", "End of the Turn", "No more possible moves for the " + currentPlayer);
                    }

                    removeDice(currentPlayer.getDice());
                    for (Die die : currentPlayer.getDice()) {
                        die.setUsed(false);
                    }
                    currentPlayer.rollDice();
                    switchCurrentPlayer();
                    try {
                        currentPlayer.generatePossibleMoves();
                    } catch (BoardNotSpecifiedException ex) {
                        Logger.getLogger(HumanVsHumanGame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    addAndShowDice(currentPlayer.getDice());
                    if (currentPlayer.getPossibleMoves().isEmpty()) {
                        handle(event);
                    }
                }
            }
        }
    }
    
}