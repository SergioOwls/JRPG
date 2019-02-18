package Game.Entities.Dynamics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import Game.GameStates.InWorldState;
import Game.GameStates.PauseState;
import Game.GameStates.State;
import Game.World.Walls;
import Game.World.InWorldAreas.CaveArea;
import Game.World.InWorldAreas.InWorldWalls;
import Main.GameSetUp;
import Main.Handler;
import java.awt.event.KeyEvent;

public class Player extends BaseDynamicEntity {

	private Rectangle player;
	private boolean canMove;
	public static boolean checkInWorld;
	public static final int InMapWidth = 25, InMapHeight = 25, InAreaWidth = 70, InAreaHeight = 70;
	private int currentWidth, currentHeight;
	public static boolean isinArea = false;

	public Player(Handler handler, int xPosition, int yPosition) {
		super(handler, yPosition, yPosition);

		this.xPosition = xPosition;
		this.yPosition = yPosition;

		currentWidth = InMapWidth;
		currentHeight = InMapHeight;

		player = new Rectangle();
		checkInWorld = false;
	}

	@Override
	public void tick() {
		super.tick();
		UpdateNextMove();
		PlayerInput();

		if (State.getState().equals(handler.getGame().inWorldState)) {
			checkInWorld = true;
		} else {
			checkInWorld = false;
		}

		if (this.handler.getKeyManager().keyJustPressed(KeyEvent.VK_L))
			System.out.println(" x: " + this.getXOffset() + " y: " + this.getYOffset());
	}

	@Override
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		player = new Rectangle((int) xPosition, (int) yPosition, currentWidth, currentHeight);

		g2.setColor(Color.RED);
		g2.fill(player);

		if (GameSetUp.DEBUGMODE) {
			g2.draw(nextArea);
		}
	}

	private void UpdateNextMove() {
		switch (facing) {
		case "Up":
			nextArea = new Rectangle((int) xPosition, (int) yPosition - speed, currentWidth, currentHeight / 2);

			break;
		case "Down":
			nextArea = new Rectangle((int) xPosition, (int) yPosition + currentHeight, currentWidth, speed);

			break;
		case "Left":
			nextArea = new Rectangle((int) xPosition - speed, (int) yPosition, speed, currentHeight);

			break;
		case "Right":
			nextArea = new Rectangle((int) xPosition + currentWidth, (int) yPosition, speed, currentHeight);

			break;
		}
	}

	private void PlayerInput() {

		canMove = true;

		if (handler.getKeyManager().keyJustPressed(KeyEvent.VK_ESCAPE)) {
			PauseState.lastState = State.getState(); // Saves the current State to later go back to it.
			State.setState(handler.getGame().pauseState);
		} else {

			if (handler.getKeyManager().runbutt) {
				speed = 2;
			} else {
				speed = 8;
			}

			CheckForWalls();

			if (handler.getKeyManager().down & canMove) {
				Move(false, -speed);
				facing = "Down";
			} else if (handler.getKeyManager().up & canMove) {
				Move(false, speed);
				facing = "Up";
			} else if (handler.getKeyManager().right & canMove) {
				Move(true, -speed);
				facing = "Right";
			} else if (handler.getKeyManager().left & canMove) {
				Move(true, speed);
				facing = "Left";
			}
		}

	}

	private void PushPlayerBack() {

		canMove = false;
		switch (facing) {
		case "Down":
			Move(false, 1);
			break;
		case "Up":
			Move(false, -1);
			break;
		case "Right":
			Move(true, 1);
			break;
		case "Left":
			Move(true, -1);
			break;
		}
	}

	private void CheckForWalls() {

		if (!checkInWorld) {
			for (Walls w : handler.getWorldManager().getWalls()) {

				if (nextArea.intersects(w)) {

					if (w.getType().equals("Wall")) {
						PushPlayerBack();
					}

					else if (w.getType().startsWith("Door")) {
						canMove = true;

						if (w.getType().equals("Door Cave")) {
							InWorldState.caveArea.oldPlayerXCoord = (int) (handler.getXDisplacement());
							InWorldState.caveArea.oldPlayerYCoord = (int) (handler.getYDisplacement());
							CaveArea.isInCave = true;
							setWidthAndHeight(InAreaWidth, InAreaHeight);
							handler.setXInWorldDisplacement(CaveArea.playerXSpawn);
							handler.setYInWorldDisplacement(CaveArea.playerYSpawn);
							State.setState(handler.getGame().inWorldState.setArea(InWorldState.caveArea));
						}

						if (w.getType().equals("Door S")) {

							InWorldState.SArea.oldPlayerXCoord = (int) (handler.getXDisplacement());
							InWorldState.SArea.oldPlayerYCoord = (int) (handler.getYDisplacement());
							this.isinArea = true;
							setWidthAndHeight(InMapWidth, InMapHeight);
							State.setState(handler.getGame().inWorldState.setArea(InWorldState.SArea));
						}
					}

				}
			}
		} else

		{
			if (CaveArea.isInCave) {
				for (InWorldWalls iw : CaveArea.caveWalls) {
					if (iw.intersects(player)) {
						if (iw.getType().equals("Wall"))
							PushPlayerBack();
						else {

							if (iw.getType().equals("Start Exit")) {

								handler.setXDisplacement(handler.getXDisplacement() - 450); // Sets the player x/y outside the
								handler.setYDisplacement(handler.getYDisplacement() + 380); // Cave

							} else if (iw.getType().equals("End Exit")) {

								handler.setXDisplacement(InWorldState.caveArea.oldPlayerXCoord);// Sets the player x/y
								handler.setYDisplacement(InWorldState.caveArea.oldPlayerYCoord);// outside theCave
							}
							
							setWidthAndHeight(InMapWidth, InMapHeight);
							State.setState(handler.getGame().mapState);
							CaveArea.isInCave = false;

						}
					}
				}
			}

			else if (Player.isinArea) {

				for (InWorldWalls iw : InWorldState.SArea.getWalls()) {

					if (iw.intersects(player)) {
						if (iw.getType().equals("Wall"))
							PushPlayerBack();

					}
				}
			}
		}
	}

	/**
	 * 
	 * @param XorY  where true is X and false is Y
	 * @param speed
	 */
	private void Move(boolean XorY, int speed) {

		if (!checkInWorld) {
			if (XorY) {
				handler.setXDisplacement(handler.getXDisplacement() + speed);
			} else {
				handler.setYDisplacement(handler.getYDisplacement() + speed);
			}
		} else {
			if (XorY) {
				handler.setXInWorldDisplacement((handler.getXInWorldDisplacement() + speed));
			} else {
				handler.setYInWorldDisplacement(handler.getYInWorldDisplacement() + speed);
			}

		}

	}

	@Override
	public Rectangle getCollision() {
		return player;
	}

	/**
	 * !!!!!!!!!TO REDESIGN OR DELETE!!!!!!!
	 * 
	 * 
	 * Called when the player has collided with another static entity. Used to push
	 * the player back from passing through a static entity.
	 *
	 * @param collidedXPos the xPosition the static entity is located at.
	 */
	public void WallBoundary(double collidedXPos) {

		int playerXPos = Math.abs(handler.getXDisplacement());

		if (playerXPos < collidedXPos / 2) {
			handler.setXDisplacement(handler.getXDisplacement() + 2);
		} else if (playerXPos > collidedXPos / 2) {
			handler.setXDisplacement(handler.getXDisplacement() - 2);
		}
	}

	/*
	 * Although the TRUE Player position is in the middle of the screen, these two
	 * methods give us the value as if the player was part of the world.
	 */
	@Override
	public double getXOffset() {

		if (!checkInWorld)
			return -this.handler.getXDisplacement() + xPosition;
		else
			return -this.handler.getXInWorldDisplacement() + xPosition;
	}

	@Override
	public double getYOffset() {

		if (!checkInWorld)
			return -this.handler.getYDisplacement() + yPosition;
		else
			return -this.handler.getYInWorldDisplacement() + yPosition;
	}

	public void setWidthAndHeight(int newWidth, int newHeight) {
		this.currentWidth = newWidth;
		this.currentHeight = newHeight;
	}
}
