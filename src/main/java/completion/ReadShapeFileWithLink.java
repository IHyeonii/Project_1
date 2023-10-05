package completion;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReadShapeFileWithLink {
  public static void main(String[] args) throws Exception {
    File nodeFile = new File("C:\\Users\\ihyeon\\Downloads\\ConvertCoord2\\ConvertCoord.shp");
//    File linkFile = new File("C:\\Users\\ihyeon\\Downloads\\ConvertCoord2\\ConvertCoord2.shp");

    FileDataStore store = FileDataStoreFinder.getDataStore(nodeFile);
    SimpleFeatureSource source = store.getFeatureSource();

    // 도곽노드 = 좌표가 중복 = 좌표를 key로 저장 -> map 중복키 제거
    // 키 : 좌표, 값: 노드id 2개
    Map<String, ArrayList<Integer>> duplicateNode = new HashMap<>();


    try {
      SimpleFeatureCollection featureCollection = source.getFeatures();
      SimpleFeatureIterator iterator = featureCollection.features();

        while (iterator.hasNext()) {
        SimpleFeature feature = iterator.next(); // shp 파일 한 줄씩 읽어올거야
//          System.out.println(feature);
//        nodeAttribute.add(feature.getAttributes());
//        Point point = (Point) feature.getAttribute(0);
//        System.out.println(point);
          // [POINT (885653.9227541254 1473920.033272428), 41340000, 1, 7, ] = nodeAttribute(0)

          String coordinateValue = feature.getDefaultGeometry().toString(); // 형상좌표가 Key
          Integer idxname = (Integer) feature.getAttribute("idxname");
          Integer nodeid = (Integer) feature.getAttribute("nodeid");
          Integer nodeattr = (Integer) feature.getAttribute("nodeattr");

          int nodeNumber = idxname + nodeid; // Value

          if (nodeattr == 7) { //
            if (duplicateNode.containsKey(coordinateValue)) { // 값을 갖고 있으면
              // duplicateNode 키를 가져와서, 해당 값과 일치하는 nodeid를 add
              duplicateNode.get(coordinateValue).add(nodeid); // 맵에다 해당 좌표값의 노드 id를 add
            } else { // 중복 없으면
              ArrayList<Integer> nodeArr = new ArrayList<>();
              nodeArr.add(nodeid);
              duplicateNode.put(coordinateValue, nodeArr);
            }
          }


            // 방향을 바꾸는게 아니라, shp 파일 겨우 읽어서 중복된 키, 값을 map에 담지도 못 하는 상황
            // 설계 방향
            // 1. node_attr == 7: 노드의 속성이 7번인 노드 id를 가져온다.
            // 7번 속성의 위치를 찾아? 아님 문자열을 추출해 ?
            // 2. 좌표(형상정보)를 키로 설정해서 해당 키가 있으면(중복) -> 그 키에 nodeid 담아
            // 3. map에 키가 없으면 (중복 아니면) -> map에 새로운 키, 값으로 put 해
            // 4. 그 다음, map.size() == 2 인 도곽아이디의 링크를 가져온다.
            // 5. 링크 방향이 같으면 키값 제거 -> 새로운 라인 생성하고
            // 6. 링크 방향이 다르면, 길이가 긴 걸 기준으로 짧은 쪽을 reverse 해서 라인 생성한다.
          }
      System.out.println(duplicateNode);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
