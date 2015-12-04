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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public Map<String, Integer> characterNGramGenerator(int N, String source, String language){
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
        else
        {
            collection.insertOne(new Document("N", N));
        }

        for(int i = 0; i < source.length()-N; i++){
            String prepare;

            prepare = source.substring(i, i+N).toLowerCase();
            prepare = prepare.replaceAll("[^a-zA-Z. ]", "");


            if (db.containsKey(prepare)) {
                int count = db.get(prepare);
                db.put(prepare, count+1);
            }
            else {
                if(prepare.contains(".")) {

                    prepare = prepare.replaceAll("\\.", "_");

                }

                if(!prepare.equals("") && prepare.length()==N && !prepare.equals(" "))
                    db.put(prepare, 1);
            }

        }

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

    /*public void mongoCharacterNGramGenerator(int N, String source, String language) {
        String[] words = source.split("\\.+\\s+");
        int wordCount = words.length;
        int chCount = source.length();
        MongoCollection collection;

        System.out.println("N: " + N);

        System.out.println("character count: " + chCount);

        if(collectionExists(language)){
            collection = mongoDB.getCollection(language);
        }
        else{
            System.out.println("no exists");
            mongoDB.createCollection(language);
            collection = mongoDB.getCollection(language);
        }

        for (int i = 0; i < source.length() - N+1; i++) {
            String prepare;
            final Document[] doc = {null};
            final boolean[] exists = {false};
            prepare = source.substring(i, i + N);

            FindIterable<Document> it = collection.find(new Document("N", N)).limit(1);

            it.forEach(new Block<Document>() {
                @Override
                public void apply(final Document document) {
                    doc[0] = document;
                    exists[0] = true;
                }
            });


            if(exists[0]){
                Document newd = (Document) doc[0].get("grams");
                if(newd.containsKey(prepare)) {
                    int count = (int) newd.get(prepare);
                    collection.updateOne(new Document("N", N), new Document("$set", new Document("grams." + prepare, count + 1)));
                }
                else{
                    collection.updateOne(new Document("N", N), new Document("$set", new Document("grams." + prepare, 1)));
                }
            }
            else
            {
                collection.insertOne(new Document("N", N).append("grams", new Document(prepare, 1)));
            }



        }



    }*/

   /* public String prepareGram(int N, String source){

    }*/

    public void documentTraining(String pathToFile) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();

        byte[] encoded = Files.readAllBytes(Paths.get(classLoader.getResource(pathToFile).getPath()));
        characterNGramGenerator(4, new String(encoded), "english");

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


}
