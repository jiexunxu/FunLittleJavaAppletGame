package bullethell.applet;

import java.awt.Color;
import java.awt.Graphics;

class Bullet {
	// after the timer exceeds T, this bullet is deleted
	long T;
	// The time mark that this bullet is created
	long t0;
	// used by BulletCreator to identify at which stage this bullet is created
	int id;
	// x and y velocities of the bullet
	double vx;
	double vy;
	// x and y coordinates of the bullet
	double x;
	double y;
	// x and y accelerations of the bullet
	double ax;
	double ay;
	// bullet radius
	double r;
	// bullet damage to player if hit
	int dmg;
	// screen boundings
	int boundX;
	int boundY;
	// bullet color
	Color color;
	
	Bullet(double r, int dmg, double x, double y, double vx, double vy, double ax, double ay, Color c){
		this.r=r;this.dmg=dmg;this.x=x;this.y=y;this.vx=vx;this.vy=vy;this.ax=ax;this.ay=ay;this.color=c;
		boundX=GameEnvironment.boundX;
		boundY=GameEnvironment.boundY;
		t0=System.currentTimeMillis();
		T=99999;
		id=1;
	}
	
	void update(){
		x+=vx;y+=vy;vx+=ax;vy+=ay;
	}
	
	int hitPlayer(double px, double py, double pr){
		if(Math.sqrt((px-x)*(px-x)+(py-y)*(py-y))<r+pr)
			return dmg;
		return 0;
	}
	
	boolean offScreen(){
		return (x<-30 || x>boundX+30 || y<-30 || y>boundY+30);
	}
	
	boolean timedOut(){
		return System.currentTimeMillis()-t0>T;
	}
	
	void render(Graphics g){		
		g.setColor(color);
		g.fillOval((int)(x-r), (int)(y-r), (int)(2*r), (int)(2*r));
		g.setColor(Color.WHITE);
		g.drawOval((int)(x-r), (int)(y-r), (int)(2*r), (int)(2*r));
	}
}
