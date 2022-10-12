package com.eBrother.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class Protector {

	static String m_cryptokey = "ebrother$2014";
	private static final String ALGORITHM = "AES";
	private static final int ITERATIONS = 2;
	private static final byte[] keyValue =  new byte[] { 'Q', 'z', 'J', 'R', 'm', 'Q', 'F', 'h', '3', '6', 'x', 'r', 'Z', 'Q', '2', 'l'};
	
	public static String encrypt(String value, String salt) throws Exception {
	
		Key key = generateKey();
		Cipher c = Cipher.getInstance(ALGORITHM);  
		c.init(Cipher.ENCRYPT_MODE, key);  
		String valueToEnc = null;
		String eValue = value;
		for (int i = 0; i < ITERATIONS; i++) {
			
			valueToEnc = salt + eValue;
			// System.out.println( "\n\n\n\n======EN==================\nORG:" +  valueToEnc + "\nBYTE:" + valueToEnc.getBytes());
			byte[] encValue = c.doFinal(valueToEnc.getBytes());
			
			String qq = new String ( encValue);
			// System.out.println( "ENC:" + qq + "\nBYTE:" + encValue.toString());
			eValue = new String (Base64.encode(encValue));
			// System.out.println( "B64:" + eValue + "\nBYTE:" + Base64.encode(encValue));
		}        
		return eValue;

	}
	
	public static String decrypt(String value, String salt) throws Exception {        

		Key key = generateKey();
		Cipher c = Cipher.getInstance(ALGORITHM);
		c.init(Cipher.DECRYPT_MODE, key);  
		String dValue = null;
		String valueToDecrypt = value.replaceAll ( "\n", "" );

		int kk = salt.length();
		
		String sztemp = "0agveWGTCIDcKLMJd1RMv/KLmOacUqxkCWt4CXSFSdzL6T1ufK7oYZ5xI4NcJDd1NiNbYV0VN9AK+FduvK73+jw1r6xFqMXpYJk6rd6CNRjFysENh/gv2FDh63IqWXuFQH/ABpup8oxmgRL8Nbrfu5ZtMQaRAZgRzggovUDVFRE=";
		/*
		System.out.println( "\n\n\n\n====CH================\nORG:" +  new String (Base64.decode(sztemp)));
		
		sztemp = sztemp.replaceAll ( "\n", "" );
		byte[] q1 = c.doFinal(Base64.decode(sztemp));
		System.out.println( "\nCH2: " + new String ( q1));
		
		String q2 = new String( q1 );
		String q3 = q2.substring( salt.length());
		System.out.println( "\nCH3: " + q2 + " \nCH4: " + q3);
		
		q3 = q3.replaceAll ( "\n", "" );
		byte[] qq2 = c.doFinal(Base64.decode( q3));
		
		System.out.println( "\nCH5: " + new String (Base64.decode( q3)) + " \nCH6: " + new String(qq2));
		
		String qq3 = new String(qq2).substring( salt.length());
		
		System.out.println( "\nCH7 : " + qq3 );
		*/
		
		byte[] decValue = null;
		byte[] decordedValue;
		
		for (int i = 0; i < ITERATIONS; i++) {
			
			sztemp = valueToDecrypt.replaceAll ( "\n", "" );
			decordedValue = Base64.decode(sztemp);
			decValue = c.doFinal(decordedValue);
			dValue = new String(decValue).substring( kk );
			valueToDecrypt = dValue;
		}        
		
		// System.out.println( "qqq -> "  + new String ( decValue, "EUC-KR"));
		
		return dValue;
	
	}

	
	public static String decrypt(String value, String salt, String enc ) throws Exception {        

		Key key = generateKey();
		Cipher c = Cipher.getInstance(ALGORITHM);
		c.init(Cipher.DECRYPT_MODE, key);  
		String dValue = null;
		String valueToDecrypt = value.replaceAll ( "\n", "" );

		int kk = salt.length();
		
		String sztemp = "0agveWGTCIDcKLMJd1RMv/KLmOacUqxkCWt4CXSFSdzL6T1ufK7oYZ5xI4NcJDd1NiNbYV0VN9AK+FduvK73+jw1r6xFqMXpYJk6rd6CNRjFysENh/gv2FDh63IqWXuFQH/ABpup8oxmgRL8Nbrfu5ZtMQaRAZgRzggovUDVFRE=";
		/*
		System.out.println( "\n\n\n\n====CH================\nORG:" +  new String (Base64.decode(sztemp)));
		
		sztemp = sztemp.replaceAll ( "\n", "" );
		byte[] q1 = c.doFinal(Base64.decode(sztemp));
		System.out.println( "\nCH2: " + new String ( q1));
		
		String q2 = new String( q1 );
		String q3 = q2.substring( salt.length());
		System.out.println( "\nCH3: " + q2 + " \nCH4: " + q3);
		
		q3 = q3.replaceAll ( "\n", "" );
		byte[] qq2 = c.doFinal(Base64.decode( q3));
		
		System.out.println( "\nCH5: " + new String (Base64.decode( q3)) + " \nCH6: " + new String(qq2));
		
		String qq3 = new String(qq2).substring( salt.length());
		
		System.out.println( "\nCH7 : " + qq3 );
		*/
		
		try {
			
			byte[] decValue = null;
			byte[] decordedValue;
			
			for (int i = 0; i < ITERATIONS; i++) {
				
				// 이유는 모르겠으나, BUG가 있다.
				sztemp = valueToDecrypt.replaceAll ( "\n", "" );
				
				decordedValue = Base64.decode(sztemp);
				decValue = c.doFinal(decordedValue);
				dValue = new String(decValue).substring( kk );
				valueToDecrypt = dValue;
			}        
			
			dValue = new String ( decValue, "EUC-KR").substring(kk);
		}
		catch ( Exception e ) {
			dValue = value;
		}
		
		return dValue;
	
	}
	
	private static Key generateKey() throws Exception {
		
		Key key = new SecretKeySpec(keyValue, ALGORITHM);
		// SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
		// key = keyFactory.generateSecret(new DESKeySpec(keyValue));
		return key;
	}
	
    public static void main(String[] args)  {

    	String szopt = null;
    	String szdata = null;
    	if ( args.length < 3 ) {
    		
    		System.out.println ( "Usage : [en/de] key value");
    		
    		System.exit (1);
    		
    	}

    	szopt = args[0];
		m_cryptokey = args[1];
		szdata = args[2];
		
    	try {
    		
    		if ( szopt.equals("en")) {
    			System.out.print ( encrypt ( szdata, m_cryptokey )); 
    		}
    		// System.out.println ( encrypt ( "/&UDM=sdfsdf&kr=xvxcv", "ebrotherpilms"));
    		else {
    			System.out.print ( decrypt ( szdata, m_cryptokey ));
    		}
    	}
    	catch ( Exception e ) {
    		e.printStackTrace();
    	}

    }	
	
}
