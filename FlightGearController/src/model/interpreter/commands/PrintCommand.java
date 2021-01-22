package model.interpreter.commands;

import model.interpreter.Parser;

public class PrintCommand implements Command {
    @Override
    public void doCommand(String[] array) {
       for (int i=1;i<array.length;i++) {
           if(Parser.symTable.containsKey(array[i]))  { 
        	   	System.out.print(array[i]+Parser.symTable.get(array[i]).getV());
           } else { System.out.print(array[i]); }
       }
   
       System.out.println("");
    }
}