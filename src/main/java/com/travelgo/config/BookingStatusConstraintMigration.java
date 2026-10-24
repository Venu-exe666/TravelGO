package com.travelgo.config;

import com.travelgo.enums.BookingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Keeps the SQL Server enum check constraint in sync with {@link BookingStatus}.
 * Hibernate can create enum constraints, but ddl-auto=update does not expand an
 * existing constraint when a new Java enum value is introduced.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BookingStatusConstraintMigration implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BookingStatusConstraintMigration.class);
    private static final String TABLE_NAME = "bookings";
    private static final String COLUMN_NAME = "booking_status";
    private static final String CONSTRAINT_NAME = "CK_bookings_booking_status";

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public BookingStatusConstraintMigration(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            if (productName == null || !productName.toLowerCase(Locale.ROOT).contains("microsoft sql server")) {
                return;
            }
        }

        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys.tables WHERE object_id = OBJECT_ID(N'dbo.bookings')",
                Integer.class);
        if (tableExists == null || tableExists == 0) {
            return;
        }

        List<String> constraintNames = jdbcTemplate.queryForList("""
                SELECT cc.name
                FROM sys.check_constraints cc
                JOIN sys.columns c
                  ON cc.parent_object_id = c.object_id
                 AND cc.parent_column_id = c.column_id
                WHERE cc.parent_object_id = OBJECT_ID(N'dbo.bookings')
                  AND c.name = 'booking_status'
                """, String.class);

        boolean alreadyCurrent = constraintNames.stream().anyMatch(name -> {
            String definition = jdbcTemplate.queryForObject(
                    "SELECT definition FROM sys.check_constraints WHERE name = ? AND parent_object_id = OBJECT_ID(N'dbo.bookings')",
                    String.class,
                    name);
            return definition != null && Arrays.stream(BookingStatus.values())
                    .allMatch(status -> definition.contains(status.name()));
        });
        if (alreadyCurrent) {
            return;
        }

        for (String name : constraintNames) {
            jdbcTemplate.execute("ALTER TABLE dbo.bookings DROP CONSTRAINT " + quoteIdentifier(name));
        }

        String allowedValues = Arrays.stream(BookingStatus.values())
                .map(status -> "'" + status.name().replace("'", "''") + "'")
                .collect(Collectors.joining(", "));
        jdbcTemplate.execute("ALTER TABLE dbo.bookings WITH CHECK ADD CONSTRAINT "
                + quoteIdentifier(CONSTRAINT_NAME)
                + " CHECK (booking_status IN (" + allowedValues + "))");
        jdbcTemplate.execute("ALTER TABLE dbo.bookings CHECK CONSTRAINT " + quoteIdentifier(CONSTRAINT_NAME));
        log.info("Updated dbo.bookings.booking_status constraint for statuses: {}", allowedValues);
    }

    private String quoteIdentifier(String identifier) {
        return "[" + identifier.replace("]", "]]" ) + "]";
    }
}
