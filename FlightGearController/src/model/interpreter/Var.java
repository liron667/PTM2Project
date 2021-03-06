package model.interpreter;

import java.util.Observable;
import java.util.Observer;

public class Var extends Observable implements Observer {
	// Data members.
	double value;
	String var_name;
	String location;
	
	// CTOR.
	public Var() {}
	
	// CTOR.
	public Var(String loc) {
		super();
		location = loc;
	}
	
	// CTOR.
	public Var(double v) {
		this.value = v;
		this.location = null;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		Double d = new Double(0);
		if(arg.getClass() == (d.getClass())) {
			if(this.value!=(double)arg) {
				this.setV((double) arg);
				this.setChanged();
				this.notifyObservers(arg+"");
			}
		}
	}

	@Override
	public String toString() { return this.location; }


	// Set & Get value.
	public void setV(double value) {
		if(this.value != value) {
			this.value = value;
			setChanged();
			notifyObservers(value);
		}
	}
	
	public double getV() { return value; }
	
	// Set & Get Name.
	public void setName(String name) { this.var_name = name; }
	public String getName() { return var_name; }
	
	// Set & Get Loc.
	public void setLoc(String loc) { location = loc; }
	public String getLoc() { return location; }
}