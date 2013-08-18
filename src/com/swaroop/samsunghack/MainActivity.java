package com.swaroop.samsunghack;

import java.io.IOException;
import java.util.List;
import com.immersion.uhl.Launcher;

import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.immersion.uhl.Launcher;

public class MainActivity extends FragmentActivity implements OnMarkerClickListener, OnMyLocationChangeListener {
	
	GoogleMap googleMap;
	MarkerOptions markerOptions;
	LatLng latLng;
	LatLng curLoc;
	float[] dist = new float[1];
	protected Launcher mLauncher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mLauncher = new Launcher(this);
		
		SupportMapFragment supportMapFragment = (SupportMapFragment) 
				getSupportFragmentManager().findFragmentById(R.id.map);

		// Getting a reference to the map
		googleMap = supportMapFragment.getMap();
		
		// Getting reference to btn_find of the layout activity_main
        Button btn_find = (Button) findViewById(R.id.btn_find);
        
        // Defining button click event listener for the find button
        OnClickListener findClickListener = new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// Getting reference to EditText to get the user input location
				EditText etLocation = (EditText) findViewById(R.id.et_location);
				
				// Getting user input location
				String location = etLocation.getText().toString();
				
				if(location!=null && !location.equals("")){
					new GeocoderTask().execute(location);
				}
			}
		};
		
		// Setting button click event listener for the find button
		btn_find.setOnClickListener(findClickListener);		
		
		googleMap.setOnMarkerClickListener((OnMarkerClickListener) this);
		
		googleMap.setMyLocationEnabled(true);
		googleMap.setOnMyLocationChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public boolean onMarkerClick(Marker marker) {
	    // TODO Auto-generated method stub
		LatLng temp = marker.getPosition();
		
		Polyline line = googleMap.addPolyline(new PolylineOptions()
	    .add(temp, curLoc)
	    .width(5)
	    .color(Color.RED));
        
		return true;
	}
	
	
	// An AsyncTask class for accessing the GeoCoding Web Service
		private class GeocoderTask extends AsyncTask<String, Void, List<Address>>{

			@Override
			protected List<Address> doInBackground(String... locationName) {
				// Creating an instance of Geocoder class
				Geocoder geocoder = new Geocoder(getBaseContext());
				List<Address> addresses = null;
				
				try {
					// Getting a maximum of 3 Address that matches the input text
					addresses = geocoder.getFromLocationName(locationName[0], 3);
				} catch (IOException e) {
					e.printStackTrace();
				}			
				return addresses;
			}
			
			
			@Override
			protected void onPostExecute(List<Address> addresses) {			
		        
		        if(addresses==null || addresses.size()==0){
					Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
				}
		        
		        // Clears all the existing markers on the map
		        googleMap.clear();
				
		        // Adding Markers on Google Map for each matching address
				for(int i=0;i<addresses.size();i++){				
					
					Address address = (Address) addresses.get(i);
					
			        // Creating an instance of GeoPoint, to display in Google Map
			        latLng = new LatLng(address.getLatitude(), address.getLongitude());
			        
			        String addressText = String.format("%s, %s",
	                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
	                        address.getCountryName());

			        markerOptions = new MarkerOptions();
			        markerOptions.position(latLng);
			        markerOptions.title(addressText);

			        googleMap.addMarker(markerOptions);
			        
			        // Locate the first location
			        if(i==0)			        	
						googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng)); 	
				}
			}
		}


		@Override
		public void onMyLocationChange(Location loc) {
			// TODO Auto-generated method stub
			curLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
			CameraUpdate myLoc = CameraUpdateFactory.newCameraPosition(
		            new CameraPosition.Builder().target(new LatLng(loc.getLatitude(),
		                    loc.getLongitude())).zoom(6).build());
			
			googleMap.moveCamera(myLoc);
			googleMap.setOnMyLocationChangeListener(null);	
			
			Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), 37.376961, -121.921738, dist);
			
			if(dist[0] <= 1)
				mLauncher.play(Launcher.DOUBLE_STRONG_CLICK_100);
			else if(dist[0] < 6 && dist[0] > 1)
				mLauncher.play(Launcher.DOUBLE_STRONG_CLICK_66);
			else
				mLauncher.play(Launcher.DOUBLE_BUMP_33);
		}
}
