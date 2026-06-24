package com.two.backend.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Keeps existing local databases compatible with newer application versions.
 */
@Configuration
public class DatabaseCompatibilityConfig {

    @Bean
    public ApplicationRunner ensureConsultationReadsTable(JdbcTemplate jdbcTemplate) {
        return args -> {
            jdbcTemplate.execute("""
                create table if not exists consultation_reads (
                    plan_id bigint not null,
                    participant_user_id bigint not null,
                    reader_role tinyint not null,
                    reader_user_id bigint not null,
                    last_read_at timestamp not null default current_timestamp,
                    primary key (plan_id, participant_user_id, reader_role, reader_user_id),
                    key idx_consultation_reads_reader (reader_role, reader_user_id),
                    key idx_consultation_reads_participant (participant_user_id)
                ) engine=InnoDB default charset=utf8mb4
                """);
            jdbcTemplate.update("update users set email = ? where name = ?", "nie-ningbo@zhimingsoft.com", "聂宁波");
            jdbcTemplate.update("update users set email = ? where name = ?", "tang-xiaosong@zhimingsoft.com", "唐笑松");
            jdbcTemplate.update("update users set email = ? where name = ?", "shi-jian@zhimingsoft.com", "史简");
        };
    }
}
