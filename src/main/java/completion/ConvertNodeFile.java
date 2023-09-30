package completion;

import com.opencsv.CSVReader;
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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertNodeFile {
  public static void main(String[] args) throws Exception {

    File file = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\Node_test.csv");
    File linkFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\Link_test.csv");

    // 1. FeatureType 생성
    // SimpleFeatureType 생성 =  CSV 파일에서 읽어온 데이터를 설명, 속성 유형과 구조 정의
    final SimpleFeatureType TYPE =
        DataUtilities.createType( // DataUtilities 사용
            "NodeFile",
            "the_geom:Point:srid=4162,"
                + // <- the geometry attribute
                "idxname:Integer,"
                + // <-  attribute
                "nodeid:Integer," + "nodeattr:Integer," + "ndname:String"
        );
    System.out.println("TYPE:" + TYPE);

    // 2. Features 생성
    // 이제 CSV 파일을 읽고 각 줄을 파싱하여 데이터 추출하기
    List<SimpleFeature> features = new ArrayList<>();

    // 2-1. 새로운 포인트 생성을 위한 GeometryFactory 사용
    // GeometryFactory는 각 feature의 지오메트리 속성을 생성하는 데 사용
    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    // 2-2. SimpleFeatureBuilder 사용하여 Features(SimpleFeature 객체) 생성
    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      CSVReader csvReader = new CSVReader(reader); // 노드파일 읽기

      BufferedReader linkReader = new BufferedReader(new FileReader(linkFile));
      CSVReader linkCsvReader = new CSVReader(linkReader); // 링크파일 읽기

      // 링크에서 추출한 노드 형상 담을 자료구조
      Map<Integer, Coordinate> map = new HashMap<>();

      /* First line of the data file is the header */
      String[] line = csvReader.readNext();
      String[] linkLine = linkCsvReader.readNext();

      /*
      * 1. while문을 두 번 따로 돌아야 한다. -> 각각 별개의 파일이라 하나의 while 문에서 돌면 ..?
      * 2. 링크를 먼저 읽어서 노드 형상을 얻는다.
      * 3. 노드 형상을 사용할 수 있도록 while 문 밖에 선언 된 자료구조에 담는다 (ex. Map)
      * 4. 노드를 읽으면서 노드의 키값 == 링크의 키값 ; Point 객체에 담아서 add 한다.
      * */
      while ((linkLine = linkCsvReader.readNext()) != null) {  // 링크 먼저 읽기
        Integer idxname = Integer.parseInt(linkLine[0]);
        Integer stndid = Integer.parseInt(linkLine[2]);
        Integer edndid = Integer.parseInt(linkLine[3]);

        Integer stNode = idxname+stndid;
        Integer endNode = idxname+edndid;

        // Link에서 형상만 추출
        String lineString = linkLine[14];
        int start = lineString.indexOf('(');
        int end = lineString.indexOf(')');
        String result = lineString.substring(start+1, end); // 괄호 안의 숫자만 출력

        String[] sep = result.split(",| ");

        // 처음과 끝 값만 추출: 경도(long) 위도(latti) 순서
        double stLongitude = Double.parseDouble(sep[0]); // 경도
        double stLatitude = Double.parseDouble(sep[1]); // 위도
        double endLongitude = Double.parseDouble(sep[sep.length-2]); // 경도 끝값
        double endLatitude = Double.parseDouble(sep[sep.length-1]); // 위도 끝값

        Coordinate startCoord = new Coordinate(stLongitude, stLatitude);
        Coordinate endCoord = new Coordinate(endLongitude, endLatitude);

        map.put(stNode, startCoord);
        map.put(endNode, endCoord);

        /**
         * 노드 정보 추출하기
         * */
        while ((line = csvReader.readNext()) != null) {
          Integer idx = Integer.parseInt(line[0]);
          Integer nodeid = Integer.parseInt(line[1]);
          Integer nodeattr = Integer.parseInt(line[2]);
          String ndname = line[3];

          Integer NodeNumber = idx + nodeid;

          Point point = geometryFactory.createPoint();

          if (NodeNumber ==  stNode) {
            point = geometryFactory.createPoint(map.put(stNode, startCoord));
          } else if (NodeNumber == endNode) {
            point = geometryFactory.createPoint(map.put(endNode, endCoord));
          }

          // featureBuilder 객체를 사용하여 feature에 추가
          featureBuilder.add(idxname);
          featureBuilder.add(nodeid);
          featureBuilder.add(nodeattr);
          featureBuilder.add(ndname);
          featureBuilder.add(point);

          // featureBuilder 사용하여 SimpleFeature 객체 생성 -> 여기에 모든 데이터가 포함 됨
          SimpleFeature feature = featureBuilder.buildFeature(null);

          // 생성한 feature를 features 리스트에 추가
          features.add(feature);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    // 3. Create a shapefile From a FeatureCollection
    // getNewShapeFile 메서드 호츌
    File newFile = getNewShapeFile(file);

    // 3-1.DataStoreFactory -> 원하는 공간 인덱스를 표시하는 매개변수와 함께 사용해야 함
    ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

    Map<String, Serializable> params = new HashMap<>();
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

    System.out.println("SHAPE:" + SHAPE_TYPE); // 타입이 Point로 넘어와..

    if (featureSource instanceof SimpleFeatureStore) {
      SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
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
  private static File getNewShapeFile(File csvFile) { //getNewShapeFile 호출될 때 매개변수로 전달되는 파일
    File newFile = new File("");

    // 3. 만약 새로운 파일이 원본 CSV 파일과 동일한 경우 오류를 출력하고 프로그램을 종료합니다.
    if (newFile.equals(csvFile)) {
      System.out.println("Error: cannot replace " + csvFile);
      System.exit(0);
    }
    return newFile;
  }
}
