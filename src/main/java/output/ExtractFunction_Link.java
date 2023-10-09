package output;

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
import java.util.*;

public class ExtractFunction_Link { // 통합링크 좌표를 못 읽어옴
  public static void main(String[] args) throws Exception {
    File nodeFile = new File("C:\\Users\\ihyeon\\Downloads\\ConvertCoord2\\ConvertCoord.shp");
    File linkFile = new File("C:\\Users\\ihyeon\\Downloads\\ConvertCoord2\\ConvertCoord2.shp");

    FileDataStore store = FileDataStoreFinder.getDataStore(nodeFile);
    SimpleFeatureSource source = store.getFeatureSource();

    FileDataStore dataStore = FileDataStoreFinder.getDataStore(linkFile);
    SimpleFeatureSource linkFeatureSource = dataStore.getFeatureSource();

    // 병합대상인 도곽 노드 Key: 좌표, Value: 도곽노드 id 2개
    Map<String, ArrayList<Long>> duplicateNode = new HashMap<>();

    // Key: 도곽 id인 경우
    Map<Long, ArrayList<Object>> nodeInfo = new HashMap<>();

    try {
      SimpleFeatureCollection featureCollection = source.getFeatures();
      SimpleFeatureIterator iterator = featureCollection.features();

      // 노드.shp 파일 한 줄씩 읽어서 중복노드 추출
      while (iterator.hasNext()) {
        SimpleFeature feature = iterator.next();
        Integer idxname1 = (Integer) feature.getAttribute("idxname");
        Integer nodeid1 = (Integer) feature.getAttribute("nodeid");
        Integer nodeattr = (Integer) feature.getAttribute("nodeattr");

        // Point의 좌표 추출 -> 이걸 도곽노드의 Key로 사용할거야
        Point point = (Point) feature.getAttribute(0);
        int x = (int) Math.round(point.getX());
        int y = (int) Math.round(point.getY());
        String key = x + "," + y;

        // idxname + nodeid = 고유값 생성
        String idxname = Integer.toString(idxname1);
        String nodeid = Integer.toString(nodeid1);
        long duplicateNodeId = Long.parseLong(idxname + nodeid);

        if (nodeattr == 7) { // nodeattr = 7; 도곽 노드 속성으로 데이터 검열해서 가져오기
          ArrayList<Object> arr = new ArrayList();
          arr.add(idxname1);
          arr.add(nodeid1);
          arr.add(nodeattr);
          arr.add(point);

          nodeInfo.put(duplicateNodeId, arr);

          if (duplicateNode.containsKey(key)) { // 이미 키(도곽 좌표)가 존재하는 경우
            duplicateNode.get(key).add(duplicateNodeId); // 기존 키에 노드 추가
          } else {
            // 중복 없으면 (새로운 키인 경우)
            ArrayList<Long> nodeArr = new ArrayList<>();
            nodeArr.add(duplicateNodeId);
            duplicateNode.put(key, nodeArr); // 새로운 키와 값을 생성
          }
        }
      }
      iterator.close(); // 노드 속성정보 및 도곽정보 추출 끝
      System.out.println("nodeInfo" + nodeInfo.size());

      /**
       * 링크 읽기
       * */
      SimpleFeatureCollection linkFeatureCollection = linkFeatureSource.getFeatures();
      SimpleFeatureIterator linkIterator = linkFeatureCollection.features();

      // 중복 노드와 일치하는 링크의 정보 저장 -> Key: 좌표 , Value: idxname+linkid (linkId)
      Map<String, ArrayList<Long>> duplicateLink = new HashMap<>(); // 중복 노드의 좌표, 링크id 2개 저장

      // 병합대상 아닌 링크 정보 저장소
      ArrayList<SimpleFeature> ret = new ArrayList<>();

      // 병합대상인 링크 정보
      Map<Long, ArrayList<Object>> linkToMerge = new HashMap<>();

      // 링크.shp 한 줄씩 읽기
      while (linkIterator.hasNext()) {
        SimpleFeature next = linkIterator.next();
        Integer idxname = (Integer) next.getAttribute("idxname");
        Integer linkid = (Integer) next.getAttribute("linkid");
        Integer stndid = (Integer) next.getAttribute("stndid");
        Integer edndid = (Integer) next.getAttribute("edndid");
        MultiLineString multiLineString = (MultiLineString) next.getAttribute(0);
        LineString lineString = (LineString) multiLineString.getGeometryN(0);

        // idxname + nodeid = 고유값 생성
        String idxname1 = Integer.toString(idxname);
        String linkid1 = Integer.toString(linkid);
        String stndid1 = Integer.toString(stndid);
        String edndid1 = Integer.toString(edndid);

        long fromNode = Long.parseLong(idxname1 + stndid1);
        long toNode = Long.parseLong(idxname1 + edndid1);

        // 링크정보 담을거야
        long linkId = Long.parseLong(idxname1 + linkid1);

        // 도곽 id와 링크의 st가 일치하면 -> 병합대상인 링크
        if (nodeInfo.containsKey(fromNode)) {
          Point point = lineString.getStartPoint();
          int x = (int) Math.round(point.getX());
          int y = (int) Math.round(point.getY());
          String key = x + "," + y;

          if (duplicateLink.containsKey(key)) {
            duplicateLink.get(key).add(linkId);
          } else {
            // 중복 없으면 (새로운 키인 경우)
            ArrayList<Long> linkArr = new ArrayList<>();
            linkArr.add(linkId);
            duplicateLink.put(key, linkArr); // 새로운 키와 값을 생성
          }

        } else if (nodeInfo.containsKey(toNode)) {
          Point point = lineString.getEndPoint();
          int x = (int) Math.round(point.getX());
          int y = (int) Math.round(point.getY());
          String key = x + "," + y;

          if (duplicateLink.containsKey(key)) {
            duplicateLink.get(key).add(linkId);
          } else {
            ArrayList<Long> linkArr = new ArrayList<>();
            linkArr.add(linkId);
            duplicateLink.put(key, linkArr);
          }
        } else { // 도곽이 아닌 경우
          ret.add(next);
          continue;
        }

        // 도곽과 연결된 링크 정보만 담기
        ArrayList<Object> linkInfo = new ArrayList<>();
        linkInfo.add(idxname);
        linkInfo.add(linkid);
        linkInfo.add(fromNode);
        linkInfo.add(toNode);
        linkInfo.add(lineString);

        linkToMerge.put(linkId, linkInfo);
      }
      linkIterator.close();

      /**
       * 도곽 노드와 연결된 "링크 병합"
       * */

      // coordinates 저장할게 필요해
      ArrayList<Coordinate[]> linkCoors = new ArrayList<>();

      for (String key : duplicateLink.keySet()) {
        ArrayList<Long> arrNode = duplicateNode.get(key);
        ArrayList<Long> arrLink = duplicateLink.get(key); // 둘 다 도곽의 좌표
        if (arrLink.size() != 2) { // arrLink(좌표) 2개여야 도곽과 일치하는 링크 -> 895224,1475821
          continue;
        }

        // 링크의 도곽 id 정보 추출
        long link1 = arrLink.get(0); // 1번 좌표
        System.out.println("link1= " + link1);
        System.out.println("link1= " + arrLink.get(0).toString());
        ArrayList<Object> arr = linkToMerge.get(link1);
        LineString ls1 = (LineString) arr.get(4); // 형상
        long link1snode = (long) arr.get(2);
        long link1enode = (long) arr.get(3);

        long link2 = arrLink.get(1); // 2번 좌표
        ArrayList<Object> arr1 = linkToMerge.get(link2);
        LineString ls2 = (LineString) arr1.get(4); // 형상
        long link2snode = (long) arr.get(2);
        long link2enode = (long) arr.get(3);

        Long node1 = arrNode.get(0);
        Long node2 = arrNode.get(1);

        // 방향이 같은 경우
        // arrNode == 라인1의 end && 라인2의 start 이거나 : link1 -> link2
        if ((node1.equals(link1enode) && node2.equals(link2snode)) || (node2.equals(link1enode) && node1.equals(link2snode))) {
          Coordinate[] coordinates1 = ls1.getCoordinates();// 링크1의 모든 절점
          Coordinate[] coordinates2 = ls2.getCoordinates();
          System.out.println("" + coordinates1);

          // 방을 만들어 줬다.
          Coordinate[] coordinates = new Coordinate[coordinates1.length + coordinates2.length - 1]; // 전체 절점의 갯수
          int i = 0;
          for (int j = 0; j < coordinates1.length; j++) { // 방에다 데이터를 넣어준다.
            coordinates[i++] = coordinates1[j]; // coordinate 방의 0번 부터 절점의 0번 넣어주면서 반복
          }
          for (int j = 1; j < coordinates2.length; j++) {
            coordinates[i++] = coordinates2[j];
          }
          linkCoors.add(coordinates);
          System.out.println("coordinates=" + coordinates);
        }

        // arrNode == 라인1의 start 이거나 && 라인2의 end : link2 -> link1
        if ((node1.equals(link1snode) && node2.equals(link2enode)) || (node2.equals(link1snode) && node1.equals(link2enode))) {
          Coordinate[] coordinates1 = ls1.getCoordinates(); // 링크1의 모든 절점
          Coordinate[] coordinates2 = ls2.getCoordinates();

          Coordinate[] coordinates = new Coordinate[coordinates2.length + coordinates1.length - 1]; // 전체 절점의 갯수
          int i = 0;
          for (int j = 0; j < coordinates2.length; j++) { // 방에다 데이터를 넣어준다.
            coordinates[i++] = coordinates2[j]; // coordinate 방의 0번 부터 절점의 0번 넣어주면서 반복
          }

          for (int j = 1; j < coordinates1.length; j++) {
            coordinates[i++] = coordinates1[j];
          }
          linkCoors.add(coordinates);
        }

        // 방향이 다른 경우
        // arrNode == 라인 1의 start && 라인2의 start
        if ((node1.equals(link1snode) && node1.equals(link2snode)) || (node2.equals(link1snode) && node2.equals(link2snode))) {
          LineString reverse = ls1.reverse();
          Coordinate[] reverseCoordinatesOfLink1 = reverse.getCoordinates();
          Coordinate[] ls2Coordinates = ls2.getCoordinates(); // 링크의 절점 다 가져와서

          // Coordinate 방을 만들어서 다 추가해줘야지
          Coordinate[] coordinates = new Coordinate[reverseCoordinatesOfLink1.length + ls2Coordinates.length - 1]; // 전체 절점의 갯수
          int i = 0;
          for (int j = 0; j < reverseCoordinatesOfLink1.length; j++) { // 방에다 데이터를 넣어준다.
            coordinates[i++] = reverseCoordinatesOfLink1[j]; // coordinate 방의 0번 부터 절점의 0번 넣어주면서 반복
          }
          for (int j = 1; j < ls2Coordinates.length; j++) {
            coordinates[i++] = ls2Coordinates[j];
          }
          linkCoors.add(coordinates);
        }

        // arrNode == 라인 1의 end && 라인2의 end
        if ((node1.equals(link1enode) && node1.equals(link2enode)) || (node2.equals(link1enode) && node2.equals(link2enode))) {
          LineString reverse = ls1.reverse();
          Coordinate[] reverseCoordinatesOfLink1 = reverse.getCoordinates();
          Coordinate[] ls2Coordinates = ls2.getCoordinates(); // 링크의 절점 다 가져와서

          Coordinate[] coordinates = new Coordinate[reverseCoordinatesOfLink1.length + ls2Coordinates.length - 1]; // 전체 절점의 갯수
          int i = 0;
          for (int j = 0; j < reverseCoordinatesOfLink1.length; j++) { // 방에다 데이터를 넣어준다.
            coordinates[i++] = reverseCoordinatesOfLink1[j]; // coordinate 방의 0번 부터 절점의 0번 넣어주면서 반복
          }
          for (int j = 1; j < ls2Coordinates.length; j++) {
            coordinates[i++] = ls2Coordinates[j];
          }
          linkCoors.add(coordinates);
        }
      }
      System.out.println("linkCoors= " + linkCoors.size());
//      System.out.println("linkCoors= " + Arrays.toString(linkCoors.get(0)));
//      System.out.println("ret: " +ret.size());
      System.out.println("mergeLink: " + linkToMerge.size());
      exportNodeShp(ret, linkCoors, linkToMerge);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  // 5. output shapefile
  static void exportNodeShp(ArrayList<SimpleFeature> ret, ArrayList<Coordinate[]> linkCoors, Map<Long, ArrayList<Object>> linkToMerge) throws Exception { // 여긴 받을 준비
    // 새로운 shp 파일 저장경로 및 파일명 설정
    File newFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\output\\IncludeLink2.shp");

    // SimpleFeatureType 생성 =  CSV 파일에서 읽어온 데이터를 설명, 속성 유형과 구조 정의
    final SimpleFeatureType TYPE =
        DataUtilities.createType( // DataUtilities 사용
            "LinkFile",
            "the_geom:LineString:srid=5179,"
                + // <- the geometry attribute
                "idxname:Integer," + "linkid:Integer," + "stndid:Integer," + "edndid:Integer"
        );
    System.out.println("TYPE:" + TYPE);

    List<SimpleFeature> features = new ArrayList<>();

    GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

    SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);

    for (SimpleFeature originalLink : ret) {
      MultiLineString multiLineString = (MultiLineString) originalLink.getAttribute(0);
      LineString originalLine = (LineString) multiLineString.getGeometryN(0);

      Coordinate[] originalCoors = originalLine.getCoordinates();

      LineString orgLineString = geometryFactory.createLineString(originalCoors);

      Integer idxname = (Integer) originalLink.getAttribute("idxname");
      Integer linkid = (Integer) originalLink.getAttribute("linkid");
      Integer stndid = (Integer) originalLink.getAttribute("stndid");
      Integer edndid = (Integer) originalLink.getAttribute("edndid");

      featureBuilder.add(orgLineString);
      featureBuilder.add(idxname);
      featureBuilder.add(linkid);
      featureBuilder.add(stndid);
      featureBuilder.add(edndid);

      SimpleFeature feature = featureBuilder.buildFeature(null);
      features.add(feature);
    }

    // 병합링크 추가
    Iterator<Coordinate[]> iterator = linkCoors.iterator();
    while (iterator.hasNext()) {
      Coordinate[] next = iterator.next();

      Set<Map.Entry<Long, ArrayList<Object>>> entries = linkToMerge.entrySet();
      for (Map.Entry<Long, ArrayList<Object>> entrySet : entries) {
        SimpleFeature value = (SimpleFeature) entrySet.getValue();

        Integer idxname = (Integer) value.getAttribute("idxname");
        Integer linkid = (Integer) value.getAttribute("linkid");
        Integer stndid = (Integer) value.getAttribute("fromNode");
        Integer edndid = (Integer) value.getAttribute("toNode");

        LineString mergeLineString = geometryFactory.createLineString(next);

        featureBuilder.add(mergeLineString);
        featureBuilder.add(idxname);
        featureBuilder.add(linkid);
        featureBuilder.add(stndid);
        featureBuilder.add(edndid);

        SimpleFeature feature = featureBuilder.buildFeature(null);
        features.add(feature);
      }
    }



    ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

    Map<String, Serializable> params = new HashMap<>();
    params.put("url", newFile.toURI().toURL());
    params.put("create spatial index", Boolean.TRUE);

    DataStore newDataStore = dataStoreFactory.createNewDataStore(params);
    newDataStore.createSchema(TYPE); // TYPE -> 파일 내용 설명하는 템플릿으로 사용

    // ShapeFile에 feature data 작성
    Transaction transaction = new DefaultTransaction("create");

    String typeName = newDataStore.getTypeNames()[0];
    SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
    SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

    System.out.println("SHAPE:" + SHAPE_TYPE);

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
}

