package service;

import connection.DatabaseConnection;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Random;
import model.ModelLogin;
import model.ModelUser;
import static service.Encryption.hashData;


public class ServiceUser {

    private final Connection con;

    public ServiceUser() {
        con = DatabaseConnection.getInstance().getConnection();
    }

    private boolean checkDuplicateCode(String code) throws SQLException {
        boolean duplicate = false;
        PreparedStatement p = con.prepareStatement("select UserID from `user` where VerifyCode=? limit 1");
        p.setString(1, code);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            duplicate = true;
        }
        r.close();
        p.close();
        return duplicate;
    }

    private String generateVerifyCode() throws SQLException {
        DecimalFormat df = new DecimalFormat("000000");
        Random ran = new Random();
        String code = df.format(ran.nextInt(1000000));  //  Random from 0 to 999999
        while (checkDuplicateCode(code)) {
            code = df.format(ran.nextInt(1000000));
        }
        return code;
    }
    
    public boolean checkDuplicateUser(String user) throws SQLException {
        boolean duplicate = false;
        PreparedStatement p = con.prepareStatement("select UserID from `user` where UserName=? and `Status`='Verified' limit 1");
        p.setString(1, user);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            duplicate = true;
        }
        r.close();
        p.close();
        return duplicate;
    }

    public boolean checkDuplicateEmail(String user) throws SQLException {
        boolean duplicate = false;
        PreparedStatement p = con.prepareStatement("select UserID from `user` where Email=? and `Status`='Verified' limit 1");
        p.setString(1, user);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            duplicate = true;
        }
        r.close();
        p.close();
        return duplicate;
    }

    public void doneVerify(String userID) throws SQLException {
        PreparedStatement p = con.prepareStatement("update `user` set VerifyCode='', `Status`='Verified' where UserID=? limit 1");
        p.setString(1, userID);
        p.execute();
        p.close();
    }

    public boolean verifyCodeWithUser(String userID, String code) throws SQLException {
        boolean verify = false;
        PreparedStatement p = con.prepareStatement("select UserID from `user` where UserID=? and VerifyCode=? limit 1");
        p.setString(1, userID);
        p.setString(2, code);
        ResultSet r = p.executeQuery();
        if (r.next()) {
            verify = true;
        }
        r.close();
        p.close();
        return verify;
    }
    
    public void insertUser(ModelUser user) throws SQLException, NoSuchAlgorithmException {
        String sql = "INSERT INTO `user` (UserID,UserName, Email, `Password`, VerifyCode) VALUES (?,?, ?, ?, ?)";
        
        // ✅ Tell JDBC you want to get auto-generated keys (like userID)
        PreparedStatement pst = con.prepareStatement(sql);

        String code = generateVerifyCode();
        pst.setString(1, user.getUserID());
        pst.setString(2, user.getUserName());
        pst.setString(3, user.getEmail());
        pst.setString(4, hashData(user.getPassword()));
        pst.setString(5, code);

        // ✅ Run the insert
        pst.executeUpdate();

        // ✅ Get the generated keys (userID)
        
        // ✅ Set the generated verify code
        user.setVerifyCode(code);

   
        pst.close();
    }
    
    public ModelUser login(ModelLogin login)throws SQLException{
        ModelUser data = null;
        PreparedStatement pst = con.prepareStatement("select UserID,UserName,Email from user where BINARY(Email)=? and BINARY(Password)=? and Status = 'Verified' limit 1");
        pst.setString(1, login.getEmail());
        pst.setString(2, login.getPassword());
        ResultSet r = pst.executeQuery();
        if(r.next()){
            String userID = r.getString(1);
            String userName = r.getString(2);
            String email = r.getString(3);
            data = new ModelUser(userID,userName,email,"");
        }
        r.close();
        pst.close();
        return data;
    } 
    
    
    public void deleteuser(ModelUser user) throws SQLException{
        String Email = user.getEmail();
        String code = user.getVerifyCode();
            PreparedStatement pst = con.prepareStatement("DELETE FROM USER WHERE Email = ? and VerifyCode = ?");
            pst.setString(1, Email);
            pst.setString(2, code);
            pst.executeUpdate();
            pst.close();
    }

}
    
