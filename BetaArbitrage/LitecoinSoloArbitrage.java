/*
Project SIMBA, a crypto currency AI trading bot 
Copyright (C) 2014  Matthew Bennett 

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
*/

package BetaArbitrage;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimerTask;

import BetaArbitrage.BTCE.BTCEException;
import BetaArbitrage.BTCE.CancelOrder;
import BetaArbitrage.BTCE.Info;
import BetaArbitrage.BTCE.OrderList;
import BetaArbitrage.BTCE.OrderListOrder;
import BetaArbitrage.BTCE.Ticker;
import BetaArbitrage.BTCE.Trade;
import BetaArbitrage.BTCE.TradeHistory;
import BetaArbitrage.BTCE.TradeHistoryOrder;
import BetaArbitrage.BTCE.TradesDetail;
import BetaArbitrage.BTCE.TransactionHistory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;
import java.util.Timer;
import java.util.Scanner;
public class LitecoinSoloArbitrage {
  //main method

    //authorization and account variables 
    static String key = ""; // public key to log in 
    static String secret = "";   // private key to log in 
    static long auth_request_limit = 1000;  // time between request 
    long auth_last_request = 0;     
    static long request_limit = 15000;
    long nonce = 0, last_nonce = 0; // log in nonce
    static String trade_type; // buy or sell  
    static Timer timer; //timer for pulling in stats
    static int delay = 2000; // time interval to pull in stats in milliseconds
    static BTCE btce; // acount 

    //trade variables 
    static boolean hold; // when true no trades are done
    static String pair; // currency pair 
    static double price; // price of currency on trade
    static Info info; // account info
    static double amount; // amount of funds bought on trade
    static double funds; // funds availiable to trade

    //currency variables(pairs)
    public static final String BTC_USD = "btc_usd" ;
    public static final String BTC_RUR = "btc_rur" ;
    public static final String BTC_EUR = "btc_eur" ;
    public static final String LTC_BTC = "ltc_btc" ;
    public static final String LTC_USD = "ltc_usd" ;
    public static final String LTC_RUR = "ltc_rur" ;
    public static final String LTC_EUR = "ltc_eur" ;
    public static final String NMC_BTC = "nmc_btc" ;
    public static final String NMC_USD = "nmc_usd" ;
    public static final String PPC_BTC = "ppc_btc" ;
    public static final String PPC_USD = "ppc_usd" ;
    public static final String USD_RUR = "usd_rur" ;
    public static final String EUR_USD = "eur_usd" ;

    //various method variables 
    static double btcToLtc; 
    static double btcToLtcTemp;
    static double btcToUsd;
    static double ltcToUsd;
    static double remainder;
    static double profit;
    static double stringProfit;
    static boolean orderFilled;
    static Double accountBtc;
    static Double accountLtc;
    static double currentFunds;

    //main method of course 
    public static void main(String[] args) throws BTCEException, InterruptedException
    {
      btce = new BTCE();
      key = "";
      secret = "";
      btce.setAuthKeys(key, secret);
      btce.setAuthRequestLimit(auth_request_limit);
      btce.setRequestLimit(request_limit);
      info = btce.getInfo();

      //Timer task that is the arbitraige 
      Timer btTimer = new Timer();
      btcTrader matt = new btcTrader();
      cleanStaleFunds cleaner = new cleanStaleFunds();
      btTimer.schedule(matt,0,delay);
      btTimer.schedule(cleaner,0,3000);


    }






  /**
  buy method
  */
  public static boolean buy(String currncey) throws BTCEException, NullPointerException
  { 
    info = btce.getInfo();
      if(currncey.equals("btc/usd"))
        {
          price = btcToUsd - 0.06;
          pair = BTCE.Pairs.BTC_USD;
          funds = 9; 
        }
      else if(currncey.equals("ltc/btc"))
        {
          price = btcToLtcTemp;
          pair = BTCE.Pairs.LTC_BTC;
          funds = info.info.funds.btc; 
        }
      else if(currncey.equals("ltc/usd"))
        {
          price = ltcToUsd;
          pair = BTCE.Pairs.LTC_USD;
          funds = 9;
        }

      trade_type = BTCE.TradeType.BUY; // type of trade
      amount = round(funds / (price*1.002),4); // set amount to trade to max
      System.out.println(amount);
      System.out.println(trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
      Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
      System.out.println(trade);
      if(trade.success == 1)
      {
        return true;
      }
      else 
      {
        return false;
      }
  }


  /**
  sell method 
  */
  public static boolean sellLtc() throws BTCEException, NullPointerException 
  {
      trade_type = BTCE.TradeType.SELL; // type of trade
      price = ltcToUsd;  // sell slightly lower than asking price to insure immedaite trade 
      pair = BTCE.Pairs.LTC_USD; //which currncey pair to buy 
      info = btce.getInfo();  // account info 
    
         amount = info.info.funds.ltc;
          
        System.out.println(trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
        Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
        System.out.println(trade);
        if(trade.success == 1)
        {
          return true;
        }
        else 
        {
          return false; 
        }
  }

  public static void sellB() throws BTCEException
  {
    trade_type = BTCE.TradeType.SELL; // type of trade
    price = btce.getTicker(BTC_USD).sell;  // sell slightly lower than asking price to insure immedaite trade 
    pair = BTCE.Pairs.BTC_USD; //which currncey pair to buy 
    info = btce.getInfo(); // account info 
    amount =0; funds = 0; 

         amount = info.info.funds.btc;
      
      System.out.println(trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
      Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
      System.out.println(trade);


  }
  
  public static void sellL() throws BTCEException
  {
    trade_type = BTCE.TradeType.SELL; // type of trade
    price = btce.getTicker(LTC_USD).sell;  // sell slightly lower than asking price to insure immedaite trade 
    pair = BTCE.Pairs.LTC_USD; //which currncey pair to buy 
    info = btce.getInfo(); // account info 
    amount =0; funds = 0; 

         amount = info.info.funds.ltc;
      
      System.out.println(trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
      Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
      System.out.println(trade);


  }
  public static boolean sellBtc() throws BTCEException, NullPointerException 
  {
      trade_type = BTCE.TradeType.SELL; // type of trade
      price = btcToUsd + 0.06;  // sell slightly lower than asking price to insure immedaite trade 
      pair = BTCE.Pairs.BTC_USD; //which currncey pair to buy 
      info = btce.getInfo();  // account info 
    
         amount = info.info.funds.btc;
        
          
        System.out.println(trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
        Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
        System.out.println(trade);
        if(trade.success == 1)
        {
          return true;
        }
        else 
        {
          return false; 
        }
  }

  public static boolean sellLtcToBtc() throws BTCEException, NullPointerException 
  {
      trade_type = BTCE.TradeType.SELL; // type of trade
      price = btcToLtcTemp;  // sell slightly lower than asking price to insure immedaite trade 
      pair = BTCE.Pairs.LTC_BTC; //which currncey pair to buy 
      info = btce.getInfo();  // account info 
        try{
         amount = info.info.funds.ltc;
        }catch(NullPointerException e){
          info = btce.getInfo();
          amount = info.info.funds.ltc;
        }
        System.out.println(trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
        Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
        System.out.println(trade);
        if(trade.success == 1)
        {
          return true;
        }
        else 
        {
          return false; 
        }
  }

  /**
  rounding method 
  */
  private static double round(double value, int places) 
  {
      if (places < 0) throw new IllegalArgumentException();

      long factor = (long) Math.pow(10, places);
      value = value * factor;
      long tmp = Math.round(value);
      return (double) tmp / factor;
  }

  /**
  Thread for BTC 
  */
  static class btcTrader extends TimerTask  
  {
    public void run()
    {
     try {
    checkForProfit();
  } catch (NullPointerException e) {
    // TODO Auto-generated catch block
    try {
  } catch (NullPointerException e1) {
    // TODO Auto-generated catch block
    e1.printStackTrace();
  }
    e.printStackTrace();
  } catch (BTCEException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  } catch (InterruptedException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
    }
  }
/**
sells stale funds left behind by trades 
*/ 
static class cleanStaleFunds extends TimerTask
{
  public void run()
  {
    System.out.println("Clearing out accounts of stale funds from old trades");
  try {
  info = btce.getInfo();
} catch (BTCEException e1) {
  // TODO Auto-generated catch block
  e1.printStackTrace();
}
  try {
  if(info.info.funds.btc > 0)
    {
      try {
    sellB();
  } catch (NullPointerException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  } catch (BTCEException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
    }
  if(info.info.funds.ltc > 0)
    {
      try {
    sellL();
  } catch (NullPointerException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  } catch (BTCEException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
    }
  }catch(NullPointerException e)
  {
    //do nothing 
  }
  }
}
  /**
  check to see if the trade between currencies is going to Profitable 
   * @throws BTCEException 
   * @throws NullPointerException 
   * @throws InterruptedException 
  */
  public static void checkForProfit() throws NullPointerException, BTCEException, InterruptedException
  {
    System.out.println();
      System.out.println("Checking For a Profitable Loop of Trades... Please Stand By...");

      //Variable holding values of currency pairs that could be exploited 
      btcToUsd = btce.getTicker(BTC_USD).buy;
      btcToLtcTemp = btce.getTicker(LTC_BTC).buy;
      btcToLtc = btcToLtcTemp * btcToUsd;
      ltcToUsd = btce.getTicker(LTC_USD).sell;
      remainder = ltcToUsd - btcToLtc;
     

    System.out.println("BTC/USD = "+btcToUsd);
    System.out.println("BTC/LTC temp =" +btcToLtcTemp);
    System.out.println("BTC/LTC ="+ btcToLtc);
    System.out.println("LTC/USD ="+ltcToUsd);
    System.out.println("remainder ="+ remainder);
    if(remainder > 0)
    {
      // IMPORTANT IMPORTANT IMPORTANT IMPORTANT IMPORTANT NEXT 5 LINES BELOW!!!!!
      profit = (9 / btcToLtc) * remainder;
      stringProfit = profit -0.04;
      System.out.println("profit would be "+stringProfit);
      if(profit > 0.04)
      {
        System.out.println("Trade Loop Intiating For a Profit of " +stringProfit);
        if(buy("btc/usd") == true)
        {
          info = btce.getInfo();
          accountBtc = info.info.funds.btc;
          currentFunds = 9 / btcToUsd - 0.05;
          while(accountBtc < currentFunds)
          {
            Thread.sleep(1000);
            info = btce.getInfo();
            accountBtc = info.info.funds.btc;
            System.out.println("waiting for order #1 to be filled, btc funds are currently "+accountBtc);
          }
        }
        if(buy("ltc/btc") == true)
        {
         info = btce.getInfo();
         accountLtc = info.info.funds.ltc;
         currentFunds = currentFunds / btcToLtcTemp - 0.05;
         while(accountLtc < currentFunds)
         {
            Thread.sleep(1000);
            info = btce.getInfo();     
            accountLtc = info.info.funds.ltc;
            System.out.println("waiting for order #2 to be filled, ltc funds are currently "+accountLtc);
          }
        }
        else if(buy("ltc/btc") == true)
        {
         info = btce.getInfo();
         accountLtc = info.info.funds.ltc;
         currentFunds = currentFunds / btcToLtcTemp - 0.05;
         while(accountLtc < currentFunds)
          {
            Thread.sleep(1000);
            info = btce.getInfo();
            accountLtc = info.info.funds.ltc;
            System.out.println("waiting for order #2 to be filled, ltc funds are currently "+accountLtc);
          }
        }
         sellLtc();
      }
    }
    
    
      System.out.println("checking for reverse profits in a reverse seqeuance of trades...");
      btcToUsd = btce.getTicker(BTC_USD).sell;
      btcToLtcTemp = btce.getTicker(LTC_BTC).sell;
      btcToLtc = btcToLtcTemp * btcToUsd;
      ltcToUsd = btce.getTicker(LTC_USD).buy;
      remainder = btcToLtc - ltcToUsd;
      
      System.out.println("BTC/USD = "+btcToUsd);
      System.out.println("BTC/LTC temp =" +btcToLtcTemp);
      System.out.println("BTC/LTC ="+ btcToLtc);
      System.out.println("LTC/USD ="+ltcToUsd);
      System.out.println("remainder ="+ remainder);
      // IMPORTANT IMPORTANT IMPORTANT IMPORTANT IMPORTANT NEXT 5 LINES BELOW!!!!!
      profit = (9 / btcToLtc) * remainder;
      stringProfit = profit - 0.04;
      System.out.println("profit would be "+stringProfit);
      if(profit > 0.04)
      {
        if(buy("ltc/usd") == true)
        {
          info = btce.getInfo();
          accountLtc = info.info.funds.ltc;
          currentFunds = 9 / ltcToUsd - 0.05;

          while(accountLtc < currentFunds)
          {
            Thread.sleep(1000);
            info = btce.getInfo();
            accountLtc = info.info.funds.btc;
            System.out.println("waiting for order #1 to be filled, ltc funds are currently "+accountBtc);
          }
        }
        if(sellLtcToBtc() == true)
        {
         info = btce.getInfo();
         accountBtc = info.info.funds.btc;
         currentFunds = currentFunds * btcToLtcTemp - 0.05;
         while(accountBtc < currentFunds)
          {
            Thread.sleep(1000);
            info = btce.getInfo();
            accountBtc = info.info.funds.btc;
            System.out.println("waiting for order #2 to be filled, btc funds are currently "+accountLtc);
          }
        }
        else if(sellLtcToBtc() == true)
        {
         info = btce.getInfo();
         accountBtc = info.info.funds.btc;
         currentFunds = currentFunds * btcToLtcTemp - 0.05;
         while(accountBtc < currentFunds)
          {
            Thread.sleep(1000);
            info = btce.getInfo();
            accountBtc = info.info.funds.btc;
            System.out.println("waiting for order #2 to be filled, btc funds are currently "+accountLtc);
          }
        }
        if(sellBtc() == false)
        {
          Thread.sleep(1000);
          sellBtc();
        } 
    }

  }

  
  
}

