package online.yjyy.gmall0508.list;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

    @Autowired
    JestClient jestClient;
    @Test
    public void contextLoads() {

        }
    @Test
    public void  testES() throws IOException {
/*    // 写一个操作语句
    String query = "{\n" +
            "    \"query\":{\n" +
            "      \"match\": {\"actorList.name\":\"张译\"}\n" +
            "    }\n" +
            "}";
    // 查询操作
    Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie").build();
    // 执行结果
    SearchResult searchResult = jestClient.execute(search);
    // 将结果集转换成Map
    List<SearchResult.Hit<HashMap, Void>> hits = searchResult.getHits(HashMap.class);
        for (SearchResult.Hit<HashMap, Void> hit : hits) {
        HashMap map = hit.source;
        Object name = map.get("name");
        System.out.println(name+"============");
        }


    }*/
}}
