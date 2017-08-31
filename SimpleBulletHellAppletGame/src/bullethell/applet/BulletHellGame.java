/* steps to publish the applet:
 * 1) Right click on SimpleBulletHellAppletGame project folder, export everything as a jar named bulletHellApplet.jar
 * 2) Put the exported jar in X:\EclipseWorkSpace\SimpleBulletHellAppletGame\src\. There should be an index.html in that folder too.
 * 3) On Windows, open command prompt. Type cd "C:\Program Files\Java\jdk1.8.0_91\bin\"
 * 4) On command prompt, type jarsigner.exe -keystore myKeystore -verbose "X:\EclipseWorkSpace\SimpleBulletHellAppletGame\src\bulletHellApplet.jar" appletkey
 * 5) Once the jar is signed, copy bulletHellApplet.jar and index.html to the website hosting this applet, and you're done
 * 
 */
package bullethell.applet;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URL;

import javax.imageio.ImageIO;

public class BulletHellGame extends Applet implements MouseListener, MouseMotionListener{
	private static final long serialVersionUID = 1L;
	
	// Number of millseconds spent per frame. This is the inverse of FPS, and is used in the main game loop
	long singleFrameTimer;
	// Current mouse coordinates. Updated whenever mouse moves
	int mouseX;
	int mouseY;
	
	// Double buffer rendering variables
	Graphics bufferGraphics;
	Image offscreen;
	Dimension fieldDim;
	
	GameEnvironment game;
	
	@Override public void start(){ initGame(); }
	
	@Override
	public void paint(Graphics g){
		bufferGraphics.clearRect(0, 0, fieldDim.width, fieldDim.height);
		game.renderOneFrame(bufferGraphics);
		g.drawImage(offscreen, 0, 0, this);
	}
	
	@Override 
	public void mouseClicked(MouseEvent e) {
		if(game.level.levelStarted)
			game.player.releaseEMP(); 
	}

	@Override public void mouseEntered(MouseEvent e) {}

	@Override public void mouseExited(MouseEvent e) {}

	@Override public void mousePressed(MouseEvent e) {}

	@Override 
	public void mouseReleased(MouseEvent e) {
		if(!game.level.levelStarted)
			game.processLevelSelectionFrame(); 
	}
	
	@Override public void mouseMoved(MouseEvent e) { 
		mouseX=e.getX(); mouseY=e.getY(); 
	}
	
	@Override public void mouseDragged(MouseEvent e) {}
		
	@Override public void update(Graphics g){ paint(g); }

	public void startGameThreads(){ 	
    	Thread musicThread=new Thread(){
    		public void run(){
    			gameMusic();
    		}
    	};
    	
    	Thread mainGameThread=new Thread(){
    		public void run(){
    			gameLoop();
    		}
    	};
    	
    	musicThread.start();
    	mainGameThread.start();
 	}
	
	private void gameLoop(){
		long lastTimer, timeDiff;
		// TODO: When restarting the applet, kill this thread
		while(true){
			lastTimer=System.currentTimeMillis();
			game.processOneFrame(mouseX, mouseY);
			this.repaint();
			timeDiff=System.currentTimeMillis()-lastTimer;
			if(timeDiff<singleFrameTimer)
				try { Thread.sleep(singleFrameTimer-timeDiff); } catch (InterruptedException e) {}
		}
	}
	
	private void gameMusic(){
		while(true){
			for(int i=0;i<game.music.length;i++){
				if(game.musicPlaying[i] && game.musicType!=i){
					game.music[i].stop();
					game.musicPlaying[i]=false;
				}else if(!game.musicPlaying[i] && game.musicType==i){
					game.music[i].loop();
					game.musicPlaying[i]=true;
				}
			}
			try { Thread.sleep(50); } catch (InterruptedException e) {}
		}
	}

	private void initGame(){
		this.setSize(1280, 720);
		singleFrameTimer=36;
		
		addMouseListener(this);
		addMouseMotionListener(this);
		setFocusable(true);
		requestFocusInWindow();
		
		fieldDim=getSize();
		offscreen=createImage((int)fieldDim.getWidth(), (int)fieldDim.getHeight());
		bufferGraphics=offscreen.getGraphics();
		
		GameEnvironment.setFieldDim(fieldDim.width, fieldDim.height);
		game=new GameEnvironment();
		try {
			GameEnvironment.bossBackground=ImageIO.read(new URL(getCodeBase(), "bossBackground.jpg"));
			GameEnvironment.secretBackground=ImageIO.read(new URL(getCodeBase(), "secretBackground.jpg"));
		} catch (Exception e) {}

		game.music[0]=getAudioClip(getCodeBase(), "bossMusic.wav");
		game.music[1]=getAudioClip(getCodeBase(), "stageSelectionMusic.wav");
		game.music[2]=getAudioClip(getCodeBase(), "stageMusic.wav");
		
		startGameThreads();
	}
}
