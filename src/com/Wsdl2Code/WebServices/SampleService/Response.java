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
import com.Wsdl2Code.WebServices.SampleService.VectorWSPerson;
import com.Wsdl2Code.WebServices.SampleService.VectorString;
import com.Wsdl2Code.WebServices.SampleService.VectorByte;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;
import java.util.Hashtable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

public class Response implements KvmSerializable {
    
    public VectorWSPerson customArray;
    public VectorString stringArray;
    public VectorByte byteArray;
    public String errMessage;
    public int resultCode;
    
    public Response(){}
    
    public Response(SoapObject soapObject)
    {
        if (soapObject == null)
            return;
        if (soapObject.hasProperty("CustomArray"))
        {
            SoapObject j = (SoapObject)soapObject.getProperty("CustomArray");
            customArray = new VectorWSPerson(j);
        }
        if (soapObject.hasProperty("stringArray"))
        {
            SoapObject j = (SoapObject)soapObject.getProperty("stringArray");
            stringArray = new VectorString(j);
        }
        if (soapObject.hasProperty("byteArray"))
        {
            SoapPrimitive j = (SoapPrimitive)soapObject.getProperty("byteArray");
            byteArray = new VectorByte(j);
        }
        if (soapObject.hasProperty("errMessage"))
        {
            Object obj = soapObject.getProperty("errMessage");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) soapObject.getProperty("errMessage");
                errMessage = j.toString();
            }
        }
        if (soapObject.hasProperty("resultCode"))
        {
            Object obj = soapObject.getProperty("resultCode");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) soapObject.getProperty("resultCode");
                resultCode = Integer.parseInt(j.toString());
            }
        }
    }
    @Override
    public Object getProperty(int arg0) {
        switch(arg0){
            case 0:
                return customArray;
            case 1:
                return stringArray;
            case 2:
                return byteArray.toString();
            case 3:
                return errMessage;
            case 4:
                return resultCode;
        }
        return null;
    }
    
    @Override
    public int getPropertyCount() {
        return 5;
    }
    
    @Override
    public void getPropertyInfo(int index, @SuppressWarnings("rawtypes") Hashtable arg1, PropertyInfo info) {
        switch(index){
            case 0:
                info.type = PropertyInfo.VECTOR_CLASS;
                info.name = "CustomArray";
                break;
            case 1:
                info.type = PropertyInfo.VECTOR_CLASS;
                info.name = "stringArray";
                break;
            case 2:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "byteArray";
                break;
            case 3:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "errMessage";
                break;
            case 4:
                info.type = PropertyInfo.INTEGER_CLASS;
                info.name = "resultCode";
                break;
        }
}

@Override
public void setProperty(int arg0, Object arg1) {
}
}
