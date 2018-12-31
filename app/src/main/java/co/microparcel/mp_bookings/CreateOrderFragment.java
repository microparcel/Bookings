package co.microparcel.mp_bookings;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreateOrderFragment extends Fragment {

    View v;
    Button fare_estimate_Button;
    private int location_switch, vehicle_switch;
    private final int REQUEST_CODE_PLACEPICKER = 1;
    private BottomNavigationView vehicle_selector_Bar;
    AlertDialog.Builder builder, farebuilder, payment_method;
    private TextView pickup_location_TextView, drop_location_TextView, vehicle_name_and_type_TextView, item_name_TextView, payment_type_name_TextView, is_insurance_there_TextView, loading_unloading_title_TextView;
    private String DistanceResult;
    private String DurationResult;
    private String pickup, drop, bodytype;
    private ImageView payment_method_main_ImageView, select_item_type_ImageView, select_insurance_ImageView, loading_unloading_ImageView;

    public CreateOrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_create_order, container, false);
        select_item_type_ImageView = v.findViewById(R.id.select_item_type_ImageView);
        payment_type_name_TextView = v.findViewById(R.id.payment_type_name_TextView);
        select_insurance_ImageView = v.findViewById(R.id.select_insurance_ImageView);
        is_insurance_there_TextView = v.findViewById(R.id.is_insurance_there_TextView);
        loading_unloading_ImageView = v.findViewById(R.id.loading_unloading_ImageView);
        loading_unloading_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callLoadingUnloading();
            }
        });
        loading_unloading_title_TextView = v.findViewById(R.id.loading_unloading_title_TextView);
        select_insurance_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callInsurance();
            }
        });
        Reset();
        pickup_location_TextView = v.findViewById(R.id.pickup_location_TextView);
        pickup_location_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location_switch = 1;
                startPlacePickerActivity();
            }
        });
        drop_location_TextView = v.findViewById(R.id.drop_location_TextView);
        drop_location_TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location_switch = 2;
                startPlacePickerActivity();
            }
        });

        vehicle_name_and_type_TextView = v.findViewById(R.id.vehicle_name_and_type_TextView);

        vehicle_selector_Bar = v.findViewById(R.id.vehicle_selector_Bar);
        vehicle_selector_Bar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Toast.makeText(getContext(), "" + menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                switch (menuItem.getTitle().toString()) {
                    case "Bike":
                        vehicle_switch = 1;
                        callBike();
                        break;
                    case "Loading":
                        vehicle_switch = 2;
                        callLoading();
                        break;
                    case "Tata Ace":
                        vehicle_switch = 3;
                        callTataAce();
                        break;
                    case "AL Dost":
                        vehicle_switch = 4;
                        callALDost();
                        break;
                    case "M Pickup":
                        vehicle_switch = 5;
                        callMPickup();
                        break;
                }
                return true;
            }
        });

        item_name_TextView = v.findViewById(R.id.item_name_TextView);

        fare_estimate_Button = v.findViewById(R.id.fare_estimate_Button);
        fare_estimate_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(pickup)) {
                    pickup_location_TextView.setError("Select pickup location!");
                    return;
                }

                if (TextUtils.isEmpty(drop)) {
                    drop_location_TextView.setError("Select drop location!");
                    return;
                }
                SearchDistanceCommand(v);
                callFare();
            }
        });

        payment_method_main_ImageView = v.findViewById(R.id.payment_method_main_ImageView);
        payment_method_main_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callPaymentMethod();
            }
        });

        select_item_type_ImageView = v.findViewById(R.id.select_item_type_ImageView);
        select_item_type_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callGoodsList();
            }
        });

        return v;
    }


    public void Reset() {
        DistanceResult = "";
        DurationResult = "";
        //Toast.makeText(getContext(), ""+DistanceResult+" "+DurationResult, Toast.LENGTH_SHORT).show();
    }

    public void SearchDistanceCommand(View view) {

        Reset();

        if (pickup.isEmpty() || drop.isEmpty()) {
            Toast MissingTextErrorHandle = Toast.makeText(getContext(), "You need to input data into both fields!", Toast.LENGTH_SHORT);
            MissingTextErrorHandle.show();
        } else {
            new AsyncTaskParseJson().execute();
        }
    }

    public class AsyncTaskParseJson extends AsyncTask<String, String, String> {

        String FormattedStartLocation = pickup.replaceAll(" ", "+");
        String FormattedGoalLocation = drop.replaceAll(" ", "+");

        String yourServiceUrl = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric&origins=" + FormattedStartLocation + "&destinations=" + FormattedGoalLocation
                + "&key=AIzaSyA609lXN9_qcwJiHpo02CTuB0oXGxDDumo";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {
            try {

                httpConnect jParser = new httpConnect();
                String json = jParser.getJSONFromUrl(yourServiceUrl);
                JSONObject object = new JSONObject(json);

                //contains ALL routes
                JSONArray array = object.getJSONArray("rows");
                // Get the first route
                JSONObject route = array.getJSONObject(0);
                // Take all elements
                JSONArray elements = route.getJSONArray("elements");
                //Tale First Element
                JSONObject element = elements.getJSONObject(0);

                // Get Duration
                JSONObject durationObject = element.getJSONObject("duration");
                String duration = durationObject.getString("text");
                DurationResult = duration;

                // Get Distance
                JSONObject distanceObject = element.getJSONObject("distance");
                String distance = distanceObject.getString("text");
                DistanceResult = distance;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {

            if (DistanceResult == null || DurationResult == null) {
                Toast ResultErrorHandle = Toast.makeText(getContext(), "We could not find any results! Sorry!", Toast.LENGTH_SHORT);
                ResultErrorHandle.show();
            }

            // ProgressBar spinner;
            // spinner = (ProgressBar) findViewById(R.id.progressBar1);
            // spinner.setVisibility(View.INVISIBLE);


            if (DistanceResult.indexOf("km") != -1) {
                DistanceResult = DistanceResult.replaceAll("[^\\d.]", "");
                Toast.makeText(getContext(), DistanceResult + " " + DurationResult, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Can't serve distance under 100 meters.", Toast.LENGTH_SHORT).show();
            }


        }
    }


    private void callBike() {

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View mView = layoutInflaterAndroid.inflate(R.layout.select_vehicle_layout, null);
        builder = new AlertDialog.Builder(getContext());
        builder.setView(mView);
        ImageView vehicle_imageView = mView.findViewById(R.id.vehicle_imageView);
        vehicle_imageView.setImageResource(R.drawable.ic_bike_delivery);
        final Button close_alert_Button = mView.findViewById(R.id.close_alert_Button);
        final TextView vehicle_capacity_TextView, vehicle_width, vehicle_height, vehicle_lenght, vehicle_name_TextView;
        vehicle_capacity_TextView = mView.findViewById(R.id.vehicle_capacity_TextView);
        vehicle_width = mView.findViewById(R.id.vehicle_width);
        vehicle_height = mView.findViewById(R.id.vehicle_height);
        vehicle_lenght = mView.findViewById(R.id.vehicle_lenght);
        vehicle_name_TextView = mView.findViewById(R.id.vehicle_name_TextView);
        vehicle_name_TextView.setText(getString(R.string.bike_del));
        RadioGroup body_type_radioGroup = mView.findViewById(R.id.body_type_radioGroup);
        vehicle_capacity_TextView.setText(getString(R.string.bike_cap));
        vehicle_width.setText(getString(R.string.bike_width));
        vehicle_height.setText(getString(R.string.bike_height));
        vehicle_lenght.setText(getString(R.string.bike_length));
        body_type_radioGroup.setVisibility(View.INVISIBLE);
        final AlertDialog dialog = builder.create();
        dialog.show();
        select_item_type_ImageView.setEnabled(false);

        dialog.setCancelable(false);
        close_alert_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vehicle_name_and_type_TextView.setText("Delivery Bike");
                dialog.dismiss();
            }
        });

    }

    private void callLoading() {

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View mView = layoutInflaterAndroid.inflate(R.layout.select_vehicle_layout, null);
        builder = new AlertDialog.Builder(getContext());
        builder.setView(mView);
        ImageView vehicle_imageView = mView.findViewById(R.id.vehicle_imageView);
        vehicle_imageView.setImageResource(R.drawable.ic_loading_delivery);
        final Button close_alert_Button = mView.findViewById(R.id.close_alert_Button);
        final TextView vehicle_capacity_TextView, vehicle_width, vehicle_height, vehicle_lenght, vehicle_name_TextView;
        final RadioGroup body_type_radioGroup;
        RadioButton bodyRadio;
        select_item_type_ImageView.setEnabled(true);
        vehicle_capacity_TextView = mView.findViewById(R.id.vehicle_capacity_TextView);
        vehicle_width = mView.findViewById(R.id.vehicle_width);
        vehicle_height = mView.findViewById(R.id.vehicle_height);
        vehicle_lenght = mView.findViewById(R.id.vehicle_lenght);
        vehicle_name_TextView = mView.findViewById(R.id.vehicle_name_TextView);
        vehicle_name_TextView.setText("Loading Rickshaw");
        body_type_radioGroup = mView.findViewById(R.id.body_type_radioGroup);
        body_type_radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.open_body_radioButton :
                        bodytype = "with Open Body";
                        Toast.makeText(getContext(), "with Open Body", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.closed_body_radioButton :
                        bodytype = "with Closed Body";
                        Toast.makeText(getContext(), "with Closed Body", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });
        vehicle_capacity_TextView.setText("Capacity: 0.50 tons or 500 kgs");
        vehicle_width.setText("Width: 4.50 ft");
        vehicle_height.setText("Height: 5.50 ft");
        vehicle_lenght.setText("Length: 6 ft");
        body_type_radioGroup.setVisibility(View.VISIBLE);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        close_alert_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (body_type_radioGroup.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getContext(), "Please select body type !", Toast.LENGTH_SHORT).show();
                    body_type_radioGroup.setBackground(getResources().getDrawable(R.drawable.nogravity_border_red));
                }else {
                    vehicle_name_and_type_TextView.setText("Loading Rickshaw");
                    dialog.dismiss();
                }
            }
        });

    }

    private void callTataAce() {

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View mView = layoutInflaterAndroid.inflate(R.layout.select_vehicle_layout, null);
        builder = new AlertDialog.Builder(getContext());
        builder.setView(mView);
        select_item_type_ImageView.setEnabled(true);
        ImageView vehicle_imageView = mView.findViewById(R.id.vehicle_imageView);
        vehicle_imageView.setImageResource(R.drawable.ic_ace_delivery);
        final Button close_alert_Button = mView.findViewById(R.id.close_alert_Button);
        final TextView vehicle_capacity_TextView, vehicle_width, vehicle_height, vehicle_lenght, vehicle_name_TextView;
        final RadioGroup body_type_radioGroup;
        RadioButton closed_body_radioButton, open_body_radioButton;
        vehicle_capacity_TextView = mView.findViewById(R.id.vehicle_capacity_TextView);
        vehicle_width = mView.findViewById(R.id.vehicle_width);
        vehicle_height = mView.findViewById(R.id.vehicle_height);
        vehicle_lenght = mView.findViewById(R.id.vehicle_lenght);
        vehicle_name_TextView = mView.findViewById(R.id.vehicle_name_TextView);
        vehicle_name_TextView.setText("Tata Ace");
        body_type_radioGroup = mView.findViewById(R.id.body_type_radioGroup);
        body_type_radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.open_body_radioButton :
                        bodytype = "with Open Body";
                        Toast.makeText(getContext(), "with Open Body", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.closed_body_radioButton :
                        bodytype = "with Closed Body";
                        Toast.makeText(getContext(), "with Closed Body", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });
        vehicle_capacity_TextView.setText("Capacity: 0.75 tons or 750 kgs");
        vehicle_width.setText("Width: 5.00 ft");
        vehicle_height.setText("Height: 6.00 ft");
        vehicle_lenght.setText("Length: 7.00 ft");
        body_type_radioGroup.setVisibility(View.VISIBLE);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        close_alert_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (body_type_radioGroup.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getContext(), "Please select body type !", Toast.LENGTH_SHORT).show();
                    body_type_radioGroup.setBackground(getResources().getDrawable(R.drawable.nogravity_border_red));
                }else {
                    vehicle_name_and_type_TextView.setText("Tata Ace");
                    dialog.dismiss();
                }
            }
        });


    }

    private void callALDost() {

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View mView = layoutInflaterAndroid.inflate(R.layout.select_vehicle_layout, null);
        builder = new AlertDialog.Builder(getContext());
        builder.setView(mView);
        select_item_type_ImageView.setEnabled(true);
        ImageView vehicle_imageView = mView.findViewById(R.id.vehicle_imageView);
        vehicle_imageView.setImageResource(R.drawable.ic_dost_delivery);
        final Button close_alert_Button = mView.findViewById(R.id.close_alert_Button);
        final TextView vehicle_capacity_TextView, vehicle_width, vehicle_height, vehicle_lenght, vehicle_name_TextView;
        final RadioGroup body_type_radioGroup;
        RadioButton closed_body_radioButton, open_body_radioButton;
        vehicle_capacity_TextView = mView.findViewById(R.id.vehicle_capacity_TextView);
        vehicle_width = mView.findViewById(R.id.vehicle_width);
        vehicle_height = mView.findViewById(R.id.vehicle_height);
        vehicle_lenght = mView.findViewById(R.id.vehicle_lenght);
        vehicle_name_TextView = mView.findViewById(R.id.vehicle_name_TextView);
        vehicle_name_TextView.setText("AL Dost");
        body_type_radioGroup = mView.findViewById(R.id.body_type_radioGroup);
        body_type_radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.open_body_radioButton :
                        bodytype = "with Open Body";
                        Toast.makeText(getContext(), "with Open Body", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.closed_body_radioButton :
                        bodytype = "with Closed Body";
                        Toast.makeText(getContext(), "with Closed Body", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });
        vehicle_capacity_TextView.setText("Capacity: 1.25 tons or 1250 kgs");
        vehicle_width.setText("Width: 5.30 ft");
        vehicle_height.setText("Height: 6.90 ft");
        vehicle_lenght.setText("Length: 8.00 ft");
        body_type_radioGroup.setVisibility(View.VISIBLE);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        close_alert_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (body_type_radioGroup.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getContext(), "Please select body type !", Toast.LENGTH_SHORT).show();
                    body_type_radioGroup.setBackground(getResources().getDrawable(R.drawable.nogravity_border_red));
                }else {
                    vehicle_name_and_type_TextView.setText("AL Dost");
                    dialog.dismiss();
                }
            }
        });


    }

    private void callMPickup() {

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View mView = layoutInflaterAndroid.inflate(R.layout.select_vehicle_layout, null);
        builder = new AlertDialog.Builder(getContext());
        builder.setView(mView);
        select_item_type_ImageView.setEnabled(true);
        ImageView vehicle_imageView = mView.findViewById(R.id.vehicle_imageView);
        vehicle_imageView.setImageResource(R.drawable.ic_pickup_delivery);
        final Button close_alert_Button = mView.findViewById(R.id.close_alert_Button);
        final TextView vehicle_capacity_TextView, vehicle_width, vehicle_height, vehicle_lenght, vehicle_name_TextView;
        final RadioGroup body_type_radioGroup;
        RadioButton bodyTypeRadio;
        vehicle_capacity_TextView = mView.findViewById(R.id.vehicle_capacity_TextView);
        vehicle_width = mView.findViewById(R.id.vehicle_width);
        vehicle_height = mView.findViewById(R.id.vehicle_height);
        vehicle_lenght = mView.findViewById(R.id.vehicle_lenght);
        vehicle_name_TextView = mView.findViewById(R.id.vehicle_name_TextView);
        vehicle_name_TextView.setText("M Pickup");
        body_type_radioGroup = mView.findViewById(R.id.body_type_radioGroup);
        body_type_radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.open_body_radioButton :
                        bodytype = "with Open Body";
                        body_type_radioGroup.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        Toast.makeText(getContext(), "with Open Body", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.closed_body_radioButton :
                        bodytype = "with Closed Body";
                        body_type_radioGroup.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                        Toast.makeText(getContext(), "with Closed Body", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        });
        vehicle_capacity_TextView.setText("Capacity: 1.35 tons or 1350 kgs");
        vehicle_width.setText("Width: 5.30 ft");
        vehicle_height.setText("Height: 6.90 ft");
        vehicle_lenght.setText("Length: 8.00 ft");
        body_type_radioGroup.setVisibility(View.VISIBLE);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        close_alert_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (body_type_radioGroup.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getContext(), "Please select body type !", Toast.LENGTH_SHORT).show();
                    body_type_radioGroup.setBackground(getResources().getDrawable(R.drawable.nogravity_border_red));
                }else {
                    vehicle_name_and_type_TextView.setText("M Pickup");
                    dialog.dismiss();
                }

            }
        });


    }

    private void startPlacePickerActivity() {
        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
        // this would only work if you have your Google Places API working

        try {
            Intent intent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                intent = intentBuilder.build(Objects.requireNonNull(getActivity()));
            }
            startActivityForResult(intent, REQUEST_CODE_PLACEPICKER);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displaySelectedPlaceFromPlacePicker(Intent data) {
        Place placeSelected = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            placeSelected = PlacePicker.getPlace(data, Objects.requireNonNull(getContext()));
        }

        String name = placeSelected.getName().toString();
        String address = placeSelected.getAddress().toString();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            address = Objects.requireNonNull(placeSelected.getAddress()).toString();
        }
        if (location_switch == 1) {
            pickup_location_TextView.setText(name);
            pickup = address;
        }
        if (location_switch == 2) {
            drop_location_TextView.setText(name);
            drop = address;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PLACEPICKER && resultCode == RESULT_OK) {
            displaySelectedPlaceFromPlacePicker(data);
        }
    }

    private void callFare() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View mView = layoutInflater.inflate(R.layout.estimate_fare_alert, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            farebuilder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
        }
        farebuilder.setView(mView);
        final AlertDialog dialog = farebuilder.create();
        dialog.show();
    }

    private void callPaymentMethod(){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View mView = layoutInflater.inflate(R.layout.payment_method_alert, null);
        builder = new AlertDialog.Builder(getContext());
        builder.setView(mView);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        final ImageView cop_ImageView, cod_ImageView, onlinepayment_ImageView;
        cod_ImageView = mView.findViewById(R.id.cod_ImageView);
        cop_ImageView = mView.findViewById(R.id.cop_ImageView);
        onlinepayment_ImageView = mView.findViewById(R.id.onlinepayment_ImageView);
        cop_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              payment_method_main_ImageView.setImageResource(R.drawable.ic_funds);
              payment_type_name_TextView.setText("Cash on Pickup");
              dialog.dismiss();
            }
        });

        cod_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payment_method_main_ImageView.setImageResource(R.drawable.ic_funds);
                payment_type_name_TextView.setText("Cash on Delivery");
                dialog.dismiss();
            }
        });

        onlinepayment_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payment_method_main_ImageView.setImageResource(R.drawable.ic_credit);
                payment_type_name_TextView.setText("Online Payments");
                dialog.dismiss();
            }
        });

    }

    private void callGoodsList(){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View mView = layoutInflater.inflate(R.layout.select_material_type, null);
        builder = new AlertDialog.Builder(getContext());
        builder.setView(mView);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        // Array of strings for ListView Title
        final String[] listviewTitle = new String[]{
                "Furniture", "Food & Beverages", "House Shifting", "Machines/Equip./SpareParts", "Wood/Timber/Plywood",
                "Courier/Packers and Movers", "Vehicles/Automotive Parts", "Chemicals/Paints/Oils", "Tiles/Ceramics/Sanitaryware",
                "Glassware", "Pipes/Metal Rods > 7ft", "Pipes/Metal Rods < 7ft", "Metal Sheets",
                "Gas/Commercial Cylinder", "Construction Materials", "Garments/Apparel/Textile", "Electrical/Electronics"


        };


        final int[] listviewImage = new int[]{
                R.drawable.ic_bed, R.drawable.ic_fast_food, R.drawable.ic_insurance, R.drawable.ic_assembly, R.drawable.ic_fence,
                R.drawable.ic_boxg, R.drawable.ic_car, R.drawable.ic_petroleum, R.drawable.ic_toilet, R.drawable.ic_cheers,
                R.drawable.ic_pipe, R.drawable.ic_pipe, R.drawable.ic_bending, R.drawable.ic_gas, R.drawable.ic_brick, R.drawable.ic_fashion,
                R.drawable.ic_responsive
        };


        List<HashMap<String, String>> aList = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < 17; i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put("listview_title", listviewTitle[i]);
            hm.put("listview_image", Integer.toString(listviewImage[i]));
            aList.add(hm);
        }

        String[] from = {"listview_image", "listview_title"};
        int[] to = {R.id.item_image, R.id.item_title};

        SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(), aList, R.layout.custom_item_layout, from, to);
        final ListView androidListView = (ListView) mView.findViewById(R.id.items_ListView);
        androidListView.setAdapter(simpleAdapter);
        androidListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String,String> map =(HashMap<String,String>)androidListView.getItemAtPosition(position);
                String itemname = map.get("listview_title");
                item_name_TextView.setText(itemname    );
                select_item_type_ImageView.setImageResource(listviewImage[position]);
                dialog.dismiss();
            }
        });
    }

    private void callInsurance(){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View mView = layoutInflater.inflate(R.layout.insurance_option_alert, null);
        builder = new AlertDialog.Builder(getContext());
        builder.setView(mView);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(false);
        final ImageView no_insurance_ImageView, yes_insurance_ImageView;
        no_insurance_ImageView = mView.findViewById(R.id.no_insurance_ImageView);
        yes_insurance_ImageView = mView.findViewById(R.id.yes_insurance_ImageView);
        no_insurance_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_insurance_ImageView.setImageResource(R.drawable.ic_no_insurance);
                is_insurance_there_TextView.setText("No Insurance");
                dialog.dismiss();
            }
        });

        yes_insurance_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_insurance_ImageView.setImageResource(R.drawable.ic_yes_insurance);
                is_insurance_there_TextView.setText("Yes Insurance");
                dialog.dismiss();
            }
        });
    }

    private void callLoadingUnloading(){
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View mView = layoutInflater.inflate(R.layout.loading_unloading_alert, null);
        builder = new AlertDialog.Builder(getContext());
        builder.setView(mView);
        final AlertDialog dialog = builder.create();
        dialog.show();
        ImageView nolu_ImageView, l_ImageView, u_ImageView, l_u_ImageView;
        nolu_ImageView = mView.findViewById(R.id.nolu_ImageView);
        l_ImageView = mView.findViewById(R.id.l_ImageView);
        u_ImageView = mView.findViewById(R.id.u_ImageView);
        l_u_ImageView = mView.findViewById(R.id.l_u_ImageView);
        nolu_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading_unloading_ImageView.setImageResource(R.drawable.ic_boxg);
                loading_unloading_title_TextView.setText("No Loading/Unlaoding");
                dialog.dismiss();
            }
        });
        l_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading_unloading_ImageView.setImageResource(R.drawable.ic_loading);
                loading_unloading_title_TextView.setText("Loading Only");
                dialog.dismiss();
            }
        });
        u_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading_unloading_ImageView.setImageResource(R.drawable.ic_loading);
                loading_unloading_title_TextView.setText("Unloading Only");
                dialog.dismiss();
            }
        });
        l_u_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading_unloading_ImageView.setImageResource(R.drawable.ic_loading);
                loading_unloading_title_TextView.setText("Laoding and Unloading");
                dialog.dismiss();
            }
        });

    }

}
