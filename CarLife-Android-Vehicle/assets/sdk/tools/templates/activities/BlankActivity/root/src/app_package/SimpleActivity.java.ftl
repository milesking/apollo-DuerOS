package ${packageName};

import <#if appCompat>android.support.v7.app.ActionBarActivity<#else>android.app.Activity</#if>;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
<#if applicationPackage??>import ${applicationPackage}.R;</#if>

public class ${activityClass} extends ${(appCompat)?string('ActionBar','')}Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.${layoutName});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.${menuName}, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
