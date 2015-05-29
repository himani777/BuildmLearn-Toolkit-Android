package org.buildmlearn.toolkit.activity;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.ThemeSingleton;

import org.buildmlearn.toolkit.R;
import org.buildmlearn.toolkit.constant.Constants;
import org.buildmlearn.toolkit.model.Template;
import org.buildmlearn.toolkit.model.TemplateInterface;

public class TemplateEditor extends AppCompatActivity {

    private final static String TAG = "TEMPLATE EDITOR";

    private ListView templateEdtiorList;
    private int templateId;
    private Template template;
    private TemplateInterface selectedTemplate;
    private int selectedPosition = -1;
    private boolean showTemplateSelectedMenu;
    private View selectedView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_editor);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        templateId = getIntent().getIntExtra(Constants.TEMPLATE_ID, -1);
        if (templateId == -1) {
            Toast.makeText(this, "Invalid template ID, closing Template Editor activity", Toast.LENGTH_LONG).show();
            finish();
        }


        if (savedInstanceState == null) {
            setUpTemplateEditor();
        } else {
            restoreTemplateEditor(savedInstanceState);
        }

        findViewById(R.id.button_add_item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedTemplate.addItem(TemplateEditor.this);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(Constants.TEMPLATE_OBJECT, selectedTemplate);
        super.onSaveInstanceState(outState);
    }

    private void populateListView(final BaseAdapter adapter) {
        if (templateEdtiorList == null) {
            templateEdtiorList = (ListView) findViewById(R.id.template_editor_listview);
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View templateHeader = inflater.inflate(R.layout.listview_header_template, templateEdtiorList, false);
        templateEdtiorList.addHeaderView(templateHeader, null, false);
        templateEdtiorList.setAdapter(adapter);

        templateEdtiorList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    return false;
                }

                if (selectedPosition == position - 1) {
                    selectedPosition = -1;
                    view.setBackgroundResource(0);
                    restoreColorScheme();
                } else {
                    if (selectedView != null) {
                        selectedView.setBackgroundResource(0);
                    }
                    selectedView = view;
                    selectedPosition = position - 1;
                    Log.d(TAG, "Position: " + selectedPosition);
                    view.setBackgroundColor(getResources().getColor(R.color.color_divider));
                    changeColorScheme();
                }
                return true;
            }
        });

    }

    private void setUpActionBar(String title) {
        ActionBar actionBar = getSupportActionBar();
        templateEdtiorList = (ListView) findViewById(R.id.template_editor_listview);
        if (actionBar == null) {
            throw new AssertionError();
        }
        actionBar.setTitle(selectedTemplate.getTitle());
    }

    private void setUpTemplateEditor() {
        Log.d(TAG, "Activity Created");
        Template[] templates = Template.values();
        template = templates[templateId];
        Class templateClass = template.getTemplateClass();
        try {
            Object templateObject = templateClass.newInstance();
            selectedTemplate = (TemplateInterface) templateObject;
            Toast.makeText(this, selectedTemplate.onAttach(), Toast.LENGTH_LONG).show();
            populateListView(selectedTemplate.newTemplateEditorAdapter(this));
            setUpActionBar(selectedTemplate.getTitle());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void restoreTemplateEditor(Bundle savedInstanceState) {
        Log.d(TAG, "Activity Restored");
        selectedTemplate = (TemplateInterface) savedInstanceState.getSerializable(Constants.TEMPLATE_OBJECT);
        if (selectedTemplate == null) {
            Toast.makeText(this, "Unable to restore Activity state, finsihing Template Editor activity", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, selectedTemplate.onAttach(), Toast.LENGTH_LONG).show();
            populateListView(selectedTemplate.currentTemplateEditorAdapter());
            setUpActionBar(selectedTemplate.getTitle());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG, "onCreateOptionsMenu");
//        getMenuInflater().inflate(R.menu.menu_template_editor, menu);
//        showTemplateSelectedMenu = false;

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu");
        if(showTemplateSelectedMenu) {
            getMenuInflater().inflate(R.menu.menu_template_item_selected, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_template_editor, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_delete:
                selectedTemplate.deleteItem(selectedPosition);
                restoreSelectedView();
                break;
            case R.id.action_edit:
                selectedTemplate.editItem(this, selectedPosition);
                restoreSelectedView();
                break;
            case R.id.action_simulate:

                break;
            case R.id.action_save:

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void restoreSelectedView() {
        if(selectedView != null) {
            selectedView.setBackgroundResource(0);
        }

        restoreColorScheme();
    }

    public void changeColorScheme() {
        int primaryColor = getResources().getColor(R.color.color_primary_dark);
        int primaryColorDark = getResources().getColor(R.color.color_selected_dark);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(primaryColor));
        ThemeSingleton.get().positiveColor = primaryColor;
        ThemeSingleton.get().neutralColor = primaryColor;
        ThemeSingleton.get().negativeColor = primaryColor;
        ThemeSingleton.get().widgetColor = primaryColor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryColorDark);
            getWindow().setNavigationBarColor(primaryColor);
        }

        showTemplateSelectedMenu = true;
        invalidateOptionsMenu();
    }

    public void restoreColorScheme() {
        int primaryColor = getResources().getColor(R.color.color_primary);
        int primaryColorDark = getResources().getColor(R.color.color_primary_dark);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(primaryColor));
        ThemeSingleton.get().positiveColor = primaryColor;
        ThemeSingleton.get().neutralColor = primaryColor;
        ThemeSingleton.get().negativeColor = primaryColor;
        ThemeSingleton.get().widgetColor = primaryColor;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryColorDark);
            getWindow().setNavigationBarColor(primaryColor);
        }
        showTemplateSelectedMenu = false;
        invalidateOptionsMenu();
    }
}