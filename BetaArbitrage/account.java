/*
Project SIMBA, a crypto currency AI trading bot 
Copyright (C) 2014  Matthew Bennett 

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
*/
package BetaArbitrage;

import BetaArbitrage.BTCE.BTCEException;
import BetaArbitrage.BTCE.Info;
/**
The base class for the SIMBA ARBITRAGE bot, logs into the account with the user provided API keys
@key & @secret  trading keys for a btc-e account
*/
public class account
{
    static String key = ""; // public key to log in 
    static String secret = "";   // private key to log in 
    static long auth_request_limit = 1000;  // time between request 
    long auth_last_request = 0;     
    static long request_limit = 15000;
    long nonce = 0, last_nonce = 0; // log in nonce
    static String trade_type; // buy or sell  
    static BTCE btce; // instance of a actual btc-e.com acount 


    public account(String key, String secret) throws BTCEException
    {
      key = this.key;
      secret = this.secret;
      btce = new BTCE();
      key = "xxxxxxxx-xxxxxxxx-xxxxxxxx-xxxxxxxx-xxxxxxxx"; //enter public key here
      secret = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"; //enter private key here 
      btce.setAuthKeys(key, secret);
      btce.setAuthRequestLimit(auth_request_limit);
      btce.setRequestLimit(request_limit);
    }
}

