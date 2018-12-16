-- SGA HISTORIC
CREATE OR REPLACE TRIGGER add_to_SGA_HIST
AFTER UPDATE ON SGA FOR EACH ROW
BEGIN
    INSERT INTO SGA_HIST
        (name,total,timestamp)
    VALUES
        (:OLD.name,:OLD.total,:OLD.timestamp);
END ;

-- PGA HISTORIC
CREATE OR REPLACE TRIGGER add_to_PGA_HIST
AFTER UPDATE ON PGA FOR EACH ROW
BEGIN
    INSERT INTO PGA_HIST
        (usedPga,timestamp)
    VALUES
        (:OLD.usedPga,:OLD.timestamp);
END ;

-- CPU HISTORIC
CREATE OR REPLACE TRIGGER add_to_CPU_HIST
AFTER UPDATE ON CPU FOR EACH ROW
BEGIN
    INSERT INTO CPU_HIST
        (username,cpuUsage,timestamp)
    VALUES
        (:OLD.username,:OLD.cpuUsage,:OLD.timestamp);
END ;