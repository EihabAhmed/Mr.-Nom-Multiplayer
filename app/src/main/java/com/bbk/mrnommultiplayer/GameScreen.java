package com.bbk.mrnommultiplayer;

import java.util.List;

import android.graphics.Color;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Input.KeyEvent;
import com.badlogic.androidgames.framework.Input.TouchEvent;
import com.badlogic.androidgames.framework.Pixmap;
import com.badlogic.androidgames.framework.Screen;

public class GameScreen extends Screen {
	MrNomGame myGame;

	enum GameState {
		Ready,
		Running,
		Paused,
		GameOver
	}
	
	GameState state = GameState.Ready;
	World world;
	int oldScore = 0;
	String score = "0";
	boolean firstTouch = true;

	int touchedX = 0;
	int touchedY = 0;
	boolean moved = true;

	float firstGameOver;
	
	public GameScreen(Game game) {
		super(game);
		world = new World();
		world.init();
		myGame = (MrNomGame)game;
	}

	@Override
	public void update(float deltaTime) {
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
		List<KeyEvent> keyEvents = game.getInput().getKeyEvents();
		
		if (state == GameState.Ready)
			updateReady(touchEvents, keyEvents);
		if (state == GameState.Running)
			updateRunning(touchEvents, keyEvents, deltaTime);
		if (state == GameState.Paused)
			updatePaused(touchEvents);
		if (state == GameState.GameOver)
			updateGameOver(touchEvents, keyEvents);
	}

	private void updateReady(List<TouchEvent> touchEvents, List<KeyEvent> keyEvents) {
		for (int i = 0; i < keyEvents.size(); i++) {
			KeyEvent event = keyEvents.get(i);
			if (event.keyCode == android.view.KeyEvent.KEYCODE_BACK && event.type == KeyEvent.KEY_UP) {
				game.setScreen(new MainMenuScreen(game));
				if (Settings.soundEnabled)
					Assets.click.play(1);
				return;
			}
		}

		/*if (touchEvents.size() > 0)
			state = GameState.Running;*/

		int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
			if (event.type == TouchEvent.TOUCH_UP) {
				if (firstTouch) {
					firstTouch = false;
					return;
				}

				if (inBounds(event, 0, 416, 64, 64)) {
					game.setScreen(new MainMenuScreen(game));
					if (Settings.soundEnabled)
						Assets.click.play(1);
					return;
				}

				state = GameState.Running;
				world.lastBonusTime = System.nanoTime() / 1000000000.0f;
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
				if (Settings.soundEnabled)
					Assets.click.play(1);
				return;
			}
		}

		int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);

			if (event.type == TouchEvent.TOUCH_DOWN) {
//				if (firstTouch) {
//					firstTouch = false;
//					//return;
//				}
//
//				else {
					touchedX = event.x;
					touchedY = event.y;
					moved = false;
//				}

				/*if (Settings.controlMode == Settings.ControlMode.Arrows) {
					if (event.x < 64 && event.y > 416) {
						world.snake.turnLeft();
					}
					if (event.x > 256 && event.y > 416) {
						world.snake.turnRight();
					}
				} else if (Settings.controlMode == Settings.ControlMode.Touch) {
					if (firstTouch) {
						firstTouch = false;
					} else {
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
					}
				}*/
			}

			if (event.type == TouchEvent.TOUCH_DRAGGED) {
//				if (firstTouch) {
//					firstTouch = false;
//					//return;
//				}
//
//				else {
					if (!moved) {
						//return;

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
//				}
			}

			if (event.type == TouchEvent.TOUCH_UP) {
				/*if (event.x < 64 && event.y < 64) {
					if (Settings.soundEnabled)
						Assets.click.play(1);
					state = GameState.Paused;
					return;
				}*/

				/*if (firstTouch) {
					firstTouch = false;
					//return;
				}

				else {
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
					touchedY = 0;
				}*/
			}
		}
		
		world.update(deltaTime);
		if (world.gameOver) {
			if (Settings.soundEnabled)
				Assets.bitten.play(1);
			state = GameState.GameOver;
			firstGameOver = System.nanoTime() / 1000000000.0f;
		}
		if (oldScore != world.score) {
			oldScore = world.score;
			score = "" + oldScore;
			if (!world.eatGreen && !world.eatRed)
				if (Settings.soundEnabled)
					Assets.eat.play(1);
		}
		if (world.eatGreen) {
			if (Settings.soundEnabled)
				Assets.emmm.play(1);
			world.eatGreen = false;
		}
		if (world.eatRed) {
			if (Settings.soundEnabled)
				Assets.eo3.play(1);
			world.eatRed = false;
		}
	}

	private void updatePaused(List<TouchEvent> touchEvents) {
		int len = touchEvents.size();
		for (int i = 0; i < len; i++) {
			TouchEvent event = touchEvents.get(i);
			if (event.type == TouchEvent.TOUCH_UP) {
				if (event.x > 80 && event.x <= 240) {
					if (event.y  > 100 && event.y  <= 148) {
						if (Settings.soundEnabled)
							Assets.click.play(1);
						state = GameState.Running;
						world.lastBonusTime = System.nanoTime() / 1000000000.0f - world.timeElapsedSinceLastBonus;
						return;
					}
					if (event.y > 148 && event.y < 196) {
						if (Settings.soundEnabled)
							Assets.click.play(1);
						game.setScreen(new MainMenuScreen(game));
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

					game.setScreen(new MainMenuScreen(game));
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

						game.setScreen(new MainMenuScreen(game));
						if (Settings.soundEnabled)
							Assets.click.play(1);
						return;
					}
				}
			}
		}
	}

	@Override
	public void present(float deltaTime) {
		Graphics g = game.getGraphics();
		
		g.drawPixmap(Assets.background, 0, 0);
		drawWorld(world);

		if (state == GameState.Ready)
			drawReadyUI();
		if (state == GameState.Running)
			drawRunningUI();
		if (state == GameState.Paused)
			drawPausedUI();
		if (state == GameState.GameOver)
			drawGameOverUI();
		
		drawText(g, score, g.getWidth() / 2 - score.length() * 20 / 2, g.getHeight() - 42);
	}

	private void drawWorld(World world) {
		Graphics g = game.getGraphics();
		Snake snake = world.snake;
		SnakePart head = snake.parts.get(0);

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
	}

	private void drawReadyUI() {
		Graphics g = game.getGraphics();
		
		g.drawPixmap(Assets.ready, 47, 100);
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
		
		g.drawPixmap(Assets.pause, 80, 100);
		g.drawLine(0, 416, 480, 416, Color.BLACK);
	}

	private void drawGameOverUI() {
		Graphics g = game.getGraphics();
		
		g.drawPixmap(Assets.gameOver, 62, 100);
		if (System.nanoTime() / 1000000000.0f - firstGameOver >= 2)
			g.drawPixmap(Assets.buttons, 128, 200, 0, 128, 64, 64);
		g.drawLine(0, 416, 480, 416, Color.BLACK);
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
		if (state == GameState.Running)
			state = GameState.Paused;
		
		if (world.gameOver) {
			Settings.addScore(world.score);
			Settings.save(game.getFileIO());
		}
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void dispose() {
		
	}
}