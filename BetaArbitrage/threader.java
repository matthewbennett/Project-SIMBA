/*
Project SIMBA, a crypto currency AI trading bot 
Copyright (C) 2014  Matthew Bennett 

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
*/

package BetaArbitrage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.*;
import java.util.Timer;

import BetaArbitrage.BTCE.BTCEException;
/**
The method that runs the thread setup in the implemented sequence class
*/
public class threader extends implementedSequences
{
  public static void main(String args[]) throws BTCEException
  {	  
	
	setTradingFunds(8);
  //trade api keys need to be enetered here as well as the account class for now
	account matts = new account("xxxxxxxx-xxxxxxxx-xxxxxxxx-xxxxxxxx-xxxxxxxx","xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
    aggregator matt = new aggregator(); // the instance that holds the exploit sequence threads 
    Timer mattsWatch = new Timer(); // time to run the aggregator instance 
    mattsWatch.schedule(matt,0,2000); // the schedualer for the timer task 
  }
}