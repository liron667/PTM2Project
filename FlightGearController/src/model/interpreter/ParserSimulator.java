package model.interpreter;

import java.util.ArrayList;
import java.util.HashMap;

import model.interpreter.commands.*;


public class ParserSimulator implements IParser{
	/// Data members.
	private HashMap<String, Command> cmdTable=new HashMap<>();
	public static HashMap<String,Var> symTable=new HashMap<>();
	private ArrayList<String[]> linesList;
	
	// CTOR.
	public ParserSimulator(ArrayList<String[]> lines) {
		String openDataServer = "openDataServer";
		String connection = "connect";
		String whileSym = "while";
		this.linesList = lines;
		cmdTable.put(openDataServer, new OpenDataServer());
		cmdTable.put(connection, new ConnectCommand());
		cmdTable.put(whileSym, new LoopCommand());
	}
	
	public void parse() {
		String closeSym = "}";
		for (int i = 0; i < linesList.size(); i++) {
			Command cmd = cmdTable.get(linesList.get(i)[0]);
			
			if(cmd != null) {
				if(linesList.get(i)[0].equals("while")) {
					int index = i;
					
					while(!linesList.get(i)[0].equals(closeSym )) { 
						i++; 
					}
					
					this.parseCondition(new ArrayList<String[]>(linesList.subList(index, i)));
				}
			}
		
			cmd.doCommand(linesList.get(i));
		}
	}
	
	private void parseCondition(ArrayList<String[]> array) {
		String closeSym = "}";
		ArrayList<Command> comds = new ArrayList<>();
		int i = 0;
		String[] tmp = array.get(i);
		ConditionCommand c = (ConditionCommand) cmdTable.get(tmp[0]);

		i++;
		for (; i < array.size(); i++) {
			Command com = cmdTable.get(array.get(i)[0]);
			if(com != null) {
				if(array.get(i)[0].equals("while")) {
					int index= i; 
					
					while(array.get(i)[0]!=closeSym) {
						i++; 
					}
					
					this.parseCondition(new ArrayList<String[]>(array.subList(index, i)));
				}
			}
			
			comds.add(com);
		}
	}
}