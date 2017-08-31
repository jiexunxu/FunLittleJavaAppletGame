package bullethell.applet;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

class Player {
	// Defines the player's polygon shaped fighter and its scale
	double[] xpoints={1, .333, -1, -.6, -1, -.6, -1, .333};
	double[] ypoints={0, .5, .75, .3, 0, -.3, -.75, -.5};	
	double scale;
	// Player's coordinates
	int x;
	int y;
	// Player's speed
	double v;
	// Player's radius
	int r;
	// Player's hp
	double hp;
	double maxhp;
	double regen;
	double dmgReduction;
	
	// A pulse instantly destroys all bullets near the fighter within radius pulseRadius. Uses pulse energy and need to recharge
	int pulseRegen;
	int maxPulse;
	int pulse;
	int pulseRadius;
	int curPulseRadius;
	int pulseRadiusSpd;
	
	/* Ranges from level 1 to level 11. Level 1 shoots one stream of bullets, level 2 shoots 2 streams. 
	 * The angle between the streams is bulletAngleSpread degrees. Each additional level gives one additional stream of bullets.
	 */
	int bulletLevel;
	int bulletDmg;
	long bulletSpd;
	PlayerBullet pbullet;
	
	double credit;
	
	long lastInvicibilityStartTime;
	long invincibilityTimer;
	boolean invincible;
	
	int[] ups; // Stores the upgrade level for the following 10 upgrades
	int[] upgradeCosts={32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 1000000}; 	
	int[] dmgUps={10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 40};
	long[] attSpdUps={600, 580, 560, 540, 520, 500, 480, 460, 440, 420, 400, 300};
	int[] maxhpUps={100, 120, 140, 160, 180, 200, 220, 240, 260, 280, 300, 400};
	double[] dmgRedUps={1.0, 0.97, 0.94, 0.91, 0.88, 0.85, 0.82, 0.79, 0.76, 0.73, 0.7, 0.55};
	double[] regenUps={0.0, 0.002, 0.004, 0.006, 0.008, 0.010, 0.012, 0.014, 0.016, 0.018, 0.020, 0.030}; 
	double[] mvSpdUps={5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 17.5};
	int[] spreadUps={1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
	long[] invTimeUps={500, 600, 700, 800, 900, 1000, 1100, 1200, 1300, 1400, 1500, 2000}; 
	int[] pulseRegenUps={10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 25};
	int[] pulseRadiusUps={100, 115, 130, 145, 160, 175, 190, 205, 220, 235, 250, 325}; 
	
	
	// Player's color and player's blue hit circle's color, and the invincibility colors for the player
	Color color;
	Color bcolor;
	Color hcolor;
	Color hbcolor;
	Color colorInv;
	Color hcolorInv;
	Color hpbarColor;
	Color creditColor;
	Color empColor;
	Color empFullColor;
	Color empFadeColor;
	
	Player(){
		scale=13;		
		r=3;
		credit=0;
		ups=new int[10];
		for(int i=0;i<ups.length;i++)
			ups[i]=11;
		
		color=new Color(10, 180, 20, 255);
		bcolor=new Color(124, 255, 230, 255);
		hcolor=new Color(20, 80, 120, 255);
		hbcolor=new Color(230, 100, 255, 255);
		colorInv=new Color(0, 222, 96, 70);
		hcolorInv=new Color(0, 95, 168, 128);
		hpbarColor=new Color(0, 255, 120, 150);
		creditColor=new Color(255, 255, 0, 200);
		empColor=new Color(50, 200, 200, 200);
		empFullColor=new Color(150, 255, 255, 230);
		empFadeColor=new Color(empColor.getRed(), empColor.getGreen(), empColor.getBlue(), empColor.getAlpha());
		
		pbullet=new PlayerBullet();
	}
	
	void initPlayerForNewLevel(){
		bulletDmg=dmgUps[ups[0]];
		bulletSpd=attSpdUps[ups[1]];
		maxhp=maxhpUps[ups[2]];
		dmgReduction=dmgRedUps[ups[3]];
		regen=regenUps[ups[4]];
		v=mvSpdUps[ups[5]];
		bulletLevel=spreadUps[ups[6]];
		invincibilityTimer=invTimeUps[ups[7]];
		pulseRegen=pulseRegenUps[ups[8]];		
		pulseRadius=pulseRadiusUps[ups[9]];
				
		lastInvicibilityStartTime=0;
		invincible=false;
				
		x=GameEnvironment.boundX/21; 
		y=GameEnvironment.boundY/2;
		maxPulse=20000;
		pulse=maxPulse-1000;
		curPulseRadius=0;
		hp=maxhp;
		pulseRadiusSpd=5;

		pbullet.newPlayerBulletLevel(bulletLevel);
		pbullet.bulletShootingInterval=bulletSpd;
		pbullet.bulletDmg=bulletDmg;
	}
	
	void update(int mouseX, int mouseY, Level level){
		movePlayerToCursor(mouseX, mouseY);
		pbullet.shootBullet(x, y);
		pbullet.updateBullets();		
		// curPulseRadius>0 means EMP is firing. Increases EMP radius per frame 
		if(curPulseRadius>0){
			curPulseRadius+=pulseRadiusSpd;
			if(curPulseRadius>pulseRadius)
				curPulseRadius=0; // Finished firing EMP
		}
		// hp and EMP pulse regen
		hp=Math.min(maxhp, hp+regen);
		pulse=Math.min(maxPulse, pulse+pulseRegen);
		// player invincibility
		if(System.currentTimeMillis()-lastInvicibilityStartTime<invincibilityTimer)
			invincible=true;
		else
			invincible=false;
		// Calculate player damage from enemy bullet
		int dmg=level.hitPlayer(x, y, r);		
		if(invincible){
			hp=hp-dmg*dmgReduction*0.1;
		}else{
			if(dmg>0){
				hp=hp-dmg*dmgReduction;
				lastInvicibilityStartTime=System.currentTimeMillis();
			}
		}
		if(hp<=0)
			level.gameover=true;
	}
	
	// This function is called by BulletHellGame's mouseClicked function directly
	void releaseEMP(){
		if(pulse==maxPulse){
			pulse=0;
			curPulseRadius=5;
		}
	}
	
	void render(Graphics g){
		if(invincible)
			g.setColor(colorInv);
		else
			g.setColor(color);
		int[] xpts=new int[xpoints.length];
		int[] ypts=new int[ypoints.length];
		for(int i=0;i<xpts.length;i++){
			xpts[i]=(int)(xpoints[i]*scale+x);
			ypts[i]=(int)(ypoints[i]*scale+y);
		}
		g.fillPolygon(xpts, ypts, xpoints.length);
		if(!invincible){
			g.setColor(bcolor);
			g.drawPolygon(xpts, ypts, xpts.length);
		}
			
		if(invincible)
			g.setColor(hcolorInv);
		else
			g.setColor(hcolor);
		g.fillOval((int)(x-r), (int)(y-r), 2*r+1, 2*r+1);
		if(!invincible){
			g.setColor(hbcolor);
			g.drawOval((int)(x-r), (int)(y-r), 2*r+1, 2*r+1);
		}
		// Fire the EMP pulse 
		if(curPulseRadius>0){
			g.setColor(empFadeColor);
			g.drawOval(x-curPulseRadius, y-curPulseRadius, curPulseRadius*2, curPulseRadius*2);
			g.drawOval(x-curPulseRadius+2, y-curPulseRadius+2, curPulseRadius*2-4, curPulseRadius*2-4);
			g.drawOval(x-curPulseRadius+4, y-curPulseRadius+4, curPulseRadius*2-8, curPulseRadius*2-8);
			empFadeColor=new Color(empFadeColor.getRed(), empFadeColor.getGreen(), empFadeColor.getBlue(), (int)(empFadeColor.getAlpha()/1.02));
		}else{
			empFadeColor=new Color(empColor.getRed(), empColor.getGreen(), empColor.getBlue(), empColor.getAlpha());
		}
		// Status info
		g.setColor(hpbarColor);
		g.setFont(new Font("Times New Roman", Font.PLAIN, 20));
		g.drawString("HP="+String.valueOf((int)hp), 5, 21);
		g.fillRect(80, 7, (int)hp*2, 15);
		if(pulse==maxPulse)
			g.setColor(empFullColor);
		else
			g.setColor(empColor);
		g.drawString("EMP", 5, 42);
		g.fillRect(80, 32, pulse/100, 7);
		g.setColor(creditColor);
		g.drawString(String.valueOf((int)credit), 30, 63);
		g.fillOval(5, 49, 15, 15);
	}
	
	private void movePlayerToCursor(int mouseX, int mouseY){
		double l=GameEnvironment.vectorLength(x-mouseX, y-mouseY);
		if(l>v){
			x=x+(int)((mouseX-x)*v/l);
			y=y+(int)((mouseY-y)*v/l);
		}else{
			x=mouseX;y=mouseY;
		}
	}
}
