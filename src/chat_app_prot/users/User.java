package chat_app_prot.users;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import chat_app_prot.Message;

public class User {
	public static int USER_STATE_OFFLINE = 0;
	public static int USER_STATE_ONLINE = 1;
	
	private String name, email;
	private int userState = -1;
	private ArrayList<User> cached = new ArrayList<User>(); // this is where we log our recent users
	private ArrayList<Message> messageHistory = new ArrayList<Message>(); // this is a history logger
	private ArrayList<User> allUsers; // these are the users you are connected to.
	// we need a message queue to relay the messages to the client connected to this node...
	private Queue<Message> messageQueue = new LinkedList<Message>(); // this is gonna be our message queue
	private Queue<Message> pendingMessages = new LinkedList<Message>();
	
	public User(String name, String email, ArrayList<User> users) {
		this.email = email;
		this.name = name;
		this.allUsers = users;
//		this.allUsers.add(this); // append the user to the list of all users 
	}
	
	public String getUserName() {
		return this.name;
	}
	public String getUserEmail() {
		return this.email;
	}
	public ArrayList<User> getCache(){
		return this.cached;
	}
	public Queue<Message> getMessageQueue(){
		return this.messageQueue;
	}
	public Queue<Message> getPendingMessageQueue(){
		return this.pendingMessages;
	}
	public ArrayList<Message> getHistory(){
		return this.messageHistory;
	}
	public int getUserState() {return this.userState;}
	public void setUserState(int state) {this.userState = state;}
	
	public boolean signUp() {
		return this.allUsers.add(this);
	}
	public boolean appendMessage(Message message) {
		this.messageHistory.add(message);
		return this.messageQueue.add(message);
	}
	public boolean appendMessagePending(Message message) {
		return this.pendingMessages.add(message);
	}
	public void sendMessage(Message message) {
		// 1. figure out the recipient of the message
		// 2. Search for the recipient in the cached list
		// 3. if found, send message, else
		// 4. if not found,  look in the all users list, 
		// 5. Append the user to the cached list 
		// 6. Send Messages and return true
		if((message.getMessageType()&0x0F) == Message.SINGLE_REC) { // if this is a single recipient Message
			for(User user: cached) {
				if((user.getUserName().equals(message.getRecipient()[0])) || (user.getUserEmail().equals(message.getRecipient()[0]))) {
					// the user is in the cache ...
					user.appendMessage(message); // append the message to that users thread	
					return;
				}
			}
			for(User user: allUsers) { // search through the main list
				if((user.getUserName().equals(message.getRecipient()[0])) || (user.getUserEmail().equals(message.getRecipient()[0]))) {
					user.appendMessage(message); // append the message to that users thread	
					this.cached.add(user); // add the user to the cache
					return;
				}
			}
		}
	}
}
