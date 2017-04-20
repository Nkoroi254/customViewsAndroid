package com.arachi.nkoroi.customviewandroiddocumentation;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

  /**
   * Called when the activity is first created.
   * @param savedInstanceState
   */
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Resources resources = getResources();

    setContentView(R.layout.activity_main);
    final PieChart pie = (PieChart) this.findViewById(R.id.Pie);
    pie.addItem("Agamemnon", 2, resources.getColor(R.color.seafoam));
    pie.addItem("Bocephus", 3.5f, resources.getColor(R.color.chartreuse));
    pie.addItem("Calliope", 2.5f, resources.getColor(R.color.emerald));
    pie.addItem("Daedalus", 3, resources.getColor(R.color.bluegrass));
    pie.addItem("Euripides", 1, resources.getColor(R.color.turquoise));
    pie.addItem("Ganymede", 3, resources.getColor(R.color.slate));

    ((Button) findViewById(R.id.Reset)).setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        pie.setCurrentItem(0);
      }
    });
  }
}
