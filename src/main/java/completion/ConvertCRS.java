package completion;

import org.geotools.api.data.*;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ConvertCRS {
  public static void main(String[] args) throws FactoryException, IOException {
    File inputShpFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\Node_test.shp");
    File outputShpFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\convertCrsNode.shp");

    // 데이터 저장소(DataStore)로부터 Shapefile 데이터를 읽어옵니다.
    FileDataStore dataStore = FileDataStoreFinder.getDataStore(inputShpFile);
    SimpleFeatureSource featureSource = dataStore.getFeatureSource();
    SimpleFeatureCollection featureCollection = featureSource.getFeatures();

    // 1. CoordinateReferenceSystem 객체를 사용하여 좌표 시스템 정의
    CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4162");
    CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:5179");

    // allow for some error
    boolean lenient = true;

    // 2. MathTransform 사용하여 변환 수행
    MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, lenient);


    /**
     * 여기까지 공식문서
     * */

    SimpleFeatureType schema = featureSource.getSchema();

    // 데이터 저장소(DataStore)를 생성하여 출력 Shapefile을 생성합니다.
    DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
    Map<String, Serializable> params = new HashMap<>();
    params.put("url", outputShpFile.toURI().toURL());
    params.put("create spatial index", Boolean.TRUE);
    DataStore newDataStore = factory.createNewDataStore(params);
    SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(schema);
    newDataStore.createSchema(featureType); // DataSourceException


    // 여기부터 이 상 해
    Transaction transaction = new DefaultTransaction("Reproject");

    try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer = dataStore.getFeatureWriter(transaction);
         SimpleFeatureIterator iterator = featureCollection.features()) {
      while (iterator.hasNext()) {
        SimpleFeature feature = iterator.next();
        SimpleFeature copy = writer.next();
        copy.setAttributes(feature.getAttributes());

        Geometry originalGeometry = (Geometry) feature.getDefaultGeometry();
        Geometry transformedGeometry = JTS.transform(originalGeometry, transform);

        copy.setDefaultGeometry(transformedGeometry);
        writer.write();
      }
      transaction.commit();
      JOptionPane.showMessageDialog(null, "Export to shapefile complete");
    } catch (Exception problem) {
      problem.printStackTrace();
      transaction.rollback();
      JOptionPane.showMessageDialog(null, "Export to shapefile failed");
    } finally {
      transaction.close();
    }
  }
}
