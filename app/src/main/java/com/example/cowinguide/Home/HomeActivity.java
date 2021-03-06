package com.example.cowinguide.Home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.cowinguide.Adapter.CallLogPojo;
import com.example.cowinguide.Adapter.CustomerServicePojo;
import com.example.cowinguide.CallBack.CommonDialogListner;
import com.example.cowinguide.CallBack.OnConsumerItemClicked;
import com.example.cowinguide.CallBack.OnItemClickListner;
import com.example.cowinguide.Dialog.CustomDialog;
import com.example.cowinguide.Post.PostActivity;
import com.example.cowinguide.Utility.AppConstant;
import com.example.cowinguide.Utility.SessionManager.Session.Sessionmanager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.cowinguide.Home.Fragment.ConsumerFragment;
import com.example.cowinguide.Home.Fragment.ProviderFragment;
import com.example.cowinguide.R;
import com.example.cowinguide.View.Login.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.function.Consumer;

import static com.example.cowinguide.Utility.Utility.showSnackBar;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener,BottomNavigationView.OnNavigationItemSelectedListener, CommonDialogListner, OnItemClickListner, OnConsumerItemClicked {

    Button logout;
    RelativeLayout homeRel;
    private FragmentManager mFragmentManager;
    private ConsumerFragment consumerFragment = new ConsumerFragment();
    private ProviderFragment providerFragment = new ProviderFragment();
    private Fragment currentFragment;
    private BottomNavigationView navigation;
    private FloatingActionButton floatingBtn;
    private CustomDialog customDialog;
    private FrameLayout framlayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //hideStatusBar();
        setContentView(R.layout.activity_home);
        init();
    }

    private void init(){
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(this);
        homeRel = findViewById(R.id.homeRel);
        framlayout = findViewById(R.id.framlayout);
        floatingBtn = findViewById(R.id.floatingBtn);
        floatingBtn.setOnClickListener(this);
        navigation = (BottomNavigationView) findViewById(R.id.bottomnavigation);
        mFragmentManager = getSupportFragmentManager();
        customDialog = new CustomDialog(this);

        addFragment(consumerFragment, false);
        currentFragment = consumerFragment;
        navigation.setSelectedItemId(R.id.consumer_nav);

        navigation.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

    }


    public void addFragment(Fragment fragment, boolean isBackStack) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (fragment.isAdded()) {
            return;
        }
        checkForCosumerFragment(fragment, isBackStack);

        transaction.replace(framlayout.getId(), fragment);

        if (isBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        switch (item.getItemId()){
            case R.id.consumer_nav:
                if(currentFragment == null || (!(currentFragment instanceof ConsumerFragment))){
                    currentFragment = new ConsumerFragment();
                    addFragment(currentFragment,false);
                }
                break;
            case R.id.provider_nav:
                if(currentFragment == null || (!(currentFragment instanceof ProviderFragment))){
                    currentFragment = new ProviderFragment();
                    addFragment(currentFragment,false);
                }
                break;
        }
        return true;
    }


    private void checkForCosumerFragment(Fragment fragment, boolean isBackStack) {
        if (fragment instanceof ConsumerFragment) {
            for (int i = 0; i < mFragmentManager.getBackStackEntryCount(); ++i) {
                mFragmentManager.popBackStack();
            }
        } else {
            if (isBackStack) {
                if (mFragmentManager.getBackStackEntryCount() > 0) {
                    mFragmentManager.popBackStack();
                }
            }
        }
    }

    private void hideStatusBar() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.floatingBtn:{
                 OpenAlertForLogout();
                break;
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        customDialog=new CustomDialog(this);
        EventBus.getDefault().register(this);
        addFragment(consumerFragment, false);
        navigation.setSelectedItemId(R.id.consumer_nav);
        currentFragment = consumerFragment;
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent =new Intent(this, LoginActivity.class);
        showSnackBar(homeRel,getString(R.string.logout_successfully));
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    Sessionmanager.get().clear();
                    Thread.sleep(1000);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private void OpenAlertForLogout() {
        customDialog.setDailog("D", AppConstant.LOGOUT_ALERT,getString(R.string.yes),getString(R.string.no), AppConstant.USER_LOGOUT,"","");
    }


    @Override
    public void OnYesClickListner(int code) {
        if(AppConstant.USER_LOGOUT==code){
            logoutUser();
        }
    }

    @Override
    public void OnNoClickListner(int code) {
        if(AppConstant.USER_LOGOUT==code){
            customDialog.getDialog().dismiss();
        }
    }

    @Override
    public void OnCloseClickListner(int code) {

    }


    @Override
    public void onClickItem(CallLogPojo obj) {
        Bundle bundle = new Bundle();
        Intent intent = new Intent(this, PostActivity.class);
        bundle.putSerializable(AppConstant.DATA, obj);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doThis(CallLogPojo obj) {
       // Log.i("MyBack","Back");
        if(currentFragment == null || (!(currentFragment instanceof ConsumerFragment))){
            currentFragment = new ConsumerFragment();
            navigation.setSelectedItemId(R.id.consumer_nav);
            addFragment(currentFragment,false);
        }
    }

    @Override
    public void onConsumerClickItem(CustomerServicePojo obj) {
        String mob = obj.getNumber();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + mob));
        startActivity(intent);
        //Toast.makeText(this, "Haii Consumer " + mob, Toast.LENGTH_SHORT).show();
    }
}