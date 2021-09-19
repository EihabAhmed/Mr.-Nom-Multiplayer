package com.bbk.mrnommultiplayer;

import com.badlogic.androidgames.framework.Game;
import com.badlogic.androidgames.framework.Graphics;
import com.badlogic.androidgames.framework.Screen;
import com.badlogic.androidgames.framework.Graphics.PixmapFormat;

public class LoadingScreen extends Screen {
	public LoadingScreen(Game game) {
		super(game);
	}

	@Override
	public void update(float deltaTime) {
		Graphics g = game.getGraphics();
		Assets.background = g.newPixmap("background.png", PixmapFormat.RGB565);
		Assets.logo = g.newPixmap("logo.png", PixmapFormat.ARGB4444);
		Assets.mainMenu = g.newPixmap("mainmenu.png", PixmapFormat.ARGB4444);
		Assets.multiplayerMenu = g.newPixmap("multiplayermenu.png", PixmapFormat.ARGB4444);
		Assets.enterServerIP = g.newPixmap("enterserverip.png", PixmapFormat.ARGB4444);
		Assets.serverIP = g.newPixmap("serverip.png", PixmapFormat.ARGB4444);
		Assets.buttons = g.newPixmap("buttons.png", PixmapFormat.ARGB4444);
		Assets.button = g.newPixmap("button.png", PixmapFormat.ARGB4444);
		Assets.backspace = g.newPixmap("backspace.png", PixmapFormat.ARGB4444);
		Assets.help1 = g.newPixmap("help1.png", PixmapFormat.ARGB4444);
		Assets.help2 = g.newPixmap("help2.png", PixmapFormat.ARGB4444);
		Assets.help3 = g.newPixmap("help3.png", PixmapFormat.ARGB4444);
		Assets.help4 = g.newPixmap("help4.png", PixmapFormat.ARGB4444);
		Assets.help5 = g.newPixmap("help5.png", PixmapFormat.ARGB4444);
		Assets.help6 = g.newPixmap("help6.png", PixmapFormat.ARGB4444);
		Assets.numbers = g.newPixmap("numbers.png", PixmapFormat.ARGB4444);
		Assets.numbersRed = g.newPixmap("numbersred.png", PixmapFormat.ARGB4444);
		Assets.numbersBlue = g.newPixmap("numbersblue.png", PixmapFormat.ARGB4444);
		Assets.numbersGreen = g.newPixmap("numbersgreen.png", PixmapFormat.ARGB4444);
		Assets.waitingClient = g.newPixmap("waitingclient.png", PixmapFormat.ARGB4444);
		Assets.connectingToServer = g.newPixmap("connectingtoserver.png", PixmapFormat.ARGB4444);
		Assets.waitingStart = g.newPixmap("waitingstart.png", PixmapFormat.ARGB4444);
		Assets.ready = g.newPixmap("ready.png", PixmapFormat.ARGB4444);
		Assets.pause = g.newPixmap("pausemenu.png", PixmapFormat.ARGB4444);
		Assets.pause2 = g.newPixmap("pausemenu2.png", PixmapFormat.ARGB4444);
		Assets.gameOver = g.newPixmap("gameover.png", PixmapFormat.ARGB4444);
		Assets.serverWon = g.newPixmap("serverwon.png", PixmapFormat.ARGB4444);
		Assets.clientWon = g.newPixmap("clientwon.png", PixmapFormat.ARGB4444);
		Assets.clientDisconnected = g.newPixmap("clientdisconnected.png", PixmapFormat.ARGB4444);
		Assets.error1 = g.newPixmap("error1.png", PixmapFormat.ARGB4444);
		Assets.noConnection = g.newPixmap("noconnection.png", PixmapFormat.ARGB4444);
		Assets.error3 = g.newPixmap("error3.png", PixmapFormat.ARGB4444);
		Assets.serverExited = g.newPixmap("serverexited.png", PixmapFormat.ARGB4444);
		Assets.error5 = g.newPixmap("error5.png", PixmapFormat.ARGB4444);
		Assets.error6 = g.newPixmap("error6.png", PixmapFormat.ARGB4444);
		Assets.error7 = g.newPixmap("error7.png", PixmapFormat.ARGB4444);
		Assets.error8 = g.newPixmap("error8.png", PixmapFormat.ARGB4444);
		Assets.disconnectedFromServer = g.newPixmap("disconnectedfromserver.png", PixmapFormat.ARGB4444);
		Assets.error10 = g.newPixmap("error10.png", PixmapFormat.ARGB4444);
		Assets.error11 = g.newPixmap("error11.png", PixmapFormat.ARGB4444);
		Assets.connectionClosed = g.newPixmap("connectionclosed.png", PixmapFormat.ARGB4444);
		Assets.cannotConnect = g.newPixmap("cannotconnect.png", PixmapFormat.ARGB4444);
		Assets.error14 = g.newPixmap("error14.png", PixmapFormat.ARGB4444);
		Assets.timeOut = g.newPixmap("timeout.png", PixmapFormat.ARGB4444);
		Assets.selectGameType = g.newPixmap("selectgametype.png", PixmapFormat.ARGB4444);
		Assets.radioButtons = g.newPixmap("radiobuttons.png", PixmapFormat.ARGB4444);
		Assets.winScore = g.newPixmap("winscore.png", PixmapFormat.ARGB4444);
		Assets.gameTime = g.newPixmap("gametime.png", PixmapFormat.ARGB4444);
		Assets.upDown = g.newPixmap("updown.png", PixmapFormat.ARGB4444);
		Assets.headUp = g.newPixmap("headup.png", PixmapFormat.ARGB4444);
		Assets.headLeft = g.newPixmap("headleft.png", PixmapFormat.ARGB4444);
		Assets.headDown = g.newPixmap("headdown.png", PixmapFormat.ARGB4444);
		Assets.headRight = g.newPixmap("headright.png", PixmapFormat.ARGB4444);
		Assets.tail = g.newPixmap("tail.png", PixmapFormat.ARGB4444);
		Assets.headUpClient = g.newPixmap("headupclient.png", PixmapFormat.ARGB4444);
		Assets.headLeftClient = g.newPixmap("headleftclient.png", PixmapFormat.ARGB4444);
		Assets.headDownClient = g.newPixmap("headdownclient.png", PixmapFormat.ARGB4444);
		Assets.headRightClient = g.newPixmap("headrightclient.png", PixmapFormat.ARGB4444);
		Assets.tailClient = g.newPixmap("tailclient.png", PixmapFormat.ARGB4444);
		Assets.stain1 = g.newPixmap("stain1.png", PixmapFormat.ARGB4444);
		Assets.stain2 = g.newPixmap("stain2.png", PixmapFormat.ARGB4444);
		Assets.stain3 = g.newPixmap("stain3.png", PixmapFormat.ARGB4444);
		Assets.stain1Red = g.newPixmap("stain1red.png", PixmapFormat.ARGB4444);
		Assets.stain2Red = g.newPixmap("stain2red.png", PixmapFormat.ARGB4444);
		Assets.stain3Red = g.newPixmap("stain3red.png", PixmapFormat.ARGB4444);
		Assets.stain1Green = g.newPixmap("stain1green.png", PixmapFormat.ARGB4444);
		Assets.stain2Green = g.newPixmap("stain2green.png", PixmapFormat.ARGB4444);
		Assets.stain3Green = g.newPixmap("stain3green.png", PixmapFormat.ARGB4444);
		Assets.bbkLogo = g.newPixmap("bbklogo.png", PixmapFormat.ARGB4444);

		Assets.click = game.getAudio().newSound("click.ogg");
		Assets.eat = game.getAudio().newSound("eat.ogg");
		Assets.eo3 = game.getAudio().newSound("eo3.ogg");
		Assets.emmm = game.getAudio().newSound("emmm.ogg");
		Assets.bitten = game.getAudio().newSound("bitten.ogg");
		Assets.chime = game.getAudio().newSound("chime.ogg");

		Settings.load(game.getFileIO());
		game.setScreen(new MainMenuScreen(game));
	}

	@Override
	public void present(float deltaTime) {

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
