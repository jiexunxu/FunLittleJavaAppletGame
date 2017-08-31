package bullethell.applet;

import java.awt.Graphics;

/* This class is responsible for spawning all kinds of enemies at the appropriate in-game time. 
 * Level design happen in the method private void setLevelInfo(), refer there for details 
 */
class Level {
	static final int MAXLEVEL=32;
	static int selectedDifficulty;
	static boolean bossClearedOnHell=false;
	
	int level;
	int maxlevelCleared;
	boolean levelStarted;
	boolean bossLevel; // indicate whether this level is a boss level
		
	long gameStartTime;
	long timeLapsed;	
	long timeLastEnemySpawned;
		
	boolean gameover; // true if player is dead
	boolean gamewon; // true if player reached certain time mark in non-boss levels, and destroyed boss in boss-levels
	boolean bossCleared; // true if player just defeated the final boss
	long gameoverStartTime; // After gameover or gamewon, remain in the level for 5 seconds then return to the stage selection screen
	
	DLList<Enemy> enemies;

	Event[] levelInfo;
	int lastLevelInfoEventIdx;
	long levelWonTimeMark;
	
	/* The int array info determines the frequency certain enemies will spawn at certain second marks in the game. For each enemy spawn information,
	 * always starts with the time mark in seconds, followed by the type of enemies, followed by spawn interval in milliseconds, followed by a -1
	 * The last two entries must be a time mark followed by a -1 to indicate that after that time mark enemies will not spawn anymore
	 * 
	 * For example, if levelInfo={5 0 1000 -1 15 -1 20 0 500 -1 35 0 1000 1 2000 -1 50 -1} means at 5s mark, spawn type 0 enemy every 1000ms, then at 15s mark,
	 * take a break (spawn nothing), then at 20s mark, spawn type 0 enemy every 500ms, then at 35s mark, spawn type 0 enemy every 1000ms and spawn type 1
	 * enemy every 2000ms. Finally at 50s mark, stop spawning everything
	 * 
	 * All emenies spawned have a chance to promote to a higher type (but always the same class). This increases randomness.
	 * 
	 * The info entered here is based on the highest difficulty. The function reduceDifficulty below will reduce the spawn frequency of all enemies in the
	 * input array. For each difficulty level below the hardest, enemies spawn time is increased by 100%
	 * 
	 * As a note, small class enemies are numbered 0, 15, 30, 45, 60, 75, 90, medium class enemies are numbered 5, 20, 35, 50, 65, 80, 95, large class enemies are 10, 25, 40, 55, 70, 85, 100
	 */
	private void setLevelInfo(){
		int[] info={};
		switch(this.level){
			case 1: int[] lvl1={0, -1, 3, 0, 2000, -1, 20, 0, 1400, -1, 40, 0, 700, -1, 60, -1}; 
					info=lvl1;break;
			case 2: int[] lvl2={0, -1, 3, 15, 1900, -1, 20, 15, 2000, 0, 1800, -1, 40, 15, 700, -1, 60, -1}; 
					info=lvl2;break;
			case 3: int[] lvl3={0, -1, 3, 30, 1800, -1, 20, 30, 3000, 15, 2700, 0, 2400, -1, 40, 5, 5000, 0, 2000, -1, 60, -1};
					info=lvl3;break;
			case 4: int[] lvl4={0, -1, 3, 45, 1700, -1, 20, 45, 2000, 30, 1800, -1, 40, 20, 5000, 15, 1900, -1, 60, -1};
					info=lvl4;break;
			case 5: int[] lvl5={0, -1, 3, 60, 1600, -1, 20, 60, 4000, 45, 3600, 30, 3200, 15, 2800, -1, 40, 35, 4000, 30, 1800, -1, 60, -1};
					info=lvl5;break;
			case 6: int[] lvl6={0, -1, 3, 75, 1500, -1, 20, 75, 3000, 60, 2700, 45, 2400, -1, 40, 50, 4000, 45, 1700, -1, 60, -1};
					info=lvl6;break;
			case 7: int[] lvl7={0, -1, 3, 90, 1400, -1, 20, 90, 2000, 75, 1800, -1, 40, 65, 4000, 60, 1600, -1, 60, -1};
					info=lvl7;break;
			case 8: int[] lvl8={0, 0, 500, -1, 10, 15, 500, -1, 20, 30, 500, -1, 30, 45, 500, -1, 40, 80, 4000, 75, 1500, -1, 60, -1};
					info=lvl8;break;
			case 9: int[] lvl9={0, 60, 500, -1, 13, 75, 500, -1, 26, 90, 500, -1, 40, 95, 4000, 90, 1400, -1, 60, -1};
					info=lvl9;break;
			case 10: int[] lvl10={0, 15, 2400, 60, 2400, 75, 2400, -1, 20, 0, 2000, 90, 2000, 80, 3000, -1, 40, 5, 6000, 65, 6000, 70, -1};
					info=lvl10;break;
			case 11: int[] lvl11={0, 30, 2100, 90, 2100, 0, 2100, -1, 20, 45, 1800, 90, 1800, 20, 3000, -1, 40, 20, 5500, 80, 5500, 70, -1};
					info=lvl11;break;
			case 12: int[] lvl12={0, 45, 1800, 90, 1800, 60, 1800, -1, 20, 75, 1600, 30, 1600, 95, 3000, -1, 40, 35, 5000, 50, 5000, 70, -1};
					info=lvl12;break;
			case 13: int[] lvl13={0, 0, 1500, 45, 1500, 90, 1500, -1, 20, 15, 1500, 75, 1500, 35, 3000, -1, 40, 5, 4500, 95, 4500, 70, -1};
					info=lvl13;break;
			case 14: int[] lvl14={0, 10, 11000, -1, 20, 0, 500, -1, 30, 0, 1000, 5, 2000, -1, 50, 0, 2000, 5, 4000, 10, 30000, -1, 70, -1};
					info=lvl14;break;
			case 15: int[] lvl15={0, 25, 11000, -1, 20, 15, 500, -1, 30, 15, 1000, 20, 2000, -1, 50, 15, 2000, 20, 4000, 25, 30000, -1, 70, -1};
					info=lvl15;break;
			case 16: int[] lvl16={0, 40, 11000, -1, 20, 30, 500, -1, 30, 30, 1000, 35, 2000, -1, 50, 30, 2000, 35, 4000, 40, 30000, -1, 70, -1};
					info=lvl16;break;
			case 17: int[] lvl17={0, 55, 11000, -1, 20, 45, 500, -1, 30, 45, 1000, 50, 2000, -1, 50, 45, 2000, 50, 4000, 55, 30000, -1, 70, -1};
					info=lvl17;break;
			case 18: int[] lvl18={0, 70, 11000, -1, 20, 60, 500, -1, 30, 60, 1000, 65, 2000, -1, 50, 60, 2000, 65, 4000, 70, 30000, -1, 70, -1};
					info=lvl18;break;
			case 19: int[] lvl19={0, 85, 11000, -1, 20, 75, 500, -1, 30, 75, 1000, 80, 2000, -1, 50, 75, 2000, 80, 4000, 85, 30000, -1, 70, -1};
					info=lvl19;break;
			case 20: int[] lvl20={0, 100, 11000, -1, 20, 90, 500, -1, 30, 90, 1000, 95, 2000, -1, 50, 90, 2000, 95, 4000, 100, 30000, -1, 70, -1};
					info=lvl20;break;
			case 21: int[] lvl21={0, 0, 500, 30, 600, 45, 700, -1, 30, 15, 500, 75, 600, 90, 700, -1, 60, 15, 1000, 45, 1000, 60, 1100, 75, 1100, 90, 1200, -1, 90, -1};
					info=lvl21;break;
			case 22: int[] lvl22={0, 20, 1600, 50, 1700, -1, 30, 5, 1500, 80, 1600, -1, 60, 35, 2000, 65, 2100, 95, 2200, -1, 100, -1};
					info=lvl22;break;
			case 23: int[] lvl23={0, 10, 30000, -1, 15, 25, 30000, -1, 30, 40, 30000, -1, 45, 55, 30000, -1, 60, 70, 30000, -1, 75, 85, 30000, -1, 90, 100, 30000, -1, 105, -1};
					info=lvl23;break;
			case 24: int[] lvl24={};
					info=lvl24;break;
			case 25: int[] lvl25={};
					info=lvl25;break;
			case 26: int[] lvl26={};
					info=lvl26;break;
			case 27: int[] lvl27={};
					info=lvl27;break;
			case 28: int[] lvl28={};
					info=lvl28;break;
			case 29: int[] lvl29={};
					info=lvl29;break;
			case 30: int[] lvl30={};
					info=lvl30;break;
			case 31: int[] lvl31={};
					info=lvl31;break;
			default:break;
		}
		parseInfo(info);
	}
	
	Level(){
		levelStarted=false;
		maxlevelCleared=31;
	}
	
	void initLevel(int level, int difficulty){
		this.level=level;

		selectedDifficulty=difficulty;
		
		gameStartTime=System.currentTimeMillis();
		timeLapsed=0;
		timeLastEnemySpawned=0;
		
		gameover=false;
		gamewon=false;
		bossCleared=false;
		gameoverStartTime=0;
		
		enemies=new DLList<Enemy>();
		
		bossLevel=(level==MAXLEVEL);
		if(!bossLevel)
			setLevelInfo();
		else
			enemies.add(new Boss());
		
		levelStarted=true;
	}
	
	void update(double px, double py, Player p){
		timeLapsed=System.currentTimeMillis()-gameStartTime;
		if(timeLapsed>levelWonTimeMark && enemies.head==null){
			if(bossLevel){
				bossCleared=true;
				if(Level.selectedDifficulty==5){
					Level.bossClearedOnHell=true;
				}
			}else{
				gamewon=true;
				if(level>maxlevelCleared)
					maxlevelCleared=level;
			}
		}
		
		if(!gamewon && !bossLevel)
			updateEnemySpawn();
		updateEnemies(px, py, p);
		
		if(gameover || gamewon || bossCleared){
			if(gameoverStartTime==0)
				gameoverStartTime=System.currentTimeMillis();
			long timer=5000;
			if(bossCleared)
				timer=11000;
			if(System.currentTimeMillis()-gameoverStartTime>timer){
				bossCleared=false;
				bossLevel=false;
				levelStarted=false;
			}
		}
	}
	
	int hitPlayer(double px, double py, double pr){
		DLNode<Enemy> n=enemies.head;
		while(n!=null){
			int dmg=n.e.hitPlayer(px, py, pr);
			if(dmg>0)
				return dmg;
			n=n.next;
		}
		return 0;
	}
	
	void render(Player p, Graphics g){
		DLNode<Enemy> n=enemies.head;
		while(n!=null){
			n.e.render(g);
			n=n.next;
		}
		if(!gameover){
			p.pbullet.render(g);
			p.render(g);
		}
		n=enemies.head;
		while(n!=null){
			n.e.renderBullets(g);
			n=n.next;
		}
	}
		
	private void parseInfo(int[] info){
		int eventCount=0;
		for(int i=0;i<info.length;i++)
			if(info[i]==-1)
				eventCount++;
		levelInfo=new Event[eventCount];
		int lastEventIdx=0;
		eventCount=0;
		for(int i=0;i<info.length;i++){
			if(info[i]==-1){
				Event e=new Event(i-lastEventIdx);
				for(int j=lastEventIdx;j<i;j++)
					e.event[j-lastEventIdx]=info[j];
				levelInfo[eventCount]=e;
				eventCount++;
				lastEventIdx=i+1;
			}
		}
		levelWonTimeMark=levelInfo[levelInfo.length-1].event[0]*1000;
		lastLevelInfoEventIdx=0;
	}
	
	private void updateEnemySpawn(){
		if(lastLevelInfoEventIdx+1<levelInfo.length && timeLapsed>levelInfo[lastLevelInfoEventIdx+1].event[0]*1000){
			lastLevelInfoEventIdx++;
			timeLastEnemySpawned=0; // If spawn event is changed, reset the enemy spawn timer to guarantee an immediate enemy spawn
		}
		int[] event=levelInfo[lastLevelInfoEventIdx].event;
		int i=1;
		while(i<event.length){
			int baseType=event[i];
			int enemyTypePromote=randomEnemyPromoteBasedOnLevel((double)level);
			int enemyType=Math.min(baseType+enemyTypePromote, baseType-baseType%5+4);
			spawnEnemies(enemyType, event[i+1]);
			i+=2;
		}
	}
	
	// Spawn a single type of enemies at a specified interval
	private void spawnEnemies(int type, long interval){
		if(System.currentTimeMillis()-timeLastEnemySpawned>interval){
			timeLastEnemySpawned=System.currentTimeMillis();
			Enemy e=new Enemy(type);
			enemies.add(e);
		}		
	}
	
	private void updateEnemies(double px, double py, Player p){
		DLNode<Enemy> n=enemies.head;
		while(n!=null){
			n.e.update(px, py, p);
			if(n.e.dead && n.e.bc.finished)
				enemies.remove(n);
			n=n.next;
		}
	}
	
	// Returns an integer in the range of 0~4. The higher the current level, the more likely this function will return high numbers
	// such as 4, 3. 
	private int randomEnemyPromoteBasedOnLevel(double curLevel){
		double rng=Math.random();
		if(rng<curLevel/MAXLEVEL-0.5) 
			return 4; // Chance to return 4 is zero until curLevel>MAXLEVEL/2, then it linearly increases to 0.5
		else if(rng<curLevel/MAXLEVEL-0.2) 
			return 3; // Chance to return 3 is zero until curLevel>MAXLEVEL/5, then it linearly increases to 0.8 if 4 is not returned
		else if(rng<curLevel/MAXLEVEL)
			return 2; // Chance to return 2 linearly increases to 1
		else if(rng<curLevel/MAXLEVEL+0.3)
			return 1; // Chance to return 1 is 0.4 at the start of the level, and linearly increases until curLEVEL>MAXLEVEL*0.7, and will always return 1 afterwards if 2, 3, 4 are not returned
		return 0;
	}
}

class Event{
	int[] event;
	Event(int n) { event=new int[n]; }
}