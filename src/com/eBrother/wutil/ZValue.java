package com.eBrother.wutil;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Array;
import java.util.*;

public class ZValue extends Hashtable implements java.io.Serializable
{
    /** use serialVersionUID from JDK 1.0.2 for interoperability */
    private static final long serialVersionUID = 5849794470754667710L;

	protected Log log = LogFactory.getLog(this.getClass());
	
    protected String name = null;

    public ZValue()
    {
    }

	public ZValue(String name) {
		this.name = name;
	}

    public Object get(Object s)
    {   
    	Object obj = super.get(s);
    	if(obj == null)
    		obj = "";
    	return obj;
    }

    public String getValue(String s)
    {
        return getString(s);
    }
    
    

    public boolean getBoolean(String s)
    {
        String s1 = getString(s);
        boolean flag = false;
        try
        {
            flag = (new Boolean(s1)).booleanValue();
        }
        catch(Exception exception) { }

        return flag;
    }

    public double getDouble(String s)
    {
        String s1 = removeComma(getString(s));
        if(s1.equals(""))
            return 0.0D;
        double d = 0.0D;
        try
        {
            d = Double.valueOf(s1).doubleValue();
        }
        catch(Exception exception)
        {
            d = 0.0D;
        }

        return d;
    }

    public float getFloat(String s)
    {
        return (float)getDouble(s);
    }

    public int getInt(String s)
    {
        double d = getDouble(s);
        return (int)d;
    }
    
    public int getInt(String s, int initVal)
    {
    	int ret = getInt(s);
    	if(ret == 0)
    		ret = initVal;
    	return ret;
    }

    public long getLong(String s)
    {
        String s1 = removeComma(getString(s));
        if(s1.equals(""))
            return 0L;
        long l = 0L;
        try
        {
            l = Long.valueOf(s1).longValue();
        }
        catch(Exception exception)
        {
            l = 0L;
        }

        return l;
    }

    public String getString(String s)
    {
        String s1 = null;
        try
        {
            Object obj = super.get(s);
            Class class1 = obj.getClass();

            if(obj == null)
                s1 = "";
            else if(class1.isArray())
            {
                int i = Array.getLength(obj);
                if(i == 0)
                {
                    s1 = "";
                }
                else
                {
                    Object obj1 = Array.get(obj, 0);
                    if(obj1 == null)
                        s1 = "";
                    else
                        s1 = obj1.toString();
                }
            }
            else
            {
                s1 = obj.toString();
            }
        }
        catch(Exception exception)
        {
            s1 = "";
        }

        return s1.trim();
    }

    public Vector getVector(String s)
    {
        Vector vector = new Vector();
        try
        {
            Object obj = super.get(s);
            Class class1 = obj.getClass();
            if(obj != null)
            {
                if(class1.isArray())
                {
                    int i = Array.getLength(obj);
                    if(i != 0)
                    {
                        for(int j = 0; j < i; j++)
                        {
                            Object obj1 = Array.get(obj, j);
                            if(obj1 == null)
                                vector.addElement("");
                            else
                                vector.addElement(obj1.toString());
                        }
                    }
                }
                else
                {
                    vector.addElement(obj.toString());
                }
            }
        }
        catch(Exception exception) { }

        return vector;
    }

    public java.util.ArrayList getArrayList(String s)
    {
        java.util.ArrayList al = new java.util.ArrayList();
        try
        {
            Object obj = super.get(s);
            Class class1 = obj.getClass();
            if(obj != null)
            {
                if(class1.isArray())
                {
                    int i = Array.getLength(obj);
                    if(i != 0)
                    {
                        for(int j = 0; j < i; j++)
                        {
                            Object obj1 = Array.get(obj, j);
                            if(obj1 == null)
                                al.add("");
                            else
                                al.add(obj1.toString());
                        }
                    }
                }
                else
                {
                    al.add(obj.toString());
                }
            }
        }
        catch(Exception exception) { }

        return al;
    }

    public void put(String s, String s1)
    {
    	super.put(s, s1);
    }

    public void put(String s, java.util.ArrayList al)
    {
        String as[] = new String[al.size()];
        for(int i = 0; i < al.size(); i++) {
            if( al.get(i) != null )
                as[i] = (String)al.get(i);
            else
                as[i] = "";
        }

        super.put(s, as);
    }

    public void putVector(String s, Vector vector)
    {
        String as[] = new String[vector.size()];
        for(int i = 0; i < vector.size(); i++)
            as[i] = vector.get(i) != null ? vector.get(i).toString() : "";

        put(s, as);
    }

    protected static String removeComma(String s)
    {
        if(s == null)
            return null;
        if(s.indexOf(",") != -1)
        {
            StringBuffer stringbuffer = new StringBuffer();
            for(int i = 0; i < s.length(); i++)
            {
                char c = s.charAt(i);
                if(c != ',')
                    stringbuffer.append(c);
            }

            return stringbuffer.toString();
        }
        else
        {
          return s;
        }
    }

    public void setValue(String s, String s1)
    {
        super.put(s, s1);
    }

    public void setVector(String s, Vector vector)
    {
        String as[] = new String[vector.size()];
        for(int i = 0; i < vector.size(); i++)
            as[i] = vector.get(i) != null ? vector.get(i).toString() : "";

        put(s, as);
    }

    public void set(String s, String s1)
    {
        super.put(s, s1);
    }

    public void set(String s, java.util.ArrayList al)
    {
        String as[] = new String[al.size()];
        for(int i = 0; i < al.size(); i++){
            if(al.get(i) != null)
                as[i] = (String)al.get(i);
            else
                as[i] = "";
        }
        put(s, as);
    }


	public String getJSONValue() {
		
		org.json.simple.JSONObject jObj = new org.json.simple.JSONObject( );
		
		Enumeration<String> ee = this.keys();
		
		// for (Enumeration<String> ee = zvl.keys(); ee.hasMoreElements();) {

		while(ee.hasMoreElements()){
			String name = (String)ee.nextElement();
			Object obj = get(name);
			String value = null;
			if(obj instanceof String[])
			{
				obj = getArrayList(name);
				value = ZValue.arrayListToDelimitedString((ArrayList)obj, "|");
			}
			else
			{
				value = getString(name);
			}
			jObj.put(name, value);
		}
		
	    return(jObj.toString( ));
	}
	
	public static String getArrayJSONValue(List<ZValue> zvalueList)
	{
		HashMap<String, ArrayList<String>> container = new HashMap<String, ArrayList<String>>();
		for(ZValue zvl : zvalueList) {
						
			for (Enumeration<String> ee = zvl.keys(); ee.hasMoreElements();) {
				
				String name = ee.nextElement ();
				String value = zvl.getString(name);
		    	System.out.println(name + " : " + name + " value : " + value);
				ArrayList<String> valueList = container.get(name);
				if(valueList == null) {
					valueList = new ArrayList<String>();
					valueList.add(value);
					container.put(name, valueList);
				}
				else {
					valueList.add(value);
				}			
			}
		}	

		org.json.simple.JSONObject jObj = new org.json.simple.JSONObject( );
		Iterator it = container.keySet().iterator();
		while(it.hasNext()){
			String name = (String)it.next();
			String value = ZValue.arrayListToDelimitedString(container.get(name), "|");
			jObj.put(name, value);
		}
	    return(jObj.toString( ));
	}
	
	public static String getListToJSONValue(List<ZValue> zvalueList){
		String str = "";
		
		if (zvalueList!=null && zvalueList.size()>0){
			for(ZValue zvl : zvalueList) {

				Enumeration<String> ee = zvl.keys();
				
				// while(it.hasNext()){
				while(ee.hasMoreElements()){
					String name = (String)ee.nextElement();
					// Iterator it = zvl.keyList().iterator();
					String temp = "";
					String value = zvl.getString(name);
					if (!temp.equals("")) temp += ", ";
						temp += "\"" + name + "\":\"" + value +"\""; //URLEncoder.encode(value)

					if (!str.equals("")) str += ", ";
					str += "{" + temp + "}";
				}
			}
			System.out.println("[" + str+ "]");
		}
		return "[" + str+ "]";
	}
	
	public String getArrayInQueryString(String key){
		Object obj = this.get(key).getClass();
		String returnStr = "";
		if (obj==null){
			return "";
		}
		
		if (obj.equals(String.class)) {
			String[] temp = null;
		/*	if (get(CamelUtil.convert2CamelCase((String)key))!=null){
				temp = (String[])get(CamelUtil.convert2CamelCase((String)key));
				if (temp.length>0){
					for (int i=0; i<temp.length;i++){
						if (!returnStr.equals("")) {returnStr+="','";}
						returnStr+=temp[i];
					}
				}
			}*/
		}
		
		if (returnStr.length()>0){
			returnStr = "'" + returnStr + "'";
		}
		
		return returnStr;
	}
	

    private static String arrayListToDelimitedString(ArrayList arr, String delim) {
        if (arr == null)
            return "null";
        else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < arr.size(); i++) {
                if (i > 0)
                    sb.append(delim);
                sb.append(arr.get(i));
            }
            return sb.toString();
        }
    }

    @Override
    public String toString()
    {
    	StringBuffer result = new StringBuffer();
    	result.append("{");
    	Iterator it = keySet().iterator();
    	while(it.hasNext())
    	{
    		String key = (String)it.next();
    		String value = getValue(key);
    		result.append("("+key+","+value+")\n");
    	}
    	result.append("}");
    	return result.toString();
    }
    
    public static void main(String[] args)throws Exception{
    	/*
    	ZValue zvl1 = new ZValue();
    	zvl1.put("k1", "v11");
    	zvl1.put("k2", "v12");
    	zvl1.put("k3", "v12");

    	ZValue zvl2 = new ZValue();
    	zvl2.put("k1", "v21");
    	zvl2.put("k2", "v22");
    	zvl2.put("k3", "v22");

    	ZValue zvl3 = new ZValue();
    	zvl3.put("k1", "v31");
    	zvl3.put("k2", "v32");
    	zvl3.put("k3", "v32");
    	
    	ArrayList list = new ArrayList();
    	list.add(zvl1);
    	list.add(zvl2);
    	list.add(zvl3);
    	
    	String jsonV = ZValue.getArrayJSONValue(list);
    	System.out.println(jsonV);
    	*/

    	ZValue zvl1 = new ZValue();
    	zvl1.put("k1", "v11");
    	zvl1.put("k2", "v12");
    	zvl1.put("k3", "v12");
    	zvl1.put("arr", new String[]{"1","2","3"});
    	System.out.println(zvl1.getJSONValue());

    }

}
