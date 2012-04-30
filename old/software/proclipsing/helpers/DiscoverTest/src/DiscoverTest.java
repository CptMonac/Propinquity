import java.util.ArrayList;

import processing.core.PApplet;
import processing.serial.*;
import xbee.*;


public class DiscoverTest extends PApplet {

	final int MODE_CHECK_SERIAL = 0;
	final int MODE_CHECK_XPAN = 1;
	final int MODE_SEND_DISCOVER = 2;
	final int MODE_RECEIVE_DISCOVER = 3;
	final int MODE_CHECKS_DONE = 4;
	final int MODE_RUNNING = 5;
	int mode = MODE_CHECK_SERIAL;

	final int XBEE_DISCOVER_TIMEOUT = 5000; // 5 sec

	Player[] players;

	ArrayList<String> foundProxs;
	ArrayList<String> foundVibes;
	ArrayList<String> foundUndefs;

	public static DiscoverTest game;
	public GameState gamestate;
	
	public void setup() {
		game = this;
		gamestate = new GameState(this);

		// 1. scan test for local xbees 
		XBeeManager.instance().init();

		// 2. discover test for remote xbees
		foundProxs = new ArrayList<String>();
		foundVibes = new ArrayList<String>();
		foundUndefs = new ArrayList<String>();
	}


	public void draw() {
		int startDiscover = 0; // time when discover package is sent

		// scan test for local xbees via serial
		if (mode == MODE_CHECK_SERIAL && XBeeManager.instance().hasAllNIs()) {
			println("Local XBees found: " + XBeeManager.instance().getNodeIDs() + ".");
			mode++;
		}
		// set up networks for all local xbees
		else if (mode == MODE_CHECK_XPAN) {
			initPlayers();
			mode++;
		}
		// discover remote xbees
		else if (mode == MODE_SEND_DISCOVER) {
			println("Scanning for remote xbees...");
			println("Player 1:");
			gamestate.players[0].discoverRemoteXbees();
			println("Player 2:");
			gamestate.players[1].discoverRemoteXbees();
			startDiscover = millis();
			mode++;
		}
		else if (mode == MODE_RECEIVE_DISCOVER) {
			if (millis()-startDiscover <= XBEE_DISCOVER_TIMEOUT) {
				print(".");
				delay(1000); // 1 sec
			}
			else {
				mode++;
				// TODO: record - which xbees have we??
			}
		}
		else if (mode == MODE_CHECKS_DONE) {
			println("");
			println("Checks done");
			printDiscovered();

			/*
			for (int i=0;i<2;i++) {
				XPan xpan = gamestate.players[i].getProxXpan();
					if (xpan != null) {
						xpan.broadcastVibe(1000, 0);
						int rgb[] = {0, 0, 255};
						if (i == 0) {
	  						xpan.sendOutgoing(1, xpan.getProxStatePacket(true, rgb, 1000, 0));
	  						xpan.sendOutgoing(2, xpan.getProxStatePacket(true, rgb, 1000, 0));
						}
						else {
	  						xpan.sendOutgoing(9, xpan.getProxStatePacket(true, rgb, 1000, 0));
	  						xpan.sendOutgoing(10, xpan.getProxStatePacket(true, rgb, 1000, 0));
						}
				}
				xpan = gamestate.players[i].getVibeXpan();
					if (xpan != null) {
						xpan.broadcastVibe(200, 0);
					}
			}
			*/
			mode++;
		}
		else if (mode == MODE_RUNNING) {
			gamestate.update();
			render(gamestate);
		}
	}

	private void render(GameState gamestate) {
		
		
		
		
	}


	void printDiscovered() {
		println("Discovered proximity patches");
		for(int i=0; i<foundProxs.size() ; i++)
			println(foundProxs.get(i));
		println("Discovered vibration gloves");
		for(int i=0; i<foundVibes.size() ; i++)
			println(foundVibes.get(i));
		println("Discovered undefined remote xbee senders");
		for(int i=0; i<foundUndefs.size() ; i++)
			println(foundUndefs.get(i));
	}

	void initPlayers() {
		gamestate.players = new Player[2];
		gamestate.players[0] = new Player();
		gamestate.players[1] = new Player();

		Player.init();
		
		gamestate.players[0].vibeXpan = Player.address_to_xpan.get(5);
		gamestate.players[0].proxXpan = Player.address_to_xpan.get(1);
		gamestate.players[1].vibeXpan = Player.address_to_xpan.get(13);
		gamestate.players[1].proxXpan = Player.address_to_xpan.get(9);
		
	}

	void xBeeDiscoverEvent(XBeeReader xbee) {
		XBeeDataFrame data = xbee.getXBeeReading();
		println("Received ApiId " + data.getApiID());
		if (data.getApiID() != XBeeReader.ATCOMMAND_RESPONSE) return;
		data.parseXBeeRX16Frame();

		int[] buffer = data.getBytes();

		if (buffer.length > 11) {
			//check first letter of NI parameter
			String name = "";
			for(int i = 11; i < buffer.length; i++)
				name += (char)buffer[i];

			switch (buffer[11]) {
			case 'P':
				foundProxs.add(name);
				println(" Found proximity patch: " + name + " at "+millis());
				break;
			case 'V':
				foundVibes.add(name);
				println(" Found vibration patch: " + name + " at "+millis());
				break;
			default:
				foundUndefs.add(name);
				println(" Found undefined patch: " + name + " at "+millis());
				break;
			}
		}
	}

	public void xBeeEvent(XBeeReader xbee) {
		if (mode == MODE_CHECK_SERIAL) {
			XBeeManager.instance().xBeeEvent(xbee);
		}
		else if (mode == MODE_RECEIVE_DISCOVER) {
			xBeeDiscoverEvent(xbee);
		}
		else if (mode == MODE_RUNNING) {
			gamestate.xBeeEvent(xbee);
		}
	}

	public void keyPressed() {
		if (mode == MODE_RUNNING) {
		  switch (key) {
		  	case '1' : // set vibe mapping
		  		gamestate.setVibeForAll(200, 128);
		  		break;
		  	case '2' :
		  		gamestate.setVibeForAll(500, 128);
		  		break;
		  	case '3' :
		  		gamestate.setVibeForAll(1000, 64);
		  		break;
		  	case '4' :
		  		gamestate.setVibeForAll(1000, 128);
		  		break;
		  	case '5' :
		  		gamestate.setVibeForAll(2000, 128);
		  		break;
		  	case '6' :
		  		gamestate.setVibeForAll(2000, 255);
		  		break;
		  	case 'r' : // set color
		  		System.out.println("Set everything red.");
		  		int[] red = {255,0,0};
		  		gamestate.setPatchColorForAll(red);
		  		break;
		  	case 'g' : // set color
		  		System.out.println("Set everything green.");
		  		int[] green = {0,255,0};
		  		gamestate.setPatchColorForAll(green);
		  		break;
		  	case 'b' : // set color
		  		System.out.println("Set everything blue.");
		  		int[] blue = {0,0,255};
		  		gamestate.setPatchColorForAll(blue);
		  		break;
		  	case 'a' :
		  		// toggle patch 1 player 1
		  		System.out.println("Toggle patch 1 player 1.");
		  		gamestate.activatePatch(1, !gamestate.isPatchActive(1));
		  		break;
		  	case 's' :
		  		// toggle patch 2 player 1
		  		System.out.println("Toggle patch 2 player 1.");
		  		gamestate.activatePatch(2, !gamestate.isPatchActive(2));
		  		break;
		  	case 'd' :
		  		// toggle patch 3 player 1
		  		System.out.println("Toggle patch 3 player 1.");
		  		gamestate.activatePatch(3, !gamestate.isPatchActive(3));
		  		break;
		  	case 'j' :
		  		// toggle patch 1 player 2
		  		System.out.println("Toggle patch 1 player 2.");
		  		gamestate.activatePatch(9, !gamestate.isPatchActive(9));
		  		break;
		  	case 'k' :
		  		// toggle patch 2 player 2
		  		System.out.println("Toggle patch 2 player 2.");
		  		gamestate.activatePatch(10, !gamestate.isPatchActive(10));
		  		break;
		  	case 'l' :
		  		// toggle patch 3 player 2
		  		System.out.println("Toggle patch 3 player 2.");
		  		gamestate.activatePatch(11, !gamestate.isPatchActive(11));
		  		break;
		    case 'q':
		    	// stop vibe and stop blink
		    	gamestate.setVibeForAll(0,0);
		  		delay(1000); // 1 sec so the packets can be transmitted for sure
		    	int[] noColor = {0,0,0};
		  		gamestate.setPatchColorForAll(noColor);
		  		delay(1000); // 1 sec so the packets can be transmitted for sure
		    	System.exit(0);
		    	break;      
		  }
		}
	}
	
	
	public static void main(String _args[]) {
		PApplet.main(new String[] { DiscoverTest.class.getName() });
	}
}
