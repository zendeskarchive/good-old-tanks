package com.getbase.hackkrk.tanks.server.simulation;

import com.getbase.hackkrk.tanks.server.model.scene.LandscapeFactory;
import com.getbase.hackkrk.tanks.server.model.scene.Physics;
import com.getbase.hackkrk.tanks.server.model.scene.PhysicsProvider;
import com.getbase.hackkrk.tanks.server.model.tournament.TournamentConfiguration;
import com.getbase.hackkrk.tanks.server.model.tournament.debugui.UI;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.concurrent.atomic.LongAdder;

@Slf4j
public class TestLandscapes {
    public static void main(String... args) throws InterruptedException {
        LandscapeFactory factory = new LandscapeFactory();
        
        Physics p = new PhysicsProvider().provide(TournamentConfiguration.createDefault());
        UI ui = new UI();
        EventQueue.invokeLater(() -> ui.setVisible(true));

        for (int i = 1; i <= 100; i++) {
            UI.SceneView sv = new UI.SceneView();
            sv.landscape = factory.create(true, i);
            sv.physics = p;
            ui.getView().setSceneView(sv);
            Thread.sleep(100);
        }
    }
}
