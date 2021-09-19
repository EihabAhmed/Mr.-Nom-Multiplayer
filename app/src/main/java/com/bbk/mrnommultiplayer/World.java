package com.bbk.mrnommultiplayer;

import java.util.Random;

public class World {
	static final int WORLD_WIDTH = 20;
	static final int WORLD_HEIGHT = 26;
	static final int SCORE_INCREMENT = 10;
	static final float TICK_INITIAL = 0.2f;
	static final float TICK_DECREMENT = 0.03f;
	
	public Snake snake;
	public Stain stain;
	public BonusStain bonusStain = null;
	public boolean gameOver = false;
	public int score = 0;
	boolean fields[][] = new boolean[WORLD_WIDTH][WORLD_HEIGHT];
	Random random = new Random();
	float tickTime = 0;
	float tick = TICK_INITIAL;

	float lastBonusTime;
	float timeElapsedSinceLastBonus;
	boolean eatGreen = false;
	boolean eatRed = false;
	
	public World() {

	}

	public void init() {
		snake = new Snake();
		snake.init();
		placeStain();
	}

	protected void placeStain() {
		for (int x = 0; x < WORLD_WIDTH; x++) {
			for (int y = 0; y < WORLD_HEIGHT; y++) {
				fields[x][y] = false;
			}
		}
		
		int len = snake.parts.size();
		for (int i = 0; i < len; i++) {
			SnakePart part = snake.parts.get(i);
			fields[part.x][part.y] = true; 
		}
		
		int stainX = random.nextInt(WORLD_WIDTH);
		int stainY = random.nextInt(WORLD_HEIGHT);
		while (true) {
			if (fields[stainX][stainY] == false)
				break;
			stainX += 1;
			if (stainX >= WORLD_WIDTH) {
				stainX = 0;
				stainY += 1;
				if (stainY >= WORLD_HEIGHT) {
					stainY = 0;
				}
			}
		}
		stain = new Stain(stainX, stainY, random.nextInt(3));
	}

	protected void placeBonusStain() {
		for (int x = 0; x < WORLD_WIDTH; x++) {
			for (int y = 0; y < WORLD_HEIGHT; y++) {
				fields[x][y] = false;
			}
		}

		int len = snake.parts.size();
		for (int i = 0; i < len; i++) {
			SnakePart part = snake.parts.get(i);
			fields[part.x][part.y] = true;
		}
		fields[stain.x][stain.y] = true;

		int bonusType = random.nextInt(2);

		int bonusStainX = 0;
		int bonusStainY = 0;

		if (bonusType == BonusStain.GREEN_STAIN) {
			bonusStainX = random.nextInt(WORLD_WIDTH);
			bonusStainY = random.nextInt(WORLD_HEIGHT);
			while (true) {
				if (fields[bonusStainX][bonusStainY] == false)
					break;
				bonusStainX += 1;
				if (bonusStainX >= WORLD_WIDTH) {
					bonusStainX = 0;
					bonusStainY += 1;
					if (bonusStainY >= WORLD_HEIGHT) {
						bonusStainY = 0;
					}
				}
			}
		} else if (bonusType == BonusStain.RED_STAIN) {
			if (snake.direction == Snake.UP) {
				bonusStainX = snake.parts.get(0).x;
				bonusStainY = snake.parts.get(0).y - 5;
				if (bonusStainY < 0)
					bonusStainY += WORLD_HEIGHT;
			}
			if (snake.direction == Snake.DOWN) {
				bonusStainX = snake.parts.get(0).x;
				bonusStainY = snake.parts.get(0).y + 5;
				if (bonusStainY >= WORLD_HEIGHT)
					bonusStainY -= WORLD_HEIGHT;
			}
			if (snake.direction == Snake.RIGHT) {
				bonusStainX = snake.parts.get(0).x + 5;
				bonusStainY = snake.parts.get(0).y;
				if (bonusStainX >= WORLD_WIDTH)
					bonusStainX -= WORLD_WIDTH;
			}
			if (snake.direction == Snake.LEFT) {
				bonusStainX = snake.parts.get(0).x - 5;
				bonusStainY = snake.parts.get(0).y;
				if (bonusStainX < 0)
					bonusStainX += WORLD_WIDTH;
			}

			while (true) {
				if (fields[bonusStainX][bonusStainY] == false)
					break;

				if (snake.direction == Snake.UP) {
					bonusStainY--;
					if (bonusStainY < 0)
						bonusStainY += WORLD_HEIGHT;
				}
				if (snake.direction == Snake.DOWN) {
					bonusStainY++;
					if (bonusStainY >= WORLD_HEIGHT)
						bonusStainY -= WORLD_HEIGHT;
				}
				if (snake.direction == Snake.RIGHT) {
					bonusStainX++;
					if (bonusStainX >= WORLD_WIDTH)
						bonusStainX -= WORLD_WIDTH;
				}
				if (snake.direction == Snake.LEFT) {
					bonusStainX--;
					if (bonusStainX < 0)
						bonusStainX += WORLD_WIDTH;
				}
			}
		}

		bonusStain = new BonusStain(bonusStainX, bonusStainY, random.nextInt(3), bonusType);
	}
	
	public void update(float deltaTime) {
		if (gameOver)
			return;
		
		tickTime += deltaTime;
		
		while (tickTime > tick) {
			tickTime -= tick;
			snake.advance();
			if (snake.checkBitten(this)) {
				gameOver = true;
				return;
			}

			if (System.nanoTime() / 1000000000.0f - lastBonusTime >= 10) {
				if (random.nextBoolean()) {
					placeBonusStain();
					lastBonusTime = System.nanoTime() / 1000000000.0f + 5;
				} else {
					lastBonusTime += 5;
				}
			}

			if (System.nanoTime() / 1000000000.0f - lastBonusTime >= 0 &&
					System.nanoTime() / 1000000000.0f - lastBonusTime < 10) {
				bonusStain = null;
			}
			
			SnakePart head = snake.parts.get(0);
			if (head.x == stain.x && head.y == stain.y) {
				score += SCORE_INCREMENT;
				snake.eat();
				if (snake.parts.size() == WORLD_WIDTH * WORLD_HEIGHT) {
					gameOver = true;
					return;
				} else {
					placeStain();
				}
				
				if (score % 100 == 0 && tick - TICK_DECREMENT > 0) {
					tick -= TICK_DECREMENT;
				}
			}

			if (bonusStain != null) {
				if (head.x == bonusStain.x && head.y == bonusStain.y) {
					if (bonusStain.bonusType == BonusStain.GREEN_STAIN) {
						score += SCORE_INCREMENT * 3;
						snake.eat();
						if (snake.parts.size() == WORLD_WIDTH * WORLD_HEIGHT) {
							gameOver = true;
							return;
						}

						if (score % 100 == 0 || score % 100 == 10 || score % 100 == 20) {
							if (tick - TICK_DECREMENT > 0) {
								tick -= TICK_DECREMENT;
							}
						}
						eatGreen = true;
					}

					if (bonusStain.bonusType == BonusStain.RED_STAIN) {
						if (score > 0) {
							score -= SCORE_INCREMENT;
							if (score % 20 == 0 && snake.parts.size() > 3)
								snake.parts.remove(snake.parts.size() - 1);
						}
						eatRed = true;
					}

					bonusStain = null;
					lastBonusTime = System.nanoTime() / 1000000000.0f;
				}
			}
		}
	}
}