import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.*;
import java.util.Scanner;



public class Client {

	private DatagramSocket socket = null;
	private DatagramSocket bindSocket = null;
	private FileEvent event = null;
	private String sourceFilePath = "/home/mario/Desktop/";
	private String destinationPath = "/home/mario/Downloads/";
	private String hostName = "10.0.0.251";
	private String type;
	private String file;
	private DatagramPacket packet;

	public Client(String type,DatagramSocket bindSocket,String file) {

		this.type = type;
		this.bindSocket = bindSocket;
		this.file = file;
		this.sourceFilePath = sourceFilePath + file;

	}

	public void createConnection() {
		try {
		System.out.println("TODOS");
		socket = new DatagramSocket();
		InetAddress IPAddress = InetAddress.getByName(hostName);
        InetAddress fs1 = InetAddress.getByName("10.0.0.22");
        InetAddress fs2 = InetAddress.getByName("10.0.0.23");
		byte[] incomingData = new byte[1024];
		byte[] buf = type.getBytes();

		if(type.equals("Upload")){
			event = getFileEvent();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(outputStream);
			os.writeObject(event);
			byte[] data = outputStream.toByteArray();
			packet = new DatagramPacket(data, data.length, IPAddress, 9876);
		}
		else{


        packet = new DatagramPacket(buf, buf.length, fs1, 9876);

		if(!type.equals("Estado") && !type.equals("List")){
			packet = new DatagramPacket(buf, buf.length, IPAddress, 9876);
       	 	CDownloadThread ct = new CDownloadThread(bindSocket);
       		ct.start();
       	}
		else if(type.equals("List")) {
			packet = new DatagramPacket(buf, buf.length, IPAddress, 9876);
		}
       }


		socket.send(packet);
		System.out.println(type);
		System.out.println("File sent from client");

		DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
		socket.receive(incomingPacket);
		String response = new String(incomingPacket.getData());

		String[] splited = response.split("\\s+");

		if(splited[0].equals("List")){
			System.out.println("-----Lista de Ficheiros-----");
			for(int i = 1; i<splited.length;i++){
					System.out.println(splited[i]);
				}
		}
		else {
			if(type.equals("Estado")) {
				System.out.println("Response from server:\n" + response);
				packet = new DatagramPacket(buf, buf.length, fs2, 9876);
		 		socket.send(packet);
				socket.receive(incomingPacket);
				response = new String(incomingPacket.getData());

				System.out.println("Server 2: " + response);
			}
			else {
				System.out.println("Response from server:" + response);
			}
		 }
		Thread.sleep(100);

		//System.exit(0);

} catch (UnknownHostException e) {
e.printStackTrace();
} catch (SocketException e) {
e.printStackTrace();
} catch (IOException e) {
e.printStackTrace();
} catch (InterruptedException e) {
e.printStackTrace();
}
}


public FileEvent getFileEvent() {
	FileEvent fileEvent = new FileEvent();
	String fileName = sourceFilePath.substring(sourceFilePath.lastIndexOf("/") + 1, sourceFilePath.length());
	String path = sourceFilePath.substring(0, sourceFilePath.lastIndexOf("/") + 1);
	fileEvent.setDestinationDirectory(destinationPath);
	fileEvent.setFilename(fileName);
	fileEvent.setSourceDirectory(sourceFilePath);
	File file = new File(sourceFilePath);
	if (file.isFile()) {
		try {
			DataInputStream diStream = new DataInputStream(new FileInputStream(file));
			long len = (int) file.length();
			byte[] fileBytes = new byte[(int) len];
			int read = 0;
			int numRead = 0;
			while (read < fileBytes.length && (numRead = diStream.read(fileBytes, read, fileBytes.length - read)) >= 0) {
			read = read + numRead;
		}

		fileEvent.setFileSize(len);
		fileEvent.setFileData(fileBytes);
		fileEvent.setStatus("Success");

} catch (Exception e) {
e.printStackTrace();
fileEvent.setStatus("Error");
}
} else {
System.out.println("path specified is not pointing to a file");
fileEvent.setStatus("Error");
}
return fileEvent;
}



public static void main(String[] args) {

	DatagramSocket bindsocket= null;
	String st = "";
	try{

	bindsocket = new DatagramSocket(9876);

	} catch (SocketException e) {
		e.printStackTrace();
	}

	Scanner s = new Scanner(System.in);
	int op = -1;
	 do {
            System.out.println("------OPCOES------");
            System.out.println("1 - List Files");
            System.out.println("2 - Download File");
            System.out.println("3 - Upload File");
            System.out.println("4 - Estado do Server");
            System.out.println("0 - Sair");
            op = s.nextInt();
            switch(op) {
            	case 1:Client client0 = new Client("List",bindsocket,"");
            		   client0.createConnection();
            		   break;
                case 2:System.out.println("Name File");
                	   s.nextLine();
                	   st = s.nextLine();
                	   Client client = new Client("Download "+st,bindsocket,"");
					   client.createConnection();
					   break;
                case 3:System.out.println("Name File");
               		   s.nextLine();
                	   st = s.nextLine();
                	   Client client2 = new Client("Upload",bindsocket,st);
					   client2.createConnection();
                       break;
                case 4:Client client3 = new Client("Estado",bindsocket,"");
                	   client3.createConnection();
                case 0:break;
            }
        }while (op != 0);
        System.exit(0);
	}
}


class CDownloadThread extends Thread {

	private DatagramSocket socket = null;
	private FileEvent fileEvent = null;


    public CDownloadThread(DatagramSocket bindSocket) {
    	this.socket = bindSocket;

  	}

  public void createAndListenSocket() {
	try {

			byte[] incomingData = new byte[1024 * 1000 * 50];
			DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
			socket.receive(incomingPacket);

			byte[] data = incomingPacket.getData();

			String data2 = new String(incomingPacket.getData(), incomingPacket.getOffset(), incomingPacket.getLength());
			String[] splited = data2.split("\\s+");

			ByteArrayInputStream in = new ByteArrayInputStream(data);
			ObjectInputStream is = new ObjectInputStream(in);
			fileEvent = (FileEvent) is.readObject();
			if (fileEvent.getStatus().equalsIgnoreCase("Error")) {
				System.out.println("Some issue happened while packing the data @ client side");
				System.exit(0);
			}
			createAndWriteFile(); // writing the file to hard disk


			Thread.sleep(2000);
			//System.exit(0);

} catch (SocketException e) {
e.printStackTrace();
} catch (IOException e) {
e.printStackTrace();
} catch (ClassNotFoundException e) {
e.printStackTrace();
} catch (InterruptedException e) {
e.printStackTrace();
}
}

public void createAndWriteFile() {
String outputFile = fileEvent.getDestinationDirectory() + fileEvent.getFilename();
if (!new File(fileEvent.getDestinationDirectory()).exists()) {
new File(fileEvent.getDestinationDirectory()).mkdirs();
}
File dstFile = new File(outputFile);
FileOutputStream fileOutputStream = null;
try {
fileOutputStream = new FileOutputStream(dstFile);
fileOutputStream.write(fileEvent.getFileData());
fileOutputStream.flush();
fileOutputStream.close();
System.out.println("Output file : " + outputFile + " is successfully saved ");

} catch (FileNotFoundException e) {
e.printStackTrace();
} catch (IOException e) {
e.printStackTrace();
}
}

    @Override
    public void run() {
        try {

              	createAndListenSocket();

        }
        catch (Exception io) {
            io.printStackTrace();
        }

    }

}
