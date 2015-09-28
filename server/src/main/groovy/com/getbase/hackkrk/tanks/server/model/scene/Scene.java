package com.getbase.hackkrk.tanks.server.model.scene;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Scene {

    private Landscape landscape;

    @JsonView(View.Internal.class)
    private Physics physics;

    public Scene(Landscape landscape, Physics physics) {
        this.landscape = landscape;
        this.physics = physics;
    }
}
