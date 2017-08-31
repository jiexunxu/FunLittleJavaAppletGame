package bullethell.applet;

import java.awt.Color;
import java.awt.Graphics;

/* All enemies will spawn at the right border. They will have deterministic HP and bullet damage, but will have some randomness in movement speed and spawn location.
 * 
 * Each enemy will use only one type of bullet pattern. Each enemy has three classes: one small, one medium, and 
 * one large. Small enemies tend to be weak, give less bounty, and appear in swarms. Medium enemies are strong, and only appeear regularly in 20+ levels. Large enemies
 * are very strong and require quite a while to take down even with decent upgrades. They will rarely show up but if they do, they will present to be a headache.
 * Furthermore, each class of enemy is divided into 5 types, ranging from weakest to strongest. This type number is used for identification in the Enemy class. Stronger
 * type enemies will only appear more regularly in later levels. Stronger type enemies also tend to be darker.
 * 
 * Type of enemies implemented:
 * 
 * Enemies that uses bullet pattern 1:
 * enemyType = 0~4 (small):Periodically shoots bullets towards the player. Very weak. Tend to swarm the player in mass.
 * enemyType = 5~9 (medium): Stronger, shoots faster but its bullets are somewhat inaccurate.
 * enemyType = 10~14 (large): Strongest in this class. Shoots very fast, with some scattering. Its bullets also have a little bit homing ability	
 * 
 * Enemies that uses bullet pattern 2:
 * enemyType = 15~19 (small):Periodically shoots bursts of evenly spreaded bullets towards the player.
 * enemyType = 20~24 (medium): Stronger, frequently shoots a wider but inaccurate arc of bullets towards the player.
 * enemyType = 25~29 (large): Strongest in this class. Quickly shoots bursts of very wide and a little bit inaccurate bullets towards the player. 
 * 
 * enemyType = 30~34 (small): Shoots a single, big bullet that will disintegrate into several small bullets toward the player
 * enemyType = 35~39 (medium): Shoots several large bullets that will all disintegrate into smaller ones. These smaller bullets will retarget the player
 * enemyType = 40~44 (large): Shoots huge bullets that will disintegrate into big bullets. These big bullets will further disintegrate into smaller ones that will retarget the player
 */
class Enemy {
	// An integer representing the type of enemy. This will set the enemies hp, move speed, move pattern, bullet pattern etc
	int enemyType;
	int hp;
	double maxHP;
	double x;
	double y;
	double vx;
	double vy;
	double ax;
	double ay;
	// The hitbox of this enemy. If a player bullet is within the rectangle (x+-hitx, y+-hity), this enemy is considered hit
	int hitx;
	int hity;
	// Is true if this enemy has been destroyed by player. Will not render this enemy, but still needs its BulletCreator instance to work
	boolean dead=false;;
	// If enemy almost comes to a stop, then set its v and a to zero. This will make the enemy stay in place. So the enemy must be destroyed to beat the level
	// This variable is used almost exclusively for large enemies and bosses
	boolean halt=false;
	// Bounty given to the player when this enemy is killed
	double bounty=0;
		
	// Enemies create a BulletCreator, the BulletCreator creates all bullets from this enemy
	BulletCreator bc;
	// Parameters passed to the BulletCreator instance
	double[] pm;
	
	Color color;
	
	
	// =================== Enemy type 0 to type 4 ============================	
	private void initType0To4(){		
		int et=enemyType-0;double yPerturb=0.9; // yPerturb shows how much perturbation enemies can spawn on y axis. Higher number has bigger perturbation
		hp=5*et+10;vx=-3+Math.random()*0.5;vy=Math.random()*0.2-0.1;ax=0;ay=0;hitx=10;hity=10;bounty=1+et;				
		x=GameEnvironment.boundX+hitx;
		y=(int)(GameEnvironment.boundY*(0.5+(Math.random()-0.5)*yPerturb));
		color=new Color(255-55*et, 0, 0, 255);
		
		double[] pm=new double[5];
		pm[0]=2000-200*et;pm[1]=3;pm[2]=10+et*3;pm[3]=0;pm[4]=0;
		bc=new BulletCreator(1, pm, new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
	}

	private double[] updateParamsType0To4(Player p){
		double[] pm=new double[6];	int et=enemyType-0;
		double px=p.x;double py=p.y;pm[0]=x;pm[1]=y;
		double l=GameEnvironment.vectorLength(px-x, py-y);
		pm[2]=(5+0.3*et)*(px-pm[0])/l;
		pm[3]=(5+0.3*et)*(py-pm[1])/l;
		pm[4]=0;pm[5]=0;
		return pm;
	}
	
	private void renderType0To4(Graphics g){
		int[] xpts=new int[3];xpts[0]=(int)x+hitx;xpts[1]=(int)x+hitx;xpts[2]=(int)x-hitx;
		int[] ypts=new int[3];ypts[0]=(int)y-hity;ypts[1]=(int)y+hity;ypts[2]=(int)y;
		g.fillPolygon(xpts, ypts, xpts.length);
	}
	
	// =================== Enemy type 5 to type 9 ============================
	private void initType5To9(){
		int et=enemyType-5;double yPerturb=0.4; // yPerturb shows how much perturbation enemies can spawn on y axis. Higher number has bigger perturbation
		hp=100*et+1000;vx=-1;vy=Math.random()*0.05-0.025;ax=0;ay=0;hitx=30;hity=30;bounty=et*5+30;
		x=GameEnvironment.boundX+hitx;
		y=(int)(GameEnvironment.boundY*(0.5+(Math.random()-0.5)*yPerturb));
		color=new Color(255-55*et, 0, 0, 255);
		
		double[] pm=new double[5];
		pm[0]=700-50*et;pm[1]=5;pm[2]=20+et*6;pm[3]=0.5;pm[4]=0;		
		bc=new BulletCreator(1, pm, new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
	}
	
	private double[] updateParamsType5To9(Player p){
		double[] pm=new double[6]; int et=enemyType-5;	
		double px=p.x;double py=p.y;pm[0]=x;pm[1]=y;
		double l=GameEnvironment.vectorLength(px-x, py-y);
		pm[2]=(4+0.1*et)*(px-pm[0])/l;
		pm[3]=(4+0.1*et)*(py-pm[1])/l;
		pm[4]=0;pm[5]=0;
		return pm;
	}
	
	private void renderType5To9(Graphics g){
		int[] xpts=new int[4];xpts[0]=(int)x;xpts[1]=(int)x+hitx;xpts[2]=(int)x;xpts[3]=(int)x-hitx;
		int[] ypts=new int[4];ypts[0]=(int)y-hity;ypts[1]=(int)y;ypts[2]=(int)y+hity;ypts[3]=(int)y;
		g.fillPolygon(xpts, ypts, xpts.length);		
	}
	
	// =================== Enemy type 10 to type 14 ============================
	private void initType10To14(){
		halt=true;
		int et=enemyType-10;double yPerturb=0.1; // yPerturb shows how much perturbation enemies can spawn on y axis. Higher number has bigger perturbation
		hp=1000*et+6000;vx=-3.0;vy=0;ax=-vx/150;ay=0;hitx=40;hity=100;bounty=et*50+200;
		x=GameEnvironment.boundX+hitx;
		y=(int)(GameEnvironment.boundY*(0.5+(Math.random()-0.5)*yPerturb));
		color=new Color(255-55*et, 0, 0, 255);
		
		double[] pm=new double[5];
		pm[0]=200-25*et;pm[1]=7;pm[2]=30+et*10;pm[3]=0.9;pm[4]=0.1;		
		bc=new BulletCreator(1, pm, new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
	}
	
	private double[] updateParamsType10To14(Player p){
		double[] pm=new double[9]; int et=enemyType-10;	
		double px=p.x;double py=p.y;pm[0]=x-hitx*0.5+Math.random()*hitx;pm[1]=y-hity*0.9+Math.random()*hity*1.8;
		double l=GameEnvironment.vectorLength(px-x, py-y);
		pm[2]=3*(px-pm[0])/l;
		pm[3]=3*(py-pm[1])/l;
		pm[4]=0;pm[5]=0;pm[6]=px;pm[7]=py;pm[8]=et/200;
		return pm;
	}
	
	private void renderType10To14(Graphics g){
		int[] xpts=new int[5];xpts[0]=(int)x+hitx;xpts[1]=(int)x+hitx;xpts[2]=(int)x-hitx;xpts[3]=(int)x;xpts[4]=(int)x-hitx;
		int[] ypts=new int[5];ypts[0]=(int)y-hity;ypts[1]=(int)y+hity;ypts[2]=(int)y+hity;ypts[3]=(int)y;ypts[4]=(int)y-hity;
		g.fillPolygon(xpts, ypts, xpts.length);	
	}
	
	// =================== Enemy type 15 to type 19 ============================	
	private void initType15To19(){		
		int et=enemyType-15;double yPerturb=0.9; // yPerturb shows how much perturbation enemies can spawn on y axis. Higher number has bigger perturbation
		hp=6*et+13;vx=-2.7+Math.random()*0.5;vy=Math.random()*0.2-0.1;ax=0;ay=0;hitx=5;hity=10;bounty=2+et*1.2;				
		x=GameEnvironment.boundX+hitx;
		y=(int)(GameEnvironment.boundY*(0.5+(Math.random()-0.5)*yPerturb));
		color=new Color(255-55*et, 127-27*et, 0, 255);
		
		double[] pm=new double[7];
		pm[0]=4000-200*et;pm[1]=4;pm[2]=12+enemyType*3;pm[3]=7;pm[4]=10*Math.PI/180;pm[5]=0;pm[6]=0;
		bc=new BulletCreator(2, pm, new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
	}

	private double[] updateParamsType15To19(Player p){
		double[] pm=new double[8];	int et=enemyType-15;
		double px=p.x;double py=p.y;pm[0]=x;pm[1]=y;
		double l=GameEnvironment.vectorLength(px-x, py-y);
		pm[2]=(4.5+0.2*et)*(px-pm[0])/l;
		pm[3]=(4.5+0.2*et)*(py-pm[1])/l;
		pm[4]=0;pm[5]=0;pm[6]=px;pm[7]=py;
		return pm;
	}
	
	private void renderType15To19(Graphics g){
		int[] xpts=new int[3];xpts[0]=(int)x+hitx;xpts[1]=(int)x+hitx;xpts[2]=(int)x-hitx;
		int[] ypts=new int[3];ypts[0]=(int)y-hity;ypts[1]=(int)y+hity;ypts[2]=(int)y;
		g.fillPolygon(xpts, ypts, xpts.length);	
	}
	
	// =================== Enemy type 20 to type 24 ============================
	private void initType20To24(){
		int et=enemyType-20;double yPerturb=0.4; // yPerturb shows how much perturbation enemies can spawn on y axis. Higher number has bigger perturbation
		hp=120*et+1200;vx=-0.9;vy=Math.random()*0.05-0.025;ax=0;ay=0;hitx=30;hity=30;bounty=et*6+40;
		x=GameEnvironment.boundX+hitx;
		y=(int)(GameEnvironment.boundY*(0.5+(Math.random()-0.5)*yPerturb));
		color=new Color(255-55*et, 127-27*et, 0, 255);
		
		double[] pm=new double[7];
		pm[0]=3600-60*et;pm[1]=6;pm[2]=25+et*7;pm[3]=21;pm[4]=3*Math.PI/180;pm[5]=0.2;pm[6]=0;
		bc=new BulletCreator(2, pm, new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
	}
	
	private double[] updateParamsType20To24(Player p){
		double[] pm=new double[8]; int et=enemyType-20;	
		double px=p.x;double py=p.y;pm[0]=x;pm[1]=y;
		double l=GameEnvironment.vectorLength(px-x, py-y);
		pm[2]=(5+0.1*et)*(px-pm[0])/l;
		pm[3]=(5+0.1*et)*(py-pm[1])/l;
		pm[4]=0;pm[5]=0;pm[6]=px;pm[7]=py;
		return pm;
	}
	
	private void renderType20To24(Graphics g){
		g.fillOval((int)x-hitx, (int)y-hity, hitx*2, hity*2);
	}
	
	// =================== Enemy type 25 to type 29 ============================
	private void initType25To29(){
		halt=true;
		int et=enemyType-25;double yPerturb=0.1; // yPerturb shows how much perturbation enemies can spawn on y axis. Higher number has bigger perturbation
		hp=1100*et+6600;vx=-3.2;vy=0;ax=-vx/150;ay=0;hitx=50;hity=110;bounty=et*60+240;
		x=GameEnvironment.boundX+hitx;
		y=(int)(GameEnvironment.boundY*(0.5+(Math.random()-0.5)*yPerturb));
		color=new Color(255-55*et, 127-27*et, 0, 255);
			
		double[] pm=new double[17];
		pm[0]=600-30*et;pm[1]=8;pm[2]=30+et*10;pm[3]=41;pm[4]=4*Math.PI/180;pm[5]=0.1;pm[6]=0;
		bc=new BulletCreator(2, pm, new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
	}
	
	private double[] updateParamsType25To29(Player p){
		double[] pm=new double[10]; 
		double px=p.x;double py=p.y;pm[0]=x-hitx*0.3+Math.random()*hitx*0.6;pm[1]=y-hity*0.9+Math.random()*hity*1.8;
		double l=GameEnvironment.vectorLength(px-x, py-y);
		pm[2]=4*(px-pm[0])/l;
		pm[3]=4*(py-pm[1])/l;
		pm[4]=0;pm[5]=0;pm[6]=px;pm[7]=py;
		return pm;
	}
	
	private void renderType25To29(Graphics g){
		int[] xpts=new int[5];xpts[0]=(int)x+hitx;xpts[1]=(int)x+hitx;xpts[2]=(int)x;xpts[3]=(int)x-hitx;xpts[4]=(int)x;
		int[] ypts=new int[5];ypts[0]=(int)y-hity;ypts[1]=(int)y+hity;ypts[2]=(int)y+hity;ypts[3]=(int)y;ypts[4]=(int)y-hity;
		g.fillPolygon(xpts, ypts, xpts.length);		
	}
	
	// =================== Enemy type 30 to type 34 ============================	
	private void initType30To34(){		
		int et=enemyType-30;double yPerturb=0.9; // yPerturb shows how much perturbation enemies can spawn on y axis. Higher number has bigger perturbation
		hp=7*et+17;vx=-2.6+Math.random()*0.4;vy=Math.random()*0.2-0.1;ax=0;ay=0;hitx=5;hity=10;bounty=2.4+et*1.4;				
		x=GameEnvironment.boundX+hitx;
		y=(int)(GameEnvironment.boundY*(0.5+(Math.random()-0.5)*yPerturb));
		color=new Color(255-55*et, 255-55*et, 0, 255);
		
		double[] pm=new double[17];
		pm[0]=6000-400*et;pm[1]=16;pm[2]=36+et*12;pm[3]=1;pm[4]=30*Math.PI/180;pm[5]=0;pm[6]=0;
		pm[7]=3000;pm[8]=1;pm[9]=3;pm[10]=12+et*4;pm[11]=11;pm[12]=10*Math.PI/180;pm[13]=255;pm[14]=70;pm[15]=70;pm[16]=255;
		bc=new BulletCreator(2, pm, new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
	}

	private double[] updateParamsType30To34(Player p){
		double[] pm=new double[10];	int et=enemyType-30;
		double px=p.x;double py=p.y;pm[0]=x;pm[1]=y;
		double l=GameEnvironment.vectorLength(px-x, py-y);
		double alpha=Math.random()*0.4+0.8;
		pm[2]=(6+0.1*et)*(px-pm[0])/l*alpha;
		pm[3]=(6+0.1*et)*(py-pm[1])/l*alpha;
		pm[4]=0;pm[5]=0;pm[6]=px;pm[7]=py;pm[8]=1.0;pm[9]=0.3;
		return pm;
	}
	
	private void renderType30To34(Graphics g){
		int[] xpts=new int[4];xpts[0]=(int)x+hitx;xpts[1]=(int)x;xpts[2]=(int)x-hitx;xpts[3]=(int)x;
		int[] ypts=new int[4];ypts[0]=(int)y;ypts[1]=(int)y+hity;ypts[2]=(int)y;ypts[3]=(int)y-hity;
		g.fillPolygon(xpts, ypts, xpts.length);	
	}
	
	// =================== Enemy type 35 to type 39 ============================
	private void initType35To39(){
		int et=enemyType-35;double yPerturb=0.4; // yPerturb shows how much perturbation enemies can spawn on y axis. Higher number has bigger perturbation
		hp=130*et+1300;vx=-0.85;vy=Math.random()*0.05-0.025;ax=0;ay=0;hitx=35;hity=25;bounty=et*7+45;
		x=GameEnvironment.boundX+hitx;
		y=(int)(GameEnvironment.boundY*(0.5+(Math.random()-0.5)*yPerturb));
		color=new Color(255-55*et, 255-55*et, 0, 255);
		
		double[] pm=new double[17];
		pm[0]=4500-100*et;pm[1]=24;pm[2]=48+et*16;pm[3]=4;pm[4]=10*Math.PI/180;pm[5]=0;pm[6]=0;
		pm[7]=4000;pm[8]=2;pm[9]=6;pm[10]=18+et*6;pm[11]=15;pm[12]=6*Math.PI/180;pm[13]=230;pm[14]=60;pm[15]=60;pm[16]=255;
		bc=new BulletCreator(2, pm, new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
	}
	
	private double[] updateParamsType35To39(Player p){
		double[] pm=new double[10]; int et=enemyType-35;	
		double px=p.x;double py=p.y;pm[0]=x;pm[1]=y;
		double l=GameEnvironment.vectorLength(px-x, py-y);
		double alpha=Math.random()*0.6+0.7;
		pm[2]=(5.2+0.1*et)*(px-pm[0])/l*alpha;
		pm[3]=(5.2+0.1*et)*(py-pm[1])/l*alpha;
		pm[4]=0;pm[5]=0;pm[6]=px;pm[7]=py;pm[8]=0.8;pm[9]=0.2;
		return pm;
	}
	
	private void renderType35To39(Graphics g){
		g.fillOval((int)x-hitx, (int)y-hity, hitx*2, hity*2);
	}
	
	// =================== Enemy type 40 to type 44 ============================
	private void initType40To44(){
		halt=true;
		int et=enemyType-40;double yPerturb=0.1; // yPerturb shows how much perturbation enemies can spawn on y axis. Higher number has bigger perturbation
		hp=1150*et+6800;vx=-3.6;vy=0;ax=-vx/120;ay=0;hitx=55;hity=120;bounty=et*66+270;
		x=GameEnvironment.boundX+hitx;
		y=(int)(GameEnvironment.boundY*(0.5+(Math.random()-0.5)*yPerturb));
		color=new Color(255-55*et, 255-55*et, 0, 255);
			
		double[] pm=new double[27];
		pm[0]=3300-50*et;pm[1]=40;pm[2]=80+et*24;pm[3]=6;pm[4]=8*Math.PI/180;pm[5]=0.3;pm[6]=0;
		pm[7]=3000;pm[8]=1;pm[9]=20;pm[10]=40+et*12;pm[11]=8;pm[12]=7*Math.PI/180;pm[13]=180;pm[14]=30;pm[15]=30;pm[16]=255;
		pm[17]=3000;pm[18]=2;pm[19]=10;pm[20]=20+et*6;pm[21]=7;pm[22]=6*Math.PI/180;pm[23]=255;pm[24]=90;pm[25]=90;pm[26]=255;
		bc=new BulletCreator(2, pm, new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
	}
	
	private double[] updateParamsType40To44(Player p){
		double[] pm=new double[12]; 
		double px=p.x;double py=p.y;pm[0]=x-hitx*0.3+Math.random()*hitx*0.6;pm[1]=y-hity*0.9+Math.random()*hity*1.8;
		double l=GameEnvironment.vectorLength(px-x, py-y);
		double alpha=Math.random()*0.6+0.7;
		pm[2]=4.4*(px-pm[0])/l*alpha;
		pm[3]=4.4*(py-pm[1])/l*alpha;
		pm[4]=0;pm[5]=0;pm[6]=px;pm[7]=py;pm[8]=2.4;pm[9]=0.1;pm[10]=1.0;pm[11]=0.25;
		return pm;
	}
	
	private void renderType40To44(Graphics g){
		g.fillOval((int)x-hitx, (int)y-hitx, 2*hitx, 2*hitx);
		g.fillRect((int)x, (int)y-hity, hitx, 2*hity);
	}	
	
	// =================== Other functions =========================
	Enemy(){}
	
	Enemy(int enemyType){
		this.enemyType=enemyType;
		dead=false;
		if(enemyType<=4){
			initType0To4();
		}else if(enemyType<=9){
			initType5To9();
		}else if(enemyType<=14){
			initType10To14();
		}else if(enemyType<=19){
			initType15To19();
		}else if(enemyType<=24){
			initType20To24();
		}else if(enemyType<=29){
			initType25To29();
		}else if(enemyType<=34){
			initType30To34();
		}else if(enemyType<=39){
			initType35To39();
		}else if(enemyType<=44){
			initType40To44();
		}
		maxHP=hp;
	}
	
	void update(double px, double py, Player p){
		updateEnemy(p);
		updateEnemyBullets(px, py, p);
	}
	
	void render(Graphics g){
		if(!dead)
			renderEnemyHPBar(g);
		if(dead)
			color=new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(color.getAlpha()/1.1));
		g.setColor(color);
		if(enemyType<=4){
			renderType0To4(g);
		}else if(enemyType<=9){
			renderType5To9(g);
		}else if(enemyType<=14){
			renderType10To14(g);
		}else if(enemyType<=19){
			renderType15To19(g);
		}else if(enemyType<=24){
			renderType20To24(g);
		}else if(enemyType<=29){
			renderType25To29(g);
		}else if(enemyType<=34){
			renderType30To34(g);
		}else if(enemyType<=39){
			renderType35To39(g);
		}else if(enemyType<=44){
			renderType40To44(g);
		}
	}
	
	void renderBullets(Graphics g){ bc.render(g); }
	
	int hitPlayer(double px, double py, double pr){
		return bc.hitPlayer(px, py, pr);
	}
	
	private void updateEnemyBullets(double px, double py, Player p){
		double[] pm={};
		if(enemyType<=4){
			pm=updateParamsType0To4(p);
		}else if(enemyType<=9){
			pm=updateParamsType5To9(p);
		}else if(enemyType<=14){
			pm=updateParamsType10To14(p);
		}else if(enemyType<=19){
			pm=updateParamsType15To19(p);
		}else if(enemyType<=24){
			pm=updateParamsType20To24(p);
		}else if(enemyType<=29){
			pm=updateParamsType25To29(p);
		}else if(enemyType<=34){
			pm=updateParamsType30To34(p);
		}else if(enemyType<=39){
			pm=updateParamsType35To39(p);
		}else if(enemyType<=44){
			pm=updateParamsType40To44(p);
		}
		bc.update(pm, p, dead);
	}

	private void updateEnemy(Player p){
		if(!dead){
			if(!halt || vx<0)
				x+=vx;y+=vy;vx+=ax;vy+=ay;								
		}
		if(outOfLeftScreen())
			dead=true;
		hp=hp-p.pbullet.enemyDamageTaken((int)x, (int)y, hitx, hity, dead);
		if(hp<=0){
			dead=true;
			hp=1;
			p.credit+=bounty/((7-Level.selectedDifficulty)/2);			
		}		
	}
	
	private void renderEnemyHPBar(Graphics g){		
		double ratio=hp/maxHP;
		g.setColor(new Color(255, 0, 0, 255));
		g.fillRect((int)x-hitx, (int)y-hity-10, (int)(hitx*2*ratio), 5);
		g.setColor(new Color(255, 255, 255, 255));
		g.drawRect((int)x-hitx, (int)y-hity-10, hitx*2, 5);
	}
	
	private boolean outOfLeftScreen(){
		return x<-20;
	}
}
