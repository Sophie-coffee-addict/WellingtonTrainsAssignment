
import java.util.*;

import ecs100.UI;

import java.io.*;

/**
 * Station
 * Information about an individual station:
 * - The name
 * - The fare zone it is in (1 - 14)
 * - The distance from the hub station (Wellington)
 * - The set of TrainLines that go through that station.
 * The constructor just takes the name, zone and distance;
 * TrainLines must then be added to the station, one by one.
 */

public class Station{
	

    private String name;  
    private int zone;          // fare zone
    private double distance;   // distance from Wellington
    private List<TrainLine> trainLines = new ArrayList<TrainLine>();  //the train lines passing a station

    public Station(String name, int zone, double dist){
        this.name = name;
        this.zone = zone;
        this.distance = dist;
    }

    public String getName(){
        return this.name;
    }

    public int getZone(){
        return this.zone;
        
    }
   //return the distance from this station to the hub, Wellington
    public double getDistance() {
    	return this.distance;
    }

    /**
     * Add a TrainLine to the station
     */
    public void addTrainLine(TrainLine line){
        trainLines.add(line);
    }

    public List<TrainLine> getTrainLines(){
        return Collections.unmodifiableList(trainLines); //Return an unmodifiable version of the set of train lines.
    }

    /**
     * toString is the station name plus zone, plus number of train lines
     */
    public String toString(){
        return name+" (zone "+zone+", "+trainLines.size()+" lines)";
    }

    public void printName() {
    	UI.print(name+"\t");
    }
    
    public void printTrainLineInfo() {
    	for (int i = 0; i < trainLines.size();i++) {
    		UI.print(trainLines.get(i));
    		
    	}
    }
}
