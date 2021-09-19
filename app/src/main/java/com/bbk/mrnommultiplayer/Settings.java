package com.bbk.mrnommultiplayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.badlogic.androidgames.framework.FileIO;

public class Settings {
	public static boolean soundEnabled = true;
	public static int[] highscores = new int[] {100, 80, 50, 30, 10};
	
	/*public enum ControlMode {
		Arrows,
		Touch
	}
	
	public static ControlMode controlMode = ControlMode.Arrows;*/

	public enum WinCondition {
		Score,
		Time
	}

	public static WinCondition winCondition = WinCondition.Score;

	public static int winScore = 100;
	public static int gameTime = 60;

	public static String serverIP = "";
	
	public static void load(FileIO files) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(files.readFile(".mrnom")));
			soundEnabled = Boolean.parseBoolean(in.readLine());
			for (int i = 0; i < 5; i++) {
				highscores[i] = Integer.parseInt(in.readLine());
			}
			/*int ctrl = Integer.parseInt(in.readLine());
			if (ctrl == 0)
				controlMode = ControlMode.Arrows;
			else if (ctrl == 1)
				controlMode = ControlMode.Touch;*/
		} catch (IOException e) {
			// :( It's ok we have defaults
		} catch (NumberFormatException e) {
			// :/ It's ok, defaults save our day
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
			}
		}
	}
	
	public static void save(FileIO files) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(files.writeFile(".mrnom")));
			out.write(Boolean.toString(soundEnabled));
			for (int i = 0; i < 5; i++) {
				out.write("\n");
				out.write(Integer.toString(highscores[i]));
			}
			out.write("\n");
			/*if (controlMode == ControlMode.Arrows)
				out.write("0");
			else if (controlMode == ControlMode.Touch)
				out.write("1");*/
		} catch (IOException e) {
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
			}
		}
	}
	
	public static void addScore(int score) {
		for (int i = 0; i < 5; i++) {
			if (highscores[i] < score) {
				for (int j = 4; j > i; j--)
					highscores[j] = highscores[j - 1];
				highscores[i] = score;
				break;
			}
		}
	}
}