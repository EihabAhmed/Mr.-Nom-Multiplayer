package com.bbk.mrnommultiplayer;

public class WorldServer extends World {
    public Snake snakeClient;
    public int scoreClient = 0;

    public boolean serverWon = false;
    public boolean clientWon = false;
    public boolean bitten = false;

    public long startTime;
    public long elapsedTime = 0;

    public WorldServer() {

    }

    public void init() {
        snake = new Snake();
        snake.initServer();
        snakeClient = new Snake();
        snakeClient.initClient();
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

        len = snakeClient.parts.size();
        for (int i = 0; i < len; i++) {
            SnakePart part = snakeClient.parts.get(i);
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

        len = snakeClient.parts.size();
        for (int i = 0; i < len; i++) {
            SnakePart part = snakeClient.parts.get(i);
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
            int chooseTargetSnake = random.nextInt(2);
            Snake targetSnake = null;
            
            if (chooseTargetSnake == 0) {
                targetSnake = snake;
            } else if (chooseTargetSnake == 1) {
                targetSnake = snakeClient;
            }
            
            if (targetSnake.direction == Snake.UP) {
                bonusStainX = targetSnake.parts.get(0).x;
                bonusStainY = targetSnake.parts.get(0).y - 5;
                if (bonusStainY < 0)
                    bonusStainY += WORLD_HEIGHT;
            }
            if (targetSnake.direction == Snake.DOWN) {
                bonusStainX = targetSnake.parts.get(0).x;
                bonusStainY = targetSnake.parts.get(0).y + 5;
                if (bonusStainY >= WORLD_HEIGHT)
                    bonusStainY -= WORLD_HEIGHT;
            }
            if (targetSnake.direction == Snake.RIGHT) {
                bonusStainX = targetSnake.parts.get(0).x + 5;
                bonusStainY = targetSnake.parts.get(0).y;
                if (bonusStainX >= WORLD_WIDTH)
                    bonusStainX -= WORLD_WIDTH;
            }
            if (targetSnake.direction == Snake.LEFT) {
                bonusStainX = targetSnake.parts.get(0).x - 5;
                bonusStainY = targetSnake.parts.get(0).y;
                if (bonusStainX < 0)
                    bonusStainX += WORLD_WIDTH;
            }

            while (true) {
                if (fields[bonusStainX][bonusStainY] == false)
                    break;

                if (targetSnake.direction == Snake.UP) {
                    bonusStainY--;
                    if (bonusStainY < 0)
                        bonusStainY += WORLD_HEIGHT;
                }
                if (targetSnake.direction == Snake.DOWN) {
                    bonusStainY++;
                    if (bonusStainY >= WORLD_HEIGHT)
                        bonusStainY -= WORLD_HEIGHT;
                }
                if (targetSnake.direction == Snake.RIGHT) {
                    bonusStainX++;
                    if (bonusStainX >= WORLD_WIDTH)
                        bonusStainX -= WORLD_WIDTH;
                }
                if (targetSnake.direction == Snake.LEFT) {
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

        if (Settings.winCondition == Settings.WinCondition.Time) {
            elapsedTime = System.nanoTime() - startTime;

            if (elapsedTime / 1000000000 >= Settings.gameTime) {
                if (score > scoreClient)
                    serverWon = true;
                else if (scoreClient > score)
                    clientWon = true;
                else
                    gameOver = true;

                return;
            }
        }

        tickTime += deltaTime;

        while (tickTime > tick) {
            tickTime -= tick;
            snake.advance();
            snakeClient.advance();
            /*if (snake.checkBitten(this) || snakeClient.checkBitten(this)) {
                gameOver = true;
                return;
            }*/
            SnakePart head = snake.parts.get(0);
            SnakePart headClient = snakeClient.parts.get(0);

            boolean removeStain = false;

            if (head.x == stain.x && head.y == stain.y) {
                score += SCORE_INCREMENT;
                if (Settings.winCondition == Settings.WinCondition.Score && score >= Settings.winScore) {
                    serverWon = true;
                    return;
                }

                snake.eat();
                if (snake.parts.size() == WORLD_WIDTH * WORLD_HEIGHT) {
                    gameOver = true;
                    return;
                } else {
                    removeStain = true;
                }

//                if ((score + scoreClient) % 100 == 0 && tick - TICK_DECREMENT > 0) {
//                    tick -= TICK_DECREMENT;
//                }
            }

            if (headClient.x == stain.x && headClient.y == stain.y) {
                scoreClient += SCORE_INCREMENT;
                if (Settings.winCondition == Settings.WinCondition.Score && scoreClient >= Settings.winScore) {
                    clientWon = true;
                    return;
                }
                snakeClient.eat();
                if (snakeClient.parts.size() == WORLD_WIDTH * WORLD_HEIGHT) {
                    gameOver = true;
                    return;
                } else {
                    removeStain = true;
                }

//                if ((score + scoreClient) % 100 == 0 && tick - TICK_DECREMENT > 0) {
//                    tick -= TICK_DECREMENT;
//                }
            }

            if (removeStain)
                placeStain();

            if (bonusStain != null) {
                removeStain = false;

                if (head.x == bonusStain.x && head.y == bonusStain.y) {
                    if (bonusStain.bonusType == BonusStain.GREEN_STAIN) {
                        score += SCORE_INCREMENT * 3;
                        if (Settings.winCondition == Settings.WinCondition.Score && score >= Settings.winScore) {
                            serverWon = true;
                            return;
                        }
                        snake.eat();
                        if (snake.parts.size() == WORLD_WIDTH * WORLD_HEIGHT) {
                            gameOver = true;
                            return;
                        }

//                        if (score % 100 == 0 || score % 100 == 10 || score % 100 == 20) {
//                            if (tick - TICK_DECREMENT > 0) {
//                                tick -= TICK_DECREMENT;
//                            }
//                        }
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

//                    bonusStain = null;
//                    lastBonusTime = System.nanoTime() / 1000000000.0f;
                    removeStain = true;
                }

                if (headClient.x == bonusStain.x && headClient.y == bonusStain.y) {
                    if (bonusStain.bonusType == BonusStain.GREEN_STAIN) {
                        scoreClient += SCORE_INCREMENT * 3;
                        if (Settings.winCondition == Settings.WinCondition.Score && scoreClient >= Settings.winScore) {
                            clientWon = true;
                            return;
                        }
                        snakeClient.eat();
                        if (snakeClient.parts.size() == WORLD_WIDTH * WORLD_HEIGHT) {
                            gameOver = true;
                            return;
                        }

//                        if (scoreClient % 100 == 0 || scoreClient % 100 == 10 || scoreClient % 100 == 20) {
//                            if (tick - TICK_DECREMENT > 0) {
//                                tick -= TICK_DECREMENT;
//                            }
//                        }
                        eatGreen = true;
                    }

                    if (bonusStain.bonusType == BonusStain.RED_STAIN) {
                        if (scoreClient > 0) {
                            scoreClient -= SCORE_INCREMENT;
                            if (scoreClient % 20 == 0 && snakeClient.parts.size() > 3)
                                snakeClient.parts.remove(snakeClient.parts.size() - 1);
                        }
                        eatRed = true;
                    }

//                    bonusStain = null;
//                    lastBonusTime = System.nanoTime() / 1000000000.0f;
                    removeStain = true;
                }

                if (removeStain) {
                    bonusStain = null;
                    lastBonusTime = System.nanoTime() / 1000000000.0f;
                }
            }

            if (snake.checkBitten(this)) {
                if (score > 0) {
                    score -= SCORE_INCREMENT;
                    if (score % 20 == 0 && snake.parts.size() > 3)
                        snake.parts.remove(snake.parts.size() - 1);
                }
                bitten = true;
            }

            if (snakeClient.checkBitten(this)) {
                if (scoreClient > 0) {
                    scoreClient -= SCORE_INCREMENT;
                    if (scoreClient % 20 == 0 && snakeClient.parts.size() > 3)
                        snakeClient.parts.remove(snakeClient.parts.size() - 1);
                }
                bitten = true;
            }

            if (head.x == headClient.x && head.y == headClient.y) {
                if (score > 0) {
                    score -= SCORE_INCREMENT;
                    if (score % 20 == 0 && snake.parts.size() > 3)
                        snake.parts.remove(snake.parts.size() - 1);
                }
                if (scoreClient > 0) {
                    scoreClient -= SCORE_INCREMENT;
                    if (scoreClient % 20 == 0 && snakeClient.parts.size() > 3)
                        snakeClient.parts.remove(snakeClient.parts.size() - 1);
                }
                bitten = true;
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
        }
    }
}

