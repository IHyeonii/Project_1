package completion;

import org.geotools.api.data.*;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
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

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CRSLab { //좌표는 변경됐는데 값이 안들어감 ㅋ
  public static void main(String[] args) throws Exception {

    File inputShpFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\Node_test.shp");
    File outputShpFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\changeCRS\\convertNode.shp");

    // FileDataStore -> shp 로드, featureSource 구하기
    FileDataStore dataStore = FileDataStoreFinder.getDataStore(inputShpFile);
    SimpleFeatureSource featureSource = dataStore.getFeatureSource();
    SimpleFeatureCollection featureCollection = featureSource.getFeatures();

    // 좌표체계 정의
    CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4162");
    CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:5179");

    // Allow for some error
    boolean lenient = true;

    // 좌표체계 변환
    MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, lenient);

    // Define the schema for the output Shapefile

    SimpleFeatureType schema = featureSource.getSchema(); // 입력 shp파일 스키마 가져와서
    SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
    featureTypeBuilder.init(schema);
    featureTypeBuilder.setCRS(targetCRS);

    // Create a new DataStore for the output Shapefile
    DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
    Map<String, Serializable> create = new HashMap<>();
    create.put("url", outputShpFile.toURI().toURL()); // 이 부분이 애매해
    create.put("create spatial index", Boolean.TRUE);
    DataStore newDataStore = factory.createNewDataStore(create);
    SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(schema, targetCRS);
    newDataStore.createSchema(featureType);

    // Get the name of the new Shapefile
    // 근데, newDataStore 기져오면 convertNode 존재하지 않는데
    String typeName = dataStore.getTypeNames()[0];

    Transaction transaction = new DefaultTransaction("Reproject");
    try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
             dataStore.getFeatureWriterAppend(typeName, transaction);
         SimpleFeatureIterator iterator = featureCollection.features()) {
      while (iterator.hasNext()) {
        // copy the contents of each feature and transform the geometry
        SimpleFeature feature = iterator.next();
        SimpleFeature copy = writer.next();
        copy.setAttributes(feature.getAttributes());

        Geometry geometry = (Geometry) feature.getDefaultGeometry();
        System.out.println("geometry" + geometry);
        Geometry geometry2 = JTS.transform(geometry, transform);

        copy.setDefaultGeometry(geometry2);
        writer.write();
      }
      transaction.commit();
    } catch (Exception problem) {
      problem.printStackTrace();
      transaction.rollback();
    } finally {
      transaction.close();
    }
  }
}
