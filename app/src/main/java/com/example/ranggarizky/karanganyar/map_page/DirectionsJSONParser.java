package com.example.ranggarizky.karanganyar.map_page;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

import com.example.ranggarizky.karanganyar.R;
import com.example.ranggarizky.karanganyar.model.Leg;
import com.example.ranggarizky.karanganyar.model.RedZone;
import com.example.ranggarizky.karanganyar.model.Route;
import com.example.ranggarizky.karanganyar.model.RouteData;
import com.example.ranggarizky.karanganyar.model.Step;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anupamchugh on 27/11/15.
 */

public class DirectionsJSONParser {

    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public RouteData parse(ArrayList<Route> data,ArrayList<RedZone> redzones){
        ArrayList<MarkerOptions> transMarker = new ArrayList<>();
        ArrayList<Boolean> isDangerous = new ArrayList<>();
        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,String>>>() ;
        //ArrayList jRoutes = null;
        ArrayList<Leg>  legs= new ArrayList<>();
        ArrayList<Step> steps = new ArrayList<>();

        try {

            /** Traversing all routes */
            for(int i=0;i<data.size();i++){
                legs = data.get(i).getLegs();
                List path = new ArrayList<HashMap<String, String>>();
                Boolean dangerous = false;

                /** Traversing all legs */
                for(int j=0;j<legs.size();j++){
                    steps = legs.get(j).getSteps();

                    /** Traversing all steps */
                    for(int k=0;k<steps.size();k++){
                        String polyline = "";
                        polyline = steps.get(k).getPolyline().getPoints();
                        List list = decodePoly(polyline);
                        Log.e("joss",String.valueOf(steps.size()/2));
                        if(k == (steps.size()/2)){
                            LatLng loc = new LatLng(steps.get(k).getStart_location().getLat(),
                                    steps.get(k).getStart_location().getLng());
                            MarkerOptions options = new MarkerOptions();
                            options.position(loc);
                            options.title(legs.get(j).getDuration().getText());
                            options.snippet(legs.get(j).getDistance().getText());
                            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.empty));
                            transMarker.add(options);
                        }

                        /** Traversing all points */
                        for(int l=0;l <list.size();l++){
                            HashMap<String, String> hm = new HashMap<String, String>();
                            Location locationStep = new Location("point A");
                            locationStep.setLatitude(((LatLng)list.get(l)).latitude);
                            locationStep.setLongitude(((LatLng)list.get(l)).longitude);
                            //int lol = 0;
                            for (RedZone redZone : redzones){
                                Location locationRedZone = new Location("point B");
                                locationRedZone.setLatitude(redZone.getLocation().getLat());
                                locationRedZone.setLongitude(redZone.getLocation().getLong());

                               /* if(i == 0 && lol == 2){
                                    Log.e("cek",String.valueOf(redZone.getLocation().getLat()) + " "+
                                            String.valueOf(redZone.getLocation().getLong()));
                                    Log.e("cek",String.valueOf(locationRedZone.distanceTo(locationStep)));
                                }*/

                                 if(locationRedZone.distanceTo(locationStep) < 200  && (!dangerous)){
                                    dangerous = true;

                                }
                               // lol++;

                            }

                            hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                            hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
                isDangerous.add(dangerous);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        RouteData results = new RouteData();
        results.setLineData(routes);
        results.setInfoWindows(transMarker);
        results.setIsDangerous(isDangerous);
        return results;
    }




    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}