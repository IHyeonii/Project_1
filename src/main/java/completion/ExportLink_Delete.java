package completion;

import org.geotools.api.data.*;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportLink_Delete { // 병합정보 추출 완료, 이제 방향 정해서 통합링크 구성하기
  public static ArrayList<SimpleFeature> searchLink(Long nodeA, Long nodeB, SimpleFeatureCollection featureCollection1) {
    SimpleFeatureIterator linkIterator = featureCollection1.features();
    ArrayList<SimpleFeature> ret = new ArrayList<>();
    while (linkIterator.hasNext()) {
      SimpleFeature next = linkIterator.next();
      Integer idxname = (Integer) next.getAttribute("idxname");
      Integer stndid = (Integer) next.getAttribute("stndid");
      Integer edndid = (Integer) next.getAttribute("edndid");
      Integer length = (Integer) next.getAttribute("length");
      Integer linkid = (Integer) next.getAttribute("linkid");

      // idxname + nodeid = 고유값 생성
      String idxname1 = Integer.toString(idxname);
      String stndid1 = Integer.toString(stndid);
      String edndid1 = Integer.toString(edndid);

      long fromNode = Long.parseLong(idxname1 + stndid1);
      long toNode = Long.parseLong(idxname1 + edndid1);

      if (nodeA == fromNode || nodeA == toNode) {
        ret.add(next); // A를 찾았다.
      }

      if (nodeB == fromNode || nodeB == toNode) {
        ret.add(next);
      }

      if (ret.size() == 2) {
        break;
      }
    }
    linkIterator.close();
    return ret;
  }
  public static void main(String[] args) throws Exception {
    File nodeFile = new File("C:\\Users\\ihyeon\\Downloads\\ConvertCoord2\\ConvertCoord.shp");
    File linkFile = new File("C:\\Users\\ihyeon\\Downloads\\ConvertCoord2\\ConvertCoord2.shp");

    FileDataStore store = FileDataStoreFinder.getDataStore(nodeFile);
    SimpleFeatureSource source = store.getFeatureSource();

    FileDataStore dataStore = FileDataStoreFinder.getDataStore(linkFile);
    SimpleFeatureSource linkFeatureSource = dataStore.getFeatureSource();

    Map<String, ArrayList<Long>> duplicateNode = new HashMap<>();
    Map<String, ArrayList<SimpleFeature>> hashMap = new HashMap<>();

    try {
      SimpleFeatureCollection featureCollection = source.getFeatures();
      SimpleFeatureIterator iterator = featureCollection.features();

      while (iterator.hasNext()) {
        SimpleFeature feature = iterator.next(); // shp 파일 한 줄씩 읽어올거야
        Integer idxname = (Integer) feature.getAttribute("idxname");
        Integer nodeid = (Integer) feature.getAttribute("nodeid");
        Integer nodeattr = (Integer) feature.getAttribute("nodeattr");

        // Point 좌표
        Point point = (Point) feature.getAttribute(0);
        int x = (int) Math.round(point.getX());
        int y = (int) Math.round(point.getY());
        String key = x + "," + y;

        // idxname + nodeid = 고유값 생성
        String idxname1 = Integer.toString(idxname);
        String nodeid1 = Integer.toString(nodeid);
        long nodeNumber = Long.parseLong(idxname1 + nodeid1);

        if (nodeattr == 7) { //
          if (duplicateNode.containsKey(key)) { // 값을 갖고 있으면
            // duplicateNode 키 가져와서, 해당 값과 일치하는 nodeid를 add
            duplicateNode.get(key).add(nodeNumber);
          } else {
            // 중복 없으면
            ArrayList<Long> nodeArr = new ArrayList<>();
            nodeArr.add(nodeNumber);
            duplicateNode.put(key, nodeArr);
          }
        }
      }
      iterator.close();

      for (Map.Entry<String, ArrayList<Long>> entrySet : duplicateNode.entrySet()) {
        if (entrySet.getValue().size() != 2) {
          continue;
        } // 이 코드 없으면 Index 1 out of bounds for length 1

        String key = entrySet.getKey(); // 키 = 도곽의 좌표(포인트)
        ArrayList<Long> nodeIds = entrySet.getValue(); // 값: 도과 노드id1, 노드id2
        Long nodeA = nodeIds.get(0);
        Long nodeB = nodeIds.get(1);

        SimpleFeatureCollection linkFeatureCollection = linkFeatureSource.getFeatures();
        ArrayList<SimpleFeature> ret = searchLink(nodeA, nodeB, linkFeatureCollection);

        if (ret.size() == 2) {
          hashMap.put(key, ret); // 병합대상을 얻어왔다
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    makeLink(hashMap, linkFile);
  }

  // makeLink 메서드 호출할 때 같이 넘겨줄 (타입 변수명)
  // ( 소괄호 ) 안의 타입을 넘겨받으면 메서드 실행
  static void makeLink(Map<String, ArrayList<SimpleFeature>> hashMap, File linkFile) throws Exception {
    // 병합대상 출력 완료 => 이제
    final SimpleFeatureType TYPE =
        DataUtilities.createType( // DataUtilities 사용
            "newLinkFile",
            "the_geom:LineString:srid=5179,"
            + "name:String"
        );
    System.out.println("TYPE:" + TYPE);

    List<SimpleFeature> features = new ArrayList<>();

    for (Map.Entry<String, ArrayList<SimpleFeature>> entrySet : hashMap.entrySet()) {
      ArrayList<SimpleFeature> value = entrySet.getValue();
      SimpleFeature featureA = value.get(0); // 링크 1번
      SimpleFeature featureB = value.get(1); // 링크 2번

      // 링크 A의 좌표정보
      MultiLineString multiLineString = (MultiLineString) featureA.getAttribute(0);
      LineString lineString1 = (LineString) multiLineString.getGeometryN(0);
//      System.out.println(Arrays.toString(lineString1.getCoordinates())); // Coordinate는 하나만 출력

      // 링크 B도..
      MultiLineString multiLineString2 = (MultiLineString) featureB.getAttribute(0);
      LineString lineString2 = (LineString) multiLineString2.getGeometryN(0);
//      System.out.println(Arrays.toString(lineString1.getCoordinates()));

      //
      Point startPoint = lineString1.getStartPoint();
      Point endPoint = lineString2.getEndPoint();

      GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

      Coordinate[] points = new Coordinate[2];
      points[0] = new Coordinate(startPoint.getX(), startPoint.getY());
      points[1] = new Coordinate(endPoint.getX(), endPoint.getY());
      LineString lineString = geometryFactory.createLineString(points);

      Integer length = (Integer) featureA.getAttribute("length");
//      System.out.println(length);
      // 양쪽좌표를 갖고, 두개를 합친다
      SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
      featureBuilder.add(lineString);
      featureBuilder.add("line");

      SimpleFeature feature = featureBuilder.buildFeature(null);
      features.add(feature); // 합친걸 여기다 add해
    }

    // 저장
    File newFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\output\\output3.shp");

    ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

    Map<String, Serializable> params = new HashMap<>();
    params.put("url", newFile.toURI().toURL());
    params.put("create spatial index", Boolean.TRUE);

    DataStore newDataStore = dataStoreFactory.createNewDataStore(params);

    // 3-2. ShapeFile 설정 위한 createSchema(SimpleFeatureType) 메서드 사용
    newDataStore.createSchema(TYPE); // TYPE -> 파일 내용 설명하는 템플릿으로 사용

    // 4. ShapeFile에 feature data 작성
    Transaction transaction = new DefaultTransaction("create");

    String typeName = newDataStore.getTypeNames()[0];
    SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
    SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

    System.out.println("SHAPE:" + SHAPE_TYPE); // 타입이 Point로 넘어와..

    // 기존 라인 그대로 들고 오고
    // 새로합친거 추가해서 shp 만든다. ?????????????????????????????????


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

  static void makeLineString(Map<String, ArrayList<SimpleFeature>> hashMap) {
    // 위에서 해도 된데
    // 이제 방향을 찾아서 바꾸고, 로직을 설계해서 컴펌을 받고, 코딩을 해보겠습니다.
    //
  }
}
