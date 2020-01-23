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
        new UDPListener(this, this.user.getInetAddress());
        new Server(this);

        this.userList= new ArrayList<>();
    }



    //Reply to a broadcast
    public void sendUDPConnectionReply(InetAddress address){
        this.udpSend.sendReply(this.user.getLogin(),address);
    }

    public void sendUDPLoginChanged(){
        this.udpSend.sendLoginChanged(this.user.getLogin());
    }
    public void sendUDPFirst(){
        this.udpSend.sendFirstMessage(this.user.getLogin());
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

    synchronized public void addUser(User user, int mode){
        boolean found=false;
        if (user.getLogin().equals(this.user.getLogin())){
            this.udpSend.sendWrongLogin(user.getInetAddress());
            found=true;
        }
        else {
            for (User currentUser : userList) {
                if (currentUser.getLogin().equals(user.getLogin())) {
                    this.udpSend.sendWrongLogin(user.getInetAddress());
                    found = true;
                }
            }
        }
        if (!found){
            String id = this.control.findId(user.getLogin());
            user.setId(id);
            this.userList.add(user);
            this.control.addUser(user.getLogin());
            if (mode==1) {
                sendUDPConnectionReply(user.getInetAddress());
            }
        }
    }


    public void changeLoginUser(){
        this.control.changeUserLogin();
    }

    synchronized public void replaceUser(String login, InetAddress addr){
        String oldLogin=null;
        for (User currentUser : userList) {
            if (currentUser.getInetAddress().equals(addr)){
                oldLogin=currentUser.getLogin();
                currentUser.setLogin(login);
            }
        }
        this.control.changeLogin(oldLogin,login);
    }

    synchronized public void removeUser (InetAddress addr){
        String login=null;
        for (User currentUser : userList) {
            if (currentUser.getInetAddress().equals(addr)){
                login=currentUser.getLogin();
                this.userList.remove(currentUser);
            }
        }
        if (login!=null) {
            this.control.removeUser(login);
        }
    }

    public void close(){
        this.udpSend.sendDisconnection();
    }

    /*
    Get Methods
    */
    public List<User> getUserList(){
        return userList;
    }








}