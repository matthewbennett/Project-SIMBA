/*
Project SIMBA, a crypto currency AI trading bot 
Copyright (C) 2014  Matthew Bennett 

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
*/

package BetaArbitrage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TimerTask;
import BetaArbitrage.BTCE.BTCEException;

/**
Adds instances of the sequence class, each new sequence must extend the Thread class, then a new instance of the thread should be added to the aggretor 
*/

public class implementedSequences extends sequence
{

//litcoin exploit sequence 
  static class litecoinSequence extends Thread
  {
    public void run()
    {
      try 
      {
    	  fileName = "litecoinOutput.txt";
    	  //output = new FileWriter(fileName);
    	  //writer = new BufferedWriter(output);
    tradeSequence(LTC_USD, LTC_BTC,"litecoin", "ltc/btc");
      }
      catch (NullPointerException | BTCEException | InterruptedException | IOException e) 
      {
         e.printStackTrace();
      }
    }
  }
  //namecoin exploit sequence
  static class namecoinSequence extends Thread
  {
    public void run()
    {
    	
      try 
      {
    	  fileName = "litecoinOutput.txt";
    	  //output = new FileWriter(fileName);
    	  //writer = new BufferedWriter(output);
    tradeSequence(NMC_USD, NMC_BTC,"namecoin", "nmc/btc");
      }
      catch (NullPointerException | BTCEException | InterruptedException | IOException e) 
      {
        e.printStackTrace();
      }
    }
  }
  //peercoin exploit sequence 
  static class peercoinSequence extends Thread
  {
    public void run()
    {
      try 
      {
    	  fileName = "litecoinOutput.txt";
    	  //output = new FileWriter(fileName);
    	  //writer = new BufferedWriter(output);
    tradeSequence(PPC_USD, PPC_BTC,"peercoin", "ppc/btc");
      } 
      catch (NullPointerException | BTCEException | InterruptedException | IOException e)
      {
         e.printStackTrace();
      }
    }
  }
  // new instances of each exploit sequence are added here to be ran as thread of a timer task
  static class aggregator extends TimerTask
  {
    public void run()
    {
      try 
      {
    btcToUsdBuy = account.btce.getTicker(BTC_USD).buy;
      } 
      catch (BTCEException e) 
      {

    e.printStackTrace();
      }
      try 
      {
    btcToUsdSell = account.btce.getTicker(BTC_USD).sell;
      } 
      catch (BTCEException e)
      {
    e.printStackTrace();
      }

      litecoinSequence t1 = new litecoinSequence();
      namecoinSequence t2 = new namecoinSequence();
      peercoinSequence t3 = new peercoinSequence();

      t1.start();
      t2.start();
      t3.start();

    }
  }


}