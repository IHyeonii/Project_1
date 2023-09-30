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
    File inputFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\Node_test.shp");

    // FileDataStore -> shp 파일 읽고 featureSource 구하기
    FileDataStore dataStore = FileDataStoreFinder.getDataStore(inputFile);
    SimpleFeatureSource featureSource = dataStore.getFeatureSource(); //FeatureSource -> 지리공간 정보를 Java 객체로 액세스 가능
    SimpleFeatureType schema = featureSource.getSchema(); // 원본 shp파일 스키마 가져와서

    // 좌표체계 정의
    CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4162");
    CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:5179");

    // Allow for some error
    boolean lenient = true;

    // 좌표체계 변환
    MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, lenient);

    //FeatureCollection = JDBC와 유사, 메모리 연결, Iterator 꼭 닫으래
    SimpleFeatureCollection featureCollection = featureSource.getFeatures();

    // SimpleFeatureBuilder = Creation of features (SimpleFeature objects)
    SimpleFeatureTypeBuilder featureTypeBuilder = new SimpleFeatureTypeBuilder();
    featureTypeBuilder.init(schema);
    featureTypeBuilder.setCRS(targetCRS); //출력 파일의 스키마 설정을 위해 필요

    /*
     * Get an output file name and create the new shapefile
     */
    File newFile = getNewShapeFile(inputFile);

    DataStoreFactorySpi factory = new ShapefileDataStoreFactory();
    Map<String, Serializable> params = new HashMap<>();
    params.put("url", newFile.toURI().toURL());
    params.put("create spatial index", Boolean.TRUE);
    DataStore newDataStore = factory.createNewDataStore(params);
    SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(schema, targetCRS);


    // Get the name of the new Shapefile
    // 근데, newDataStore 기져오면 convertNode 존재하지 않는데
    String typeName = dataStore.getTypeNames()[0];
    newDataStore.createSchema(featureType);

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

  private static File getNewShapeFile(File csvFile) {
    File newFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\changeCRS\\convertNode.shp");

    // 새로운 파일이 원본 CSV 파일과 동일한 경우
    if (newFile.equals(csvFile)) {
      System.out.println("Error: cannot replace " + csvFile);
      System.exit(0);
    }
    return newFile;
  }
}
