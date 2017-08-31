package bullethell.applet;

import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

class GameEnvironment {	
	static int boundX;
	static int boundY;
	static BufferedImage bossBackground;
	static BufferedImage secretBackground;
	
	Player player;
	Level level;
	
	int mouseX;
	int mouseY;
	
	int bossClearedColorIdx;
	long bossClearedTextTimer;
	// central x, y coordinates of the 10 upgrade buttons
	int[] upButtonCoorX;
	int[] upButtonCoorY;
	// half-width and half-height of the 10 upgrade buttons
	int upButtonW;
	int upButtonH;
	
	// central x, y coordinates of the stage selection buttons
	int[] stageButtonCoorX;
	int[] stageButtonCoorY;
	int stageButtonW;
	int stageButtonH;
	
	// central x, y coordinates of the difficulty selection buttons
	int[] difButtonCoorX;
	int[] difButtonCoorY;
	int difButtonW;
	int difButtonH;
	int selectedDifficulty; // ranges from 1 to 5
	
	// central x, y coordinates of the reset button
	int resetButtonCoorX;
	int resetButtonCoorY;
	int resetButtonW;
	int resetButtonH;
	boolean confirmingReset;
	
	boolean showInstructions;
	
	int[] gameBkStarsX;
	int[] gameBkStarsY;
	int[] gameBkStarsV;
	
	DLList<GameBackgroundStar> bkstars;
	long bkstarsTimer;
	long bkstarsInterval;
	
	int titleColor;
	long titleColorTimer;
	long titleColorInterval;
	
	AudioClip[] music; // music[0] is boss music, music[1] is stage selection music, music[2] is stage music	
	boolean[] musicPlaying; // musicPlaying[0] is true if boss music is playing, musicPlaying[1] is true if stage selection music is playing, musicPlaying[2] is true if stage music is playing
	int musicType;
	
	static void setFieldDim(int bx, int by){
		boundX=bx;
		boundY=by;
	}

	// Utility functions
	static double vectorLength(double x, double y){
		return Math.sqrt(x*x+y*y);
	}
	
	GameEnvironment(){
		player=new Player();
		level=new Level();
		
		upButtonW=120;
		upButtonH=40;		
		upButtonCoorX=new int[10];
		upButtonCoorY=new int[10];
		upButtonCoorX[0]=boundX/2-upButtonW*4-20;upButtonCoorX[5]=upButtonCoorX[0];
		upButtonCoorX[1]=boundX/2-upButtonW*2-10;upButtonCoorX[6]=upButtonCoorX[1];
		upButtonCoorX[2]=boundX/2;upButtonCoorX[7]=upButtonCoorX[2];
		upButtonCoorX[3]=boundX/2+upButtonW*2+10;upButtonCoorX[8]=upButtonCoorX[3];
		upButtonCoorX[4]=boundX/2+upButtonW*4+20;upButtonCoorX[9]=upButtonCoorX[4];
		upButtonCoorY[0]=boundY-upButtonH*3-40;upButtonCoorY[1]=upButtonCoorY[0];upButtonCoorY[2]=upButtonCoorY[0];upButtonCoorY[3]=upButtonCoorY[0];upButtonCoorY[4]=upButtonCoorY[0];
		upButtonCoorY[5]=boundY-upButtonH-30;upButtonCoorY[6]=upButtonCoorY[5];upButtonCoorY[7]=upButtonCoorY[5];upButtonCoorY[8]=upButtonCoorY[5];upButtonCoorY[9]=upButtonCoorY[5];
		
		stageButtonW=20;
		stageButtonH=stageButtonW;
		stageButtonCoorX=new int[Level.MAXLEVEL];
		stageButtonCoorY=new int[Level.MAXLEVEL];		
		int xcor=145+stageButtonW;
		int ycor=180+stageButtonH;
		for(int i=1;i<=stageButtonCoorX.length;i++){
			stageButtonCoorX[i-1]=xcor;stageButtonCoorY[i-1]=ycor;
			xcor=xcor+stageButtonW*2+21;
			if(i%16==0){
				xcor=145+stageButtonW;
				ycor=ycor+stageButtonW*2+21;
			}
		}
		
		difButtonW=90;
		difButtonH=40;
		difButtonCoorX=new int[5];
		difButtonCoorY=new int[5];
		difButtonCoorX[0]=boundX/2-difButtonW*4-100;
		difButtonCoorX[1]=boundX/2-difButtonW*2-50;
		difButtonCoorX[2]=boundX/2;
		difButtonCoorX[3]=boundX/2+difButtonW*2+50;
		difButtonCoorX[4]=boundX/2+difButtonW*4+100;
		difButtonCoorY[0]=400;
		difButtonCoorY[1]=difButtonCoorY[0];difButtonCoorY[2]=difButtonCoorY[0];difButtonCoorY[3]=difButtonCoorY[0];difButtonCoorY[4]=difButtonCoorY[0];
		
		resetButtonW=55;
		resetButtonH=10;
		resetButtonCoorX=GameEnvironment.boundX-resetButtonW-10;
		resetButtonCoorY=resetButtonH+5;
		confirmingReset=false;
		
		selectedDifficulty=5;
		
		showInstructions=false;
		
		bkstars=new DLList<GameBackgroundStar>();	
		bkstarsTimer=0;
		bkstarsInterval=300;
		
		titleColor=0;
		titleColorTimer=0;
		titleColorInterval=1000;	
		
	/*	try {
			URL is=getClass().getResource("bossBackground.jpg");
		//	bossBackground=ImageIO.read(is);
			is=getClass().getResource("secretBackground.jpg");
			secretBackground=ImageIO.read(is);
		} catch (IOException e) {
			bossBackground=new BufferedImage(GameEnvironment.boundX, GameEnvironment.boundY, BufferedImage.TYPE_3BYTE_BGR);
		} */
		
		music=new AudioClip[3];
		musicPlaying=new boolean[music.length];
		musicType=-1;
	}
	
	/* Following two are the two main methods for this class. The first one processes all events for one game frame,
	 * and the second paints everything to the rendering screen. processOneFrame is called repeated in the main game loop,
	 * while renderOneFrame() is called in the public void paint() method in the applet
	 */
	void processOneFrame(int mouseX, int mouseY){
		this.mouseX=mouseX;this.mouseY=mouseY;
		if(level.levelStarted){
			if(level.bossLevel)
				musicType=0;
			else
				musicType=2;
			level.update(player.x, player.y, player);
			if(!level.gameover)
				player.update(mouseX, mouseY, level);
		}else{
			musicType=1;
		}
	}
	
	void renderOneFrame(Graphics g){
		if(!level.levelStarted){			
			renderStageSelectionScreen(g);
		}else{
			renderGameBackground(g);
			level.render(player, g);
			if(level.gameover)
				renderGameoverText(g);
			else if(level.gamewon)
				renderGamewonText(g);	
			else if(level.bossCleared)
				renderBossClearedText(g);
		}
	}
	
	// Called by the applet whenever mouse is released
	void processLevelSelectionFrame(){
		performUpgrade();
		int selectedLevel=clickedLevel();
		selectDifficulty();
		resetGame();
		if(selectedLevel!=-1){
			initLevel(selectedLevel);
		}
	}	
	
	private void performUpgrade(){
		int upsID=-1;
		for(int i=0;i<upButtonCoorX.length;i++)
			if(mouseOverButton(upButtonCoorX[i], upButtonCoorY[i], upButtonW, upButtonH))
				upsID=i;
		if(upsID==-1 || player.ups[upsID]==player.upgradeCosts.length)
			return;
		int cost=player.upgradeCosts[player.ups[upsID]];
		if(cost<=player.credit){
			player.ups[upsID]++;
			player.credit-=cost;
		}
	}
	
	private int clickedLevel(){
		for(int i=0;i<stageButtonCoorX.length;i++)
			if(i<=level.maxlevelCleared && mouseOverButton(stageButtonCoorX[i], stageButtonCoorY[i], stageButtonW, stageButtonH))
				return i+1;
		return -1;
	}
	
	private void selectDifficulty(){
		for(int i=0;i<difButtonCoorX.length;i++)
			if(mouseOverButton(difButtonCoorX[i], difButtonCoorY[i], difButtonW, difButtonH))
				selectedDifficulty=i+1;
	}
	
	private void resetGame(){
		if(mouseOverButton(resetButtonCoorX, resetButtonCoorY, resetButtonW, resetButtonH)){
			if(!confirmingReset){
				confirmingReset=true;
			}else{
				confirmingReset=false;
				Level.bossClearedOnHell=false;
				level.maxlevelCleared=0;
				player.credit=0;
				for(int i=0;i<player.ups.length;i++)
					player.ups[i]=0;
			}
		}else{
			confirmingReset=false;
		}
	}
	
	private void initLevel(int level){
		player.initPlayerForNewLevel();
		this.level.initLevel(level, selectedDifficulty);
	}
	
	private void renderGameBackground(Graphics g){
		if(level.bossLevel){
			if(level.bossCleared)
				g.drawImage(bossBackground, 0, 0, null);
			return;
		}
		if(Level.bossClearedOnHell){
			g.drawImage(secretBackground, 0, 0, null);
			return;
		}
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, boundX, boundY);
		g.setColor(Color.WHITE);
		if(System.currentTimeMillis()-bkstarsTimer>bkstarsInterval){
			bkstarsTimer=System.currentTimeMillis();
			bkstars.add(new GameBackgroundStar());
		}
		DLNode<GameBackgroundStar> n=bkstars.head;
		while(n!=null){
			n.e.update();
			if(n.e.outOfScreen())
				bkstars.remove(n);
			n.e.render(g);
			n=n.next;
		}
	}
	
	private void renderGameoverText(Graphics g){
		g.setColor(Color.RED);
		g.setFont(new Font("Courier New", Font.BOLD, 72));
		g.drawString("GAME OVER", 420, 390);
	}
	
	private void renderGamewonText(Graphics g){
		g.setColor(new Color(0, 255, 200, 255));
		g.setFont(new Font("Courier New", Font.PLAIN, 210));
		g.drawString("LEVEL" , 310, 280);
		g.drawString("CLEARED!", 150, 510);
	}
	
	private void renderBossClearedText(Graphics g){
		switch(bossClearedColorIdx){
			case 0: g.setColor(new Color(255, 0, 0, 255));break;
			case 1: g.setColor(new Color(255, 127, 0, 255));break;
			case 2: g.setColor(new Color(255, 255, 0, 255));break;
			case 3: g.setColor(new Color(0, 255, 0, 255));break;
			case 4: g.setColor(new Color(0, 255, 255, 255));break;
			case 5: g.setColor(new Color(0, 0, 255, 255));break;
			case 6: g.setColor(new Color(127, 0, 255, 255));break;
			default:g.setColor(new Color(255, 255, 255, 255));break;			
		}
		if(System.currentTimeMillis()-bossClearedTextTimer>300){
			bossClearedColorIdx=(bossClearedColorIdx+1)%8;
			bossClearedTextTimer=System.currentTimeMillis();
		}
		g.setFont(new Font("Times New Roman", Font.BOLD, 100));
		g.drawString("CONGRATULATIONS!" , 105, 170);
		g.drawString("YOU HAVE RESTORED", 80, 300);
		g.drawString("OUR COLORFUL WORLD!", 10, 430);
		g.drawString("YOU ROCK!!!", 330, 560);
	}
	
	private void renderStageSelectionScreen(Graphics g){
		renderGameBackground(g);
		renderInstructionButton(g);
		if(showInstructions){
			renderInstructions(g);
		}else{		
			renderTitle(g);
			renderStageSelectionButtons(g);
			renderUpgradeButtons(g);
			renderDifficultySelectionButtons(g);
			renderResetButton(g);
		}
	}
	
	private void renderTitle(Graphics g){
		if(System.currentTimeMillis()-titleColorTimer>titleColorInterval){
			titleColorTimer=System.currentTimeMillis();
			titleColor=(titleColor+1)%7;
		}
		switch(titleColor){
			case 0: g.setColor(new Color(255, 0, 0, 255));break;
			case 1: g.setColor(new Color(255, 127, 0, 255));break;
			case 2: g.setColor(new Color(255, 255, 0, 255));break;
			case 3: g.setColor(new Color(0, 255, 0, 255));break;
			case 4: g.setColor(new Color(0, 255, 255, 255));break;
			case 5: g.setColor(new Color(0, 0, 255, 255));break;
			case 6: g.setColor(new Color(127, 0, 255, 255));break;
			default: break;
		}
		g.setFont(new Font("Summit", Font.BOLD, 72));
		g.drawString("SEVEN COLORS OF THE WORLD", 60, 120);
		g.drawRoundRect(40, 55, 1185, 80, 30, 30);
		g.drawRoundRect(37, 52, 1191, 86, 36, 36);
	}
	
	private void renderInstructions(Graphics g){
		g.setColor(Color.CYAN);
		g.setFont(new Font("Arial", Font.PLAIN, 17));
		int dt=21;int st=50;
		String linebreak="";
		String intro0="OBJECTIVE:";
		String intro1="Welcome to my simple bullet-hell style java applet shooter, written in java from scratch without any external libraries, code, or images.";
		String intro2="In this game, you control a small, agile fighter. You must destroy enemy flyers while avoiding terrible, terrible amounts of bullets shot from them.";
		String intro3="The final boss, HEPTA-DARKNESS, has stolen the seven colors of rainbow from our world. You must destroy him to return the rainbow colors to our once colorful world!";
		String intro4="IF YOU DEFEAT HEPTA-DARKNESS IN HELL MODE, YOU WILL UNLOCK A SECRET BACKGROUND!!!";
		String intro5="INSTRUCTIONS:";
		String intro6="Select a stage below to play. Your ship follows your mouse and shoots automatically. Your ship's hitbox is the blue circle. Don't get hit there by enemy bullets or you lose HP!";
		String intro7="Each time you're hit, your ship automatically phases out for a short time. During this time you only take 10% damage, multipled by your own damage reduction.";
		String intro8="When your energy bar is full, left click to deplete it and activate EMP pulse that destroys every bullet near you. Energy bar recharges automatically.";
		String intro9="Earn credits by destroying enemies. Spend credits by clicking on the 10 upgrade buttons on the bottom of this screen to upgrade your ship.";
		String intro10="You can also select one of the five difficulties by clicking on its corresponding button. At higher difficulties, enemies shoot more bullets but also gives more bounty.";
		String intro11="Your game saves each time you enter the stage selection screen. You can also manually save by clicking on the save button on the top right corner.";
		String intro12="There're 32 levels in total. You keep all credits earned even if you failed a level. Defeat HEPTA-DARKNESS to win. Good luck and have fun!";
		String intro13="TIPS:";
		String intro14="If you find a level too difficult, try tuning down the difficulty and/or play a previous level again, earn more credits, upgrade your ship, and try again.";
		String intro15="Your EMP has unlimited number of uses per stage, so don't hesitate to use it in dire situations. Try to destroy as many bullets as possible per use.";
		String intro16="Your last level of upgrades are so expansive that you normally won't be able to afford it unless you defeat HEPTA-DARKNESS. Defeating HEPTA-DARKNESS boss gives a huge bounty.";
		String intro17="HEPTA-DARKNESS has 7 phases, representing the seven colors of the rainbow. As the boss gets into higher phases, he will shoot more and stronger bullets. Be careful!";
		String intro18="You are advised not to attempt HEPTA-DARKNESS on hell difficulty before your upgrades are maxed out. HEPTA-DARKNESS is still tough to hunt down even with a maxed out ship.";
		String intro19="Try the game, especially the final boss, on hell difficulty, once you max out your ship. Only hell difficulty will prove a challenge and fun for you at that point.";
		String intro20="You can fly into enemies and bosses and not take any damage. The only thing that can damage you in this game are enemy bullets";
		String intro21="When in a pinch, don't panic and fly through swarms of bullets because you're never invincible even when phased out. Careful maneuvering is the key to victory.";
		String intro22="Sometimes it might be necessary to be hit by a few small bullets in order to avoid getting hit by those big and damaging bullets.";
		g.drawString(intro0, 5, st);
		g.drawString(intro1, 5, st+dt);
		g.drawString(intro2, 5, st+dt*2);
		g.drawString(intro3, 5, st+dt*3);
		g.drawString(intro4, 5, st+dt*4);
		g.drawString(linebreak, 5, st+dt*5);
		g.drawString(intro5, 5, st+dt*6);
		g.drawString(intro6, 5, st+dt*7);
		g.drawString(intro7, 5, st+dt*8);
		g.drawString(intro8, 5, st+dt*9);
		g.drawString(intro9, 5, st+dt*10);
		g.drawString(intro10, 5, st+dt*11);
		g.drawString(intro11, 5, st+dt*12);
		g.drawString(linebreak, 5, st+dt*13);
		g.drawString(intro12, 5, st+dt*14);
		g.drawString(intro13, 5, st+dt*15);
		g.drawString(intro14, 5, st+dt*16);
		g.drawString(intro15, 5, st+dt*17);
		g.drawString(intro16, 5, st+dt*18);
		g.drawString(intro17, 5, st+dt*19);
		g.drawString(intro18, 5, st+dt*20);
		g.drawString(intro19, 5, st+dt*21);
		g.drawString(intro20, 5, st+dt*22);
		g.drawString(intro21, 5, st+dt*23);
		g.drawString(intro22, 5, st+dt*24);
	}
	
	private void renderStageSelectionButtons(Graphics g){
		for(int i=0;i<stageButtonCoorX.length;i++){
			String msg=int2Str(i+1);
			boolean unlocked=true;
			if(i>level.maxlevelCleared)
				unlocked=false;
			renderSingleStageSelectButton(stageButtonCoorX[i], stageButtonCoorY[i], stageButtonW, stageButtonH, msg, unlocked, g);
		}
	}
	
	private void renderDifficultySelectionButtons(Graphics g){
		renderSingleDifficultySelectionButton(difButtonCoorX[0], difButtonCoorY[0], difButtonW, difButtonH, "CASUAL", 1, g);
		renderSingleDifficultySelectionButton(difButtonCoorX[1], difButtonCoorY[1], difButtonW, difButtonH, "  EASY", 2, g);
		renderSingleDifficultySelectionButton(difButtonCoorX[2], difButtonCoorY[2], difButtonW, difButtonH, "NORMAL", 3, g);
		renderSingleDifficultySelectionButton(difButtonCoorX[3], difButtonCoorY[3], difButtonW, difButtonH, "  HARD", 4, g);
		renderSingleDifficultySelectionButton(difButtonCoorX[4], difButtonCoorY[4], difButtonW, difButtonH, "  HELL", 5, g);
	}
	
	private void renderUpgradeButtons(Graphics g){
		g.setColor(Color.YELLOW);
		g.setFont(new Font("Times New Roman", Font.ITALIC, 32));
		String creditStr="CREDITS:   "+(int)player.credit;
		g.drawString(creditStr, 30, 500);
		
		String msg, desc;
		msg="ATK ("+int2Str(player.ups[0])+")";
		desc="Increases player's bullet damage. Each point increases bullet damage by 20%. Final level increases damage by 100%.";
		renderSingleUpgradeButton(upButtonCoorX[0], upButtonCoorY[0], upButtonW, upButtonH, msg, desc, 0, g);
		msg="SPD ("+int2Str(player.ups[1])+")";
		desc="Increases player's attack speed. Each point increases attack speed by 15%. Final level increases attack speed by 75%.";
		renderSingleUpgradeButton(upButtonCoorX[1], upButtonCoorY[1], upButtonW, upButtonH, msg, desc, 1, g);
		msg="MHP ("+int2Str(player.ups[2])+")";
		desc="Increases player's maximum HP. Each upgrade increases maximum HP by 20. Final level increases maximum HP by 100.";
		renderSingleUpgradeButton(upButtonCoorX[2], upButtonCoorY[2], upButtonW, upButtonH, msg, desc, 2, g);
		msg="RED ("+int2Str(player.ups[3])+")";
		desc="Reduces enemy bullets' incoming damage. Each upgrade reduces incoming damage by 3%. Final level reduces incoming damage by 15%.";
		renderSingleUpgradeButton(upButtonCoorX[3], upButtonCoorY[3], upButtonW, upButtonH, msg, desc, 3, g);		
		msg="REG ("+int2Str(player.ups[4])+")";
		desc="Increases player's HP regeneration speed. Each upgrade increases HP regen by roughly 0.1hp/s. Final level increases HP regen by 0.5hp/s.";
		renderSingleUpgradeButton(upButtonCoorX[4], upButtonCoorY[4], upButtonW, upButtonH, msg, desc, 4, g);
		msg="MOV ("+int2Str(player.ups[5])+")";
		desc="Increases player's movement speed. Each upgrade increases movement speed of player's ship by 10%. Final level increases movement speed by 50%.";
		renderSingleUpgradeButton(upButtonCoorX[5], upButtonCoorY[5], upButtonW, upButtonH, msg, desc, 5, g);
		msg="SPR ("+int2Str(player.ups[6])+")";
		desc="Allows the player to shoot up to 10 streams of spreading bullets. Each upgrade increases stream by 1. Final level adds 5 more streams.";
		renderSingleUpgradeButton(upButtonCoorX[6], upButtonCoorY[6], upButtonW, upButtonH, msg, desc, 6, g);
		msg="TME ("+int2Str(player.ups[7])+")";
		desc="Increases player's phase time after being hit. Each upgrade increases phase time by 0.1s. Final level increases phase time by 0.5s.";
		renderSingleUpgradeButton(upButtonCoorX[7], upButtonCoorY[7], upButtonW, upButtonH, msg, desc, 7, g);
		msg="PRG ("+int2Str(player.ups[8])+")";
		desc="Increases player's EMP pulse energy regeneration rate. Each point increases energy regen by 10%. Final level increases energy regen by 50%.";
		renderSingleUpgradeButton(upButtonCoorX[8], upButtonCoorY[8], upButtonW, upButtonH, msg, desc, 8, g);
		msg="PRD ("+int2Str(player.ups[9])+")";
		desc="Increases player's EMP pulse radius. Each upgrade increases pulse radius by 15%. Final level increases pulse radius by 75%.";
		renderSingleUpgradeButton(upButtonCoorX[9], upButtonCoorY[9], upButtonW, upButtonH, msg, desc, 9, g);
	}
	
	private void renderInstructionButton(Graphics g){
		int w=resetButtonW;int h=resetButtonH;int x=GameEnvironment.boundX-resetButtonCoorX;int y=resetButtonCoorY;
		if(mouseOverButton(x, y, w, h)){
			showInstructions=true;
			g.setColor(Color.YELLOW);
		}else{
			showInstructions=false;
			g.setColor(Color.MAGENTA);
		}
		g.fillRect(x-w, y-h, w*2, h*2);
		g.setColor(Color.BLACK);
		g.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		g.drawString("INSTRUCTIONS", x-45, y+4);
	}
	
	private void renderResetButton(Graphics g){
		int w=resetButtonW;int h=resetButtonH;int x=resetButtonCoorX;int y=resetButtonCoorY;
		if(mouseOverButton(x, y, w, h)){
			g.setColor(Color.WHITE);
			g.setFont(new Font("Times New Roman", Font.PLAIN, 16));
			g.drawString("WARNING! RESET GAME will reset all player credits, upgrade, and cleared levels!!!", 630, y+5);
			g.setColor(Color.YELLOW);			
		}else{
			g.setColor(Color.MAGENTA);
		}
		g.fillRect(x-w, y-h, w*2, h*2);
		g.setColor(Color.BLACK);
		g.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		if(confirmingReset)
			g.drawString("ARE YOU SURE?", x-48, y+4);
		else
			g.drawString("RESET GAME", x-40, y+4);				
	}
	
	// Buttons have a 2*w pixel width and 2*h pixel height, centered at (x, y)
	private void renderSingleStageSelectButton(int x, int y, int w, int h, String msg, boolean unlocked, Graphics g){
		if(unlocked && mouseOverButton(x, y, w, h)){
			g.setColor(new Color(255, 255, 0, 255));
		}else{
			g.setColor(new Color(255, 0, 0, 200));
		}
		g.fillRoundRect(x-w, y-h, w*2, h*2, h/2, h/2);
		if(unlocked)
			g.setColor(Color.WHITE);
		else
			g.setColor(Color.DARK_GRAY);
		g.setFont(new Font("Times New Roman", Font.BOLD, 21));
		g.drawString(msg, x-12, y+8);
	}
	
	// Buttons have a 2*w pixel width and 2*h pixel height, centered at (x, y)
	private void renderSingleUpgradeButton(int x, int y, int w, int h, String msg, String description, int upsID, Graphics g){
		if(mouseOverButton(x, y, w, h)){
			renderDescriptionMsg(description, g);
			renderCostMsg(upsID, g);
			g.setColor(new Color(255, 255, 0, 255));
		}else{
			g.setColor(new Color(0, 255, 55, 170));
		}
		g.fillRoundRect(x-w, y-h, w*2, h*2, h/2, h/2);
		g.setColor(Color.BLUE);
		g.setFont(new Font("Times New Roman", Font.BOLD, 36));
		g.drawString(msg, x-70, y+10);
	}
	
	private void renderSingleDifficultySelectionButton(int x, int y, int w, int h, String msg, int id, Graphics g){
		if(id==selectedDifficulty)
			g.setColor(new Color(87, 46, 222, 200));
		else
			g.setColor(new Color(75, 173, 238, 200));
		g.fillOval(x-w, y-h, w*2, h*2);
		g.setColor(new Color(200, 200, 200, 200));
		g.setFont(new Font("Times New Roman", Font.BOLD, 30));
		g.drawString(msg, x-60, y+8);
	}
	
	private void renderDescriptionMsg(String description, Graphics g){
		g.setColor(Color.WHITE);
		g.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		g.drawString(description, 10, boundY-10);
	}
	
	private void renderCostMsg(int upsID, Graphics g){
		String cost;
		if(player.ups[upsID]==player.upgradeCosts.length)
			cost="MAXED";
		else
			cost=String.valueOf(player.upgradeCosts[player.ups[upsID]]);
		g.setColor(Color.YELLOW);
		g.setFont(new Font("Times New Roman", Font.BOLD, 32));
		g.drawString("NEXT UPGRADE COST:   "+cost, 750, 500);
	}
	
	// Assume n=0...99. Pad a zero in front when n<10
	private String int2Str(int n){
		if(n>9)
			return String.valueOf(n);
		return "0"+String.valueOf(n);
	}
	// Returns true if mouse is over the button centered at (x, y) with width 2*w and height 2*h 
	private boolean mouseOverButton(int x, int y, int w, int h){
		return (mouseX-x<w && mouseX-x>-w && mouseY-y<h && mouseY-y>-h);
	}
}

class GameBackgroundStar{
	int x;int y;int v;
	GameBackgroundStar(){
		x=GameEnvironment.boundX+10;
		y=(int)(Math.random()*GameEnvironment.boundY);
		v=(int)(Math.random()*10)+5;
	}
	
	void update(){
		x=x-v;
	}
	
	void render(Graphics g){
		g.drawLine(x-1, y, x+1, y);
		g.drawLine(x, y-1, x, y+1);
	}
	
	boolean outOfScreen(){
		return x<0;
	}
}
