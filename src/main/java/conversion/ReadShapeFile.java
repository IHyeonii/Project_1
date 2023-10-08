package conversion;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.type.FeatureType;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadShapeFile {
  public static void main(String[] args) throws Exception {
    File nodeFile = new File("C:\\Users\\ihyeon\\Downloads\\ConvertCoord2\\ConvertCoord.shp");

    List<Object> nodeAttribute = new ArrayList<>();
    FileDataStore store = FileDataStoreFinder.getDataStore(nodeFile);
    SimpleFeatureSource source = store.getFeatureSource();
    FeatureType schema = source.getSchema();

    try {
      SimpleFeatureCollection featureCollection = source.getFeatures();
      SimpleFeatureIterator iterator = featureCollection.features();
      while (iterator.hasNext()) {
        // copy the contents of each feature and transform the geometry
        SimpleFeature feature = iterator.next();
        nodeAttribute.add(feature.getAttributes());
      }
      System.out.println("attributes :" + nodeAttribute);
      // [POINT (885653.9227541254 1473920.033272428), 41340000, 1, 7, ] : 내가 TYPE 정한 순서
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
