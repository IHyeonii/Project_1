package tutorial;

import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DataUtilities;
import org.geotools.swing.data.JFileDataStoreChooser;

import javax.swing.*;
import java.io.File;

/**
 * This example reads data for point locations and associated attributes from a comma separated text (CSV) file
 * and exports them as a new shapefile.
 * */
public class Csv2Shape {
  public static void main(String[] args) throws Exception {
    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

    File file = JFileDataStoreChooser.showOpenFile("csv", null);
    if (file == null) {
      return;
    }

    // 1. Create a FeatureType
    // To describe the data that we are importing from the CSV file and writing to a shapefile.
    final SimpleFeatureType TYPE =
        DataUtilities.createType( // use the DataUtilities
            "Location",
            "the_geom:Point:srid=4326,"
                + // <- the geometry attribute: Point type
                "name:String,"
                + // <- a String attribute
                "number:Integer" // a number attribute
        );
    System.out.println("TYPE:" + TYPE);



  }
}
