package com.getbase.hackkrk.tanks.server.model.tournament.debugui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.getbase.hackkrk.tanks.server.model.scene.Landscape;
import com.getbase.hackkrk.tanks.server.model.scene.Physics;
import com.getbase.hackkrk.tanks.server.model.tournament.Player;
import com.getbase.hackkrk.tanks.server.simulation.events.BulletPositionChange;
import com.getbase.hackkrk.tanks.server.simulation.events.TankHealthChange;
import com.getbase.hackkrk.tanks.server.simulation.events.TankPositionChange;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import mikera.vectorz.Vector2;

@SuppressWarnings("serial")
public class UI extends JFrame {
	private View view;

	public UI() {
		this.view = new View();
		add(view);
		setTitle("UI");
		setSize(1400, 650);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static class SceneView{
	    public Physics physics;
	    public Landscape landscape;
	    public Map<Player, List<Vector2>> trajectories = Collections.synchronizedMap(new LinkedHashMap<>());
	    public Map<Player, Vector2> tankPositions = Collections.synchronizedMap(new LinkedHashMap<>());
	    public Map<Player, Double> tankHealth  = Collections.synchronizedMap(new LinkedHashMap<>());
	    
        public void onBulletPositionChange(BulletPositionChange e) {
            List<Vector2> trajectory = trajectories.getOrDefault(e.getOwner(), new ArrayList<>());
            trajectory.add(e.getNewPosition().toVector());
            trajectories.putIfAbsent(e.getOwner(), trajectory);
        }

        public void onTankPositionChange(TankPositionChange e) {
            Vector2 tankPos = tankPositions.getOrDefault(e.getOwner(), Vector2.of(0,0));
            tankPos.set(e.getNewPosition().toVector());
            tankPositions.putIfAbsent(e.getOwner(), tankPos);
        }
        
        public void onTankHealthChange(TankHealthChange e){
            tankHealth.put(e.getOwner(),e.getNewHealth());
        }
	}

	public static class View extends JPanel {
	    private volatile SceneView sceneView = new SceneView();

		public View() {
			Timer timer = new Timer(50, (e) -> repaint());
			timer.start();
		}

		public void setSceneView(SceneView sceneView) {
			this.sceneView = sceneView;
		}
		public SceneView getSceneView(){
		    return this.sceneView;
		}

		private void doDrawing(Graphics g) {
		    Graphics2D g2d = (Graphics2D) g;
		    drawTrajectories(g2d);
		    drawLandscape(g2d);
		    drawTanks(g2d);
		}

        private void drawTanks(Graphics2D g2d) {
            List<Color> colors = new LinkedList<>();
            colors.add(Color.blue);
            colors.add(Color.red);
            int colorId = 0;
            for(Player player : new HashSet<>(sceneView.tankPositions.keySet())){
                Vector2 p = sceneView.tankPositions.get(player);
                if(p == null)
                    continue;
                g2d.setColor(colors.get(colorId));
                colorId = (colorId + 1) % colors.size();
                
                GeometryFactory geom = new GeometryFactory();
                GeometricShapeFactory shapes = new GeometricShapeFactory(geom);
                shapes.setNumPoints(16);
                
                shapes.setCentre(new Coordinate(p.x, p.y));
                shapes.setSize(sceneView.physics.getTankDiameter());
                Polygon hitbox = shapes.createCircle();
                
                Coordinate[] coords = hitbox.getCoordinates();
                for(int cid = 1; cid < coords.length; cid++){
                    drawLine(g2d, coords[cid-1].x, coords[cid-1].y, coords[cid].x, coords[cid].y);
                }
                
                Double hp = sceneView.tankHealth.get(player);
                drawString(g2d, p.x, p.y, player, hp != null ? hp : -1);
            }
            
        }

        private void drawTrajectories(Graphics2D g2d) {
            List<Color> colors = new LinkedList<>();
            colors.add(Color.blue);
            colors.add(Color.red);
            int colorId = 0;
            for(List<Vector2> toDraw : new LinkedList<>(sceneView.trajectories.values())){
                g2d.setColor(colors.get(colorId));
                colorId = (colorId + 1) % colors.size();
                
    			if(toDraw != null){
    				for (int i = 1; i < toDraw.size(); i++) {
    				    
    				    // nasty hack to avoid ugly lines in between turns
    				    if((toDraw.get(i - 1).x != 0 || toDraw.get(i - 1).y != 0) 
    				            && (toDraw.get(i).x != 0 ||  toDraw.get(i).y != 0))
    				        if(Math.abs(toDraw.get(i - 1).x - toDraw.get(i).x) < 500){ // to handle cylindric map
    				            drawLine(g2d, toDraw.get(i - 1).x, toDraw.get(i - 1).y, toDraw.get(i).x, toDraw.get(i).y);
    				        }
    				}
    			}
		    }
        }
        
        private void drawLandscape(Graphics2D g2d) {
            if(sceneView.landscape == null){
                return;
            }
            
            List<Point> points = sceneView.landscape.getPoints();
            g2d.setPaint(Color.black);
            Random r = new Random(123);
            for (int i = 1; i < points.size(); i++) {
                g2d.setPaint(new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255)));
                drawLine(g2d, points.get(i - 1).getX(), points.get(i - 1).getY(), points.get(i).getX(),  points.get(i).getY());
            }
        }
        
        private void drawLine(Graphics2D g, double x1, double y1, double x2, double y2){
            int w = getWidth();
            int h = getHeight();
            
            int _x1 = (int) (w / 2 + x1);
            int _y1 = (int) (h / 2 - y1)+300;
            int _x2 = (int) (w / 2 + x2);
            int _y2 = (int) (h / 2 - y2)+300;

            g.drawLine(_x1, _y1, _x2, _y2);
        }
        
        private void drawString(Graphics2D g2d, double x, double y, Player player, double hp) {
            int w = getWidth();
            int h = getHeight();
            
            int _x1 = (int) (w / 2 + x);
            int _y1 = (int) (h / 2 - y)+300;
            
            
            String msg = player.getName() + " [" + (int)hp + "]";
            
            g2d.drawString(msg, _x1, _y1 - 30);
            
        }
        
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			doDrawing(g);
		}
	}

	public View getView() {
		return view;
	}
	
	
}
