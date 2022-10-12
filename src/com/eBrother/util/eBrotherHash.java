package com.eBrother.util;

import java.util.Hashtable;

public class eBrotherHash
{
//    static Hashtable hsLog, hsSite, hsPath, hsFile, hsQuery, hsFileEx, hsRefSite, hsRefUrl;
    static Hashtable[] hsTable = new Hashtable[8];
    static {
        for (int i=0; i<8 ; i++)
            hsTable[i] = new Hashtable();
    }

    public static long getebrotherHash(String strValue, int index)
    {
        if ( hsTable[index].get(strValue) != null )
            return Long.parseLong((String)hsTable[index].get(strValue));
        else
            return 0L;
    }

    public static void setebrotherHash(String strValue, long longValue, int index)
    {
        hsTable[index].put( strValue, Long.toString(longValue) );
    }

    public static void printAll()
    {
        for ( int i = 0; i < 8 ; i++)
            System.out.println( hsTable[i].toString() );
    }
}
