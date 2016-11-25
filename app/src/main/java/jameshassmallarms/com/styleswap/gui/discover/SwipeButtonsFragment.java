package jameshassmallarms.com.styleswap.gui.discover;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import jameshassmallarms.com.styleswap.R;
import jameshassmallarms.com.styleswap.gui.BlankFragment;
import jameshassmallarms.com.styleswap.impl.Match;
import jameshassmallarms.com.styleswap.impl.User;
import jameshassmallarms.com.styleswap.infrastructure.DatabaseHandler;
import jameshassmallarms.com.styleswap.infrastructure.FireBaseQueries;
import jameshassmallarms.com.styleswap.infrastructure.Linker;
import jameshassmallarms.com.styleswap.infrastructure.QueryMaster;

/**
 * Created by Alan on 25/10/2016.
 */

public class SwipeButtonsFragment extends Fragment {

    public static final int LOADING_SIZE = 10;
    private BlankFragment blank;
    private ImageButton likeObject;
    private ImageButton dislikeObject;
    private String description;
    private TextView userNameView;
    private Fragment nestedCard;
    private FragmentTransaction transaction;
    private ArrayList<NestedInfoCard> nestedCards;
    private Queue<NestedInfoCard> nestedQueue;
    private String userName;
    private int dressSize = 8;
    private int searchRadius = 10;
    private boolean active;
    private Linker linker;
    private FireBaseQueries fireBaseQueries = new FireBaseQueries();
    private ArrayList<Match> matchs = new ArrayList<>();
    private DatabaseHandler convert = new DatabaseHandler(getContext());

    QueryMaster q = new QueryMaster() {
        @Override
        public void run(DataSnapshot s) {
            description = s.getValue().toString();
        }
    };

    QueryMaster P = new QueryMaster() {
        @Override
        public void run(DataSnapshot s) {
            userNameView.setText(s.getValue().toString());
        }
    };


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set the view

        View root = inflater.inflate(R.layout.fragment_swipe_buttons, container, false);
        blank = new BlankFragment();
        linker = (Linker) getActivity();
        nestedCards = new ArrayList<NestedInfoCard>();
        nestedCard = new NestedInfoCard();
        nestedQueue = new LinkedList<NestedInfoCard>();

        userName = linker.getLoggedInUser();
        Log.d("TAG", "prafff: " + linker.getLoggedInUser());


        //System.out.println(linker.getLoggedInUser());
        likeObject = (ImageButton) root.findViewById(R.id.fragment_yes_button);
        dislikeObject = (ImageButton) root.findViewById(R.id.fragment_no_button);

        if (matchs.isEmpty()) {
            loadBlankFragment();
        } else {
            getMatchs();
            replaceFragment(nestedQueue.poll());

        }


        likeObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (nestedQueue.isEmpty()) {
                    loadBlankFragment();
                    getMatchs();
                } else {
                    replaceFragment(nestedQueue.poll());

                }
            }
        });

        dislikeObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nestedQueue.isEmpty()) {
                    loadBlankFragment();
                    getMatchs();
                } else {
                    replaceFragment(nestedQueue.poll());
                }
            }
        });

        return root;
    }

    public void onStart() {
        super.onStart();
        userName = linker.getLoggedInUser();
        if (userName != null && nestedQueue.size() == 0) {
            getMatchs();
            System.out.println("here");
        }

    }

    private NestedInfoCard loadFragment(Match match) {
        //Need to add in a query to get name description and picture
        String u = match.getMatchName();
        String desc = match.getMatchBio();
        String email = match.getMatchMail();
        if ((match.getByteArray()) == null){
            Log.d("Debug_swipe", "Empty match byte array");
        }
        byte[] image = match.getByteArray();
        if(image == null){
            Log.d("IMAGE DOESn't Exist", "LAMEEEE");
        }
        Bundle b = new Bundle();
        b.putString("UserName", u);
        b.putString("Description", desc);
        b.putString("Email", email);
        b.putByteArray("IMG", image);
        NestedInfoCard nest = new NestedInfoCard();
        nest.setArguments(b);
        Log.d("tag", "Fragment added to stack");
        return nest;
    }

    private void replaceFragment(NestedInfoCard nest) {
        transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_match_frame, nest, "TAG").commit();
        Log.d("Fragment", "Replaced");
        active = true;
    }

    //    private void fillFragments(){
//        count = 0;
//
//        nestedCards.clear();
//        for(int i = 0; i < LOADING_SIZE; i++){
//            NestedInfoCard card = loadFragment();
//            nestedCards.add(card);
//        }
//    }
//    //TODO dont match if matched recently
    private void getNewMatchs() {
        //users email
        DatabaseReference mUserRef = fireBaseQueries.getUserLocationReferenceByEmail(userName);
        final GeoFire geoFire = new GeoFire(mUserRef.getParent());


        geoFire.getLocation(mUserRef.getKey(), new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.latitude, location.longitude), searchRadius);//my search radius
                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(key);
                            fireBaseQueries.executeIfExists(ref, new QueryMaster() {
                                @Override
                                public void run(DataSnapshot s) {
                                    User user = s.getValue(User.class);
                                    if (user.getDressSize() == dressSize) {//my dress size
                                        addToQueue(user.getName(),user);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onKeyExited(String key) {

                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {

                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {

                        }
                    });
                } else {
                    System.out.println(String.format("There is no location for key %s in GeoFire", key));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("There was an error getting the GeoFire location: " + databaseError);
            }
        });

    }

    private void getMatchs() {
        final DatabaseReference matchedMe = fireBaseQueries.getMatchedme(userName);//users email

        fireBaseQueries.executeIfExists(matchedMe, new QueryMaster() {
            @Override
            public void run(DataSnapshot s) {
                GenericTypeIndicator<ArrayList<Match>> t = new GenericTypeIndicator<ArrayList<Match>>() {
                };
                ArrayList<Match> update = s.getValue(t);
                if (update.size() > 1) {

                    for (int i = 1; i < update.size(); ) {

                       addToQueue(update.get(i).getMatchName(), update.get(i));
                        update.remove(i);
                    }
                    //matchedMe.setValue(update);//comment back in for vinal version just not removing so i can test
                }
                getNewMatchs();
                replaceFragment(nestedQueue.poll());
            }
        });
    }


    private void loadBlankFragment() {
        transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_match_frame, blank, "TAG").commit();
    }


    public void addToQueue(String username, final User user) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference picRef = storage.getReferenceFromUrl("gs://styleswap-f3aa9.appspot.com").child(username + "/" + "Dress");

        final long ONE_MEGABYTE = 1024 * 1024;
        picRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Match temp = user.toMatch();
                temp.setByteArray(bytes);
                nestedQueue.add(loadFragment(temp));

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

    public void addToQueue(String username, final Match match) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference picRef = storage.getReferenceFromUrl("gs://styleswap-f3aa9.appspot.com").child(username + "/" + "Dress");

        final long ONE_MEGABYTE = 1024 * 1024;
        picRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                match.setByteArray(bytes);
                nestedQueue.add(loadFragment(match));

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });
    }

}