package com.bbk.mrnommultiplayer;

import java.util.List;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Input.KeyEvent;
import com.badlogic.androidgames.framework.Input.TouchEvent;
import com.badlogic.androidgames.framework.Screen;

public class SelectWinConditionScreen extends Screen {
    enum DigitsColor {
        Blue,
        Green
    }

    public SelectWinConditionScreen(Game game) {
        super(game);
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
                if (inBounds(event, 16, 350, 288, 63)) {
                    game.setScreen(new GameScreenServer(game));
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;
                }
            }
            if (event.type == TouchEvent.TOUCH_DOWN) {
                if (inBounds(event, 20, 85, 246, 50)) {
                    Settings.winCondition = Settings.WinCondition.Score;
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;
                }
                if (inBounds(event, 20, 225, 246, 50)) {
                    Settings.winCondition = Settings.WinCondition.Time;
                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;
                }

                if (inBounds(event, 160, 135, 50, 30)) {
                    Settings.winCondition = Settings.WinCondition.Score;
                    if (Settings.winScore < 200)
                        Settings.winScore += 10;

                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;
                }
                if (inBounds(event, 160, 165, 50, 30)) {
                    Settings.winCondition = Settings.WinCondition.Score;
                    if (Settings.winScore > 10)
                        Settings.winScore -= 10;

                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;
                }

                if (inBounds(event, 160, 275, 50, 30)) {
                    Settings.winCondition = Settings.WinCondition.Time;
                    if (Settings.gameTime < 120)
                        Settings.gameTime += 5;

                    if (Settings.soundEnabled)
                        Assets.click.play(1);
                    return;
                }
                if (inBounds(event, 160, 305, 50, 30)) {
                    Settings.winCondition = Settings.WinCondition.Time;
                    if (Settings.gameTime > 5)
                        Settings.gameTime -= 5;

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
        g.drawPixmap(Assets.selectGameType, 0, 20);

        if (Settings.winCondition == Settings.WinCondition.Score) {
            g.drawPixmap(Assets.radioButtons, 20, 90, 0, 0, 35, 35);
            g.drawPixmap(Assets.radioButtons, 20, 230, 0, 35, 35, 35);
        } else {
            g.drawPixmap(Assets.radioButtons, 20, 90, 0, 35, 35, 35);
            g.drawPixmap(Assets.radioButtons, 20, 230, 0, 0, 35, 35);
        }

        g.drawPixmap(Assets.winScore, 70, 85);
        drawText(g, Integer.toString(Settings.winScore), 100, 145, DigitsColor.Blue);
        g.drawPixmap(Assets.upDown, 160, 135);

        g.drawPixmap(Assets.gameTime, 70, 225);
        drawText(g, Integer.toString(Settings.gameTime), 100, 285, DigitsColor.Green);
        g.drawPixmap(Assets.upDown, 160, 275);

        g.drawPixmap(Assets.multiplayerMenu, 16, 350, 0, 0, 288, 63);

        g.drawPixmap(Assets.buttons, 0, 416, 64, 64, 128, 128);
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

            if (digitsColor == DigitsColor.Blue)
                g.drawPixmap(Assets.numbersBlue, x, y, srcX, 0, srcWidth, 32);
            else if (digitsColor == DigitsColor.Green)
                g.drawPixmap(Assets.numbersGreen, x, y, srcX, 0, srcWidth, 32);
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