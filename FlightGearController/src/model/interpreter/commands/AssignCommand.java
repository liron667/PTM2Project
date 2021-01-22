package model.interpreter.commands;

import model.interpreter.Parser;
import model.interpreter.experssions.ShuntingYardPostfix;

public class AssignCommand implements Command {
    @Override
    public void doCommand(String[] array) {
        if (array[2].equals("bind")) {
            if(Parser.symTable.get(array[0]).getV()!=Parser.symTable.get(array[3]).getV()) { 
            	Parser.symTable.get(array[0]).setV(Parser.symTable.get(array[3]).getV()); 
            }
            
            Parser.symTable.get(array[3]).addObserver(Parser.symTable.get(array[0]));
            Parser.symTable.get(array[0]).addObserver(Parser.symTable.get(array[3]));
        } else {
            StringBuilder exp = new StringBuilder();
            
            for (int i = 2; i < array.length; i++) { exp.append(array[i]); }
            
            double tmp = ShuntingYardPostfix.calc(exp.toString());
            
            if(Parser.symTable.get(array[0]).getLoc()!=null) {
                ConnectCommand.out.println("set " + Parser.symTable.get(array[0]).getLoc() + " " + tmp);
                ConnectCommand.out.flush();
                System.out.println("set " + Parser.symTable.get(array[0]).getLoc() + " " + tmp);
            } else { Parser.symTable.get(array[0]).setV(tmp); }
        }
    }
}