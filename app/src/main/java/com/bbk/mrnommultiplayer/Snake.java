package com.bbk.mrnommultiplayer;

import java.util.ArrayList;
import java.util.List;

public class Snake {
	public static final int UP = 0;
	public static final int LEFT = 1;
	public static final int DOWN = 2;
	public static final int RIGHT = 3;
	
	public List<SnakePart> parts = new ArrayList<SnakePart> ();
	public int direction;
	
	public Snake() {

	}

	public void init() {
		direction = UP;
		parts.add(new SnakePart(10, 12));
		parts.add(new SnakePart(10, 13));
		parts.add(new SnakePart(10, 14));
	}

	public void initServer() {
		direction = UP;
		parts.add(new SnakePart(15, 19));
		parts.add(new SnakePart(15, 20));
		parts.add(new SnakePart(15, 21));
	}

	public void initClient() {
		direction = DOWN;
		parts.add(new SnakePart(4, 6));
		parts.add(new SnakePart(4, 5));
		parts.add(new SnakePart(4, 4));
	}

	public void turnLeft() {
		direction += 1;
		if (direction > RIGHT)
			direction = UP;
	}
	
	public void turnRight() {
		direction -= 1;
		if (direction < UP)
			direction = RIGHT;
	}
	
	public void eat() {
		SnakePart end = parts.get(parts.size() - 1);
		parts.add(new SnakePart(end.x, end.y));
	}
	
	public void advance() {
		SnakePart head = parts.get(0);
		
		int len = parts.size() -1;
		for (int i = len; i > 0; i--) {
			SnakePart before = parts.get(i - 1);
			SnakePart part = parts.get(i);
			part.x = before.x;
			part.y = before.y;
		}
		
		if (direction == UP)
			head.y -= 1;
		if (direction == LEFT)
			head.x -= 1;
		if (direction == DOWN)
			head.y += 1;
		if (direction == RIGHT)
			head.x += 1;
		
		if (head.x < 0)
			head.x = 19;
		if (head.x > 19)
			head.x = 0;
		if (head.y < 0)
			head.y = 25;
		if (head.y > 25)
			head.y = 0;
	}
	
	public boolean checkBitten(World world) {
		SnakePart head = parts.get(0);

		int len = world.snake.parts.size();
		for (int i = 1; i < len; i++) {
			SnakePart part = world.snake.parts.get(i);
			if (part.x == head.x && part.y == head.y)
				return true;
		}

		if (world instanceof WorldServer) {
			WorldServer worldServer = (WorldServer) world;
			len = worldServer.snakeClient.parts.size();
			for (int i = 1; i < len; i++) {
				SnakePart part = worldServer.snakeClient.parts.get(i);
				if (part.x == head.x && part.y == head.y)
					return true;
			}
		}

		return false;
	}
}