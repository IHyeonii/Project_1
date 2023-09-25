//package conversion;
//
//import com.opencsv.CSVReader;
//import org.geotools.api.data.SimpleFeatureSource;
//import org.geotools.api.data.SimpleFeatureStore;
//import org.geotools.api.data.Transaction;
//import org.geotools.api.feature.simple.SimpleFeature;
//import org.geotools.api.feature.simple.SimpleFeatureType;
//import org.geotools.data.DataUtilities;
//import org.geotools.data.DefaultTransaction;
//import org.geotools.data.collection.ListFeatureCollection;
//import org.geotools.data.shapefile.ShapefileDataStore;
//import org.geotools.data.shapefile.ShapefileDataStoreFactory;
//import org.geotools.data.simple.SimpleFeatureCollection;
//import org.geotools.feature.simple.SimpleFeatureBuilder;
//import org.geotools.geometry.jts.JTSFactoryFinder;
//import org.locationtech.jts.geom.Coordinate;
//import org.locationtech.jts.geom.GeometryFactory;
//import org.locationtech.jts.geom.LineString;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class ConvertLinkFile {
//  public static void main(String[] args) throws Exception {
//    File file = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\Link_test.csv");
//
//    // 1. FeatureType 생성
//    // SimpleFeatureType 생성 =  CSV 파일에서 읽어온 데이터를 설명, 속성 유형과 구조 정의
//    final SimpleFeatureType TYPE =
//        DataUtilities.createType( // DataUtilities 사용
//            "LinkFile",
//            "the_geom:LineString:srid=4326,"
//            + // <- the geometry attribute: LineString type
//            "idxname:Integer,"
//            + // <-  attribute
//            "linkid:Integer," + "stndid:Integer," + "edndid:Integer," + "linkcate:Integer," + "roadcate:Integer,"
//            + "roadno:Integer," + "lane:Integer," + "linkfacil:Integer," + "ks1:String," + "ks2:String,"
//            + "oneway:Integer," + "speedlh:Integer," + "length:Integer"
//        );
//    System.out.println("TYPE:" + TYPE);
//
//    // 2. Features 생성 -> Point, Line(점들의 연결), Polygon, Attribute..
//    // 이제 CSV 파일을 읽고 각 줄을 파싱하여 데이터 추출하기
//    List<SimpleFeature> features = new ArrayList<>();
//
//    // 2-1. 새로운 포인트 생성을 위한 GeometryFactory 사용
//    // GeometryFactory는 각 feature의 지오메트리 속성을 생성하는 데 사용
//    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
////    Coordinate[] coords  =
////        new Coordinate[] {new Coordinate(0, 2), new Coordinate(2, 0), new Coordinate(8, 6) };
////
////    LineString line = geometryFactory.createLineString(coordinates);
//
//    // 2-2. SimpleFeatureBuilder 사용하여 Features(SimpleFeature 객체) 생성
//    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
//
//    try {
//      BufferedReader reader = new BufferedReader(new FileReader(file));
//      CSVReader csvReader = new CSVReader(reader);
//
//      /* First line of the data file is the header */
//      String[] line = csvReader.readNext();
//      System.out.println("Header: " + line);
//      System.out.println("첫 줄 = " + line);
//
//      while ((line = csvReader.readNext()) != null) {
//        if (reader.readLine() != null) {
//          String[] split = reader.readLine().split(",");
//
//          String lineString = line[14];
//
//          int start = lineString.indexOf('(');
//          int end = lineString.indexOf(')');
//          String result = lineString.substring(start+1, end); // 괄호 안의 숫자만 출력
//
//          // 콤마 또는 공백을 구분자로 분리
//          String[] value = result.split(",| ");
//
//          Integer idxname = Integer.parseInt(split[0]);
//          Integer linkid = Integer.parseInt(split[1]);
//          Integer stndid = Integer.parseInt(split[2]);
//          Integer edndid = Integer.parseInt(split[3]);
//          Integer linkcate = Integer.parseInt(split[4]);
//          Integer roadcate = Integer.parseInt(split[5]);
//          Integer roadno = Integer.parseInt(split[6]);
//          Integer lane = Integer.parseInt(split[7]);
//          Integer linkfacil = Integer.parseInt(split[8]);
//          String ks1 = split[9];
//          String ks2 = split[10];
//          Integer oneway = Integer.parseInt(split[11]);
//          Integer speedlh = Integer.parseInt(split[12]);
//          Integer length = Integer.parseInt(split[13]);
//
//          // geom 부분 .. 여길 길이 상관 없이 어 떻 게 가져오냐
//
//          for (int i = 0; i < value.length; i++) {
//            double longitude = Double.parseDouble(value[i]);
//            double latitude = Double.parseDouble(value[i]);
//            Coordinate coord = new Coordinate(longitude, latitude);
//
//          }
//          /* Longitude (= x coord) first ! */
//          // 위도와 경도를 사용하여 Point 객체를 생성
//          // 두 개의 Point 생성하여 LineString 만들기
//
//          // LineString 생성
//          LineString lineInfo = geometryFactory.createLineString(new Coordinate[]{startCoord, endCoord});
//
//          // featureBuilder 객체를 사용하여 point 객체랑 name, number -> feature에 추가
//          featureBuilder.add(lineInfo);
//
//          // featureBuilder 사용하여 SimpleFeature 객체 생성 -> 여기에 모든 데이터가 포함 됨
//          SimpleFeature feature = featureBuilder.buildFeature(null);
//
//          // 생성한 feature를 features 리스트에 추가
//          features.add(feature);
//        }
//
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//
//    // 3. Create a shapefile From a FeatureCollection
//    // getNewShapeFile 메서드 호츌
//    File newFile = getNewShapeFile(file); // 읽어온 csv 파일 가져와서
//
//    // 3-1.DataStoreFactory -> 원하는 공간 인덱스를 표시하는 매개변수와 함께 사용해야 함
//    ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
//
//    Map<String, Serializable> params = new HashMap<>();
//    params.put("url", newFile.toURI().toURL());
//    params.put("create spatial index", Boolean.TRUE);
//
//    ShapefileDataStore newDataStore =
//        (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
//
//    // 3-2. ShapeFile 설정 위한 createSchema(SimpleFeatureType) 메서드 사용
//    newDataStore.createSchema(TYPE); // TYPE -> 파일 내용 설명하는 템플릿으로 사용
//
//    // 4. ShapeFile에 feature data 작성
//    Transaction transaction = new DefaultTransaction("create");
//
//    String typeName = newDataStore.getTypeNames()[0];
//    SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
//    SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();
//
//    System.out.println("SHAPE:" + SHAPE_TYPE); // 타입이 Point로 넘어와..
//
//    if (featureSource instanceof SimpleFeatureStore) {
//      SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
//      /*
//       * SimpleFeatureStore has a method to add features
//       * SimpleFeatureCollection 개체를 사용하므로, ListFeatureCollection 사용 가능
//       * class to wrap our list of features.
//       */
//      SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, features);
//      featureStore.setTransaction(transaction);
//      try {
//        featureStore.addFeatures(collection);
//        transaction.commit();
//      } catch (Exception problem) {
//        problem.printStackTrace();
//        transaction.rollback();
//      } finally {
//        transaction.close();
//      }
//      System.exit(0); // success!
//    } else {
//      System.out.println(typeName + " does not support read/write access");
//      System.exit(1);
//    }
//  }
//
//  // 5. output shapefile
//  private static File getNewShapeFile(File csvFile) { //getNewShapeFile 호출될 때 매개변수로 전달되는 파일
//    File newFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\locations.shp");
//
//    // 3. 만약 새로운 파일이 원본 CSV 파일과 동일한 경우 오류를 출력하고 프로그램을 종료합니다.
//    if (newFile.equals(csvFile)) {
//      System.out.println("Error: cannot replace " + csvFile);
//      System.exit(0);
//    }
//
//    return newFile;
//  }
//}
