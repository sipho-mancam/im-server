package chat_app_prot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import chat_app_prot.users.User;

public class ClientsHandle  extends Thread{
	
	private ObjectOutputStream outStream;
	private ObjectInputStream inputStream;
	private final Socket client;
	private Message tempMessage, sentMessage;
	private User user;
	private ArrayList<User> allUSers;
	
	
	public ClientsHandle(Socket client, ArrayList<User> allUsers) {
		this.client = client;
		this.allUSers = allUsers;
	}
	
	public void run() {
		try {
			outStream = new ObjectOutputStream(this.client.getOutputStream());
			inputStream = new ObjectInputStream(this.client.getInputStream());
			tempMessage = new Message();
			user = handshake(inputStream, outStream, this.allUSers);
			
			if(user == null) {
				// send the message to the user to let them know they are not signed in
				throw(new Exception());
			}
			// check for any pending messages
			if(user.getPendingMessageQueue().isEmpty()) {
				System.out.println(user.getUserName()+"'s pending message queue is empty");
			}
			else {
				//clear the queue
				while(!user.getPendingMessageQueue().isEmpty()) {
					sentMessage = user.getPendingMessageQueue().remove();
					sentMessage.sendMessage(outStream);
				}
			}

			while(user.getUserState() == User.USER_STATE_ONLINE) {
				if(user.getMessageQueue().isEmpty()) {
					// Do Nothing
				}
				else {
					sentMessage = user.getMessageQueue().remove();
					sentMessage.sendMessage(outStream); // if you have messages in your queue, send them out
					sentMessage.printMessage();
				}
				
				if(inputStream.available() > 0) {
					tempMessage.recieveMessage(inputStream);
					user.sendMessage(tempMessage);
					tempMessage.printMessage();
				}
			}
			
			this.client.close();
		
		}catch(IOException err) {
			//Interrogate the error 
			if("Broken pipe (Write failed)".equals(err.getLocalizedMessage())) {
				user.setUserState(User.USER_STATE_OFFLINE);
				user.appendMessagePending(sentMessage);
			}
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("There was a generic exception"+e.getLocalizedMessage());
		}
		
	}
	
	private User handshake(ObjectInputStream in, ObjectOutputStream out, ArrayList<User> allUsers)throws Exception {
		// sign in the user and retrieve their object from the list
		User temp = null;
		String name = null, email = null;
		int count = 0;
		while(count < 2) {
			if(in.available() > 0) {
				if(count == 0) {
					name = in.readUTF(); // get the name 
					
					count++;
				}
				else {
					email = in.readUTF(); // get the email address
					count++;
				}
			}
		}
		for(User user: allUsers) {
			//System.out.println("Checking");
			System.out.println(user.getUserName().equals(name));
			if(user.getUserName().equals(name)) {
					temp = user;
					out.writeUTF(user.getUserEmail());
					out.flush();
					user.setUserState(User.USER_STATE_ONLINE);
					return user;
				}
				else if(user.getUserEmail().equals(email)) {
					temp = user;
					out.writeUTF(user.getUserEmail());
					out.flush();
					user.setUserState(User.USER_STATE_ONLINE);
					System.out.println("User found email");
					return user;
				}
			}
		
		temp = new User(name, email, this.allUSers); // create new user and append to the list if doesn't exist
		temp.setUserState(User.USER_STATE_ONLINE);
		this.allUSers.add(temp);
		System.out.println("Users: "+this.allUSers);
		out.writeUTF(this.allUSers.get(this.allUSers.size()-1).getUserEmail());
		out.flush();
		
		return this.allUSers.get(this.allUSers.size()-1);	
	}
	public User getUser() {return this.user;}
}
