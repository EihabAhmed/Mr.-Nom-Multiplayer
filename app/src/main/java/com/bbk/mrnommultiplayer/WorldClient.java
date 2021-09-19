package com.bbk.mrnommultiplayer;

public class WorldClient extends World {
    public Snake snakeClient;
    public int scoreClient = 0;

    public WorldClient() {
    }

    public void init() {
        snake = new Snake();
        snake.initServer();
        snakeClient = new Snake();
        snakeClient.initClient();
    }
}