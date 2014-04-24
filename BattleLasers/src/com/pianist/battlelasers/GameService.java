package com.pianist.battlelasers;

import java.util.ArrayList;

public class GameService
{
	private static GameService _instance = new GameService();
	
	private ArrayList <Move> moves;
	private boolean update;
	
	public static GameService getInstance () {
		return _instance;
	}
	
	private void checkForUpdate () {
		
	}
	

}
