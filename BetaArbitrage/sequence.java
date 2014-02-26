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
import java.io.IOException;
import BetaArbitrage.BTCE.BTCEException;

public class sequence extends buyClass 
{
  public static final String BTC_USD = "btc_usd" ;
  public static final String BTC_RUR = "btc_rur" ;
  public static final String BTC_EUR = "btc_eur" ;
  public static final String LTC_BTC = "ltc_btc" ;
  public static final String LTC_USD = "ltc_usd" ;
  public static final String LTC_RUR = "ltc_rur" ;
  public static final String LTC_EUR = "ltc_eur" ;
  public static final String NMC_BTC = "nmc_btc" ;
  public static final String NMC_USD = "nmc_usd" ;
  public static final String NVC_BTC = "nvc_btc" ;
  public static final String NVC_USD = "nvc_usd" ;
  public static final String TRC_BTC = "trc_btc" ;
  public static final String PPC_BTC = "ppc_btc" ;
  public static final String PPC_USD = "ppc_usd" ;
  public static final String FTC_BTC = "ftc_btc" ;
  public static final String USD_RUR = "usd_rur" ;
  public static final String EUR_USD = "eur_usd" ;

  private static double addedValue;
  public static double btcToUsdBuy;
  public static double btcToUsdSell;


  
/**
This class is contains the logic of the Arbitrage
*/


  /*
  @ pairs should be a uppercase final 
  @ currency  
  @ pair to be the param in the buy and sell
  **/
    public static void tradeSequence(String pairs, String btcPairs, String currency, String tradePairs) throws NullPointerException, BTCEException, InterruptedException, IOException
  {
    boolean sellStatus = false;
    boolean buyStatus;
    double btcToCrypto;
    double btcToCryptoTemp;
    double cryptoToUsd;
    int counter = 0;
    double remainder;
    String reversePairs = null;
    double profit;
    double stringProfit;
    double accountBtc;
    double accountCrypto = 0;
    double currentFunds = 0;



    writeTextFile(fileName,"");
   writeTextFile(fileName,currency+" trade sequence started");

    //Variable holding values of currency pairs that could be exploited 
    btcToCryptoTemp = account.btce.getTicker(btcPairs).buy; // bitcoin to desired currency, value is in bitcoin 
    btcToCrypto = btcToCryptoTemp * btcToUsdBuy; // converts the value from BTC to usd 
    cryptoToUsd = account.btce.getTicker(pairs).sell; // desired currency to usd value 
    remainder = cryptoToUsd - btcToCrypto; // value of the difference in value between the pairs 
    addedValue = 0.04; // adds additional value to desired profit to account for trade fee
     

  //write values for checking the testing & checking 
    writeTextFile(fileName,"Bitcoin/"+currency+" = "+btcToUsdBuy);
    writeTextFile(fileName,"Bitcoin/"+currency+" temp =" +btcToCryptoTemp);
    writeTextFile(fileName,"Bitcoin/"+currency+" ="+ btcToCrypto);
    writeTextFile(fileName,currency+"/USD ="+cryptoToUsd);
    writeTextFile(fileName,currency+"remainder ="+ remainder);
//if the remainder is positive further check for possible exploit 
    if(remainder > 0)
    {
      // IMPORTANT IMPORTANT IMPORTANT IMPORTANT IMPORTANT NEXT 5 LINES BELOW!!!!!
      profit = ( tradingFunds / btcToCrypto) * remainder; // the amount of profit that would be made 
      stringProfit = profit - addedValue; // for printing to the user 
      writeTextFile(fileName,currency+" tradingFunds "+tradingFunds);
      writeTextFile(fileName,currency+" profit would be "+stringProfit);
      if(profit > addedValue) // if the trade loop truly is profitable intiate the trade sequence 
      {
        writeTextFile(fileName,currency+" Trade Loop Intiating For a Profit of " +stringProfit);
        buyStatus = buy("btc/usd", btcToUsdBuy); //actual buy trade
        //waits for the buy order to be filled to move forward to next trade 
        while(buyStatus == false || counter > 2)
        {
          buyStatus = buy("btc/usd",btcToUsdBuy); // holds the boolean value of the trade success or not 
          counter++; // counts the amount of times the trade was attempted 
        }
        counter = 0;
        //second trade to btc/crypto 
        if(buyStatus == true)
        {
          info = account.btce.getInfo();
          accountBtc = info.info.funds.btc;
          currentFunds = tradingFunds / btcToUsdBuy - 0.05;
          while(accountBtc < currentFunds)
          {
            Thread.sleep(1000);
            info = account.btce.getInfo();
            try{
            accountBtc = info.info.funds.btc;
              }catch(NullPointerException e){
                info = account.btce.getInfo();
                accountBtc = info.info.funds.btc;
              }
            writeTextFile(fileName,"waiting for order #1 to be filled, btc funds are currently "+accountBtc);
          }
        }

        buyStatus = buy(tradePairs, btcToCryptoTemp);
        while(buyStatus == false)
        {
          buyStatus = buy(tradePairs, btcToCryptoTemp);
        }
        if(buyStatus == true)
        {
         info = account.btce.getInfo();
         switch(tradePairs)
         {
          case "ltc/btc": accountCrypto = info.info.funds.ltc; break;
          case "nmc/btc": accountCrypto = info.info.funds.nmc; break;
          case "ppc/btc": accountCrypto = info.info.funds.ppc; break;
         }
         currentFunds = currentFunds / btcToCryptoTemp - 0.05; // amount of btc ordered
         while(accountCrypto < currentFunds)
         {
            Thread.sleep(1000);
            info = account.btce.getInfo();
            try{
              switch(tradePairs)
                {
                 case "ltc/btc": accountCrypto = info.info.funds.ltc; break;
                 case "nmc/btc": accountCrypto = info.info.funds.nmc; break;
                 case "ppc/btc": accountCrypto = info.info.funds.ppc; break;
                }
              }catch(NullPointerException e){
                info = account.btce.getInfo();
               switch(tradePairs)
                {
                 case "ltc/btc": accountCrypto = info.info.funds.ltc; break;
                 case "nmc/btc": accountCrypto = info.info.funds.nmc; break;
                 case "ppc/btc": accountCrypto = info.info.funds.ppc; break;
                }
              }
            writeTextFile(fileName,"waiting for order #2 to be filled, ltc funds are currently "+accountCrypto);
          }
        }
//final trade of the exploit for a profit 
        switch(tradePairs)
          {
          case "ltc/btc": sellStatus = sellLtc(cryptoToUsd); break;
          case "nmc/btc": sellStatus = sellNmc(cryptoToUsd); break;
          case "ppc/btc": sellStatus = sellPpc(cryptoToUsd); break;
          }
          while(sellStatus == false)
          {
           switch(tradePairs)
             {
             case "ltc/btc": sellStatus = sellLtc(cryptoToUsd); break;
             case "nmc/btc": sellStatus = sellNmc(cryptoToUsd); break;
             case "ppc/btc": sellStatus = sellPpc(cryptoToUsd); break;
             }
          }
      }
    }
    
    //checks for a exploit trade sequence in reverse order 
      writeTextFile(fileName,"checking for reverse profits in a reverse seqeuance of trades...");

      btcToCryptoTemp = account.btce.getTicker(btcPairs).sell;
      btcToCrypto = btcToCryptoTemp * btcToUsdSell;
      cryptoToUsd = account.btce.getTicker(pairs).buy;
      remainder = btcToCrypto - cryptoToUsd;
      
    writeTextFile(fileName,"Bitcoin/"+currency+" = "+btcToUsdSell);
    writeTextFile(fileName,"Bitcoin/"+currency+" temp =" +btcToCryptoTemp);
    writeTextFile(fileName,"Bitcoin/"+currency+" ="+ btcToCrypto);
    writeTextFile(fileName,currency+"/USD ="+cryptoToUsd);
    writeTextFile(fileName,"remainder ="+ remainder);

      // IMPORTANT IMPORTANT IMPORTANT IMPORTANT IMPORTANT NEXT 5 LINES BELOW!!!!!
      profit = (tradingFunds / btcToCrypto) * remainder;
      stringProfit = profit - addedValue;
      writeTextFile(fileName,"profit would be "+stringProfit);
      if(profit > addedValue)
      {
        switch(tradePairs)
        {
          case "ltc/btc": reversePairs = "ltc/usd"; break;
          case "nmc/btc": reversePairs = "nmc/usd"; break;
          case "ppc/btc": reversePairs = "ppc/usd"; break;
        }

        buyStatus = buy(reversePairs,cryptoToUsd);
        while(buyStatus == false || counter > 2)
        {
          buyStatus = buy(tradePairs,cryptoToUsd);
          counter++;
        }
        counter = 0;
        if(buyStatus == true)
        {
         info = account.btce.getInfo();
         switch(tradePairs)
         {
          case "ltc/btc": accountCrypto = info.info.funds.ltc; break;
          case "nmc/btc": accountCrypto = info.info.funds.nmc; break;
          case "ppc/btc": accountCrypto = info.info.funds.ppc; break;
         }
         currentFunds = currentFunds / btcToCryptoTemp - 0.05; // amount of btc ordered
         while(accountCrypto < currentFunds)
         {
            Thread.sleep(1000);
            info = account.btce.getInfo();
            try{
              switch(tradePairs)
                {
                 case "ltc/btc": accountCrypto = info.info.funds.ltc; break;
                 case "nmc/btc": accountCrypto = info.info.funds.nmc; break;
                 case "ppc/btc": accountCrypto = info.info.funds.ppc; break;
                }
              }catch(NullPointerException e){
                info = account.btce.getInfo();
               switch(tradePairs)
                {
                 case "ltc/btc": accountCrypto = info.info.funds.ltc; break;
                 case "nmc/btc": accountCrypto = info.info.funds.nmc; break;
                 case "ppc/btc": accountCrypto = info.info.funds.ppc; break;
                }
              }
            writeTextFile(fileName,"waiting for order #2 to be filled, ltc funds are currently "+accountCrypto);
          }
        }
        switch(tradePairs)
          {
          case "ltc/btc": sellStatus = sellLtcToBtc(btcToCryptoTemp); break;
          case "nmc/btc": sellStatus = sellNmcToBtc(btcToCryptoTemp); break;
          case "ppc/btc": sellStatus = sellPpcToBtc(btcToCryptoTemp); break;
          }
        while(sellStatus == false)
        {
          switch(tradePairs)
            {
            case "ltc/btc": sellStatus = sellLtcToBtc(btcToCryptoTemp); break;
            case "nmc/btc": sellStatus = sellNmcToBtc(btcToCryptoTemp); break;
            case "ppc/btc": sellStatus = sellPpcToBtc(btcToCryptoTemp); break;
            } 
        }
        if(sellStatus == true)
        {
         info = account.btce.getInfo();
         switch(tradePairs)
         {
          case "ltc/btc": accountCrypto = info.info.funds.ltc; break;
          case "nmc/btc": accountCrypto = info.info.funds.nmc; break;
          case "ppc/btc": accountCrypto = info.info.funds.ppc; break;
         }
         currentFunds = currentFunds * btcToCryptoTemp - 0.05; // amount of btc ordered
         while(accountCrypto < currentFunds)
         {
            Thread.sleep(1000);
            info = account.btce.getInfo();
            try{
              switch(tradePairs)
                {
                 case "ltc/btc": accountCrypto = info.info.funds.ltc; break;
                 case "nmc/btc": accountCrypto = info.info.funds.nmc; break;
                 case "ppc/btc": accountCrypto = info.info.funds.ppc; break;
                }
              }catch(NullPointerException e){
                info = account.btce.getInfo();
               switch(tradePairs)
                {
                 case "ltc/btc": accountCrypto = info.info.funds.ltc; break;
                 case "nmc/btc": accountCrypto = info.info.funds.nmc; break;
                 case "ppc/btc": accountCrypto = info.info.funds.ppc; break;
                }
              }
            writeTextFile(fileName,"waiting for order #2 to be filled, ltc funds are currently "+accountCrypto);
          }
        }

        sellStatus = sellBtc(btcToUsdSell);
        while(sellStatus == false)
        {
          sellStatus = sellBtc(btcToUsdSell);
        }
    }
      
  }
}