package instana_bcu;

import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class CreateDao {
	
	public void appInsert(List<Document> docs) {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase  database = mongoClient.getDatabase("instana_bcu");
		MongoCollection col = database.getCollection("app");
		col.insertMany(docs);
	}
	
	public void cpuInsert(List<Document> docs) {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase  database = mongoClient.getDatabase("instana_bcu");
		MongoCollection col = database.getCollection("cpu");
		col.insertMany(docs);
	}
	
	public void memoryInsert(List<Document> docs) {
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		MongoDatabase  database = mongoClient.getDatabase("instana_bcu");
		MongoCollection col = database.getCollection("memory");
		col.insertMany(docs);
	}
}
