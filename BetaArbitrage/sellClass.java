/*
Project SIMBA, a crypto currency AI trading bot 
Copyright (C) 2014  Matthew Bennett 

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
*/
package BetaArbitrage;


import java.io.*;
import BetaArbitrage.BTCE.BTCEException;
import BetaArbitrage.BTCE.Info;
import BetaArbitrage.BTCE.Trade;

/**
The sellClass contains all necessary methods for the SIMBA ARBITRAGE bot, seperated to different methods due to compiler issue
*/
public class sellClass 
{
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
    
    public static Info info; // this info is for the sequence class only!!!!!!!!
    public static BTCE btce = new BTCE();
    public static String fileName = null;
    public static FileWriter output;
    public static BufferedWriter writer;
    
    

  
    
    public static boolean sellLtc(double pricePoint) throws BTCEException, NullPointerException 
  {
       boolean hold; // when true no trades are done
       String pair = null; // currency pair 
       double price = 0; // price of currency on trade
       Info info = null; // account info
       double amount; // amount of funds bought on trade
       double funds = 0; // funds availiable to trade
       double tradingFunds = 0; //funds that the user allows this bot to trade with
       String trade_type; // buy or sell  
      trade_type = BTCE.TradeType.SELL; // type of trade
      price = pricePoint;  // sell slightly lower than asking price to insure immedaite trade 
      pair = BTCE.Pairs.LTC_USD; //which currncey pair to buy 
      info = btce.getInfo();  // account info 
    
         amount = info.info.funds.ltc;
          
        writeTextFile(fileName, trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
        Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
        writeTextFile(fileName, trade.toString());
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
       boolean hold; // when true no trades are done
       String pair = null; // currency pair 
       double price = 0; // price of currency on trade
       Info info = null; // account info
       double amount; // amount of funds bought on trade
       double funds = 0; // funds availiable to trade
       double tradingFunds = 0; //funds that the user allows this bot to trade with
       String trade_type; // buy or sell  
    trade_type = BTCE.TradeType.SELL; // type of trade
    price = btce.getTicker(BTC_USD).sell;  // sell slightly lower than asking price to insure immedaite trade 
    pair = BTCE.Pairs.BTC_USD; //which currncey pair to buy 
    info = btce.getInfo(); // account info 
    amount =0; funds = 0; 

         amount = info.info.funds.btc;
      
      writeTextFile(fileName, trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
      Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
      writeTextFile(fileName, trade.toString());


  }
  
  public static void sellL() throws BTCEException
  {
       boolean hold; // when true no trades are done
       String pair = null; // currency pair 
       double price = 0; // price of currency on trade
       Info info = null; // account info
       double amount; // amount of funds bought on trade
       double funds = 0; // funds availiable to trade
       double tradingFunds = 0; //funds that the user allows this bot to trade with
       String trade_type; // buy or sell  
    trade_type = BTCE.TradeType.SELL; // type of trade
    price = btce.getTicker(LTC_USD).sell;  // sell slightly lower than asking price to insure immedaite trade 
    pair = BTCE.Pairs.LTC_USD; //which currncey pair to buy 
    info = btce.getInfo(); // account info 
    amount =0; funds = 0; 

         amount = info.info.funds.ltc;
      
      writeTextFile(fileName, trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
      Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
      writeTextFile(fileName, trade.toString());


  }

    public static void sellN() throws BTCEException
  {
       boolean hold; // when true no trades are done
       String pair = null; // currency pair 
       double price = 0; // price of currency on trade
       Info info = null; // account info
       double amount; // amount of funds bought on trade
       double funds = 0; // funds availiable to trade
       double tradingFunds = 0; //funds that the user allows this bot to trade with
       String trade_type; // buy or sell  
    trade_type = BTCE.TradeType.SELL; // type of trade
    price = btce.getTicker(NMC_USD).sell;  // sell slightly lower than asking price to insure immedaite trade 
    pair = BTCE.Pairs.NMC_USD; //which currncey pair to buy 
    info = btce.getInfo(); // account info 
    amount =0; funds = 0; 

         amount = info.info.funds.nmc;
      
      writeTextFile(fileName, trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
      Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
      writeTextFile(fileName, trade.toString());


  }

    public static void sellP() throws BTCEException
  {
       boolean hold; // when true no trades are done
       String pair = null; // currency pair 
       double price = 0; // price of currency on trade
       Info info = null; // account info
       double amount; // amount of funds bought on trade
       double funds = 0; // funds availiable to trade
       double tradingFunds = 0; //funds that the user allows this bot to trade with
       String trade_type; // buy or sell  
    trade_type = BTCE.TradeType.SELL; // type of trade
    price = btce.getTicker(PPC_USD).sell;  // sell slightly lower than asking price to insure immedaite trade 
    pair = BTCE.Pairs.PPC_USD; //which currncey pair to buy 
    info = btce.getInfo(); // account info 
    amount =0; funds = 0; 

         amount = info.info.funds.ltc;
      
      writeTextFile(fileName, trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
      Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
      writeTextFile(fileName, trade.toString());


  }
  public static boolean sellBtc(double pricePoint) throws BTCEException, NullPointerException 
  {
       boolean hold; // when true no trades are done
       String pair = null; // currency pair 
       double price = 0; // price of currency on trade
       Info info = null; // account info
       double amount; // amount of funds bought on trade
       double funds = 0; // funds availiable to trade
       double tradingFunds = 0; //funds that the user allows this bot to trade with
       String trade_type; // buy or sell  
      trade_type = BTCE.TradeType.SELL; // type of trade
      price = pricePoint + 0.06;  // sell slightly lower than asking price to insure immedaite trade 
      pair = BTCE.Pairs.BTC_USD; //which currncey pair to buy 
      info = btce.getInfo();  // account info 
    
         amount = info.info.funds.btc;
         amount = Math.round(amount);
          
        writeTextFile(fileName, trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
        Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
        writeTextFile(fileName, trade.toString());
        if(trade.success == 1)
        {
          return true;
        }
        else 
        {
          return false; 
        }
  }

  public static boolean sellLtcToBtc(double pricePoint) throws BTCEException, NullPointerException 
  {
       boolean hold; // when true no trades are done
       String pair = null; // currency pair 
       double price = 0; // price of currency on trade
       Info info = null; // account info
       double amount; // amount of funds bought on trade
       double funds = 0; // funds availiable to trade
       double tradingFunds = 0; //funds that the user allows this bot to trade with
       String trade_type; // buy or sell  
      trade_type = BTCE.TradeType.SELL; // type of trade
      price = pricePoint;  // sell slightly lower than asking price to insure immedaite trade 
      pair = BTCE.Pairs.LTC_BTC; //which currncey pair to buy 
      info = btce.getInfo();  // account info 
        try{
         amount = info.info.funds.ltc;
        }catch(NullPointerException e){
          info = btce.getInfo();
          amount = info.info.funds.ltc;
        }
        writeTextFile(fileName, trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
        Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
        writeTextFile(fileName, trade.toString());
        if(trade.success == 1)
        {
          return true;
        }
        else 
        {
          return false; 
        }
  }

      public static boolean sellNmc(double priceUnit) throws BTCEException, NullPointerException 
  {
       boolean hold; // when true no trades are done
       String pair = null; // currency pair 
       double price = 0; // price of currency on trade
       Info info = null; // account info
       double amount; // amount of funds bought on trade
       double funds = 0; // funds availiable to trade
       double tradingFunds = 0; //funds that the user allows this bot to trade with
       String trade_type; // buy or sell  
      trade_type = BTCE.TradeType.SELL; // type of trade
      price = priceUnit;  // sell slightly lower than asking price to insure immedaite trade 
      pair = BTCE.Pairs.NMC_USD; //which currncey pair to buy 
      info = btce.getInfo();  // account info 
    
         amount = info.info.funds.nmc;
          
        writeTextFile(fileName, trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
        Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
        writeTextFile(fileName, trade.toString());
        if(trade.success == 1)
        {
          return true;
        }
        else 
        {
          return false; 
        }
  }

        public static boolean sellNmcToBtc(double priceUnit) throws BTCEException, NullPointerException 
  {
         boolean hold; // when true no trades are done
         String pair = null; // currency pair 
         double price = 0; // price of currency on trade
         Info info = null; // account info
         double amount; // amount of funds bought on trade
         double funds = 0; // funds availiable to trade
         double tradingFunds = 0; //funds that the user allows this bot to trade with
         String trade_type; // buy or sell  
      trade_type = BTCE.TradeType.SELL; // type of trade
      price = priceUnit;  // sell slightly lower than asking price to insure immedaite trade 
      pair = BTCE.Pairs.NMC_BTC; //which currncey pair to buy 
      info = btce.getInfo();  // account info 
    
         amount = info.info.funds.nmc;
          
        writeTextFile(fileName, trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
        Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
        writeTextFile(fileName, trade.toString());
        if(trade.success == 1)
        {
          return true;
        }
        else 
        {
          return false; 
        }
  }

      public static boolean sellPpcToBtc(double priceUnit) throws BTCEException, NullPointerException 
  {
       boolean hold; // when true no trades are done
       String pair = null; // currency pair 
       double price = 0; // price of currency on trade
       Info info = null; // account info
       double amount; // amount of funds bought on trade
       double funds = 0; // funds availiable to trade
       double tradingFunds = 0; //funds that the user allows this bot to trade with
       String trade_type; // buy or sell  
      trade_type = BTCE.TradeType.SELL; // type of trade
      price = priceUnit;  // sell slightly lower than asking price to insure immedaite trade 
      pair = BTCE.Pairs.PPC_BTC; //which currncey pair to buy 
      info = btce.getInfo();  // account info 
    
         amount = info.info.funds.ppc;
          
        writeTextFile(fileName, trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
        Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
        writeTextFile(fileName, trade.toString());
        if(trade.success == 1)
        {
          return true;
        }
        else 
        {
          return false; 
        }
  }

      public static boolean sellPpc(double priceUnit) throws BTCEException, NullPointerException 
  {
       boolean hold; // when true no trades are done
       String pair = null; // currency pair 
       double price = 0; // price of currency on trade
       Info info = null; // account info
       double amount; // amount of funds bought on trade
       double funds = 0; // funds availiable to trade
       double tradingFunds = 0; //funds that the user allows this bot to trade with
       String trade_type; // buy or sell  
      trade_type = BTCE.TradeType.SELL; // type of trade
      price = priceUnit;  // sell slightly lower than asking price to insure immedaite trade 
      pair = BTCE.Pairs.PPC_USD; //which currncey pair to buy 
      info = btce.getInfo();  // account info 
    
         amount = info.info.funds.ppc;
          
        writeTextFile(fileName, trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
        Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
        writeTextFile(fileName, trade.toString());
        if(trade.success == 1)
        {
          return true;
        }
        else 
        {
          return false; 
        }
  }

public static synchronized void writeTextFile(String fileName, String s)
{
  FileWriter output = null;
  try 
  {
    output = new FileWriter(fileName,true);
    BufferedWriter writer = new BufferedWriter(output);
    writer.append(s);
    writer.newLine();
    writer.flush();
  }catch(IOException e)
    {
      e.printStackTrace();
    } finally {
      if(output != null)
      {
        try {
          output.close();
        }catch(IOException e){
          e.printStackTrace();
        }
    }
  }
}

}
