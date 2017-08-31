package bullethell.applet;

import java.awt.Color;
import java.awt.Graphics;

/* Creates sets of bullets based on a timer and different patterns, and store all bullets inside its array
 * 
 * When constructing this instance, a double array pm is passed to initialize the instance.
 * The length of patParams and the meaning of each entry varies by different patterns
 * 
 * Each time update is called, a double array pm is passed to update the bullet based on various info.
 * Again, the length of patParams and the meaning of each entry varies by different patterns
 * 
 * Here's a list of all patterns currently implemented:
 * 
 * Pattern==1: A single stream of bullets
 * Upon creation, pm[0] contains the interval timer for next bullet shoot (in milliseconds), pm[1] is bullet radius, pm[2] is bullet damage, 
 * pm[3] is the relative perturbation allowed for the bullet's velocities (for e.g pm[9]=0.4 means the bullet's v can be randomly perturbed 
 * between 0.6*v~1.4*v). pm[4] is the perturbation for the bullet's acceleration.
 * Upon updating, pm[0]~pm[5] contains the spawn location, velocity, and acceleration of the bullet, pm[6], pm[7] contains the player's
 * coordinates, pm[8] is the magnitude of acceleration correction, useful for homing bullets, 
 * 
 * Pattern==2: Spreading bullets that can create more spreading bullets on the way (up to two times)
 * Upon creation, pm[0] contains the interval timer for next bullet burst (in milliseconds), pm[1] is bullet radius, pm[2] is bullet damage, 
 * pm[3] is number of bullets per burst, pm[4] is spread angle per two adjacent bullets, pm[5] and pm[6] are the perturbations,
 * pm[7], if present, is the timer that second wave of bullets will be created. pm[8], when casted to an integer, determines a couple of things: 
 * If the first wave of bullets is to be destroyed when the second wave is created, then pm[8]>0; otherwise pm[8]<0. 
 * If the direction of the second wave's central bullet velocity and acceleration will follow its creator, then pm[8]=1 or -1, else if the direction
 * will point to the player's current location, then pm[8]=2 or -2. If pm[8]>1000, pm[8] will store the timeout timer for the second wave of bullets.
 * pm[9]~pm[16] are the radius, damage, #bullets, spread angle, and rgba color of the 2nd wave
 * pm[17], if present, is the timer that third wave of bullets will be created. pm[18] works in similar fashion as pm[8]. 
 * pm[19]~[26] are the radius, damage, #bullets, spread angle and rgba color of the 3rd wave. pm[27] and pm[28], if present, will be the timeout timer
 * for second and third waves of bullets. 
 * Upon updating, pm[0]~pm[5] contains the spawn location, velocity, and acceleration of the central bullet in the spread, pm[6], pm[7] 
 * contains the player's coordinates, pm[8]~pm[11] contains the magnitude of the velocities and accelerations for 2nd and 3rd wave of bullets
 * 
 * Pattern==3: Bursts of bullets that shoots toward the same direction. Can spawn child bullets on the way once.
 * Upon creation, pm[0] contains the interval timer between bullet bursts (in milliseconds), pm[1] is the interval timer between bullets in a single burst,
 * pm[2] is number of bullets per burst, pm[3] is bullet radius, pm[4] is bullet damage. pm[5] and pm[6] are the perturbations. pm[7], if present, is the
 * interval timer that the bullets will spawn child bullets, pm[8]~pm[13] are the radius, damage and rgba color of the child bullets.
 * Upon updating, pm[0]~pm[5] contains the spawn location, velocity, and acceleration of the first bullet burst,  pm[6], pm[7] contains the player's 
 * coordinates, if pm[6] is negative, then child bullets will shoot in a random direction. pm[8], pm[9] contains the magnitude of velocities and 
 * accelerations for the child bullets, pm[10], if present, is timeout timer for secondary bullets.
 */
class BulletCreator {
	DLList<Bullet> bullets; // List of bullets created by THIS BulletCreator instance

	int pattern;
	boolean hostDead; // If the enemy that created this BulletCreator instance is dead, this is set to true
	boolean finished; // If this BulletCreator instance no longer has any Bullet instances in bullets, set this to true
	
	long startTime; // The starting time of this BulletCreator instance 
	long[] timer; // An array of timers useful for various patterns that will create more and more bullets 
	long lastShootTime; // A timer recording the latest bullet creation time for each pattern
	double[] pm; // An array of miscellaneous parameters used for different patterns
	int boundX;
	int boundY;
	Color color;
	
	// Two floating point numbers that will perturb the velocities and accelerations of each NEWLY CREATED bullet instance.
	// If a bullet has velocity vx, vy, it will be perturbed to be in the range [(1-perturbV)*vx, (1+perturbV)*vx), 
	// [(1-perturbV)*vy, (1+perturbV)*vy), same holds for acceleration 
	double perturbV;
	double perturbA;
	
	// Variables for bullet pattern 2 and 3
	Color c2;
	Color c3;
	
	// Variables for pattern 3
	double lastBurstx, lastBursty, lastBurstvx, lastBurstvy, lastBurstax, lastBurstay;
	boolean startBurst;
	int burstBulletsShot;
	long lastShootBurstTime;
	long lastShootChildTime;
	
	// =================== Bullet Pattern 1 ============================	
	private void initPat1(double[] pm){
		this.pm=pm;
		lastShootTime=0;
		timer=new long[1]; timer[0]=(long)pm[0];	
		perturbV=pm[3];perturbA=pm[4];
	}
	
	private void updatePat1(double[] param){
		if(System.currentTimeMillis()-lastShootTime>=timer[0]*(7-Level.selectedDifficulty)/2 && !hostDead){		
			Bullet b=new Bullet(pm[1], (int)pm[2], param[0], param[1], param[2], param[3], param[4], param[5], color);
			perturbVelocityAndAcceleration(b);
			addBullet(b);
			lastShootTime=System.currentTimeMillis();
		}
		if(param.length>6){
			double x, y, px, py, a;
			DLNode<Bullet> n=bullets.head;
			while(n!=null){
				x=n.e.x;y=n.e.y;px=param[6];py=param[7];a=param[8];
				double l=GameEnvironment.vectorLength(px-x, py-y);
				n.e.ax=(px-x)*a/l;
				n.e.ay=(py-y)*a/l;
				n=n.next;
			}	
		}
	}
	
	// =================== Bullet Pattern 2 ============================	
	private void initPat2(double[] pm){
		this.pm=pm;
		lastShootTime=0;
		timer=new long[3]; timer[0]=(long)pm[0];	
		perturbV=pm[5];perturbA=pm[6];
		if(pm.length>7){
			timer[1]=(long)pm[7];
			c2=new Color((int)pm[13], (int)pm[14], (int)pm[15], (int)pm[16]);
		}
		if(pm.length>17){
			timer[2]=(long)pm[17];
			c3=new Color((int)pm[23], (int)pm[24], (int)pm[25], (int)pm[26]);
		}
	}
	
	private void updatePat2(double[] param){
		// Create the first wave of bullets
		if(System.currentTimeMillis()-lastShootTime>=timer[0]*(7-Level.selectedDifficulty)/2 && !hostDead){			
			addSpreadingBullets(1, (int)pm[3], pm[4], pm[1], (int)pm[2], param[0], param[1], param[2], param[3], param[4], param[5], color, 99999);	
			lastShootTime=System.currentTimeMillis();
		}
		// Loop through all bullets and create the 2nd wave of bullets when appropriate
		if(timer[1]>0){
			DLNode<Bullet> n=bullets.head;	
			long bulletTimeout=99999;
			if(pm[8]>1000)
				bulletTimeout=(long)pm[8];
			while(n!=null){
				Bullet b=n.e;
				if(b.id==1 && System.currentTimeMillis()-b.t0>timer[1])
					createAdditionalSpreadingBullets(2, n, param[6], param[7], param[8], param[9], bulletTimeout);
				n=n.next;
			}	
		}
		// Loop through all bullets and create the 2nd wave of bullets when appropriate
		if(timer[2]>0){
			DLNode<Bullet> n=bullets.head;
			long bulletTimeout=99999;
			if(pm[18]>1000)
				bulletTimeout=(long)pm[18];
			while(n!=null){
				Bullet b=n.e;
				if(b.id==2 && System.currentTimeMillis()-b.t0>timer[2])
					createAdditionalSpreadingBullets(3, n, param[6], param[7], param[10], param[11], bulletTimeout);
				n=n.next;
			}	
		}
	}
	
	// =================== Bullet Pattern 3 ============================	
	private void initPat3(double[] pm){
		this.pm=pm;
		lastShootTime=0;
		lastShootChildTime=System.currentTimeMillis();
		timer=new long[3];
		timer[0]=(long)pm[0]; timer[1]=(long)pm[1];
		perturbV=pm[5];perturbA=pm[6];
		startBurst=false;
		if(pm.length>7){
			timer[2]=(long)pm[7];
			c2=new Color((int)pm[10], (int)pm[11], (int)pm[12], (int)pm[13]);
		}
	}
	
	private void updatePat3(double[] param){
		// Create the first wave of bullets
		if((startBurst || System.currentTimeMillis()-lastShootTime>=timer[0]*(7-Level.selectedDifficulty)/2) && !hostDead){
			if(!startBurst){
				startBurst=true;burstBulletsShot=0;
				lastBurstx=param[0];lastBursty=param[1];lastBurstvx=param[2];lastBurstvy=param[3];lastBurstax=param[4];lastBurstay=param[5];
				lastShootBurstTime=0;
			}
			if(System.currentTimeMillis()-lastShootBurstTime>timer[1]){
				Bullet b=new Bullet(pm[3], (int)pm[4], lastBurstx, lastBursty, lastBurstvx, lastBurstvy, lastBurstax, lastBurstay, color);
				perturbVelocityAndAcceleration(b);
				addBullet(b);
				lastShootBurstTime=System.currentTimeMillis();
				burstBulletsShot++;
			}
			if(burstBulletsShot>(int)pm[2]){
				lastShootTime=System.currentTimeMillis();
				startBurst=false;
			}
		}
		// Loop through every parent bullet to spawn child bullets
		if(pm.length>7){
			if(System.currentTimeMillis()-lastShootChildTime>timer[2]){
				DLNode<Bullet> n=bullets.head;
				long bulletTimeout=99999;
				if(param.length>10)
					bulletTimeout=(long)param[10];
				while(n!=null){
					Bullet b=n.e;
					if(b.id==1){
						double vx, vy, ax, ay;double px=param[6];double py=param[7];
						if(param[6]<0){
							 double theta=Math.random()*Math.PI*2;
							 vx=Math.cos(theta)*param[8];vy=Math.sin(theta)*param[8];
							 ax=Math.cos(theta)*param[9];ay=Math.sin(theta)*param[9];
						}else{
							double l=GameEnvironment.vectorLength(px-b.x, py-b.y);
							double ux=(px-b.x)/l;double uy=(py-b.y)/l;
							vx=param[8]*ux;vy=param[8]*uy;ax=param[9]*ux;ay=param[9]*uy;
						}
						Bullet b2=new Bullet(pm[8], (int)pm[9], b.x, b.y, vx, vy, ax, ay, c2);
						perturbVelocityAndAcceleration(b2);
						b2.id=2;b2.T=bulletTimeout;
						addBullet(b2);					
					}
					n=n.next;
				}
				lastShootChildTime=System.currentTimeMillis();
			}
		}
	}
	
	
	
	// ================= Other functions =====================
	private void createAdditionalSpreadingBullets(int waveId, DLNode<Bullet> n, double px, double py, double v, double a, long bulletTimeout){
		Bullet b=n.e;
		double r, delta;
		int dmg, bCount;
		if(waveId==2){
			r=pm[9];delta=pm[12];dmg=(int)pm[10];bCount=(int)pm[11];
			if(pm[8]>0)
				bullets.remove(n);
			else
				n.e.id=3; // Make sure this bullet cannot keep creating secondary bullets and eat up all the JVM memory
			double[] va=secondaryBulletVelocitiesAndAccelerations((int)pm[8], b, px, py, v, a);
			addSpreadingBullets(waveId, bCount, delta, r, dmg, b.x, b.y, va[0], va[1], va[2], va[3], c2, bulletTimeout);
		}else{
			r=pm[19];delta=pm[22];dmg=(int)pm[20];bCount=(int)pm[21];	
			if(pm[18]>0)
				bullets.remove(n);
			else
				n.e.id=3;
			double[] va=secondaryBulletVelocitiesAndAccelerations((int)pm[18], b, px, py, v, a);
			addSpreadingBullets(waveId, bCount, delta, r, dmg, b.x, b.y, va[0], va[1], va[2], va[3], c3, bulletTimeout);
		}
	}
	// Given the central bullet's position, velocity and acceleration, create a fan of #bCount bullets with spread angle delta between them,
	// perturb the velocities and accelerations, and add these bullets to the bullet list
	private void addSpreadingBullets(int id, int bCount, double delta, double r, int dmg, double x, double y, double vx, double vy, double ax, double ay, Color color, long bulletTimeout){
		double sinTheta, cosTheta;
		Bullet b;
		b=new Bullet(r, dmg, x, y, vx, vy, ax, ay, color);
		b.id=id;b.T=bulletTimeout;
		perturbVelocityAndAcceleration(b);
		addBullet(b);
		for(int i=1;i<=bCount/2;i++){
			sinTheta=Math.sin(delta*i);cosTheta=Math.cos(delta*i);
			b=new Bullet(r, dmg, x, y, vx*cosTheta-vy*sinTheta, vx*sinTheta+vy*cosTheta, 
					ax*cosTheta-ay*sinTheta, ax*sinTheta+ay*cosTheta, color); // rotate the bullet by theta
			b.id=id;b.T=bulletTimeout;
			perturbVelocityAndAcceleration(b);
			addBullet(b);
			b=new Bullet(r, dmg, x, y, vx*cosTheta+vy*sinTheta, -vx*sinTheta+vy*cosTheta, 
					ax*cosTheta+ay*sinTheta, -ax*sinTheta+ay*cosTheta, color); // rotate the bullet by -theta
			b.id=id;b.T=bulletTimeout;
			perturbVelocityAndAcceleration(b);
			addBullet(b);
		}
	}
	
	private double[] secondaryBulletVelocitiesAndAccelerations(int pm, Bullet b, double px, double py, double v, double a){
		double[] va=new double[4];
		double x=b.x;double y=b.y;double vx=b.vx;double vy=b.vy;
		double lv=GameEnvironment.vectorLength(vx, vy);
		if(pm==1 || pm==-1){
				va[0]=v*vx/lv;
				va[1]=v*vy/lv;
				va[2]=a*vx/lv;
				va[3]=a*vy/lv;
		}else if(pm==2 || pm==-2){
			double l=GameEnvironment.vectorLength(x-px, y-py);
			va[0]=v*(px-x)/l;va[1]=v*(py-y)/l;va[2]=a*(px-x)/l;va[3]=a*(py-y)/l;
		}
		return va;
	}
	
	// =================== Other methods ============================
	BulletCreator(){} // Used by the final boss only
	
	BulletCreator(int pattern, double[] pm, Color color){
		startTime=System.currentTimeMillis();
		boundX=GameEnvironment.boundX;
		boundY=GameEnvironment.boundY;
		this.pattern=pattern;
		bullets=new DLList<Bullet>();
		hostDead=false;
		finished=false;
		this.color=color;
		switch(this.pattern){
			case 1: initPat1(pm); break;
			case 2: initPat2(pm); break;
			case 3: initPat3(pm); break;
			default: break;
		}
	}
	
	/* The main function of this class. Will be called repeated by the gaming environment to update the bullets based on
	 * different bullet patterns.
	 */
	void update(double pm[], Player p, boolean hostDead){
		this.hostDead=hostDead;
		switch(this.pattern){
			case 1: updatePat1(pm); break;
			case 2: updatePat2(pm); break;
			case 3: updatePat3(pm); break;
		}
		updateBullets(p);
	}
	
	int hitPlayer(double px, double py, double pr){
		DLNode<Bullet> n=bullets.head;
		while(n!=null){
			int dmg=n.e.hitPlayer(px, py, pr);
			if(dmg>0){
				bullets.remove(n);
				return dmg;
			}
			n=n.next;
		}
		return 0;
	}
	
	void render(Graphics g){
		DLNode<Bullet> n=bullets.head;
		while(n!=null){
			n.e.render(g);
			n=n.next;
		}
	}
	
	private void addBullet(Bullet b){
		bullets.add(b);
	}
	
	private void updateBullets(Player p){
		boolean noMoreBullets=true;
		DLNode<Bullet> n=bullets.head;
		while(n!=null){
			noMoreBullets=false;
			n.e.update();
			if((p.curPulseRadius>0 && bulletWithinEMP(n.e, p)) || n.e.offScreen() || n.e.timedOut())
				bullets.remove(n);
			n=n.next;
		}
		if(noMoreBullets && System.currentTimeMillis()-startTime>3000)
			finished=true;
		else
			finished=false;
	}
	
	private boolean bulletWithinEMP(Bullet b, Player p){
		double l=GameEnvironment.vectorLength(p.x-b.x, p.y-b.y);
		return l<p.curPulseRadius;
	}
	
	private void perturbVelocityAndAcceleration(Bullet b){
		b.vx*=Math.random()*perturbV*2+1-perturbV;
		b.vy*=Math.random()*perturbV*2+1-perturbV;
		b.ax*=Math.random()*perturbA*2+1-perturbA;
		b.ay*=Math.random()*perturbA*2+1-perturbA;
	}
}
