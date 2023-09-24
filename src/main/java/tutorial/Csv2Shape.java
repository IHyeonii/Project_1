package tutorial;

import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.data.Transaction;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // 1. FeatureType 생성
    // To describe the data that we are importing from the CSV file and writing to a shapefile.
    final SimpleFeatureType TYPE =
        DataUtilities.createType( // DataUtilities 사용
            "Location",
            "the_geom:Point:srid=4326,"
                + // <- the geometry attribute: Point type
                "name:String,"
                + // <- a String attribute
                "number:Integer" // a number attribute
        );
    System.out.println("TYPE:" + TYPE);
    // QuickStart 실행 -> csv파일 선택하면
    // SimpleFeatureTypeImpl 위치 식별하면 -> Feature(the_geom:the_geom,name:name,number:number) 확장 됨

    // 2. Features 생성 -> Point, Line(점들의 연결), Polygon, Attribute..
    // 이제 CSV 파일을 읽고 각 줄을 파싱하여 데이터 추출하기
    List<SimpleFeature> features = new ArrayList<>();

    // 2-1. 새로운 포인트 생성을 위한 GeometryFactory 사용
    // GeometryFactory는 각 feature의 지오메트리 속성을 생성하는 데 사용
    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    // 2-2. SimpleFeatureBuilder 사용하여 Features(SimpleFeature 객체) 생성
    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      /* First line of the data file is the header */
      String line = reader.readLine();
      System.out.println("Header: " + line);

      for (line = reader.readLine(); line != null; line = reader.readLine()) {
        if (line.trim().length() > 0) { // skip blank lines -> isEmpty로 해보기
          String[] tokens = line.split("\\,"); // 쉼표(,)로 구분된 데이터 파싱

          // cvs 파일 각 세로 값들
          double latitude = Double.parseDouble(tokens[0]);
          double longitude = Double.parseDouble(tokens[1]);
          String name = tokens[2].trim(); // CITY 값을 문자열로 추출하고 양 끝의 공백 제거 -> 이게 dbf 파일로 넘어감
          int number = Integer.parseInt(tokens[3].trim());

          /* Longitude (= x coord) first ! */
          // 위도와 경도를 사용하여 Point 객체를 생성
          Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

          // featureBuilder 객체를 사용하여 point 객체랑 name, number -> feature에 추가
          featureBuilder.add(point);
          featureBuilder.add(name);
          featureBuilder.add(number);

          // featureBuilder 사용하여 SimpleFeature 객체 생성 -> 여기에 모든 데이터가 포함 됨
          SimpleFeature feature = featureBuilder.buildFeature(null); // null 대신 feature의 ID나 식별자 지정 가능

          // 생성한 feature를 features 리스트에 추가
          features.add(feature);
        }
      }
    }

    // 3. Create a shapefile From a FeatureCollection
    // getNewShapeFile 메서드 호츌
    File newFile = getNewShapeFile(file); // 39번 라인 -> 출력할 파일(csv) name 가져오기

    // 3-1.DataStoreFactory -> 원하는 공간 인덱스를 표시하는 매개변수와 함께 사용해야 함
    ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

    Map<String, Serializable> params = new HashMap<>(); // 직렬화 ?
    params.put("url", newFile.toURI().toURL());
    params.put("create spatial index", Boolean.TRUE);

    ShapefileDataStore newDataStore =
        (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

    // 3-2. ShapeFile 설정 위한 createSchema(SimpleFeatureType) 메서드 사용
    newDataStore.createSchema(TYPE); // TYPE -> 파일 내용 설명하는 템플릿으로 사용

    // 4. ShapeFile에 feature data 작성
    Transaction transaction = new DefaultTransaction("create");

    String typeName = newDataStore.getTypeNames()[0];
    SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
    SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();
    /*
     * The Shapefile format has a couple limitations:
     * "the_geom" is always first, and used for the geometry attribute name
     * "the_geom" must be of type Point, MultiPoint, MuiltiLineString, MultiPolygon
     * Attribute names are limited in length
     */
    System.out.println("SHAPE:" + SHAPE_TYPE);

    if (featureSource instanceof SimpleFeatureStore) {
      SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
      /*
       * SimpleFeatureStore has a method to add features
       * SimpleFeatureCollection 개체를 사용하므로, ListFeatureCollection 사용 가능
       * class to wrap our list of features.
       */
      SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
      featureStore.setTransaction(transaction);
      try {
        featureStore.addFeatures(collection);
        transaction.commit();
      } catch (Exception problem) {
        problem.printStackTrace();
        transaction.rollback();
      } finally {
        transaction.close();
      }
      System.exit(0); // success!
    } else {
      System.out.println(typeName + " does not support read/write access");
      System.exit(1);
    }
  }

  // 5. output shapefile
  private static File getNewShapeFile(File csvFile) { // 매개변수로 입력할 csv 파일
    String path = csvFile.getAbsolutePath();
    String newPath = path.substring(0, path.length() - 4) + ".shp";

    JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
    chooser.setDialogTitle("Save shapefile");
    chooser.setSelectedFile(new File(newPath));

    int returnVal = chooser.showSaveDialog(null);

    if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
      // the user cancelled the dialog
      System.exit(0);
    }

    File newFile = chooser.getSelectedFile(); // ShapeFile 이름과 경로를 newFile 개체로 반환
    if (newFile.equals(csvFile)) {
      System.out.println("Error: cannot replace " + csvFile);
      System.exit(0);
    }

    return newFile;
  }
}
