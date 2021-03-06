-- Generated by Oracle SQL Developer REST Data Services 17.3.0.271.2323
-- Exported REST Definitions from ORDS Schema Version 3.0.12.263.15.32
-- Schema: MONITOR   Date: Fri Dec 21 18:00:26 GMT 2018
--
BEGIN
  ORDS.ENABLE_SCHEMA(
      p_enabled             => TRUE,
      p_schema              => 'MONITOR',
      p_url_mapping_type    => 'BASE_PATH',
      p_url_mapping_pattern => 'monitor',
      p_auto_rest_auth      => FALSE);    

  ORDS.DEFINE_MODULE(
      p_module_name    => 'CPU',
      p_base_path      => '/cpu/',
      p_items_per_page =>  25,
      p_status         => 'PUBLISHED',
      p_comments       => NULL);      
  ORDS.DEFINE_TEMPLATE(
      p_module_name    => 'CPU',
      p_pattern        => 'cpu',
      p_priority       => 0,
      p_etag_type      => 'HASH',
      p_etag_query     => NULL,
      p_comments       => NULL);
  ORDS.DEFINE_HANDLER(
      p_module_name    => 'CPU',
      p_pattern        => 'cpu',
      p_method         => 'GET',
      p_source_type    => 'json/collection',
      p_items_per_page =>  25,
      p_mimes_allowed  => '',
      p_comments       => NULL,
      p_source         => 
'select * from cpu
union
select * from 
    (select * from cpu_hist order by "timestamp" desc)
        where ROWNUM <= 4*(select count(*) from cpu)'
      );

  ORDS.DEFINE_MODULE(
      p_module_name    => 'datafiles',
      p_base_path      => '/datafiles/',
      p_items_per_page =>  25,
      p_status         => 'PUBLISHED',
      p_comments       => NULL);      
  ORDS.DEFINE_TEMPLATE(
      p_module_name    => 'datafiles',
      p_pattern        => 'df',
      p_priority       => 0,
      p_etag_type      => 'HASH',
      p_etag_query     => NULL,
      p_comments       => NULL);
  ORDS.DEFINE_HANDLER(
      p_module_name    => 'datafiles',
      p_pattern        => 'df',
      p_method         => 'GET',
      p_source_type    => 'json/collection',
      p_items_per_page =>  25,
      p_mimes_allowed  => '',
      p_comments       => NULL,
      p_source         => 
'select * from monitor.datafiles'
      );

  ORDS.DEFINE_MODULE(
      p_module_name    => 'PGA',
      p_base_path      => '/pga/',
      p_items_per_page =>  25,
      p_status         => 'PUBLISHED',
      p_comments       => NULL);      
  ORDS.DEFINE_TEMPLATE(
      p_module_name    => 'PGA',
      p_pattern        => 'pga',
      p_priority       => 0,
      p_etag_type      => 'HASH',
      p_etag_query     => NULL,
      p_comments       => NULL);
  ORDS.DEFINE_HANDLER(
      p_module_name    => 'PGA',
      p_pattern        => 'pga',
      p_method         => 'GET',
      p_source_type    => 'json/collection',
      p_items_per_page =>  25,
      p_mimes_allowed  => '',
      p_comments       => NULL,
      p_source         => 
'select * from pga
union
select * from 
    (select * from pga_hist order by "timestamp" desc)
        where ROWNUM <= 4*(select count(*) from pga)'
      );

  ORDS.DEFINE_MODULE(
      p_module_name    => 'sessions',
      p_base_path      => '/sessions/',
      p_items_per_page =>  25,
      p_status         => 'PUBLISHED',
      p_comments       => NULL);      
  ORDS.DEFINE_TEMPLATE(
      p_module_name    => 'sessions',
      p_pattern        => 'sessions',
      p_priority       => 0,
      p_etag_type      => 'HASH',
      p_etag_query     => NULL,
      p_comments       => NULL);
  ORDS.DEFINE_HANDLER(
      p_module_name    => 'sessions',
      p_pattern        => 'sessions',
      p_method         => 'GET',
      p_source_type    => 'json/collection',
      p_items_per_page =>  25,
      p_mimes_allowed  => '',
      p_comments       => NULL,
      p_source         => 
'select * from monitor.sessions'
      );

  ORDS.DEFINE_MODULE(
      p_module_name    => 'SGA',
      p_base_path      => '/sga/',
      p_items_per_page =>  25,
      p_status         => 'PUBLISHED',
      p_comments       => NULL);      
  ORDS.DEFINE_TEMPLATE(
      p_module_name    => 'SGA',
      p_pattern        => 'sga',
      p_priority       => 0,
      p_etag_type      => 'HASH',
      p_etag_query     => NULL,
      p_comments       => NULL);
  ORDS.DEFINE_HANDLER(
      p_module_name    => 'SGA',
      p_pattern        => 'sga',
      p_method         => 'GET',
      p_source_type    => 'json/collection',
      p_items_per_page =>  25,
      p_mimes_allowed  => '',
      p_comments       => NULL,
      p_source         => 
'select * from sga
union
select * from 
    (select * from sga_hist order by "timestamp" desc)
        where ROWNUM <= 4*(select count(*) from sga)'
      );

  ORDS.DEFINE_MODULE(
      p_module_name    => 'tablespaces',
      p_base_path      => '/tablespaces/',
      p_items_per_page =>  25,
      p_status         => 'PUBLISHED',
      p_comments       => NULL);      
  ORDS.DEFINE_TEMPLATE(
      p_module_name    => 'tablespaces',
      p_pattern        => 'tablespaces',
      p_priority       => 0,
      p_etag_type      => 'HASH',
      p_etag_query     => NULL,
      p_comments       => NULL);
  ORDS.DEFINE_HANDLER(
      p_module_name    => 'tablespaces',
      p_pattern        => 'tablespaces',
      p_method         => 'GET',
      p_source_type    => 'json/collection',
      p_items_per_page =>  25,
      p_mimes_allowed  => '',
      p_comments       => NULL,
      p_source         => 
'select * from monitor.tablespaces'
      );

  ORDS.DEFINE_MODULE(
      p_module_name    => 'users',
      p_base_path      => '/users/',
      p_items_per_page =>  25,
      p_status         => 'PUBLISHED',
      p_comments       => NULL);      
  ORDS.DEFINE_TEMPLATE(
      p_module_name    => 'users',
      p_pattern        => 'usr',
      p_priority       => 0,
      p_etag_type      => 'HASH',
      p_etag_query     => NULL,
      p_comments       => NULL);
  ORDS.DEFINE_HANDLER(
      p_module_name    => 'users',
      p_pattern        => 'usr',
      p_method         => 'GET',
      p_source_type    => 'json/collection',
      p_items_per_page =>  25,
      p_mimes_allowed  => '',
      p_comments       => NULL,
      p_source         => 
'select * from monitor.users'
      );


  COMMIT; 
END;