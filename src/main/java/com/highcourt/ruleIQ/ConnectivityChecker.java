package com.highcourt.ruleIQ;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class ConnectivityChecker implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ConnectivityChecker.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private KafkaAdmin kafkaAdmin;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        checkMySQLConnection();
        checkRedisConnection();
        checkKafkaConnection();
    }

    private void checkMySQLConnection() throws InterruptedException {
        logger.info("Checking MySQL connectivity");
        int retries = 5;
        while (retries > 0) {
            try {
                jdbcTemplate.execute("SELECT 1");
                logger.info("MySQL connection is OK.");
                return;
            } catch (Exception e) {
                retries--;
                if (retries == 0) {
                    throw new IllegalStateException("MySQL is not available", e);
                }
                Thread.sleep(2000); // Wait 2 seconds before retrying
            }
        }
    }

    private void checkRedisConnection() {
        logger.info("Checking Redis connectivity");
        try {
            redisConnectionFactory.getConnection().ping();
            logger.info("Redis connection is OK.");
        } catch (Exception e) {
            logger.error("Failed to connect to Redis: {}", e.getMessage());
            System.exit(1);
        }
    }

    private void checkKafkaConnection() {
        logger.info("Checking Kafka connectivity");
        try {
            Map<String, Object> configs = kafkaAdmin.getConfigurationProperties();
            try (AdminClient adminClient = AdminClient.create(configs)) {
                ListTopicsResult topics = adminClient.listTopics();
                Set<String> names = topics.names().get(10, TimeUnit.SECONDS);
                logger.info("Kafka connection is OK. Available topics: {}", names);
            }
        } catch (Exception e) {
            System.err.println("Failed to connect to Kafka: " + e.getMessage());
            throw new IllegalStateException("Kafka is not available", e);
        }
    }

}
