package view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import viewModel.ViewModel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.net.URL;
import java.util.*;

public class FlightController implements Initializable, Observer {
	// Data members.
	@FXML
    private Canvas airplane;
    @FXML
    private Canvas xMark;
    @FXML
    private  TextArea TextArea;
    @FXML
    private TextField port, ip;
    @FXML
    private Button submit;
    @FXML
    private Slider throttle,rudder;
    @FXML
    private RadioButton automatic, manualy;
    @FXML
    private MapDisplayer map;
    @FXML
    private Circle border, Joystick;
    @FXML
    private TitledPane background;

    private Stage stage = new Stage();
    
    private static int Who;
    double orgSceneX; 
    double orgSceneY;
    double orgTranslateX;
    private ViewModel viewModel;
    double orgTranslateY;
    public DoubleProperty markXScene, markYScene, aileron, elevator, xAirplane,yAirplane, xStart,yStart,offset ,heading;
    public double lastX;
    public double lastY;
    public int mapData[][];
    private Image plane[];
    private Image mark;
    private BooleanProperty path;
    private String[] solution;

    public void LoadDate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files","csv"));
        fileChooser.setCurrentDirectory(new File("./"));
        int v = fileChooser.showOpenDialog(null);
        if (v == JFileChooser.APPROVE_OPTION) {
            BufferedReader bufferedReader = null;
            String line = "";
            String cvsSplitBy = ",";

            ArrayList<String[]> numbers = new ArrayList<>();
            try {
                bufferedReader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));
                String[] start = bufferedReader.readLine().split(cvsSplitBy);
                xStart.setValue(Double.parseDouble(start[0]));
                yStart.setValue(Double.parseDouble(start[1]));
                start = bufferedReader.readLine().split(cvsSplitBy);
                offset.setValue(Double.parseDouble(start[0]));
                
                while ((line = bufferedReader.readLine()) != null) {
                    numbers.add(line.split(cvsSplitBy));
                }
                
                mapData = new int[numbers.size()][];

                for (int i = 0; i < numbers.size(); i++) {
                    mapData[i] = new int[numbers.get(i).length];

                    for (int j = 0; j < numbers.get(i).length; j++) {
                        String tmp = numbers.get(i)[j];
                        mapData[i][j] = Integer.parseInt(tmp);
                    }
                }

                this.viewModel.setData(mapData);
                this.drawAirplane();
                map.setMapData(mapData);

            } catch (FileNotFoundException e) { e.printStackTrace(); }
              catch (IOException e) { e.printStackTrace(); }
        }
    }

    public void LoadText() {
        TextArea.clear();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("./"));
        int v = fileChooser.showOpenDialog(null);
        ArrayList<String> list = new ArrayList<>();
        
        if(v == JFileChooser.APPROVE_OPTION) {
            try {
                Scanner scanner = new Scanner(new BufferedReader(new FileReader(fileChooser.getSelectedFile())));
                
                while(scanner.hasNextLine() == true) {
                    TextArea.appendText(scanner.nextLine());
                    TextArea.appendText("\n");
                }

                viewModel.parse();
            } catch (FileNotFoundException e) { e.printStackTrace(); }
        }
    }

    public void Connect() {
        Parent root = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PopupMessage.fxml"));
            root = fxmlLoader.load();
            
            FlightController fc = fxmlLoader.getController();
            fc.viewModel = this.viewModel;
            
            stage.setTitle("Connection");
            stage.setScene(new Scene(root));
            
            if(!stage.isShowing()) {
                stage.show();
                Who = 0;
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void Calc() {
        Parent root = null;
        String popupMessage = "PopupMessage.fxml";
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(popupMessage));
            root = fxmlLoader.load();
            FlightController fc = fxmlLoader.getController();
            
            fc.viewModel = this.viewModel;
            fc.mapData = this.mapData;
            fc.xMark = this.xMark;
            fc.path = new SimpleBooleanProperty();
            fc.path.bindBidirectional(this.path);
            
            stage.setTitle("Connection");
            stage.setScene(new Scene(root));
            
            if(!stage.isShowing()) {
                Who = 1;
                stage.show();
            }
            
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void Submit() {
        this.viewModel.ip.bindBidirectional(ip.textProperty());
        this.viewModel.port.bindBidirectional(port.textProperty());
        
        if(Who == 0) {
            viewModel.connect();
            Stage stage = (Stage) submit.getScene().getWindow();
            stage.close();
        }
        
        if(Who == 1) {
            double H = xMark.getHeight();
            double W = xMark.getWidth();
            double h = H / mapData.length;
            double w = W / mapData[0].length;
        
            viewModel.findPath(h,w);
            
            path.setValue(true);
            
            Stage stage = (Stage)submit.getScene().getWindow();
            stage.close();
        }
        
        ip.clear();
        port.clear();
    }

    public void AutoPilot(){ Select("auto"); }

    public void Manual() { Select("manual"); }

    public void Select(String s) {
        if(s.equals("auto")) {
            if(manualy.isSelected()) {
                manualy.setSelected(false);
                automatic.setSelected(true);

            }
            
            viewModel.execute();
        } else if(s.equals("manual")) {
            if(automatic.isSelected()) {
                automatic.setSelected(false);
                manualy.setSelected(true);
                viewModel.stopAutoPilot();
            }
        }
    }

    public void drawAirplane() {
        if((xAirplane.getValue() != null) && (yAirplane.getValue() != null)) {
            double H = airplane.getHeight();
            double W = airplane.getWidth();
            double h = H / mapData.length;
            double w = W / mapData[0].length;
            
            GraphicsContext gc = airplane.getGraphicsContext2D();
            lastX = xAirplane.getValue();
            lastY = yAirplane.getValue()*-1;
            
            gc.clearRect(0,0,W,H);

            if((heading.getValue() >= 0) && (heading.getValue() < 39)) { gc.drawImage(plane[0], (w * lastX), (lastY * h), 25, 25); }
            if((heading.getValue() >=39) && (heading.getValue() < 80)) { gc.drawImage(plane[1], (w * lastX), (lastY * h), 25, 25); }
            if((heading.getValue() >= 80) && (heading.getValue() < 129)) { gc.drawImage(plane[2], (w * lastX), (lastY * h), 25, 25); }
            if((heading.getValue() >= 129) && (heading.getValue() < 170)) { gc.drawImage(plane[3], (w * lastX), (lastY * h), 25, 25); }
            if((heading.getValue() >= 170) && (heading.getValue() < 219)) { gc.drawImage(plane[4], (w * lastX), (lastY * h), 25, 25); }
            if((heading.getValue() >= 219) && heading.getValue() < 260) { gc.drawImage(plane[5], (w * lastX), (lastY * h), 25, 25); }               
            if(heading.getValue()>=260&&heading.getValue()<309) { gc.drawImage(plane[6], (w * lastX), (lastY * h), 25, 25); }
            if(heading.getValue() >= 309) { gc.drawImage(plane[7], (w * lastX), (lastY * h), 25, 25); }
        }
    }

    public void drawMark() {
        double H = xMark.getHeight();
        double W = xMark.getWidth();
        double h = H / mapData.length;
        double w = W / mapData[0].length;
        
        GraphicsContext gc = xMark.getGraphicsContext2D();
        
        gc.clearRect(0, 0, W, H);
        gc.drawImage(mark, (markXScene.getValue() - 13), markYScene.getValue() , 25, 25);
       
        if(path.getValue()) { viewModel.findPath(h,w); }
    }

    public void drawLine() {
        double height = xMark.getHeight();
        double width = xMark.getWidth();
        double mapHeight = height / mapData.length;
        double mapWidth = width / mapData[0].length;
        
        GraphicsContext gc=xMark.getGraphicsContext2D();
        
        String move=solution[1];
        
        double x = xAirplane.getValue() * mapWidth + (10 * mapWidth);
        double y =yAirplane.getValue() * -mapHeight + 6*mapHeight;
 
        for(int i = 2; i < solution.length; i++) {
            switch (move) {
                case "Right":
                    gc.setStroke(Color.BLACK);
                    gc.strokeLine(x, y, x + mapWidth, y);
                    x +=  mapWidth;
                    break;
                case "Left":
                    gc.setStroke(Color.BLACK);
                    gc.strokeLine(x, y, x -  mapWidth, y);
                    x -=  mapWidth;
                    break;
                case "Up":
                    gc.setStroke(Color.BLACK);
                    gc.strokeLine(x, y, x, y - mapHeight);
                    y -=  mapHeight;
                    break;
                case "Down":
                    gc.setStroke(Color.BLACK);
                    gc.strokeLine(x, y, x, y +  mapHeight);
                    y += mapHeight;
            }
            
            move = solution[i];
        }
    }

    EventHandler<MouseEvent> MarkOnMousePressedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            markXScene.setValue(e.getX());
            markYScene.setValue(e.getY());
            drawMark();
        }
    };

    EventHandler<MouseEvent> circleOnMousePressedEventHandler = new EventHandler<MouseEvent>() {
    	@Override
        public void handle(MouseEvent t) {
        	orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();
            orgTranslateX = ((Circle)(t.getSource())).getTranslateX();
            orgTranslateY = ((Circle)(t.getSource())).getTranslateY();
    	}
    };

    EventHandler<MouseEvent> circleOnMouseDraggedEventHandler = new EventHandler<MouseEvent>() {
		@Override
		public void handle(MouseEvent t) {
        	double offsetX = t.getSceneX() - orgSceneX;
            double offsetY = t.getSceneY() - orgSceneY;
            double newTranslateX = orgTranslateX + offsetX;
            double newTranslateY = orgTranslateY + offsetY;
            
            if(isInCircle(newTranslateX,newTranslateY)) {
            	((Circle) (t.getSource())).setTranslateX(newTranslateX);
                ((Circle) (t.getSource())).setTranslateY(newTranslateY);
                
                if(manualy.isSelected()) {
                	aileron.setValue(nirmulX(newTranslateX));
                	elevator.setValue(nirmulY(newTranslateY));
                	viewModel.setJoystick();
                }
            }
		}
	};
 
    EventHandler<MouseEvent> circleOnMouseReleasedEventHandler = new EventHandler<MouseEvent>() {
    	@Override
        public void handle(MouseEvent t) {
    		((Circle)(t.getSource())).setTranslateX(orgTranslateX);
            ((Circle)(t.getSource())).setTranslateY(orgTranslateY);
    	}
	};	
	
   private double nirmulX(double num) {
        double max = (border.getRadius() - Joystick.getRadius()) + border.getCenterX();
        double min = border.getCenterX() - (border.getRadius() - Joystick.getRadius());
        double new_max = 1;
        double new_min = -1;
        return (((num - min) / (max - min) * (new_max - new_min) + new_min));
    }
   
    private double nirmulY(double num) {
        double min = (border.getRadius() - Joystick.getRadius()) + border.getCenterY();
        double max = border.getCenterY() - (border.getRadius() - Joystick.getRadius());
        double new_max = 1;
        double new_min =-1;
        return (((num - min) / (max - min) * (new_max - new_min) + new_min));
    }

    private  boolean isInCircle(double x,double y){
        return ((Math.pow((x - border.getCenterX()), 2) + Math.pow(( y - border.getCenterY()), 2))) <= (Math.pow(border.getRadius() - Joystick.getRadius(), 2));
    }



    public void setViewModel(ViewModel viewModel) {
        this.viewModel = viewModel;
        throttle.valueProperty().bindBidirectional(viewModel.throttle);
        rudder.valueProperty().bindBidirectional(viewModel.rudder);
        aileron = new SimpleDoubleProperty();
        elevator = new SimpleDoubleProperty();
        aileron.bindBidirectional(viewModel.aileron);
        elevator.bindBidirectional(viewModel.elevator);
        xAirplane = new SimpleDoubleProperty();
        yAirplane = new SimpleDoubleProperty();
        xStart = new SimpleDoubleProperty();
        yStart = new SimpleDoubleProperty();
        xAirplane.bindBidirectional(viewModel.airplaneX);
        yAirplane.bindBidirectional(viewModel.airplaneY);
        xStart.bindBidirectional(viewModel.startX);
        yStart.bindBidirectional(viewModel.startY);
        offset = new SimpleDoubleProperty();
        offset.bindBidirectional(viewModel.offset);
        viewModel.script.bindBidirectional(TextArea.textProperty());
        heading = new SimpleDoubleProperty();
        heading.bindBidirectional(viewModel.heading);
        markXScene = new SimpleDoubleProperty();
        markYScene = new SimpleDoubleProperty();
        markYScene.bindBidirectional(viewModel.markSceneY);
        markXScene.bindBidirectional(viewModel.markSceneX);
        path = new SimpleBooleanProperty();
        path.bindBidirectional(viewModel.path);
        path.setValue(false);
        plane = new Image[8];
        
        try {
            plane[0]=new Image(new FileInputStream("./resources/plane0.png"));
            plane[1]=new Image(new FileInputStream("./resources/plane45.png"));
            plane[2]=new Image(new FileInputStream("./resources/plane90.png"));
            plane[3]=new Image(new FileInputStream("./resources/plane135.png"));
            plane[4]=new Image(new FileInputStream("./resources/plane180.png"));
            plane[5]=new Image(new FileInputStream("./resources/plane225.png"));
            plane[6]=new Image(new FileInputStream("./resources/plane270.png"));
            plane[7]=new Image(new FileInputStream("./resources/plane315.png"));
            mark=new Image(new FileInputStream("./resources/mark.png"));
        } catch (FileNotFoundException e) { e.printStackTrace(); }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(location.getPath().contains("Flight.fxml")) {
            throttle.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(manualy.isSelected()) { viewModel.setThrottle(); }
            });

            rudder.valueProperty().addListener((observable, oldValue, newValue) -> {
                if(manualy.isSelected()) { viewModel.setRudder(); }
            });
            
            Joystick.setOnMousePressed(circleOnMousePressedEventHandler);
            Joystick.setOnMouseDragged(circleOnMouseDraggedEventHandler);
            Joystick.setOnMouseReleased(circleOnMouseReleasedEventHandler);
            
            xMark.setOnMouseClicked(MarkOnMousePressedEventHandler);
            
            Who =- 1;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o == viewModel) {
            if(arg == null) { 
            	drawAirplane();
            } else {
                solution=(String[])arg;
                this.drawLine();
            }
        }
    }
}