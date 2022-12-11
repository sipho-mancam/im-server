package chat_app_prot;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import chat_app_prot.users.User;

public class Server {
	
	public static void main(String[] args) throws IOException {
		ArrayList<ClientsHandle> clients = new ArrayList<ClientsHandle>();
		ArrayList<User> allUsers = new ArrayList<User>();
		ServerSocket server;
		int port = 9090;
		
		server = new ServerSocket(port);
		System.out.println("Listening on Port: "+ port);
		
		try {
			
			while(true) {
				ClientsHandle cl;			
				cl = new ClientsHandle(server.accept(), allUsers);
				System.out.println("Connected.");
				clients.add(cl);
				cl.start();
			}	
		}catch(IOException e) {
			e.printStackTrace();
			server.close();
		}
	}
}
