package net.hydrogen2oxygen.diluter;

import org.junit.Test;

public class MainTest {

   @Test
   public void test() throws Exception {
      String[] args = {"data/testdata.csv","1","target/testdata-unique.csv","target/testdata-duplicate.csv","data/template-example.txt","target/extract.sql"};
      Main.main(args);
   }
}
