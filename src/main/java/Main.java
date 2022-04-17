import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class Main {

    // Метод записи файла
    public static boolean writeFile(String filePath, String sets) {
        FileWriter fw;
        try {
            fw = new FileWriter(filePath);
            PrintWriter out = new PrintWriter(fw);
            out.write(sets);
            out.println();
            fw.close();
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Метод конвертирования XML в объект Java
    public static List<Employee> parseXML(Node root) {
        List<Employee> list = new ArrayList<>();
        NodeList nodeList = root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node_ = nodeList.item(i);

            if (Node.ELEMENT_NODE == node_.getNodeType() && node_.getNodeName().equals("employee")) {
                Element element = (Element) node_.getChildNodes();
                String[] context = element.getTextContent().replaceAll(" ", "").split("\n");
                list.add(new Employee(Long.valueOf(context[1]), context[2], context[3], context[4], Integer.valueOf(context[5])));
            }
            parseXML(node_);
        }
        return list;
    }

    //Метод чтения JSON-файла
    public static JSONArray readString(String fileName) {
        JSONParser parser = new JSONParser();
        JSONArray arr = new JSONArray();

        try {
            Object obj = parser.parse(new FileReader(fileName));
            arr = (JSONArray) obj;

        } catch (IOException | ParseException e){
            e.printStackTrace();
        }

        return arr;
    }

    //Метод конвертирования JSON в Java-object
    public static List<Employee> jsonToList(JSONArray arr) {
        List<Employee> list = new ArrayList<>();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        for (int i = 0; i < arr.size(); i++) {
            String str = arr.get(i).toString();
            Employee emp = gson.fromJson(str, Employee.class);
            list.add(emp);
        }
        return list;
    }

    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileNameCsv = "data.csv";
        String fileNameXml = "data.xml";
        String fileNameJson = "data2.json";

        /*
             CSV to JSON
         */
        try (CSVReader csvReader = new CSVReader(new FileReader(fileNameCsv)); FileWriter fileWriter = new FileWriter("data.json")) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();

            List<Employee> list = csv.parse();

            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            String json = gson.toJson(list, new TypeToken<List<Employee>>() {
            }.getType());

            Main.writeFile("data.json", FormatJsonUtils.formatJson(json));

            /*
             XML to JSON
            */
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builderXML = factory.newDocumentBuilder();
            Document doc = builderXML.parse(new File(fileNameXml));
            Node root = doc.getDocumentElement();

            list = parseXML(root);
            json = gson.toJson(list, new TypeToken<List<Employee>>() {
            }.getType());
            Main.writeFile("data2.json", FormatJsonUtils.formatJson(json));

            /*
             JSON to Java-object
            */

            list = jsonToList(readString(fileNameJson));
            for (Employee employee : list) {
                System.out.println(employee.toString());
            }
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }
}
