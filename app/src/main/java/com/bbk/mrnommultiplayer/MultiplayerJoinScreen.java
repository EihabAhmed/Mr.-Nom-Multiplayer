package com.bbk.mrnommultiplayer;

import android.graphics.Color;

import java.util.List;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Input.KeyEvent;
import com.badlogic.androidgames.framework.Input.TouchEvent;
import com.badlogic.androidgames.framework.Screen;

public class MultiplayerJoinScreen extends Screen {
    String serverIP;

    public MultiplayerJoinScreen(Game game) {
        super(game);
        serverIP = Settings.serverIP;
    }

    @Override
    public void update(float deltaTime) {
        Graphics g = game.getGraphics();
        List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
        List<KeyEvent> keyEvents = game.getInput().getKeyEvents();

        for (int i = 0; i < keyEvents.size(); i++) {
            KeyEvent event = keyEvents.get(i);
            if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
                game.setScreen(new MultiplayerMenuScreen(game));
                if (Settings.soundEnabled)
                    Assets.click.play(1);
                return;
            }
        }

        int len = touchEvents.size();
        for (int i = 0; i < len; i++) {
            TouchEvent event = touchEvents.get(i);
            if (event.type == TouchEvent.TOUCH_UP) {
                if (inBounds(event, 0, g.getHeight() - 64, 64, 64)) {
                    game.setScreen(new MultiplayerMenuScreen(game));
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;
                }
                if (inBounds(event, 16, 130, 288, 63)) {
                    Settings.serverIP = serverIP;
                    game.setScreen(new GameScreenClient(game));
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;
                }

                for (int row = 0; row < 3; row++) {
                    for (int col = 0; col < 3; col++) {
                        if (inBounds(event, 50 + col * 90, 220 + row * 45, 40, 40)) {
                            if (serverIP.length() < 15)
                                serverIP += Integer.toString(row * 3 + col + 1);
                            if (Settings.soundEnabled)
                                Assets.click.play(1);
                            return;
                        }
                    }
                }
                if (inBounds(event, 50, 355, 40, 40)) {
                    if (serverIP.length() < 15)
                        serverIP += ".";
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;
                }
                if (inBounds(event, 140, 355, 40, 40)) {
                    if (serverIP.length() < 15)
                        serverIP += "0";
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;
                }
                if (inBounds(event, 230, 355, 40, 40)) {
                    if (serverIP.length() > 0)
                        serverIP = serverIP.substring(0, serverIP.length() - 1);
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;

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

    @Override
    public void present(float deltaTime) {
        Graphics g = game.getGraphics();

        g.drawPixmap(Assets.background, 0, 0);
        g.drawPixmap(Assets.enterServerIP, 0, 20);
        g.drawRect(10, 80, 305, 35, Color.rgb(200, 200, 200));
        drawText(g, serverIP, 10, 80);
        g.drawPixmap(Assets.multiplayerMenu, 16, 130, 0, 63, 288, 63);

        for (int row = 0, num = 1; row < 4; row++) {
            for (int col = 0; col < 3; col++, num++) {
                g.drawPixmap(Assets.button, 50 + col * 90, 220 + row * 45);

                if (row < 3) {
                    g.drawPixmap(Assets.numbers, 50 + col * 90 + 10, 220 + row * 45 + 4, num * 20, 0, 20, 32);
                }
            }
        }
        g.drawPixmap(Assets.numbers, 50 + 15, 355 + 4, 200, 0, 210, 32);
        g.drawPixmap(Assets.numbers, 140 + 10, 355 + 4, 0, 0, 20, 32);
        g.drawPixmap(Assets.backspace, 230 + 4, 355 + 4);

        g.drawPixmap(Assets.buttons, 0, 416, 64, 64, 128, 128);
    }

    public void drawText(Graphics g, String line, int x, int y) {
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

            g.drawPixmap(Assets.numbers, x, y, srcX, 0, srcWidth, 32);
            x += srcWidth;
        }
    }

    @Override
    public void pause() {
        Settings.save(game.getFileIO());
    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }
}