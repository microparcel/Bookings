package co.microparcel.mp_bookings;


import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONArray;
import org.json.JSONObject;

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
    private TextView pickup_location_TextView, drop_location_TextView, vehicle_name_and_type_TextView;
    private String DistanceResult;
    private String DurationResult;
    private String pickup, drop, bodytype;
    private ImageView payment_method_main_ImageView;

    public CreateOrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_create_order, container, false);
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
       // final Button close_aler  t_Button = mView.findViewById(R.id.close_alert_Button);
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
        vehicle_name_and_type_TextView.setText("Delivery Bike");
        dialog.setCancelable(false);
        /*close_alert_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });*/

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
        RadioGroup body_type_radioGroup;
        RadioButton bodyRadio;
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
                dialog.dismiss();
            }
        });

    }

    private void callTataAce() {

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View mView = layoutInflaterAndroid.inflate(R.layout.select_vehicle_layout, null);
        builder = new AlertDialog.Builder(getContext());
        builder.setView(mView);
        ImageView vehicle_imageView = mView.findViewById(R.id.vehicle_imageView);
        vehicle_imageView.setImageResource(R.drawable.ic_ace_delivery);
        final Button close_alert_Button = mView.findViewById(R.id.close_alert_Button);
        final TextView vehicle_capacity_TextView, vehicle_width, vehicle_height, vehicle_lenght, vehicle_name_TextView;
        RadioGroup body_type_radioGroup;
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
                dialog.dismiss();
            }
        });


    }

    private void callALDost() {

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View mView = layoutInflaterAndroid.inflate(R.layout.select_vehicle_layout, null);
        builder = new AlertDialog.Builder(getContext());
        builder.setView(mView);
        ImageView vehicle_imageView = mView.findViewById(R.id.vehicle_imageView);
        vehicle_imageView.setImageResource(R.drawable.ic_dost_delivery);
        final Button close_alert_Button = mView.findViewById(R.id.close_alert_Button);
        final TextView vehicle_capacity_TextView, vehicle_width, vehicle_height, vehicle_lenght, vehicle_name_TextView;
        RadioGroup body_type_radioGroup;
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
                dialog.dismiss();
            }
        });


    }

    private void callMPickup() {

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View mView = layoutInflaterAndroid.inflate(R.layout.select_vehicle_layout, null);
        builder = new AlertDialog.Builder(getContext());
        builder.setView(mView);
        ImageView vehicle_imageView = mView.findViewById(R.id.vehicle_imageView);
        vehicle_imageView.setImageResource(R.drawable.ic_pickup_delivery);
        final Button close_alert_Button = mView.findViewById(R.id.close_alert_Button);
        final TextView vehicle_capacity_TextView, vehicle_width, vehicle_height, vehicle_lenght, vehicle_name_TextView;
        RadioGroup body_type_radioGroup;
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
                        Toast.makeText(getContext(), "with Open Body", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.closed_body_radioButton :
                        bodytype = "with Closed Body";
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
                dialog.dismiss();
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
        final ImageView cop_ImageView, cod_ImageView, onlinepayment_ImageView;
        cod_ImageView = mView.findViewById(R.id.cod_ImageView);
        cop_ImageView = mView.findViewById(R.id.cop_ImageView);
        onlinepayment_ImageView = mView.findViewById(R.id.onlinepayment_ImageView);
        cop_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              cop_ImageView.setBackground(getResources().getDrawable(R.drawable.nogravity_border_primary));
              cod_ImageView.setBackgroundColor(getResources().getColor(R.color.colorWhite));
              onlinepayment_ImageView.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            }
        });

        cod_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cod_ImageView.setBackground(getResources().getDrawable(R.drawable.nogravity_border_primary));
                cop_ImageView.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                onlinepayment_ImageView.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            }
        });

        onlinepayment_ImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlinepayment_ImageView.setBackground(getResources().getDrawable(R.drawable.nogravity_border_primary));
                cop_ImageView.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                cod_ImageView.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            }
        });
        Button select_payment_method_Button = mView.findViewById(R.id.select_payment_method_Button);
        select_payment_method_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
