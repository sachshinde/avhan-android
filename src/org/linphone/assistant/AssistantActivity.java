package org.linphone.assistant;
/*
AssistantActivity.java
Copyright (C) 2015  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
import static android.content.Intent.ACTION_MAIN;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.security.PublicKey;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.linphone.CallActivity;
import org.linphone.Contact;
import org.linphone.ContactDetailsFragment;
import org.linphone.ContactsListFragment;
import org.linphone.LinphoneActivity;
import org.linphone.LinphoneManager;
import org.linphone.LinphonePreferences;
import org.linphone.LinphoneService;
import org.linphone.LinphonePreferences.AccountBuilder;
import org.linphone.R;
import org.linphone.StatusFragment;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAddress.TransportType;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.LinphoneService;

import webservicehandler.ACSAuthentication;
import webservicehandler.ACSKeyPairValue;
import webservicehandler.HandleResponse;
import webservicehandler.MD5;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.Wsdl2Code.WebServices.SampleService.IWsdl2CodeEvents;
import com.Wsdl2Code.WebServices.SampleService.SampleService;
import com.Wsdl2Code.WebServices.SampleService.VectorByte;
import com.Wsdl2Code.WebServices.SampleService.WS_Enums.TestEnum;
/**
 * @author Sylvain Berfini
 */
public class AssistantActivity extends Activity implements OnClickListener {
	private static final String MyPREFERENCES = null;
	private static AssistantActivity instance;
	private Toast mToastToShow;
	private ImageView back, cancel;
	private AssistantFragmentsEnum currentFragment;
	private AssistantFragmentsEnum firstFragment;
	private Fragment fragment;
	private LinphonePreferences mPrefs;
	private boolean accountCreated = false, newAccount = false;
	private LinphoneCoreListenerBase mListener;
	private LinphoneAddress address;
	private StatusFragment status;
	private Dialog dialog;
	
	
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getResources().getBoolean(R.bool.orientation_portrait_only)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		setContentView(R.layout.assistant);
		initUI();

	firstFragment = getResources().getBoolean(R.bool.assistant_use_linphone_login_as_first_fragment) ? AssistantFragmentsEnum.LINPHONE_LOGIN : AssistantFragmentsEnum.WELCOME;
		if (findViewById(R.id.fragment_container) != null) {
			if (savedInstanceState == null) {
				display(firstFragment);
			} else {
				currentFragment = (AssistantFragmentsEnum) savedInstanceState.getSerializable("CurrentFragment");
			}
		}
		mPrefs = LinphonePreferences.instance();
		if(mPrefs.isFirstLaunch()) {
			status.enableSideMenu(false);
		}

		mListener = new LinphoneCoreListenerBase(){
			@Override
			public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg, LinphoneCore.RegistrationState state, String smessage) {
				if(accountCreated && !newAccount){
					if(address != null && address.asString().equals(cfg.getIdentity()) ) {
						if (state == RegistrationState.RegistrationOk) {
							if (LinphoneManager.getLc().getDefaultProxyConfig() != null) {
								System.out.println("RegistrationOk..............................................");
								launchEchoCancellerCalibration(true);
								
							}
						} else if (state == RegistrationState.RegistrationFailed) {
							if(dialog == null || !dialog.isShowing()) {
								//dialog = createErrorDialog(cfg, smessage);
								//dialog.show();
								System.out.println("RegistrationFailed..............................................");
								launchEchoCancellerCalibration(true);
								
							}
						}
					}
				}
			}
		};
		instance = this;
		instance.displayLoginLinphone();
	}
	

	@Override
	protected void onResume() {
		
		super.onResume();
		System.out.println(".......................AssistantActivity........................");
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null) {
			lc.addListener(mListener);
		}
	}

	@Override
	protected void onPause() {
		LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
		if (lc != null) {
			lc.removeListener(mListener);
		}

		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("CurrentFragment", currentFragment);
		super.onSaveInstanceState(outState);
	}

	public static AssistantActivity instance() {
		return instance;
	}

	public void updateStatusFragment(StatusFragment fragment) {
		status = fragment;
	}

	private void initUI() {
		back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(this);
		cancel = (ImageView) findViewById(R.id.cancel);
		cancel.setOnClickListener(this);
	}

	private void changeFragment(Fragment newFragment) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.replace(R.id.fragment_container, newFragment);
		transaction.commitAllowingStateLoss();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.cancel) {
			LinphonePreferences.instance().firstLaunchSuccessful();
			if (getResources().getBoolean(R.bool.setup_cancel_move_to_back)) {
				moveTaskToBack(true);
			} else {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		} else if (id == R.id.back) {
			onBackPressed();
		}
	}

	@Override
	public void onBackPressed() {/*
		if (currentFragment == firstFragment) {
			LinphonePreferences.instance().firstLaunchSuccessful();
			if (getResources().getBoolean(R.bool.setup_cancel_move_to_back)) {
				moveTaskToBack(true);
			} else {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		} else if (currentFragment == AssistantFragmentsEnum.LOGIN
				|| currentFragment == AssistantFragmentsEnum.LINPHONE_LOGIN
				|| currentFragment == AssistantFragmentsEnum.CREATE_ACCOUNT
				|| currentFragment == AssistantFragmentsEnum.REMOTE_PROVISIONING) {
			WelcomeFragment fragment = new WelcomeFragment();
			changeFragment(fragment);
			currentFragment = AssistantFragmentsEnum.WELCOME;
			back.setVisibility(View.INVISIBLE);
		} else if (currentFragment == AssistantFragmentsEnum.WELCOME) {
			finish();
		}*/
	}

	private void launchEchoCancellerCalibration(boolean sendEcCalibrationResult) {
		boolean needsEchoCalibration = LinphoneManager.getLc().needsEchoCalibration();
		if (needsEchoCalibration && mPrefs.isFirstLaunch()) {
			EchoCancellerCalibrationFragment fragment = new EchoCancellerCalibrationFragment();
			fragment.enableEcCalibrationResultSending(sendEcCalibrationResult);
			changeFragment(fragment);
			currentFragment = AssistantFragmentsEnum.ECHO_CANCELLER_CALIBRATION;
			back.setVisibility(View.VISIBLE);
			cancel.setEnabled(false);
		} else {
			success();
		}		
	}
	public void testlogin(String username, String password, String displayName, boolean validate){


		System.out.println("before loguin"+username);
		System.out.println("before password"+password);
	

		HandleResponse objHandleResponse = null;
		objHandleResponse = new HandleResponse();

		String strEncryptPassword = null;
		//String strPassword = "Test#$56!@";
		String strPassword = password;
		String strAuthenticateResponse = null;
		//String strLoginId = "planfirma_user1@dev1";
		String strLoginId = username;
		String strACSSessionid = null;
		String[] strArray = null;
		int inValue;
		String StrSessionID = null;
		ACSKeyPairValue objKPair = null;
		ACSAuthentication objAuthentication = null;
		String strSIPResponse = "";

		try {

			//            objACSKeyPairValue = new ACSKeyPairValue();

			objHandleResponse.setStrLoginWebServiceURL("https://demo2.avhan.com:9443/LoginWebServiceP/indexLogin.jsp ?");
			objHandleResponse.setStrDataHandlerURL("https://demo2.avhan.com:9443/ ACS Web s erviceP/ CallACSWS .jsp");


			// Call Key Pair Generation // Parse Key
			objKPair = objHandleResponse.SendKeyPairReq();

			// Get encruypted Password k1,pass
			strEncryptPassword = objHandleResponse.encryptPassword(strPassword, objKPair.getStrKeyValue1());

			// Call Authenticate
			strAuthenticateResponse = objHandleResponse.sendAuthenticatinReq(strLoginId, strEncryptPassword, objKPair.getStrKeyValue2());

			// Get Result
			objAuthentication = objHandleResponse.validateWebserviceCall(strAuthenticateResponse);

			System.out.println("Authenticateion Result " + objAuthentication.getnResult());

			int nResult = objAuthentication.getnResult();
			if (nResult == 1) {
				// If Sucess
				strACSSessionid = objAuthentication.getStrACSSessionid();
				// Call SIP details
				strSIPResponse = objHandleResponse.SendSIPServerReq(strACSSessionid);
				System.out.println("strSIPResponse .."+strSIPResponse);

				accountCreated = true;
				saveCreatedAccount("username", "password", "displayName", "domain",null);
				Fragment newFragment = new ContactsListFragment();
				//changeFragment(newFragment);
				Intent intent = new Intent(this, LinphoneActivity.class);
				intent.putExtra("VideoEnabled", true);
				//startOrientationSensor();
				startActivity(intent);

			}
			else
			{
				System.out.println("Not valid user result.."+objAuthentication.getnResult());
			}



		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}


		//setContentView(R.layout.contacts_list);*/
	}
	private void logIn(String username, String password, String displayName, String domain, TransportType transport, boolean sendEcCalibrationResult) {
		
		boolean checkconn = isOnline();
				
		if(checkconn == false){
			
			Toast.makeText(AssistantActivity.this, 
					"Please check internet connection " +
					"", Toast.LENGTH_SHORT).show();
					Toast.makeText(AssistantActivity.this, 
							"Please check internet connection " +
							"", Toast.LENGTH_SHORT).show();
			
		}else{
		
		HandleResponse objHandleResponse = null;
		objHandleResponse = new HandleResponse();
		System.out.println("hi  "+username+" "+password);
		System.out.println("DisplayName  "+displayName);
		System.out.println("transport  "+transport);
		String strEncryptPassword = null;
		String strEPassword = null;
		String strPassword = password;
		String strLoginId = username;
		String strAppid = "appid";
		String strAuthenticateResponse = null;
		String StrSessionID = null;
		ACSKeyPairValue objKPair = null;
		ACSAuthentication objAuthentication = null;
		String strSIPResponse = "";
		System.setProperty("jsse.enableSNIExtension", "false");
final LinphoneService ls=new LinphoneService();
System.out.println("LinphoneServices :"+ls.BadGateway);
System.out.println("LinphoneServices :"+ls.aliveBandwidth);
System.out.println("LinphoneServices :"+ls.aliveCommand);
System.out.println("LinphoneServices :"+ls.aliveCustomercode);
System.out.println("LinphoneServices :"+ls.aliveSender);
System.out.println("LinphoneServices :"+ls.aliveSendertype);
System.out.println("LinphoneServices :"+ls.appid);
System.out.println("LinphoneServices :"+ls.applicationid);
System.out.println("LinphoneServices :"+ls.BadGateway);

System.out.println("LinphoneServices :"+ls.reqdatetime); 




		try {
			
			
			
			
			
			
			URL GenerateKeyUrl = new URL("https://demo2.avhan.com:9443/LoginWebServiceP/indexLogin.jsp?methodname=generateKey&data="+URLEncoder.encode("{\"appid\":"+ls.appid+"}","UTF-8"));
		//	System.out.println("GenerateKeyUrl : "+GenerateKeyUrl);
			BufferedReader br = new BufferedReader(new InputStreamReader(GenerateKeyUrl.openStream()));
			String strTemp = "";
			while (null != (strTemp = br.readLine())) {
				if(strTemp.isEmpty()){continue;}else{break;}
					}
		//	System.out.println("Generete Key Respoce : "+strTemp);
			JSONParser parser = new JSONParser(); 
			JSONObject json = (JSONObject) parser.parse(strTemp);
			System.out.println("keyvalue1 : "+json.get("keyvalue1"));
			System.out.println("keyvalue2 : "+json.get("keyvalue2"));
			String strRandomValue = json.get("keyvalue2").toString();
			MD5 obj = new MD5();
			MD5 obj1 = new MD5();
			obj.init();
			obj1.init();
			strEncryptPassword = obj.ConvertString(strPassword);
			strEPassword = obj1.ConvertString(strEncryptPassword + json.get("keyvalue1"));
			


			URL LoginUrl = new URL("https://demo2.avhan.com:9443/LoginWebServiceP/indexLogin.jsp?methodname=login&data={\"password\":"+"\""+strEPassword+"\""+",\"loginid\":"+"\""+strLoginId+"\""+",\"randomvalue\":"+"\""+strRandomValue+"\""+",\"applicationid\":"+ls.applicationid+"}");
	//		System.out.println("LoginUrl :"+LoginUrl);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(LoginUrl.openStream()));
			
			String strTemp1 = "";
			while (null != (strTemp1 = br1.readLine())) {
				if(strTemp1.isEmpty()){continue;}else{break;}
			}
	//		System.out.println("login Response :"+strTemp1);

			JSONObject json1 = (JSONObject) parser.parse(strTemp1);
			System.out.println("result : "+json1.get("result"));
			String varResult = json1.get("result").toString();
			
			if(varResult.equals("1")){
				
				String varAcssessionid=json1.get("acssessionid").toString();
	//			System.out.println("varAcssessionid :"+varAcssessionid);
				
				SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);	
				Editor editor = sharedpreferences.edit();
				editor.putString("acsessionid", varAcssessionid);
				editor.putString("userid", username);
				editor.putString("password", password);
				editor.commit();
								
				URL SIPServerUrl = new URL("https://demo2.avhan.com:9443/ACSWebserviceP/CallACSWS.jsp?data="+URLEncoder.encode("{\"command\":"+ls.sipCommand+",\"acssessionid\":"+"\""+varAcssessionid+"\""+",\"reqdatetime\":"+"\""+ls.reqdatetime+"\""+"}","UTF-8"));
	//			System.out.println("SIPServerUrl :"+SIPServerUrl);
				BufferedReader br2 = new BufferedReader(new InputStreamReader(SIPServerUrl.openStream()));
				
				String strTemp2 = "";
				while (null != (strTemp2 = br2.readLine())) {
					if(strTemp2.isEmpty()){continue;}else{break;}
				}
				System.out.println("SIPServerUrl Response :"+strTemp2);

				JSONObject json2 = (JSONObject) parser.parse(strTemp2.substring(15));
				System.out.println("SIPServer Status : "+json2);
				ArrayList sipserver = new ArrayList();
				sipserver = (ArrayList) json2.get("sipservers");
				JSONObject sipserverjson = (JSONObject) (sipserver.get(0));
				
				System.out.println("stunservers : "+sipserverjson.get("sipserverip"));
				/*String varStatus = json2.get("status").toString();*/
				String varAcssessionid2=json2.get("acssessionid").toString();
				String extension=json2.get("extension").toString();
				String pass=json2.get("password").toString();
				System.out.println("varAcssessionid2 : "+varAcssessionid2);	
				String sipdomain= sipserverjson.get("sipserverip").toString();
				System.out.print("sipdomain"+sipdomain);
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null && getCurrentFocus() != null) {
					imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
					}
				saveCreatedAccount(extension, pass, username, sipdomain, transport);
				//saveCreatedAccount("sachin88", "sachin88", "displayName", domain, transport);
				final Timer t = new Timer();
				t.scheduleAtFixedRate(new TimerTask() {
				@Override
				    public void run() {
						try{
								SharedPreferences prefs = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE); 
								String acsessionid = prefs.getString("acsessionid", "No acsessionid found");//"No acsessionid defined" is the default value.
		//						System.out.println("in shared preference :"+acsessionid);
								URL AliveUrl = new URL("https://demo2.avhan.com:9443/ACSWebserviceP/CallACSWS.jsp?data="+URLEncoder.encode("{\"command\":"+ls.aliveCommand+",\"acssessionid\":"+"\""+acsessionid+"\""+",\"customercode\":"+"\""+ls.aliveCustomercode+"\""+",\"sendertype\":"+"\""+ls.aliveSendertype+"\""+",\"bandwidth\":"+"\""+ls.aliveBandwidth+"\""+",\"sender\":"+"\""+ls.aliveSender+"\""+",\"reqdatetime\":"+"\""+ls.reqdatetime+"\""+"}","UTF-8"));
				
		//						System.out.println("AliveUrl :"+AliveUrl+"\n\n\n");
								BufferedReader br3 = new BufferedReader(new InputStreamReader(AliveUrl.openStream()));
		//						System.out.println("br3 :"+br3.readLine());
								String strTemp3 = "";
								while (null != (strTemp3 = br3.readLine())) {
									if(strTemp3.isEmpty()){continue;}else{break;}
								}
		//						System.out.println("Alive Status :"+strTemp3);
		//						System.out.println("New Status :"+strTemp3.substring(15) );
				
								JSONParser parser = new JSONParser(); 
								JSONObject json3 = (JSONObject) parser.parse(strTemp3.substring(15));
								String varStatus = json3.get("status").toString();
		//						System.out.println("Status :"+varStatus);
								if (!varStatus.equals("3")){
									t.cancel();
									t.purge();
									Toast.makeText(AssistantActivity.this, 
									"User already logged in " +
									"", Toast.LENGTH_SHORT).show();
									Toast.makeText(AssistantActivity.this, 
											"User already logged in " +
											"", Toast.LENGTH_SHORT).show();
									try{
										URL LogoutUrl = new URL("https://demo2.avhan.com:9443/ACSWebserviceP/CallACSWS.jsp?data="+URLEncoder.encode("{\"acssessionid\":"+"\""+acsessionid+"\""+",\"command\":"+ls.logoutCommand+",\"reqdatetime\":"+"\""+ls.reqdatetime+"\""+",\"reasonid\":"+ls.logoutReasionid+"}","UTF-8"));
						//				System.out.println("LogoutUrl :"+LogoutUrl+"\n\n\n");
										BufferedReader br5 = new BufferedReader(new InputStreamReader(LogoutUrl.openStream()));
						//				System.out.println("br4 :"+br5.readLine());
										String strTemp5 = "";
										while (null != (strTemp5 = br5.readLine())) {
											if(strTemp5.isEmpty()){continue;}else{break;}
										}
						//				System.out.println("LogoutUrl Status :"+strTemp5);
						//				System.out.println("New LogoutUrl Status :"+strTemp5.substring(15) );
										
									 
										JSONObject json5 = (JSONObject) parser.parse(strTemp5.substring(15));
										String varStatus5 = json5.get("status").toString();
										quit();
										displayLoginLinphone();
						//				System.out.println("Status :"+varStatus5);
						//				System.out.println("statusdesc : "+json5.get("statusdescription").toString());
						//				System.out.println("revisionno : "+json5.get("revisionno").toString());
									}catch (Exception e) {
											e.printStackTrace();
										}
								}
		//		System.out.println("statusdescription : "+json3.get("statusdescription").toString());
		//		System.out.println("revisionno : "+json3.get("revisionno").toString());
						}catch (Exception e) {
								e.printStackTrace();
							}					
							   	}
									},0,10000);
							launchEchoCancellerCalibration(false);
							/*InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							if (imm != null && getCurrentFocus() != null) {
								imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
								}
							*/	System.out.println("before loguin"+username);
							//	saveCreatedAccount("sachin88", "sachin88", displayName, domain, transport);
					
			}else if(varResult.equals("32")){
				
				
				Toast.makeText(AssistantActivity.this, 
					    "already logged in, please logout from other device" +
					    "", Toast.LENGTH_SHORT).show();
				Toast.makeText(AssistantActivity.this, 
					    "already logged in, please logout from other device" +
					    "", Toast.LENGTH_SHORT).show();
				
				SharedPreferences prefs = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE); 
				String acsessionid = prefs.getString("acsessionid", "No acsessionid found");//"No name defined" is the default value.
				System.out.println("in shared preference :"+acsessionid);
				try{
					
					URL LogoutUrl = new URL("https://demo2.avhan.com:9443/ACSWebserviceP/CallACSWS.jsp?data="+URLEncoder.encode("{\"acssessionid\":"+"\""+acsessionid+"\""+",\"command\":"+ls.logoutCommand+",\"reqdatetime\":"+"\""+ls.reqdatetime+"\""+",\"reasonid\":"+ls.logoutReasionid+"}","UTF-8"));
		//			System.out.println("LogoutUrl :"+LogoutUrl+"\n\n\n");
					BufferedReader br4 = new BufferedReader(new InputStreamReader(LogoutUrl.openStream()));
		//			System.out.println("br4 :"+br4.readLine());
					String strTemp4 = "";
					while (null != (strTemp4 = br4.readLine())) {
						if(strTemp4.isEmpty()){continue;}else{break;}
					}
		//			System.out.println("LogoutUrl Status :"+strTemp4);
		//			System.out.println("New LogoutUrl Status :"+strTemp4.substring(15) );
					JSONObject json4 = (JSONObject) parser.parse(strTemp4.substring(15));
					String varStatus = json4.get("status").toString();
					quit();
					accountCreated=false;
					 Intent GooglePlusSignInActivity = new Intent(this,AssistantActivity.class);
					    startActivity(GooglePlusSignInActivity);
					
				
				}catch (Exception e) {
						e.printStackTrace();
					}	
								
				
			}else{
				
				
				Toast.makeText(AssistantActivity.this, 
					    "Incorrect Username and Password" +
					    "", Toast.LENGTH_SHORT).show();
				

				Toast.makeText(AssistantActivity.this, 
					    "Incorrect Username and Password" +
					    "", Toast.LENGTH_SHORT).show();
				
				System.out.println("unsuccessful !!!!");}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	}
	
	public boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	public void checkAccount(String username, String password, String displayName, String domain) {
		saveCreatedAccount(username, password, displayName, domain, null);
	}

	public void linphoneLogIn(String username, String password, String displayName, boolean validate) {
		if (validate) {

			checkAccount(username, password, displayName, getString(R.string.default_domain));
		} else {
			if(accountCreated) {

				retryLogin(username, password, displayName, getString(R.string.default_domain), null);
			} else { 
				logIn(username, password, displayName, getString(R.string.default_domain), null, true);
			}
		}
	}

	public void genericLogIn(String username, String password, String displayName, String domain, TransportType transport) {
		if(accountCreated) {
			retryLogin(username, password, displayName, domain, transport);
		} else {
			logIn(username, password, displayName, domain, transport, false);
		}
	}

	private void display(AssistantFragmentsEnum fragment) {
		System.out.println("start fragment"+fragment );
		switch (fragment) {
		case WELCOME:
			displayMenu();
			break;
		case LINPHONE_LOGIN:
			displayLoginLinphone();
			break;
		default:
			throw new IllegalStateException("Can't handle " + fragment);
		}
	}

	public void displayMenu() {
		
		System.out.println("welcome+++++++++++++++++");
		final LinphoneService ls=new LinphoneService();
		
		SharedPreferences prefs = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE); 
		String acsessionid = prefs.getString("acsessionid", "No acsessionid found");//"No acsessionid defined" is the default value.
		String password = prefs.getString("password", null);
		String userid = prefs.getString("userid", null);
		System.out.println("in shared preference :"+password);
		System.out.println("in shared preference :"+userid);
		if(password==null){
			displayLoginLinphone();
			
		}else{

			try{
					URL AliveUrl = new URL("https://demo2.avhan.com:9443/ACSWebserviceP/CallACSWS.jsp?data="+URLEncoder.encode("{\"command\":"+ls.aliveCommand+",\"acssessionid\":"+"\""+acsessionid+"\""+",\"customercode\":"+"\""+ls.aliveCustomercode+"\""+",\"sendertype\":"+"\""+ls.aliveSendertype+"\""+",\"bandwidth\":"+"\""+ls.aliveBandwidth+"\""+",\"sender\":"+"\""+ls.aliveSender+"\""+",\"reqdatetime\":"+"\""+ls.reqdatetime+"\""+"}","UTF-8"));
	
//						System.out.println("AliveUrl :"+AliveUrl+"\n\n\n");
					BufferedReader br3 = new BufferedReader(new InputStreamReader(AliveUrl.openStream()));
//						System.out.println("br3 :"+br3.readLine());
					String strTemp3 = "";
					while (null != (strTemp3 = br3.readLine())) {
						if(strTemp3.isEmpty()){continue;}else{break;}
					}
//						System.out.println("Alive Status :"+strTemp3);
//						System.out.println("New Status :"+strTemp3.substring(15) );
	
					JSONParser parser = new JSONParser(); 
					JSONObject json3 = (JSONObject) parser.parse(strTemp3.substring(15));
					String varStatus = json3.get("status").toString();
						System.out.println("Status :"+varStatus);
					if (!varStatus.equals("3")){
					
						try{
							URL LogoutUrl = new URL("https://demo2.avhan.com:9443/ACSWebserviceP/CallACSWS.jsp?data="+URLEncoder.encode("{\"acssessionid\":"+"\""+acsessionid+"\""+",\"command\":"+ls.logoutCommand+",\"reqdatetime\":"+"\""+ls.reqdatetime+"\""+",\"reasonid\":"+ls.logoutReasionid+"}","UTF-8"));
			//				System.out.println("LogoutUrl :"+LogoutUrl+"\n\n\n");
							BufferedReader br5 = new BufferedReader(new InputStreamReader(LogoutUrl.openStream()));
			//				System.out.println("br4 :"+br5.readLine());
							String strTemp5 = "";
						
			//				System.out.println("LogoutUrl Status :"+strTemp5);
			//				System.out.println("New LogoutUrl Status :"+strTemp5.substring(15) );
							
						 
							JSONObject json5 = (JSONObject) parser.parse(strTemp5.substring(15));
							String varStatus5 = json5.get("status").toString();
							quit();
							logIn(userid, password, userid, getString(R.string.default_domain), null, false);
			//				System.out.println("Status :"+varStatus5);
			//				System.out.println("statusdesc : "+json5.get("statusdescription").toString());
			//				System.out.println("revisionno : "+json5.get("revisionno").toString());
						
						}catch (Exception e) {
								e.printStackTrace();
							}
					}
					
//		System.out.println("statusdescription : "+json3.get("statusdescription").toString());
//		System.out.println("revisionno : "+json3.get("revisionno").toString());
			}catch (Exception e) {
					e.printStackTrace();
				}					
				   	
			
			
			
			
			
		}
		
		
		/*fragment = new WelcomeFragment();
		changeFragment(fragment);
		currentFragment = AssistantFragmentsEnum.WELCOME;
		back.setVisibility(View.INVISIBLE);*/
	}

	public void displayLoginGeneric() {
		fragment = new LoginFragment();
		changeFragment(fragment);
		currentFragment = AssistantFragmentsEnum.LOGIN;
		back.setVisibility(View.VISIBLE);
	}

	public void displayLoginLinphone() {
		System.out.println("login+++++++++++++++++");
		fragment = new LinphoneLoginFragment();
		changeFragment(fragment);
		currentFragment = AssistantFragmentsEnum.LINPHONE_LOGIN;
		back.setVisibility(View.VISIBLE);
	}

	public void displayCreateAccount() {
		fragment = new CreateAccountFragment();
		changeFragment(fragment);
		currentFragment = AssistantFragmentsEnum.CREATE_ACCOUNT;
		back.setVisibility(View.VISIBLE);
	}

	public void displayRemoteProvisioning() {
		fragment = new RemoteProvisioningFragment();
		changeFragment(fragment);
		currentFragment = AssistantFragmentsEnum.REMOTE_PROVISIONING;
		back.setVisibility(View.VISIBLE);
	}

	public void retryLogin(String username, String password, String displayName, String domain, TransportType transport) {
		accountCreated = false;
		saveCreatedAccount(username, password, displayName, domain, transport);
	}

	public void loadLinphoneConfig(){
		//LinphoneManager.getInstance().loadConfig();
		//LinphoneManager.getInstance().restartLinphoneCore();
	}

	public void saveCreatedAccount(String username, String password, String displayName, String domain, TransportType transport) {
		
		System.out.println("username"+ username+" password"+ password+" displayName"+ displayName+"domain"+domain+" TransportType"+ transport);

		if (accountCreated)
			return;

		if(username.startsWith("sip:")) {
			username = username.substring(4);

		}

		if (username.contains("@"))
			username = username.split("@")[0];

		if(domain.startsWith("sip:")) {
			domain = domain.substring(4);

		}

		String identity = "sip:" + username + "@" + domain;
		try {
			address = LinphoneCoreFactory.instance().createLinphoneAddress(identity);
		} catch (LinphoneCoreException e) {
			e.printStackTrace();
		}

		if(address != null && displayName != null && !displayName.equals("")){
			address.setDisplayName(displayName);
		}

		boolean isMainAccountLinphoneDotOrg = domain.equals(getString(R.string.default_domain));
		AccountBuilder builder = new AccountBuilder(LinphoneManager.getLc())
		.setUsername(username)
		.setDomain(domain)
		.setDisplayName(displayName)
		.setPassword(password);


		if (isMainAccountLinphoneDotOrg) {
			if (getResources().getBoolean(R.bool.disable_all_security_features_for_markets)) {
				builder.setProxy(domain)
				.setTransport(TransportType.LinphoneTransportTcp);
			}
			else {
				builder.setProxy(domain)
				.setTransport(TransportType.LinphoneTransportTls);
			}

			builder.setExpires("604800")
			.setAvpfEnabled(true)
			.setAvpfRRInterval(3)
			.setQualityReportingCollector("sip:voip-metrics@sip.linphone.org")
			.setQualityReportingEnabled(true)
			.setQualityReportingInterval(180)
			.setRealm("sip.linphone.org")
			.setNoDefault(false);

            System.out.println("yes I am here :)");
			mPrefs.setStunServer(getString(R.string.default_stun));
			mPrefs.setIceEnabled(true);
		} else {
			
			System.out.println("yes I am here :);;;;;;");
			String forcedProxy = "";
			if (!TextUtils.isEmpty(forcedProxy)) {
				//builder.setProxy(forcedProxy)
				//setOutboundProxyEnabled(true)
				//.setAvpfRRInterval(5);
				builder.setProxy(domain)
				.setTransport(TransportType.LinphoneTransportUdp)
				.setOutboundProxyEnabled(true);
			}
			builder.setProxy(domain)
			.setTransport(TransportType.LinphoneTransportUdp);
			builder.setNoDefault(false)
			.setOutboundProxyEnabled(true);
			if(transport != null){
				builder.setTransport(transport);
			}else{
				builder.setTransport(TransportType.LinphoneTransportUdp);
				
			}
		}

		if (getResources().getBoolean(R.bool.enable_push_id)) {
			String regId = mPrefs.getPushNotificationRegistrationID();
			String appId = getString(R.string.push_sender_id);
			if (regId != null && mPrefs.isPushNotificationEnabled()) {
				String contactInfos = "app-id=" + appId + ";pn-type=google;pn-tok=" + regId;
				builder.setContactParameters(contactInfos);
			}
		}

		try {
			builder.saveNewAccount();
			accountCreated = true;
		} catch (LinphoneCoreException e) {
			e.printStackTrace();
		}
		
		
	}

	public void displayWizardConfirm(String username) {
		CreateAccountActivationFragment fragment = new CreateAccountActivationFragment();

		newAccount = true;
		Bundle extras = new Bundle();
		extras.putString("Username", username);
		fragment.setArguments(extras);
		changeFragment(fragment);

		currentFragment = AssistantFragmentsEnum.CREATE_ACCOUNT_ACTIVATION;
		back.setVisibility(View.INVISIBLE);
	}

	public void isAccountVerified(String username) {
		Toast.makeText(this, getString(R.string.assistant_account_validated), Toast.LENGTH_LONG).show();
		LinphoneManager.getLcIfManagerNotDestroyedOrNull().refreshRegisters();
		//launchEchoCancellerCalibration(true);
	}

	public void isEchoCalibrationFinished() {
		success();
		
	}

	public Dialog createErrorDialog(LinphoneProxyConfig proxy, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		if(message.equals("Forbidden")) {
			message = getString(R.string.assistant_error_bad_credentials);
		}
		builder.setMessage(message)
		.setTitle(proxy.getState().toString())
		.setPositiveButton(getString(R.string.continue_text), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				success();
			}
		})
		.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				LinphoneManager.getLc().removeProxyConfig(LinphoneManager.getLc().getDefaultProxyConfig());
				LinphonePreferences.instance().resetDefaultProxyConfig();
				LinphoneManager.getLc().refreshRegisters();
				dialog.dismiss();
			}
		});
		return builder.show();
	}

	public void success() {
		mPrefs.firstLaunchSuccessful();
		LinphoneActivity.instance().isNewProxyConfig();
		setResult(Activity.RESULT_OK);
		finish();
	}

	public void Wsdl2CodeStartedRequest() {
		// TODO Auto-generated method stub

	}

	public void Wsdl2CodeEndedRequest() {
		// TODO Auto-generated method stub

	}

	public void Wsdl2CodeFinished(String string, String result) {
		// TODO Auto-generated method stub

	}
	public void quit() {
		finish();
		
	//	LinphoneManager.destroy();
	//	stopService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
		String linphoneConfigFile = null;
		String linphoneInitialConfigFile = null;
		 File cache = getCacheDir();
		 File appDir = new File(cache.getParent());
		 if (appDir.exists()) {
	            String[] children = appDir.list();
	            for (String s : children) {
	                if (!s.equals("lib")) {
	                    deleteDir(new File(appDir, s));
	               
	                }
	            }
	        }
	    }
		
		
	
public static boolean deleteDir(File dir) {
    if (dir != null && dir.isDirectory()) {
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            boolean success = deleteDir(new File(dir, children[i]));
            if (!success) {
                return false;
            }
        }
    }

    return dir.delete();
}
}
