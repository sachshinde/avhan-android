package com.Wsdl2Code.WebServices.SampleService;

//------------------------------------------------------------------------------
// <wsdl2code-generated>
//    This code was generated by http://www.wsdl2code.com version  2.2
//
// Date Of Creation: 2/20/2013 11:16:36 PM
//    Please dont change this code, regeneration will override your changes
//</wsdl2code-generated>
//
//------------------------------------------------------------------------------
//
//This source code was auto-generated by Wsdl2Code  Version
//
public class WS_Enums {

    public enum SoapProtocolVersion{
        	Default(0),
        	Soap11(1),
        	Soap12(2);
        
        private int code;
        
        SoapProtocolVersion(int code){
            this.code = code;
        }
        
        public int getCode(){
            return code;
        }
        
        public static SoapProtocolVersion fromString(String str)
        {
            if (str.equals("Default"))
                return Default;
            if (str.equals("Soap11"))
                return Soap11;
            if (str.equals("Soap12"))
                return Soap12;
            return null;
        }
    }
    public enum TestEnum{
        	TestEnum1(0),
        	TestEnum2(1),
        	TestEnum3(2),
        	TestEnum4(3);
        
        private int code;
        
        TestEnum(int code){
            this.code = code;
        }
        
        public int getCode(){
            return code;
        }
        
        public static TestEnum fromString(String str)
        {
            if (str.equals("TestEnum1"))
                return TestEnum1;
            if (str.equals("TestEnum2"))
                return TestEnum2;
            if (str.equals("TestEnum3"))
                return TestEnum3;
            if (str.equals("TestEnum4"))
                return TestEnum4;
            return null;
        }
    }
}