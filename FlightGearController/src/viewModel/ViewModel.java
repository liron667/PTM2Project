package viewModel;

import javafx.beans.property.*;
import model.Model;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;

public class ViewModel extends Observable implements Observer {

	public DoubleProperty throttleXairplane,Yairplane;
    public DoubleProperty Ystart, Xstart;
    public DoubleProperty XmarkScene, YmarkScene;
    public DoubleProperty offset ,heading ,rudder, aileron, lift;
    public StringProperty script, ip ,port;
    public BooleanProperty path;
    private int data[][];
    private Model model;

    public ViewModel() {
        throttle = new SimpleDoubleProperty();
        rudder = new SimpleDoubleProperty();
        aileron = new SimpleDoubleProperty();
        lift = new SimpleDoubleProperty();
        Xairplane = new SimpleDoubleProperty();
        Yairplane = new SimpleDoubleProperty();
        Xstart = new SimpleDoubleProperty();
        Ystart = new SimpleDoubleProperty();
        offset = new SimpleDoubleProperty();
        heading = new SimpleDoubleProperty();
        XmarkScene = new SimpleDoubleProperty();
        YmarkScene = new SimpleDoubleProperty();
        script = new SimpleStringProperty();
        ip = new SimpleStringProperty();
        port = new SimpleStringProperty();
        path = new SimpleBooleanProperty();
    }

    public void setData(int[][] data) {
        this.data = data;
        model.GetPlane(Xstart.getValue(), Ystart.doubleValue(),offset.getValue());
    }
    
    public void setModel(Model model) { this.model=model; }

    public void setThrottle() {
        String[] data = { "set /controls/engines/current-engine/throttle "+throttle.getValue() };
        model.send(data);
    }

    public void setRudder() {
        String[] data = { "set /controls/flight/rudder "+rudder.getValue() };
        model.send(data);
    }

    public void setJoystick() {
        String[] data = {
                "set /controls/flight/aileron " + aileron.getValue(),
                "set /controls/flight/elevator " + lift.getValue(),
        };
        model.send(data);
    }
    
    // connect to the manual controller.
    public void connect() { model.connectManual(ip.getValue(),Integer.parseInt(port.getValue())); }

    // Parsers the received scrip
    public void parse() {
        Scanner scanner = new Scanner(script.getValue());
        ArrayList<String> list = new ArrayList<>();
        
        while(scanner.hasNextLine() == true)
        {
            list.add(scanner.nextLine());
        }
        String[] tmp = new String[list.size()];

        for(int i = 0; i < list.size(); i++)
        {
            tmp[i] = list.get(i);
        }
        model.parse(tmp);
        scanner.close();
    }

    public void execute() { model.execute(); }

    //  autopilot controller.
    public void stopAutoPilot(){ model.stopAutoPilot(); }

    // find the shortest-path from the plane to its destination.
    public void findPath(double h,double w) {
    	// Checks for if it's the first time, then needed to connect.
    	if (!path.getValue()) { model.connectPath(ip.getValue(), Integer.parseInt(port.getValue())); }
    	
        model.findPath((int)(Yairplane.getValue() / (-1)),
        			   (int)(Xairplane.getValue() + (15)), 
        			   Math.abs((int)(YmarkScene.getValue() / h)),
        			   Math.abs((int)(XmarkScene.getValue() / w)),
        			   data);
    }

    // Update.
    @Override
    public void update(Observable o, Object arg) {
        if(o == model)
        {
            String[] tmp = (String[])arg;
            if(tmp[0].equals("plane")) {
                double x = Double.parseDouble(tmp[1]);
                double y = Double.parseDouble(tmp[2]);
                x = (x - Xstart.getValue() + offset.getValue());
                x /= offset.getValue();
                y = (y - Ystart.getValue() + offset.getValue()) / offset.getValue();
                Xairplane.setValue(x);
                Yairplane.setValue(y);
                heading.setValue(Double.parseDouble(tmp[3]));
                setChanged();
                notifyObservers();
            }
            else if(tmp[0].equals("path"))
            {
                setChanged();
                notifyObservers(tmp);
            }
        }
    }
}