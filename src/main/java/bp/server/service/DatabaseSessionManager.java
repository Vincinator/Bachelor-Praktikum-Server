package bp.server.service;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class DatabaseSessionManager {

    private static DatabaseSessionManager INSTANCE;

    private static SessionFactory sessionFactory;

    static {
        try {
            StandardServiceRegistry standardRegistry =
                    new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml").build();
            Metadata metaData =
                    new MetadataSources(standardRegistry).getMetadataBuilder().build();
            sessionFactory = metaData.getSessionFactoryBuilder().build();
        } catch (Throwable th) {
            System.err.println("Enitial SessionFactory creation failed" + th);
            throw new ExceptionInInitializerError(th);
        }
    }

    public DatabaseSessionManager(){

    }


    public static DatabaseSessionManager instance(){
        if(INSTANCE == null){
            INSTANCE = new DatabaseSessionManager();
        }
        return INSTANCE;
    }

    public SessionFactory getSessionFactory(){
        return sessionFactory;
    }

}
