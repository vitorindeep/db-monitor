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


SELECT * FROM V$SESSTAT;


SELECT * FROM DBA_TABLESPACES;