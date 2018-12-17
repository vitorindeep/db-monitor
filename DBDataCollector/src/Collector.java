import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.lang.String ;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
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
                        "FROM " +
                        "   (SELECT tablespace_name," +
                        "   SUM (bytes) / (1024 * 1024) \"FREE(MB)\"" +
                        "   FROM dba_free_space" +
                        "    GROUP BY tablespace_name) fr," +
                        "(SELECT tablespace_name, SUM(bytes) / (1024 * 1024) \"SIZE(MB)\", COUNT(*)" +
                        "\"File Count\", SUM(maxbytes) / (1024 * 1024) \"MAX_EXT\"" +
                        "FROM dba_data_files" +
                        "GROUP BY tablespace_name) df," +
                        "(SELECT tablespace_name" +
                        "FROM dba_tablespaces) ts" +
                        "WHERE fr.tablespace_name = df.tablespace_name (+)" +
                        "AND fr.tablespace_name = ts.tablespace_name (+)" +
                        "ORDER BY \"% Free\" desc;";
                Statement getStmt = sysConn.createStatement();
                ResultSet resultSet = getStmt.executeQuery(getTablespaces);

                while(resultSet.next()) {

                    Statement monitorStmt = monitorConn.createStatement();
                    String updateQuery = "UPDATE \"MONITOR.TABLESPACES\" " +
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
                String getDatafiles = "SELECT Substr(df.file_name,1,500) NAME_DF, " +
                        "Substr(df.tablespace_name,1,40) NAME_TB, " +
                        "Round(df.bytes/1024/1024,0) FILE_SIZE, " +
                        "decode(e.used_bytes,NULL,0,Round(e.used_bytes/1024/1024,0)) USED_SIZE, " +
                        "decode(f.free_bytes,NULL,0,Round(f.free_bytes/1024/1024,0)) FREE_SIZE, " +
                        "c.CURRENT_TIMESTAMP TIMESTAMP, " +
                        "d.CURRENT_TIMESTAMP TIMESTAMP_FK " +
                        " FROM DBA_DATA_FILES DF, " +
                        " (SELECT file_id, " +
                        " Sum(Decode(bytes,NULL,0,bytes)) used_bytes " +
                        " FROM dba_extents " +
                        " GROUP by file_id) E, " +
                        " (SELECT Max(bytes) free_bytes, file_id " +
                        " FROM dba_free_space " +
                        " GROUP BY file_id) f, " +
                        " (SELECT CURRENT_TIMESTAMP FROM dual) c, " +
                        " (SELECT CURRENT_TIMESTAMP FROM dual) d " +
                        " WHERE e.file_id (+) = df.file_id " +
                        " AND df.file_id = f.file_id (+) " +
                        " ORDER BY df.tablespace_name,df.file_name" ;

                resultSet = getStmt.executeQuery(getDatafiles);
                while(resultSet.next()) {

                    Statement stmt1 = monitorConn.createStatement();

                    String tbs1 = "UPDATE DATAFILE " +
                            " SET USED_SIZE = " + resultSet.getString("USED_SIZE") +
                            "," + " FILE_SIZE = " + resultSet.getString("FILE_SIZE") +
                            "," + " FREE_SIZE = " + resultSet.getString("FREE_SIZE") +
                            "," + " TIMESTAMP = CURRENT_TIMESTAMP" +
                            " WHERE NAME_DF = " + "'" + resultSet.getString("NAME_DF") + "'";

                    int i;
                    i = stmt1.executeUpdate(tbs1);

                    if(i==0) {

                        getDatafiles = "INSERT INTO DATAFILE (NAME_DF,NAME_TB,FILE_SIZE,USED_SIZE,FREE_SIZE,TIMESTAMP) "
                                + "VALUES(?, ?, ?, ?, ?, CURRENT_TIMESTAMP)" ;
                        psmt = monitorConn.prepareStatement(getDatafiles) ;


                        psmt.setString(1,resultSet.getString("NAME_DF")) ;
                        psmt.setString(2,resultSet.getString("NAME_TB")) ;
                        psmt.setFloat(3,Float.parseFloat(resultSet.getString("FILE_SIZE"))) ;
                        if(resultSet.getString("USED_SIZE") != null )
                            psmt.setFloat(4,Float.parseFloat(resultSet.getString("USED_SIZE"))) ;
                        else
                            psmt.setNull(4,java.sql.Types.NUMERIC);
                        if(resultSet.getString("FREE_SIZE") != null )
                            psmt.setFloat(5,Float.parseFloat(resultSet.getString("FREE_SIZE"))) ;
                        else psmt.setNull(5,java.sql.Types.NUMERIC);
                        psmt.executeUpdate();
                        psmt.close() ;
                    }

                }


                // USERS

                String users = "select USER_ID,USERNAME,ACCOUNT_STATUS,"+
                        " DEFAULT_TABLESPACE DEFAULT_TB,TEMPORARY_TABLESPACE TEMP_TB,CREATED,c.CURRENT_TIMESTAMP " +
                        " from dba_users, " +
                        " (SELECT CURRENT_TIMESTAMP FROM dual) c " +
                        " where ACCOUNT_STATUS = 'OPEN' "+
                        " order by 1" ;
                resultSet = getStmt.executeQuery(users);

                while(resultSet.next()) {

                    Statement stmt3 = monitorConn.createStatement();
                    String tbs3 = " UPDATE USERS" +
                            " SET ACCOUNT_STATUS = "+"'"+resultSet.getString("ACCOUNT_STATUS")+"'"+
                            "," + " TIMESTAMP = CURRENT_TIMESTAMP "  +
                            " WHERE USER_ID = "  + resultSet.getString("USER_ID") ;
                    int i=0;
                    i = stmt3.executeUpdate(tbs3);
                    if(i==0) {
                        users = "INSERT INTO USERS (USER_ID,USERNAME,ACCOUNT_STATUS,TEMP_TB,CREATED,TIMESTAMP,NAME_TB) "
                                + "VALUES(?, ?, ?, ?, ?, CURRENT_TIMESTAMP,?)" ;
                        psmt = monitorConn.prepareStatement(users) ;

                        psmt.setFloat(1,Float.parseFloat(resultSet.getString("USER_ID"))) ;
                        psmt.setString(2,resultSet.getString("USERNAME")) ;
                        psmt.setString(3,resultSet.getString("ACCOUNT_STATUS")) ;
                        psmt.setString(4,resultSet.getString("TEMP_TB")) ;
                        psmt.setDate(5,resultSet.getDate("CREATED")) ;
                        psmt.setString(6,resultSet.getString("DEFAULT_TB"));
                        psmt.executeUpdate();
                        psmt.close() ;
                    }
                }

                // SESSIONS

                String ses = "select SID, USER# USER_ID, USERNAME, SERIAL# SERIAL ,c.CURRENT_TIMESTAMP TIMESTAMP" +
                        " from v$session, (SELECT CURRENT_TIMESTAMP FROM dual) c " + " WHERE USERNAME IS NOT NULL" +
                        " order by 1" ;

                resultSet = getStmt.executeQuery(ses);

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

                            ses = "INSERT INTO SESSIONS (SID,USER_ID,USERNAME,SERIAL,TIMESTAMP)"
                                    + "VALUES(?, ?, ?, ?, CURRENT_TIMESTAMP)" ;
                            psmt = monitorConn.prepareStatement(ses) ;

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
