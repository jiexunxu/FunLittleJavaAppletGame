package bullethell.applet;

import java.awt.Color;
import java.awt.Graphics;

class PlayerBullet {
	// x and y coordinates of all the bullets. Since at most maxBulletsPerStream #bullets per bullet stream can be 
	// on the screen, the second dimension of the array is maxBulletsPerStream. An unoccupied array spot is always -1.
	double[][] x;
	double[][] y;
	int maxBulletsPerStream;
	// x and y velocities of all the bullets
	double[] vx;
	double[] vy;
	// width and height of each bullet
	int w;
	int h;
	// x, y bounds of the screen
	int boundX;
	int boundY;
	
	double bulletSpeed;
	double bulletAngleSpread;
 
	int bulletDmg;
	long lastBulletShootTimer;
	long bulletShootingInterval;
	
	Color color;
	
	PlayerBullet(){		
		w=12;h=4;
		maxBulletsPerStream=7;
		bulletSpeed=17;
		bulletAngleSpread=3*Math.PI/180;
		lastBulletShootTimer=0L; 
		boundX=GameEnvironment.boundX;
		boundY=GameEnvironment.boundY;
	}
	
	/* Ranges from level 1 to level 11. Level 1 shoots one stream of bullets, level 2 shoots 2 streams. 
	 * The angle between the streams is bulletAngleSpread degrees. Each additional level gives one additional stream of bullets.
	 */
	void newPlayerBulletLevel(int level){
		if(level==12){
			bulletAngleSpread=2*Math.PI/180;
			level=16;
		}
		x=new double[level][maxBulletsPerStream];y=new double[level][maxBulletsPerStream];
		for(int i=0;i<level;i++){
			for(int j=0;j<x[0].length;j++){
				x[i][j]=-1;
				y[i][j]=-1;
			}
		}
		vx=new double[level];vy=new double[level];
		double angle=bulletAngleSpread*(level-1)/2;
		for(int i=0;i<level;i++){
			vx[i]=bulletSpeed*Math.cos(angle);
			vy[i]=-bulletSpeed*Math.sin(angle);			
			angle=angle-bulletAngleSpread;
		}
		color=new Color(7, 150, 255, 90);
	}
	
	void shootBullet(double px, double py){
		if(System.currentTimeMillis()-lastBulletShootTimer>bulletShootingInterval){	
			lastBulletShootTimer=System.currentTimeMillis();
			for(int i=0;i<x.length;i++){
				for(int j=0;j<x[0].length;j++){
					if(x[i][j]<0){
						x[i][j]=px;
						y[i][j]=py;
						break;
					}
				}
			}
		}	
	}
	
	void updateBullets(){
		for(int i=0;i<x.length;i++){
			for(int j=0;j<x[0].length;j++){
				if(x[i][j]>=0){
					x[i][j]+=vx[i];
					y[i][j]+=vy[i];
				}
				if(outOfBounds(i, j)){
					x[i][j]=-1;
					y[i][j]=-1;
				}
			}
		}
	}
	
	/* Test if any bullets hit the enemy centered at (x, y) with a hitbox size of (2*hitx, 2*hity).
	 * If true, remove the bullet that hits that enemy and return true. Otherwise return false.
	 */
	int enemyDamageTaken(int x, int y, int hitx, int hity, boolean isdead){
		if(isdead)
			return 0;
		int dmg=0;
		for(int i=0;i<this.x.length;i++){
			for(int j=0;j<this.x[0].length;j++){
				if(this.x[i][j]-x<hitx+w/2 && this.x[i][j]-x>-hitx-w/2 && this.y[i][j]-y<hity+h/2 && this.y[i][j]-y>-hity-h/2){
					this.x[i][j]=-1;this.y[i][j]=-1;
					dmg=dmg+bulletDmg;
				}
			}
		}
		return dmg;
	}
	
	void render(Graphics g){
		g.setColor(color);
		for(int i=0;i<x.length;i++)
			for(int j=0;j<x[0].length;j++)
				if(x[i][j]>=0)
					g.fillRect((int)(x[i][j]-w/2), (int)(y[i][j]-h/2), (int)w, (int)h);
	}
	
	private boolean outOfBounds(int i, int j){
		return (x[i][j]<0 || x[i][j]>boundX || y[i][j]<0 || y[i][j]>boundY);
	}
}
