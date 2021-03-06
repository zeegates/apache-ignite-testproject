package ignite.service

import ignite.model.Person
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.IgniteException
import org.apache.ignite.lifecycle.LifecycleBean
import org.apache.ignite.lifecycle.LifecycleEventType
import org.apache.ignite.resources.IgniteInstanceResource
import org.h2.jdbcx.JdbcConnectionPool
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper

import java.sql.ResultSet
import java.sql.SQLException

class CacheLoader implements LifecycleBean {

    @IgniteInstanceResource
    transient Ignite ignite

    transient JdbcTemplate jdbcTemplate

    CacheLoader() {
        jdbcTemplate = new JdbcTemplate(JdbcConnectionPool.create("jdbc:h2:tcp://localhost/~/test", "sa", ""))
    }

    @Override
    void onLifecycleEvent(LifecycleEventType evt) throws IgniteException {

        if (evt == LifecycleEventType.AFTER_NODE_START) {
            IgniteCache<Long, Person> cache = ignite.cache("person")

            def persons = jdbcTemplate.query("select * from persons", new RowMapper<Person>() {

                @Override
                Person mapRow(final ResultSet rs, final int rowNum) throws SQLException {
                    new Person(
                            id: rs.getLong(1),
                            balance: rs.getLong(2),
                            type: rs.getLong(3)
                    )
                }
            })

            persons.each {
                cache.put(it.id, it)
            }
        }

    }
}
