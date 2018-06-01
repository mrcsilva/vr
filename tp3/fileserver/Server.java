import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.*;
import java.lang.management.*;

public class Server{
private DatagramSocket socket = null;
private FileEvent fileEvent = null;
final File folder = new File("/home/mario/Desktop");

public Server() {

}

public String listFilesForFolder(final File folder) {

    String reply = "";
    System.out.println(folder.getAbsolutePath());
    for (final File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
            //listFilesForFolder(fileEntry);
        } else {
            System.out.println(fileEntry.getName());
            reply = reply + fileEntry.getName() + " ";
        }
    }
    return reply;
}

public void createAndListenSocket() {
    try {
        socket = new DatagramSocket(9876);
        byte[] incomingData = new byte[1024 * 1000 * 50];

        while (true) {

            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            socket.receive(incomingPacket);
            byte[] data = incomingPacket.getData();
            String data2 = new String(incomingPacket.getData(), incomingPacket.getOffset(), incomingPacket.getLength());

            String[] splited = data2.split("\\s+");

            System.out.println("Received packet from: " + incomingPacket.getAddress().getHostAddress());
            if(splited[0].equals("Download")){
                if (splited.length == 2) {
                    DownloadThread dt = new DownloadThread(splited[1], incomingPacket.getAddress());
                    dt.start();
                }
            }

            else{

            if(!splited[0].equals("Estado") && !splited[0].equals("List")){

                ByteArrayInputStream in = new ByteArrayInputStream(data);
                ObjectInputStream is = new ObjectInputStream(in);
                fileEvent = (FileEvent) is.readObject();
                if (fileEvent.getStatus().equalsIgnoreCase("Error")) {
                    System.out.println("Some issue happened while packing the data @ client side\n");
                    //System.exit(0);
                }else  createAndWriteFile(); // writing the file to hard disk
            }
        }
            InetAddress IPAddress = incomingPacket.getAddress();
            int port = incomingPacket.getPort();
            String reply;

            if(splited[0].equals("Estado")){
                OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
                reply = "Estado " + os.getSystemLoadAverage();
            }
            else if(splited[0].equals("List")){
                reply = "List " + listFilesForFolder(folder);
                System.out.println(reply);
            }
            else reply = "Thank you for the message";

            byte[] replyBytea = reply.getBytes();
            DatagramPacket replyPacket = new DatagramPacket(replyBytea, replyBytea.length, IPAddress, port);
            socket.send(replyPacket);
            System.out.println("Reply: " +reply +"\n");
            Thread.sleep(500);

}

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
System.out.println("Output file : " + outputFile + " is successfully saved\n");

} catch (FileNotFoundException e) {
e.printStackTrace();
} catch (IOException e) {
e.printStackTrace();
}

}





    public static void main(String[] args) {

        Server server = new Server();
        server.createAndListenSocket();

       }
}




    class DownloadThread extends Thread {

    private DatagramSocket socket = null;
    private FileEvent event = null;
    private String sourceFilePath = "/home/mario/Desktop/";
    private String destinationPath = "/home/mario/Downloads/";
    private InetAddress hostName;

    public DownloadThread(String file, InetAddress ip) {
        this.sourceFilePath = sourceFilePath + file;
        this.hostName = ip;
  }

  public void createConnection() {
        try {

        socket = new DatagramSocket();
        InetAddress IPAddress = hostName;
        byte[] incomingData = new byte[1024];
        event = getFileEvent();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(outputStream);
        os.writeObject(event);
        System.out.println("Sending data to: " + IPAddress.getHostAddress());
        byte[] data = outputStream.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 9876);
        socket.send(sendPacket);
        System.out.println("File sent from client");
        Thread.sleep(2000);
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

    @Override
    public void run() {
        try {

                  createConnection();

        }
        catch (Exception io) {
            io.printStackTrace();
        }

    }
}
