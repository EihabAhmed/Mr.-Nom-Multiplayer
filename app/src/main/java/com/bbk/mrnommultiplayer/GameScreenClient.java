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
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class GameScreenClient extends Screen {
    String serverIP;
    int serverPort = 2516;
    Handler handlerToThread;
    Handler handlerFromThread;
    static Socket socket;
    PrintWriter printWriter;

    boolean disconnected;

    Thread clientThread;

    enum GameState {
        WaitingConnection,
        Ready,
        Running,
        Paused,
        GameOver,
        ServerWon,
        ClientWon,
        Error8,
        DisconnectedFromServer,
        Error10,
        Error11,
        ConnectionClosed,
        CannotConnect,
        Error14,
        TimeOut
    }

    enum DigitsColor {
        White,
        Red,
        Blue,
        Green
    }

    GameState state = GameState.WaitingConnection;
    WorldClient world;
    int oldScore = 0;
    int oldScoreClient = 0;
    String score = "0";
    String scoreClient = "0";
    //boolean firstTouch = true;
    boolean pressedStartClient = false;
    float lastUpdateTime = System.nanoTime() / 1000000000.0f;
    int remainingTime = Settings.gameTime;
    int remainingTimeLast = 0;

    int touchedX = 0;
    int touchedY = 0;
    boolean moved;

    boolean pausedByMe = false;

    float firstGameOver;

    MrNomGame myGame;

    public GameScreenClient(Game game) {
        super(game);
        world = new WorldClient();
        world.init();

        myGame = (MrNomGame)game;

        serverIP = Settings.serverIP;

        handlerFromThread = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if (message.what == 0) {
                    lastUpdateTime = System.nanoTime() / 1000000000.0f;
                    state = GameState.Running;
                }
                if (message.what == 1) {
                    lastUpdateTime = System.nanoTime() / 1000000000.0f;
                    updateWorld((String)(message.obj));

                    Message returnMessage = handlerToThread.obtainMessage(5);
                    returnMessage.sendToTarget();
                }
                if (message.what == 2) {
                    state = GameState.Paused;
                    pausedByMe = false;
                }
                if (message.what == 3) {
                    state = GameState.GameOver;
                    firstGameOver = System.nanoTime() / 1000000000.0f;
                }
                if (message.what == 4) {
                    state = GameState.ServerWon;
                    firstGameOver = System.nanoTime() / 1000000000.0f;
                }
                if (message.what == 5) {
                    state = GameState.ClientWon;
                    firstGameOver = System.nanoTime() / 1000000000.0f;
                }
                if (message.what == 6) {
                    remainingTime = Settings.gameTime;
                    state = GameState.Ready;
                }
                if (message.what == 7)
                    if (Settings.soundEnabled)
                        Assets.bitten.play(1);
                if (message.what == 8) {
                    if (message.obj.equals("9"))
                        state = GameState.DisconnectedFromServer;
                    if (message.obj.equals("10"))
                        state = GameState.Error10;
                    if (message.obj.equals("11"))
                        state = GameState.Error11;
                    if (message.obj.equals("12"))
                        state = GameState.ConnectionClosed;
                    if (message.obj.equals("13"))
                        state = GameState.CannotConnect;
                    if (message.obj.equals("14"))
                        state = GameState.DisconnectedFromServer;
                }
                if (message.what == 9) {
                    myGame.showAd();
                    prepareNewGame();
                }
                if (message.what == 10) {
                    String str = ((String)(message.obj));
                    str = str.substring(str.indexOf(" ") + 1);

                    if (nextToken(str).equals("Score")) {
                        Settings.winCondition = Settings.WinCondition.Score;
                        str = str.substring(str.indexOf(" ") + 1);

                        Settings.winScore = Integer.parseInt(str);
                    } else {
                        Settings.winCondition = Settings.WinCondition.Time;
                        str = str.substring(str.indexOf(" ") + 1);

                        Settings.gameTime = Integer.parseInt(str);
                    }
                }
                if (message.what == 11)
                    if (Settings.soundEnabled)
                        Assets.emmm.play(1);
                if (message.what == 12)
                    if (Settings.soundEnabled)
                        Assets.eo3.play(1);
            }
        };
    }

    void updateWorld(String str) {
        synchronized(this) {
            str = str.substring(str.indexOf(" ") + 1);

            /********************* Snake *************************/
            int snakeSize = Integer.parseInt(nextToken(str));
            str = str.substring(str.indexOf(" ") + 1);

            world.snake.parts.clear();
            for (int i = 0; i < snakeSize; i++) {
                int x = Integer.parseInt(nextToken(str));
                str = str.substring(str.indexOf(" ") + 1);

                int y = Integer.parseInt(nextToken(str));
                str = str.substring(str.indexOf(" ") + 1);

                world.snake.parts.add(new SnakePart(x, y));
            }

            world.snake.direction = Integer.parseInt(nextToken(str));
            str = str.substring(str.indexOf(" ") + 1);
            /********************* Snake *************************/

            /********************* SnakeClient *******************/
            int snakeClientSize = Integer.parseInt(nextToken(str));
            str = str.substring(str.indexOf(" ") + 1);

            world.snakeClient.parts.clear();
            for (int i = 0; i < snakeClientSize; i++) {
                int x = Integer.parseInt(nextToken(str));
                str = str.substring(str.indexOf(" ") + 1);

                int y = Integer.parseInt(nextToken(str));
                str = str.substring(str.indexOf(" ") + 1);

                world.snakeClient.parts.add(new SnakePart(x, y));
            }

            world.snakeClient.direction = Integer.parseInt(nextToken(str));
            str = str.substring(str.indexOf(" ") + 1);
            /********************* SnakeClient *******************/

            /************* Stain ***********************/
            int x = Integer.parseInt(nextToken(str));
            str = str.substring(str.indexOf(" ") + 1);

            int y = Integer.parseInt(nextToken(str));
            str = str.substring(str.indexOf(" ") + 1);

            int type = Integer.parseInt(nextToken(str));
            str = str.substring(str.indexOf(" ") + 1);

            world.stain = new Stain(x, y, type);
            /************* Stain ***********************/

            /************* Bonus Stain ***********************/
            if (nextToken(str).equals("BonusStainYes")) {
                str = str.substring(str.indexOf(" ") + 1);

                x = Integer.parseInt(nextToken(str));
                str = str.substring(str.indexOf(" ") + 1);

                y = Integer.parseInt(nextToken(str));
                str = str.substring(str.indexOf(" ") + 1);

                type = Integer.parseInt(nextToken(str));
                str = str.substring(str.indexOf(" ") + 1);

                int bonusType = Integer.parseInt(nextToken(str));
                str = str.substring(str.indexOf(" ") + 1);

                world.bonusStain = new BonusStain(x, y, type, bonusType);
            }
            if (nextToken(str).equals("BonusStainNo")) {
                str = str.substring(str.indexOf(" ") + 1);
                str = str.substring(str.indexOf(" ") + 1);
                str = str.substring(str.indexOf(" ") + 1);
                str = str.substring(str.indexOf(" ") + 1);
                str = str.substring(str.indexOf(" ") + 1);

                world.bonusStain = null;
            }

            /************* Bonus Stain ***********************/

            world.score = Integer.parseInt(nextToken(str));
            str = str.substring(str.indexOf(" ") + 1);

            world.scoreClient = Integer.parseInt(nextToken(str));

            if (Settings.winCondition == Settings.WinCondition.Time) {
                str = str.substring(str.indexOf(" ") + 1);
                remainingTime = Integer.parseInt(nextToken(str));
            }
        }
    }

    String nextToken(String str) {
        int index = str.indexOf(" ");

        return str.substring(0, index);
    }

    @Override
    public void update(float deltaTime) {
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        List<KeyEvent> keyEvents = game.getInput().getKeyEvents();

        if ((System.nanoTime() / 1000000000.0f) - lastUpdateTime >= 10) {
            if (state == GameState.Running)
                state = GameState.TimeOut;
        }

        if (state == GameState.WaitingConnection)
            updateWaitingConnection(touchEvents, keyEvents);
        if (state == GameState.Ready)
            updateReady(touchEvents, keyEvents);
        if (state == GameState.Running)
            updateRunning(touchEvents, keyEvents);
        if (state == GameState.Paused)
            updatePaused(touchEvents);
        if (state == GameState.GameOver)
            updateGameOver(touchEvents, keyEvents);
        if (state == GameState.ServerWon)
            updateGameOver(touchEvents, keyEvents);
        if (state == GameState.ClientWon)
            updateGameOver(touchEvents, keyEvents);
        if (state == GameState.Error8)
            updateErrors(touchEvents, keyEvents);
        if (state == GameState.DisconnectedFromServer)
            updateErrors(touchEvents, keyEvents);
        if (state == GameState.Error10)
            updateErrors(touchEvents, keyEvents);
        if (state == GameState.Error11)
            updateErrors(touchEvents, keyEvents);
        if (state == GameState.ConnectionClosed)
            updateErrors(touchEvents, keyEvents);
        if (state == GameState.CannotConnect)
            updateErrors(touchEvents, keyEvents);
        if (state == GameState.Error14)
            updateErrors(touchEvents, keyEvents);
        if (state == GameState.TimeOut)
            updateErrors(touchEvents, keyEvents);
    }

    private void updateWaitingConnection(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                game.setScreen(new MultiplayerJoinScreen(game));
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
                    game.setScreen(new MultiplayerJoinScreen(game));
                    return;
                }
            }
        }
    }

    private void updateReady(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                game.setScreen(new MultiplayerJoinScreen(game));
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
                    game.setScreen(new MultiplayerJoinScreen(game));
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;
                }

                if (!pressedStartClient) {
                    pressedStartClient = true;
                    if (Settings.soundEnabled)
                        Assets.click.play(1);

                    Message message = handlerToThread.obtainMessage(0);
                    message.sendToTarget();
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

    private void updateRunning(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                state = GameState.Paused;
                pausedByMe = true;

                Message message = handlerToThread.obtainMessage(1);
                message.sendToTarget();

                if (Settings.soundEnabled)
                    Assets.click.play(1);
                return;
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);

            if (event.type == TouchEvent.TOUCH_DOWN) {
                touchedX = event.x;
                touchedY = event.y;
                moved = false;

                /*if (Settings.controlMode == Settings.ControlMode.Arrows) {
                    if (event.x < 64 && event.y > 416) {
                        Message message = handlerToThread.obtainMessage(2, "TurnLeft");
                        message.sendToTarget();
                    }
                    if (event.x > 256 && event.y > 416) {
                        Message message = handlerToThread.obtainMessage(2, "TurnRight");
                        message.sendToTarget();
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
                        Snake snakeClient = world.snakeClient;
                        SnakePart head = snakeClient.parts.get(0);

                        if (snakeClient.direction == Snake.UP) {
                            if (touchX > head.x) {
                                Message message = handlerToThread.obtainMessage(2, "TurnRight");
                                message.sendToTarget();
                            } else if (touchX < head.x) {
                                Message message = handlerToThread.obtainMessage(2, "TurnLeft");
                                message.sendToTarget();
                            }
                        } else if (snakeClient.direction == Snake.LEFT) {
                            if (touchY > head.y) {
                                Message message = handlerToThread.obtainMessage(2, "TurnLeft");
                                message.sendToTarget();
                            } else if (touchY < head.y) {
                                Message message = handlerToThread.obtainMessage(2, "TurnRight");
                                message.sendToTarget();
                            }
                        } else if (snakeClient.direction == Snake.DOWN) {
                            if (touchX > head.x) {
                                Message message = handlerToThread.obtainMessage(2, "TurnLeft");
                                message.sendToTarget();
                            } else if (touchX < head.x) {
                                Message message = handlerToThread.obtainMessage(2, "TurnRight");
                                message.sendToTarget();
                            }
                        } else if (snakeClient.direction == Snake.RIGHT) {
                            if (touchY > head.y) {
                                Message message = handlerToThread.obtainMessage(2, "TurnRight");
                                message.sendToTarget();
                            } else if (touchY < head.y) {
                                Message message = handlerToThread.obtainMessage(2, "TurnLeft");
                                message.sendToTarget();
                            }
                        }
                    }
//                    }
                }*/
            }

            if (event.type == TouchEvent.TOUCH_DRAGGED) {
                if (!moved) {
                    Snake snake = world.snakeClient;
                    if (snake.direction == Snake.UP) {
                        if (event.x - touchedX > 20) {
                            Message message = handlerToThread.obtainMessage(2, "TurnRight");
                            message.sendToTarget();
                        }
                        if (event.x - touchedX < -20) {
                            Message message = handlerToThread.obtainMessage(2, "TurnLeft");
                            message.sendToTarget();
                        }
                    } else if (snake.direction == Snake.LEFT) {
                        if (event.y - touchedY < -20) {
                            Message message = handlerToThread.obtainMessage(2, "TurnRight");
                            message.sendToTarget();
                        }
                        if (event.y - touchedY > 20) {
                            Message message = handlerToThread.obtainMessage(2, "TurnLeft");
                            message.sendToTarget();
                        }
                    } else if (snake.direction == Snake.DOWN) {
                        if (event.x - touchedX > 20) {
                            Message message = handlerToThread.obtainMessage(2, "TurnLeft");
                            message.sendToTarget();
                        }
                        if (event.x - touchedX < -20) {
                            Message message = handlerToThread.obtainMessage(2, "TurnRight");
                            message.sendToTarget();
                        }
                    } else if (snake.direction == Snake.RIGHT) {
                        if (event.y - touchedY < -20) {
                            Message message = handlerToThread.obtainMessage(2, "TurnLeft");
                            message.sendToTarget();
                        }
                        if (event.y - touchedY > 20) {
                            Message message = handlerToThread.obtainMessage(2, "TurnRight");
                            message.sendToTarget();
                        }
                    }

                    if (Math.abs(event.x - touchedX) > 20 || Math.abs(event.y - touchedY) > 20) {
                        touchedX = event.x;
                        touchedY = event.y;
                        moved = true;
                    }
                }
            }

            if (event.type == TouchEvent.TOUCH_UP) {
                /*if (event.x < 64 && event.y < 64) {
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    state = GameState.Paused;

                    Message message = handlerToThread.obtainMessage(1);
                    message.sendToTarget();

                    return;
                }*/

                /*Snake snake = world.snakeClient;
                if (snake.direction == Snake.UP) {
                    if (event.x - touchedX > 40) {
                        Message message = handlerToThread.obtainMessage(2, "TurnRight");
                        message.sendToTarget();
                    }
                    if (event.x - touchedX < -40) {
                        Message message = handlerToThread.obtainMessage(2, "TurnLeft");
                        message.sendToTarget();
                    }
                } else if (snake.direction == Snake.LEFT) {
                    if (event.y - touchedY < -40) {
                        Message message = handlerToThread.obtainMessage(2, "TurnRight");
                        message.sendToTarget();
                    }
                    if (event.y - touchedY > 40) {
                        Message message = handlerToThread.obtainMessage(2, "TurnLeft");
                        message.sendToTarget();
                    }
                } else if (snake.direction == Snake.DOWN) {
                    if (event.x - touchedX > 40) {
                        Message message = handlerToThread.obtainMessage(2, "TurnLeft");
                        message.sendToTarget();
                    }
                    if (event.x - touchedX < -40) {
                        Message message = handlerToThread.obtainMessage(2, "TurnRight");
                        message.sendToTarget();
                    }
                } else if (snake.direction == Snake.RIGHT) {
                    if (event.y - touchedY < -40) {
                        Message message = handlerToThread.obtainMessage(2, "TurnLeft");
                        message.sendToTarget();
                    }
                    if (event.y - touchedY > 40) {
                        Message message = handlerToThread.obtainMessage(2, "TurnRight");
                        message.sendToTarget();
                    }
                }

                touchedX = 0;
                touchedY = 0;*/
            }
        }

        if (Settings.winCondition == Settings.WinCondition.Time) {
            if (remainingTimeLast - remainingTime == 1 && remainingTime <= 10) {
                if (Settings.soundEnabled)
                    Assets.chime.play(1);
            }
            remainingTimeLast = remainingTime;
        }

        if (oldScore < world.score) {
            boolean playSound = false;
            if (world.score - oldScore == 10)
                playSound = true;
            oldScore = world.score;
            score = "" + oldScore;
            if (Settings.soundEnabled && playSound)
                Assets.eat.play(1);
        }
        if (oldScoreClient < world.scoreClient) {
            boolean playSound = false;
            if (world.scoreClient - oldScoreClient == 10)
                playSound = true;
            oldScoreClient = world.scoreClient;
            scoreClient = "" + oldScoreClient;
            if (Settings.soundEnabled && playSound)
                Assets.eat.play(1);
        }
        if (oldScore > world.score) {
            oldScore = world.score;
            score = "" + oldScore;
        }
        if (oldScoreClient > world.scoreClient) {
            oldScoreClient = world.scoreClient;
            scoreClient = "" + oldScoreClient;
        }
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
                            lastUpdateTime = System.nanoTime() / 1000000000.0f;
                            state = GameState.Running;

                            Message message = handlerToThread.obtainMessage(3);
                            message.sendToTarget();

                            return;
                        }
                    }
                    if (event.y > 148 && event.y < 196) {
                        if (Settings.soundEnabled)
                            Assets.click.play(1);
                        game.setScreen(new MultiplayerJoinScreen(game));
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

                    game.setScreen(new MultiplayerJoinScreen(game));
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

                        prepareNewGame();

                        Message message = handlerToThread.obtainMessage(4);
                        message.sendToTarget();
                    }
                }
            }
        }
    }

    private void prepareNewGame() {
        world = new WorldClient();
        world.init();

        remainingTime = Settings.gameTime;
        remainingTimeLast = 0;
        state = GameState.Ready;

        oldScore = 0;
        oldScoreClient = 0;
        score = "0";
        scoreClient = "0";
        pressedStartClient = false;

        touchedX = 0;
        touchedY = 0;
    }

    private void updateErrors(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                game.setScreen(new MultiplayerJoinScreen(game));
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
                    game.setScreen(new MultiplayerJoinScreen(game));
                    return;
                }
            }
        }
    }

    @Override
    public void present(float deltaTime) {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.background, 0, 0);
        if (!(state == GameState.WaitingConnection || state == GameState.Ready
                                || state == GameState.CannotConnect)) {
            synchronized (this) {
                drawWorld(world);
            }
        }

        if (state == GameState.WaitingConnection)
            drawWaitingConnectionUI();
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
        if (state == GameState.Error8)
            drawError8UI();
        if (state == GameState.DisconnectedFromServer)
            drawDisconnectedFromServerUI();
        if (state == GameState.Error10)
            drawError10UI();
        if (state == GameState.Error11)
            drawError11UI();
        if (state == GameState.ConnectionClosed)
            drawConnectionClosedUI();
        if (state == GameState.CannotConnect)
            drawCannotConnectUI();
        if (state == GameState.Error14)
            drawError14UI();
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
            drawText(g, Integer.toString(remainingTime),
                    160 - Integer.toString(Settings.winScore).length() * 20 / 2,
                    g.getHeight() - 42, DigitsColor.Green);
        }
    }

    private void drawWorld(WorldClient world) {
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

    private void drawWaitingConnectionUI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.connectingToServer, 47, 150);
        g.drawPixmap(Assets.buttons, 0, 416, 64, 64, 128, 128);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawReadyUI() {
        Graphics g = game.getGraphics();

        if (!pressedStartClient)
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

    private void drawError8UI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.error8, 62, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawDisconnectedFromServerUI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.disconnectedFromServer, 12, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawError10UI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.error10, 62, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawError11UI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.error11, 62, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawConnectionClosedUI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.connectionClosed, 12, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawCannotConnectUI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.cannotConnect, 13, 100);
        g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
        g.drawLine(0, 416, 480, 416, Color.BLACK);
    }

    private void drawError14UI() {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.error14, 62, 100);
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

            Message message = handlerToThread.obtainMessage(1);
            message.sendToTarget();

        }

        if (world.gameOver) {
            Settings.addScore(world.score);
            Settings.addScore(world.scoreClient);
            Settings.save(game.getFileIO());
        }


    }

    @Override
    public void resume() {
        if (serverIP != null) {
            if (clientThread == null) {
                clientThread = new Thread(new ClientThread());
                clientThread.start();
            }
        }
    }

    @Override
    public void dispose() {
        try {
            if (socket != null) {
                socket.shutdownInput();
                socket.close();
            }
        } catch (Exception e) {
            state = GameState.Error8;
        }
    }

    public class ClientThread implements Runnable {


        public ClientThread() {
            handlerToThread = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message message) {
                    if (message.what == 0) {
                        if (printWriter != null)
                            printWriter.println("Ready");
                    }
                    if (message.what == 1) {
                        if (printWriter != null)
                            printWriter.println("Paused");
                    }
                    if (message.what == 2) {
                        if (printWriter != null)
                            printWriter.println((String) (message.obj));
                    }
                    if (message.what == 3) {
                        if (printWriter != null)
                            printWriter.println("Running");
                    }
                    if (message.what == 4) {
                        if (printWriter != null)
                            printWriter.println("PrepareNewGame");
                    }
                    if (message.what == 5) {
                        if (printWriter != null)
                            printWriter.println("I Received");
                    }
                }
            };
        }

        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(serverIP);

                socket = new Socket(serverAddr, serverPort);
                try {
                    printWriter = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);

                    disconnected = false;

                    Message message = handlerFromThread.obtainMessage(6);
                    message.sendToTarget();

                    //---get an InputStream object to read from the server---
                    //BufferedReader br = new BufferedReader(
                            //new InputStreamReader(socket.getInputStream()));

                    try {
                        Thread listenThread = new Thread(new Listen(socket));
                        listenThread.start();

                        while (!disconnected);
                        //---disconnected from the server---

                        message = handlerFromThread.obtainMessage(8, "9");
                        message.sendToTarget();
                    } catch (Exception e) {
                        message = handlerFromThread.obtainMessage(8, "10");
                        message.sendToTarget();
                    }

                } catch (Exception e) {
                    Message message = handlerFromThread.obtainMessage(8, "11");
                    message.sendToTarget();
                }
                Message message = handlerFromThread.obtainMessage(8, "12");
                message.sendToTarget();
            } catch (Exception e) {
                Message message = handlerFromThread.obtainMessage(8, "13");
                message.sendToTarget();
            }
        }
    }

    public class Listen implements Runnable {
        Socket server;
        public Listen(Socket server) {
            this.server = server;
        }
        public void run() {
            try {

                BufferedReader br = new BufferedReader(new InputStreamReader(server.getInputStream()));

                String line;
                while ((line = br.readLine()) != null) {
                    final String strReceived = line;
                    if (strReceived.equals("Running")) {
                        Message message = handlerFromThread.obtainMessage(0);
                        message.sendToTarget();
                    }
                    if (strReceived.startsWith("Update")) {
                        Message message = handlerFromThread.obtainMessage(1, strReceived);
                        message.sendToTarget();
                    }
                    if (strReceived.equals("Paused")) {
                        Message message = handlerFromThread.obtainMessage(2);
                        message.sendToTarget();
                    }
                    if (strReceived.equals("GameOver")) {
                        Message message = handlerFromThread.obtainMessage(3);
                        message.sendToTarget();
                    }
                    if (strReceived.equals("ServerWon")) {
                        Message message = handlerFromThread.obtainMessage(4);
                        message.sendToTarget();
                    }
                    if (strReceived.equals("ClientWon")) {
                        Message message = handlerFromThread.obtainMessage(5);
                        message.sendToTarget();
                    }
                    if (strReceived.equals("Bitten")) {
                        Message message = handlerFromThread.obtainMessage(7);
                        message.sendToTarget();
                    }
                    if (strReceived.equals("PrepareNewGame")) {
                        Message message = handlerFromThread.obtainMessage(9);
                        message.sendToTarget();
                    }
                    if (strReceived.startsWith("WinCondition")) {
                        Message message = handlerFromThread.obtainMessage(10, strReceived);
                        message.sendToTarget();
                    }
                    if (strReceived.startsWith("ExitReadyMode")) {
                        Message message = handlerFromThread.obtainMessage(8, "12");
                        message.sendToTarget();
                    }
                    if (strReceived.equals("EatGreen")) {
                        Message message = handlerFromThread.obtainMessage(11);
                        message.sendToTarget();
                    }
                    if (strReceived.equals("EatRed")) {
                        Message message = handlerFromThread.obtainMessage(12);
                        message.sendToTarget();
                    }
                }

                disconnected = true;
            } catch (Exception e) {
                Message message = handlerFromThread.obtainMessage(8, "14");
                message.sendToTarget();
            }
        }
    }
}