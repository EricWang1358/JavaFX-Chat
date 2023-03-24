package com.client.chatwindow;

import com.client.login.LoginController;
import com.messages.Message;
import com.messages.MessageType;
import com.messages.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

import static com.messages.MessageType.CONNECTED;

public class Listener implements Runnable{

    private static final String HASCONNECTED = "has connected";

    private static String picture;
    private Socket socket;
    public String hostname;
    public int port;
    public static String username;
    public ChatController controller;
    private static ObjectOutputStream oos;
    private InputStream is;
    private ObjectInputStream input;
    private OutputStream outputStream;
    Logger logger = LoggerFactory.getLogger(Listener.class);

    public Listener(String hostname, int port, String username, String picture, ChatController controller) { // 定义 Listener 构造函数
        this.hostname = hostname; // 给成员变量 hostname 赋值
        this.port = port; // 给成员变量 port 赋值
        Listener.username = username; // 给静态成员变量 username 赋值
        Listener.picture = picture; // 给静态成员变量 picture 赋值
        this.controller = controller; // 给成员变量 controller 赋值
    } // Listener 构造函数结束
    public void run() {
        try {
            // 与服务器建立socket连接
            socket = new Socket(hostname, port);
            // 在登录窗口上显示场景
            LoginController.getInstance().showScene();
            // 获取输出流并创建ObjectOutputStream实例
            outputStream = socket.getOutputStream();
            oos = new ObjectOutputStream(outputStream);
            // 获取输入流并创建ObjectInputStream实例
            is = socket.getInputStream();
            input = new ObjectInputStream(is);
        } catch (IOException e) {
            // 在登录窗口上显示连接错误对话框
            LoginController.getInstance().showErrorDialog("Could not connect to server");
            // 在日志中记录连接错误
            logger.error("Could not Connect");
        }
        // 在日志中记录连接已经被接受
        logger.info("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());

        try {
            // 连接已建立，执行连接操作
            connect();
            // 在日志中记录输入输出流准备就绪
            logger.info("Sockets in and out ready!");
            // 循环读取输入流的消息，只要socket保持连接状态
            while (socket.isConnected()) {
                // 读取输入流中的Message对象
                Message message = null;
                message = (Message) input.readObject();

                if (message != null) {
                    // 在日志中记录收到的消息
                    logger.debug("Message recieved:" + message.getMsg() + " MessageType:" + message.getType() + "Name:" + message.getName());
                    switch (message.getType()) {
                        // 如果收到的消息类型是USER或VOICE，则在聊天界面上显示该消息
                        case USER:
                            controller.addToChat(message);
                            break;
                        case VOICE:
                            controller.addToChat(message);
                            break;
                        // 如果收到的消息类型是NOTIFICATION，则在聊天界面上显示新用户通知
                        case NOTIFICATION:
                            controller.newUserNotification(message);
                            break;
                        // 如果收到的消息类型是SERVER，则在聊天界面上显示该消息
                        case SERVER:
                            controller.addAsServer(message);
                            break;
                        // 如果收到的消息类型是CONNECTED、DISCONNECTED或STATUS，则在用户列表中更新用户状态
                        case CONNECTED:
                            controller.setUserList(message);
                            break;
                        case DISCONNECTED:
                            controller.setUserList(message);
                            break;
                        case STATUS:
                            controller.setUserList(message);
                            break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            // 在控制台上打印异常信息
            e.printStackTrace();
            // 在聊天界面上显示登出场景
            controller.logoutScene();
        }
    }


    /* This method is used for sending a normal Message
     * @param msg - The message which the user generates
     */
    public static void send(String msg) throws IOException {
        Message createMessage = new Message(); // 创建一个新的Message对象
        createMessage.setName(username); // 设置Message的用户名
        createMessage.setType(MessageType.USER); // 设置Message的类型为USER
        createMessage.setStatus(Status.AWAY); // 设置Message的状态为AWAY
        createMessage.setMsg(msg); // 设置Message的文本消息
        createMessage.setPicture(picture); // 设置Message的用户头像图片
        oos.writeObject(createMessage); // 将Message写入对象输出流
        oos.flush(); // 刷新输出流
    }

    /* This method is used for sending a voice Message
 * @param msg - The message which the user generates
 */
    public static void sendVoiceMessage(byte[] audio) throws IOException {
        Message createMessage = new Message(); // 创建一个新的Message对象
        createMessage.setName(username); // 设置Message的用户名
        createMessage.setType(MessageType.VOICE); // 设置Message的类型为VOICE
        createMessage.setStatus(Status.AWAY); // 设置Message的状态为AWAY
        createMessage.setVoiceMsg(audio); // 设置Message的语音消息
        createMessage.setPicture(picture); // 设置Message的用户头像图片
        oos.writeObject(createMessage); // 将Message写入对象输出流
        oos.flush(); // 刷新输出流
    }

    /* This method is used for sending a status update Message
     * @param status - The status which the user updates
     */
    public static void sendStatusUpdate(Status status) throws IOException {
        Message createMessage = new Message(); // 创建一个新的Message对象
        createMessage.setName(username); // 设置Message的用户名
        createMessage.setType(MessageType.STATUS); // 设置Message的类型为STATUS
        createMessage.setStatus(status); // 设置Message的状态
        createMessage.setPicture(picture); // 设置Message的用户头像图片
        oos.writeObject(createMessage); // 将Message写入对象输出流
        oos.flush(); // 刷新输出流
    }
    /* This method is used to send a connecting message */
    public static void connect() throws IOException {
        Message createMessage = new Message(); // 创建一个新的Message对象
        createMessage.setName(username); // 设置Message的用户名
        createMessage.setType(CONNECTED); // 设置Message的类型为CONNECTED
        createMessage.setMsg(HASCONNECTED); // 设置Message的文本消息
        createMessage.setPicture(picture); // 设置Message的用户头像图片
        oos.writeObject(createMessage); // 将Message写入对象输出流
    }

}
