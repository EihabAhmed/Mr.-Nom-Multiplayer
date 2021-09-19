package com.bbk.mrnommultiplayer;

import java.util.List;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Screen;
import com.badlogic.androidgames.framework.Input.KeyEvent;
import com.badlogic.androidgames.framework.Input.TouchEvent;

public class HelpScreen2 extends Screen {
	public HelpScreen2(Game game) {
		super(game);
	}

	@Override
	public void update(float deltaTime) {
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		List<KeyEvent> keyEvents = game.getInput().getKeyEvents();

		for (int i = 0; i < keyEvents.size(); i++) {
			KeyEvent event = keyEvents.get(i);
			if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
				game.setScreen(new HelpScreen(game));
				if (Settings.soundEnabled)
					Assets.click.play(1);
				return;
			}
		}
		
		int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
			if (event.type == TouchEvent.TOUCH_UP) {
				if (event.x > 256 && event.y > 416) {
					game.setScreen(new HelpScreen3(game));
					if (Settings.soundEnabled)
						Assets.click.play(1);
					return;
				}
			}
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		g.drawPixmap(Assets.background, 0, 0);
		g.drawPixmap(Assets.help2, 64, 100);
		g.drawPixmap(Assets.buttons, 256, 416, 0, 64, 64, 64);
	}

	@Override
	public void pause() {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void dispose() {
		
	}
}