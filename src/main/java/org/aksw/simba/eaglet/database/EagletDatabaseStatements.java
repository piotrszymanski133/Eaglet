package org.aksw.simba.eaglet.database;

import java.io.Closeable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.aksw.gerbil.database.IntegerRowMapper;
import org.aksw.gerbil.database.StringRowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class EagletDatabaseStatements implements Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(EagletDatabaseStatements.class);
	private final static String INSERT_USER = "INSERT INTO Users (name) VALUES (:userName)";
	private final static String INSERT_DOCUMENT = "INSERT INTO Documents (userId, documentUri,fileName) VALUES (:userId, :documentUri, :fileName)";
	private final static String GET_USER = "SELECT id FROM Users WHERE name=:userName";
	private final static String GET_DOCUMENT_FILES = "SELECT fileName FROM Documents WHERE documentUri=:documentUri";
	private final static String GET_DOCUMENTS_REVIEWED_BY_USER = "SELECT documentUri FROM Documents WHERE userId=:userId";

	private final static String SHUTDOWN = "SHUTDOWN";

	public static final int USER_NOT_FOUND = -1;

	private final NamedParameterJdbcTemplate template;

	public EagletDatabaseStatements(DataSource dataSource) {
		this.template = new NamedParameterJdbcTemplate(dataSource);
	}

	public int getUser(String userName) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("userName", userName);
		List<Integer> result = this.template.query(GET_USER, parameters, new IntegerRowMapper());
		if (result.size() >= 1) {
			return result.get(0);
		} else {
			return USER_NOT_FOUND;
		}
	}

	public List<String> getDocumentFilenames(String documentUri) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("documentUri", documentUri);
		List<String> result = this.template.query(GET_DOCUMENT_FILES, parameters, new StringRowMapper());
		if (result.size() >= 1) {
			return result;
		} else {
			return null;
		}
	}

	public List<String> getDocumentUser(int userId) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("userId", userId);
		List<String> result = this.template.query(GET_DOCUMENTS_REVIEWED_BY_USER, parameters, new StringRowMapper());
		if (result.size() >= 1) {
			return result;
		} else {
			return new ArrayList<String>(0);
		}
	}

	public void addUser(String name) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();

		parameters.addValue("userName", name);
		this.template.update(INSERT_USER, parameters);

	}

	public void addDocument(int userid, String documentUri, String fileName) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("userId", userid);
		parameters.addValue("documentUri", documentUri);
		parameters.addValue("fileName", fileName);

		this.template.update(INSERT_DOCUMENT, parameters);

	}

	@Override
	public void close() {
		this.template.execute(SHUTDOWN, new PreparedStatementCallback<Object>() {
			@Override
			public Object doInPreparedStatement(PreparedStatement arg0) throws SQLException, DataAccessException {
				// nothing to do
				return null;
			}
		});
	}
}
