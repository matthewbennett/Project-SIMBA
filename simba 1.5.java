/*
Project SIMBA, a crypto currency AI trading bot 
Copyright (C) 2014  Matthew Bennett 

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
*/ 

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.TimerTask;

import com.abwaters.btce.BTCE;
import com.abwaters.btce.BTCE.BTCEException;
import com.abwaters.btce.BTCE.CancelOrder;
import com.abwaters.btce.BTCE.Info;
import com.abwaters.btce.BTCE.OrderList;
import com.abwaters.btce.BTCE.OrderListOrder;
import com.abwaters.btce.BTCE.Ticker;
import com.abwaters.btce.BTCE.Trade;
import com.abwaters.btce.BTCE.TradeHistory;
import com.abwaters.btce.BTCE.TradeHistoryOrder;
import com.abwaters.btce.BTCE.TradesDetail;
import com.abwaters.btce.BTCE.TransactionHistory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TimerTask;
import java.util.Timer;
import java.util.Scanner; 

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class SimbaBeta
{
	//static double sellPrice;// price to sell  CONSIDERING SLASHING 
	//static double buyPrice;//price to buyPrice   CONSIDERING SLASHING 
	static String key = "";	// public key to log in 
	static String secret = "";	 // private key to log in 
	static long auth_request_limit = 1000;	// time between request 
	long auth_last_request = 0;			
	static long request_limit = 15000;
	long nonce = 0, last_nonce = 0;	// log in nonce
	static String trade_type;	// buy or sell 	
	static Timer timer; //timer for pulling in stats
	static int delay = 30000; // time interval to pull in stats in milliseconds
	static BTCE btce;	// acount 
	
	static ArrayList<Double> buyBtc = new ArrayList<Double>(); // array holding 10 minutes of buying prices
	static ArrayList<Double> buyBtcD1 = new ArrayList<Double>(); // derivative 1 of the buying prices, with array[10] = slope of the 10 minutes 
	static ArrayList<Double> sellBtc = new ArrayList<Double>();
	static ArrayList<Double> sellBtcD1 = new ArrayList<Double>();
	static TradesDetail[] tradeBook = new TradesDetail[100];

	static double fundUSD;
	static double fundsBTC;
	

	static boolean hold; // when true no trades are done
	static String pair; // currency pair 
	static double price; // price of currency on trade
	static Info info; // account info
	static double amount; // amount of funds bought on trade
	static double funds; // funds availiable to trade
	static double startingHoldValue; // value of currency when hold is intiated 
	static double currentHoldValue; // value of the currency after every pass 
	static double boughtPrice; // price at which the currency was bought
	static double fundsBoughtBtc; // amount of funds bought
	static double fundsBoughtLtc;
	static double overallSlope; 
	static double numerator; // used in derivative methods
	static double denomenator; // used in derivative methods 
	static String btstatus = "Intaiting"; 
	static int points;
	static double pointSetter;
	static double sellPrice;
	static double marker;
	static String rade;
	static int indexStart;
	static int indexEnd;
	static String radeAmountString;
	static double radeAmount;
	static String radeTradeString;
	static int radeBids;
	static int radeSells;
	static double radeBidsAmount;
	static double radeSellsAmount;
	static double buyTrend;
	static double sellTrend;
	static double halfHourSlopeSell;
	static double halfHourSlopeBuy;
	static double hourSlopeBuy;
	static double hourSlopeSell;
	static double trendBuySum;
	static double trendSellSum;

	


	static boolean buyingBtc; //determines last trade
	static boolean demoBool;
	static boolean buyingLtc; 
	static int ssCounter; // counter for slopeStates method 
	static int btCounter;


	final static String BTC_USD = "btc_usd" ;  // currency pair  

	public static void main(String[] args) throws BTCEException
	{
	/**
		intiates the account variable @BTCE and logs into the account
	*/
  btce = new BTCE();
  key = "YSJS8N4E-UXEQYNOS-4QZ4GGNO-GJXRAK9H-7CXR8F5P";
  secret = "1342261b8a293852349a03e3b121d2b8ea0b35e9279b040255f76b3c082cde63";
  pointSetter = 2;
  btce.setAuthKeys(key, secret);
  btce.setAuthRequestLimit(auth_request_limit);
  btce.setRequestLimit(request_limit);
  info = btce.getInfo();

  pullStats();
  buyOrSell();
	btCounter = 0;
	Timer btTimer = new Timer();
  btcTrader matt = new btcTrader();
  btTimer.schedule(matt,0,delay);
}
/**
buying or selling 
*/ 
public static void buyOrSell() throws BTCEException
{
	 if(info.info.funds.btc < .1) // determines buying or selling to start 
		  {
		    	buyingBtc = true;
			}
			else
			{
				buyingBtc = false;
			
		    ssCounter =0;
			}
			   try {
				boughtPrice = lastBuy();
			} catch (BTCEException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
}

/**
pulls stats into arrays for either btc or ltc specified @parm currecny 
 * @throws BTCEException 
*/
public static void pullStats() throws BTCEException
{
	if(buyBtc.size() > 119)
	{
		buyBtc.add(0, btce.getTicker(BTC_USD).buy);
		sellBtc.add(0, btce.getTicker(BTC_USD).sell);
		buyBtc.remove(120);
		sellBtc.remove(120);	
	}
	else
	{
		buyBtc.add(0, btce.getTicker(BTC_USD).buy);
		sellBtc.add(0, btce.getTicker(BTC_USD).sell);
	}
}
/**
determines rather or not the market is @ a tipping point
*/
public static boolean tippingPoint() throws BTCEException
{
	boolean tipping = false;
	double difference = radeBidsAmount - radeSellsAmount;
	if(difference < 3 && difference > -3)
	{
		tipping = true;
	}
	return tipping;
}
/**
Trade Book Stats 
 * @return 
*/ 
public static void radeBook() throws BTCEException
{
	tradeBook = btce.getTrades(BTC_USD);
	for(int i = 0; i < 99; i++)
	{
		// get the amount of btc for trade i
		radeAmount = tradeBook[i].amount;
		// get the trade i's trade type 
		radeTradeString = tradeBook[i].trade_type;

		if(radeTradeString.equals("bid"))
		{
			radeBids++;
			radeBidsAmount += radeAmount;
		}
		else
		{
			radeSells++;
			radeSellsAmount += radeAmount;
		}
		
	}
	System.out.println("Rade Book buys "+radeBidsAmount);
	System.out.println("Rade Book Sells "+radeSellsAmount);
}
/** 
pulls in the last buy to make sure the bot does not sell assest lower than the last purchase 
*/
public static double lastBuy() throws BTCEException
{
	TransactionHistory transaction_history = btce.getTransactionHistory();
	String here = transaction_history.toString();
	int marker = here.indexOf("p",100);
	String price = here.substring(marker+6,marker + 8);
			
			double lastAmount = Double.parseDouble(price);
			return lastAmount;

}
/**
get the trend for an hour
*/
public static void getTrend()throws BTCEException
{
	tradeBook = btce.getTrades(BTC_USD);
	for(int i = 0; i < 99; i++)
	{
		// get the amount of btc for trade i
		radeAmount = tradeBook[i].amount;
		// get the trade i's trade type 
		radeTradeString = tradeBook[i].trade_type;

		if(radeTradeString.equals("bid"))
		{
			 trendBuySum =+ radeAmount;
		}
		else
		{
			 trendSellSum =+ radeAmount;
		}
	}
	ArrayList<Double> trendArraySell = new ArrayList<Double>();
	ArrayList<Double> trendArrayBuy = new ArrayList<Double>();
	trendArrayBuy.add(trendBuySum / 99);
	trendArraySell.add(trendSellSum / 99);
	trendBuySum = 0;
	trendSellSum = 0; 
	if(trendArraySell.size() > 360)
	{
		for(int j = 0; j < 359; j++)
		{
			trendBuySum += trendArrayBuy.get(j);
			trendSellSum += trendArraySell.get(j);
			buyTrend = trendBuySum / 360;
			sellTrend = trendSellSum / 360;

			trendBuySum = 0;
			trendSellSum = 0;
			trendArraySell.clear();
			trendArrayBuy.clear();
		}
	}
}

/**
gets the slope for the past 30 minutes
*/ 
public static void halfHourSlope()
{
	if(buyBtc.size() > 180)
	{
	halfHourSlopeBuy = (buyBtc.get(0) - buyBtc.get(180) / buyBtc.get(180));
	halfHourSlopeSell = (sellBtc.get(0) - sellBtc.get(180) / sellBtc.get(180));
	}
}
/**
gets the slope for the past hour
*/
public static void hourSlope()
{
	if(buyBtc.size() > 360)
	{
	hourSlopeBuy = (buyBtc.get(0) - buyBtc.get(360) / buyBtc.get(360));
	hourSlopeSell = (sellBtc.get(0) - sellBtc.get(360) / sellBtc.get(360));
	}
}
/** 
performs the fisrt derivative for the specified currency either btc or ltc @parm currency and fills the approriate arrays 
*/
public static void slopeStats()
{
	if(buyBtcD1.size() > 359)
	{
		buyBtcD1.remove(360);
		sellBtcD1.remove(360);
	}
	numerator =	buyBtc.get(0) - buyBtc.get(1);
	denomenator = buyBtc.get(1);
	overallSlope = ( numerator / denomenator ) * 100;
	buyBtcD1.add(0, overallSlope);
	numerator =	sellBtc.get(0) - sellBtc.get(1);
	denomenator = sellBtc.get(1);
	overallSlope = ( numerator / denomenator ) * 100;
	sellBtcD1.add(0, overallSlope);
}
/**
buy method
*/
public static void buy() throws BTCEException, NullPointerException
{ 
		trade_type = BTCE.TradeType.BUY; // type of trade
		price = btce.getTicker(BTC_USD).buy;  // buy slightly higher than asking price to insure immedaite trade 
		boughtPrice = price;
		pair = BTCE.Pairs.BTC_USD; //which currncey pair to buy 
		info = btce.getInfo(); // account info 
		if(info.info.funds.usd < 5 || info.info.funds.usd < .1)
		{
			System.out.println("not enough funds to buy");
		}
		else
		{
	
	        funds = (int) info.info.funds.usd; // set funds to the funds in info 
	        amount = round(funds / (price*1.002),4); // set amount to trade to max
	    	  
	    System.out.println(trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
	    Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
    	System.out.println(trade);
		}
}
/**
sell method 
*/
public static void sell() throws BTCEException, NullPointerException 
{
		trade_type = BTCE.TradeType.SELL; // type of trade
		price = btce.getTicker(BTC_USD).sell;  // sell slightly lower than asking price to insure immedaite trade 
		pair = BTCE.Pairs.BTC_USD; //which currncey pair to buy 
		info = btce.getInfo();  // account info 
		if(info.info.funds.btc < 0.01)
		{
			System.out.println("not enough btc to sell");
		}
		else
		{
	
	  	 amount = info.info.funds.btc;
	    	
	    System.out.println(trade_type.toUpperCase()+" @ Price: "+price+" Amount: "+amount);
	   	Trade trade = btce.trade(pair,trade_type,price,amount); // actual trade
    	System.out.println(trade);
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
performs logic with collected data and checks for trades
*/
public static void checkForTrades() throws BTCEException, NullPointerException
{
	  if(buyingBtc == false)
  {
    if(boughtPrice + 5 + (info.info.funds.btc * sellBtc.get(0)) * .02 < sellBtc.get(0))
    {
    if(sellBtc.get(0) > trendSellSum) {points++;}
    if(halfHourSlopeBuy < 1) {points++;}
    if(hourSlopeSell < 1) {points++;}
    if(tippingPoint() == true) {points++;}
    if(points > pointSetter)
    {
      //sell();
      System.out.println("sell");
      sellPrice = sellBtc.get(0);
      points = 0;
      buyingBtc = true;
    }
    }
    else {points = 0;}
  }
  else 
  {
    marker++;
    if(buyBtc.get(0) < sellPrice || marker == 30)
    {
      if(buyBtc.get(0) < trendBuySum){points++;}
      if(halfHourSlopeBuy > 1){points++;}
      if(hourSlopeSell > 1){points++;}
      if(tippingPoint() == true){points++;}

      if(points > pointSetter)
      {
        //buy()
        System.out.println("buy");
        points = 0;
        marker = 0;
        buyingBtc = false;
      }
      else
      {
        points = 0;
      }

    }
  }
}
/**
Thread for BTC 
*/
static class btcTrader extends TimerTask 
{
	public void run()
	{
		try {
			System.out.println("Bitcoin trader is pulling stats");
			pullStats();
			slopeStats();
			radeBook();
		} catch (BTCEException e1) {
			e1.printStackTrace();
		}
		try {
			System.out.println("Bitcoin trader is checking for trades");
			System.out.println("buy: "+buyBtc.get(0) + "sell: "+ sellBtc.get(0) + "slope: "+buyBtcD1.get(0));
			checkForTrades();
			radeBidsAmount = 0;
			radeSellsAmount = 0;
		} catch (NullPointerException e) {
			fundsBoughtBtc = 0;
			e.printStackTrace();
		} catch (BTCEException e) {
			e.printStackTrace();
		}
	}
}
}