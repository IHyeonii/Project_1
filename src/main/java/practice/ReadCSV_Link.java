package practice;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class ReadCSV_Link {
  public static void main(String[] args) throws Exception {
    File targetFile = new File("C:\\Users\\ihyeon\\Desktop\\Qbic_1stTask\\Link_test.csv");

    BufferedReader reader = new BufferedReader(new FileReader(targetFile));

    CSVReader openCSVReader = new CSVReader(reader);

    String[] str;

    ArrayList<NodeData> datas = new ArrayList<>();

    try {
      String[] header  = openCSVReader.readNext(); //처음 필드명 빼고

      while ((str = openCSVReader.readNext()) != null) {
        NodeData nodeData = new NodeData();

        // 읽어온 파일 String으로 들어오니까 int 형변환
        nodeData.setId(Integer.parseInt( str[0]));
        nodeData.setNodeId(Integer.parseInt( str[1]));
        nodeData.setNodeAttribute(Integer.parseInt( str[2]));
        nodeData.setNodeName(str[3]);
        // 이렇게 한 줄 씩 읽어서 반복한 걸

        datas.add(nodeData); // 만들어 둔 datas에 담아
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(datas);
  }
}
