package main;

import Component.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import connection.DatabaseConnection;
import java.security.NoSuchAlgorithmException;
import model.*;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.TimingTargetAdapter;
import service.Authentication;
import service.ServiceUser;
import java.sql.*;
import java.util.*;

public class login extends javax.swing.JFrame {
    
    private MigLayout layout;
    private PanelCover cover;
    private PanelLoading loading;
    private PanelVerifyCode verifyCode;
    private PanelLoginandRegister loginandRegister;
    private boolean isLogin;
    private final double addSize = 30;
    private final double coverSize = 40;
    private final double loginSize = 60;
    private final DecimalFormat df = new DecimalFormat("##0.###",DecimalFormatSymbols.getInstance(Locale.US));
    private ServiceUser service;
    
    public login() {
        initComponents();
        init();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bg = new javax.swing.JLayeredPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        bg.setBackground(new java.awt.Color(255, 255, 255));
        bg.setOpaque(true);

        javax.swing.GroupLayout bgLayout = new javax.swing.GroupLayout(bg);
        bg.setLayout(bgLayout);
        bgLayout.setHorizontalGroup(
            bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 914, Short.MAX_VALUE)
        );
        bgLayout.setVerticalGroup(
            bgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 593, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(bg)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(bg)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents
    
    
    private void init(){
        service = new ServiceUser();
        layout = new MigLayout("fill,insets 0");
        cover = new PanelCover();
        loading = new PanelLoading();
        verifyCode = new PanelVerifyCode();
        ActionListener eventRegister = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                register();
            }
        };
        
        ActionListener eventLogin = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        };
        
        loginandRegister = new PanelLoginandRegister(eventRegister,eventLogin);
        TimingTarget target = new TimingTargetAdapter(){
            @Override
            public void timingEvent(float fraction) {
                double fractionCover;
                double fractionlogin;
                double size = coverSize;
                if (fraction<=0.5f) {
                    size += fraction * size;
                } else {
                    size += addSize - fraction * addSize;
                }
                if(isLogin){
                    fractionCover = 1f - fraction;
                    fractionlogin = fraction;
                    if (fraction>=0.5f) {
                        cover.registerRight(fractionCover * 100);
                    } else {
                        cover.loginRight(fractionlogin * 100);
                    }
                }
                else{
                    fractionCover = fraction;
                    fractionlogin = 1f - fraction;
                    if (fraction<=0.5f) {
                        cover.registerLeft(fraction * 100);
                    } else {
                        cover.loginLeft((1f-fraction) * 100);
                    }
                }
                if (fraction>=0.5f) {
                    loginandRegister.showRegister(isLogin);
                }
                
                fractionCover = Double.parseDouble(df.format(fractionCover));
                fractionlogin = Double.parseDouble(df.format(fractionlogin));
                layout.setComponentConstraints(cover,"width " + size + "%, pos " + fractionCover +"al 0 n 100% " );
                layout.setComponentConstraints(loginandRegister,"width " + loginSize + "%, pos " + fractionlogin +"al 0 n 100% " );
                bg.revalidate();
            }

            @Override
            public void end() {
                isLogin = !isLogin;
            }
        };
        Animator animator = new Animator(800,target);
        animator.setAcceleration(0.5f);
        animator.setDeceleration(0.5f);
        animator.setResolution(0);
        
        //Layout of UI
        bg.setLayout(layout);
        bg.setLayer(loading,JLayeredPane.POPUP_LAYER);
        bg.setLayer(verifyCode,JLayeredPane.POPUP_LAYER);
        bg.add(loading, "pos 0 0 100% 100%");
        bg.add(verifyCode, "pos 0 0 100% 100%");
        bg.add(cover,"width "+ coverSize + "%, pos 0al 0 n 100%");
        bg.add(loginandRegister,"width "+ loginSize + "%, pos 1al 0 n 100%");
        
        cover.addEvent(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
                if (!animator.isRunning()) {
                    animator.start();
                }
            }
        });
        
        verifyCode.addEventButtonVerify(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ae){
                try {
                    ModelUser user = loginandRegister.getUser();
                    if (service.verifyCodeWithUser(user.getUserID(),verifyCode.getInputCode())) {
                        service.doneVerify(user.getUserID());
                        showMessage(Message.MessageType.SUCCESS, "Register success");
                        verifyCode.setVisible(false);
                        loginandRegister.setBlankText();
                    } else {
                        showMessage(Message.MessageType.ERROR, "Verify code is incorrect");
                    }
                } catch (SQLException e) {
                    showMessage(Message.MessageType.ERROR, "Error");
                }
            }
        });
        
        verifyCode.addEventButtonCancel(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ModelUser user = loginandRegister.getUser();
                try {
                    service.deleteuser(user);
                } catch (SQLException ex) {
                    showMessage(Message.MessageType.ERROR, "Can't delete user");
                }
            }
        }
        );
        
    }
    
    private void sendMain(ModelUser user){
        new Thread(new Runnable() {
            @Override
            public void run() {
                loading.setVisible(true);
                ModelMessage ms = new Authentication().sendMain(user.getEmail(), user.getVerifyCode());
                if (ms.isSuccess()) {
                    loading.setVisible(false);
                    verifyCode.setVisible(true);
                }else{
                    loading.setVisible(false);
                    showMessage(Message.MessageType.ERROR, ms.getMessage());
                }
            }
        }).start();
    }
    
    private void register(){
        ModelUser user = loginandRegister.getUser();
        try {
            if (user.getUserName().isBlank() || user.getEmail().isBlank() || user.getPassword().isBlank()) {
                showMessage(Message.MessageType.ERROR, "Field is empty");
            }else if (service.checkDuplicateUser(user.getUserName())){
                showMessage(Message.MessageType.ERROR,"User Name already exists");
            }else if (service.checkDuplicateEmail(user.getEmail())) {
                showMessage(Message.MessageType.ERROR, "Email is already exists");
            }else{
                service.insertUser(user);
                sendMain(user);
            }
        } catch (SQLException e) {
            showMessage(Message.MessageType.ERROR, "Error Register");
        }catch(NoSuchAlgorithmException es){
            showMessage(Message.MessageType.ERROR, "Error");
        }
    }
    
    private void login(){
        ModelLogin data = loginandRegister.getDataLogin();
        try {
            ModelUser user = service.login(data);
            if (user != null) {
                this.dispose();
                Userdashboard.main(user);
            } else {
                showMessage(Message.MessageType.ERROR, "Credentials are invailed");
            }
        } catch (SQLException e) {
            showMessage(Message.MessageType.ERROR, "Login Error");
        }
    }
    
    
    //show popup message
    private void showMessage(Message.MessageType messageType, String message){
        Message ms =new Message();
        ms.showMessage(messageType, message);
        TimingTarget target = new TimingTargetAdapter(){
            @Override
            public void begin(){
                if (!ms.isShow()) {
                    bg.add(ms,"pos 0.5al -30", 0);
                    ms.setVisible(true);
                    bg.repaint();
                }
            }

            @Override
            public void timingEvent(float fraction) {
               float f;
                if (ms.isShow()) {
                    f = 40 * (1f - fraction);
                } else {
                    f = 40 * fraction;
                }
                layout.setComponentConstraints(ms, "pos 0.5al " + (int) (f-30));
                bg.repaint();
                bg.revalidate();
            }

            @Override
            public void end() {
                if(ms.isShow()){
                    bg.remove(ms);
                    bg.repaint();
                    bg.revalidate();
                }else{
                    ms.setShow(true);
                }
            }
        };
        Animator animator = new Animator(300, target);
        animator.setResolution(0);
        animator.setAcceleration(0.5f);
        animator.setDeceleration(0.5f);
        animator.start();
        new Thread(new Runnable() {
            @Override
            public void run() { 
                try {
                    Thread.sleep(1200);
                    animator.start();
                } catch (InterruptedException e) {
                    System.err.println(e);
                }
            }
        }).start();
    }
    
    public static void main(String args[]) {
        try {
            DatabaseConnection.getInstance().connectToDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new login().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLayeredPane bg;
    // End of variables declaration//GEN-END:variables
}
