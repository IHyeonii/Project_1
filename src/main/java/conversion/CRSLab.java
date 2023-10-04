package conversion;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;

import java.io.File;

public class CRSLab { //좌표는 변경됐는데 값이 안들어감 ㅋ
  public static void main(String[] args) throws Exception {
    File inputFile = new File("C:\\Users\\ihyeon\\Desktop\\FirstTask\\Node_test.shp");

    // FileDataStore -> shp 파일 읽고 featureSource 구하기
    FileDataStore dataStore = FileDataStoreFinder.getDataStore(inputFile);
    SimpleFeatureSource featureSource = dataStore.getFeatureSource(); //FeatureSource -> 지리공간 정보를 Java 객체로 액세스 가능

    SimpleFeatureType schema = featureSource.getSchema();

    // 읽어온 파일의 Geometry를 찍어야 한다. = SimpleFeature 사용


    // 좌표체계 정의
    CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4162");
    CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:5179");

    // Allow for some error
    boolean lenient = true;

    // 좌표체계 변환
    MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, lenient);

    try (SimpleFeatureIterator iterator = featureSource.getFeatures().features()) {
      while (iterator.hasNext()) {
        SimpleFeature feature = iterator.next();

        // 형상 정보 가져오기
        Geometry geometry = (Geometry) feature.getDefaultGeometry();
        Geometry transform1 = JTS.transform(geometry, transform);

        // 가져온 형상 정보를 사용하거나 출력할 수 있습니다.
        System.out.println("형상 정보: " + geometry);
      }
    }
  }
}

