package propinquity;

import java.util.Vector;

import controlP5.Button;
import controlP5.CVector3f;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Textfield;

import processing.core.*;

public class PlayerList implements UIElement {

	final int MAX_PLAYERS = 12;

	final int WIDTH = 200;
	final int NEXT_WIDTH = 50;
	final int NEXT_HEIGHT = 20;
	final int NEW_WIDTH = 100;
	final int NEW_HEIGHT = 20;
	final int PLAYER_HEIGHT = 20;
	final int REMOVE_WIDTH = 12;
	final int REMOVE_HEIGHT = 20;
	final int VERT_SPACER = 20;
	final int NEXT_ID = 3;
	final int NEW_ID = 4;

	Propinquity parent;

	String plistFile;

	ControlP5 controlP5;

	String[] playerNames;
	Vector<Textfield> playerFields;
	Vector<Button> removeButtons;
	Button nextButton;
	Button newButton;

	boolean isVisible;

	public PlayerList(Propinquity parent) {
		this(parent, null);
	}

	public PlayerList(Propinquity parent, String plistFile) {
		this.parent = parent;
		this.plistFile = plistFile;

		isVisible = true;
		controlP5 = new ControlP5(parent);

		// create text fields for each
		playerFields = new Vector<Textfield>();
		removeButtons = new Vector<Button>();

		// load the player list
		// playerList = new Vector();
		String[] players = parent.loadStrings(plistFile);
		System.out.println(players);
		if(players != null) {
			for(int i = 0; i < players.length; i++) {
				if(players[i].length() > 0) {
					// add UI
					addPlayer(players[i]);
				}
			}
		}

		if(playerFields.size() < 1)
			addPlayer("Player 1");
		if(playerFields.size() < 2)
			addPlayer("Player 2");

		// create button to add new players
		newButton = controlP5.addButton("NEW PLAYER", 0, parent.width / 2 - WIDTH / 2, parent.height / 2, NEW_WIDTH,
				NEW_HEIGHT);
		newButton.setId(NEW_ID);

		// create next button
		nextButton = controlP5.addButton("NEXT", 0, parent.width / 2 + WIDTH / 2 - NEXT_WIDTH, parent.height / 2,
				NEXT_WIDTH, NEXT_HEIGHT);
		nextButton.setId(NEXT_ID);

		layout();
	}

	public String[] getNames() {
		return playerNames;
	}

	public void draw() {

	}

	void addPlayer(String name) {
		// add a new text field to the list
		Textfield playerField = controlP5.addTextfield("Player " + playerFields.size(), parent.width / 2 - WIDTH / 2,
				parent.height / 2, WIDTH - REMOVE_WIDTH * 2, PLAYER_HEIGHT);
		playerField.setAutoClear(false);
		playerField.setText(name);
		playerField.setCaptionLabel("Player " + (playerFields.size() + 1));
		playerField.setFocus(true);

		// add a matching remove button
		Button removeBtn = controlP5.addButton("Remove " + playerFields.size(), playerFields.size(), parent.width / 2
				+ WIDTH / 2 - REMOVE_WIDTH, parent.height / 2, REMOVE_WIDTH, REMOVE_HEIGHT);
		removeBtn.setCaptionLabel("x");

		playerFields.add(playerField);
		removeButtons.add(removeBtn);
	}

	public void layout() {
		float y = parent.height / 2 - (PLAYER_HEIGHT + VERT_SPACER) * (playerFields.size() + 1) / 2;

		// move existing player fields up
		CVector3f pos = new CVector3f(0, 0, 0);
		Textfield tf;
		Button btn;
		for(int i = 0; i < playerFields.size(); i++) {
			tf = playerFields.get(i);
			pos = tf.position();
			tf.setPosition(pos.x, y);

			btn = removeButtons.get(i);
			pos = btn.position();
			btn.setPosition(pos.x, y);

			y += PLAYER_HEIGHT + VERT_SPACER;
		}

		y += VERT_SPACER;

		// move buttons down
		pos = newButton.position();
		newButton.setPosition(pos.x, y);
		pos = nextButton.position();
		nextButton.setPosition(pos.x, y);

		if(playerFields.size() >= MAX_PLAYERS) newButton.hide();
		else newButton.show();
	}

	public void process() {
		// clear empty textfields
		for(int i = 0; i < playerFields.size(); i++) {
			Textfield tf = playerFields.get(i);
			if(tf.getText().length() == 0) {
				playerFields.remove(i);
				i--;
			}
		}

		// save player list
		playerNames = new String[PApplet.max(playerFields.size(), 2)];
		playerNames[0] = "Player 1";
		playerNames[1] = "Player 2";

		for(int i = 0; i < playerFields.size(); i++)
			playerNames[i] = playerFields.get(i).getText();

		parent.saveStrings(parent.dataPath(plistFile), playerNames);

		parent.changeGameState(GameState.LevelSelect);
	}

	public void show() {
		isVisible = true;
		controlP5.show();
	}

	public void hide() {
		isVisible = false;
		controlP5.hide();
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void controlEvent(ControlEvent theEvent) {
		if(isVisible) {
			switch(theEvent.controller().id()) {
			case (NEXT_ID):
				process();
				break;
			case (NEW_ID):
				addPlayer("");
				layout();

				break;
			default:
				if(theEvent.controller() instanceof Button && theEvent.controller().name().indexOf("Remove") >= 0) {
					// remove textfield that matches button value
					int value = (int) theEvent.controller().value();
					int i;

					controlP5.Controller ctrl;
					ctrl = controlP5.controller("Player " + value);
					i = playerFields.indexOf(ctrl);
					playerFields.remove(i);
					ctrl.hide();
					controlP5.remove(ctrl.name());

					// remove button itself
					ctrl = controlP5.controller("Remove " + value);
					removeButtons.remove(i);
					ctrl.hide();
					controlP5.remove(ctrl.name());

					// adjust values
					for(; i < playerFields.size(); i++) {
						Textfield rtf = playerFields.get(i);
						rtf.setCaptionLabel("Player " + (i + 1));
					}

					layout();
				}
				break;
			}
		}
	}

	public void keyPressed(int keycode) {
		if(isVisible && keycode == PConstants.ENTER)
			process();
	}
}
