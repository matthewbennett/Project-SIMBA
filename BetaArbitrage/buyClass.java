/*
Project SIMBA, a crypto currency AI trading bot 
Copyright (C) 2014  Matthew Bennett 

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
package BetaArbitrage;
*/

import BetaArbitrage.BTCE.BTCEException;
import BetaArbitrage.BTCE.Info;
import BetaArbitrage.BTCE.Trade;
import BetaArbitrage.BTCE;
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

/**
The buyClass contains all necessary methods for the SIMBA ARBITRAGE bot
*/
public class buyClass extends sellClass
{
  public static double tradingFunds; //funds that the user allows this bot to trade with
/**
@currecny currency pair lowercase, example "btc/usd" the first numerator currency is the intended currency to buy 
@priceUnit the price at to buy the currency at  
*/
  public static boolean buy(String currncey, double priceUnit) throws BTCEException, NullPointerException
  { 
      // class variables 
     boolean hold; // when true no trades are done
     String pair = null; // currency pair 
     double price = 0; // price of currency on trade
     double amount = 0; // amount of funds bought on trade
     double funds = 0; // funds availiable to trade
     String trade_type; // buy or sell  

    switch(currncey)
    {
      case "btc/usd": 
            price = (long) (priceUnit - 0.06);
            pair = BTCE.Pairs.BTC_USD;
            funds = tradingFunds; 
            break;
      case "ltc/btc":
            price =  priceUnit;
            pair = BTCE.Pairs.LTC_BTC;
            funds = info.info.funds.btc;
            break; 
      case "ltc/usd": 
            price =  priceUnit;
            pair = BTCE.Pairs.LTC_USD;
            funds = tradingFunds;
            break;
      case "nmc/usd":
            price =  priceUnit;
            pair = BTCE.Pairs.NMC_USD;
            funds = tradingFunds;
            break;
      case "nmc/btc":
            price =  priceUnit;
            pair = BTCE.Pairs.NMC_BTC;
            funds = tradingFunds;
            break;
      case "ppc/btc":
            price =  priceUnit;
            pair = BTCE.Pairs.PPC_BTC;
            funds = tradingFunds;
            break;
      case "ppc/usd":
            price =  priceUnit;
            pair = BTCE.Pairs.PPC_USD;
            funds = tradingFunds;  
            break;
    }
    writeTextFile(fileName,"funds "+funds+" amount "+amount);
      trade_type = BTCE.TradeType.BUY; // type of trade
      amount = round(funds / (price*1.002),4); // set amount to trade to max
      writeTextFile(fileName,Double.toString(amount));
      writeTextFile(fileName,trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
      Trade trade = account.btce.trade(pair,trade_type,price,amount); // actual trade
      writeTextFile(fileName,trade.toString());
      if(trade.success == 1)
      {
        return true;
      }
      else 
      {
        return false;
      }
  }
  
  //rounding method specific for the trade methods 
  private static double round(double value, int places) 
  {
      if (places < 0) throw new IllegalArgumentException();

      long factor = (long) Math.pow(10, places);
      value = value * factor;
      long tmp = Math.round(value);
      return (double) tmp / factor;
  }
  public static void setTradingFunds(double input)
  {
    tradingFunds = input;
  }
}