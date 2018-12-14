-- Throughput: The amount of work the system is doing per unit of time.
-- Throughput is usually recorded as an absolute number.
-- Examples include the number of transactions or queries per second.

-- Success: Represents the percentage of work that was executed successfully,
-- i.e. the number of successful queries.

-- Error: Captures the number of erroneous results, usually expressed as a rate of errors per unit of time. This yields errors per unit of work.
-- Error metrics are often captured separately from success metrics when there are several potential sources of error, some of which are more serious or actionable than others.

-- Performance: Quantifies how efficiently a component is doing its work.
-- The most common performance metric is latency, which represents the time required to complete a unit of work.
-- Latency can be expressed as an average or as a percentile, such as “99% of requests returned within 0.1s.”

-- V$ Dynamic Performance Views
-- ***GENERAL***
--V$SYSSTAT
--V$SESSION
--V$SESSTAT
--V$PROCESS
--V$SQL
--V$SQL_PLAN
--V$SQL_PLAN_STATISTICS

-- ***CPU/WAIT***
--V$SYS_TIME_MODEL
--V$SESS_TIME_MODEL

-- TABLESPACES INFO
SELECT 
   ts.tablespace_name, "File Count",
   TRUNC("SIZE(MB)", 2) "Size(MB)",
   TRUNC(fr."FREE(MB)", 2) "Free(MB)",
   TRUNC("SIZE(MB)" - "FREE(MB)", 2) "Used(MB)",
   df."MAX_EXT" "Max Ext(MB)",
   (fr."FREE(MB)" / df."SIZE(MB)") * 100 "% Free"
FROM 
   (SELECT tablespace_name,
   SUM (bytes) / (1024 * 1024) "FREE(MB)"
   FROM dba_free_space
    GROUP BY tablespace_name) fr,
(SELECT tablespace_name, SUM(bytes) / (1024 * 1024) "SIZE(MB)", COUNT(*)
"File Count", SUM(maxbytes) / (1024 * 1024) "MAX_EXT"
FROM dba_data_files
GROUP BY tablespace_name) df,
(SELECT tablespace_name
FROM dba_tablespaces) ts
WHERE fr.tablespace_name = df.tablespace_name (+)
AND fr.tablespace_name = ts.tablespace_name (+)
ORDER BY "% Free" desc;

select * from dba_tablespaces;
  
-- DATAFILES INFO
SELECT * FROM dba_data_files;

-- USERS
SELECT USERNAME, ACCOUNT_STATUS, COMMON, EXPIRY_DATE, DEFAULT_TABLESPACE, TEMPORARY_TABLESPACE, PROFILE, CREATED
FROM dba_users;

-- PGA E SGA MEMORY
-- sga
show sga;
select sum(bytes) from v$sgastat
    where POOL='shared pool' and NOT NAME='free memory';
select * from v$sysstat
    where NAME LIKE 'session%';
select * from v$sesstat;
    where NAME LIKE 'session%';
select * from v$sga;
-- pga
SELECT name, value FROM v$pgastat
    WHERE NAME='total PGA inuse';
SELECT ROUND(SUM(pga_used_mem)/(1024*1024),2) PGA_USED_MB FROM v$process;

-- SESSIONS
select * from v$session
    where username IS NOT NULL;
    
-- CPU
select 
   ss.username,
   se.SID,
   VALUE/100 cpu_usage_seconds
from
   v$session ss, 
   v$sesstat se, 
   v$statname sn
where
   se.STATISTIC# = sn.STATISTIC#
and
   NAME like '%CPU used by this session%'
and
   se.SID = ss.SID
and 
   ss.status='ACTIVE'
and 
   ss.username is not null
order by VALUE desc;
select * from v$sesstat;

