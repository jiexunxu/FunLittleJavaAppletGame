package bullethell.applet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/* The boss has 7 phases. Each phase corresponds to a rainbow color. Boss attack patterns are drawn randomly from a library of possible attacks
 * As the boss's phase increases, each attack pattern get stronger, and multiple attack patterns are more and more likely to appear at the same time
 */
class Boss extends Enemy{
	// The boss has 7 phases, which means the boss has hp*maxPhase total HP. 
	int phase=0;
	int maxPhase=6;
	
	// Stores all attack patterns this boss has. The boss's attack patterns will be randomly taken from this library.
	// As the boss's phase increases, the boss's attack power increases too
	BulletCreator[] bcsLib;
	boolean[] libsToUpdate;
	BulletCreator[] oldbcsLib;
	boolean[] oldlibsToUpdate;
	long patternStartTime;
	long patternDuration;
	long patternGraceStartTime;
	long patternGraceDuration;
	boolean patternGraceStarted;
	
	// Used to count the #steps taken to move the boss and its rotating arcs to pivot positions
	int setToMoveToPivotStepCount;
	int setToMoveToPivotMaxSteps;
	
	// Variables to render the top, middle and bottom rotating arcs
	Arc[] arcs;	
	int rearArcDist;
	int rearArcR;
	// Used to animate boss rotation
	double baseModelAngle;
	double baseModelRotatingSpd;
	int decayingBaseModelAlpha;
	// Used to animation a new phase explosion
	int newPhaseExplosionRadius=0;
	int newPhaseExplosionMaxRadius=(int)(GameEnvironment.boundX*1.5);
	double newPhaseExplosionSpd;
	double newPhaseExplosionMaxSpd=81;
	int newPhaseExplosionAlpha;
	
	// This is the position that the boss will move to at the start of each phase
	int pivotX=GameEnvironment.boundX*3/4;
	int pivotY=GameEnvironment.boundY/2;
	double[][] pivots;
	boolean movingToPivots;
	
	BufferedImage bossBackground;
	BufferedImage background;
	
	long phase6Timer;
	
	Boss(){		
		hitx=210;hity=hitx;
		rearArcDist=(int)(hitx*1.1);
		rearArcR=hitx/3;
		bounty=1200000;
		bc=new BulletCreator(); // ad-hoc instance. bc.finished is set to true when boss is defeated and boss's death animation is finished
		enemyType=999;
		x=GameEnvironment.boundX+hitx;
		y=GameEnvironment.boundY/2;
		baseModelAngle=0;
		bossBackground=GameEnvironment.bossBackground;
		setToMoveToPivotMaxSteps=50;
		decayingBaseModelAlpha=255;
		
		arcs=new Arc[5];
		initNewPhase();		
	}
	
	@Override
	void update(double px, double py, Player p){
		if(movingToPivots){ 
			if(setToMoveToPivotStepCount>=setToMoveToPivotMaxSteps){
				movingToPivots=false;
				x=pivotX;y=pivotY;vx=0;vy=0;ax=0;ay=0;
				for(int i=0;i<arcs.length;i++){
					if(pivots.length>1){
						arcs[i].x=pivots[i+1][0];arcs[i].y=pivots[i+1][1];
					}
					arcs[i].vx=0;arcs[i].vy=0;arcs[i].ax=0;arcs[i].ay=0;
				}
				switch(phase){
					case 0: initPhase0(); break;
					case 1: initPhase1(); break;
					case 2: initPhase2(); break;
					case 3: initPhase3(); break;
					case 4: initPhase4(); break;
					case 5: initPhase5(); break;
					case 6: initPhase6(); break;
					default: break;
				}
			}else{
				setToMoveToPivotStepCount++;
			}
		}else{	
			switch(phase){
				case 0: updatePhase0(p); break;
				case 1: updatePhase1(p); break;
				case 2: updatePhase2(p); break;
				case 3: updatePhase3(p); break;
				case 4: updatePhase4(p); break;
				case 5: updatePhase5(p); break;
				case 6: updatePhase6(p); break;
				default: break;
			}
		}
		updateBoss(p);
		if(!dead){
			for(int i=0;i<arcs.length;i++)
				arcs[i].update(this);
			updateBCsLib(p);
		}
	}
	
	@Override
	void render(Graphics g){
		renderBossBackground(g);
		renderBaseModel(g);
		if(!dead){
			renderBossHPBar(g);
			renderRotatingArcs(g);
		}
		if(newPhaseExplosionRadius>0)
			renderNewPhaseExplosion(g);
	}
	
	@Override
	void renderBullets(Graphics g){
		if(!dead){
			for(int i=0;i<bcsLib.length;i++)
				bcsLib[i].render(g);
		}
	}
	
	@Override
	int hitPlayer(double px, double py, double pr){
		int dmg=0;
		for(int i=0;i<bcsLib.length;i++){
			dmg=bcsLib[i].hitPlayer(px, py, pr);
			if(dmg>0)
				return dmg;
		}
		return dmg;
	}
	
	private void initNewPhase(){
		maxHP=130000-(5-Level.selectedDifficulty)*20000;
		hp=(int)maxHP;
		baseModelRotatingSpd=(5.5-phase*0.5)*Math.PI/180;
		double X=GameEnvironment.boundX;double Y=GameEnvironment.boundY;
		arcs[0]=new Arc(x-rearArcDist, y-rearArcDist, rearArcR, 0, 60, 14);
		arcs[1]=new Arc(x+rearArcDist, y-rearArcDist, rearArcR, 90, 60, 14);
		arcs[2]=new Arc(x-rearArcDist, y+rearArcDist, rearArcR, 180, 60, 14);
		arcs[3]=new Arc(x+rearArcDist, y+rearArcDist, rearArcR, 270, 60, 14);
		arcs[4]=new Arc(x, y, hitx*5/6, 0, 30, 7);
		if(phase<=1 || phase==6){
			pivots=new double[6][2];
			pivots[0][0]=pivotX;pivots[0][1]=pivotY;
			pivots[1][0]=pivotX-rearArcDist;pivots[1][1]=pivotY-rearArcDist;
			pivots[2][0]=pivotX+rearArcDist;pivots[2][1]=pivotY-rearArcDist;
			pivots[3][0]=pivotX-rearArcDist;pivots[3][1]=pivotY+rearArcDist;
			pivots[4][0]=pivotX+rearArcDist;pivots[4][1]=pivotY+rearArcDist;
			pivots[5][0]=pivotX;pivots[5][1]=pivotY;
		}else if(phase==2){
			pivots=new double[6][2];
			double r=rearArcR;double m=5;
			pivots[0][0]=pivotX;pivots[0][1]=pivotY;
			pivots[1][0]=r+m;pivots[1][1]=r+m;
			pivots[2][0]=X-(r+m);pivots[2][1]=r+m;
			pivots[3][0]=r+m;pivots[3][1]=Y-(r+m);
			pivots[4][0]=X-(r+m);pivots[4][1]=Y-(r+m);
			pivots[5][0]=pivotX;pivots[5][1]=pivotY;
		}else if(phase==3){
			pivots=new double[6][2];double m=rearArcR/3;
			pivots[0][0]=pivotX;pivots[0][1]=pivotY;
			pivots[1][0]=rearArcR-m;pivots[1][1]=rearArcR-m;
			pivots[2][0]=GameEnvironment.boundX-rearArcR+m;pivots[2][1]=rearArcR-m;
			pivots[3][0]=GameEnvironment.boundX-rearArcR+m;pivots[3][1]=GameEnvironment.boundY-rearArcR+m;
			pivots[4][0]=rearArcR-m;pivots[4][1]=GameEnvironment.boundY-rearArcR+m;
			pivots[5][0]=pivotX;pivots[5][1]=pivotY;
		}else if(phase==4){
			pivots=new double[6][2];
			pivots[0][0]=pivotX;pivots[0][1]=pivotY;
			pivots[1][0]=pivotX;pivots[1][1]=GameEnvironment.boundY/6;
			pivots[2][0]=pivotX;pivots[2][1]=2*GameEnvironment.boundY/6;
			pivots[3][0]=pivotX;pivots[3][1]=4*GameEnvironment.boundY/6;
			pivots[4][0]=pivotX;pivots[4][1]=5*GameEnvironment.boundY/6;
			pivots[5][0]=pivotX;pivots[5][1]=pivotY;
		}else if(phase==5){
			pivots=new double[6][2];
			pivots[0][0]=pivotX;pivots[0][1]=pivotY;
			pivots[1][0]=rearArcR;pivots[1][1]=rearArcR;
			pivots[2][0]=GameEnvironment.boundX-rearArcR;pivots[2][1]=rearArcR;
			pivots[3][0]=GameEnvironment.boundX-rearArcR;pivots[3][1]=GameEnvironment.boundY-rearArcR;
			pivots[4][0]=rearArcR;pivots[4][1]=GameEnvironment.boundY-rearArcR;
			pivots[5][0]=pivotX;pivots[5][1]=pivotY;
		}
		setToMoveToPivotPosition(1/(double)setToMoveToPivotMaxSteps);
		initBCsLib();
		patternStartTime=0;
		patternGraceStartTime=0;
		patternDuration=15000-phase*500;
		patternGraceDuration=3000+phase*300;
		patternGraceStarted=true;
		
		// Copy image to background
		background=new BufferedImage(bossBackground.getWidth(), bossBackground.getHeight(), bossBackground.getType());
		Graphics g=background.getGraphics();
		g.drawImage(bossBackground, 0, 0, null);
		g.dispose();
		
		for(int i=0;i<background.getWidth();i++){
			for(int j=0;j<background.getHeight();j++){
				Color color=new Color(background.getRGB(i, j));
				int red=color.getRed();int green=color.getGreen();int blue=color.getBlue();
				switch(phase){
					case 0:red/=20;green/=20;blue/=20;break;
					case 1:red/=6/1;green/=20;blue/=20;break;
					case 2:red/=6/2;green/=20;blue/=20;break;
					case 3:red/=6/3;green/=6/1;blue/=20;break;
					case 4:red/=6/3;green/=6/2;blue/=20;break;
					case 5:red/=6/3;green/=6/3;blue/=6/1;break;
					case 6:red/=6/3;green/=6/3;blue/=6/2;break;
					default:break;
				}
				Color newColor=new Color(red, green, blue);
				background.setRGB(i, j, newColor.getRGB());
			}
		}
	}
	
	int debugId=-1;
	// Following two functions work with all attack patterns this boss has. This is definitely the most interesting part of the code
	private void initBCsLib(){
		double[] pm;	
		bcsLib=new BulletCreator[22];
		// Quickly shoots bullets towards player
		pm=new double[5];pm[0]=100-phase*5;pm[1]=5;pm[2]=32+phase*2;pm[3]=0;pm[4]=0;
		bcsLib[0]=new BulletCreator(1, pm, new Color(255, 100, 100, 255)); 
		// Shoots super wide arcs of bullets towards the player
		pm=new double[7];pm[0]=2000-phase*120;pm[1]=5;pm[2]=30+phase;pm[3]=60+phase*5;pm[4]=4*Math.PI/180;pm[5]=0;pm[6]=0;
		bcsLib[1]=new BulletCreator(2, pm, new Color(50, 255, 200, 255));
		// Shoots bursts of inaccurate bullets towards the player
		pm=new double[7];pm[0]=1100-phase*100;pm[1]=6;pm[2]=40+phase*3;pm[3]=9+phase*2;pm[4]=1*Math.PI/180;pm[5]=0.6;pm[6]=0.1;
		bcsLib[2]=new BulletCreator(2, pm, new Color(200, 120, 30, 255));
		// Shoots big bullets with a bit of homing abilities towards the player
		pm=new double[5];pm[0]=1000-phase*50;pm[1]=50;pm[2]=200+phase*20;pm[3]=0.1;pm[4]=0;
		bcsLib[3]=new BulletCreator(1, pm, new Color(255, 255, 255, 110)); 	
		// Quickly shoots a single bullet that disintegrate into a circle once
		pm=new double[17];
		pm[0]=800-50*phase;pm[1]=20;pm[2]=70+phase*7;pm[3]=1;pm[4]=0;pm[5]=0;pm[6]=0;
		pm[7]=3000;pm[8]=1;pm[9]=5;pm[10]=24+phase*2;pm[11]=31;pm[12]=12*Math.PI/180;pm[13]=160;pm[14]=200;pm[15]=0;pm[16]=255;
		bcsLib[4]=new BulletCreator(2, pm, new Color(220, 255, 30, 255)); 
		// Shoots spreads of bullets that disintegrate once. Disintegrated bullets will retarget the player
		pm=new double[17];
		pm[0]=2000-80*phase;pm[1]=24;pm[2]=120+phase*12;pm[3]=4+phase/2;pm[4]=14*Math.PI/180;pm[5]=0;pm[6]=0;
		pm[7]=3000;pm[8]=2;pm[9]=6;pm[10]=24+phase*2;pm[11]=3+phase/2;pm[12]=21*Math.PI/180;pm[13]=0;pm[14]=30;pm[15]=160;pm[16]=255;
		bcsLib[5]=new BulletCreator(2, pm, new Color(0, 110, 180, 255)); 
		// Shoots a single big bullet that disintegrate twice. Both levels of disintegrated bullets will retarget the player
		pm=new double[27];
		pm[0]=3000-50*phase;pm[1]=100;pm[2]=240+phase*30;pm[3]=1;pm[4]=0;pm[5]=0.0;pm[6]=0;
		pm[7]=3000;pm[8]=2;pm[9]=24;pm[10]=80+phase*8;pm[11]=3+phase;pm[12]=11*Math.PI/180;pm[13]=0;pm[14]=120;pm[15]=25;pm[16]=255;
		pm[17]=3000;pm[18]=2;pm[19]=3;pm[20]=10+phase*2;pm[21]=3+phase/2;pm[22]=12*Math.PI/180;pm[23]=0;pm[24]=90;pm[25]=0;pm[26]=255;
		bcsLib[6]=new BulletCreator(2, pm, new Color(0, 150, 50, 255)); 
		// Shoots bursts of inaccurate bullets that disintegrate twice. Both levels of disintegrated bullets are inaccurate and will not retarget the player
		pm=new double[27];
		pm[0]=1200-60*phase;pm[1]=30;pm[2]=100+phase*10;pm[3]=2+phase/3;pm[4]=10*Math.PI/180;pm[5]=0.7;pm[6]=0.7;
		pm[7]=2000;pm[8]=1;pm[9]=15;pm[10]=50+phase*5;pm[11]=2+phase/5;pm[12]=10*Math.PI/180;pm[13]=0;pm[14]=25;pm[15]=120;pm[16]=255;
		pm[17]=2000;pm[18]=1;pm[19]=7;pm[20]=15+phase*3;pm[21]=2+phase/6;pm[22]=10*Math.PI/180;pm[23]=0;pm[24]=0;pm[25]=90;pm[26]=255;
		bcsLib[7]=new BulletCreator(2, pm, new Color(0, 50, 150, 255)); 
		// Shoots wide arcs of slow bullets that will spawn additional bullets twice on the way towards the player
		pm=new double[27];
		pm[0]=1200-20*phase;pm[1]=13;pm[2]=90+phase*9;pm[3]=2+phase/3;pm[4]=20*Math.PI/180;pm[5]=0;pm[6]=0;
		pm[7]=5000;pm[8]=-2;pm[9]=13;pm[10]=90+phase*9;pm[11]=1+phase/3;pm[12]=10*Math.PI/180;pm[13]=180;pm[14]=60;pm[15]=120;pm[16]=255;
		pm[17]=5000;pm[18]=-2;pm[19]=13;pm[20]=90+phase*9;pm[21]=2;pm[22]=10*Math.PI/180;pm[23]=180;pm[24]=60;pm[25]=120;pm[26]=255;
		bcsLib[8]=new BulletCreator(2, pm, new Color(180, 60, 120, 255)); 
		// Shoots continuous streams of bullets towards player
		pm=new double[5];pm[0]=30;pm[1]=5;pm[2]=13+phase;pm[3]=0;pm[4]=0;
		bcsLib[9]=new BulletCreator(1, pm, new Color(80, 160, 255, 255)); 
		// Shoots slow 360 degree bullets from the center.
		pm=new double[7];pm[0]=900+phase*20;pm[1]=11;pm[2]=68+phase*7;pm[3]=41;pm[4]=8*Math.PI/180;pm[5]=0;pm[6]=0;
		bcsLib[10]=new BulletCreator(2, pm, new Color(50, 255, 200, 255));
		// Fast shooting bullets that retargets toward the player twice on the way
		pm=new double[27];
		pm[0]=370-15*phase;pm[1]=10;pm[2]=50+phase*8;pm[3]=1;pm[4]=0;pm[5]=0;pm[6]=0;
		pm[7]=2000;pm[8]=2;pm[9]=10;pm[10]=50+phase*8;pm[11]=1;pm[12]=0;pm[13]=50;pm[14]=170;pm[15]=255;pm[16]=255;
		pm[17]=2000;pm[18]=2;pm[19]=10;pm[20]=50+phase*8;pm[21]=1;pm[22]=0;pm[23]=50;pm[24]=170;pm[25]=255;pm[26]=255;
		bcsLib[11]=new BulletCreator(2, pm, new Color(50, 170, 255, 255));
		// Fast shooting bullets that leaves an explosive trailer on the field for a few seconds
		pm=new double[17];
		pm[0]=700-50*phase;pm[1]=13;pm[2]=65+phase*10;pm[3]=1;pm[4]=0;pm[5]=0.1;pm[6]=0;
		pm[7]=4000;pm[8]=6000+phase*500;pm[9]=64;pm[10]=40+phase*8;pm[11]=1;pm[12]=0;pm[13]=255;pm[14]=90;pm[15]=90;pm[16]=100;
		bcsLib[12]=new BulletCreator(2, pm, new Color(220, 150, 150, 255));
		// Spreads of inaccurate bullets that splinter onces. The splinter retargets the player. The splinter will leave a trail of marks on the field
		pm=new double[27];
		pm[0]=2000-80*phase;pm[1]=27;pm[2]=95+phase*10;pm[3]=3+phase/2;pm[4]=7*Math.PI/180;pm[5]=0.5;pm[6]=0.05;
		pm[7]=3000;pm[8]=2;pm[9]=10;pm[10]=54+phase*9;pm[11]=2+phase/3;pm[12]=18*Math.PI/180;pm[13]=15;pm[14]=110;pm[15]=230;pm[16]=255;
		pm[17]=2000;pm[18]=4000;pm[19]=36;pm[20]=66+phase*9;pm[21]=1;pm[22]=0;pm[23]=10;pm[24]=100;pm[25]=200;pm[26]=100;
		bcsLib[13]=new BulletCreator(2, pm, new Color(20, 125, 255, 255));
		// Shoots long and fast bursts of streams of bullets toward the player
		pm=new double[7];
		pm[0]=1000-phase*50;pm[1]=30;pm[2]=70;pm[3]=8;pm[4]=17+phase*2;pm[5]=0;pm[6]=0;
		bcsLib[14]=new BulletCreator(3, pm, new Color(0, 50, 120, 255));
		// Shoots bursts of bullets that will continuously create slow moving bullets in random directions
		pm=new double[14];
		pm[0]=2100-phase*110;pm[1]=90;pm[2]=40;pm[3]=14;pm[4]=28+phase*3;pm[5]=0;pm[6]=0;
		pm[7]=2000-phase*100;pm[8]=12;pm[9]=22+phase*3;pm[10]=0;pm[11]=60;pm[12]=0;pm[13]=255;
		bcsLib[15]=new BulletCreator(3, pm, new Color(0, 60, 0, 255));
		// Shoots bursts of bullets from the four rotating arcs that will create child bullets toward the player. These child bullets disperse after a certain time
		pm=new double[14];
		pm[0]=1200-phase*25;pm[1]=180;pm[2]=10;pm[3]=12;pm[4]=21+phase*2;pm[5]=0;pm[6]=0;
		pm[7]=3000-phase*120;pm[8]=7;pm[9]=14+phase*2;pm[10]=90;pm[11]=10;pm[12]=0;pm[13]=255;
		bcsLib[16]=new BulletCreator(3, pm, new Color(100, 15, 0, 255));
		// Shoots bursts of bullets from the four rotating arcs. They target random directions and spawn child bullets that also target random directions. Child bullets disperse after a certain time.
		pm=new double[14];
		pm[0]=700-phase*10;pm[1]=210;pm[2]=7+phase/3;pm[3]=16;pm[4]=32+phase*3;pm[5]=0;pm[6]=0;
		pm[7]=1800;pm[8]=12;pm[9]=24+phase*2;pm[10]=10;pm[11]=0;pm[12]=90;pm[13]=255;
		bcsLib[17]=new BulletCreator(3, pm, new Color(15, 0, 100, 255));
		// Shoot spreads of bullets anywhere on the border of the battlefield to the player
		pm=new double[7];pm[0]=1500-phase*120;pm[1]=15;pm[2]=31+phase*4;pm[3]=5+phase/2;pm[4]=9*Math.PI/180;pm[5]=0;pm[6]=0;
		bcsLib[18]=new BulletCreator(2, pm, new Color(140, 30, 120, 255));
		// Shoots horizontal or vertical bullets anywhere on the border of the battlefield
		pm=new double[5];pm[0]=120-phase*5;pm[1]=6;pm[2]=15+phase;pm[3]=0;pm[4]=0;
		bcsLib[19]=new BulletCreator(1, pm, new Color(170, 80, 150, 255));
		// Shoots bullets anywhere on the border of the battlefield towards the player. Bullets leave an explosive trailer after certain time
		pm=new double[17];
		pm[0]=1100-70*phase;pm[1]=16;pm[2]=56+phase*10;pm[3]=1;pm[4]=0;pm[5]=0.3;pm[6]=0.3;
		pm[7]=4500;pm[8]=4000+phase*500;pm[9]=52;pm[10]=36+phase*8;pm[11]=1;pm[12]=0;pm[13]=80;pm[14]=200;pm[15]=130;pm[16]=100;
		bcsLib[20]=new BulletCreator(2, pm, new Color(80, 200, 130, 255));
		// Shoots a slow, easy-to-dodge, and innocent-looking black bullet that will actually one-shot-kill the player if hit
		pm=new double[5];pm[0]=2600-phase*100;pm[1]=21;pm[2]=10000;pm[3]=0;pm[4]=0;
		bcsLib[21]=new BulletCreator(1, pm, new Color(0, 0, 0, 255)); 
	}
	
	private void updateBCsLib(Player p){
		double px=p.x;double py=p.y;double[] pm;
		int arcId=(int)(Math.random()*4);
		
		pm=new double[6];pm[0]=arcs[arcId].x;pm[1]=arcs[arcId].y;		
		targetTowardsPlayer(pm, px, py, 2, 0.1, Math.random()*0.2+0.9);
		bcsLib[0].update(pm, p, libsToUpdate[0]);
		
		pm=new double[6];pm[0]=arcs[arcId].x;pm[1]=arcs[arcId].y;	
		targetTowardsPlayer(pm, px, py, 5, 0, 1);
		bcsLib[1].update(pm, p, libsToUpdate[1]);
		
		pm=new double[6];pm[0]=arcs[arcId].x;pm[1]=arcs[arcId].y;	
		targetTowardsPlayer(pm, px, py, 2.4, 0.2, Math.random()+0.5);
		bcsLib[2].update(pm, p, libsToUpdate[2]);;
		
		pm=new double[9];pm[0]=arcs[4].x;pm[1]=arcs[4].y;
		targetTowardsPlayer(pm, px, py, 2, 0.05, 1);
		pm[6]=px;pm[7]=py;pm[8]=0.1;
		bcsLib[3].update(pm, p, libsToUpdate[3]);
		
		pm=new double[10];pm[0]=arcs[arcId].x;pm[1]=arcs[arcId].y;	
		targetTowardsPlayer(pm, px, py, 5.4, 0, Math.random()*0.4+0.8);
		pm[6]=px;pm[7]=py;pm[8]=2.1;pm[9]=0.1;
		bcsLib[4].update(pm, p, libsToUpdate[4]);
		
		pm=new double[10];pm[0]=arcs[arcId].x;pm[1]=arcs[arcId].y;	
		targetTowardsPlayer(pm, px, py, 7, 0, Math.random()*0.3+0.85);
		pm[6]=px;pm[7]=py;pm[8]=5.2;pm[9]=0;
		bcsLib[5].update(pm, p, libsToUpdate[5]);
		
		pm=new double[12];pm[0]=arcs[4].x;pm[1]=arcs[4].y;
		targetTowardsPlayer(pm, px, py, 5, 0, Math.random()*0.6+0.7);
		pm[6]=px;pm[7]=py;pm[8]=5.5;pm[9]=0;pm[10]=6;pm[11]=0;
		bcsLib[6].update(pm, p, libsToUpdate[6]);
		
		pm=new double[12];pm[0]=arcs[arcId].x;pm[1]=arcs[arcId].y;	
		targetTowardsPlayer(pm, px, py, 5, 0.1, Math.random()+0.5);
		pm[6]=px;pm[7]=py;pm[8]=5;pm[9]=0.1;pm[10]=5;pm[11]=0.1;
		bcsLib[7].update(pm, p, libsToUpdate[7]);
		
		pm=new double[12];pm[0]=arcs[4].x;pm[1]=arcs[4].y;
		targetTowardsPlayer(pm, px, py, 3, 0, 1);
		pm[6]=px;pm[7]=py;pm[8]=3;pm[9]=0;pm[10]=3;pm[11]=0;
		bcsLib[8].update(pm, p, libsToUpdate[8]);
		
		pm=new double[6];pm[0]=arcs[arcId].x;pm[1]=arcs[arcId].y;	
		targetTowardsPlayer(pm, px, py, 5, 0, 1);
		bcsLib[9].update(pm, p, libsToUpdate[9]);
		
		pm=new double[6];pm[0]=arcs[4].x;pm[1]=arcs[4].y;
		targetTowardsPlayer(pm, px, py, 3, 0, 1);
		bcsLib[10].update(pm, p, libsToUpdate[10]);
		
		pm=new double[12];pm[0]=arcs[arcId].x;pm[1]=arcs[arcId].y;	
		targetTowardsPlayer(pm, px, py, 7.2, 0, 1);
		pm[6]=px;pm[7]=py;pm[8]=7.2;pm[9]=0;pm[10]=7.2;pm[11]=0;
		bcsLib[11].update(pm, p, libsToUpdate[11]);
		
		pm=new double[12];pm[0]=arcs[arcId].x;pm[1]=arcs[arcId].y;	
		targetTowardsPlayer(pm, px, py, 6.3, 0, Math.random()*0.8+0.6);
		pm[6]=px;pm[7]=py;pm[8]=0;pm[9]=0;
		bcsLib[12].update(pm, p, libsToUpdate[12]);
		
		pm=new double[12];pm[0]=arcs[4].x;pm[1]=arcs[4].y;
		targetTowardsPlayer(pm, px, py, 2, 0.1, Math.random()*0.4+0.8);
		pm[6]=px;pm[7]=py;pm[8]=1.5;pm[9]=0.2;pm[10]=0;pm[11]=0;
		bcsLib[13].update(pm, p, libsToUpdate[13]);
		
		pm=new double[6];pm[0]=arcs[4].x;pm[1]=arcs[4].y;		
		targetTowardsPlayer(pm, px, py, 14, 0, 1);
		bcsLib[14].update(pm, p, libsToUpdate[14]);
		
		pm=new double[11];pm[0]=arcs[4].x;pm[1]=arcs[4].y;		
		targetTowardsPlayer(pm, px, py, 6, 0, 1);
		pm[6]=-1;pm[7]=-1;pm[8]=3.2;pm[9]=0.1;pm[10]=99999;
		bcsLib[15].update(pm, p, libsToUpdate[15]);
		
		pm=new double[11];pm[0]=arcs[arcId].x;pm[1]=arcs[arcId].y;		
		targetTowardsPlayer(pm, px, py, 5, 0, 1);
		pm[6]=px;pm[7]=py;pm[8]=1.2;pm[9]=0.15;pm[10]=3000;
		bcsLib[16].update(pm, p, libsToUpdate[16]);
		
		pm=new double[11];pm[0]=arcs[arcId].x;pm[1]=arcs[arcId].y;
		targetTowardsPlayer(pm, px, py, 3.2, 0.15, 1);		
		double theta=Math.random()*Math.PI-Math.PI/2;
		double vx=pm[2];double vy=pm[3];double ax=pm[4];double ay=pm[5];
		pm[2]=vx*Math.cos(theta)-vy*Math.sin(theta);pm[3]=vx*Math.sin(theta)+vy*Math.cos(theta);
		pm[4]=ax*Math.cos(theta)-ay*Math.sin(theta);pm[5]=ax*Math.sin(theta)+ay*Math.cos(theta);
		pm[6]=-1;pm[7]=-1;pm[8]=2.7;pm[9]=0.13;pm[10]=3600;
		bcsLib[17].update(pm, p, libsToUpdate[17]);
		
		pm=new double[6];randomPointAtGameBorder(pm);
		targetTowardsPlayer(pm, px, py, 6.4, 0, Math.random()*0.8+0.6);
		bcsLib[18].update(pm, p, libsToUpdate[18]);
		
		pm=new double[6];randomPointAtGameBorder(pm);
		double v=4.5*(Math.random()*0.4+0.8);double a=0.1*(Math.random()*0.4+0.8);
		if(Math.random()>0.5){
			pm[2]=v*Math.signum(1-pm[0]);pm[3]=0;pm[4]=Math.signum(pm[2])*a;pm[5]=0;
		}else{
			pm[3]=v*Math.signum(1-pm[1]);pm[2]=0;pm[5]=Math.signum(pm[3])*a;pm[4]=0;
		}
		bcsLib[19].update(pm, p, libsToUpdate[19]);
		
		pm=new double[12];randomPointAtGameBorder(pm);
		targetTowardsPlayer(pm, px, py, 5, 0, Math.random()*0.6+0.7);
		pm[6]=px;pm[7]=py;pm[8]=0;pm[9]=0;
		bcsLib[20].update(pm, p, libsToUpdate[20]);
		
		pm=new double[6];pm[0]=arcs[4].x;pm[1]=arcs[4].y;		
		targetTowardsPlayer(pm, px, py, 3, 0.1, Math.random()*0.2+0.9);
		bcsLib[21].update(pm, p, libsToUpdate[21]);
	}
	
	private void targetTowardsPlayer(double[] pm, double px, double py, double v, double a, double alpha){
		double l=GameEnvironment.vectorLength(px-pm[0], py-pm[1]);
		double ux=(px-pm[0])/l*alpha;double uy=(py-pm[1])/l*alpha;
		pm[2]=v*ux;pm[3]=v*uy;pm[4]=a*ux;pm[5]=a*uy;
	}
	
	private void randomPointAtGameBorder(double[] pm){
		if(Math.random()<0.5){
			pm[0]=GameEnvironment.boundX;
			if(Math.random()<0.5)
				pm[0]=0;
			pm[1]=Math.random()*GameEnvironment.boundY;
		}else{
			pm[0]=Math.random()*GameEnvironment.boundX;
			pm[1]=GameEnvironment.boundY;
			if(Math.random()<0.5)
				pm[1]=0;
		}
	}
	
	// =============== Phase 0 =================
	private void initPhase0(){		
		vx=0;vy=-0.4;ax=0;ay=0;	
		for(int i=0;i<arcs.length;i++)
			arcs[i].followBoss=true;
	}
	
	private void updatePhase0(Player p){
		ay=0.02*Math.signum(GameEnvironment.boundY/2-y);
	}
	
	// =============== Phase 1 =================
	private void initPhase1(){
		vx=0.7;vy=0;ax=0;ay=0;
		for(int i=0;i<arcs.length;i++)
			arcs[i].followBoss=true;
	}
	
	private void updatePhase1(Player p){
		ax=0.05*Math.signum(GameEnvironment.boundX*2/3-x);
	}
	
	// =============== Phase 2 =================
	private void initPhase2(){
		vx=0;vy=-0.4;ax=0;ay=0;	
		arcs[4].followBoss=true;
	}
	
	private void updatePhase2(Player p){
		ay=0.02*Math.signum(GameEnvironment.boundY/2-y);
	}
	
	// =============== Phase 3 =================
	private void initPhase3(){
		vx=0;vy=-0.8;ax=0;ay=0;
		arcs[4].followBoss=true;
		for(int i=0;i<4;i++)
			arcs[i].vx=8.1;		
	}
	
	private void updatePhase3(Player p){
		ay=0.04*Math.signum(GameEnvironment.boundY/2-y);
		for(int i=0;i<4;i++){
			if(arcs[i].x<arcs[i].r && arcs[i].vx<0)
				arcs[i].vx=-arcs[i].vx;
			if(arcs[i].x>GameEnvironment.boundX-arcs[i].r && arcs[i].vx>0)
				arcs[i].vx=-arcs[i].vx;
		}
	}
	
	// =============== Phase 4 =================
	private void initPhase4(){
		vx=1.1;vy=0;ax=0;ay=0;
		for(int i=0;i<arcs.length;i++)
			arcs[i].followBoss=true;
	}
	
	private void updatePhase4(Player p){
		ax=0.07*Math.signum(GameEnvironment.boundX*6/10-x);
	}
	
	// =============== Phase 5 =================
	private void initPhase5(){
		vx=0.6;vy=0.6;ax=0;ay=0;
		arcs[4].followBoss=true;
		for(int i=0;i<4;i++)
			arcs[i].vx=11;
	}
	
	private void updatePhase5(Player p){
		ay=0.03*Math.signum(pivotY-y);
		ax=0.03*Math.signum(pivotX-x);
		double r=arcs[0].r;
		for(int i=0;i<4;i++){
			if(arcs[i].x<r && arcs[i].vx<0){
				arcs[i].x=r;arcs[i].vy=-arcs[i].vx;arcs[i].vx=0;
			}else if(arcs[i].y>GameEnvironment.boundY-r && arcs[i].vy>0){
				arcs[i].y=GameEnvironment.boundY-r;arcs[i].vx=arcs[i].vy;arcs[i].vy=0;
			}else if(arcs[i].x>GameEnvironment.boundX-r && arcs[i].vx>0){
				arcs[i].x=GameEnvironment.boundX-r;arcs[i].vy=-arcs[i].vx;arcs[i].vx=0;
			}else if(arcs[i].y<r && arcs[i].vy<0){
				arcs[i].y=r;arcs[i].vx=arcs[i].vy;arcs[i].vy=0;
			}
		}
	}
	
	// =============== Phase 6 =================
	private void initPhase6(){
		vx=0;vy=0;ax=0;ay=0;phase6Timer=0;
		phase6Timer=System.currentTimeMillis();
	}
	
	private void updatePhase6(Player p){
		if(System.currentTimeMillis()-phase6Timer>3000){
			for(int i=0;i<4;i++){
				while(true){
					double newx=Math.random()*(GameEnvironment.boundX-arcs[i].r*2)+arcs[i].r;
					double newy=Math.random()*(GameEnvironment.boundY-arcs[i].r*2)+arcs[i].r;
					if((newx-p.x>130 || newx-p.x<-130) && (newy-p.y>130 || newy-p.y<-130)){
						arcs[i].x=newx;arcs[i].y=newy;
						break;
					}
				}				
			}
			double newx=Math.random()*(GameEnvironment.boundX/2-hitx)+GameEnvironment.boundX/2;		
			double newy=Math.random()*(GameEnvironment.boundY-hity*2)+hity;
			this.x=newx;this.y=newy;
			arcs[4].x=newx;arcs[4].y=newy;
			phase6Timer=System.currentTimeMillis();
		}
	}
		
	// =============== Other private functions =================
	private void updateBoss(Player p){
		x+=vx;y+=vy;vx+=ax;vy+=ay;	
		hp=hp-p.pbullet.enemyDamageTaken((int)x, (int)y, hitx, hity, dead);
		if(hp<=0  && !dead){
			phase++;
			newPhaseExplosionRadius=hitx/2;
			newPhaseExplosionAlpha=255;
			newPhaseExplosionSpd=newPhaseExplosionMaxSpd;
			if(phase>maxPhase){
				dead=true;
				p.credit+=bounty/((7-Level.selectedDifficulty)/2);
			}
			initNewPhase();
		}
		if(dead){
			decayingBaseModelAlpha-=1;
			if(decayingBaseModelAlpha<0){
				decayingBaseModelAlpha=0;
				bc.finished=true;
			}
		}
		if(!patternGraceStarted){
			long timeLapsed=System.currentTimeMillis()-patternStartTime;
			if(timeLapsed>patternDuration){	
				patternGraceStarted=true;
				for(int i=0;i<libsToUpdate.length;i++)
					libsToUpdate[i]=true;
				patternGraceStartTime=System.currentTimeMillis();				
			}
		}else{
			long timeLapsed=System.currentTimeMillis()-patternGraceStartTime;
			if(timeLapsed>patternGraceDuration){
				patternGraceStarted=false;
				computeLibsToUpdate();
				patternStartTime=System.currentTimeMillis();
			}
		}
	}
	
	private void computeLibsToUpdate(){
		libsToUpdate=new boolean[bcsLib.length];
		double phaseRatio=(double)phase/maxPhase;
		double rng=Math.random();
		int patternCount=1;
		if(rng<phaseRatio-0.9) 
			patternCount=3;
		else if(rng<phaseRatio+0.9) 
			patternCount=2;   
		
		for(int i=0;i<libsToUpdate.length;i++)
			libsToUpdate[i]=true;
		
		if(phase>=5)
			libsToUpdate[21]=false;
		
		int patCounter=0;
		while(patCounter<patternCount){
			int idx=(int)(Math.random()*libsToUpdate.length);
			if(libsToUpdate[idx]){
				libsToUpdate[idx]=false;
				patCounter++;
			}
		}
		
		if(debugId>=0){
			for(int i=0;i<libsToUpdate.length;i++)
				libsToUpdate[i]=true;
			libsToUpdate[debugId]=false;
		}
	}
	
	// Move the boss and its rotating arcs (if pivots.length>1) to positions specified in pivots with velocity factor vf
	private void setToMoveToPivotPosition(double vf){
		movingToPivots=true;
		setToMoveToPivotStepCount=0;
		vx=vf*(pivots[0][0]-x);vy=vf*(pivots[0][1]-y);
		ax=0;ay=0;
		if(pivots.length>1){
			for(int i=1;i<pivots.length;i++){
				arcs[i-1].vx=vf*(pivots[i][0]-arcs[i-1].x);
				arcs[i-1].vy=vf*(pivots[i][1]-arcs[i-1].y);
			}
		}
	}
	
	private void renderBossBackground(Graphics g){
		g.drawImage(background, 0, 0, null);
	}
	
	private void renderBaseModel(Graphics g){
		// paints a heptagram. Starting vertex is vertex 0 on top, vertex 1 is the one next to vertex 0 counterclockwise, vertex 2 the one next to vertex 1 counterclockwise etc
		int[] xpts=new int[7];int[] ypts=new int[7];
		double delta=Math.PI*2/7;double r=(double)hitx;
		int x0=(int)x; int y0=(int)y;
		double cosA=Math.cos(baseModelAngle);double sinA=Math.sin(baseModelAngle);
		int xh0, yh0;int[] hepOrder={0, 3, 6, 2, 5, 1, 4};
		for(int i=0;i<xpts.length;i++){
			xh0=heptagramCoorX(hepOrder[i], delta, r);yh0=heptagramCoorY(hepOrder[i], delta, r);
			xpts[i]=(int)(cosA*xh0-sinA*yh0+x0);
			ypts[i]=(int)(sinA*xh0+cosA*yh0+y0);
		}
		Color bodyColor=new Color(255-phase*36, 255-phase*36, 255-phase*36, decayingBaseModelAlpha);
		g.setColor(bodyColor);
		g.fillPolygon(xpts, ypts, xpts.length);
		baseModelAngle+=baseModelRotatingSpd;
		
		int starBallR=36;
		if(phase<1){
			g.setColor(new Color(255, 0, 0, 255));
			g.fillOval(xpts[0]-starBallR, ypts[0]-starBallR, starBallR*2, starBallR*2);		
		}
		if(phase<2){
			g.setColor(new Color(255, 127, 0, 255));
			g.fillOval(xpts[1]-starBallR, ypts[1]-starBallR, starBallR*2, starBallR*2);
		}
		if(phase<3){
			g.setColor(new Color(255, 255, 0, 255));
			g.fillOval(xpts[2]-starBallR, ypts[2]-starBallR, starBallR*2, starBallR*2);
		}
		if(phase<4){
			g.setColor(new Color(0, 255, 0, 255));
			g.fillOval(xpts[3]-starBallR, ypts[3]-starBallR, starBallR*2, starBallR*2);
		}		
		if(phase<5){
			g.setColor(new Color(0, 255, 255, 255));
			g.fillOval(xpts[4]-starBallR, ypts[4]-starBallR, starBallR*2, starBallR*2);
		}
		if(phase<6){
			g.setColor(new Color(0, 0, 255, 255));
			g.fillOval(xpts[5]-starBallR, ypts[5]-starBallR, starBallR*2, starBallR*2);
		}
		if(phase<7){
			g.setColor(new Color(127, 0, 255, 255));
			g.fillOval(xpts[6]-starBallR, ypts[6]-starBallR, starBallR*2, starBallR*2);
		}
	}
	
	private void renderBossHPBar(Graphics g){		
		double ratio=hp/maxHP;
		g.setColor(new Color(0, 255, 0, 255));
		int hpbarLength=(int)(hity*2*ratio);
		g.fillRect((int)x+hitx+36, (int)y+hity-hpbarLength, 7, hpbarLength);
		g.setColor(new Color(255, 255, 255, 255));
		g.drawRect((int)x+hitx+36, (int)y-hity, 7, hitx*2);
	}
	
	private void renderRotatingArcs(Graphics g){
		switch(phase){
			case 0: g.setColor(new Color(255, 0, 0, 127));break;
			case 1: g.setColor(new Color(255, 127, 0, 127));break;
			case 2: g.setColor(new Color(255, 255, 0, 127));break;
			case 3: g.setColor(new Color(0, 255, 0, 127));break;
			case 4: g.setColor(new Color(0, 255, 255, 127));break;
			case 5: g.setColor(new Color(0, 0, 255, 127));break;
			case 6: g.setColor(new Color(127, 0, 255, 127));break;
			default: break;
		}
		for(int i=0;i<arcs.length;i++)
			arcs[i].render(g);		
	}
	
	private void renderNewPhaseExplosion(Graphics g){
		switch(phase-1){
			case 0: g.setColor(new Color(255, 0, 0, newPhaseExplosionAlpha));break;
			case 1: g.setColor(new Color(255, 127, 0, newPhaseExplosionAlpha));break;
			case 2: g.setColor(new Color(255, 255, 0, newPhaseExplosionAlpha));break;
			case 3: g.setColor(new Color(0, 255, 0, newPhaseExplosionAlpha));break;
			case 4: g.setColor(new Color(0, 255, 255, newPhaseExplosionAlpha));break;
			case 5: g.setColor(new Color(0, 0, 255, newPhaseExplosionAlpha));break;
			case 6: g.setColor(new Color(127, 0, 255, newPhaseExplosionAlpha));break;
			default: break;
		}
		int r=newPhaseExplosionRadius;
		for(int i=0;i<36;i++){
			g.drawOval((int)x-r, (int)y-r, r*2, r*2);
			r-=17;
		}
		newPhaseExplosionAlpha-=2;
		if(newPhaseExplosionAlpha<0)
			newPhaseExplosionAlpha=0;
		newPhaseExplosionRadius+=newPhaseExplosionSpd;
		newPhaseExplosionSpd/=1.06;
		if(newPhaseExplosionRadius>newPhaseExplosionMaxRadius)
			newPhaseExplosionRadius=0;
			
	}
	
	private int heptagramCoorX(int v, double delta, double r){
		return (int)(Math.cos(Math.PI/2+delta*v)*r);
	}
	
	private int heptagramCoorY(int v, double delta, double r){
		return -(int)(Math.sin(Math.PI/2+delta*v)*r);
	}
}

// The rotating arc that accompanies the boss. Will also shoot bullets from them
class Arc{
	double x;
	double y;
	double vx=0;
	double vy=0;
	double ax=0;
	double ay=0;
	int r;
	int arcAngle;
	int arcSpan;
	int arcSpd;
	boolean followBoss=false;
	
	Arc(double x, double y, int r, int arcAngle, int arcSpan, int arcSpd){
		this.x=x;this.y=y;this.r=r;this.arcAngle=arcAngle;this.arcSpan=arcSpan;this.arcSpd=arcSpd;
	}
	
	void update(Boss b){
		if(followBoss){
			x+=b.vx;y+=b.vy;vx+=b.ax;vy+=b.ay;
		}else{
			x+=vx;y+=vy;vx+=ax;vy+=ay;
		}
	}
	
	void render(Graphics g){
		g.fillArc((int)x-r, (int)y-r, 2*r, 2*r, arcAngle, arcSpan);
		arcAngle+=arcSpd;
	}
}
