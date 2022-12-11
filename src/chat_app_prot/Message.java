package chat_app_prot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Message {
	
	public static final byte BROADCAST = 0x00;
	public static final byte SINGLE_REC = 0x01;
	
	public static final byte TEXT = 0x10;
	public static final byte OTHERDATA = 0x20;
	public static final byte ENDPACKET = -0x01;
	
	private String[] recipient; 
	private String sender, messageTime;
	private byte messageType, eom;
	private Data messageBody;
	private int recipientLength = 0, bodyLength = 0;
	
	public Message(String sender, byte messageType, String[] recipient, Data messageBody) {
		this.sender = sender;
		this.recipient = recipient;
		this.messageType = messageType;
		this.messageBody = messageBody;
		this.bodyLength = messageBody.getBodyLength();
		this.recipientLength = this.recipient.length;
	}
	public Message() {}

	public String[] getRecipient() {return this.recipient;}
	public String getSender() {return this.sender;}
	public Data getBody() {return this.messageBody;}
	public int getMessageType() {return this.messageType;}
	public byte getEOM() {return this.eom;}
	public int getBodyLength() {return this.bodyLength;}
	public int getRecipientLength() {return this.recipientLength;}
	public String getMessageTime() {return this.messageTime;}
	public void setRecipient(String[] recipient) {this.recipient = recipient;}
	public void setSender(String sender) {this.sender = sender;}
	public void setMessageType(byte messageType) {this.messageType = messageType;}
	public void setBody(Data body) {this.messageBody = body;}
	public void setEOM(byte eom) {this.eom= eom;}
	public void setBodyLength(int bodyLength) {this.bodyLength = bodyLength;}
	public void setRecipientLength(int recipientLength) {this.recipientLength = recipientLength;}
	public void setMessageTime(String time) {this.messageTime = time;}
	
	public void sendMessage(ObjectOutputStream out)throws IOException {
		out.writeUTF(sender);
		out.flush();
		out.writeByte(this.messageType);
		out.flush();
		out.writeInt(recipient.length);
		out.flush();
		for(int j=0; j<recipient.length; j++) {
			out.writeUTF(this.recipient[j]);
			out.flush();
		}
		out.writeInt(this.messageBody.getBodyLength());
		out.flush();
		if(this.messageBody.getBodyLength() != -1) {
			if((this.messageType & 0xF0)  == Message.TEXT) {
				out.writeUTF(this.messageBody.getTextData());
				out.flush();
			}
			else if((this.messageType & 0x0F) == Message.OTHERDATA) {
				for(int i=0; i<this.messageBody.getBodyLength(); i++) {
					out.writeInt(this.messageBody.getData()[i]);
					out.flush();
				}
			}
		}
		out.writeByte(ENDPACKET);
		out.flush();
		
//		printMessage();
	}
	
	public void recieveMessage(ObjectInputStream input) throws Exception {
		final int SENDER = 0, RECIPIENT = 3, MESSAGETYPE = 1, RECIPIENTLENGTH = 2, BODYLENGTH = 4, BODY = 5, EOM = 6;
		int count = 0, len=0;
		while(true) {
			try {
				if(count < 7) {
					if(input.available() > 0) {
						switch(count) {
						case SENDER:
							this.sender = input.readUTF();
							count ++;
							break;
						case MESSAGETYPE:
							this.messageType  = input.readByte();
							count ++;
							break;
						case RECIPIENTLENGTH:
							len = input.readInt();
							this.recipient = new String[len];
							this.recipientLength = len;
							count ++;
							break;
						case RECIPIENT:
							for(int j=0; j<len; j++) {
								this.recipient[j] = input.readUTF();
								Thread.sleep(200);
							}
							count ++;
							break;
						case BODYLENGTH:
							len = input.readInt();
							this.bodyLength = len;
							count ++;
							break;
						case BODY:
							if(len == 0) {
								if((this.getMessageType() & 0xF0) == Message.TEXT) { // this means we are reading a string
									Data temp = new Data(input.readUTF());
									this.setBody(temp);
								}
							}
							else {
								Integer[] body = new Integer[len];
								Data temp = new Data();
								for(int j=0; j<len; j++) {
									Thread.sleep(200);
									body[j] = input.readInt();
								}
								temp.setData(body);
								this.setBody(temp);
							}
							count ++;
							break;
						case EOM:
							this.setEOM(input.readByte());
							count ++;
							break;
							
						}
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			
			if(this.getEOM() == -0x0F || count >= 7)break;
		}
		
	}
	
	public void printMessage() {
		System.out.println("Sender: "+this.sender);
		System.out.println("Recipient Length: "+this.recipient.length);
		System.out.println("Recipient: "+this.recipient[0]);
		System.out.println("Body Length: "+this.getBody().getBodyLength());
		System.out.println("Body: "+this.getBody().getTextData());
		System.out.println("Message Type: "+this.messageType);
		System.out.println("End of Message: "+this.eom);
	}
}
