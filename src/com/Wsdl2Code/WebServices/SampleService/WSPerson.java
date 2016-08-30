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
import com.Wsdl2Code.WebServices.SampleService.WSAddress;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.PropertyInfo;
import java.util.Hashtable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

public class WSPerson implements KvmSerializable {
    
    public String firstName;
    public String lastName;
    public WSAddress address;
    
    public WSPerson(){}
    
    public WSPerson(SoapObject soapObject)
    {
        if (soapObject == null)
            return;
        if (soapObject.hasProperty("FirstName"))
        {
            Object obj = soapObject.getProperty("FirstName");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) soapObject.getProperty("FirstName");
                firstName = j.toString();
            }
        }
        if (soapObject.hasProperty("LastName"))
        {
            Object obj = soapObject.getProperty("LastName");
            if (obj != null && obj.getClass().equals(SoapPrimitive.class)){
                SoapPrimitive j =(SoapPrimitive) soapObject.getProperty("LastName");
                lastName = j.toString();
            }
        }
        if (soapObject.hasProperty("Address"))
        {
            SoapObject j = (SoapObject)soapObject.getProperty("Address");
            address =  new WSAddress (j);
            
        }
    }
    @Override
    public Object getProperty(int arg0) {
        switch(arg0){
            case 0:
                return firstName;
            case 1:
                return lastName;
            case 2:
                return address;
        }
        return null;
    }
    
    @Override
    public int getPropertyCount() {
        return 3;
    }
    
    @Override
    public void getPropertyInfo(int index, @SuppressWarnings("rawtypes") Hashtable arg1, PropertyInfo info) {
        switch(index){
            case 0:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "FirstName";
                break;
            case 1:
                info.type = PropertyInfo.STRING_CLASS;
                info.name = "LastName";
                break;
            case 2:
                info.type = WSAddress.class;
                info.name = "Address";
                break;
        }
}

@Override
public void setProperty(int arg0, Object arg1) {
}

}