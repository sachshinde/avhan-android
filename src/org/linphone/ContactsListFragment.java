package org.linphone;
/*
ContactsListFragment.java
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
import java.util.ArrayList;
import java.util.List;

import org.linphone.compatibility.Compatibility;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.PresenceActivityType;
import org.linphone.mediastream.Log;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentProviderOperation;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

/**
 * @author Sylvain Berfini
 */
@SuppressLint("DefaultLocale")
public class ContactsListFragment extends Fragment implements OnClickListener, OnItemClickListener {
	private LayoutInflater mInflater;
	private ListView contactsList;
	private TextView noSipContact, noContact,noDealerContact,noCustomer;
	private ImageView allContacts, linphoneContacts,customercontacts, newContact1, edit, selectAll, deselectAll, delete, cancel;
	private boolean onlyDisplayLinphoneContacts,onlyDisplayDealerContacts,onlyDisplaycustomerContacts, isEditMode;
	private View allContactsSelected, linphoneContactsSelected,dealerContctsSelected,customerContactsSelected;
	private LinearLayout editList, topbar;
	private int lastKnownPosition;
	private AlphabetIndexer indexer;
	private boolean editOnClick = false, editConsumed = false, onlyDisplayChatAddress = false;
	private String sipAddressToAdd;
	private ImageView clearSearchField;
	private EditText searchField;
	private Cursor searchCursor;

	private static ContactsListFragment instance;
	
	static final boolean isInstanciated() {
		return instance != null;
	}

	public static final ContactsListFragment instance() {
		return instance;
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
		mInflater = inflater;
        View view = inflater.inflate(R.layout.contacts_list, container, false);
        
        if (getArguments() != null) {
	        editOnClick = getArguments().getBoolean("EditOnClick");
	        sipAddressToAdd = getArguments().getString("SipAddress");
	        
	        onlyDisplayChatAddress = getArguments().getBoolean("ChatAddressOnly");
        }
        
        noSipContact = (TextView) view.findViewById(R.id.noSipContact);
        noDealerContact = (TextView) view.findViewById(R.id.noDealerContact);
        noCustomer = (TextView) view.findViewById(R.id.noCustomerContact);
        noContact = (TextView) view.findViewById(R.id.noContact);
        
        contactsList = (ListView) view.findViewById(R.id.contactsList);
        contactsList.setOnItemClickListener(this);
        
        allContacts = (ImageView) view.findViewById(R.id.all_contacts);
        allContacts.setOnClickListener(this);
        
        linphoneContacts = (ImageView) view.findViewById(R.id.linphone_contacts);
        linphoneContacts.setOnClickListener(this);
        
        newContact1 = (ImageView) view.findViewById(R.id.newContact1);
        newContact1.setOnClickListener(this);
        
       customercontacts = (ImageView) view.findViewById(R.id.customer);
       customercontacts.setOnClickListener(this);

		allContactsSelected = view.findViewById(R.id.all_contacts_select);
		linphoneContactsSelected = view.findViewById(R.id.linphone_contacts_select);
		dealerContctsSelected = view.findViewById(R.id.dealer_contacts_select);
		customerContactsSelected= view.findViewById(R.id.customer_contacts_select);
		
		
        
        /*newContact = (ImageView) view.findViewById(R.id.newContact);
        newContact.setOnClickListener(this);*/
		/*newContact1 = (ImageView) view.findViewById(R.id.newContact1);
        newContact1.setOnClickListener(this);*/
       /* newContact.setEnabled(LinphoneManager.getLc().getCallsNb() == 0);*/
        
        
        allContacts.setEnabled(onlyDisplayLinphoneContacts);
        allContacts.setEnabled(onlyDisplayDealerContacts);
        
        linphoneContacts.setEnabled(!allContacts.isEnabled());

		selectAll = (ImageView) view.findViewById(R.id.select_all);
		selectAll.setOnClickListener(this);

		deselectAll = (ImageView) view.findViewById(R.id.deselect_all);
		deselectAll.setOnClickListener(this);

		delete = (ImageView) view.findViewById(R.id.delete);
		delete.setOnClickListener(this);

		editList = (LinearLayout) view.findViewById(R.id.edit_list);
		topbar = (LinearLayout) view.findViewById(R.id.top_bar);

		cancel = (ImageView) view.findViewById(R.id.cancel);
		cancel.setOnClickListener(this);

		/*edit = (ImageView) view.findViewById(R.id.edit);
		edit.setOnClickListener(this);*/
        
		clearSearchField = (ImageView) view.findViewById(R.id.clearSearchField);
		clearSearchField.setOnClickListener(this);
		
		searchField = (EditText) view.findViewById(R.id.searchField);
		searchField.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				searchContacts(searchField.getText().toString());
			}
		});

		return view;
    }

	public int getNbItemsChecked(){
		int size = contactsList.getAdapter().getCount();
		int nb = 0;
		for(int i=0; i<size; i++) {
			if(contactsList.isItemChecked(i)) {
				nb ++;
			}
		}
		return nb;
	}

	public void enabledDeleteButton(Boolean enabled){
		if(enabled){
			delete.setEnabled(true);
			delete.setAlpha(1f);
		} else {
			if (getNbItemsChecked() == 0){
				delete.setEnabled(false);
				delete.setAlpha(0.2f);
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if (id == R.id.select_all) {
			deselectAll.setVisibility(View.VISIBLE);
			selectAll.setVisibility(View.GONE);
			enabledDeleteButton(true);
			selectAllList(true);
			return;
		}
		if (id == R.id.deselect_all) {
			deselectAll.setVisibility(View.GONE);
			selectAll.setVisibility(View.VISIBLE);
			enabledDeleteButton(false);
			selectAllList(false);
			return;
		}

		if (id == R.id.cancel) {
			quitEditMode();
			return;
		}

		if (id == R.id.delete) {
			final Dialog dialog = LinphoneActivity.instance().displayDialog(getString(R.string.delete_text));
			Button delete = (Button) dialog.findViewById(R.id.delete);
			Button cancel = (Button) dialog.findViewById(R.id.cancel);

			delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					removeContacts();
					dialog.dismiss();
					quitEditMode();
				}
			});

			cancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					dialog.dismiss();
					quitEditMode();
				}
			});
			dialog.show();
			return;
		}

		if (id == R.id.edit) {
			editList.setVisibility(View.VISIBLE);
			topbar.setVisibility(View.GONE);
			enabledDeleteButton(false);
			isEditMode = true;
		}
		
		if (id == R.id.all_contacts) {
			System.out.println("all_contacts");
			allContacts.setImageResource(R.drawable.phone);
			linphoneContacts.setImageResource(R.drawable.asc_contact);
			newContact1.setImageResource(R.drawable.dealer);
			customercontacts.setImageResource(R.drawable.customer);
					
			linphoneContactsSelected.setVisibility(View.INVISIBLE);
			dealerContctsSelected.setVisibility(View.INVISIBLE);
			customerContactsSelected.setVisibility(View.INVISIBLE);			
			allContactsSelected.setVisibility(View.VISIBLE);
			onlyDisplayLinphoneContacts = false;
			onlyDisplayDealerContacts=false;
			onlyDisplaycustomerContacts =false;
			
			allContacts.setEnabled(false);
			linphoneContacts.setEnabled(true);
			newContact1.setEnabled(true);
			customercontacts.setEnabled(true);
			
			
		} 
		else if (id == R.id.linphone_contacts) {
			System.out.println("linphone_contacts");
			
			allContacts.setImageResource(R.drawable.phonenew);
			linphoneContacts.setImageResource(R.drawable.ascnew);
			newContact1.setImageResource(R.drawable.dealer);
			customercontacts.setImageResource(R.drawable.customer);
			
			allContactsSelected.setVisibility(View.INVISIBLE);
			dealerContctsSelected.setVisibility(View.INVISIBLE);
			customerContactsSelected.setVisibility(View.INVISIBLE);	
			linphoneContactsSelected.setVisibility(View.VISIBLE);
			
			linphoneContacts.setEnabled(false);
			allContacts.setEnabled(true);
			newContact1.setEnabled(true);
			customercontacts.setEnabled(true);
			
			onlyDisplayLinphoneContacts = true;
			onlyDisplayDealerContacts=false;			
			onlyDisplaycustomerContacts =false;

		}else if (id == R.id.newContact1) {
			System.out.println("newContact1");
			allContacts.setImageResource(R.drawable.phonenew);
			linphoneContacts.setImageResource(R.drawable.asc_contact);
			newContact1.setImageResource(R.drawable.dealernew);
			customercontacts.setImageResource(R.drawable.customer);
			
			allContactsSelected.setVisibility(View.INVISIBLE);
			linphoneContactsSelected.setVisibility(View.INVISIBLE);
			customerContactsSelected.setVisibility(View.INVISIBLE);
			dealerContctsSelected.setVisibility(View.VISIBLE);
			
			newContact1.setEnabled(false);
			allContacts.setEnabled(true);
			linphoneContacts.setEnabled(true);
			customercontacts.setEnabled(true);
			
			onlyDisplayDealerContacts=true;
			onlyDisplayLinphoneContacts = false;
			onlyDisplaycustomerContacts =false;
			

		}
		else if (id == R.id.customer) {
			System.out.println("customer");
			allContacts.setImageResource(R.drawable.phonenew);
			linphoneContacts.setImageResource(R.drawable.asc_contact);
			newContact1.setImageResource(R.drawable.dealer);
			customercontacts.setImageResource(R.drawable.customernew);
			
			allContactsSelected.setVisibility(View.INVISIBLE);
			linphoneContactsSelected.setVisibility(View.INVISIBLE);
			dealerContctsSelected.setVisibility(View.INVISIBLE);
			customerContactsSelected.setVisibility(View.VISIBLE);
			
			newContact1.setEnabled(true);
			allContacts.setEnabled(true);
			linphoneContacts.setEnabled(true);
			customercontacts.setEnabled(false);
			onlyDisplayDealerContacts=false;
			
			onlyDisplayLinphoneContacts = false;
			onlyDisplaycustomerContacts =true;

		}

		if(isEditMode){
			deselectAll.setVisibility(View.GONE);
			selectAll.setVisibility(View.VISIBLE);
		}

		if (searchField.getText().toString().length() > 0) {
			searchContacts();
		} else {
			changeContactsAdapter();
		}

		if (id == R.id.newContact) {
			editConsumed = true;
			LinphoneActivity.instance().addContact(null, sipAddressToAdd);
		} 
		else if (id == R.id.clearSearchField) {
			searchField.setText("");
		}
	}

	private void selectAllList(boolean isSelectAll){
		int size = contactsList.getAdapter().getCount();
		for(int i=0; i<size; i++) {
			contactsList.setItemChecked(i,isSelectAll);
		}
	}

	private void deleteExistingContact(Contact contact) {
		String select = ContactsContract.Data.CONTACT_ID + " = ?";
		String[] args = new String[] { contact.getID() };

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
						.withSelection(select, args)
						.build()
		);

		try {
			getActivity().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
			ContactsManager.getInstance().removeAllFriends(contact);
		} catch (Exception e) {
			Log.w(e.getMessage() + ":" + e.getStackTrace());
		}
	}

	private void removeContacts(){
		int size = contactsList.getAdapter().getCount();
		for(int i=0; i<size; i++) {
			if(contactsList.isItemChecked(i)){
				Contact contact = (Contact) contactsList.getAdapter().getItem(i);
				deleteExistingContact(contact);
				ContactsManager.getInstance().removeContactFromLists(getActivity().getContentResolver(), contact);
			}
		}
	}

	public void quitEditMode(){
		isEditMode = false;
		editList.setVisibility(View.GONE);
		topbar.setVisibility(View.VISIBLE);
		invalidate();
	}

	private void searchContacts() {
		searchContacts(searchField.getText().toString());
	}

	private void searchContacts(String search) {
		if (search == null || search.length() == 0) {
			changeContactsAdapter();
			return;
		}
		changeContactsToggle();
		
		if (searchCursor != null) {
			searchCursor.close();
		}

		if(LinphoneActivity.instance().getResources().getBoolean(R.bool.use_linphone_friend)) {
			//searchCursor = Compatibility.getSIPContactsCursor(getActivity().getContentResolver(), search, ContactsManager.getInstance().getContactsId());
			//indexer = new AlphabetIndexer(searchCursor, Compatibility.getCursorDisplayNameColumnIndex(searchCursor), " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
			//contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
			//contactsList.setAdapter(new ContactsListAdapter(null, searchCursor));
		} else{
			if (onlyDisplayLinphoneContacts) {
				searchCursor = Compatibility.getSIPContactsCursor(getActivity().getContentResolver(), search, ContactsManager.getInstance().getContactsId());
				indexer = new AlphabetIndexer(searchCursor, Compatibility.getCursorDisplayNameColumnIndex(searchCursor), " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
				contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
				contactsList.setAdapter(new ContactsListAdapter(null, searchCursor));
			} else if(onlyDisplayDealerContacts){
				
				searchField.setText("");				
				//searchCursor = null;
				contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
				indexer = new AlphabetIndexer(searchCursor, Compatibility.getCursorDisplayNameColumnIndex(searchCursor), " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
				contactsList.setAdapter(new ContactsListAdapter(null, searchCursor));
				
			}else if(onlyDisplaycustomerContacts){
				
				//ssearchCursor = null;
				searchField.setText("");
				contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
				indexer = new AlphabetIndexer(searchCursor, Compatibility.getCursorDisplayNameColumnIndex(searchCursor), " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
				contactsList.setAdapter(new ContactsListAdapter(null, searchCursor));
				
			}
			else{
				searchCursor = Compatibility.getContactsCursor(getActivity().getContentResolver(), search, ContactsManager.getInstance().getContactsId());
				contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
				indexer = new AlphabetIndexer(searchCursor, Compatibility.getCursorDisplayNameColumnIndex(searchCursor), " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
				contactsList.setAdapter(new ContactsListAdapter(null, searchCursor));
			}
		}
	}
	
	private void changeContactsAdapter() {
		changeContactsToggle();
		
		if (searchCursor != null) {
			searchCursor.close();
		}
		
		Cursor allContactsCursor = ContactsManager.getInstance().getAllContactsCursor();
		Cursor sipContactsCursor = ContactsManager.getInstance().getSIPContactsCursor();

		noSipContact.setVisibility(View.GONE);
		noContact.setVisibility(View.GONE);
		contactsList.setVisibility(View.VISIBLE);

		if(LinphoneActivity.instance().getResources().getBoolean(R.bool.use_linphone_friend)) {
			indexer = new AlphabetIndexer(allContactsCursor, Compatibility.getCursorDisplayNameColumnIndex(allContactsCursor), " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
			contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
			contactsList.setAdapter(new ContactsListAdapter(ContactsManager.getInstance().getAllContacts(), allContactsCursor));
		} else {
			if (onlyDisplayLinphoneContacts) {
				if (sipContactsCursor != null && sipContactsCursor.getCount() == 0) {
					noSipContact.setVisibility(View.VISIBLE);
					contactsList.setVisibility(View.GONE);
					searchField.setVisibility(View.GONE);
					
				} else {
					searchField.setVisibility(View.VISIBLE);
					clearSearchField.setVisibility(View.VISIBLE);
					indexer = new AlphabetIndexer(sipContactsCursor, Compatibility.getCursorDisplayNameColumnIndex(sipContactsCursor), " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
					contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
					contactsList.setAdapter(new ContactsListAdapter(ContactsManager.getInstance().getSIPContacts(), sipContactsCursor));
				}
				noDealerContact.setVisibility(View.GONE);
				noCustomer.setVisibility(View.GONE);
			} else if (onlyDisplayDealerContacts) {
				
					noSipContact.setVisibility(View.GONE);
					contactsList.setVisibility(View.GONE);
					noCustomer.setVisibility(View.GONE);
					noDealerContact.setVisibility(View.VISIBLE);
					searchField.setVisibility(View.GONE);
					clearSearchField.setVisibility(View.GONE);
				
			} else if (onlyDisplaycustomerContacts) {
				
				noSipContact.setVisibility(View.GONE);
				contactsList.setVisibility(View.GONE);
				noCustomer.setVisibility(View.VISIBLE);
				noDealerContact.setVisibility(View.GONE);
				searchField.setVisibility(View.GONE);
				clearSearchField.setVisibility(View.GONE);
			
		}else{
				if (allContactsCursor != null && allContactsCursor.getCount() == 0) {
					noContact.setVisibility(View.VISIBLE);
					contactsList.setVisibility(View.GONE);
					searchField.setVisibility(View.GONE);
				} else {
					searchField.setVisibility(View.VISIBLE);
					clearSearchField.setVisibility(View.VISIBLE);
					indexer = new AlphabetIndexer(allContactsCursor, Compatibility.getCursorDisplayNameColumnIndex(allContactsCursor), " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
					contactsList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
					contactsList.setAdapter(new ContactsListAdapter(ContactsManager.getInstance().getAllContacts(), allContactsCursor));
				}noDealerContact.setVisibility(View.GONE);
				noCustomer.setVisibility(View.GONE);
			}
		}
		ContactsManager.getInstance().setLinphoneContactsPrefered(onlyDisplayLinphoneContacts);
	}
	
	private void changeContactsToggle() {
		if (onlyDisplayLinphoneContacts) {
			allContacts.setEnabled(true);
			newContact1.setEnabled(true);
			allContactsSelected.setVisibility(View.INVISIBLE);
			linphoneContacts.setEnabled(false);
			linphoneContactsSelected.setVisibility(View.VISIBLE);
		} else if (onlyDisplayDealerContacts) {
			
			allContacts.setEnabled(true);
			allContactsSelected.setVisibility(View.INVISIBLE);
			newContact1.setEnabled(false);
			linphoneContacts.setEnabled(true);
			linphoneContactsSelected.setVisibility(View.INVISIBLE);
		} else if (onlyDisplaycustomerContacts) {
			
			allContacts.setEnabled(true);
			allContactsSelected.setVisibility(View.INVISIBLE);
			newContact1.setEnabled(true);
			customercontacts.setEnabled(false);
			linphoneContacts.setEnabled(true);
			linphoneContactsSelected.setVisibility(View.INVISIBLE);
		}else{
			allContacts.setEnabled(false);
			allContactsSelected.setVisibility(View.VISIBLE);
			linphoneContacts.setEnabled(true);
			newContact1.setEnabled(true);
			linphoneContactsSelected.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Contact contact = (Contact) adapter.getItemAtPosition(position);
		if (editOnClick) {
			editConsumed = true;
			LinphoneActivity.instance().editContact(contact, sipAddressToAdd);
		} else {
			lastKnownPosition = contactsList.getFirstVisiblePosition();
			LinphoneActivity.instance().displayContact(contact, onlyDisplayChatAddress);
		}
	}
	
	@Override
	public void onResume() {
		instance = this;
		super.onResume();

		if (editConsumed) {
			editOnClick = false;
			sipAddressToAdd = null;
		}

		if (LinphoneActivity.isInstanciated()) {
			LinphoneActivity.instance().selectMenu(FragmentsAvailable.CONTACTS_LIST);
			LinphoneActivity.instance().hideTabBar(false);
			onlyDisplayLinphoneContacts = ContactsManager.getInstance().isLinphoneContactsPrefered();
		}
		changeContactsToggle();

		invalidate();
	}
	
	@Override
	public void onPause() {
		instance = null;
		if (searchCursor != null) {
			searchCursor.close();
		}
		super.onPause();
	}
	
	public void invalidate() {
		if (searchField != null && searchField.getText().toString().length() > 0) {
			searchContacts(searchField.getText().toString());
		} else {
			changeContactsAdapter();
		}
		contactsList.setSelectionFromTop(lastKnownPosition, 0);
	}
	
	class ContactsListAdapter extends BaseAdapter implements SectionIndexer {
		private int margin;
		private Bitmap bitmapUnknown;
		private List<Contact> contacts;
		private Cursor cursor;
		
		ContactsListAdapter(List<Contact> contactsList, Cursor c) {
			contacts = contactsList;
			cursor = c;

			margin = LinphoneUtils.pixelsToDpi(LinphoneActivity.instance().getResources(), 10);
			bitmapUnknown = BitmapFactory.decodeResource(LinphoneActivity.instance().getResources(), R.drawable.avatar);
		}
		
		public int getCount() {
			if(LinphoneActivity.instance().getResources().getBoolean(R.bool.use_linphone_friend)) {
				return LinphoneManager.getLc().getFriendList().length;
			} else {
				return cursor.getCount();
			}
		}

		public Object getItem(int position) {
			if (contacts == null || position >= contacts.size()) {
				return Compatibility.getContact(getActivity().getContentResolver(), cursor, position);
			} else {
				return contacts.get(position);
			}
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {
			
			
			View view = null;
			Contact contact = null;
			do {
				contact = (Contact) getItem(position);
			} while (contact == null);
			
			if (convertView != null) {
				view = convertView;
			} else {
				view = mInflater.inflate(R.layout.contact_cell, parent, false);
			}

			CheckBox delete = (CheckBox) view.findViewById(R.id.delete);
			
			TextView name = (TextView) view.findViewById(R.id.name);
			name.setText(contact.getName());

			LinearLayout separator = (LinearLayout) view.findViewById(R.id.separator);
			TextView separatorText = (TextView) view.findViewById(R.id.separator_text);
			if (getPositionForSection(getSectionForPosition(position)) != position) {
				separator.setVisibility(View.GONE);
			} else {
				/*separator.setVisibility(View.VISIBLE);
				separatorText.setText(String.valueOf(contact.getName().charAt(0)));*/
			}
			
			ImageView icon = (ImageView) view.findViewById(R.id.contact_picture);
			if (contact.getPhoto() != null) {
				icon.setImageBitmap(contact.getPhoto());
			} else if (contact.getPhotoUri() != null) {
				icon.setImageURI(contact.getPhotoUri());
			} else {
				icon.setImageResource(R.drawable.avatar);
			}

			if (isEditMode) {
				delete.setVisibility(View.VISIBLE);
				delete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
						contactsList.setItemChecked(position, b);
						if(getNbItemsChecked() == getCount()){
							deselectAll.setVisibility(View.VISIBLE);
							selectAll.setVisibility(View.GONE);
							enabledDeleteButton(true);
						} else {
							if(getNbItemsChecked() == 0){
								deselectAll.setVisibility(View.GONE);
								selectAll.setVisibility(View.VISIBLE);
								enabledDeleteButton(false);
							} else {
								deselectAll.setVisibility(View.GONE);
								selectAll.setVisibility(View.VISIBLE);
								enabledDeleteButton(true);
							}
						}
					}
				});
				if(contactsList.isItemChecked(position)) {
					delete.setChecked(true);
				} else {
					delete.setChecked(false);
				}
			} else {
				delete.setVisibility(View.GONE);
			}
			
			ImageView friendStatus = (ImageView) view.findViewById(R.id.friendStatus);
			LinphoneFriend[] friends = LinphoneManager.getLc().getFriendList();
			if (!ContactsManager.getInstance().isContactPresenceDisabled() && friends != null) {
				friendStatus.setVisibility(View.VISIBLE);
				PresenceActivityType presenceActivity = friends[0].getPresenceModel().getActivity().getType();
				if (presenceActivity == PresenceActivityType.Online) {
					friendStatus.setImageResource(R.drawable.led_connected);
				} else if (presenceActivity == PresenceActivityType.Busy) {
					friendStatus.setImageResource(R.drawable.led_error);
				} else if (presenceActivity == PresenceActivityType.Away) {
					friendStatus.setImageResource(R.drawable.led_inprogress);
				} else if (presenceActivity == PresenceActivityType.Offline) {
					friendStatus.setImageResource(R.drawable.led_disconnected);
				} else {
					friendStatus.setImageResource(R.drawable.call_quality_indicator_0);
				}
			}
			
			
			//View view = super.getView(position, convertView, parent);  
			if (position % 2 == 1) {
			    view.setBackgroundColor(Color.WHITE);  
			} else {
			    view.setBackgroundColor(Color.parseColor("#dedede"));  
			}

			//return view;
			//System.out.println("view");
			//System.out.println(view.getId());
			
			
			return view;
		}

		@Override
		public int getPositionForSection(int section) {
			return indexer.getPositionForSection(section);
		}

		@Override
		public int getSectionForPosition(int position) {
			return indexer.getSectionForPosition(position);
		}

		@Override
		public Object[] getSections() {
			return indexer.getSections();
		}
	}
}
