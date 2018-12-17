import java.sql.*;
import java.util.Properties;
import java.lang.String ;
import java.util.Date ;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Collector  {


    public static void main(String[] args){

        Connection sysConn = null;
        Connection monitorConn = null;


        try{
            Class.forName("oracle.jdbc.OracleDriver");

            // PDB ORCL SYS
            sysConn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@//localhost:1521/orcl",
                    "sys as SYSDBA",
                    "oracle"
            );

            // PDB ORCL Monitor
            monitorConn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@//localhost:1521/orcl",
                    "Monitor",
                    "monitor"
            );


            if (sysConn != null) {
                System.out.println("$ sys.orcl: Connected.");
            }
            else{
                System.out.println("$ sys.orcl: Failed connection.");
            }

            if (monitorConn != null) {
                System.out.println("$ Monitor.orcl: Connected.");
            }
            else{
                System.out.println("$ Monitor.orcl: Failed connection.");
            }


            while(true){
                // TABLESPACES
                PreparedStatement psmt;
                String getTablespaces = "SELECT " +
                        "   ts.tablespace_name, \"File Count\"," +
                        "   TRUNC(\"SIZE(MB)\", 2) \"Size(MB)\"," +
                        "   TRUNC(fr.\"FREE(MB)\", 2) \"Free(MB)\"," +
                        "   TRUNC(\"SIZE(MB)\" - \"FREE(MB)\", 2) \"Used(MB)\"," +
                        "   df.\"MAX_EXT\" \"Max Ext(MB)\"," +
                        "   (fr.\"FREE(MB)\" / df.\"SIZE(MB)\") * 100 \"% Free\"" +
                        "   FROM " +
                        "   (SELECT tablespace_name," +
                        "   SUM (bytes) / (1024 * 1024) \"FREE(MB)\"" +
                        "   FROM dba_free_space" +
                        "   GROUP BY tablespace_name) fr," +
                        "   (SELECT tablespace_name, SUM(bytes) / (1024 * 1024) \"SIZE(MB)\", COUNT(*)" +
                        "   \"File Count\", SUM(maxbytes) / (1024 * 1024) \"MAX_EXT\"" +
                        "   FROM dba_data_files" +
                        "   GROUP BY tablespace_name) df," +
                        "   (SELECT tablespace_name" +
                        "   FROM dba_tablespaces) ts" +
                        "   WHERE fr.tablespace_name = df.tablespace_name (+)" +
                        "   AND fr.tablespace_name = ts.tablespace_name (+)" +
                        "   ORDER BY \"% Free\" desc;";
                Statement getStmt = sysConn.createStatement();
                ResultSet resultSet = getStmt.executeQuery(getTablespaces);

                while(resultSet.next()) {

                    Statement monitorStmt = monitorConn.createStatement();
                    String updateQuery = "UPDATE \"MONITOR\".\"TABLESPACES\" " +
                            " SET filecount = " + Float.parseFloat(resultSet.getString("File Count")) +
                            "," + " size = " + resultSet.getString("Size(MB)") +
                            "," + " free = " + resultSet.getString("Free(MB)") +
                            "," + " used = " + resultSet.getString("Used(MB)") +
                            "," + " maxextend = " + resultSet.getString("Max Ext(MB)") +
                            "," + " percfree = " + resultSet.getString("% Free") +
                            "," + " timestamp = CURRENT_TIMESTAMP" +
                            " WHERE name = " + "'" + resultSet.getString("TABLESPACE_NAME") + "'";

                    int i=0;
                    // devolve o número queries afetadas
                    i = monitorStmt.executeUpdate(updateQuery);

                    // se não havia update a fazer (pq não constava inicialmente)
                    if(i==0) {

                        String insertQuery = "INSERT INTO \"MONITOR.TABLESPACES\" " +
                                "(name,filecount,size,free,used,maxextend,percfree,timestamp) " +
                                "VALUES(?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)" ;
                        psmt = monitorConn.prepareStatement(insertQuery);

                        psmt.setString(1,resultSet.getString("TABLESPACE_NAME")) ;
                        psmt.setFloat(2,Float.parseFloat(resultSet.getString("File Count"))) ;
                        psmt.setFloat(3,Float.parseFloat(resultSet.getString("Size(MB)"))) ;
                        psmt.setFloat(4,Float.parseFloat(resultSet.getString("Free(MB)"))) ;
                        psmt.setFloat(5,Float.parseFloat(resultSet.getString("Used(MB)"))) ;
                        psmt.setFloat(6,Float.parseFloat(resultSet.getString("Max Ext(MB)"))) ;
                        psmt.setFloat(7,Float.parseFloat(resultSet.getString("% Free"))) ;
                        psmt.executeUpdate() ;
                        psmt.close() ;
                    }
                }


                // DATAFILES
                String getDatafiles = "SELECT file_name, file_id, tablespace_name, bytes, blocks, status, autoextensible, maxbytes, maxblocks, online_status" +
                                        " FROM dba_data_files;" ;
                resultSet = getStmt.executeQuery(getDatafiles);
                while(resultSet.next()) {

                    Statement stmt1 = monitorConn.createStatement();

                    String updateDatafiles = "UPDATE \"MONITOR\".\"DATAFILES\" " +
                            " SET bytes = " + Float.parseFloat(resultSet.getString("BYTES")) +
                            "," + " blocks = " + Float.parseFloat(resultSet.getString("BLOCKS")) +
                            "," + " status = " + resultSet.getString("STATUS") +
                            "," + " autoextensible = " + resultSet.getString("AUTOEXTENSIBLE") +
                            "," + " maxbytes = " + Float.parseFloat(resultSet.getString("MAXBYTES")) +
                            "," + " maxblocks = " + Float.parseFloat(resultSet.getString("MAXBLOCKS")) +
                            "," + " onlinestatus = " + resultSet.getString("ONLINE_STATUS") +
                            "," + " timestamp = CURRENT_TIMESTAMP" +
                            " WHERE filename = " + "'" + resultSet.getString("FILE_NAME") + "'";

                    int i;
                    i = stmt1.executeUpdate(updateDatafiles);

                    if(i==0) {

                        String insertDatafiles = "INSERT INTO \"MONITOR\".\"DATAFILES\" "
                                + "(filename,fileid,tablename,bytes,blocks,status,autoextensible,maxbytes,maxblocks,onlinestatus,timestamp) "
                                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)" ;
                        psmt = monitorConn.prepareStatement(insertDatafiles) ;


                        psmt.setString(1,resultSet.getString("FILE_NAME")) ;
                        psmt.setFloat(2,Float.parseFloat(resultSet.getString("FILE_ID"))) ;
                        psmt.setString(3,resultSet.getString("TABLESPACE_NAME")) ;
                        psmt.setFloat(4,Float.parseFloat(resultSet.getString("BYTES"))) ;
                        psmt.setFloat(5,Float.parseFloat(resultSet.getString("BLOCKS"))) ;
                        psmt.setString(6,resultSet.getString("STATUS")) ;
                        psmt.setString(7,resultSet.getString("AUTOEXTENSIBLE")) ;
                        psmt.setFloat(8,Float.parseFloat(resultSet.getString("MAXBYTES"))) ;
                        psmt.setFloat(9,Float.parseFloat(resultSet.getString("MAXBLOCKS"))) ;
                        psmt.setString(10,resultSet.getString("ONLINE_STATUS")) ;
                        psmt.executeUpdate();
                        psmt.close() ;
                    }

                }


                // USERS
                String users = "SELECT USERNAME, ACCOUNT_STATUS, COMMON, EXPIRY_DATE, DEFAULT_TABLESPACE, TEMPORARY_TABLESPACE, PROFILE, CREATED" +
                        " FROM dba_users;";
                resultSet = getStmt.executeQuery(users);

                while(resultSet.next()) {

                    Statement stmtUsers = monitorConn.createStatement();
                    String updateUser = " UPDATE \"MONITOR\".\"USERS\"" +
                            " SET accStatus = " + "'" + resultSet.getString("ACCOUNT_STATUS")+"'"+
                            "," + " common = " + resultSet.getString("COMMON") +
                            "," + " defaultTablespace = " + resultSet.getString("DEFAULT_TABLESPACE") +
                            "," + " tempTablespace = " + resultSet.getString("TEMPORARY_TABLESPACE") +
                            "," + " profile = " + resultSet.getString("PROFILE") +
                            "," + " timestamp = CURRENT_TIMESTAMP "  +
                            " WHERE username = "  + resultSet.getString("USERNAME") ;

                    int i=0;
                    i = stmtUsers.executeUpdate(updateUser);

                    if(i==0) {

                        users = "INSERT INTO \"MONITOR\".\"USERS\""
                                + " (username,accStatus,common,expiryDate,defaultTablespace,tempTablespace,profile,created,timestamp)"
                                + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP,?)" ;
                        psmt = monitorConn.prepareStatement(users) ;

                        psmt.setString(1,resultSet.getString("USERNAME")) ;
                        psmt.setString(2,resultSet.getString("ACCOUNT_STATUS")) ;
                        psmt.setString(3,resultSet.getString("COMMON")) ;
                        // caso não tenha expiry date
                        if(resultSet.getDate("EXPIRY_DATE") != null )
                            psmt.setDate(4,resultSet.getDate("EXPIRY_DATE")) ;
                        else
                            psmt.setNull(4, Types.TIMESTAMP);
                        psmt.setString(5,resultSet.getString("DEFAULT_TABLESPACE")) ;
                        psmt.setString(6,resultSet.getString("TEMPORARY_TABLESPACE")) ;
                        psmt.setString(7,resultSet.getString("PROFILE")) ;
                        psmt.setDate(8,resultSet.getDate("CREATED")) ;
                        
                        psmt.executeUpdate();
                        psmt.close() ;
                    }
                }

                // SGA
                String sga = "select SID, USER# USER_ID, USERNAME, SERIAL# SERIAL ,c.CURRENT_TIMESTAMP TIMESTAMP" +
                        " from v$session, (SELECT CURRENT_TIMESTAMP FROM dual) c " + " WHERE USERNAME IS NOT NULL" +
                        " order by 1" ;

                resultSet = getStmt.executeQuery(sga);

                while(resultSet.next()) {
                    if(!resultSet.getString("USERNAME").equals("MANAGER")) {

                        Statement stmt1 = monitorConn.createStatement();
                        String tbs1 = " UPDATE SESSIONS" +
                                " SET SERIAL = "+resultSet.getString("SERIAL")+
                                "," + " TIMESTAMP = CURRENT_TIMESTAMP "  +
                                " WHERE SID = "  + resultSet.getString("SID");


                        int i=0;
                        i = stmt1.executeUpdate(tbs1);

                        if(i==0) {

                            sga = "INSERT INTO SESSIONS (SID,USER_ID,USERNAME,SERIAL,TIMESTAMP)"
                                    + "VALUES(?, ?, ?, ?, CURRENT_TIMESTAMP)" ;
                            psmt = monitorConn.prepareStatement(sga) ;

                            psmt.setString(1,resultSet.getString("SID")) ;
                            psmt.setFloat(2,Float.parseFloat(resultSet.getString("USER_ID")));
                            psmt.setString(3,resultSet.getString("USERNAME"));
                            psmt.setString(4,resultSet.getString("SERIAL"));
                            psmt.executeUpdate();
                        }
                    }
                }

                // IO

                String io = "select c.CURRENT_TIMESTAMP  TIMESTAMP ,  v1.mem FREE_MEMORY, v2.writes WRITES , v3.rrs READS" +
                        " from ( select sum(bytes)/1024 mem " +
                        " from v$sgastat where name = 'free memory') v1, " +
                        " (select SUM(VALUE) writes " +
                        " from ( select metric_name,begin_time,end_time,value " +
                        " from v$sysmetric_history " +
                        " where metric_name = 'Physical Writes Per Sec' " +
                        " order by 2 )) v2, " +
                        " (select SUM(VALUE) rrs " +
                        " from ( select metric_name,begin_time,end_time,value " +
                        " from v$sysmetric_history " +
                        " where metric_name = 'Physical Reads Per Sec' " +
                        " order by 2 )) v3 , " +
                        " (SELECT CURRENT_TIMESTAMP FROM dual) c " ;
                resultSet = getStmt.executeQuery(io);

                while(resultSet.next()) {


                    io = "INSERT INTO IO (TIMESTAMP , WRITES, READS , FREE_MEMORY)"
                            + " VALUES(CURRENT_TIMESTAMP, ?, ?, ?)";
                    psmt = monitorConn.prepareStatement(io) ;

                    psmt.setFloat(1,Float.parseFloat(resultSet.getString("WRITES"))) ;
                    psmt.setFloat(2,Float.parseFloat(resultSet.getString("READS"))) ;
                    psmt.setFloat(3,Float.parseFloat(resultSet.getString("FREE_MEMORY"))) ;
                    psmt.executeUpdate();
                    psmt.close() ;
                }


                Thread.sleep(5000);
            }

        }catch(ClassNotFoundException e){
            System.out.println(e) ;
        }catch(SQLException e){
            System.out.println(e) ;
        } catch (InterruptedException ex) {
            Logger.getLogger(Collector.class.getName()).log(Level.SEVERE, null, ex);
        }




    }

}
