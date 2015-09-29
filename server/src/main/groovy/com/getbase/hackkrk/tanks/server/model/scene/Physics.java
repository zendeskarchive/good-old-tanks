package com.getbase.hackkrk.tanks.server.model.scene;

import com.getbase.hackkrk.tanks.server.model.scene.topology.BoundedMapTopology;
import com.getbase.hackkrk.tanks.server.model.scene.topology.MapTopology;

import lombok.Data;

@Data
public class Physics {
    private double wind = -15;
    private double gravity = -9.81;
    private double maxShotPower = 100;
    private double airDragCoefficient = 0.001;

    private int initialTankHealth = 100;
    private double bulletDamage = 50;

    private MapTopology mapTopology = new BoundedMapTopology();

    /**
     * Tank radius used for collision checks
     */
    private double tankDiameter = 30;


    /**
     * How high over the ground (over the tank position) is the turret base - used to calculate initial bullet position.
     */
    private double tankTurretHeight = 10;


    /**
     * Length of the turret - used to calculate initial bullet position.
     */
    private double turretLength = 15;


    /**
     * Maximum distance a tank can move during a single turn
     */
    private double maxMovePerTurn = 30;

    private double maxMoveSpeed = 2;

    /**
     * Do not change, alters precision of the simulation
     */
    private double unitOfTime = 0.3;

    /**
     * Limits turn length in case there's an infinite loop (like a bullet hovering in mid-air)
     */
    private double maxSimulationIterationsPerTurn = 1000;

    private double powerMultiplier = 1.5;
}
