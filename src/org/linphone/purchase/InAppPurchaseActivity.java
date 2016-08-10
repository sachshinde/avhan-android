package org.linphone.purchase;
/*
InAppPurchaseListener.java
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
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import java.util.ArrayList;
import java.util.Locale;

import org.linphone.LinphoneManager;
import org.linphone.R;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.mediastream.Log;
import org.linphone.xmlrpc.XmlRpcHelper;
import org.linphone.xmlrpc.XmlRpcListenerBase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Sylvain Berfini
 */
public class InAppPurchaseActivity extends Activity implements InAppPurchaseListener, OnClickListener {
	private InAppPurchaseHelper inAppPurchaseHelper;
	private LinearLayout purchasableItemsLayout;
	private ArrayList<Purchasable> purchasedItems;
	private Button buyItemButton, recoverAccountButton;
	private Handler mHandler = new Handler();
	
	private EditText username, email;
	private TextView errorMessage;
	private boolean usernameOk = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inAppPurchaseHelper = new InAppPurchaseHelper(this, this);
		
		setContentView(R.layout.in_app_store);
		purchasableItemsLayout = (LinearLayout) findViewById(R.id.purchasable_items);
		
		username = (EditText) findViewById(R.id.username);
		email = (EditText) findViewById(R.id.email);
		errorMessage = (TextView) findViewById(R.id.username_error);
    	addUsernameHandler(username, errorMessage);
	}
	
	@Override
	protected void onDestroy() {
		inAppPurchaseHelper.destroy();
		super.onDestroy();
	}
	
	@Override
	public void onServiceAvailableForQueries() {
		email.setText(inAppPurchaseHelper.getGmailAccount());
		email.setEnabled(false);
		
		inAppPurchaseHelper.getPurchasedItemsAsync();
	}

	@Override
	public void onAvailableItemsForPurchaseQueryFinished(ArrayList<Purchasable> items) {
		purchasableItemsLayout.removeAllViews();
		
		for (Purchasable item : items) {
			displayBuySubscriptionButton(item);
		}
	}

	@Override
	public void onPurchasedItemsQueryFinished(ArrayList<Purchasable> items) {
		purchasedItems = items;
		
		if (items == null || items.size() == 0) {
			inAppPurchaseHelper.getAvailableItemsForPurchaseAsync();
		} else {
			for (Purchasable purchasedItem : purchasedItems) {
				Log.d("[In-app purchase] Found already bought item, expires " + purchasedItem.getExpireDate());
				displayRecoverAccountButton(purchasedItem);
			}
		}
	}

	@Override
	public void onPurchasedItemConfirmationQueryFinished(boolean success) {
		if (success) {
			XmlRpcHelper xmlRpcHelper = new XmlRpcHelper();
			xmlRpcHelper.createAccountAsync(new XmlRpcListenerBase() {
				@Override
				public void onAccountCreated(String result) {
					//TODO
				}
			}, getUsername(), email.getText().toString(), null);
		}
	}

	@Override
	public void onClick(View v) {
		Purchasable item = (Purchasable) v.getTag();
		if (v.equals(recoverAccountButton)) {
			XmlRpcHelper xmlRpcHelper = new XmlRpcHelper();
			xmlRpcHelper.createAccountAsync(new XmlRpcListenerBase() {
				@Override
				public void onAccountCreated(String result) {
					//TODO
				}
			}, getUsername(), email.getText().toString(), null);
		} else {
			inAppPurchaseHelper.purchaseItemAsync(item.getId(), getUsername());
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		inAppPurchaseHelper.parseAndVerifyPurchaseItemResultAsync(requestCode, resultCode, data);
	}

	@Override
	public void onRecoverAccountSuccessful(boolean success) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				recoverAccountButton.setEnabled(false);				
			}
		});
	}

	@Override
	public void onError(final String error) {
		Log.e(error);
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(InAppPurchaseActivity.this, error, Toast.LENGTH_LONG).show();	
			}
		});
	}

	@Override
	public void onActivateAccountSuccessful(boolean success) {
		if (success) {
			Log.d("[In-app purchase] Account activated");
		}
	}
	
	private void displayBuySubscriptionButton(Purchasable item) {
		View layout = LayoutInflater.from(this).inflate(R.layout.in_app_purchasable, purchasableItemsLayout);
		Button button = (Button) layout.findViewById(R.id.inapp_button);
		button.setText("Buy account (" + item.getPrice() + ")");
		button.setTag(item);
		button.setOnClickListener(this);
		XmlRpcHelper xmlRpcHelper = new XmlRpcHelper();
		xmlRpcHelper.createAccountAsync(new XmlRpcListenerBase() {
			@Override
			public void onAccountCreated(String result) {
				//TODO
			}
		}, getUsername(), email.getText().toString(), null);
		
		buyItemButton = button;
		buyItemButton.setEnabled(usernameOk);
	}
	
	private void displayRecoverAccountButton(Purchasable item) {
		View layout = LayoutInflater.from(this).inflate(R.layout.in_app_purchasable, purchasableItemsLayout);
		Button button = (Button) layout.findViewById(R.id.inapp_button);
		button.setText("Recover account");
		button.setTag(item);
		button.setOnClickListener(this);
		
		recoverAccountButton = button;
		recoverAccountButton.setEnabled(usernameOk);
	}
	
	private String getUsername() {
		String username = this.username.getText().toString();
		LinphoneProxyConfig lpc = LinphoneManager.getLc().createProxyConfig();
		username = lpc.normalizePhoneNumber(username);
		return username.toLowerCase(Locale.getDefault());
	}
	
	private boolean isUsernameCorrect(String username) {
		LinphoneProxyConfig lpc = LinphoneManager.getLc().createProxyConfig();
		return lpc.isPhoneNumber(username);
	}
	
	private void addUsernameHandler(final EditText field, final TextView errorMessage) {
		field.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			public void onTextChanged(CharSequence s, int start, int count, int after) {
				usernameOk = false;
				String username = s.toString();
				if (isUsernameCorrect(username)) {
					usernameOk = true;
					errorMessage.setText("");
				} else {
					errorMessage.setText(R.string.wizard_username_incorrect);
				}
				if (buyItemButton != null) buyItemButton.setEnabled(usernameOk);
				if (recoverAccountButton != null) recoverAccountButton.setEnabled(usernameOk);
			}
		});
	}
}
