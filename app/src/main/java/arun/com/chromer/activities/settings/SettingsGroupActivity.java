package arun.com.chromer.activities.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.base.SubActivity;
import arun.com.chromer.activities.settings.browsingmode.BrowsingModeActivity;
import arun.com.chromer.activities.settings.browsingoptions.BrowsingOptionsActivity;
import arun.com.chromer.activities.settings.lookandfeel.LookAndFeelActivity;
import arun.com.chromer.shared.Constants;
import arun.com.chromer.util.Utils;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static arun.com.chromer.shared.Constants.ACTION_CLOSE_ROOT;

public class SettingsGroupActivity extends SubActivity implements SettingsGroupAdapter.GroupItemClickListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.settings_list_view)
    RecyclerView settingsListView;
    @BindView(R.id.set_default_card)
    CardView setDefaultCard;
    @BindView(R.id.set_default_image)
    ImageView setDefaultImage;
    private SettingsGroupAdapter adapter;
    private BroadcastReceiver closeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        adapter = new SettingsGroupAdapter(this);
        settingsListView.setLayoutManager(new LinearLayoutManager(this));
        settingsListView.setAdapter(adapter);
        settingsListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter.setGroupItemClickListener(this);
        registerCloseReceiver();

        setDefaultImage.setImageDrawable(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_auto_fix)
                .color(Color.WHITE)
                .sizeDp(24));

        setDefaultCard.setOnClickListener(v -> {
            Answers.getInstance().logCustom(new CustomEvent("Set Default Clicked"));
            final String defaultBrowser = Utils.getDefaultBrowserPackage(getApplicationContext());
            if (defaultBrowser.equalsIgnoreCase("android")
                    || defaultBrowser.startsWith("org.cyanogenmod")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GOOGLE_URL)));
            } else {
                final Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + defaultBrowser));
                Toast.makeText(SettingsGroupActivity.this,
                        Utils.getAppNameWithPackage(getApplicationContext(), defaultBrowser)
                                + " "
                                + getString(R.string.default_clear_msg), Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        });
        updateDefaultBrowserCard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDefaultBrowserCard();
    }

    @Override
    protected void onDestroy() {
        adapter.cleanUp();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(closeReceiver);
        super.onDestroy();
    }

    private void updateDefaultBrowserCard() {
        if (!Utils.isDefaultBrowser(this)) {
            setDefaultCard.setVisibility(View.VISIBLE);
        } else
            setDefaultCard.setVisibility(View.GONE);
    }

    private void registerCloseReceiver() {
        closeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Timber.d("Finished from receiver");
                SettingsGroupActivity.this.finish();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(closeReceiver, new IntentFilter(ACTION_CLOSE_ROOT));
    }

    @Override
    public void onGroupItemClicked(int position, View view) {
        switch (position) {
            case 0:
                startActivity(new Intent(this, BrowsingModeActivity.class));
                break;
            case 1:
                startActivity(new Intent(this, LookAndFeelActivity.class));
                break;
            case 2:
                startActivity(new Intent(this, BrowsingOptionsActivity.class));
                break;
        }
    }
}
