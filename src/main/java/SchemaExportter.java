
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

public class SchemaExportter {

  public static void main(String[] args) {
//    Configuration config = new Configuration();
//
//    Properties properties = new Properties();
//
//    properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
    // properties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
    // properties.put("hibernate.connection.url",
    // "jdbc:mysql://localhost:3306/fni");
    // properties.put("hibernate.connection.username", "username");
    // properties.put("hibernate.connection.password", "password");
    // properties.put("hibernate.connection.driver_class", "org.mysql.Driver");
//    properties.put("hibernate.show_sql", "true");
//    config.setProperties(properties);
    
    MetadataSources metadata = new MetadataSources(new StandardServiceRegistryBuilder()
        .applySetting("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
                .build());

    metadata.addAnnotatedClass(Identifier.class);
    
    MetadataBuilderImpl builder = new MetadataBuilderImpl(metadata);
    
    SchemaExport schemaExport = new SchemaExport(builder.build());
    schemaExport.setDelimiter(";");
    schemaExport.setOutputFile("/tmp/gen.sql");
        
    schemaExport.create(true, false);
  }

}

/**
alter table Identifier drop key UK1js2b5xvk9obx1f7nf5fxmhlx;
alter table Identifier drop key UKo0se1m5aqhec7bvoynayxcw0s;
alter table Identifier add organizationKuntaApiId varchar(255);
alter table Identifier add constraint UKs3bc293w7i082jc22r4h35q6l unique (organizationKuntaApiId, type, source, sourceId);
alter table Identifier add constraint UKsgtxhd5ma9w839iwbuopq04gr unique (organizationKuntaApiId, type, source, kuntaApiId);
**/