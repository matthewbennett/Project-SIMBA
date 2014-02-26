/*
 * This is the package for the BTCE API.  By design, this will contain a single class with the goal of simplifying dependencies and making integration easy.
 * Currently the only dependency is the Google Gson library for JSON serialization <a href="http://google-gson.googlecode.com/">http://google-gson.googlecode.com/</a>.
 * <p>
 * All methods and data structures are intended to match the return results from the API as closely as possible with the intent that the calls can be made almost
 * exactly as documented by the BTC-E API documentation <a href="https://btc-e.com/api/documentation">https://btc-e.com/api/documentation</a>.
 * <p>
 * Also note that this class does not use the Spring Framework, Jave EE, or any other architectural framework with the sole goal of being as flexible as possible.
 * <p>
 * 
 * @author Bryan Waters <bryanw@abwaters.com>
 *
 */
package BetaArbitrage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * This is the only class required to connect to the BTC-E bitcoin exchange.  This class allows you to execute API calls on your account to obtain information and create and cancel trades.
 * <p>
 * The BTC-E API is documented at the following location <a href="https://btc-e.com/api/documentation">https://btc-e.com/api/documentation</a>.
 * <p>
 * <pre>
 * BTCE btce = new BTCE() ;
 * btce.setAuthKeys(btce_key,btce_secret) ;
 * Info info = btce.getInfo() ;
 * System.out.println(info.toString()) ; 
 * </pre>
 * 
 */
public class BTCE {
	
	// https://btc-e.com/api/2/btc_usd/ticker
	// https://btc-e.com/api/2/btc_usd/trades
	// https://btc-e.com/api/2/btc_usd/depth  TODO: implement depth call...

	private static final String USER_AGENT = "Mozilla/5.0 (compatible; BTCE-API/1.0; MSIE 6.0 compatible; +https://github.com/abwaters/btce-api)" ;
	private static final String TICKER_TRADES_URL = "https://btc-e.com/api/2/" ;
	private static final String API_URL = "https://btc-e.com/tapi" ;
	
	private static long auth_last_request = 0 ;
	private static long auth_request_limit = 1000 ;	// request limit in milliseconds
	private static long last_request = 0 ;
	private static long request_limit = 15000 ;	// request limit in milliseconds for non-auth calls...defaults to 15 seconds
	private static long nonce = 0, last_nonce = 0 ;
	
	private boolean initialized = false;
	private String secret, key ;
	private Mac mac ;
	private Gson gson ;

	/**
	 * Constructor
	 */
	public BTCE() {
		GsonBuilder gson_builder = new GsonBuilder();
		gson_builder.registerTypeAdapter(TransactionHistoryReturn.class, new TransactionHistoryReturnDeserializer());
		gson_builder.registerTypeAdapter(TradeHistoryReturn.class, new TradeHistoryReturnDeserializer());
		gson_builder.registerTypeAdapter(OrderListReturn.class, new OrderListReturnDeserializer());
		gson = gson_builder.create() ;
		if( nonce == 0 ) nonce = System.currentTimeMillis()/1000 ; 
	}

	/**
	 * Returns the account information. 
	 * 
	 * @return the account info.
	 */
	public Info getInfo() throws BTCEException {
		return gson.fromJson(authrequest("getInfo",null),Info.class) ;
	}
	
	/**
	 * Returns the transaction history for the account.
	 * 
	 * @return the transaction history.
	 */
	public TransactionHistory getTransactionHistory() throws BTCEException {
		return getTransactionHistory(0,0,0,0,null,0,0) ;
	}
	
	/**
	 * Returns the transaction history for the account. 
	 * <p>
	 * All arguments can be either a 0, a null or an empty string "" which will result in the argument being omitted from the API call.
	 *
	 * @param from the number of the starting transaction to include in the returned results. 
	 * @param count the number of transactions to return.
	 * @param from_id the starting id of the transaction to include in the return results.
	 * @param end_id the last id of the transaction to include in the return results.
	 * @param order the order to sort the returned results.  Can be "ASC" or "DESC".  The default value is "DESC" if this value is null or an empty string.
	 * @param since starting time frame to include results in unix time stamp format. 
	 * @param end ending time frame to include results in unix time stamp format. 
	 * @return the transaction history.
	 */
	public TransactionHistory getTransactionHistory(int from,int count,int from_id,int end_id,String order,long since,long end) throws BTCEException {
		Map<String,String> args = new HashMap<String,String>() ;		
		if( from > 0 ) args.put("from", Integer.toString(from)) ;
		if( count > 0 ) args.put("count", Integer.toString(count)) ;
		if( from_id > 0 ) args.put("from_id", Integer.toString(from_id)) ;
		if( end_id > 0 ) args.put("end_id", Integer.toString(end_id)) ;
		if( order != null && order.length() > 0 ) args.put("order", order) ;
		if( since > 0 ) args.put("since", Long.toString(since)) ;
		if( end > 0 ) args.put("end", Long.toString(end)) ;
		return gson.fromJson(authrequest("TransHistory",args),TransactionHistory.class) ;
	}

	/**
	 * Returns the trade history for the account.
	 * @return the trade history.
	 */
	public TradeHistory getTradeHistory() throws BTCEException {
		return getTradeHistory(0,0,0,0,null,0,0,null) ;
	}

	/**
	 * Returns the trade history for the account.
	 * <p>
	 * All arguments can be either a 0, a null or an empty string "" which will result in the argument being omitted from the API call.
	 *
	 * @param from the number of the starting transaction to include in the returned results. 
	 * @param count the number of transactions to return.
	 * @param from_id the starting id of the transaction to include in the return results.
	 * @param end_id the last id of the transaction to include in the return results.
	 * @param order the order to sort the returned results.  Can be "ASC" or "DESC".  The default value is "DESC" if this value is null or an empty string.
	 * @param since starting time frame to include results in unix time stamp format. 
	 * @param end ending time frame to include results in unix time stamp format. 
	 * @param pair the pair to include in the trade history. 
	 * @return the trade history.
	 */
	public TradeHistory getTradeHistory(int from,int count,int from_id,int end_id,String order,long since,long end,String pair) throws BTCEException {
		Map<String,String> args = new HashMap<String,String>() ;		
		if( from > 0 ) args.put("from", Integer.toString(from)) ;
		if( count > 0 ) args.put("count", Integer.toString(count)) ;
		if( from_id > 0 ) args.put("from_id", Integer.toString(from_id)) ;
		if( end_id > 0 ) args.put("end_id", Integer.toString(end_id)) ;
		if( order != null && order.length() > 0 ) args.put("order", order) ;
		if( since > 0 ) args.put("since", Long.toString(since)) ;
		if( end > 0 ) args.put("end", Long.toString(end)) ;
		if( pair != null && pair.length() > 0 ) args.put("pair", pair) ;
		return gson.fromJson(authrequest("TradeHistory",args),TradeHistory.class) ;
	}
	
	/**
	 * Returns the order list for the account.
	 * 
	 * @return the order list.
	 */
	public OrderList getOrderList() throws BTCEException {
		return getOrderList(0,0,0,0,null,0,0,null,0) ;
	}
	
	/**
	 * Returns the order list for the account.
	 * <p>
	 * All arguments can be either a 0, a null or an empty string "" which will result in the argument being omitted from the API call.
	 * 
	 * @param from the number of the starting transaction to include in the returned results. 
	 * @param count the number of transactions to return.
	 * @param from_id the starting id of the transaction to include in the return results.
	 * @param end_id the last id of the transaction to include in the return results.
	 * @param order the order to sort the returned results.  Can be "ASC" or "DESC".  The default value is "DESC" if this value is null or an empty string.
	 * @param since starting time frame to include results in unix time stamp format. 
	 * @param end ending time frame to include results in unix time stamp format. 
	 * @param pair the pair to include in the order list.
	 * @param active include only active orders in the order list. 
	 */
	public OrderList getOrderList(int from,int count,int from_id,int end_id,String order,long since,long end,String pair,int active) throws BTCEException {
		Map<String,String> args = new HashMap<String,String>() ;		
		if( from > 0 ) args.put("from", Integer.toString(from)) ;
		if( count > 0 ) args.put("count", Integer.toString(count)) ;
		if( from_id > 0 ) args.put("from_id", Integer.toString(from_id)) ;
		if( end_id > 0 ) args.put("end_id", Integer.toString(end_id)) ;
		if( order != null && order.length() > 0 ) args.put("order", order) ;
		if( since > 0 ) args.put("since", Long.toString(since)) ;
		if( end > 0 ) args.put("end", Long.toString(end)) ;
		if( pair != null && pair.length() > 0 ) args.put("pair", pair) ;
		if( active > 0 ) args.put("active", Long.toString(active)) ;
		return gson.fromJson(authrequest("OrderList",args),OrderList.class) ;
	}
	
	/**
	 * Execute a trade for the specified currency pair.
	 * 
	 * @param pair the currency pair to trade.  This is specified as <curr1>_<curr2>, example: "BTC_USD"
	 * @param type the type of transaction.  Can be "buy" or "sell".
	 * @param rate the rate to pay for the transaction in <curr2>.
	 * @param amount the quantity of <curr1> to buy.
	 * @return the trade results.
	 */
	public Trade trade(String pair,String type,double rate,double amount) throws BTCEException {
		Map<String,String> args = new HashMap<String,String>() ;		
		args.put("pair", pair) ;
		args.put("type", type) ;
		args.put("rate", Double.toString(rate)) ;
		args.put("amount", Double.toString(amount)) ;
		return gson.fromJson(authrequest("Trade",args),Trade.class) ;
	}
	
	/**
	 * Cancel the specified order.
	 * 
	 * @param order_id the id of the order to cancel.
	 */
	public CancelOrder cancelOrder(int order_id) throws BTCEException {
		Map<String,String> args = new HashMap<String,String>() ;		
		args.put("order_id", Integer.toString(order_id)) ;
		return gson.fromJson(authrequest("CancelOrder",args),CancelOrder.class) ;
	}
	
	/**
	 * Limits how frequently calls to the open API for trade history and tickers can be made.  
	 * If calls are attempted more frequently, the thread making the call is put to sleep for 
	 * the duration of the time left before the limit is reached.
	 * <p>
	 * This is set to 15 second based on a forum post indicating that support specified this limit.
	 * <a href="https://bitcointalk.org/index.php?topic=127553.msg1764391#msg1764391">Forum post can be read here</a>. 
	 *  
	 * @param request_limit call limit in milliseconds
	 */
	public void setRequestLimit(long request_limit) {
		BTCE.request_limit = request_limit ; 
	}
	
	/**
	 * Limits how frequently calls to the authenticated BTCE can be made.  If calls are attempted 
	 * more frequently, the thread making the call is put to sleep for the duration of the time 
	 * left before the limit is reached.
	 * <p>
	 * This is set to 1 second on the assumption that authenticated (calls using keys) are made infrequently and should receive priority.
	 * 
	 * @param auth_request_limit call limit in milliseconds
	 */
	public void setAuthRequestLimit(long auth_request_limit) {
		BTCE.auth_request_limit = auth_request_limit ; 
	}
	
	/**
	 * Sets the account API keys to use for calling methods that require access to a BTC-E account.
	 * 
	 * @param key the key obtained from Profile->API Keys in your BTC-E account.
	 * @param secret the secret obtained from Profile->API Keys in your BTC-E account.
	 */
	public void setAuthKeys(String key,String secret) throws BTCEException {
		this.key = key ;
		this.secret = secret ;
		SecretKeySpec keyspec = null ;
		try {
			keyspec = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA512") ;
		} catch (UnsupportedEncodingException uee) {
			throw new BTCEException("HMAC-SHA512 doesn't seem to be installed",uee) ;
		}

		try {
			mac = Mac.getInstance("HmacSHA512") ;
		} catch (NoSuchAlgorithmException nsae) {
			throw new BTCEException("HMAC-SHA512 doesn't seem to be installed",nsae) ;
		}

		try {
			mac.init(keyspec) ;
		} catch (InvalidKeyException ike) {
			throw new BTCEException("Invalid key for signing request",ike) ;
		}
		initialized = true ;
	}
	
	/**
	 * Get the current ticker for the specified currency pair.
	 * <p>
	 * Since this call doesn't use authorization, the request
	 * limit is typically set higher (10-15 seconds) to avoid abuse. 
	 * 
	 * @param pair
	 * @return a Ticker object for the specified pair.
	 * @throws BTCEException
	 */
	public Ticker getTicker(String pair) throws BTCEException {
		TickerWrapper tw = gson.fromJson(request(TICKER_TRADES_URL+pair+"/ticker"),TickerWrapper.class) ; 
		return tw.ticker ;
	}
	
	/**
	 * Get the current rade book for the specified currency pair.
	 * <p>
	 * Since this call doesn't use authorization, the request
	 * limit is typically set higher (10-15 seconds) to avoid abuse. 
	 * 
	 * @param pair
	 * @return an array of TradeDetail objects
	 * @throws BTCEException
	 */
	public TradesDetail[] getTrades(String pair) throws BTCEException {
		return gson.fromJson(request(TICKER_TRADES_URL+pair+"/trades"),TradesDetail[].class) ;
	}

	private final void preCall() {
		while(nonce==last_nonce) nonce++ ;
		long elapsed = System.currentTimeMillis()-last_request ;
		if( elapsed < request_limit ) {
			try {
				Thread.currentThread().sleep(request_limit-elapsed) ;
			} catch (InterruptedException e) {
				
			}
		}
		last_request = System.currentTimeMillis() ;
	}
	
	private final String request(String urlstr) throws BTCEException {
		
		// handle precall logic
		preCall() ;

		// create connection
		URLConnection conn = null ;
		StringBuffer response = new StringBuffer() ;
		try {
			URL url = new URL(urlstr);
			conn = url.openConnection() ;
			conn.setUseCaches(false) ;
			conn.setRequestProperty("User-Agent",USER_AGENT) ;
		
			// read response
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = null ;
			while ((line = in.readLine()) != null)
				response.append(line) ;
			in.close() ;
		} catch (MalformedURLException e) {
			throw new BTCEException("Internal error.",e) ;
		} catch (IOException e) {
			throw new BTCEException("Error connecting to BTC-E.",e) ;
		}
		return response.toString() ;
	}
	
	private final void preAuth() {
		while(nonce==last_nonce) nonce++ ;
		long elapsed = System.currentTimeMillis()-auth_last_request ;
		if( elapsed < auth_request_limit ) {
			try {
				Thread.currentThread().sleep(auth_request_limit-elapsed) ;
			} catch (InterruptedException e) {
				
			}
		}
		auth_last_request = System.currentTimeMillis() ;
	}
	
	private final String authrequest(String method, Map<String,String> args) throws BTCEException {
		if( !initialized ) throw new BTCEException("BTCE not initialized.") ;
		
		// prep the call
		preAuth() ;

		// add method and nonce to args
		if (args == null) args = new HashMap<String,String>() ;
		args.put("method", method) ;
		args.put("nonce",Long.toString(nonce)) ;
		last_nonce = nonce ;
		
		// create url form encoded post data
		String postData = "" ;
		for (Iterator<String> iter = args.keySet().iterator(); iter.hasNext();) {
			String arg = iter.next() ;
			if (postData.length() > 0) postData += "&" ;
			postData += arg + "=" + URLEncoder.encode(args.get(arg)) ;
		}
		
		// create connection
		URLConnection conn = null ;
		StringBuffer response = new StringBuffer() ;
		try {
			URL url = new URL(API_URL);
			conn = url.openConnection() ;
			conn.setUseCaches(false) ;
			conn.setDoOutput(true) ;
			conn.setRequestProperty("Key",key) ;
			conn.setRequestProperty("Sign",toHex(mac.doFinal(postData.getBytes("UTF-8")))) ;
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded") ;
			conn.setRequestProperty("User-Agent",USER_AGENT) ;
		
			// write post data
			OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
			out.write(postData) ;
			out.close() ;
	
			// read response
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = null ;
			while ((line = in.readLine()) != null)
				response.append(line) ;
			in.close() ;
		} catch (MalformedURLException e) {
			throw new BTCEException("Internal error.",e) ;
		} catch (IOException e) {
			throw new BTCEException("Error connecting to BTC-E.",e) ;
		}
		return response.toString() ;
	}
	
	private String toHex(byte[] b) throws UnsupportedEncodingException {
	    return String.format("%040x", new BigInteger(1,b));
	}
	
	/**
	 * Displays the amounts of various currencies associated with an account or an order.
	 */
	public class Funds {
		public double usd ;
		public double btc ;
		public double ltc ;
		public double nmc ;
		public double rur ;
		public double eur ;
		public double nvc ;
		public double trc ;
		public double ppc ;
		public double ftc ;
		public double cnc ;

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[usd=" + usd + ", btc=" + btc + ", ltc=" + ltc + ", nmc="
					+ nmc + ", rur=" + rur + ", eur=" + eur + ", nvc=" + nvc
					+ ", trc=" + trc + ", ppc=" + ppc + ", ftc=" + ftc + ", cnc="
					+ cnc + "]";
		}
	}
	
	/**
	 * returned by the {@link #getInfo() getInfo} method. Note that due to a java name collision the JSON field return is named info.
	 */
	public static class Info extends Results {
		@SerializedName("return")
		public InfoReturn info ;

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Info [success=" + success + ", error=" + error + ", return=" + info + "]";
		}

	}	

	/**
	 * base class for all data structures returned by the API.
	 */
	public static class Results {
		/**
		 * can be either 0 or 1 depending on whether API was successful.
		 */
		public int success ;
		
		/**
		 * if success is 0 then this will contain the details of the error.
		 */
		public String error = "" ;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[success=" + success + ", error=" + error + "]";
		}
	}

	/**
	 * 
	 */
	public class InfoReturn {
		public Funds funds ;
		public Rights rights ;
		public int transaction_count ;
		public int open_orders ;
		public long server_time ;

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[funds=" + funds + ", rights=" + rights
					+ ", transaction_count=" + transaction_count + ", open_orders="
					+ open_orders + ", server_time=" + server_time + "]";
		}
	}

	/**
	 * 
	 */
	public class Rights {
		public int info, trade, withdraw ;

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[info=" + info + ", trade=" + trade + ", withdraw="
					+ withdraw + "]";
		}
	}

	/**
	 * returned by the {@link #getTransactionHistory() getTransactionHistory} method. Note that due to a java name collision the JSON field return is named info.
	 */
	public static class TransactionHistory extends Results {
		@SerializedName("return")
		public TransactionHistoryReturn info ;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "TransactionHistory [success=" + success + ", error=" + error + ", return=" + info + "]";
		}
	}

	/**
	 * 
	 */
	public class TransactionHistoryReturn {
		public TransactionHistoryOrder[] transactions ;

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[transactions="+ Arrays.toString(transactions) + "]";
		}
	}
	
	/**
	 * 
	 */
	public class TransactionHistoryOrder {
		public long trans_id ;
		public TransactionHistoryOrderDetails trans_details ;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[trans_id=" + trans_id + ", trans_details="+ trans_details + "]";
		}
	}
	
	/**
	 * 
	 */
	public class TransactionHistoryOrderDetails {
		public int type ;
		public double amount ;
		public String currency ;
		public String desc ;
		public int status ;
		public long timestamp ;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[type=" + type + ", amount="
					+ amount + ", currency=" + currency + ", desc=" + desc
					+ ", status=" + status + ", timestamp=" + timestamp + "]";
		}
	}

	/**
	 * 
	 */
	private class TransactionHistoryReturnDeserializer implements JsonDeserializer<TransactionHistoryReturn> {
		  public TransactionHistoryReturn deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			  TransactionHistoryReturn thr = new TransactionHistoryReturn() ;
			  List<TransactionHistoryOrder> transactions = new ArrayList<TransactionHistoryOrder>() ;
			  if( json.isJsonObject() ) {
				  JsonObject o = json.getAsJsonObject() ;
				  Iterator<Entry<String,JsonElement>> iter = o.entrySet().iterator() ;
				  while(iter.hasNext()) {
					  Entry<String,JsonElement> jsonOrder = iter.next();
					  TransactionHistoryOrder transaction = new TransactionHistoryOrder() ;
					  transaction.trans_id = Long.parseLong(jsonOrder.getKey()) ;
					  transaction.trans_details = context.deserialize(jsonOrder.getValue(),TransactionHistoryOrderDetails.class) ;
					  transactions.add(transaction) ;
				  }
			  }
			  thr.transactions = transactions.toArray(new TransactionHistoryOrder[0]) ;
			  return thr ;
		  }
	}
	
	/**
	 * returned by the {@link #getTradeHistory() getTradeHistory} method. Note that due to a java name collision the JSON field return is named info.
	 */
	public static class TradeHistory extends Results {
		@SerializedName("return")
		public TradeHistoryReturn info ;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "TradeHistory [success=" + success + ", error=" + error + ", return=" + info + "]";
		}

	}

	/**
	 * 
	 */
	public class TradeHistoryReturn {
		public TradeHistoryOrder[] trades ;

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[trades="+ Arrays.toString(trades) + "]";
		}
	}
	
	/**
	 * 
	 */
	public class TradeHistoryOrder {
		public long trans_id ;
		public TradeHistoryOrderDetails trade_details ;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[trans_id=" + trans_id + ", trade_details="+ trade_details + "]";
		}
	}
	
	/**
	 * 
	 */
	public class TradeHistoryOrderDetails {
		public String pair, type ;
		public double amount, rate ;
		public long order_id ;
		public int is_your_order ;
		public long timestamp ;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[pair=" + pair + ", type=" + type
					+ ", amount=" + amount + ", rate=" + rate + ", order_id="
					+ order_id + ", is_your_order=" + is_your_order
					+ ", timestamp=" + timestamp + "]";
		}
	}

	private class TradeHistoryReturnDeserializer implements JsonDeserializer<TradeHistoryReturn> {
		  public TradeHistoryReturn deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			  TradeHistoryReturn thr = new TradeHistoryReturn() ;
			  List<TradeHistoryOrder> trades = new ArrayList<TradeHistoryOrder>() ;
			  if( json.isJsonObject() ) {
				  JsonObject o = json.getAsJsonObject() ;
				  Iterator<Entry<String,JsonElement>> iter = o.entrySet().iterator() ;
				  while(iter.hasNext()) {
					  Entry<String,JsonElement> jsonOrder = iter.next();
					  TradeHistoryOrder trade = new TradeHistoryOrder() ;
					  trade.trans_id = Long.parseLong(jsonOrder.getKey()) ;
					  trade.trade_details = context.deserialize(jsonOrder.getValue(),TradeHistoryOrderDetails.class) ;
					  trades.add(trade) ;
				  }
			  }
			  thr.trades = trades.toArray(new TradeHistoryOrder[0]) ;
			  return thr ;
		  }
	}
	
	/**
	 * returned by the {@link #getOrderList() getOrderList} method. Note that due to a java name collision the JSON field return is named info.
	 */
	public static class OrderList extends Results {
		@SerializedName("return")
		public OrderListReturn info ;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			 return "OrderList [success=" + success + ", error=" + error + ", return=" + info + "]";			
		}
	}
	
	/**
	 * 
	 */
	public class OrderListReturn {
		public OrderListOrder[] orders ;

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[orders="+ Arrays.toString(orders) + "]";
		}
	}
	
	/**
	 * 
	 */
	public class OrderListOrder {
		public long order_id ;
		public OrderListOrderDetails order_details ;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[order_id=" + order_id + ", order_details="	+ order_details + "]";
		}
	}
	
	/**
	 * 
	 */
	public class OrderListOrderDetails {
		public String pair ;
		public String type ;
		public double amount ;
		public double rate ;
		public int status ;
		public long timestamp ;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[pair=" + pair + ", type=" + type
					+ ", amount=" + amount + ", rate=" + rate + ", status="
					+ status + ", timestamp=" + timestamp + "]";
		}
	}
	
	private class OrderListReturnDeserializer implements JsonDeserializer<OrderListReturn> {
		  public OrderListReturn deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			  OrderListReturn olr = new OrderListReturn() ;
			  List<OrderListOrder> orders = new ArrayList<OrderListOrder>() ;
			  if( json.isJsonObject() ) {
				  JsonObject o = json.getAsJsonObject() ;
				  Iterator<Entry<String,JsonElement>> iter = o.entrySet().iterator() ;
				  while(iter.hasNext()) {
					  Entry<String,JsonElement> jsonOrder = iter.next();
					  OrderListOrder order = new OrderListOrder() ;
					  order.order_id = Long.parseLong(jsonOrder.getKey()) ;
					  order.order_details = context.deserialize(jsonOrder.getValue(),OrderListOrderDetails.class) ;
					  orders.add(order) ;
				  }
			  }
			  olr.orders = orders.toArray(new OrderListOrder[0]) ;
			  return olr ;
		  }
	}
	
	/**
	 * returned by the {@link #trade(String,String,double,double) trade} method. Note that due to a java name collision the JSON field return is named info.
	 */
	public static class Trade extends Results {
		@SerializedName("return")
		public TradeReturn info ;

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Trade [success=" + success + ", error=" + error + ", return=" + info + "]";
		}
	}
	
	/**
	 * 
	 */
	public class TradeReturn {
		public double received ;
		public double remains ;
		public long order_id ;
		public Funds funds ;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[received=" + received + ", remains=" + remains	+ ", order_id=" + order_id + ", funds=" + funds + "]";
		}
	}
	
	/**
	 * returned by the {@link #cancelOrder(int) cancelOrder} method. Note that due to a java name collision the JSON field return is named info.
	 */
	public static class CancelOrder extends Results {
		
		@SerializedName("return")
		public CancelOrderReturn info ;

		/*
		 * (non-Javadoc)
		 * @see com.abwaters.btce.BTCE.Results#toString()
		 */
		@Override
		public String toString() {
			return "CancelOrder [success=" + success + ", error=" + error + ", return=" + info + "]";
		}
	}
	
	/**
	 * 
	 */
	public class CancelOrderReturn {
		public long order_id ;
		public Funds funds ;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "[order_id=" + order_id + ", funds="
					+ funds + "]";
		}
	}

	/**
	 * Currency pair helper class.
	 */
	public static final class Pairs {
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
		public static final String FTC_BTC = "ftc_btc" ;
		public static final String USD_RUR = "usd_rur" ;
		public static final String EUR_USD = "eur_usd" ;
		public static final String PPC_USD = "ppc_usd" ;
	}

	/**
	 * Trade type helper class.
	 *
	 */
	public static class TradeType {
		public static final String BUY = "buy" ;
		public static final String SELL = "sell" ;
	}
	
	/**
	 * 
	 */
	public static class TransactionType {
		public static final int DEPOSIT = 0 ;
		public static final int WITHDRAW = 1 ;	// ?
		public static final int ORDER_CANCEL = 4 ;
		public static final int ORDER_SELL = 5 ;
	}
	
	/**
	 * 
	 */
	public static class OrderStatus {
		public static final int ACTIVE = 0 ;
		public static final int FILLED = 1 ;
		public static final int CANCELED = 2 ;
		public static final int PARTIALLY_FILLED = 3 ;
	}

	/**
	 *
	 */
	private class TickerWrapper {
		private Ticker ticker ;
	}
	
	/**
	 * 
	 */
	public static class Ticker {
		public double high ;
		public double low ;
		public double avg ;
		public double vol ;
		public double vol_cur ;
		public double last ;
		public double buy ;
		public double sell ;
		public long updated ;
		public long server_time ;
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "Ticker [high=" + high + ", low=" + low + ", avg=" + avg
					+ ", vol=" + vol + ", vol_cur=" + vol_cur + ", last="
					+ last + ", buy=" + buy + ", sell=" + sell + ", updated="
					+ updated + ", server_time=" + server_time + "]";
		}
	}

	/**
	 * 
	 */
	public static class TradesDetail {
		public long date ;
		public double price ;
		public double amount ;
		public long tid ;
		public String price_currency ;
		public String item ;
		public String trade_type ;

		@Override
		public String toString() {
			return "TradesDetail [date=" + date + ", price=" + price
					+ ", amount=" + amount + ", tid=" + tid
					+ ", price_currency=" + price_currency + ", item=" + item
					+ ", trade_type=" + trade_type + "]";
		}
	}
	
	/**
	 * An exception class specifically for the BTCE API.  The goal here is to provide a specific exception class for this API while
	 * not losing any of the details of the inner exceptions.
	 * <p>  
	 * This class is just a wrapper for the Exception class.
	 */
	public class BTCEException extends Exception {
		
		private static final long serialVersionUID = 1L;
		
		public BTCEException(String msg) {
			super(msg) ;
		}
		
		public BTCEException(String msg, Throwable e) {
			super(msg,e) ;
		}
	}
}