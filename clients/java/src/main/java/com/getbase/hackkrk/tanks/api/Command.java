package com.getbase.hackkrk.tanks.api;

public class Command {
    public double shotAngle;
    public double shotPower;
    public double moveDistance;
    
    private Command(){}
    
    /**
     * Request the bot to move by a specified (horizontal) distance.
     * 
     * @param horizontalDistance
     *            distance to move. Expected to be in the range [-15, 15].
     */
    public static Command move(double horizontalDistance){
        return new Command(0, 0, horizontalDistance);
    }
    
    /**
     * Request the bot to fire.
     * 
     * @param angle
     *            shot angle - the angle (degrees) between 'up' and firing vector. In the range [-180, 180]. 0 means shot upwards. Positive
     *            values - to the right. Negative - to the left.
     * @param power
     *            shot power in the range [1, 100]
     */
    public static Command fire(double angle, double power){
        return new Command(angle, power, 0);
    }
    
    private Command(double shotAngle, double shotPower, double moveDistance) {
        this.shotAngle = shotAngle;
        this.shotPower = shotPower;
        this.moveDistance = moveDistance;
    }
    @Override
    public String toString() {
        return "Command [shotAngle=" + shotAngle + ", shotPower=" + shotPower + ", moveDistance=" + moveDistance + "]";
    }

}
