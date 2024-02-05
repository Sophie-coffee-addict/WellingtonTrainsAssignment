import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import ecs100.UI;

public class Main {

	private Map<Integer, Double> faresMap = new HashMap<Integer, Double>();
	private Map<String, TrainLine> trainlinesMap = new HashMap<String, TrainLine>();
	private Map<String, Station> stationsMap = new HashMap<String, Station>();
	private List<String> allstationNames = new ArrayList<String>();// list of all station names
	private String start;
	private String end;
	private int time;
	private int fareZone;
	private int inputNumber;

	public void loadFiles() {
		this.readFares();
		this.readStations();
		this.readTrainLines();

	}

	public void readFares() {
		try {
			Scanner scan = new Scanner(new File("Train network data/fares.data"));
			while (scan.hasNext()) {
				scan.nextLine();
				int zone = scan.nextInt();
				double fare = scan.nextDouble();
				faresMap.put(zone, fare);
			}
		} catch (FileNotFoundException e) {
			UI.printf("Error loading file: %s%n", e);
		}
	}

	public void readStations() {
		try {
			Scanner scan = new Scanner(new File("Train network data/stations.data"));
			while (scan.hasNext()) {
				String stationName = scan.next();
				allstationNames.add(stationName);
				int fareZone = scan.nextInt();
				double distance = scan.nextDouble();
				Station station = new Station(stationName, fareZone, distance);
				stationsMap.put(stationName, station);
			}
		} catch (FileNotFoundException e) {
			UI.printf("Error loading file: %s%n", e);
		}
	}

	public void readTrainLines() {
		try {
			Scanner scan = new Scanner(new File("Train network data/train-lines.data"));
			while (scan.hasNext()) {
				String trainLineName = scan.next();
				TrainLine trainLine = new TrainLine(trainLineName);
				trainlinesMap.put(trainLineName, trainLine);

				Scanner sc1 = new Scanner(new File("Train network data/" + trainLineName + "-stations.data"));
				while (sc1.hasNext()) {
					String stationName = sc1.next();
					trainLine.addstationNames(stationName);
					Station s = stationsMap.get(stationName);
					s.addTrainLine(trainLine);
					trainLine.addStation(s);
				}
				sc1.close();

				Scanner sc2 = new Scanner(new File("Train network data/" + trainLineName + "-services.data"));
				while (sc2.hasNext()) {
					TrainService trainService = new TrainService(trainLine);
					boolean firstStop = true;
					for (int i = 0; i < trainLine.getStations().size(); i++) {
						int time = sc2.nextInt();
						trainService.addTime(time, firstStop);
						firstStop = false;
					}
					trainLine.addTrainService(trainService);
				}
				sc2.close();
			}
			scan.close();
		} catch (FileNotFoundException e) {
			UI.printf("Error loading file: %s%n", e);
		}
	}

	public void listStations() {
		UI.clearText();
		UI.setDivider(1.0);
		UI.println("Totan number of stations: " + allstationNames.size());
		UI.println("====================================");
		for (String s : allstationNames) {
			UI.println(s);
		}
	}

	public void showStationsInfo() {
		UI.clearText();
		UI.setDivider(1.0);
		for (String s : stationsMap.keySet()) {
			UI.println(stationsMap.get(s).toString());
		}
	}

	public void searchTrainLinesByStationName() {
		UI.clearText();
		UI.setDivider(1.0);
		String inputName = UI.askString("Station name: ");
		boolean notExist = true;
		for (String stationName : stationsMap.keySet()) {
			if (stationName.equalsIgnoreCase(inputName)) {
				stationsMap.get(stationName).printTrainLineInfo();
				notExist = false;
				break;
			}
		}
		if (notExist) {
			UI.println("The station you're looking for does not exist, try a different name.");
			UI.sleep(2800);
			this.searchTrainLinesByStationName();
		}
	}
	
	public void showTrainLineDetails() {
        UI.clearText();
        UI.setDivider(1.0);
        UI.println("Total number of trainlines: " + trainlinesMap.size());
        UI.println("===================================================");

        int lineNumber = 1;
        for (TrainLine trainLine : trainlinesMap.values()) {
            UI.println(lineNumber + ". " + trainLine);
            lineNumber++;
        }

        inputNumber = UI.askInt("Enter the number of the train line to view details:");

        if (inputNumber >= 1 && inputNumber <= trainlinesMap.size()) {
            TrainLine selectedTrainLine = getTrainLineByNumber(inputNumber);
            showLineDetails(selectedTrainLine);
        } else {
            UI.println("Invalid input. Please enter a valid number.");
        }
    }

    private TrainLine getTrainLineByNumber(int number) {
        int index = 1;
        for (TrainLine trainLine : trainlinesMap.values()) {
            if (index == number) {
                return trainLine;
            }
            index++;
        }
        return null;
    }

    // the second page of buttons, show each the service
    public void showLineDetails(TrainLine trainLine) {
        UI.clearText();
        UI.setDivider(1.0);
        int stationNum = trainLine.getStations().size();
        UI.println(stationNum + " stations along this line:");
        for (int i = 0; i < stationNum; i++) {
            UI.println(trainLine.getStations().get(i).getName());
        }
        UI.println();

        int serviceNum = trainLine.getTrainServices().size();
        UI.println(serviceNum + " services along this line:");
        for (int i = 0; i < serviceNum; i++) {
            trainLine.getTrainServices().get(i).printTimes();
            UI.println();
        }
    }

	// check if the user entered the right station names
	public String checkStationNameValidity(String stationName) {
		UI.clearText();
		UI.setDivider(1.0);
		boolean flag = false;
		while (flag == false) {
			for (int i = 0; i < allstationNames.size(); i++) {
				if (allstationNames.get(i).equalsIgnoreCase(stationName)) {
					flag = true;
					return stationName;
				}
			}
			flag = false;
			UI.println("Invalid station name. \nPlease enter a valid station name: ");
			stationName = UI.askString("Station name:");
		}
		return stationName;
	}

	public void planJourney() {
		UI.clearText();
		UI.setDivider(1.0);
		start = UI.askString("Start station:");
		start = this.checkStationNameValidity(start);
		end = UI.askString("Destination station:");
		end = this.checkStationNameValidity(end);
		time = UI.askInt("Specify the time for your journey, in military time format: ");

		// rule out if the user put a same station name, not to return all train lines
		if (start.equalsIgnoreCase(end)) {
			UI.println("You are already where you want to go! \nChoose different stops. \nWait for the input page. ");
			UI.sleep(2800);
			this.planJourney();
			return;
		}
		if (!this.sameTrainLine(start, end)) {
			UI.println(
					"The stations you queried are not on the same train line. \nThe best option is to go back to the Wellington station.");
		}
	}

	public void calculateFare(String startStationName, String endStationName) {
		startStationName = startStationName.substring(0, 1).toUpperCase() + startStationName.substring(1);
		endStationName = endStationName.substring(0, 1).toUpperCase() + endStationName.substring(1);
		int startZone = stationsMap.get(startStationName).getZone();
		int endZone = stationsMap.get(endStationName).getZone();
		fareZone = Math.abs((endZone - startZone)) + 1;
		double fare = faresMap.get(fareZone);
		UI.println("Train fare: " + fare + " NZD");
	}

	public boolean sameTrainLine(String start, String end) {
		for (TrainLine trainLine : trainlinesMap.values()) {
			int startIndex = trainLine.indexOfStation(start);
			int endIndex = trainLine.indexOfStation(end);

			if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
				calculateAndPrintInfo(trainLine, startIndex, endIndex);
				return true;
			}
		}
		return false;
	}

	private void calculateAndPrintInfo(TrainLine trainLine, int startIndex, int endIndex) {
		UI.println("========================================");
		UI.println("Train line " + trainLine.getName() + ":");
		calculateFare(trainLine.getStations().get(startIndex).getName(),
				trainLine.getStations().get(endIndex).getName());
		UI.print(trainLine.toString());

		int serviceSize = trainLine.getTrainServices().size();
		for (int h = 0; h < serviceSize; h++) {
			List<Integer> times = trainLine.getTrainServices().get(h).getTimes();
			int startTime = times.get(startIndex);
			int endTime = times.get(endIndex);

			if (startTime != -1 && startTime >= time && startTime <= time + 15) {
				UI.println("Next available services for the specified time:");
				UI.println(trainLine.getName());
				UI.println(times);

				String startStation = trainLine.getStations().get(startIndex).getName();
				String endStation = trainLine.getStations().get(endIndex).getName();
				printJourneyDetails(trainLine.getName(), startStation, endStation, startTime, endTime, startIndex + 1);
				break;
			}
		}
	}

	private void printJourneyDetails(String lineName, String start, String end, int startTime, int endTime,
			int stationNum) {
		UI.println("The train will leave at " + startTime + " at " + start + " and arrive at " + endTime + " at " + end
				+ ".");
		UI.println(start + " is at the " + stationNum + " station.");
		int timeSpent = endTime - startTime;
		UI.println("You will be travelling " + fareZone + " zones.");
		UI.println("This trip will take " + timeSpent + " min.");
	}

	boolean buttonSet1 = false;

	public void swapButtons() {
		UI.clearText();
		UI.initialise();
		UI.setDivider(1.0);
		buttonSet1 = !buttonSet1;
		if (buttonSet1) {
			UI.addButton("Show all trainlines", this::swapButtons);
			UI.addButton("All stations", this::listStations);
			UI.addButton("Station info", this::showStationsInfo);
			UI.addButton("Search trainlines by a station", this::searchTrainLinesByStationName);
			UI.addButton("Plan my journey", this::planJourney);
			UI.addButton("Show map", this::showMap);
			UI.addButton("Clear", UI::clearText);
			UI.addButton("Quit", UI::quit);
		} else {
			UI.clearText();
			UI.addButton("Back to main menu", this::swapButtons);
			this.showTrainLineDetails();
			UI.addButton("Show all trainlines", this::showTrainLineDetails);

		}
	}

	public void showMap() {
		UI.clearText();
		UI.setDivider(0.0);
		UI.drawImage("Train network data/system-map.png", 0, 0, 500, 700);

	}

	public Main() {
		UI.initialise();
		UI.addButton("List all stations", this::listStations);
		UI.addButton("Station into", this::showStationsInfo);
		UI.addButton("All trainlines", this::showTrainLineDetails);
		UI.addButton("Search trainlines by a station", this::searchTrainLinesByStationName);
		UI.addButton("Clear", UI::clearText);
		swapButtons();
		this.loadFiles();
	}

	public static void main(String[] args) {
		new Main();

	}
}
