package com.bbk.mrnommultiplayer;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Input.KeyEvent;
import com.badlogic.androidgames.framework.Input.TouchEvent;
import com.badlogic.androidgames.framework.Pixmap;
import com.badlogic.androidgames.framework.Screen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

public class GameScreenServer extends Screen {

    /******************** Network *************************/
    String serverIP;
    int serverPort = 2516;
    Handler handlerToThread;
    Handler handlerFromThread;
    ServerSocket serverSocket;
    PrintWriter printWriter;

    boolean disconnected;

    Thread serverThread;
    /******************** Network *************************/

    enum GameState {
        WaitingClient,
        Ready,
        Running,
        Paused,
        GameOver,
        ServerWon,
        ClientWon,
        ClientDisconnected,
        Error1,
        NoConnection,
        Error3,
        ServerExited,
        Error5,
        Error6,
        Error7,
        TimeOut
    }

    enum DigitsColor {
        White,
        Red,
        Blue,
        Green
    }

    GameState state = GameState.WaitingClient;
    WorldServer world;
    int oldScore = 0;
    int oldScoreClient = 0;
    String score = "0";
    String scoreClient = "0";
    boolean firstTouch = true;
    boolean pressedStartServer = false;
    boolean pressedStartClient = false;
    float lastUpdateTime = System.nanoTime() / 1000000000.0f;
    float lastReceivedFromClient = System.nanoTime() / 1000000000.0f;
    boolean messageSent = false;
    int remainingTime;
    int remainingTimeLast = 0;

    int touchedX = 0;
    int touchedY = 0;
    boolean moved;

    boolean pausedByMe = false;

    float firstGameOver;

    MrNomGame myGame;

    public GameScreenServer(Game game) {
        super(game);
        world = new WorldServer();
        world.init();
        myGame = (MrNomGame)game;

        /******************** Network *************************/
        handlerFromThread = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if (message.what == 0) {
                    if (state == GameState.Ready) {
                        pressedStartClient = true;
                        if (pressedStartServer) {
                            lastReceivedFromClient = System.nanoTime() / 1000000000.0f;
                            state = GameState.Running;
                            world.startTime = System.nanoTime();
                            world.lastBonusTime = System.nanoTime() / 1000000000.0f;

                            Message messageBack = handlerToThread.obtainMessage(0);
                            messageBack.sendToTarget();
                        }
                    }
                }
                if (message.what == 1) {
                    state = GameState.Paused;
                    world.timeElapsedSinceLastBonus = System.nanoTime() / 1000000000.0f - world.lastBonusTime;
                    pausedByMe = false;
                    //world.elapsedTime += System.nanoTime() - world.startTime;
                }
                if (message.what == 2) {
                    lastReceivedFromClient = System.nanoTime() / 1000000000.0f;
                    if (message.obj.equals("TurnLeft"))
                        world.snakeClient.turnLeft();

                    if (message.obj.equals("TurnRight"))
                        world.snakeClient.turnRight();
                }
                if (message.what == 3) {
                    lastReceivedFromClient = System.nanoTime() / 1000000000.0f;
                    state = GameState.Running;
                    world.startTime = System.nanoTime() - world.elapsedTime;
                    world.lastBonusTime = System.nanoTime() / 1000000000.0f - world.timeElapsedSinceLastBonus;
                }
                if (message.what == 4) {
                    state = GameState.Ready;
                    sendWinConditionToClient();
                    sendWorldStateToClient();
                }
                if (message.what == 5)
                    state = GameState.ClientDisconnected;
                if (message.what == 6) {
                    if (message.obj.equals("1"))
                        state = GameState.Error1;
                    if (message.obj.equals("2"))
                        state = GameState.NoConnection;
                    if (message.obj.equals("3"))
                        state = GameState.Error3;
                    if (message.obj.equals("4"))
                        state = GameState.ServerExited;
                    if (message.obj.equals("5"))
                        state = GameState.ClientDisconnected;
                }
                if (message.what == 7) {
                    myGame.showAd();
                    prepareNewGame();
                }
                if (message.what == 8) {
                    lastReceivedFromClient = System.nanoTime() / 1000000000.0f;
                    messageSent = false;
                }
            }
        };
        /******************** Network *************************/
    }

    @Override
    public void update(float deltaTime) {
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        List<KeyEvent> keyEvents = game.getInput().getKeyEvents();

        if ((System.nanoTime() / 1000000000.0f) - lastReceivedFromClient >= 10) {
            if (state == GameState.Running)
                state = GameState.TimeOut;
        }

        if (state == GameState.WaitingClient)
            updateWaitingClient(touchEvents, keyEvents);
        if (state == GameState.Ready)
            updateReady(touchEvents, keyEvents);
        if (state == GameState.Running)
            updateRunning(touchEvents, keyEvents, deltaTime);
        if (state == GameState.Paused)
            updatePaused(touchEvents);
        if (state == GameState.GameOver)
            updateGameOver(touchEvents, keyEvents);
        if (state == GameState.ServerWon)
            updateGameOver(touchEvents, keyEvents);
        if (state == GameState.ClientWon)
            updateGameOver(touchEvents, keyEvents);
        if (state == GameState.ClientDisconnected)
            updateNoConnection(touchEvents, keyEvents);
        if (state == GameState.Error1)
            updateNoConnection(touchEvents, keyEvents);
        if (state == GameState.NoConnection)
            updateNoConnection(touchEvents, keyEvents);
        if (state == GameState.Error3)
            updateNoConnection(touchEvents, keyEvents);
        if (state == GameState.ServerExited)
            updateNoConnection(touchEvents, keyEvents);
        if (state == GameState.Error5)
            updateNoConnection(touchEvents, keyEvents);
        if (state == GameState.Error6)
            updateNoConnection(touchEvents, keyEvents);
        if (state == GameState.Error7)
            updateNoConnection(touchEvents, keyEvents);
        if (state == GameState.TimeOut)
            updateNoConnection(touchEvents, keyEvents);
    }

    private void updateWaitingClient(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        serverIP = getLocalIpv4Address();

        if (serverIP == null) {
            state = GameState.NoConnection;
            return;
        }

        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                game.setScreen(new SelectWinConditionScreen(game));
                if (Settings.soundEnabled)
                    Assets.click.play(1);
                return;
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_UP) {
                if (event.x < 64 && event.y > 416) {
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    game.setScreen(new SelectWinConditionScreen(game));
                    return;
                }
            }
        }
    }

    private void updateReady(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                game.setScreen(new SelectWinConditionScreen(game));
                if (Settings.soundEnabled)
                    Assets.click.play(1);
                return;
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_UP) {
                if (inBounds(event, 0, 416, 64, 64)) {
                    game.setScreen(new SelectWinConditionScreen(game));
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;
                }

                if (!pressedStartServer) {
                    pressedStartServer = true;
                    if (Settings.soundEnabled)
                        Assets.click.play(1);

                    if (pressedStartClient) {
                        lastReceivedFromClient = System.nanoTime() / 1000000000.0f;
                        touchEvents.clear();
                        state = GameState.Running;
                        world.startTime = System.nanoTime();
                        world.lastBonusTime = System.nanoTime() / 1000000000.0f;

                        Message message = handlerToThread.obtainMessage(0);
                        message.sendToTarget();
                        return;
                    }
                }
            }
        }
    }

    private boolean inBounds(TouchEvent event, int x, int y, int width, int height) {
        if (event.x > x && event.x < x + width - 1 &&
                event.y > y && event.y < y + height - 1)
            return true;
        else
            return false;
    }

    private void updateRunning(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents, float deltaTime) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                state = GameState.Paused;
                world.timeElapsedSinceLastBonus = System.nanoTime() / 1000000000.0f - world.lastBonusTime;
                pausedByMe = true;
                //world.elapsedTime += System.nanoTime() - world.startTime;

                Message message = handlerToThread.obtainMessage(2);
                message.sendToTarget();

                if (Settings.soundEnabled)
                    Assets.click.play(1);
                return;
            }
        }

        if (!messageSent) {
            int len = touchEvents.size();
            for (int i = 0; i < len; i++) {
                TouchEvent event = touchEvents.get(i);

                if (event.type == TouchEvent.TOUCH_DOWN) {
//                if (firstTouch) {
//                    firstTouch = false;
//                    //return;
//                }
//
//                else {
                    touchedX = event.x;
                    touchedY = event.y;
                    moved = false;
//                }

                /*if (Settings.controlMode == Settings.ControlMode.Arrows) {
                    if (event.x < 64 && event.y > 416) {
                        world.snake.turnLeft();
                    }
                    if (event.x > 256 && event.y > 416) {
                        world.snake.turnRight();
                    }
                } else if (Settings.controlMode == Settings.ControlMode.Touch) {
//                    if (firstTouch) {
//                        firstTouch = false;
//                    } else {
                    if (event.y < 416) {
                        if (event.x < 64 && event.y < 64)
                            return;
                        int touchX = event.x / 16;
                        int touchY = event.y / 16;
                        Snake snake = world.snake;
                        SnakePart head = snake.parts.get(0);

                        if (snake.direction == Snake.UP) {
                            if (touchX > head.x)
                                world.snake.turnRight();
                            else if (touchX < head.x)
                                world.snake.turnLeft();
                        } else if (snake.direction == Snake.LEFT) {
                            if (touchY > head.y)
                                world.snake.turnLeft();
                            else if (touchY < head.y)
                                world.snake.turnRight();
                        } else if (snake.direction == Snake.DOWN) {
                            if (touchX > head.x)
                                world.snake.turnLeft();
                            else if (touchX < head.x)
                                world.snake.turnRight();
                        } else if (snake.direction == Snake.RIGHT) {
                            if (touchY > head.y)
                                world.snake.turnRight();
                            else if (touchY < head.y)
                                world.snake.turnLeft();
                        }
                    }
//                    }
                }*/
                }

                if (event.type == TouchEvent.TOUCH_DRAGGED) {
//                if (firstTouch) {
//                    firstTouch = false;
//                    //return;
//                }
//
//                else {
                    if (!moved) {
                        Snake snake = world.snake;
                        if (snake.direction == Snake.UP) {
                            if (event.x - touchedX > 20)
                                world.snake.turnRight();
                            if (event.x - touchedX < -20)
                                world.snake.turnLeft();
                        } else if (snake.direction == Snake.LEFT) {
                            if (event.y - touchedY < -20)
                                world.snake.turnRight();
                            if (event.y - touchedY > 20)
                                world.snake.turnLeft();
                        } else if (snake.direction == Snake.DOWN) {
                            if (event.x - touchedX > 20)
                                world.snake.turnLeft();
                            if (event.x - touchedX < -20)
                                world.snake.turnRight();
                        } else if (snake.direction == Snake.RIGHT) {
                            if (event.y - touchedY < -20)
                                world.snake.turnLeft();
                            if (event.y - touchedY > 20)
                                world.snake.turnRight();
                        }

                        if (Math.abs(event.x - touchedX) > 20 || Math.abs(event.y - touchedY) > 20) {
                            touchedX = event.x;
                            touchedY = event.y;
                            moved = true;
                        }
                    }
//                }
                }

                if (event.type == TouchEvent.TOUCH_UP) {
                /*if (event.x < 64 && event.y < 64) {
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    state = GameState.Paused;
                    world.elapsedTime += System.nanoTime() - world.startTime;

                    Message message = handlerToThread.obtainMessage(2);
                    message.sendToTarget();
                    return;
                }*/

                /*if (firstTouch) {
                    firstTouch = false;
                    return;
                }

                Snake snake = world.snake;
                if (snake.direction == Snake.UP) {
                    if (event.x - touchedX > 40)
                        world.snake.turnRight();
                    if (event.x - touchedX < -40)
                        world.snake.turnLeft();
                } else if (snake.direction == Snake.LEFT) {
                    if (event.y - touchedY < -40)
                        world.snake.turnRight();
                    if (event.y - touchedY > 40)
                        world.snake.turnLeft();
                } else if (snake.direction == Snake.DOWN) {
                    if (event.x - touchedX > 40)
                        world.snake.turnLeft();
                    if (event.x - touchedX < -40)
                        world.snake.turnRight();
                } else if (snake.direction == Snake.RIGHT) {
                    if (event.y - touchedY < -40)
                        world.snake.turnLeft();
                    if (event.y - touchedY > 40)
                        world.snake.turnRight();
                }

                touchedX = 0;
                touchedY = 0;*/
                }
            }

            world.update(deltaTime);
            remainingTime = Settings.gameTime - (int) (world.elapsedTime / 1000000000);
            if (Settings.winCondition == Settings.WinCondition.Time) {
                if (remainingTimeLast - remainingTime == 1 && remainingTime <= 10) {
                    if (Settings.soundEnabled)
                        Assets.chime.play(1);
                }
                remainingTimeLast = remainingTime;
            }

            if (world.gameOver) {
                sendWorldStateToClient();
                try {
                    Thread.sleep(50);
                } catch (Exception e) {

                }
                state = GameState.GameOver;
                firstGameOver = System.nanoTime() / 1000000000.0f;

                Message message = handlerToThread.obtainMessage(3);
                message.sendToTarget();
            }
            if (world.serverWon) {
                sendWorldStateToClient();
                try {
                    Thread.sleep(50);
                } catch (Exception e) {

                }
                state = GameState.ServerWon;
                firstGameOver = System.nanoTime() / 1000000000.0f;

                Message message = handlerToThread.obtainMessage(4);
                message.sendToTarget();
            }
            if (world.clientWon) {
                sendWorldStateToClient();
                try {
                    Thread.sleep(50);
                } catch (Exception e) {

                }
                state = GameState.ClientWon;
                firstGameOver = System.nanoTime() / 1000000000.0f;

                Message message = handlerToThread.obtainMessage(5);
                message.sendToTarget();
            }
            if (oldScore < world.score) {
                oldScore = world.score;
                score = "" + oldScore;
                if (Settings.soundEnabled && !world.eatGreen)
                    Assets.eat.play(1);
            }
            if (oldScoreClient < world.scoreClient) {
                oldScoreClient = world.scoreClient;
                scoreClient = "" + oldScoreClient;
                if (Settings.soundEnabled && !world.eatGreen)
                    Assets.eat.play(1);
            }
            if (world.bitten) {
                if (Settings.soundEnabled)
                    Assets.bitten.play(1);
                world.bitten = false;

                Message message = handlerToThread.obtainMessage(6);
                message.sendToTarget();
            }
            if (oldScore > world.score) {
                oldScore = world.score;
                score = "" + oldScore;
            }
            if (oldScoreClient > world.scoreClient) {
                oldScoreClient = world.scoreClient;
                scoreClient = "" + oldScoreClient;
            }

            if (world.eatGreen) {
                if (Settings.soundEnabled)
                    Assets.emmm.play(1);
                world.eatGreen = false;

                Message message = handlerToThread.obtainMessage(10);
                message.sendToTarget();
            }
            if (world.eatRed) {
                if (Settings.soundEnabled)
                    Assets.eo3.play(1);
                world.eatRed = false;

                Message message = handlerToThread.obtainMessage(11);
                message.sendToTarget();
            }

            if ((System.nanoTime() / 1000000000.0f) - lastUpdateTime >= 0.05) {
                lastUpdateTime = System.nanoTime() / 1000000000.0f;
                sendWorldStateToClient();
                messageSent = true;
            }
        }
    }
    
    public void sendWorldStateToClient() {
        String worldState = "Update ";
        
        worldState += world.snake.parts.size() + " ";
        for (int i = 0; i < world.snake.parts.size(); i++) {
            worldState += world.snake.parts.get(i).x + " ";
            worldState += world.snake.parts.get(i).y + " ";
        }
        worldState += world.snake.direction + " ";

        worldState += world.snakeClient.parts.size() + " ";
        for (int i = 0; i < world.snakeClient.parts.size(); i++) {
            worldState += world.snakeClient.parts.get(i).x + " ";
            worldState += world.snakeClient.parts.get(i).y + " ";
        }
        worldState += world.snakeClient.direction + " ";

        worldState += world.stain.x + " ";
        worldState += world.stain.y + " ";
        worldState += world.stain.type + " ";

        /************* Bonus Stain *************************/
        if (world.bonusStain != null) {
            worldState += "BonusStainYes ";
            worldState += world.bonusStain.x + " ";
            worldState += world.bonusStain.y + " ";
            worldState += world.bonusStain.type + " ";
            worldState += world.bonusStain.bonusType + " ";
        } else {
            worldState += "BonusStainNo ";
            worldState += "0 ";
            worldState += "0 ";
            worldState += "0 ";
            worldState += "0 ";
        }
        /************* Bonus Stain *************************/

        worldState += world.score + " ";
        worldState += world.scoreClient + " ";

        if (Settings.winCondition == Settings.WinCondition.Time) {
            //int remainingTime = Settings.gameTime - (int) (world.elapsedTime / 1000000000);
            worldState += remainingTime + " ";
        }

        Message message = handlerToThread.obtainMessage(1, worldState);
        message.sendToTarget();
    }

    public void sendWinConditionToClient() {
        String winCondition = "WinCondition ";

        if (Settings.winCondition == Settings.WinCondition.Score) {
            winCondition += "Score ";
            winCondition += Integer.toString(Settings.winScore);
        } else if (Settings.winCondition == Settings.WinCondition.Time) {
            winCondition += "Time ";
            winCondition += Integer.toString(Settings.gameTime);
        }

        Message message = handlerToThread.obtainMessage(8, winCondition);
        message.sendToTarget();
    }

    private void updatePaused(List<TouchEvent> touchEvents) {
        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_UP) {
                if (event.x > 80 && event.x <= 240) {
                    if (event.y  > 100 && event.y  <= 148) {
                        if (pausedByMe) {
                            pausedByMe = false;
                            if (Settings.soundEnabled)
                                Assets.click.play(1);
                            lastReceivedFromClient = System.nanoTime() / 1000000000.0f;
                            state = GameState.Running;
                            world.startTime = System.nanoTime() - world.elapsedTime;
                            world.lastBonusTime = System.nanoTime() / 1000000000.0f - world.timeElapsedSinceLastBonus;

                            Message message = handlerToThread.obtainMessage(0);
                            message.sendToTarget();

                            return;
                        }
                    }
                    if (event.y > 148 && event.y < 196) {
                        if (Settings.soundEnabled)
                            Assets.click.play(1);
                        game.setScreen(new SelectWinConditionScreen(game));
                        return;
                    }
                }
            }
        }
    }

    private void updateGameOver(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                if (System.nanoTime() / 1000000000.0f - firstGameOver >= 2) {
                    myGame.showAd();

                    game.setScreen(new SelectWinConditionScreen(game));
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;
                }
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_UP) {
                if (event.x >= 128 && event.x <= 192 &&
                        event.y >= 200 && event.y <= 264) {
                    if (System.nanoTime() / 1000000000.0f - firstGameOver >= 2) {
                        myGame.showAd();

                        if (Settings.soundEnabled)
                            Assets.click.play(1);

                        Message message = handlerToThread.obtainMessage(7);
                        message.sendToTarget();

                        prepareNewGame();
                    }
                }
            }
        }
    }

    private void prepareNewGame() {
        world = new WorldServer();
        world.init();

        state = GameState.Ready;
        remainingTime = Settings.gameTime;
        firstTouch = true;
        oldScore = 0;
        oldScoreClient = 0;
        score = "0";
        scoreClient = "0";

        pressedStartServer = false;
        pressedStartClient = false;
        lastUpdateTime = System.nanoTime() / 1000000000.0f;
        lastReceivedFromClient = System.nanoTime() / 1000000000.0f;
        remainingTimeLast = 0;

        touchedX = 0;
        touchedY = 0;

        sendWorldStateToClient();
    }

    private void updateNoConnection(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                game.setScreen(new SelectWinConditionScreen(game));
                if (Settings.soundEnabled)
                    Assets.click.play(1);
                return;
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_UP) {
                if (event.x >= 128 && event.x <= 192 &&
                        event.y >= 200 && event.y <= 264) {
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    game.setScreen(new SelectWinConditionScreen(game));
                    return;
                }
            }
        }
    }

    @Override
    public void present(float deltaTime) {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.background, 0, 0);
        if (!(state == GameState.WaitingClient || state == GameState.Ready))
            drawWorld(world);

        if (state == GameState.WaitingClient)
            drawWaitingClientUI();
        if (state == GameState.Ready)
            drawReadyUI();
        if (state == GameState.Running)
            drawRunningUI();
        if (state == GameState.Paused)
            drawPausedUI();
        if (state == GameState.GameOver)
            drawGameOverUI();
        if (state == GameState.ServerWon)
            drawServerWonUI();
        if (state == GameState.ClientWon)
            drawClientWonUI();
        if (state == GameState.ClientDisconnected)
            drawClientDisconnectedUI();
        if (state == GameState.Error1)
            drawError1UI();
        if (state == GameState.NoConnection)
            drawNoConnectionUI();
        if (state == GameState.Error3)
            drawError3UI();
        if (state == GameState.ServerExited)
            drawServerExitedUI();
        if (state == GameState.Error5)
            drawError5UI();
        if (state == GameState.Error6)
            drawError6UI();
        if (state == GameState.Error7)
            drawError7UI();
        if (state == GameState.TimeOut)
            drawTimeOutUI();

        drawText(g, score, 80 - score.length() * 20 / 2, g.getHeight() - 42,
                DigitsColor.White);
        drawText(g, scoreClient, 240 - scoreClient.length() * 20 / 2, g.getHeight() - 42,
                DigitsColor.Red);
        if (Settings.winCondition == Settings.WinCondition.Score) {
            drawText(g, Integer.toString(Settings.winScore),
                    160 - Integer.toString(Settings.winScore).length() * 20 / 2,
                    g.getHeight() - 42, DigitsColor.Blue);
        } else if (Settings.winCondition == Settings.WinCondition.Time) {
            //int remTime = Settings.gameTime - (int) (world.elapsedTime / 1000000000);

            drawText(g, Integer.toString(remainingTime),
                    160 - Integer.toString(remainingTime).length() * 20 / 2,
                    g.getHeight() - 42, DigitsColor.Green);
        }
    }

    private void drawWorld(WorldServer world) {
        Graphics g = game.getGraphics();
        Snake snake = world.snake;
        Snake snakeClient = world.snakeClient;
        SnakePart head = snake.parts.get(0);
        SnakePart headClient = snakeClient.parts.get(0);

        Stain stain = world.stain;
        Pixmap stainPixmap = null;
        if (stain.type == Stain.TYPE_1)
            stainPixmap = Assets.stain1;
        if (stain.type == Stain.TYPE_2)
            stainPixmap = Assets.stain2;
        if (stain.type == Stain.TYPE_3)
            stainPixmap = Assets.stain3;
        int x = stain.x * 16;
        int y = stain.y * 16;
        g.drawPixmap(stainPixmap, x, y);

        if (world.bonusStain != null) {
            BonusStain bonusStain = world.bonusStain;
            Pixmap bonusStainPixmap = null;
            if (bonusStain.bonusType == BonusStain.GREEN_STAIN) {
                if (bonusStain.type == BonusStain.TYPE_1)
                    bonusStainPixmap = Assets.stain1Green;
                if (bonusStain.type == BonusStain.TYPE_2)
                    bonusStainPixmap = Assets.stain2Green;
                if (bonusStain.type == BonusStain.TYPE_3)
                    bonusStainPixmap = Assets.stain3Green;
            }
            if (bonusStain.bonusType == BonusStain.RED_STAIN) {
                if (bonusStain.type == BonusStain.TYPE_1)
                    bonusStainPixmap = Assets.stain1Red;
                if (bonusStain.type == BonusStain.TYPE_2)
                    bonusStainPixmap = Assets.stain2Red;
                if (bonusStain.type == BonusStain.TYPE_3)
                    bonusStainPixmap = Assets.stain3Red;
            }
            x = bonusStain.x * 16;
            y = bonusStain.y * 16;
            g.drawPixmap(bonusStainPixmap, x, y);
        }

        int len = snake.parts.size();
        for (int i = 1; i < len; i++) {
            SnakePart part = snake.parts.get(i);
            x = part.x * 16;
            y = part.y * 16;
            g.drawPixmap(Assets.tail, x, y);
        }

        len = snakeClient.parts.size();
        for (int i = 1; i < len; i++) {
            SnakePart part = snakeClient.parts.get(i);
            x = part.x * 16;
            y = part.y * 16;
            g.drawPixmap(Assets.tailClient, x, y);
        }

        Pixmap headPixmap = null;
        if (snake.direction == Snake.UP)
            headPixmap = Assets.headUp;
        if (snake.direction == Snake.LEFT)
            headPixmap = Assets.headLeft;
        if (snake.direction == Snake.DOWN)
            headPixmap = Assets.headDown;
        if (snake.direction == Snake.RIGHT)
            headPixmap = Assets.headRight;
        x = head.x * 16 + 8;
        y = head.y * 16 + 8;
        g.drawPixmap(headPixmap, x - headPixmap.getWidth() / 2, y - headPixmap.getHeight() / 2);

        if (snakeClient.direction == Snake.UP)
            headPixmap = Assets.headUpClient;
        if (snakeClient.direction == Snake.LEFT)
            headPixmap = Assets.headLeftClient;
        if (snakeClient.direction == Snake.DOWN)
            headPixmap = Assets.headDownClient;
        if (snakeClient.direction == Snake.RIGHT)
            headPixmap = Assets.headRightClient;
        x = headClient.x * 16 + 8;
        y = headClient.y * 16 + 8;
        g.drawPixmap(headPixmap, x - headPixmap.getWidth() / 2, y - headPixmap.getHeight() / 2);
    }

    private void drawWaitingClientUI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.waitingClient, 12, 100);
        g.drawPixmap(Assets.serverIP, 70, 250);
        drawText(g, serverIP, 160 - getTextLengthInPixels(serverIP) / 2, 320, DigitsColor.White);
        g.drawPixmap(Assets.buttons, 0, 416, 64, 64, 128, 128);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private int getTextLengthInPixels(String str) {
        int length = 0;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char character = str.charAt(i);
            if (character == '.')
                length += 10;
            else
                length += 20;
        }

        return length;
    }

    private void drawReadyUI() {
        Graphics g = game.getGraphics();

        if (!pressedStartServer)
            g.drawPixmap(Assets.ready, 47, 100);
        else
            g.drawPixmap(Assets.waitingStart, 0, 100);

        if (Settings.winCondition == Settings.WinCondition.Score) {
            g.drawPixmap(Assets.winScore, 20, 300);
            drawText(g, Integer.toString(Settings.winScore), 230, 310, DigitsColor.Blue);
        } else if (Settings.winCondition == Settings.WinCondition.Time) {
            g.drawPixmap(Assets.gameTime, 20, 300);
            drawText(g, Integer.toString(Settings.gameTime), 230, 310, DigitsColor.Green);
        }

        g.drawPixmap(Assets.buttons, 0, 416, 64, 64, 128, 128);

        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawRunningUI() {
        Graphics g = game.getGraphics();

        //g.drawPixmap(Assets.buttons, 0, 0, 64, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
        /*if (Settings.controlMode == Settings.ControlMode.Arrows) {
            g.drawPixmap(Assets.buttons, 0, 416, 64, 64, 64, 64);
            g.drawPixmap(Assets.buttons, 256, 416, 0, 64, 64, 64);
        }*/
    }

    private void drawPausedUI() {
        Graphics g = game.getGraphics();

        if (pausedByMe)
            g.drawPixmap(Assets.pause, 80, 100);
        else
            g.drawPixmap(Assets.pause2, 80, 100);

        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawGameOverUI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.gameOver, 62, 100);
        if (System.nanoTime() / 1000000000.0f - firstGameOver >= 2)
            g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawServerWonUI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.serverWon, 62, 100);
        if (System.nanoTime() / 1000000000.0f - firstGameOver >= 2)
            g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawClientWonUI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.clientWon, 62, 100);
        if (System.nanoTime() / 1000000000.0f - firstGameOver >= 2)
            g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawClientDisconnectedUI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.clientDisconnected, 12, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawError1UI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.error1, 62, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawNoConnectionUI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.noConnection, 62, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawError3UI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.error3, 62, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawServerExitedUI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.serverExited, 62, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawError5UI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.error5, 62, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawError6UI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.error6, 62, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawError7UI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.error7, 62, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawTimeOutUI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.timeOut, 12, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    public void drawText(Graphics g, String line, int x, int y, DigitsColor digitsColor) {
        int len = line.length();
        for (int i = 0; i < len; i++) {
            char character = line.charAt(i);
            if (character == ' ') {
                x += 20;
                continue;
            }

            int srcX;
            int srcWidth;
            if (character == '.') {
                srcX = 200;
                srcWidth = 10;
            } else {
                srcX = (character - '0') * 20;
                srcWidth = 20;
            }

            if (digitsColor == DigitsColor.White)
                g.drawPixmap(Assets.numbers, x, y, srcX, 0, srcWidth, 32);
            else if (digitsColor == DigitsColor.Red)
                g.drawPixmap(Assets.numbersRed, x, y, srcX, 0, srcWidth, 32);
            else if (digitsColor == DigitsColor.Blue)
                g.drawPixmap(Assets.numbersBlue, x, y, srcX, 0, srcWidth, 32);
            else if (digitsColor == DigitsColor.Green)
                g.drawPixmap(Assets.numbersGreen, x, y, srcX, 0, srcWidth, 32);
            x += srcWidth;
        }
    }

    @Override
    public void pause() {
        if (state == GameState.Running) {
            state = GameState.Paused;
            pausedByMe = true;

            Message message = handlerToThread.obtainMessage(2);
            message.sendToTarget();
            //world.elapsedTime += System.nanoTime() - world.startTime;
        }

        if (world.gameOver) {
            Settings.addScore(world.score);
            Settings.addScore(world.scoreClient);
            Settings.save(game.getFileIO());
        }

        //world.elapsedTime += System.nanoTime() - world.startTime;
    }

    @Override
    public void resume() {
        /******************** Network *************************/
        // get the IP address of itself
        serverIP = getLocalIpv4Address();

        // start the server
        if (serverThread == null) {
            serverThread = new Thread(new ServerThread());
            serverThread.start();
        }
        /******************** Network *************************/
        //world.startTime = System.nanoTime() - world.elapsedTime;

    }

    @Override
    public void dispose() {
        /******************** Network *************************/
        Message message = handlerToThread.obtainMessage(9);
        message.sendToTarget();

        try {
            serverSocket.close();
        } catch (Exception e) {
            state = GameState.Error7;
        }
        /******************** Network *************************/
    }

    //---get the local IPv4 address---
    public String getLocalIpv4Address() {
        try {
            for (Enumeration<NetworkInterface> networkInterfaceEnum =
                 NetworkInterface.getNetworkInterfaces();
                 networkInterfaceEnum.hasMoreElements();) {
                NetworkInterface networkInterface = networkInterfaceEnum
                        .nextElement();
                for (Enumeration<InetAddress> ipAddressEnum = networkInterface
                        .getInetAddresses(); ipAddressEnum.hasMoreElements();) {
                    InetAddress inetAddress = ipAddressEnum.nextElement();
                    // ---check that it is not a loopback address and it is IPv4---
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            state = GameState.Error6;
        }
        return null;
    }

    public class ServerThread implements Runnable {
        public ServerThread() {
            handlerToThread = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message message) {
                    if (message.what == 0) {
                        if (printWriter != null)
                            printWriter.println("Running");
                    }
                    if (message.what == 1) {
                        if (printWriter != null)
                            printWriter.println((String) (message.obj));
                    }
                    if (message.what == 2) {
                        if (printWriter != null)
                            printWriter.println("Paused");
                    }
                    if (message.what == 3) {
                        if (printWriter != null)
                            printWriter.println("GameOver");
                    }
                    if (message.what == 4) {
                        if (printWriter != null)
                            printWriter.println("ServerWon");
                    }
                    if (message.what == 5) {
                        if (printWriter != null)
                            printWriter.println("ClientWon");
                    }
                    if (message.what == 6) {
                        if (printWriter != null)
                            printWriter.println("Bitten");
                    }
                    if (message.what == 7) {
                        if (printWriter != null)
                            printWriter.println("PrepareNewGame");
                    }
                    if (message.what == 8) {
                        if (printWriter != null)
                            printWriter.println((String) (message.obj));
                    }
                    if (message.what == 9) {
                        if (printWriter != null)
                            printWriter.println("ExitReadyMode");
                    }
                    if (message.what == 10) {
                        if (printWriter != null)
                            printWriter.println("EatGreen");
                    }
                    if (message.what == 11) {
                        if (printWriter != null)
                            printWriter.println("EatRed");
                    }
                }
            };
        }

        public void run() {
            try {
                if (serverIP != null) {
                    // create an instance of the server socket
                    serverSocket = new ServerSocket(serverPort);

                    while (true) {
                        // wait for incoming clients
                        Socket client = serverSocket.accept();

                        // the above code is a blocking call
                        // i.e. it will block until a client connects

                        printWriter = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(client.getOutputStream())),
                                true);

                        disconnected = false;

                        Message message = handlerFromThread.obtainMessage(4);
                        message.sendToTarget();

                        try {
                            // get an inputStream object to read from the socket
                            /*BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

                            OutputStream outputStream = client.getOutputStream();*/

                            Thread listenThread = new Thread(new Listen(client));
                            listenThread.start();

                            while (!disconnected);

                            // read all incoming data terminated with a \n char
                            /*String line = null;
                            while ((line = br.readLine()) != null) {
                                final String strReceived = line;

                                // send whatever you received back to the client
                                String s = line + "\n";
                                outputStream.write(s.getBytes());

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        textView1.setText(textView1.getText() + strReceived + "\n");
                                    }
                                });
                            }*/

                            message = handlerFromThread.obtainMessage(5);
                            message.sendToTarget();

                        } catch (Exception e) {
                            message = handlerFromThread.obtainMessage(6, "1");
                            message.sendToTarget();
                        }
                    }
                } else {
                    Message message = handlerFromThread.obtainMessage(6, "2");
                    message.sendToTarget();
                }
            } catch (Exception e) {
                Message message = handlerFromThread.obtainMessage(6, "3");
                message.sendToTarget();
            }

            Message message = handlerFromThread.obtainMessage(6, "4");
            message.sendToTarget();
        }
    }

    public class Listen implements Runnable {
        Socket client;
        public Listen(Socket client) {
            this.client = client;
        }
        public void run() {
            try {

                BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String line;
                while ((line = br.readLine()) != null) {
                    final String strReceived = line;
                    if (strReceived.equals("Ready")) {
                        Message message = handlerFromThread.obtainMessage(0);
                        message.sendToTarget();
                    }
                    if (strReceived.equals("Paused")) {
                        Message message = handlerFromThread.obtainMessage(1);
                        message.sendToTarget();
                    }
                    if (strReceived.equals("TurnLeft")) {
                        Message message = handlerFromThread.obtainMessage(2, strReceived);
                        message.sendToTarget();
                    }
                    if (strReceived.equals("TurnRight")) {
                        Message message = handlerFromThread.obtainMessage(2, strReceived);
                        message.sendToTarget();
                    }
                    if (strReceived.equals("Running")) {
                        Message message = handlerFromThread.obtainMessage(3);
                        message.sendToTarget();
                    }
                    if (strReceived.equals("PrepareNewGame")) {
                        Message message = handlerFromThread.obtainMessage(7);
                        message.sendToTarget();
                    }
                    if (strReceived.equals("I Received")) {
                        Message message = handlerFromThread.obtainMessage(8);
                        message.sendToTarget();
                    }
                }

                disconnected = true;
            } catch (Exception e) {
                Message message = handlerFromThread.obtainMessage(6, "5");
                message.sendToTarget();
            }
        }
    }
}