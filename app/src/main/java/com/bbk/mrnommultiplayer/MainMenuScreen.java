package com.bbk.mrnommultiplayer;

import android.content.Intent;
import android.net.Uri;

import java.util.List;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Input.KeyEvent;
import com.badlogic.androidgames.framework.Input.TouchEvent;
import com.badlogic.androidgames.framework.Screen;
import com.badlogic.androidgames.framework.impl.AndroidGame;

public class MainMenuScreen extends Screen {
	enum Buttons {
		None,
		Website
	}

	Buttons buttonPressed = Buttons.None;
	Buttons buttonHover = Buttons.None;

	AndroidGame myGame;

	public MainMenuScreen(Game game) {
		super(game);
		myGame = (AndroidGame) game;
	}

	@Override
	public void update(float deltaTime) {
		Graphics g = game.getGraphics();
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		List<KeyEvent> keyEvents = game.getInput().getKeyEvents();

		for (int i = 0; i < keyEvents.size(); i++) {
			KeyEvent event = keyEvents.get(i);
			if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
				myGame.finish();
				if (Settings.soundEnabled)
					Assets.click.play(1);
				return;
			}
		}

		int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);

			if (event.type == TouchEvent.TOUCH_DOWN) {
				if (event.pointer == 0) {
					if (buttonPressed == Buttons.None) {
						if (inBounds(event, g.getWidth() - 75, g.getHeight() - 120, 75, 120)) {
							buttonPressed = Buttons.Website;
							buttonHover = Buttons.Website;
						}
					}

					if (inBounds(event, 0, g.getHeight() - 64, 64, 64)) {
						Settings.soundEnabled = !Settings.soundEnabled;
						if (Settings.soundEnabled)
							Assets.click.play(1);
					}
					if (inBounds(event, 64, 220, 192, 42)) {
						game.setScreen(new GameScreen(game));
						if (Settings.soundEnabled)
							Assets.click.play(1);
						return;
					}
					if (inBounds(event, 64, 220 + 42, 192, 42)) {
						game.setScreen(new MultiplayerMenuScreen(game));
						if (Settings.soundEnabled)
							Assets.click.play(1);
						return;
					}
					if (inBounds(event, 64, 220 + 42 * 2, 192, 42)) {
						game.setScreen(new HighscoreScreen(game));
						if (Settings.soundEnabled)
							Assets.click.play(1);
						return;
					}
					if (inBounds(event, 64, 220 + 42 * 3, 192, 42)) {
						game.setScreen(new HelpScreen(game));
						if (Settings.soundEnabled)
							Assets.click.play(1);
						return;
					}
					/*if (inBounds(event, 64, 220 + 126, 192, 42)) {
						game.setScreen(new HelpScreen(game));
						if (Settings.soundEnabled)
							Assets.click.play(1);
						return;
					}*/
				}
			}

			if (event.type == TouchEvent.TOUCH_DRAGGED) {
				if (event.pointer == 0) {
					buttonHover = Buttons.None;
					if (inBounds(event, g.getWidth() - 75, g.getHeight() - 120, 75, 120)) {
						if (buttonPressed == Buttons.Website)
							buttonHover = Buttons.Website;
					}
				}
			}

			if (event.type == TouchEvent.TOUCH_UP) {
				if (event.pointer == 0) {
					if (inBounds(event, g.getWidth() - 75, g.getHeight() - 120, 75, 120)) {
						if (buttonPressed == Buttons.Website) {
							if (Settings.soundEnabled)
								Assets.click.play(1);
							openWebsite();
						}
					}

					buttonPressed = Buttons.None;
					buttonHover = Buttons.None;
				}
			}
		}
	}

	private void openWebsite() {
		Intent websiteIntent = new Intent();
		websiteIntent.setAction(Intent.ACTION_VIEW);
		websiteIntent.setData(Uri.parse("https://www.facebook.com/bbksoftware"));
		myGame.startActivity(websiteIntent);
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
		g.drawPixmap(Assets.logo, 32, 20);
		//g.drawPixmap(Assets.mainMenu, 64, 230, 0, 42, 192, 126);
		g.drawPixmap(Assets.mainMenu, 64, 220);
		if (Settings.soundEnabled)
			g.drawPixmap(Assets.buttons, 0, 416, 0, 0, 64, 64);
		else
			g.drawPixmap(Assets.buttons, 0, 416, 64, 0, 64, 64);

		if (buttonPressed == Buttons.Website && buttonHover == Buttons.Website)
			g.drawPixmap(Assets.bbkLogo, g.getWidth() - 75, g.getHeight() - 120, 0, 120, 75, 120);
		else
			g.drawPixmap(Assets.bbkLogo, g.getWidth() - 75, g.getHeight() - 120, 0, 0, 75, 120);
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