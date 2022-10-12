package com.eBrother.wutil;


public class Encrypt
{

    public Encrypt()
    {
    }

    public static String encrypt(String _arg, String _key)
    {
        StringBuffer _temp = new StringBuffer();
        try
        {
            _key = new String(new String(_key.getBytes("KSC5601"), "8859_1"));
            int _int = 0;
            for(int i = 0; i < _arg.length();)
            {
                if(_key.length() == _int)
                    _int = 0;
                char _xor = (char)(_arg.charAt(i) ^ _key.charAt(_int));
                if(_xor < '\020')
                    _temp.append("0" + Integer.toHexString(_xor));
                else
                    _temp.append(Integer.toHexString(_xor));
                i++;
                _int++;
            }

        }
        catch(Exception exception) { }
        return (new StringBuffer(_temp.toString())).reverse().toString();
    }
}
