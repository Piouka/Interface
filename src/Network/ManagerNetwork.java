package Network;

import java.net.*;
import java.util.*;

public class ManagerNetwork{

    private UDPSender udpSend;
    private User user;
    private List<User> userList;
    private Controller control;

    public ManagerNetwork(Controller c,User user){
        this.user=user;
        this.control=c;

        this.udpSend = new UDPSender(this.user.getLogin());
        new UDPListener(this);
        new Server(this);

        this.userList= new ArrayList<>();
        userList.add(user);
        printUserList();
    }



    //Reply to a broadcast
    public void sendUDPConnectionReply(InetAddress address){
        this.udpSend.sendReply(this.user.getLogin(),address);
    }

    public void sendUDPLoginChanged(){
        this.udpSend.sendLoginChanged(this.user.getLogin());
    }

    public void sendMessage(String userName, String msg){
        boolean stop = false;
        User destUser= new User(null, null);
        int i=0;
        while (i<this.userList.size() && !stop){
            destUser = this.userList.get(i);
            System.out.println(destUser.getLogin());
            if (destUser.getLogin().equals(userName)){
                System.out.println("found");
                stop=true;
            }
            i++;
        }
        if (!stop){System.out.println("User Not found in sendMessage");}
        else{
            System.out.println(destUser.getLogin());
            new Client(destUser, 3600, msg);
        }
    }

    public void messageReceived(InetAddress destAddr,String msg){
        boolean found=false;
        User destUser =null;
        System.out.println(destAddr);
        for (User u : this.userList){
            System.out.println("- "+u.getInetAddress());
            if (u.getInetAddress().equals(destAddr)){
                found=true;
                destUser=u;
            }
        }
        if (found){
            this.control.displayMessageReceived(destUser, msg);
        }else{
            System.out.println("Message received but no user exist at this addr");
        }
    }

    synchronized public void addUser(User user){
        boolean found=false;
        for (User currentUser : userList) {
            System.out.println(currentUser.getLogin());
            if (currentUser.getLogin().equals(user.getLogin())){
                System.out.println("found");
                this.udpSend.sendWrongLogin(user.getInetAddress());
                found=true;
            }
        }
        if (!found){
            this.userList.add(user);
            sendUDPConnectionReply(user.getInetAddress());
            System.out.println("New user on network " + user.getLogin());
            //System.out.println("New userList :");
            //printUserList();
        }
    }


    public void changeLoginUser(){
        this.control.changeUserLogin();
    }

    public void replaceUser(String login, InetAddress addr){
        for (User currentUser : userList) {
            if (currentUser.getInetAddress()==addr){
                currentUser.setLogin(login);
            }
        }
    }

    public void printUserList(){
        for (User currentUser : userList) {
            System.out.println(currentUser.getLogin() + " " + currentUser.getInetAddress());
        }
    }
    /*
    Get Methods
    */
    public List<User> getUserList(){
        return userList;
    }








}