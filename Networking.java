// A networking library to simplify server, client, and socket operations in general

// Programmed with love by Nim <3
//  https://www.nimsworld.ml
//  darealnim@gmail.com


import java.net.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


class Server {
  boolean initialized = false;
  boolean started = false;
  ServerSocket socket;
  int port;
  List<Client> clients = new ArrayList<Client>();
  int ccount = 0;
  private int maxclients;
  boolean mustDie = false;


  protected class NetworkingAcceptThread extends Thread {
    public void run() {
      System.out.println("NETWORKING STATUS: NetworkingAcceptThread initialized");
      while (!Server.this.mustDie) {
        if (Server.this.clients.size() < Server.this.maxclients) {
          try {
            Socket newclientsock = Server.this.socket.accept();
            Client newclient = new Client();
            newclient.socket = newclientsock;
            newclient.id = Server.this.ccount+1;
            newclient.alive = true;
            newclient.ip = newclientsock.getInetAddress().getHostAddress();
            Server.this.clients.add(newclient);
            Server.this.ccount += 1;
            System.out.println("NETWORKING UPDATE: New client " +  newclient.ip);
          } catch(Exception e) {
            System.out.println(e.getStackTrace());
            System.out.println(e.toString());
          }
        }
      }
    }
  }





  class Client {
    Socket socket = null;
    boolean alive = false;
    int id = -1;
    String ip = "none";
  }


  public int getClientCount() {
    return this.clients.size();
  }

  public int getClientIndexById(int id) {
    for (int i=0;i<this.clients.size();i++) {
      if (this.clients.get(i).id == id) {
        return i;
      }
    }
    return -1;
  }

  public int waitForClient() {
    int currentlastclientid = this.ccount;
    while(true) {
      try {
        TimeUnit.MILLISECONDS.sleep(300);
      } catch(Exception e) {
        System.out.println(e.toString());
      }
      if(this.ccount > currentlastclientid) {
        return this.ccount;
      }
    }
  }

  public int init(int p, int maxclients) {
    //returns 0 on non error
    if (!this.initialized){
      try{
        Server.this.port = p;
        Server.this.socket = new ServerSocket(Server.this.port);
        Server.this.initialized = true;
        Server.this.maxclients = maxclients;
        return 0;
      } catch (Exception e) {
        System.out.println("NETWORKING ERROR: " + e.toString());
        return -2;
      }
    } else {
      System.out.println("NETWORKING ERROR: The server is already initialized. Use the kill() method to stop and clear it first.");
      return -1;
    }
  }

  public void start() {
    if (!this.started) {
      this.started = true;
      this.mustDie = false;
      NetworkingAcceptThread acceptThread = new NetworkingAcceptThread();
      acceptThread.start();
      return;
    } else {
      System.out.println("NETWORKING ERROR: The server is already started. Use the stop() method to stop it first.");
      return;
    }
  }

  public void stop() {
    this.mustDie = true;
    this.started = false;
    try{
      this.socket.close();
    } catch(Exception e) {
      System.out.println(e.getStackTrace() + "\n" + e.toString() + "\n\n");
    }
  }

  public void kill() {
    this.stop();
    for(int i=0; i<this.clients.size(); i++) {
      if(this.clients.get(i).alive) {
        try {
          this.clients.get(i).socket.close();
        } catch(Exception e) {
          System.out.println(e.getStackTrace() + "\n" + e.toString() + "\n\n");
        }
      }
    }
    this.initialized = false;
  }


  //Header: 2 bytes spelling B17E, then a byte indicating request type:
  //  0: sendstring request
  //  1: sendint request
  //  2: senddouble request
  //  3: sendraw request
  //  4: Connection ABORTED! ABANDON SHIP!

  // We then follow the header with the raw data

  private byte[] craftPacket(byte type, byte[] data) {
    byte[] header = new byte[]{(byte)0xB1,(byte)0x7E,type};
    byte[] packet = new byte[header.length + data.length];
    System.arraycopy(header, 0, packet, 0, header.length);
    System.arraycopy(data, 0, packet, header.length, data.length);
    return packet;
  }

  public int sendString(int id, String s){
    //Returns -1 if something went wrong, else returns 0
    Client client = this.clients.get(getClientIndexById(id));
    if (!client.alive) {
      System.out.println("NETWORKING ERROR: Trying to send data to a dead client");
      return -1;
    }

    try{
      client.socket.getOutputStream().write(craftPacket((byte)0, s.getBytes()));
    }catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return -1;
    }
    return 0;
  }

  public int sendInt(int id, int i){
    //Returns -1 if something went wrong, else returns 0
    Client client = this.clients.get(getClientIndexById(id));
    if (!client.alive) {
      System.out.println("NETWORKING ERROR: Trying to send data to a dead client");
      return -1;
    }
    try {
      ByteBuffer b = ByteBuffer.allocate(4);
      b.putInt(i);
      byte[] result = b.array();
      client.socket.getOutputStream().write(craftPacket((byte)1, result));
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return -1;
    }
    return 0;
  }

  public int sendDouble(int id, double i) {
    //Returns -1 if something went wrong, else returns 0
    Client client = this.clients.get(getClientIndexById(id));
    if (!client.alive) {
      System.out.println("NETWORKING ERROR: Trying to send data to a dead client");
      return -1;
    }
    try {
      ByteBuffer b = ByteBuffer.allocate(8);
      b.putDouble(i);
      byte[] result = b.array();
      client.socket.getOutputStream().write(craftPacket((byte)2, result));
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return -1;
    }
    return 0;
  }

  public int sendRaw(int id, byte[] raw) {
    //Returns -1 if something went wrong, else returns 0
    Client client = this.clients.get(getClientIndexById(id));
    if (!client.alive) {
      System.out.println("NETWORKING ERROR: Trying to send data to a dead client");
      return -1;
    }
    try {
      client.socket.getOutputStream().write(craftPacket((byte)3, raw));
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return -1;
    }
    return 0;
  }

  public void dropConn(int id) {
    Client client = this.clients.get(getClientIndexById(id));
    if (!client.alive) {
      System.out.println("NETWORKING ERROR: Trying to send data to a dead client");
      return;
    }
    try {
      client.socket.getOutputStream().write(craftPacket((byte)4, new byte[0]));
      client.alive = false;
      try {
        client.socket.close();
      } catch(Exception e) {
        System.out.println(e.getStackTrace() + "\n" + e.toString() + "\n\n");
      }
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return;
    }
  }

  //-----------------------------------------------

  //Same for receiving functions. We verify the header is actually here, if yes, we verify
  //the request type is correct. Then we interpret and return data.

  public String recvString(int id, int stringlength) {
    //returns empty string on error
    Client client = this.clients.get(getClientIndexById(id));

    byte[] buffer = new byte[stringlength+3];
    try {
      client.socket.getInputStream().read(buffer);
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return "";
    }

    if (buffer[0]==(byte)0xB1 && buffer[1]==(byte)0x7E) {
      if (buffer[2]==(byte)0) {
        byte[] rawdata = Arrays.copyOfRange(buffer, 3, buffer.length);
        String recvstr = new String(rawdata);
        return recvstr;
      } else {
        if (buffer[2]==(byte)4) {
          System.out.println("NETWORKING STATUS: Client "+client.ip+" dropped connection. Setting them to dead state.");
          client.alive = false;
          try {
            client.socket.close();
          } catch(Exception e) {
            System.out.println(e.getStackTrace() + "\n" + e.toString() + "\n\n");
          }
          return "";
        }
        System.out.println("NETWORKING ERROR: Invalid request type");
        return "";
      }
    } else {
      System.out.println("NETWORKING ERROR: Invalid header");
      return "";
    }
  }

  public int recvInt(int id) {
    //returns -2147483648 on error. else returns parsed received int
    Client client = this.clients.get(getClientIndexById(id));

    byte[] buffer = new byte[7];
    try {
      client.socket.getInputStream().read(buffer);
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return -2147483648;
    }

    // for(int i=0;i<buffer.length;i++){
    //   System.out.print(buffer[i]);
    // }
    // System.out.println("");

    if (buffer[0]==(byte)0xB1 && buffer[1]==(byte)0x7E) {
      if (buffer[2]==(byte)1) {
        byte[] rawdata = Arrays.copyOfRange(buffer, 3, 7);
        ByteBuffer wrapped = ByteBuffer.wrap(rawdata); // big-endian by default
        int num = wrapped.getInt();
        return num;
      } else {
        if (buffer[2]==(byte)4) {
          System.out.println("NETWORKING STATUS: Client "+client.ip+" dropped connection. Setting them to dead state.");
          client.alive = false;
          try{
            client.socket.close();
          } catch(Exception e) {
            System.out.println(e.getStackTrace() + "\n" + e.toString() + "\n\n");
          }
          return -2147483648;
        }
        System.out.println("NETWORKING ERROR: Invalid request type");
        return -2147483648;
      }
    } else {
      System.out.println("NETWORKING ERROR: Invalid header. Expected 1, got "+buffer[2]);
      return -2147483648;
    }
  }

  public double recvDouble(int id) {
    //returns -2147483648 on error. else returns parsed received int
    Client client = this.clients.get(getClientIndexById(id));
    byte[] buffer = new byte[11];
    try {
      client.socket.getInputStream().read(buffer);
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return -2147483648.0;
    }
    if (buffer[0]==(byte)0xB1 && buffer[1]==(byte)0x7E) {
      if (buffer[2]==(byte)2) {
        byte[] rawdata = Arrays.copyOfRange(buffer, 3, 11);
        ByteBuffer wrapped = ByteBuffer.wrap(rawdata); // big-endian by default
        double num = wrapped.getDouble();
        return num;
      } else {
        if (buffer[2]==(byte)4) {
          System.out.println("NETWORKING STATUS: Client "+client.ip+" dropped connection. Setting them to dead state.");
          client.alive = false;
          try {
            client.socket.close();
          } catch(Exception e) {
            System.out.println(e.getStackTrace() + "\n" + e.toString() + "\n\n");
          }
          return -2147483648.0;
        }
        System.out.println("NETWORKING ERROR: Invalid request type");
        return -2147483648.0;
      }
    } else {
      System.out.println("NETWORKING ERROR: Invalid header");
      return -2147483648.0;
    }
  }
}




// CLIENT CLASS --------------------------------------------------------------------------------------
// CLIENT CLASS --------------------------------------------------------------------------------------
// CLIENT CLASS --------------------------------------------------------------------------------------
// CLIENT CLASS --------------------------------------------------------------------------------------


class Client {
  Socket socket = new Socket();
  int timeout = 0;
  String RHOST;
  String RPORT;

  public int connect(String RHOST, int RPORT) {
    //returns 0 on success, -1 on error
    try {
      socket = new Socket(RHOST, RPORT);
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return -1;
    }
    return 0;
  }

  private byte[] craftPacket(byte type, byte[] data) {
    byte[] header = new byte[]{(byte)0xB1,(byte)0x7E,type};
    byte[] packet = new byte[header.length + data.length];
    System.arraycopy(header, 0, packet, 0, header.length);
    System.arraycopy(data, 0, packet, header.length, data.length);
    return packet;
  }


  public int sendString(String s) {
    //Returns -1 if something went wrong, else returns 0
    if (this.socket.isClosed()) {
      System.out.println("NETWORKING ERROR: Trying to send data over a dead socket");
      return -1;
    }

    try {
      this.socket.getOutputStream().write(craftPacket((byte)0, s.getBytes()));
      return 0;
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return -1;
    }
  }

  public int sendInt(int i) {
    //Returns -1 if something went wrong, else returns 0
    if (this.socket.isClosed()) {
      System.out.println("NETWORKING ERROR: Trying to send data over a dead socket");
      return -1;
    }
    try {
      ByteBuffer b = ByteBuffer.allocate(4);
      b.putInt(i);
      byte[] result = b.array();
      this.socket.getOutputStream().write(craftPacket((byte)1, result));
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return -1;
    }
    return 0;
  }

  public int sendDouble(double i) {
    //Returns -1 if something went wrong, else returns the number of bytes sent
    if (this.socket.isClosed()) {
      System.out.println("NETWORKING ERROR: Trying to send data over a dead socket");
      return -1;
    }
    try {
      ByteBuffer b = ByteBuffer.allocate(8);
      b.putDouble(i);
      byte[] result = b.array();
      this.socket.getOutputStream().write(craftPacket((byte)2, result));
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return -1;
    }
    return 0;
  }

  public int sendRaw(byte[] raw) {
    //Returns -1 if something went wrong, else returns the number of bytes sent
    if (this.socket.isClosed()) {
      System.out.println("NETWORKING ERROR: Trying to send data over a dead socket");
      return -1;
    }
    try {
      this.socket.getOutputStream().write(craftPacket((byte)3, raw));
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return -1;
    }
    return 0;
  }

  //-----------------------------------------------

  //Same for receiving functions. We verify the header is actually here, if yes, we verify
  //the request type is correct. Then we interpret and return data.

  public String recvString(int stringlength) {
    //returns empty string on error
    byte[] buffer = new byte[stringlength+3];
    try {
      this.socket.getInputStream().read(buffer);
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return "";
    }
    if (buffer[0]==(byte)0xB1 && buffer[1]==(byte)0x7E) {
      if (buffer[2]==(byte)0) {
        byte[] rawdata = Arrays.copyOfRange(buffer, 3, buffer.length);
        String recvstr = new String(rawdata);
        return recvstr;
      } else {
        if (buffer[2]==(byte)4) {
          System.out.println("NETWORKING STATUS: Server dropped connection. Setting them to dead state.");
          try {
            this.socket.close();
          } catch(Exception e) {
            System.out.println(e.getStackTrace() + "\n" + e.toString() + "\n\n");
          }
          return "";
        }
        System.out.println("NETWORKING ERROR: Invalid request type");
        return "";
      }
    } else {
      System.out.println("NETWORKING ERROR: Invalid header");
      return "";
    }
  }

  public int recvInt() {
    //returns -2147483648 on error. else returns parsed received int
    byte[] buffer = new byte[7];
    try {
      this.socket.getInputStream().read(buffer);
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return -2147483648;
    }
    if (buffer[0]==(byte)0xB1 && buffer[1]==(byte)0x7E) {
      if (buffer[2]==(byte)1) {
        byte[] rawdata = Arrays.copyOfRange(buffer, 3, 7);
        ByteBuffer wrapped = ByteBuffer.wrap(rawdata); // big-endian by default
        int num = wrapped.getInt();
        return num;
      } else {
        if (buffer[2]==(byte)4) {
          System.out.println("NETWORKING STATUS: Server dropped connection. Setting them to dead state.");
          try {
            this.socket.close();
          } catch(Exception e) {
            System.out.println(e.getStackTrace() + "\n" + e.toString() + "\n\n");
          }
          return -2147483648;
        }
        System.out.println("NETWORKING ERROR: Invalid request type");
        return -2147483648;
      }
    } else {
      System.out.println("NETWORKING ERROR: Invalid header");
      return -2147483648;
    }
  }

  public double recvDouble() {
    //returns -2147483648 on error. else returns parsed received int
    byte[] buffer = new byte[11];
    try {
      this.socket.getInputStream().read(buffer);
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return -2147483648.0;
    }
    if (buffer[0]==(byte)0xB1 && buffer[1]==(byte)0x7E) {
      if (buffer[2]==(byte)3) {
        byte[] rawdata = Arrays.copyOfRange(buffer, 3, 11);
        ByteBuffer wrapped = ByteBuffer.wrap(rawdata); // big-endian by default
        double num = wrapped.getDouble();
        return num;
      } else {
        if (buffer[2]==(byte)4) {
          System.out.println("NETWORKING STATUS: Server dropped connection. Setting them to dead state.");
          try {
            this.socket.close();
          } catch(Exception e) {
            System.out.println(e.getStackTrace() + "\n" + e.toString() + "\n\n");
          }
          return -2147483648.0;
        }
        System.out.println("NETWORKING ERROR: Invalid request type");
        return -2147483648.0;
      }
    } else {
      System.out.println("NETWORKING ERROR: Invalid header");
      return -2147483648.0;
    }
  }



  public void dropConn() {
    if (this.socket.isClosed()) {
      System.out.println("NETWORKING ERROR: The socket is already closed");
      return;
    }
    try {
      this.socket.getOutputStream().write(craftPacket((byte)4, new byte[0]));
      try {
        this.socket.close();
      } catch(Exception e) {
        System.out.println(e.getStackTrace() + "\n" + e.toString() + "\n\n");
      }
    } catch(Exception e) {
      System.out.println("NETWORKING ERROR: " + e.toString());
      return;
    }
  }


}
