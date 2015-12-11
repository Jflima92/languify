package logic;

import com.mongodb.Block;
import com.mongodb.MongoClientURI;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.InternationalFormatter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.bson.Document;

/**
 * Created by jorgelima on 12/4/15.
 */
public class Languifier {

    private String source;
    private MongoClientURI uri;
    private MongoClient mongoClient;
    private MongoDatabase mongoDB;
    private Map<String, Integer> db;

    public Languifier(){

        uri = new MongoClientURI("mongodb://admin:languifyadmin@ds061454.mongolab.com:61454/languify");
        mongoClient = new MongoClient(uri);
        mongoDB = mongoClient.getDatabase("languify");

    }

    public HashMap<String, Integer> wordNGramGenerator(int N, String source){
        String[] words = source.split("\\s+");
        int wordCount = words.length;
        HashMap<String, Integer> ngrams = new HashMap<>();

        System.out.println("N: " + N);
        System.out.println("count: " + wordCount);


        for(int i = wordCount; i >= N; i--){
            String prepare = "";
            for(int u = N; u>0; u--){
                prepare += words[i-u] + " ";
            }

            if (ngrams.containsKey(prepare)) {

                int currentCount = ngrams.get(prepare);
                ngrams.put(prepare, currentCount+1);
            }
            else
                ngrams.put(prepare,1);
        }

        return ngrams;
    }

    public void prepareAndAdd(Map<String, Integer> ngrams, int N, String source){
        for(int u = 0; u < source.length()-N; u++){
            String prep;

            prep = source.substring(u, u+N).toLowerCase();
            prep = prep.replaceAll("[^a-zA-Z. ]", "");

            if (ngrams.containsKey(prep)) {
                int count = ngrams.get(prep);
                ngrams.put(prep, count+1);
            }
            else {
                if(prep.contains(".")) {
                    prep = prep.replaceAll("\\.", "_");
                }

                if(!prep.equals("") && prep.length()==N && !prep.equals(" "))
                    ngrams.put(prep, 1);
            }

        }
    }

    public Map<String, Integer> localCharacterNGramGenerator(int N, String source){
        Map<String, Integer> ngrams = new HashMap<>();
        int chCount = source.length();
        System.out.println("character count: " + chCount);
        prepareAndAdd(ngrams, N, source);
        return ngrams;

    }
    public Map<String, Integer> mongoCharacterNGramGenerator(int N, String source, String language){
        String[] words = source.split("\\s+");
        int wordCount = words.length;
        int chCount = source.length();
//        HashMap<String, Integer> ngrams = new HashMap<>();

        MongoCollection collection;
        final Document[] doc = {null};
        final boolean[] exists = {false};

        if(collectionExists(language)){
            collection = mongoDB.getCollection(language);
        }
        else{
            System.out.println("no exists");
            mongoDB.createCollection(language);
            collection = mongoDB.getCollection(language);
        }

        System.out.println("N: " + N);

        System.out.println("character count: " + chCount);

        loadDBFromMongo(collection, N);

        FindIterable<Document> it = collection.find(new Document("N", N)).limit(1);

        it.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                doc[0] = document;
                exists[0] = true;
            }
        });

        if(exists[0]){

        }
        else{
            collection.insertOne(new Document("N", N));
        }

        prepareAndAdd(db, N, source);

        collection.updateOne(new Document("N", N), new Document("$set", new Document("grams", (Map) db)));
        return db;
    }

    public void loadDBFromMongo(MongoCollection collection, int N){
        FindIterable<Document> it = collection.find(new Document("N", N)).limit(1);
        final Document[] doc = {null};
        final boolean[] exists = {false};
        it.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                doc[0] = document;
                exists[0] = true;
            }
        });
        if(exists[0]){
            db = (Map<String, Integer>) doc[0].get("grams");
        }
        else
            db = new HashMap<>();

    }


    public void documentTraining(String pathToFile) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();

        byte[] encoded = Files.readAllBytes(Paths.get(classLoader.getResource(pathToFile).getPath()));
        String lan = pathToFile.split("/")[1].split("_")[0];
        System.out.println(lan);
        mongoCharacterNGramGenerator(2, new String(encoded), lan);
        mongoCharacterNGramGenerator(3, new String(encoded), lan);
        mongoCharacterNGramGenerator(4, new String(encoded), lan);
        mongoCharacterNGramGenerator(5, new String(encoded), lan);

    }

    public boolean collectionExists(final String collectionName) {
        MongoIterable<String> collectionNames = mongoDB.listCollectionNames();
        for (final String name : collectionNames) {
            if (name.equalsIgnoreCase(collectionName)) {
                return true;
            }
        }
        return false;
    }

    public HashMap sortByValues(Map map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public Map retrieveNGramsfromDB(int N, String language){
        MongoCollection collection = mongoDB.getCollection(language);
        FindIterable<Document> it = collection.find(new Document("N", N)).limit(1);
        final Document[] doc = {null};
        final boolean[] exists = {false};
        it.forEach(new Block<Document>() {
            @Override
            public void apply(final Document document) {
                doc[0] = document;
                exists[0] = true;
            }
        });
        if(exists[0]){
            return sortByValues((Map<String, Integer>) doc[0].get("grams"));
        }
        else
            return null;

    }



    public LinkedHashMap combineGramsData(boolean isLocal, String msg){
        LinkedHashMap combined = new LinkedHashMap<String, Integer>() {};
        ExecutorService executor = Executors.newFixedThreadPool(5);


        if(isLocal) {
            combined.putAll(localCharacterNGramGenerator(2, msg));
            combined.putAll(localCharacterNGramGenerator(3, msg));
            combined.putAll(localCharacterNGramGenerator(4, msg));
            combined.putAll(localCharacterNGramGenerator(5, msg));
        }
        else
        {
            ConcurrentHashMap c = new ConcurrentHashMap();
            for(int i = 2; i < 6; i++){
                Future<Map> fut = executor.submit(new worker(i, msg, this));
                try {
                    System.out.println("SIZE: " + fut.get().size());
                    c.putAll(fut.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                System.out.println("AQUI");
                System.out.println("CCCC:" + c.size());
                return new LinkedHashMap(c);
            }
           /* combined.putAll(retrieveNGramsfromDB(2, msg));
            combined.putAll(retrieveNGramsfromDB(3, msg));
            combined.putAll(retrieveNGramsfromDB(4, msg));
            combined.putAll(retrieveNGramsfromDB(5, msg));*/
            System.out.println(combined.size());
            return combined;
        }

        return combined;
    }





    public int compareRankings(LinkedHashMap local, LinkedHashMap db){
        ArrayList<String> localGrams = new ArrayList<>(sortByValues(local).keySet());
        ArrayList<String> dbGrams = new ArrayList<>(sortByValues(db).keySet());
        int sum = 0;

        for(int i = 0; i< localGrams.size(); i++) {
            int localRank = i;
            int dbRank = dbGrams.indexOf(localGrams.get(i));
            if(dbRank == -1)
                dbRank = 1000;

            sum += Math.abs(dbRank-localRank);
        }

        return sum;
    }

}
