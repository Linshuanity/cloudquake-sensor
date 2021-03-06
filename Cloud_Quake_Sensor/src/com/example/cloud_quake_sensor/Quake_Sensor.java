package com.example.cloud_quake_sensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.*; 
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class Quake_Sensor extends ActionBarActivity implements SensorEventListener, OnClickListener {

	Button bstart, bstop;
    TextView Time, Ax, Ay, Az;
    // EditText TimeEt, AxEt, AyEt, AzEt;
    View mChart;
    
    SensorManager sensorManager;
    boolean started = false;
    int SamplingMilisecInterval=50;
	
    double x_val, y_val, z_val;
    
    private ArrayList<AData> sensorData;
    private LinearLayout layout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake__sensor);
        
      //onCreate: get controls
        bstart = (Button) findViewById(R.id.start_button);
        bstop = (Button) findViewById(R.id.stop_button);
        Time = (TextView) findViewById(R.id.time);
        Ax = (TextView) findViewById(R.id.x_axis);
        Ay = (TextView) findViewById(R.id.y_axis);
        Az = (TextView) findViewById(R.id.z_axis);
        //AxEt = (EditText) findViewById(R.id.x_axis_val);
        //AyEt = (EditText) findViewById(R.id.y_axis_val);
        //AzEt = (EditText) findViewById(R.id.z_axis_val);
        layout = (LinearLayout) findViewById(R.id.chart_container);
  
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        
        //onCreate: set Listener
        bstart.setOnClickListener(this);
        bstop.setOnClickListener(this);
        
        //onCreate: set onLoad status
        bstart.setEnabled(true);
        bstop.setEnabled(false);
        if (sensorData == null || sensorData.size() == 0)
        	  bstop.setEnabled(false);
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quake__sensor, menu);
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
    
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	 
    }
 
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (started) {
            x_val = event.values[0];
            y_val = event.values[1];
            z_val = event.values[2];
            long timestamp = System.currentTimeMillis();
            AData data = new AData(timestamp, x_val , y_val, z_val);
            sensorData.add(data);
            /*
            if(sensorData.size()>0){
                int lastIdx=sensorData.size()-1;
                String lastTimeData = sensorData.get(lastIdx).getTime();
                if(data.getTime().matches(lastTimeData)==false){
                	sensorData.add(data);
                }
            }else{
            	sensorData.add(data);
            }*/
            //Time.setText("Time: " + data.getTime());
            Time.setText("Time:      " + data.getTime());
            Ax.setText(String.format("X:       %.04f  ", x_val));
            Ay.setText(String.format("Y:       %.04f  ", y_val));
            Az.setText(String.format("Z:       %.04f  ", z_val));
           // Ax.setText(String.format("%.04f", Ax));
           // Ay.setText("y = " + String.format("%.4f", y));
           // Az.setText("z = " + String.format("%.4f", z));
            
        }
    }

    @Override
    public void onClick(View v) {
    	
        CognitoCachingCredentialsProvider cognitoProvider = new CognitoCachingCredentialsProvider(
            	getApplication(), // get the context for the current activity
                "584255279227",
           	    "us-east-1:2c2ba052-92fc-4915-8d86-1efa08fe7c90",
           	    "arn:aws:iam::584255279227:role/SensorStream_UnAuth",
           	    "arn:aws:iam::584255279227:role/SensorStream_Auth",
           	    Regions.US_EAST_1  
           	);

        String kinesisDirectory = "YOUR_UNIQUE_DIRECTORY";
        KinesisRecorder recorder = new KinesisRecorder(getDir(kinesisDirectory, 0), Regions.US_EAST_1, cognitoProvider);
        recorder.saveRecord("MyData".getBytes(),"SensorStream");
 
        recorder.submitAllRecords();
       
    	
    	
        switch (v.getId()) {
        case R.id.start_button:
            bstart.setEnabled(false);
            bstop.setEnabled(true);
        //    bsave.setEnabled(false);
            sensorData = new ArrayList();
            started = true;            
            Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accel,SensorManager.SENSOR_DELAY_FASTEST);
            break;
        case R.id.stop_button:
            bstart.setEnabled(true);
            bstop.setEnabled(false);
        //    bsave.setEnabled(true);
            started = false;
            
            sensorManager.unregisterListener(this);
            layout.removeAllViews();
            openChart();
            break;
      //  case R.id.bsave:
      /*  	try {
				saveFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            break; */
        default:
            break;
        }
    }
    
    private void openChart() {
        if (sensorData != null || sensorData.size() > 0) {
        	//setting up dataset and lines
            long t = ((AData) sensorData.get(0)).getTimestamp();
            
            XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
            XYSeries xSeries = new XYSeries("X");
            XYSeries ySeries = new XYSeries("Y");
            XYSeries zSeries = new XYSeries("Z");
            
            //fetch data to lines
            for (AData data : sensorData) {
                xSeries.add(data.getTimestamp() - t, data.getX());
                ySeries.add(data.getTimestamp() - t, data.getY());
                zSeries.add(data.getTimestamp() - t, data.getZ());
            }
            
            //fetch lines to dataset
            dataset.addSeries(xSeries);
            dataset.addSeries(ySeries);
            dataset.addSeries(zSeries);
 
            //setting up appearance of lines
            XYSeriesRenderer xRenderer = new XYSeriesRenderer();
            xRenderer.setColor(Color.RED);
            xRenderer.setPointStyle(PointStyle.CIRCLE);
            xRenderer.setFillPoints(true);
            xRenderer.setLineWidth(1);
            xRenderer.setDisplayChartValues(false);
 
            XYSeriesRenderer yRenderer = new XYSeriesRenderer();
            yRenderer.setColor(Color.GREEN);
            yRenderer.setPointStyle(PointStyle.CIRCLE);
            yRenderer.setFillPoints(true);
            yRenderer.setLineWidth(1);
            yRenderer.setDisplayChartValues(false);
 
            XYSeriesRenderer zRenderer = new XYSeriesRenderer();
            zRenderer.setColor(Color.BLUE);
            zRenderer.setPointStyle(PointStyle.CIRCLE);
            zRenderer.setFillPoints(true);
            zRenderer.setLineWidth(1);
            zRenderer.setDisplayChartValues(false);
 
            //setting up appearance of graph
            XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setMarginsColor(Color.argb(255, 255, 255, 255));
            multiRenderer.setMargins(new int[] { 30, 20, 30, 20 });
            multiRenderer.setBackgroundColor(Color.argb(200, 200, 200, 200));
            multiRenderer.setApplyBackgroundColor(true);
            multiRenderer.setLabelsColor(Color.BLACK);
            multiRenderer.setXLabelsColor(Color.BLACK);
            multiRenderer.setYLabelsColor(0, Color.BLACK);
            multiRenderer.setAxesColor(Color.BLACK);
            multiRenderer.setChartTitle("PLOT");
            multiRenderer.setXTitle("time");
            multiRenderer.setYTitle("Value");
            multiRenderer.setZoomButtonsVisible(true);
            
            //define ticks to each axis
           // for (int i = 0; i < sensorData.size(); i++) {
           //     multiRenderer.addXTextLabel(i + 1, ""
           //             + (sensorData.get(i).getTimestamp() - t));
           // }
           // for (int i = 0; i < 12; i++) {
           //     multiRenderer.addYTextLabel(i + 1, ""+i);
           // }
 
            //combine appearance of each lines to graph
            multiRenderer.addSeriesRenderer(xRenderer);
            multiRenderer.addSeriesRenderer(yRenderer);
            multiRenderer.addSeriesRenderer(zRenderer);
 
            // Creating a Line Chart
            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset, multiRenderer);
 
            // Adding the Line Chart to the LinearLayout
            layout.addView(mChart);
            

            
        }
    }
    
    
}

