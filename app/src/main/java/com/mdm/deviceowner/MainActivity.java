package com.mdm.deviceowner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DeviceOwnerManager dom;

    private TextView tvStatus;
    private MaterialCardView cardStatus;

    private MaterialSwitch switchDevOptions;
    private MaterialSwitch switchMultiUser;
    private MaterialSwitch switchScreenCapture;

    private MaterialButton btnLock;

    private RecyclerView rvRestrictions;
    private TextView tvNoRestrictions;
    private MaterialButton btnRefreshRestrictions;
    private ExtendedFloatingActionButton btnAddRestriction;
    private RestrictionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dom = DeviceOwnerManager.getInstance(this);

        if (!dom.isAdminActive()) {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        bindViews();
        setupSwitches();
        setupDeviceActions();
        setupRestrictionsRecycler();
        refreshAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAll();
    }

    private void bindViews() {
        tvStatus = findViewById(R.id.tvStatus);
        cardStatus = findViewById(R.id.cardStatus);

        switchDevOptions = findViewById(R.id.switchDevOptions);
        switchMultiUser = findViewById(R.id.switchMultiUser);
        switchScreenCapture = findViewById(R.id.switchScreenCapture);

        btnLock = findViewById(R.id.btnLock);

        rvRestrictions = findViewById(R.id.rvRestrictions);
        tvNoRestrictions = findViewById(R.id.tvNoRestrictions);
        btnRefreshRestrictions = findViewById(R.id.btnRefreshRestrictions);
        btnAddRestriction = findViewById(R.id.btnAddRestriction);

        btnRefreshRestrictions.setOnClickListener(v -> refreshAll());
        btnAddRestriction.setOnClickListener(v -> showAddRestrictionDialog());
    }

    private void setupDeviceActions() {
        btnLock.setOnClickListener(v -> dom.lockNow());
    }


    private void showDoRequiredDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_do_required_title)
                .setMessage(R.string.dialog_do_required_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showAddRestrictionDialog() {
        List<String> keys = dom.getAllAvailableRestrictionKeys();
        String[] items = keys.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_add_restriction_title)
                .setItems(items, (dialog, which) -> {
                    String selected = items[which];
                    dom.addRestriction(selected);
                    Toast.makeText(this, getString(R.string.toast_restriction_applied, selected), Toast.LENGTH_SHORT).show();
                    refreshAll();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void setupSwitches() {
        switchDevOptions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dom.enforceDeveloperOptions(!isChecked);
            Toast.makeText(this, isChecked ? R.string.toast_dev_opts_restored : R.string.toast_dev_opts_enforced, Toast.LENGTH_SHORT).show();
        });

        switchMultiUser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dom.enforceMultiUser(isChecked);
            Toast.makeText(this, isChecked ? R.string.toast_multi_user_restored : R.string.toast_multi_user_enforced, Toast.LENGTH_SHORT).show();
        });

        switchScreenCapture.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dom.setScreenCaptureDisabled(!isChecked);
            Toast.makeText(this, isChecked ? "Screen capture enabled" : "Screen capture disabled", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRestrictionsRecycler() {
        rvRestrictions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RestrictionAdapter(item -> {
            if (item.isSetByMe()) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_remove_restriction_title)
                        .setMessage(String.format(getString(R.string.dialog_remove_restriction_message), item.getFriendlyName()))
                        .setPositiveButton(R.string.btn_remove, (dialog, which) -> {
                            if (dom.removeRestriction(item.getKey())) {
                                Toast.makeText(this, getString(R.string.toast_restriction_removed, item.getFriendlyName()), Toast.LENGTH_SHORT).show();
                                refreshAll();
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(item.getFriendlyName())
                        .setMessage(R.string.restriction_source_other)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
        rvRestrictions.setAdapter(adapter);
    }

    private void refreshAll() {
        updateStatusCard();
        updateSwitches();
        loadRestrictions();
    }

    private void updateStatusCard() {
        if (dom.isDeviceOwner()) {
            tvStatus.setText(R.string.status_do_active_main);
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if (dom.isProfileOwner()) {
            tvStatus.setText(R.string.status_po_active_main);
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        } else if (dom.isAdminActive()) {
            tvStatus.setText(R.string.status_admin_active_main);
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else {
            tvStatus.setText(R.string.status_inactive);
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void updateSwitches() {
        switchDevOptions.setOnCheckedChangeListener(null);
        switchDevOptions.setChecked(dom.isDeveloperOptionsEnabled());
        switchDevOptions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dom.enforceDeveloperOptions(isChecked);
            Toast.makeText(this, isChecked ? R.string.toast_dev_opts_restored : R.string.toast_dev_opts_enforced, Toast.LENGTH_SHORT).show();
        });

        switchMultiUser.setOnCheckedChangeListener(null);
        switchMultiUser.setChecked(dom.isMultiUserEnabled());
        switchMultiUser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dom.enforceMultiUser(isChecked);
            Toast.makeText(this, isChecked ? R.string.toast_multi_user_restored : R.string.toast_multi_user_enforced, Toast.LENGTH_SHORT).show();
        });
    }

    private void loadRestrictions() {
        List<RestrictionItem> restrictions = dom.getAllActiveRestrictions();
        if (restrictions.isEmpty()) {
            tvNoRestrictions.setVisibility(View.VISIBLE);
            rvRestrictions.setVisibility(View.GONE);
        } else {
            tvNoRestrictions.setVisibility(View.GONE);
            rvRestrictions.setVisibility(View.VISIBLE);
            adapter.setItems(restrictions);
        }
    }
}
