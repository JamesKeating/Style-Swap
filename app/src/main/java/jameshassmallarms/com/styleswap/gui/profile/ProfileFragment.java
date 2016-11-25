package jameshassmallarms.com.styleswap.gui.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import jameshassmallarms.com.styleswap.R;
import jameshassmallarms.com.styleswap.infrastructure.Linker;

import static jameshassmallarms.com.styleswap.R.id.itemDescription;

public class ProfileFragment extends Fragment {
    private static final String REVERT_TO_TAG = "profile_fragment";
    private ImageButton gotoEditProfile;
    private TextView profileinfo;
    private TextView profileinfo2;
    private Linker linker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        linker = (Linker) getActivity();
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profileinfo = (TextView) view.findViewById(R.id.profileinfo);
        profileinfo.setText(linker.getUserName());

        profileinfo2 = (TextView) view.findViewById(R.id.profileinfo2);
        profileinfo2.setText(linker.getAge());

        gotoEditProfile = (ImageButton) view.findViewById(R.id.profilepic);
        gotoEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = manager.beginTransaction();
                EditProfileFragment editProf = new EditProfileFragment();


                ft.addToBackStack(ProfileFragment.REVERT_TO_TAG);
                ft.replace(R.id.activity_main_fragment_container, editProf, getString(R.string.fragment_edit_profle_id)).commit();
            }
        });
        return view;
    }
}
