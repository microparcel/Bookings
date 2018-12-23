package co.microparcel.mp_bookings;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import q.rorbin.badgeview.QBadgeView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreateOrderFragment extends Fragment {

    View v;
    Button proceed_Button;
    private BottomNavigationView vehicle_selector_Bar;

    public CreateOrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_create_order, container, false);

        vehicle_selector_Bar = v.findViewById(R.id.vehicle_selector_Bar);
        vehicle_selector_Bar.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){
                    case R.id.bike_delivery :
                        Toast.makeText(getContext(), "You have selected Bike Delivery "+menuItem.getItemId(), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });


        proceed_Button = v.findViewById(R.id.proceed_Button);
        proceed_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        return v;
    }

}
