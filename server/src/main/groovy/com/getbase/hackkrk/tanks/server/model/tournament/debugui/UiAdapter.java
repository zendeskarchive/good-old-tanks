package com.getbase.hackkrk.tanks.server.model.tournament.debugui;

import java.awt.EventQueue;

import com.getbase.hackkrk.tanks.server.model.scene.LandscapeFactory;
import com.getbase.hackkrk.tanks.server.model.scene.Physics;
import com.getbase.hackkrk.tanks.server.model.scene.Scene;
import com.getbase.hackkrk.tanks.server.model.tournament.debugui.UI.SceneView;
import com.getbase.hackkrk.tanks.server.model.tournament.debugui.UI.View;
import com.getbase.hackkrk.tanks.server.simulation.Simulation;
import com.getbase.hackkrk.tanks.server.simulation.events.BulletPositionChange;
import com.getbase.hackkrk.tanks.server.simulation.events.SimulationEvent;
import com.getbase.hackkrk.tanks.server.simulation.events.TankHealthChange;
import com.getbase.hackkrk.tanks.server.simulation.events.TankPositionChange;

public class UiAdapter {
    private UI ui;

    private static final UiAdapter instance = new UiAdapter();

    private UiAdapter() {
        ui = setupUi();
    }

    public static UiAdapter get() {
        return instance;
    }

    private static UI setupUi() {
        System.setProperty("java.awt.headless", "false");
        UI ui = new UI();
        EventQueue.invokeLater(() -> ui.setVisible(true));

        SceneView sv = new SceneView();
        sv.landscape = new LandscapeFactory().create(true);
        sv.physics = new Physics();

        ui.getView().setSceneView(sv);
        return ui;
    }

    public void onSimulationRun(Scene scene, Simulation sim) {
        ui.getView().getSceneView().landscape = scene.getLandscape();
        ui.getView().getSceneView().physics = scene.getPhysics();
        ui.getView().getSceneView().tankPositions.clear();
        ui.getView().getSceneView().trajectories.clear();
        ui.getView().getSceneView().tankHealth.clear();

        sim.addEventConsumer(x -> onEvent(ui.getView(), x));
    }

    private static void onEvent(View view, SimulationEvent e) {
        SceneView sv = view.getSceneView();

        if (e instanceof BulletPositionChange) {
            sv.onBulletPositionChange((BulletPositionChange) e);
        }
        if (e instanceof TankPositionChange) {
            sv.onTankPositionChange((TankPositionChange) e);
        }
        if (e instanceof TankHealthChange) {
            sv.onTankHealthChange((TankHealthChange) e);
        }

//        try {
//            Thread.sleep(10);
//        } catch (InterruptedException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
        // if(previousTimestamp != e.getTimestamp()){
        // Thread.sleep(30);
        // }
        // previousTimestamp = e.getTimestamp();
    }
}
