package de.kromke.andreas.cameradatefolders.ui.preferences;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
import de.kromke.andreas.cameradatefolders.MainActivity;
import de.kromke.andreas.cameradatefolders.R;
import de.kromke.andreas.cameradatefolders.StatusAndPrefs;
import de.kromke.andreas.cameradatefolders.databinding.FragmentPreferencesBinding;
import de.kromke.andreas.cameradatefolders.BuildConfig;

@SuppressWarnings("Convert2Lambda")
public class PreferencesFragment extends Fragment
{
    private static final String LOG_TAG = "CDF : PF";
    //private PreferencesViewModel viewModel;
    private FragmentPreferencesBinding binding;

    // convert radio button id to scheme in text form
    private static String schemeId2Val(int id)
    {
        if (id == R.id.button_scheme_y_m_d)
            return "ymd";
        else
        if (id == R.id.button_scheme_m_d)
           return "md";
        else
        if (id == R.id.button_scheme_y_d)
           return "yd";
        else
        if (id == R.id.button_scheme_y_m)
            return "ym";
        else
        if (id == R.id.button_scheme_d)
            return "d";
        else
        if (id == R.id.button_scheme_m)
            return "m";
        else
        if (id == R.id.button_scheme_y)
            return "y";
        else
            return "ymd";
    }

    // convert radio button id to prefix handling in text form
    private static int prefixId2Val(int id)
    {
        if (id == R.id.button_prefix_leave)
            return 0;
        else
        if (id == R.id.button_prefix_append)
            return 1;
        else
        if (id == R.id.button_prefix_remove)
            return 2;
        else
            return 0;
    }

    // convert scheme in text form to radio button id
    private static int schemeVal2Id(final String val)
    {
        switch (val)
        {
            case "ymd":
                return R.id.button_scheme_y_m_d;
            case "md":
                return R.id.button_scheme_m_d;
            case "yd":
                return R.id.button_scheme_y_d;
            case "ym":
                return R.id.button_scheme_y_m;
            case "d":
                return R.id.button_scheme_d;
            case "m":
                return R.id.button_scheme_m;
            case "y":
                return R.id.button_scheme_y;
        }
        return R.id.button_scheme_y_m_d;
    }

    // convert prefix in text form to radio button id
    private static int prefixVal2Id(final int val)
    {
        switch (val)
        {
            case 0:
                return R.id.button_prefix_leave;
            case 1:
                return R.id.button_prefix_append;
            case 2:
                return R.id.button_prefix_remove;
        }
        return R.id.button_prefix_leave;
    }

    // show either long or compact values
    private void updateSubfolderSchemeValues()
    {
        boolean bl = !StatusAndPrefs.mbCompactFolderNames;

        // The binding attributes are automatically generated to "databinding/FragmentPreferencesBinding.java"
        binding.buttonSchemeYMD.setText((bl) ? "1965/1965-07/1965-07-21" : "1965/07/21");
        binding.buttonSchemeMD.setText((bl) ? "1965-07/1965-07-21" : "1965-07/21");
        binding.buttonSchemeYD.setText((bl) ? "1965/1965-07-21" : "1965/07-21");
        binding.buttonSchemeYM.setText((bl) ? "1965/1965-07" : "1965/07");
    }

    // If a longer string is replaced by a shorter one in onCreateView() or onStart(),
    // the UI object will get an adapted width. If later the user switches to a longer
    // text, there will be a width overflow. Hopefully the UI object size has already been
    // calculated in onResume, so that changing the text to a shorter one will not affect
    // the UI element width.
    public void onResume()
    {
        updateSubfolderSchemeValues();
        super.onResume();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        //viewModel = new ViewModelProvider(this).get(PreferencesViewModel.class);

        binding = FragmentPreferencesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //
        // "Subfolder Scheme" radio group
        //

        final RadioGroup folderScheme = binding.schemeRadioGroup;
        //updateSubfolderSchemeValues(); // do not do it here, because this may shrink the UI object width
        folderScheme.check(schemeVal2Id(StatusAndPrefs.mFolderScheme));
        folderScheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup view, int checkedId)
            {
                int id = view.getCheckedRadioButtonId();
                Log.d(LOG_TAG, "checked Button id = " + id);
                final String val = schemeId2Val(id);
                StatusAndPrefs.writeValue(StatusAndPrefs.PREF_FOLDER_SCHEME, val);
            }
        });

        //
        // "Compact Folder Names" switch
        //

        final SwitchCompat swCompactFolderNames = binding.switchCompactFolderNames;
        swCompactFolderNames.setChecked(StatusAndPrefs.mbCompactFolderNames);
        swCompactFolderNames.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean b)
            {
                Log.d(LOG_TAG, "Compact Folder Names switch = " + b);
                StatusAndPrefs.writeValue(StatusAndPrefs.PREF_COMPACT_FOLDER_NAMES, b);
                updateSubfolderSchemeValues();
            }
        });

        //
        // backup copy switch
        //

        final SwitchCompat swBackupCopy = binding.switchBackupCopy;
        swBackupCopy.setChecked(StatusAndPrefs.mbBackupCopy);
        swBackupCopy.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean b)
            {
                Log.d(LOG_TAG, "Backup Copy switch = " + b);
                StatusAndPrefs.writeValue(StatusAndPrefs.PREF_BACKUP_COPY, b);
            }
        });

        //
        // "full file access" switch
        //

        final SwitchCompat swFullFileAccess = binding.switchFullFileAccess;
        swFullFileAccess.setChecked(StatusAndPrefs.mbFullFileAccess);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            swFullFileAccess.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton view, boolean b)
                {
                    Log.d(LOG_TAG, "Full File Access switch = " + b);
                    MainActivity mainActivity = (MainActivity) getActivity();
                    if (mainActivity != null)
                    {
                        //noinspection ConstantConditions
                        if ((BuildConfig.BUILD_TYPE.equals("release_play")) || (BuildConfig.BUILD_TYPE.equals("debug_play")))
                        {
                            mainActivity.dialogNotAvailableInPlayStoreVersion();
                            swFullFileAccess.setChecked(StatusAndPrefs.mbFullFileAccess);
                        } else
                        {
                            mainActivity.requestForPermission30();
                            // (switch update is done asynchronously)
                        }
                    }
                }
            });
        }
        else
        {
            // not available in Android before 11
            swFullFileAccess.setEnabled(false);
        }

        //
        // "force file mode" switch
        //

        final SwitchCompat swForceFileMode = binding.switchForceFileMode;
        swForceFileMode.setChecked(StatusAndPrefs.mbForceFileMode);
        if (StatusAndPrefs.mbFullFileAccess)
        {
            swForceFileMode.setText(R.string.str_force_file_mode_full_access);
        }
        swForceFileMode.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean b)
            {
                Log.d(LOG_TAG, "Force File Mode switch = " + b);
                StatusAndPrefs.writeValue(StatusAndPrefs.PREF_FORCE_FILE_MODE, b);
            }
        });


        //
        // file name prefix handling
        //

        final RadioGroup prefixHandling = binding.prefixRadioGroup;
        prefixHandling.check(prefixVal2Id(StatusAndPrefs.mPrefixMode));
        prefixHandling.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup view, int checkedId)
            {
                int id = view.getCheckedRadioButtonId();
                Log.d(LOG_TAG, "checked Button id = " + id);
                final int val = prefixId2Val(id);
                StatusAndPrefs.writeValue(StatusAndPrefs.PREF_PREFIX_HANDLING, val);
            }
        });


        //
        // dry run switch
        //

        final SwitchCompat swDryRun = binding.switchDryRun;
        swDryRun.setChecked(StatusAndPrefs.mbDryRun);
        swDryRun.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean b)
            {
                Log.d(LOG_TAG, "Dry Run switch = " + b);
                StatusAndPrefs.writeValue(StatusAndPrefs.PREF_DRY_RUN, b);
            }
        });

        //
        // "skip tidy" switch
        //

        final SwitchCompat swSkipTidy = binding.switchSkipTidy;
        swSkipTidy.setChecked(StatusAndPrefs.mbSkipTidy);
        swSkipTidy.setOnCheckedChangeListener(new SwitchCompat.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean b)
            {
                Log.d(LOG_TAG, "Skip Tidy switch = " + b);
                StatusAndPrefs.writeValue(StatusAndPrefs.PREF_SKIP_TIDY, b);
            }
        });

        //
        // "reset preferences" button
        //

        final Button buttonResetPreferences = binding.resetPreferences;
        buttonResetPreferences.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(LOG_TAG, "Reset Preferences button");
                StatusAndPrefs.reset();
            }
        });
        /*
        preferencesViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>()
        {
            @Override
            public void onChanged(@Nullable String s)
            {
                textView.setText(s);
            }
        });
        */
        return root;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }

    // called when "Full File Access" was either granted or denied
    public void updateFullFileAccessMode()
    {
        final SwitchCompat swFullFileAccess = binding.switchFullFileAccess;
        swFullFileAccess.setChecked(StatusAndPrefs.mbFullFileAccess);

        final SwitchCompat swForceFileMode = binding.switchForceFileMode;
        swForceFileMode.setChecked(StatusAndPrefs.mbForceFileMode);

        if (StatusAndPrefs.mbForceFileMode)
        {
            swForceFileMode.setText(R.string.str_force_file_mode_full_access);
        }
        else
        {
            swForceFileMode.setText(R.string.str_force_file_mode);
        }
    }
}